#!/usr/bin/env python
try:
    import os, md5, base64
    from optparse import OptionParser

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


def default():
    print("Installing using defaults.")


def main():
    # Set up a parser object which defines how to parse the expected input
    parser = OptionParser()

    parser.add_option(
        "-d",
        "--default",
        dest="default",
        action="store_true",
        default=False,
        help="Install using all default values.")

    # Parse input parameters
    (opts, args) = parser.parse_args()

    unpackdir = "./interact/packages/"
    if not os.path.exists(unpackdir):
        os.makedirs(unpackdir)

    load()
    for unpackfile in packfiles:
        unpackfile.unpack(unpackdir)

    if opts.default:
        default()

if __name__ == "__main__":
   main()
