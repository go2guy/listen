#!/usr/bin/env python
import interface
import os

class listenAction(interface.Action):
    INCLUDED_PACKAGES = ['all']

    def __init__(self, masterpkg, uiapkg):
        super(listenAction, self).__init__(masterpkg, uiapkg)

