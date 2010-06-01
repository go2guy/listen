#!/usr/bin/env python

try:
    import datetime
    import glob
    import os
    import re
    import rpm
    import sha
    import socket
    import subprocess
    import sys
    import time
    import traceback
    import urllib
    import urllib2

except ImportError, e:
    raise "Unable to import required module: " + str(e)


global myName
myName = os.path.basename(__file__)

global myHost
myHost = socket.gethostname()

def sftp(remotehost, localfile, remotefile, mode=None, username='root', password='super'):
    try:
        import paramiko

    except ImportError, e:
        raise "Unable to import required module: " + str(e)

    connection = None
    client = None
    retval = -1
    try:
        connection = paramiko.Transport((remotehost, 22))

# Can't seem to get pub/priv key authentication working.  Will just use "super" for now.
#        keyfile = open(os.path.expanduser("~/.ssh/id_dsa"))
#        print("keyfile is [ %s ]." % keyfile)
#        print("lines: \n%s" % "".join(keyfile.readlines()))
#        pubkey = paramiko.PKey()
#        pubkey.from_private_key(file_obj=keyfile)
#        print(str(pubkey))
#        pubkey.from_private_key_file(filename=os.path.expanduser("~/.ssh/id_dsa.pub"))

        connection.connect(username=username, password=password)
        if not connection.is_active():
            print("Connection to [ %s ] is not active." % remotehost)
            sys.exit(1)

        if not connection.is_authenticated():
            print("Failed authenticating to [ %s ]." % remotehost)
            sys.exit(1)

        client = paramiko.SFTPClient.from_transport(connection)
        now = datetime.datetime.now()
        print("%s %s> sftp %s %s:%s" % (str(now), myName, localfile, remotehost, remotefile))
        client.put(localfile, remotefile)

        if mode != None:
            client.chmod(remotefile, mode)

        if client != None:
            client.close()

    except paramiko.SSHException, sshe:
        print("Unable to connect to %s: %s" % (remotehost, str(sshe)))
        sys.exit(1)

    if connection != None:
        connection.close()

    return retval


def runRemote(remotehost, command, failonerror=True, username='root', password='super'):
    try:
        import paramiko

    except ImportError, e:
        raise "Unable to import required module: " + str(e)

    connection = None
    channel = None
    retval = -1
    try:
        connection = paramiko.Transport((remotehost, 22))

# Can't seem to get pub/priv key authentication working.  Will just use "super" for now.
#        keyfile = open(os.path.expanduser("~/.ssh/id_dsa"))
#        print("keyfile is [ %s ]." % keyfile)
#        print("lines: \n%s" % "".join(keyfile.readlines()))
#        pubkey = paramiko.PKey()
#        pubkey.from_private_key(file_obj=keyfile)
#        print(str(pubkey))
#        pubkey.from_private_key_file(filename=os.path.expanduser("~/.ssh/id_dsa.pub"))

        connection.connect(username=username, password=password)
        if not connection.is_active():
            print("Connection to [ %s ] is not active." % remotehost)
            sys.exit(1)

        if not connection.is_authenticated():
            print("Failed authenticating to [ %s ]." % remotehost)
            sys.exit(1)

        channel = connection.open_session()
        channel.setblocking(0)

        try:
            now = datetime.datetime.now()
            print("%s %s::%s> %s " % (str(now), myName, remotehost, command))
            channel.exec_command(command)

            # Buffer remote command output, echoing to terminal.
            # Keep reading until the remote command has finished.
            while True:
                if channel.recv_ready():
                    sys.stdout.write(channel.recv(1024 * 1024))

                if channel.exit_status_ready():
                    break

                time.sleep(1)

            # Output any stderr that might exist
            while True:
                if channel.recv_stderr_ready():
                    sys.stdout.write(channel.recv_stderr(1024 * 1024))

                else:
                    break;

            # Note any nonzero return status and exit if applicable
            retval = channel.recv_exit_status()
            if retval != 0:
                print("Remote command [ %s ] failed on [ %s ] (returned [ %s ])." % (command, remotehost, str(retval)))
                if failonerror:
                    sys.exit(1)

        except paramiko.SSHException, sshe:
            print("Remote command [ %s ] failed on [ %s ] (returned [ %s ])." % (command, remotehost, str(sshe)))
            if failonerror:
                sys.exit(1)

        if channel != None:
            channel.close()

    except paramiko.SSHException, sshe:
        print("Unable to connect to %s: %s" % (remotehost, str(sshe)))
        sys.exit(1)

    if connection != None:
        connection.close()

    return retval


