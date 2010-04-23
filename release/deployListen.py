#!/usr/bin/python -u

try:
    import datetime
    import glob
    import os
    import re
    import rpm
    import socket
    import subprocess
    import sys
    import time
    import traceback
    import urllib
    import urllib2
    from optparse import OptionParser, TitledHelpFormatter

except ImportError, e:
    raise "Unable to import required module: " + str(e)


def main():
    global phase
    global listenserver
    global hostname

    phases = {"prep": prep, "install": install, "upgrade": install, "post": post, "all": all}

    parser = OptionParser()
    parser.formatter = TitledHelpFormatter(indent_increment=2, max_help_position=40, width=120)
    parser.add_option(
        "",
        "--listenserver",
        dest="listenserver",
        action="store",
        metavar="SERVER",
        default=None,
        help="The name of the listen server host to use")

    parser.add_option(
        "",
        "--insaserver",
        dest="insaserver",
        action="store",
        metavar="SERVER",
        default=None,
        help="The name of the INSA server host to use")

    parser.add_option(
        "",
        "--phase",
        dest="phase",
        action="store",
        choices=phases.keys(),
        default=None,
        help="The phase to be executed. Must be one of " + str(phases.keys()) + ".")

    (options, args) = parser.parse_args()

    if options.listenserver == None:
        parser.error("--listenserver option not supplied")

    if options.insaserver == None:
        parser.error("--insaserver option not supplied")

    if options.phase == None:
        parser.error("--phase option not supplied")

    listenserver,__,__ = socket.gethostbyaddr(options.listenserver)
    insaserver,__,__ = socket.gethostbyaddr(options.insaserver)

    hostname = socket.gethostname()
    if hostname not in (listenserver):
        print("Local hostname [ %s ] matches no entries on input hostlist." % hostname)
        sys.exit(1)

    phases[options.phase]()


def run(command, shell=False, failonerror=True):
    now=datetime.datetime.now()
    print("%s deployListen> %s " % (str(now), " ".join(command)))

    try:
        retval = subprocess.call(command, shell=shell)
        if retval != 0:
            print("Command [ %s ] failed on [ %s ] (returned [ %s ])." % (" ".join(command), hostname, str(retval)))
            if failonerror:
                sys.exit(1)

    except Exception, e:
        print("Command [ %s ] failed on [ %s ] (returned [ %s ])." % (" ".join(command), hostname, str(e)))
        if failonerror:
            sys.exit(1)

    print


def getLatestFile(filename):
    # get all files which match the filename, sort in reverse, return first entry or None.
    files = glob.glob(filename)
    if len(files) >= 1:
        files.sort(reverse=True)
        return files[0]

    else:
        return None


def removeFiles(rootdir, pardon={}):
    # Remove all files ignoring those in the pardon list.
    # remove everything except xtt and rpms
    print("Cleaning the [ %s ] directory." % rootdir)
    deathrowFiles = []
    deathrowDirs = []
    for root, subFolders, files in os.walk(rootdir):
        if root in pardon:
            print("Ignoring removal of [ %s ] directory." % root)

        else:
            print("Adding [ %s ] to list of directories to be removed." % root)
            deathrowDirs.append(root)

            for file in files:
                file = os.path.join(root,file)
                if file in pardon:
                    print("Ignoring removal of [ %s ] file." % file)

                else:
                    print("Adding [ %s ] to list of files to be removed." % file)
                    deathrowFiles.append(file)

    # Remove all files in deathrowFiles
    if len(deathrowFiles) > 0:
        execute = ["rm", "-f"]
        execute.extend(deathrowFiles)
        run(execute)

    # Remove all empty directories in deathrowDirs
    if len(deathrowDirs) > 0:
        # sort so we remove sub directories before their parents
        deathrowDirs.sort(reverse=True)
        execute = ["rmdir", "--ignore-fail-on-non-empty"]
        execute.extend(deathrowDirs)
        run(execute)


def all():
    prep()
    install()
    post()


