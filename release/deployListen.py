#!/usr/bin/python -u

try:
    import datetime
    import deploy
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
    from optparse import OptionParser, TitledHelpFormatter

except ImportError, e:
    raise "Unable to import required module: " + str(e)


def main():
    global phase
    global controllerserver
    global ivrserver
    global realizeserver
    global hostname
    global masterpkg
    global uiapkg

    phases = {"prep": prep, "clean": clean, "license": license, "install": install, "upgrade": upgrade, "post": post, "all": all}

    parser = OptionParser()
    parser.formatter = TitledHelpFormatter(indent_increment=2, max_help_position=40, width=120)
    parser.add_option(
        "",
        "--controllerserver",
        dest="controllerserver",
        action="store",
        metavar="SERVER",
        default=None,
        help="The name of the listen controller server to use")

    parser.add_option(
        "",
        "--ivrserver",
        dest="ivrserver",
        action="store",
        metavar="SERVER",
        default=None,
        help="The name of the ivr server to use")

    parser.add_option(
        "",
        "--realizeserver",
        dest="realizeserver",
        action="store",
        metavar="SERVER",
        default=None,
        help="The name of the Realize server to use")

    parser.add_option(
        "",
        "--masterpkg",
        dest="masterpkg",
        action="store",
        metavar="PKG",
        default=None,
        help="The master package to use for installation")

    parser.add_option(
        "",
        "--uiapkg",
        dest="uiapkg",
        action="store",
        metavar="PKG",
        default=None,
        help="The uia package to use for installation")

    parser.add_option(
        "",
        "--phase",
        dest="phase",
        action="store",
        choices=phases.keys(),
        default=None,
        help="The phase to be executed. Must be one of " + str(phases.keys()) + ".")

    (options, args) = parser.parse_args()

    if options.controllerserver == None:
        parser.error("--controllerserver option not supplied")

    if options.ivrserver == None:
        parser.error("--ivrserver option not supplied")

    if options.realizeserver == None:
        parser.error("--realizeserver option not supplied")

    if options.phase == None:
        parser.error("--phase option not supplied")

    if options.masterpkg == None:
        parser.error("--masterpkg option not supplied")

    if options.uiapkg == None:
        parser.error("--uiapkg option not supplied")

    controllerserver,__,__ = socket.gethostbyaddr(options.controllerserver)
    ivrserver,__,__ = socket.gethostbyaddr(options.ivrserver)
    realizeserver,__,__ = socket.gethostbyaddr(options.realizeserver)
    masterpkg = options.masterpkg
    uiapkg = options.uiapkg

    hostname = socket.gethostname()
    hosts = set([controllerserver, ivrserver])
    if hostname not in hosts:
        localimport = getattr(sys.modules['deploy'], "__file__")
        localprog = os.path.abspath(__file__)
        remoteimport = os.path.expanduser('~/' + os.path.basename(localimport))
        remoteprog = os.path.expanduser('~/' + os.path.basename(localprog))
        remotemaster = "/interact/" + os.path.basename(masterpkg)
        remoteuia = "/interact/" + os.path.basename(uiapkg)

        # Send to all hosts
        for remotehost in hosts:
            deploy.runRemote(remotehost, "mkdir -p %s %s %s" % (os.path.dirname(remoteprog), os.path.dirname(remotemaster), os.path.dirname(remoteuia)))
            deploy.sftp(remotehost, localimport, remoteimport, 0700)
            deploy.sftp(remotehost, localprog, remoteprog, 0700)
            deploy.sftp(remotehost, masterpkg, remotemaster, 0700)
            deploy.sftp(remotehost, uiapkg, remoteuia, 0700)
            deploy.runRemote(remotehost, "%s --controllerserver=%s --ivrserver=%s --realizeserver=%s --masterpkg=%s --uiapkg=%s --phase=%s" % (remoteprog, controllerserver, ivrserver, realizeserver, remotemaster, remoteuia, options.phase))

    else:
        phases[options.phase]()


def all():
	install()

def install():
    prep()
    clean()
    doinstall()
    post()

def upgrade():
    prep()
    doinstall()
    post()

def clean():
    deploy.eraseInteractRpms()
    deploy.removeFiles("/interact/", pardonfiles=[uiapkg, masterpkg])
    deploy.removeFiles("/var/lib/com.interact.listen/")
    deploy.removeFiles("/var/lib/mysql/")


def prep():
    print("Preparing for deployment")
    stopcmds = ["service listen-controller stop",
                "service collector stop",
                "service mysqld stop"]

    killprocs = ["/interact/.*/iiMoap",
                 "/interact/.*/iiSysSrvr",
                 "/interact/.*/collector",
                 "/interact/.*/listen-controller",
                 "mysqld"]

    deploy.stop(stopcmds, killprocs)


    hostinfo = {"defaultcontroller": controllerserver,
                "defaultivr": ivrserver,
                "defaultrealize": realizeserver}

    deploy.createAlias(hostinfo)


def doinstall():
    # install uia packages
    deploy.run(["rpm", "-Uvh", "--replacepkgs", uiapkg])

    # Make sure mysqld is running
    deploy.run(["service", "mysqld", "start"], failonerror=False)

    # define an empty list for startup commands
    startlist = {}

    if hostname == controllerserver:
        deploy.run(["/interact/packages/iiInstall.sh", "-i", "--noinput", "-replacepkgs", masterpkg, "all"])
        startlist["/etc/init.d/httpd"] = "start"
        startlist["/interact/program/iiMoap"] = ""
        startlist["/interact/program/iiSysSrvr"] = ""
        startlist["/etc/init.d/collector"] = "start"
        startlist["/etc/init.d/listen-controller"] = "start"

    # License the system before we try to start anything.
    license()

    # execute listed startup commands 
    for command,action in startlist.iteritems():
        deploy.run([command, action])

def license():
    deploy.license(hostname)


def post():    
    if hostname == controllerserver: 
        # run automated tests here
        print("Performing postinstall steps") 


if __name__ == "__main__":
    main()
