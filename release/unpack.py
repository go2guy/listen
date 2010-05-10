#!/usr/bin/env python
try:
    import sys, os, re, md5, base64, datetime
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
        outfile = open(directory + self.name, "w+")
        outfile.write("".join(self.contents))

        outfile.seek(0)
        newcontents = outfile.readlines()
        outfile.close()

        if not self.md5sum == md5.new("".join(newcontents)).hexdigest():
            raise "Unpacked file [ %s ] failed md5sum verification." % outfile.name


def load():


def default(unpackdir):
    # Should be 3 packs (uia, listen, and Realize)
    uiapkg = None
    listenpkg = None
    realizepkg = None

    reuiapkg = re.compile("^uia-")
    relistenpkg = re.compile("^listen-")
    rerealizepkg = re.compile("^Reailze-")
    for unpackfile in packfiles:
        if reuiapkg.match(unpackfile.name) != None:
            uiapkg = unpackfile

        if relistenpkg.match(unpackfile.name) != None:
            listenpkg = unpackfile

        if rerealizepkg.match(unpackfile.name) != None:
            realizepkg = unpackfile

    if uiapkg == None or listenpkg == None or realizepkg == None:
        sys.exit("Unable to find required packages in bundle.")

    run(["rpm", "-Uvh", unpackdir + "/" + uiapkg])
    run(["/interact/packages/iiInstall.sh", "-i", "--noinput", "--replacefiles", "--replacepkgs", unpackdir + "/" + listenpkg, "all"])
    run(["/interact/packages/iiInstall.sh", "-i", "--noinput", "--replacefiles", "--replacepkgs", unpackdir + "/" + realizepkg, "all"])


def start():
    # Start all associated processes
    run("/interact/master/iiMoap")
    run("/interact/master/iiSysSrvr")
    run("service", "collector", "start")
    run("Service", "listen-controller", "start")
    run("service", "statistics", "start")
    run("service", "tomcat", "start")


def run(command, shell=False, failonerror=True):
    print("Executing command [ %s ]." % " ".join(command))

    try:
        start = datetime.datetime.now()
        retval = subprocess.call(command, shell=shell)
        end = datetime.datetime.now()
        print("Execution took [ %s ]." % str(end - start))

        if retval != 0:
            print("Execution of [ %s ] failed (returned [ %s ])." % (" ".join(command), str(retval)))
            if failonerror:
                sys.exit(1)

    except Exception, e:
        print("Execution of [ %s ] failed (returned [ %s ])." % (" ".join(command), str(e)))
        if failonerror:
            sys.exit(1)


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
        "-s",
        "--start",
        dest="start",
        action="store_true",
        default=False,
        help="Start all associated processes when installation is finished. (only applicable if \"default\" has also been specified.")

    # Parse input parameters.
    (opts, args) = parser.parse_args()

    # Create unpack directory if necessary."
    if not os.path.exists(unpackdir):
        os.makedirs(unpackdir)

    # Load packages into memory.
    print("Loading package to memory...")
    start = datetime.datetime.now()
    load()
    end = datetime.datetime.now()
    print("Loading package to memory took [ %s ]." % str(end - start))

    # Write files to disk.
    print("Unpacking bundle into [ %s ]..." % unpackdir)
    start = datetime.datetime.now()
    for unpackfile in packfiles:
        unpackfile.unpack(unpackdir)
    end = datetime.datetime.now()
    print("Unpacking bundle took [ %s ]." % str(end - start))

    # If default was specified, run default commands
    if opts.default:
        default(unpackdir)

        if opts.start:
            start()

if __name__ == "__main__":
   main()