def prep():
    run(["mkdir", "-p", "/interact/packages/"])

    if hostname == listenserver:
        print("Preparing for deployment")
        run(["service", "listen-controller", "stop"], failonerror=False)
        run(["service", "listen-gui", "stop"], failonerror=False)
        run(["service", "collector", "stop"], failonerror=False)

        # And kill them just to be sure
        killprocs = ["/interact/.*/iiMoap", "/interact/.*/iiSysSrvr", "/interact/.*/collector", "/interact/.*/listen-gui", "/interact/.*/listen-controller"]
        for killproc in killprocs:
            run(["pkill", "-TERM", "-f", killproc], failonerror=False)

        time.sleep(30)

        for killproc in killprocs:
            run(["pkill", "-KILL", "-f", killproc], failonerror=False)

    # setup hosts file
    try:
        # Output standard info
        print("Generating hosts file")
        fileHandle = open( "/etc/hosts" , "w+" )
        fileHandle.write("# Do not remove the following line, or various programs\n")
        fileHandle.write("# that require network functionality will fail.\n")
        fileHandle.write("127.0.0.1       localhost.localdomain localhost\n")
        fileHandle.write("::1             localhost6.localdomain6 localhost6\n")
        fileHandle.write("\n")

        # Output host aliases
        for host, alias in ([listenserver, "defaultspot"], [listenserver, "defaultcontroller"], [listenserver, "defaultweb"], insaserver, "defaultinsa"):
            #loop through and lookup names for all hosts that
            #populated
            if host != None:
                try:
                    ip = socket.gethostbyname(host)
                    # name determined, write to hosts file
                    fileHandle.write("%s %s %s %s.interact.nonreg\n" % (ip, host, alias, alias))

                except:
                    print("Unable to retrieve ip information for [ %s ]: %s" % (str(host), str(sys.exc_info()[0])))
                    sys.exit(1)

    finally:
        fileHandle.close()

    print("Removing old Interact rpm packages")
    pardon = ['xtt','xvm-tools']
    deathrow = []
    transactionSet = rpm.TransactionSet()
    iterator = transactionSet.dbMatch('group', "Interact")
    if iterator != None:
        for current in iterator:
            currentfull = current['name'] + "-" + current['version'] + "-" + current['release']
            if current['name'] in pardon:
                print("Ignoring removal of package [ %s ]." % currentfull)
            else:
                print("Adding [ %s ] to list of rpms to be removed." % currentfull)
                deathrow.append(currentfull)

    # Remove all rpms in deathrow as long as we have added a package
    if len(deathrow) > 0:
        execute = ["rpm", "-e"]
        execute.extend(deathrow)
        run(execute)

    # Clean interact directory
    pardon = ["/interact/xtt", getLatestFile("/interact/uia*.rpm"), getLatestFile("/interact/listen*.rpm")]
    removeFiles("/interact/", pardon)

    # remove interact user
    print("Removing interact user.")
    run(["userdel", "interact"], failonerror=False)
    run(["groupdel", "operator"], failonerror=False)


def license():
    # pull hostid and fingerprint for this host
    args = ['/interact/program/xmlsecurity','-hostid']
    substream = subprocess.Popen(args,stdout=subprocess.PIPE);
    for line in substream.stdout:
        print("Licensing hostid is [ %s ]." % line)
        hostid = line

    args = ['/interact/program/xmlsecurity','-fingerprint']
    substream = subprocess.Popen(args,stdout=subprocess.PIPE);
    for line in substream.stdout:
        print("Licensing fingerprint is [ %s ]." % line)
        fingerprint = line

    # cook up the args for our request to the licensing server
    password = sha.new('Interact_DeployTool_87D9wqjrJJLZ').hexdigest()
    postargs = {'command':'temp',
                'username':'DeployTool',
                'password':password,
                'hostid': hostid,
                'fingerprint': fingerprint,
                'hostname':hostname}

    args = urllib.urlencode(postargs)

    # send request for temp license, which will serve to register
    # this host with licensing server
    substream = urllib2.urlopen('https://www.iivipconnect.com/GetLicense.php', args)
    urlreturn = substream.readlines()

    # temp license has been retrieved -- but is not needed
    # now retrieve feature list
    substream = urllib2.urlopen('https://www.iivipconnect.com/GetAvailable.php', args)
    urlreturn = substream.readlines()

    features = {}
    for line in urlreturn:
        parsedline = line.strip().split(':')
        if (parsedline[2] == 'boolean'):
            postargs['feature_' + parsedline[0]] = parsedline[0]
            postargs['count_' + parsedline[0]] = 1
        else:
            postargs['feature_' + parsedline[0]] = parsedline[0]
            postargs['count_' + parsedline[0]] = 100


    # encode the features into a new postdata argument
    postargs['command'] = 'perm'
    args = urllib.urlencode(postargs)

    # fetch permanent license
    substream = urllib2.urlopen('https://www.iivipconnect.com/GetLicense.php', args)
    urlreturn = substream.readlines()

    # if we have gotten this far, and only in that case, wipe out existing
    # license and replace with this one
    print("")
    print("Retrieved license details.")
    licenseFile = open("/interact/master/.iiXmlLicense", "w+")
    for line in urlreturn:
        licenseFile.write(line)

    licenseFile.close()


def install():
    # install uia packages
    uiaPKG = getLatestFile("/interact/uia*.rpm")
    if uiaPKG == None:
        print("No UIA package found")
        sys.exit(1)

    print("Found uia package [ %s ]." % uiaPKG)
    run(["rpm", "-Uvh", uiaPKG])

    # get listen package
    listenPKG = getLatestFile("/interact/listen*.rpm")
    if listenPKG == None:
        print("No listen package found")
        sys.exit(1)

    print("Found listen package [ %s ]." % listenPKG)

    # define an empty list for startup commands
    startlist = {}

    if hostname == listenserver:
        run(["/interact/packages/iiInstall.sh", "-i", "--noinput", listenPKG, "all"])
        startlist["/interact/program/iiMoap"] = ""
        startlist["/interact/program/iiSysSrvr"] = ""
        startlist["/etc/init.d/collector"] = "start"
        startlist["/etc/init.d/listen-controller"] = "start"
        startlist["/etc/init.d/listen-gui"] = "start"

    # License the system before we try to start anything.
    license()

    # execute listed startup commands 
    for command,action in startlist.iteritems():
        run([command, action])


def post():    
    if hostname == listenserver: 
        # run automated tests here
        print("Performing postinstall steps") 

if __name__ == "__main__":
    main()
