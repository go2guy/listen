#!/usr/bin/env python
try:
    import sys, os, re, md5, base64, datetime, subprocess, socket, zlib, rpm
    from optparse import OptionParser, TitledHelpFormatter
    from StringIO import StringIO

except ImportError, e:
    raise "Unable to import required module: " + str(e)

# Generic packfile object
class packfile(object):
    name = None
    md5sum = None
    contents64 = None
    contents = None

    def __init__(self, name, md5sum, contents64):
        self.name = name
        self.md5sum = md5sum
        self.contents64 = contents64
        self.contents = []

        # Decode and write to temporary in-memory file
        decompobj = zlib.decompressobj()
        for line64 in self.contents64:
            self.contents.append(decompobj.decompress(base64.b64decode(line64)))
        self.contents.append(decompobj.flush())

    # Unpacks packfile to deistination diretory based on the file extension
    def unpack(self):
        # Create the destination directory if necessary.
        if not os.path.exists(os.path.dirname(self.name)):
            os.makedirs(os.path.dirname(self.name))

        outfile = open(self.name, "w+")
        outfile.write("".join(self.contents))
        print("Extracted [ %s ]." % self.name)

        outfile.seek(0)
        newcontents = outfile.readlines()
        outfile.close()

        if not self.md5sum == md5.new("".join(newcontents)).hexdigest():
            raise "Unpacked file [ %s ] failed md5sum verification." % outfile.name


def load():
    global packfiles


def default(sipServer):
    # Should be 3 packs (uia, listen, and Realize)
    uiapkg = None
    listenpkg = None
    realizepkg = None
    defaultsup = None

    reuiapkg = re.compile("/uia-")
    relistenpkg = re.compile("/listen-")
    rerealizepkg = re.compile("/Realize-")
    redefaultsup = re.compile("/default.sup")
    for pkgfile in packfiles:
        if reuiapkg.search(pkgfile.name) != None:
            uiapkg = pkgfile.name

        elif relistenpkg.search(pkgfile.name) != None:
            listenpkg = pkgfile.name

        elif rerealizepkg.search(pkgfile.name) != None:
            realizepkg = pkgfile.name

        elif redefaultsup.search(pkgfile.name) != None:
            defaultsup = pkgfile.name

    # If any package do not exist, exit with error
    if uiapkg == None or listenpkg == None or realizepkg == None or defaultsup == None:
        sys.exit("Unable to find required packages in bundle.")

    # Insert sipserver sup line if it was passed in on the command line.
    if sipServer != None and sipServer != "":
        supfile = open(defaultsup, "a")
        supfile.write("CCVXML /interact/apps/spotbuild/listen_conference/root.vxml~update~sipURL~%s~SIP~var~expr~1~server name" % sipServer)
        supfile.close()

    # Create the command that will be used to install listen and Realize
    listeninst = ["/interact/packages/iiInstall.sh", "-i", "--noinput", "--replacefiles", "--replacepkgs", listenpkg, "spotbuild-vip", "ivrserver", "webserver"]
    realizeinst = ["/interact/packages/iiInstall.sh", "-i", "--noinput", "--replacefiles", "--replacepkgs", realizepkg, "iiweb", "tomcat", "tomcat-native", "realize"]

    # Only use the default sup if the rpm is not currently installed.
    for rpmpkg, instcmd in ([listenpkg, listeninst], [realizepkg, realizeinst]):
        installed = False

        # First query the name of the rpm
        transactionSet = rpm.TransactionSet()
        tmphdr = transactionSet.hdrFromFdno(os.open(rpmpkg, os.O_RDONLY))
        rpmname = tmphdr[rpm.RPMTAG_NAME]

        # Then query all installed rpms and search for the name
        transactionSet = rpm.TransactionSet()
        matchIterator = transactionSet.dbMatch()
        for tmphdr in matchIterator:
            if rpmname == tmphdr[rpm.RPMTAG_NAME]:
                installed = True

        if not installed:
                instcmd.insert(2, "--supfile")
                instcmd.insert(3, defaultsup)

    # Install uia
    run(["rpm", "-Uvh", "--replacepkgs", uiapkg])

    # Clone install command and insert a --test. Then run it
    testlisteninst = list(listeninst)
    testlisteninst.insert(2, "--test")
    run(testlisteninst)

    # Clone install command and insert a --test. Then run it
    testrealizeinst = list(realizeinst)
    testrealizeinst.insert(2, "--test")
    run(testrealizeinst)

    # Install listen and Realize packages
    run(listeninst)
    run(realizeinst)