def stop(stopcmds=[], killprocs=[]):
    for stopcmd in stopcmds:
        run(stopcmd.split(), failonerror=False)

    # Don't sleep if there are no killprocs
    if len(killprocs) > 0:
        for killproc in killprocs:
            run(["pkill", "-TERM", "-f", killproc], failonerror=False)

        time.sleep(30)

        for killproc in killprocs:
            run(["pkill", "-KILL", "-f", killproc], failonerror=False)


def createAlias(hostinfo):
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
        for alias, host in hostinfo.items():
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
        fileHandle.write("\n")
        fileHandle.close()


def eraseInteractRpms(pardonrpms=[]):
    # Always leave xvm-tools
    pardonrpms.append('xvm-tools')

    # Remove all rpms in the interact group which are not in the pardon list.
    print("Removing old Interact rpm packages")
    deathrow = []
    transactionSet = rpm.TransactionSet()
    iterator = transactionSet.dbMatch('group', "Interact")
    if iterator != None:
        for current in iterator:
            currentfull = current['name'] + "-" + current['version'] + "-" + current['release']
            if current['name'] in pardonrpms:
                print("Ignoring removal of package [ %s ]." % currentfull)
            else:
                print("Adding [ %s ] to list of rpms to be removed." % currentfull)
                deathrow.append(currentfull)

    # Remove all rpms in deathrow as long as we have added a package
    if len(deathrow) > 0:
        execute = ["rpm", "-e"]
        execute.extend(deathrow)
        run(execute)


def removeFiles(rootdir, pardonfiles=[]):
    # Remove all files ignoring those in the pardonfiles list.
    # remove everything except xtt and rpms
    print("Cleaning the [ %s ] directory." % rootdir)
    deathrowFiles = []
    deathrowDirs = []
    for root, subFolders, files in os.walk(rootdir):
        if root in pardonfiles:
            print("Ignoring removal of [ %s ] directory." % root)

        else:
            print("Adding [ %s ] to list of directories to be removed." % root)
            deathrowDirs.append(root)

            for file in files:
                file = os.path.join(root,file)
                if file in pardonfiles:
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


def license(licenseHost):
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
                'hostname':licenseHost}

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
    print("Retrieved license details.")
    licenseFile = open("/interact/master/.iiXmlLicense", "w+")
    for line in urlreturn:
        licenseFile.write(line)
    licenseFile.close()

    print("Wrote license file.")
    print("")


def getLatestFile(filename):
    # get all files which match the filename, sort in reverse, return first entry or None.
    files = glob.glob(filename)
    if len(files) >= 1:
        files.sort(reverse=True)
        return files[0]

    else:
        return None


def run(command, shell=False, failonerror=True):
    now=datetime.datetime.now()
    print("%s %s> %s " % (str(now), myName, " ".join(command)))

    try:
        retval = subprocess.call(command, shell=shell)
        if retval != 0:
            print("Command [ %s ] failed on [ %s ] (returned [ %s ])." % (" ".join(command), myHost, str(retval)))
            if failonerror:
                sys.exit(1)

    except Exception, e:
        print("Command [ %s ] failed on [ %s ] (returned [ %s ])." % (" ".join(command), myHost, str(e)))
        if failonerror:
            sys.exit(1)

    print


