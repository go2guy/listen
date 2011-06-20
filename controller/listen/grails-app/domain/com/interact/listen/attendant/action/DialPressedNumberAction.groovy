package com.interact.listen.attendant.action

class DialPressedNumberAction extends Action {
    // has no attributes
    def toIvrCommand(String promptDirectory, String promptBefore) {
        return [
            promptBefore: !promptBefore || promptBefore.trim().equals('') ? '' : promptDirectory + '/' + promptBefore,
            action: 'DIAL_PRESSED_NUMBER'
        ]
    }
}
