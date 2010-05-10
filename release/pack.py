#!/usr/bin/env python

try:
    import md5, base64, re
    from optparse import OptionParser

except ImportError, e:
    raise "Unable to import required module: " + str(e)

def build(name, files):
    template = open("unpack.py", "r")
    outfile = open(name, "w")

    # Write all of the template code into output file up to the "load" method.
    templines = template.readlines()
    for index, templine in enumerate(templines):
        outfile.write(templine)

        if re.search('def[\s]+load(.*):', templine) != None:
            index += 1
            break


    # Write all of the packages in base64
    packfiles = []
    for packfile in files:
        packfile = open(packfile, "r")
        contents = []
        encont = []
        for packline in packfile.readlines():
            contents.append(packline)
            encont.append(base64.b64encode(packline))
        packfiles.append("packfile('%s','%s',['%s'])" % (packfile.name, md5.new("".join(contents)).hexdigest(), "','".join(encont)))

    outfile.write("    global packfiles" + "\n")
    outfile.write("    packfiles = [%s]" % ",".join(packfiles) + "\n")

    # Write all of the template code after the load method.
    for index, templine in enumerate(templines[index:]):
        outfile.write(templine)

    outfile.close()

def main():
    # Set up a parser object which defines how to parse the expected input
    parser = OptionParser()

    parser.add_option(
        "",
        "--name",
        dest="name",
        action="store",
        metavar="NAME",
        default=None,
        help="The name of the archive to be created.")

    # Parse input parameters
    (opts, args) = parser.parse_args()

    # system is required
    if opts.name == None:
        parser.error("--name option not supplied")

    build(opts.name, args)

if __name__ == "__main__":
   main()
