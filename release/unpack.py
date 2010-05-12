#!/usr/bin/env python
try:
    import sys, os, re, md5, base64, datetime, subprocess
    from optparse import OptionParser, TitledHelpFormatter

except ImportError, e:
    raise "Unable to import required module: " + str(e)

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
        for line64 in self.contents64:
            self.contents.append(base64.b64decode(line64))

    def unpack(self, directory):
        outfile = open(directory + "/" + self.name, "w+")
        outfile.write("".join(self.contents))
        print("Extracted [ %s ]." % self.name)

        outfile.seek(0)
        newcontents = outfile.readlines()
        outfile.close()

        if not self.md5sum == md5.new("".join(newcontents)).hexdigest():
            raise "Unpacked file [ %s ] failed md5sum verification." % outfile.name


def load():


def default(unpackdir, sipServer):
    # Should be 3 packs (uia, listen, and Realize)
    uiapkg = None
    listenpkg = None
    realizepkg = None

    reuiapkg = re.compile("^uia-")
    relistenpkg = re.compile("^listen-")
    rerealizepkg = re.compile("^Realize-")
    for pkgfile in packfiles:
        if reuiapkg.match(pkgfile.name) != None:
            uiapkg = pkgfile.name

        elif relistenpkg.match(pkgfile.name) != None:
            listenpkg = pkgfile.name

        elif rerealizepkg.match(pkgfile.name) != None:
            realizepkg = pkgfile.name

    if uiapkg == None or listenpkg == None or realizepkg == None:
        sys.exit("Unable to find required packages in bundle.")

    # create default.sup if it doesn't exist...
    os.utime(unpackdir + "/default.sup", None)

    if sipServer != None and sipServer != "":
        supfile = open(unpackdir + "/default.sup", "a")
        supfile.write("CCVXML /interact/apps/spotbuild/listen_conference/root.vxml~update~sipURL~%s~SIP~var~expr~1~server name" % sipServer)
        supfile.close()

    run(["rpm", "-Uvh", "--replacepkgs", unpackdir + "/" + uiapkg])
    run(["/interact/packages/iiInstall.sh", "-i", "--supfile", unpackdir + "/default.sup", "--test", "--noinput", "--replacefiles", "--replacepkgs", unpackdir + "/" + listenpkg, "all"])
    run(["/interact/packages/iiInstall.sh", "-i", "--supfile", unpackdir + "/default.sup", "--noinput", "--replacefiles", "--replacepkgs", unpackdir + "/" + listenpkg, "all"])
#    run(["/interact/packages/iiInstall.sh", "-i", "--noinput", "--replacefiles", "--replacepkgs", unpackdir + "/" + listenpkg, "spotbuild-vip"])
#    run(["/interact/packages/iiInstall.sh", "-i", "--noinput", "--replacefiles", "--replacepkgs", unpackdir + "/" + listenpkg, "ivrserver"])
#    run(["/interact/packages/iiInstall.sh", "-i", "--noinput", "--replacefiles", "--replacepkgs", unpackdir + "/" + listenpkg, "controller"])
    run(["/interact/packages/iiInstall.sh", "-i", "--supfile", unpackdir + "/default.sup", "--noinput", "--test", "--replacefiles", "--replacepkgs", unpackdir + "/" + realizepkg, "realizeserver"])
    run(["/interact/packages/iiInstall.sh", "-i", "--supfile", unpackdir + "/default.sup", "--noinput", "--replacefiles", "--replacepkgs", unpackdir + "/" + realizepkg, "realizeserver"])


def start():
    # Only try to start things if a license file exists
    if not os.path.exists('/interact/master/.iiXmlLicense'):
        print("Applications are not licensed.")
        sys.exit()

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
    unpackdir = "/interact/packages"

    # Set up a parser object which defines how to parse the expected input.
    parser = OptionParser()
    parser.description = """This package is a self-extracting listen bundle. Execute this file to extract all packages to the %s directory. Specify the "default" directive to install the extracted packages using all defaults.""" % unpackdir
    parser.formatter = TitledHelpFormatter(indent_increment=2, max_help_position=30, width=90)

    parser.add_option(
        "-d",
        "--default",
        dest="default",
        action="store_true",
        default=False,
        help="Install using all default values.")

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

    # Create unpack directory if necessary."
    if not os.path.exists(unpackdir):
        os.makedirs(unpackdir)

    # Load packages into memory.
    print("Loading package to memory...")
    begin = datetime.datetime.now()
    load()
    end = datetime.datetime.now()
    print("Loading package to memory took [ %s ]." % str(end - begin))
    print

    # Write files to disk.
    print("Unpacking bundle into [ %s ]..." % unpackdir)
    begin = datetime.datetime.now()
    for unpackfile in packfiles:
        unpackfile.unpack(unpackdir)
    end = datetime.datetime.now()
    print("Unpacking bundle took [ %s ]." % str(end - begin))
    print

    # If default was specified, run default commands
    if opts.default:
        default(unpackdir, opts.sipServer)

        if opts.start:
            start()

if __name__ == "__main__":
   main()
