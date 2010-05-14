#!/usr/bin/env python

try:
    import os, md5, base64, re, rpm, sys, pwd
    from optparse import OptionParser, TitledHelpFormatter
    from zipfile import ZipFile, is_zipfile, ZIP_DEFLATED
    from StringIO import StringIO

except ImportError, e:
    raise "Unable to import required module: " + str(e)

def pack(name, files):
    print

    # Determine architecture of rpms we are packaging.
    arch = { }
    transactionSet = rpm.TransactionSet()
    for packfile in files:
        if packfile.endswith('.rpm'):
            # Read rpm info from file
            if not os.path.isfile(packfile):
                raise Exception("Rpm file [ %s ] does not exist." % packfile)

            try:
                fileDescriptor = os.open(packfile, os.O_RDONLY)
                tmphdr = transactionSet.hdrFromFdno(fileDescriptor)
                arch[tmphdr[rpm.RPMTAG_ARCH]] = None

            finally:
                os.close(fileDescriptor)

    # >1 arches is OK if one of them is "noarch". get rid of the noarch and use the arch specific package
    # if the only arch is 'noarch' leave it.
    if len(arch) > 1:
        arch.pop('noarch', None)

    if len(arch) != 1:
        print("An invalid number or rpm architectures was found in input files %s: found %d but should be 1." % (files, len(arch)))
        sys.exit()

    if not os.path.exists(os.path.dirname(name)):
        os.makedirs(os.path.dirname(name))

    outfile = open(name + "." + arch.keys()[-1], 'w')
    print("Creating output file [ %s ]." % os.path.basename(outfile.name))

    template = open('unpack.py', 'r')
    print("  Adding template information from [ %s ] up to the \"load()\" method." % os.path.basename(template.name))

    # Write all of the template code into output file up to the "load" method.
    templines = template.readlines()
    for index, templine in enumerate(templines):
        outfile.write(templine)

        if re.search('def[\s]+load(.*):', templine) != None:
            index += 1
            break

    packfiles = [ ]
    for packfile in files:
        print("  Adding [ %s ] as a static, zipped, base64 encoded python object." % os.path.basename(packfile))

        # md5sum the original file
        packfile = open(packfile, "r")
        md5sum = md5.new("".join(packfile.readlines())).hexdigest()

        # Create in-memory zip file
        zipfile = StringIO()
        ziparch = ZipFile(zipfile, 'w', ZIP_DEFLATED)
        ziparch.write(packfile.name, os.path.basename(packfile.name))
        ziparch.close()
        zipfile.seek(0)

        # Base64 encode the zip file and write to the output
        encont = []
        for zipline in zipfile.readlines():
            encont.append(base64.b64encode(zipline))
        packfiles.append("packfile('%s','%s',['%s'])" % (os.path.basename(packfile.name), md5sum, "','".join(encont)))

    outfile.write("    global packfiles" + "\n")
    outfile.write("    packfiles = [%s]" % ",".join(packfiles) + "\n")

    # Write all of the template code after the load method.
    print("  Adding all remaining template information from [ %s ]." % os.path.basename(template.name))
    for index, templine in enumerate(templines[index:]):
        outfile.write(templine)

    outfile.close()

    os.chmod(outfile.name, 0755)
    print("finished writing to [ %s ]." % os.path.basename(outfile.name))
    print

    readfile = open(os.path.dirname(name) + '/README.txt', 'w')
    print("Creating readme file [ %s ]." % os.path.basename(readfile.name))
    readfile.write("""
##################################################################
# Copyright (c) Interact Incorporated. All Rights Reserved.
##################################################################

%s is a self-extracting package.

For full usage information (including additional installation options) run:
  %s -h

""" % (os.path.basename(outfile.name), os.path.basename(outfile.name), os.path.basename(outfile.name), os.path.basename(outfile.name)))
    readfile.close()
    print


def main():
    # Set up a parser object which defines how to parse the expected input
    parser = OptionParser()
    parser.usage = "%prog [options] FILE ..."
    parser.description = "Creates a self-extracting archive from all files passed in on the command line."
    parser.formatter = TitledHelpFormatter(indent_increment=2, max_help_position=30, width=90)

    # Parse input parameters
    (opts, args) = parser.parse_args()

    pack(os.path.dirname(__file__) + '/ii_artifacts/listen', args)

if __name__ == "__main__":
   main()
