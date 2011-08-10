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
    global server
    global pack
    global hostname

    phases = {"prep": prep, "license": license, "install": install, "all": all}

    parser = OptionParser()
    parser.formatter = TitledHelpFormatter(indent_increment=2, max_help_position=40, width=120)
    parser.add_option(
        "",
        "--server",
        dest="server",
        action="store",
        metavar="SERVER",
        default=None,
        help="The name of the listen server to use")

    parser.add_option(
        "",
        "--pack",
        dest="pack",
        action="store",
        metavar="PKG",
        default=None,
        help="The package to use for installation")

    parser.add_option(
        "",
        "--phase",
        dest="phase",
        action="store",
        choices=phases.keys(),
        default=None,
        help="The phase to be executed. Must be one of " + str(phases.keys()) + ".")

    (options, args) = parser.parse_args()

    if options.server == None:
        parser.error("--server option not supplied")

    if options.phase == None:
        parser.error("--phase option not supplied")

    if options.pack == None:
        parser.error("--pack option not supplied")

    server,__,__ = socket.gethostbyaddr(options.server)
    pack = options.pack

    hostname = socket.gethostname()
    if not hostname == server:
        # determine local and remote locations to use
        localimport = getattr(sys.modules['deploy'], "__file__")
        localprog = os.path.abspath(__file__)
        remoteimport = os.path.expanduser('~/' + os.path.basename(localimport))
        remoteprog = os.path.expanduser('~/' + os.path.basename(localprog))
        remotepack = "/interact/" + os.path.basename(pack)

        # Send to remote location
        deploy.runRemote(server, "mkdir -p %s %s" % (os.path.dirname(remoteprog), os.path.dirname(remotepack)))
        deploy.sftp(server, localimport, remoteimport, 0700)
        deploy.sftp(server, localprog, remoteprog, 0700)
        deploy.sftp(server, pack, remotepack, 0700)
        deploy.runRemote(server, "%s --server=%s --pack=%s --phase=%s" % (remoteprog, server, remotepack, options.phase))

    else:
        phases[options.phase]()


def all():
    prep()
    install()


def prep():
    # Set up info for stopping appropriate processes
    stopcmds = ["service listen-controller stop",
                "service collector stop",
                "service statistics stop",
                "service tomcat stop",
                "service mysqld stop"]

    killprocs = ["/interact/.*/iiMoap",
                 "/interact/.*/iiSysSrvr",
                 "/interact/.*/collector",
                 "java.*app=STATISTICS",
                 "jsvc",
                 "/interact/.*/listen-controller",
                 "mysqld"]

    deploy.stop(stopcmds, killprocs)

    # Set up necessary host info
    hostinfo = {"defaultcontroller": server,
                "defaultivr": server,
                "defaultrealize": server}

    deploy.createAlias(hostinfo)
    deploy.eraseInteractRpms()

    # Remove package files
    deploy.removeFiles("/interact/", pardonfiles=[pack])
    deploy.removeFiles("/var/lib/com.interact.listen/")
    deploy.removeFiles("/var/lib/mysql/")

    # remove interact user
    print("Removing interact user.")
    deploy.run(["userdel", "interact"], failonerror=False)
    deploy.run(["groupdel", "operator"], failonerror=False)


def install():
    # Start up mysql - needed for realize install.
    deploy.run(["service", "mysqld", "start"])

    # Make it executable
    os.chmod(pack, 0700)

    # Extract so we can install arcade stuff...
    deploy.run([pack])

    # Find the extracted packages.
    uiapkg = deploy.getLatestFile("/interact/packages/uia/uia*.rpm")
    if uiapkg == None:
        sys.exit("Unable to find latest uia package.")

    masterpkg = deploy.getLatestFile("/interact/packages/listen/listen*.rpm")
    if masterpkg == None:
        sys.exit("Unable to find latest master package.")

    # install uia packages
    deploy.run(["rpm", "-Uvh", uiapkg])

    # install arcade dependencies
    deploy.run(["/interact/packages/iiInstall.py", "--noinput", "install", masterpkg, "arcade"])

    # License the app so it starts up
    license()

    # Install using defaults and start all processes.
    deploy.run([pack, "-ds", "--sipServer=stl03a.netlogic.net"])


def license():
    deploy.license(hostname)


if __name__ == "__main__":
    main()
