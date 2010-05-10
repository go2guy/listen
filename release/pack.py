#!/usr/bin/env python

try:
    import os, md5, base64, re
    from optparse import OptionParser, TitledHelpFormatter

except ImportError, e:
    raise "Unable to import required module: " + str(e)

def build(name, files):
    template = open('unpack.py', 'r')
    outfile = open(name, 'w')

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
        packfiles.append("packfile('%s','%s',['%s'])" % (os.path.basename(packfile.name), md5.new("".join(contents)).hexdigest(), "','".join(encont)))

    outfile.write("    global packfiles" + "\n")
    outfile.write("    packfiles = [%s]" % ",".join(packfiles) + "\n")

    # Write all of the template code after the load method.
    for index, templine in enumerate(templines[index:]):
        outfile.write(templine)

    outfile.close()

def main():
    # Set up a parser object which defines how to parse the expected input
    parser = OptionParser()
    parser.usage = "%prog [options] FILE ..."
    parser.description = "Creates a self-extracting archive from all files passed in on the command line."
    parser.formatter = TitledHelpFormatter(indent_increment=2, max_help_position=30, width=90)

    # Parse input parameters
    (opts, args) = parser.parse_args()

    build('listen', args)

if __name__ == "__main__":
   main()
