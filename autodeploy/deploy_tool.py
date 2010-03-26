#!/usr/bin/python -u

import os
import getopt
import sys
import traceback
import re
import socket
import datetime
import urllib
import urllib2
import subprocess


def getLicense():
    # pull hostid and fingerprint for this host
    args = ['/interact/program/xmlsecurity','-hostid']
    substream=subprocess.Popen(args,stdout=subprocess.PIPE);
    for line in substream.stdout:
        print "Licensing hostid="+line
        hostid=line 

    args = ['/interact/program/xmlsecurity','-fingerprint']
    substream=subprocess.Popen(args,stdout=subprocess.PIPE);
    for line in substream.stdout:
        print "Licensing fingerprint="+line
        fingerprint=line 

    # cook up the args for our request to the licensing server
    postargs = {'command':'temp',
        'username':'iilincoln',
        'password':'745cb5fd855d1bfe845fbe79cb8f5f335a9afe77'}

    postargs['hostid']=hostid
    postargs['fingerprint']=fingerprint
    postargs['hostname']=hostname
    args=urllib.urlencode(postargs)

    # send request for temp license, which will server to register 
    # this host with licensing server
    substream=urllib2.urlopen('https://www.iivipconnect.com/GetLicense.php',args)
    urlreturn=substream.readlines()

    # temp license has been retrieved -- but is not needed
    # now retrieve feature list
    substream=urllib2.urlopen('https://www.iivipconnect.com/GetAvailable.php',args)
    urlreturn=substream.readlines()
    features={}
    for line in urlreturn:
        parsedline=line.strip().split(':')
        if (parsedline[2]=='boolean'):
            postargs['feature_'+parsedline[0]]=parsedline[0]
            postargs['count_'+parsedline[0]]=1
        else:
            postargs['feature_'+parsedline[0]]=parsedline[0]
            postargs['count_'+parsedline[0]]=100


    postargs['command']='perm'
    # encode the features into a new postdata argument 
    args=urllib.urlencode(postargs)
    print "Licensing request postdata "+args
    # fetch permanent license
    substream=urllib2.urlopen('https://www.iivipconnect.com/GetLicense.php',args)
    urlreturn=substream.readlines()
    
    # if we have gotten this far, and only in that case, wipe out existing
    # license and replace with this one
    print "retrieved license details:"
    licenseFile=open( "/interact/master/.iiXmlLicense", "w+" )
    for line in urlreturn:
        print line
        licenseFile.write(line)

    licenseFile.close()

def doCmd(oscmd):
    now=datetime.datetime.now()
    print str(now),"deploy_tool> ", oscmd
    osrc=os.system(oscmd)
    if (osrc != 0):
        print "Command: ",oscmd, " on: ", hostname, " returned: ",osrc
        traceback.print_exc(file=loggerFile)
        sys.exit(1)
        #raise Exception('oscmd')

def doAnyway(oscmd):
    now=datetime.datetime.now()
    print str(now),"deploy_tool> ", oscmd
    osrc=os.system(oscmd)
    if (osrc != 0):
        print "Command: ",oscmd, " on: ", hostname, " returned: ",osrc

def doPrep():
    if hostname==spotserver:
        print "Preparing for spotserver deployment"
    
    if hostname==controlserver:
        print "Preparing for DB deployment"
        doAnyway("/etc/init.d/listen-controller stop")
    
    if hostname==webserver:
        print "Preparing for WEB deployment"
        # and this is hideous, but let's shut down some software
        doAnyway("/etc/init.d/listen-web stop")
    
    # setup hosts file
    try:
        # Output standard info
        print "Generating hosts file"
        fileHandle = open( "/etc/hosts" , "w+" )
        fileHandle.write("# Do not remove the following line, or various programs\n")
        fileHandle.write("# that require network functionality will fail.\n")
        fileHandle.write("127.0.0.1       localhost.localdomain localhost\n")
        fileHandle.write("::1             localhost6.localdomain6 localhost6\n")
        fileHandle.write("\n")

        # Output host aliases
        for host, alias in ([spotserver, "defaultspot"], [controlserver, "defaultcontroller"], [webserver, "defaultweb"]):
            #loop through and lookup names for all hosts that
            #populated
            if host != None and host != "":
                try:
                    ip = socket.gethostbyname(host)
                    # name determined, write to hosts file
                    fileHandle.write(ip + " " + host + " " + alias + " " + alias + ".interact.nonreg\n")

                except:
                    print("Unable to retrieve ip information for [ " + str(host) + " ]:" + str(sys.exc_info()[0]))
                    sys.exit(1)

    finally:
        fileHandle.close()

    # assume that one and only one listenXXXX.rpm is in /interact/packages
    
    
    print "removing old Interact packages"
    goodpacks=['xvm-tools', 'iiserv', 'iiacs', 'iielndb', 'iixap', 'spotbuild', 'xmlsecurity', 'iidev', 'uia']
    nukelist = []
    packages=os.popen('rpm -qg Interact')
    for p in packages.readlines():
        nukelist.append(p)
        for g in goodpacks:
            if (p.find(g) > -1):
                print "skipped removal of %s" % p
                nukelist.remove(p)
    
    walkingdead=" ".join(nukelist)
    deathrow=walkingdead.replace("\n","")
    
    if (len(deathrow) > 0):
        removeify = "rpm -e %s" % deathrow 
        print "removing %s" % deathrow
        doAnyway(removeify)
    
    
    # smack the hell out of the /interact/listen directory
    print "Cleaning /interact/listen directory"
    pfiles = os.listdir('/interact/listen')
    nukelist=[]
    for pf in pfiles:
        nukelist.append(pf)
        if (pf.find('.rpm') > -1):
            # preserve rpm files
            nukelist.remove(pf)
    
        if (pf.find('xtt') > -1):
            # preserve xtt
            nukelist.remove(pf)
    
    for pf in nukelist:
        removeify = "rm -rf /interact/listen/%s" % (pf)
        doCmd(removeify)
    
    
