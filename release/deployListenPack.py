#!/usr/bin/python -u

try:
    import datetime
    import deployListen
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
    global pack

    phases = {"prep": deployListen.prep, "license": deployListen.license, "install": install, "post": deployListen.post, "all": all}

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

    if options.controllerserver == None:
        parser.error("--controllerserver option not supplied")

    if options.ivrserver == None:
        parser.error("--ivrserver option not supplied")

    if options.realizeserver == None:
        parser.error("--realizeserver option not supplied")

    if options.phase == None:
        parser.error("--phase option not supplied")

    if options.pack == None:
        parser.error("--pack option not supplied")

    deployListen.controllerserver,__,__ = socket.gethostbyaddr(options.controllerserver)
    deployListen.ivrserver,__,__ = socket.gethostbyaddr(options.ivrserver)
    deployListen.realizeserver,__,__ = socket.gethostbyaddr(options.realizeserver)
    deployListen.uiapkg = ""
    deployListen.masterpkg = ""
    pack = options.pack

    deployListen.hostname = socket.gethostname()
    hosts = set([deployListen.controllerserver, deployListen.ivrserver])
    if deployListen.hostname not in hosts:
        localimport = getattr(sys.modules['deployListen'], "__file__")
        localprog = os.path.abspath(__file__)
        remoteimport = os.path.expanduser('~/' + os.path.basename(localimport))
        remoteprog = os.path.expanduser('~/' + os.path.basename(localprog))
        remotepack = "/interact/" + os.path.basename(pack)

        # Send to all hosts
        for remotehost in hosts:
            deployListen.runRemote(remotehost, "mkdir -p %s %s" % (os.path.dirname(remoteprog), os.path.dirname(remotepack)))
            deployListen.sftp(remotehost, localimport, remoteimport, 0700)
            deployListen.sftp(remotehost, localprog, remoteprog, 0700)
            deployListen.sftp(remotehost, pack, remotepack, 0700)
            deployListen.runRemote(remotehost, "%s --controllerserver=%s --ivrserver=%s --realizeserver=%s --pack=%s --phase=%s" % (remoteprog, deployListen.controllerserver, deployListen.ivrserver, deployListen.realizeserver, remotepack, options.phase))

    else:
        phases[options.phase]()


def all():
    deployListen.prep()
    install()


def install():
    # Make it executable
    os.chmod(pack, 0700)

    # Extract so we can install arcade stuff...
    deployListen.run([pack])

#    # install uia packages
#    deployListen.run([pack, "-ds", "--sipServer=stl03a.netlogic.net"])
#
#    # define an empty list for startup commands
#    startlist = {}
#
#    if deployListen.hostname == deployListen.controllerserver:
#        deployListen.run(["/interact/packages/iiInstall.sh", "-i", "--noinput", deployListen.masterpkg, "all"])
#        startlist["/etc/init.d/httpd"] = "start"
#        startlist["/interact/program/iiMoap"] = ""
#        startlist["/interact/program/iiSysSrvr"] = ""
#        startlist["/etc/init.d/collector"] = "start"
#        startlist["/etc/init.d/listen-controller"] = "start"
#
#    # License the system before we try to start anything.
#    deployListen.license()
#
#    # execute listed startup commands 
#    for command,action in startlist.iteritems():
#        deployListen.run([command, action])


if __name__ == "__main__":
    main()