def start():
    # Always make sure httpd is running so docs are available.
    run(["service", "httpd", "start"])

    # Only try to start things if a license file exists
    if not os.path.exists('/interact/master/.iiXmlLicense'):
        print("Applications are not licensed.")
        return

    # Start all associated processes
    run(["/interact/program/iiMoap"])
    run(["/interact/program/iiSysSrvr"])
    run(["service", "collector", "start"])
    run(["service", "listen-controller", "start"])
    run(["service", "statistics", "start"])
    run(["service", "tomcat", "start"])


def run(command, shell=False, failonerror=True):
    print("Executing command [ %s ]." % " ".join(command))

    try:
        begin = datetime.datetime.now()
        retval = subprocess.call(command, shell=shell)
        end = datetime.datetime.now()
        print("Execution took [ %s ]." % str(end - begin))

        if retval != 0:
            print("Execution of [ %s ] failed (returned [ %s ])." % (" ".join(command), str(retval)))
            if failonerror:
                sys.exit(1)

    except Exception, e:
        print("Execution of [ %s ] failed (returned [ %s ])." % (" ".join(command), str(e)))
        if failonerror:
            sys.exit(1)

    print


def main():
    # Set up a parser object which defines how to parse the expected input.
    parser = OptionParser()
    parser.description = """This program is self-extracting software package. Simply execute this file to extract its contents to the /interact/docs/ and /interact/packages/ directories. You may also perform a default installation by specifying the appropriate options listed below."""
    parser.formatter = TitledHelpFormatter(indent_increment=2, max_help_position=30, width=90)

    parser.add_option(
        "-d",
        "--default",
        dest="default",
        action="store_true",
        default=False,
        help="Install using all default values. If an install fails due to pre-requisites not being met, fix the pre-requisites and re-run the install.")

    parser.add_option(
        "",
        "--sipServer",
        dest="sipServer",
        action="store",
        metavar="HOST",
        default=None,
        help="The host name of the server containing the SIP trunk (only applicable if \"default\" has also been specified).")

    parser.add_option(
        "-s",
        "--start",
        dest="start",
        action="store_true",
        default=False,
        help="Start all associated processes when installation is finished (only applicable if \"default\" has also been specified and a valid license file exists).")

    # Parse input parameters.
    (opts, args) = parser.parse_args()

    print

    # Load packages into memory.
    print("Loading package to memory...")
    begin = datetime.datetime.now()
    load()
    end = datetime.datetime.now()
    print("Loading package to memory took [ %s ]." % str(end - begin))
    print

    # Write files to disk.
    print("Extracting package...")
    begin = datetime.datetime.now()
    for unpackfile in packfiles:
        unpackfile.unpack()
    end = datetime.datetime.now()
    print("Extracting package took [ %s ]." % str(end - begin))
    print

    # If default was specified, run default commands
    if opts.default:
        default(opts.sipServer)
        print("Packages were successfully installed.")
        print

        if opts.start:
            start()

    print("""
Documentation can be found in:
  /interact/docs/

Documentation is also accessible via the web (if httpd is running on this machine):
  http://%s/webstatcon/

  default username: spot
  default password: performance
  documentation is under the "DOCS" tab.

""" % socket.gethostname())


if __name__ == "__main__":
   main()