# script starts here and stuff
# determine the basics and parse args
def main():
    
    # some globals, because lazy programmers love globals
    global phase
    global webserver
    global spotserver
    global controlserver
    global hostname

    phase="unset"
    webserver="unset"
    spotserver="unset"
    controlserver="unset"

    try:
        opts, args = getopt.getopt(sys.argv[1:], "" , ["webserver=","controlserver=","spotserver=","phase="])
    except getopt.GetoptError, err:
        print str(err)
        print "USAGE: deploy.py --webserver host --controlserver host --spotserver host --phase phasename"
        sys.exit(1)
   
    # open logfile and general housekeeping
    global loggerFile
    loggerFile=open("deploy.log","w+")
    for o, a in opts:
        if o in ("--webserver"):
            if (len(a.split('.')) == 1):
                webserver="%s.interact.nonreg" % (a)
            else:
                webserver=a
        elif o in ("--spotserver"):
            if (len(a.split('.')) == 1):
                spotserver="%s.interact.nonreg" % (a)
            else:
                spotserver=a
        elif o in ("--controlserver"):
            if (len(a.split('.')) == 1):
                controlserver="%s.interact.nonreg" % (a)
            else:
                controlserver=a
        elif o in ("--phase"):
            phase=a
        else:
            assert False, "unhandled option"
    
    # check for mandatory options
    
    if "unset" in (phase, webserver, spotserver, controlserver):
        print "USAGE: deploy.py --webserver host --controlserver host --spotserver host --phase phasename"
        sys.exit(1)
    
    hostname =  socket.gethostname()
    
    if hostname not in (webserver,controlserver,spotserver):
        print "local hostname %s matches no entries on input hostlist" % hostname
        sys.exit(1)
    
    print "hostlist: \nweb=%s\ncontrolserver=%s\nspotserver=%s\n" % (webserver, controlserver, spotserver)
    
    if phase == "prep":
        doPrep()
    elif phase == "install":
        doInstall()
    elif phase == "upgrade":
        doInstall()
    elif phase == "post":
        doPost()
    elif phase == "all":
        doPrep()
        doInstall()
        doPost()
    else:
        print "Uknown phase ",phase
        sys.exit(1)

    sys.exit(0)


def doInstall():
    
    # discover listen package 
    pfiles = os.listdir('/interact/')
    
    listenpackage = 'notfound'
    for item in pfiles:
        if re.match("listen.*rpm",item):
            listenpackage=item
            print "found package %s" % listenpackage
    
    if (listenpackage == 'notfound'):
        print 'No Listen package found'
        sys.exit(1)
    else:
        print "Processing Listen package: ",listenpackage
        # this will clean up any previous exploded packages
        doCmd("mv /interact/%s /interact/packages/" % listenpackage)
    
    # define an empty list for startup commands
    
    startlist = []
    if hostname==spotserver:
        doCmd("/interact/packages/iiInstall.sh -i --noinput --force /interact/packages/listen*.rpm spotapps")
        # Installation successful.
        print "Listen-spotapps installation complete"
    
    if hostname==controlserver:
        doCmd("/interact/packages/iiInstall.sh -i --noinput --force /interact/packages/listen*.rpm controller")
        # perform the schema installs
        # this is a mess
        print "Controller installation complete."
        # Add db processes to list to start
        startlist.append("/etc/init.d/listen-controller start")
    
    if hostname==webserver:
        doCmd("/interact/packages/iiInstall.sh -i --noinput --force /interact/packages/listen*.rpm web")
        # Installation successful. Start appropriate processes
        print "WEB Installation complete."
        # Add web processes to list to start
        startlist.append("/etc/init.d/listen-web start")
    
    # flush duplicates from startlist
    d = {}
    for command in startlist:
        d[command]=command
    startlist=d.values()
    
    # execute listed startup commands 
    for command in startlist:
        doCmd(command)


def doPost():    
    #postlist
    if hostname==controlserver: 
        #run automated tests here?
        print "performing postinstall steps" 

if __name__ == "__main__": main()
