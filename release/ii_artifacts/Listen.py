#!/usr/bin/env python
import interface
import os

class ListenAction(interface.Action):
    INCLUDED_PACKAGES = ['all']

    def __init__(self, masterpkg, uiapkg):
        super(ListenAction, self).__init__(masterpkg, uiapkg)

