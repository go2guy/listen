package com.interact.listen.attendant

import com.interact.listen.attendant.action.Action

class Menu {
    Action defaultAction
    boolean isEntry
    String name
    String optionsPrompt // path to options prompt wav file to play at beginning of menu
    Action timeoutAction

    static belongsTo = [menuGroup: MenuGroup]
    static hasMany = [keypressActions: Action]
    static fetchMode = [keypressActions: 'eager']

    static constraints = {
        name blank: false, maxSize: 50, unique: 'menuGroup'
        optionsPrompt: blank: false
    }

    def toIvrCommand(String promptDirectory, String promptBefore) {
        def args = [
            id: id,
            keyPresses: keypressActions.inject([]) { list, action ->
                list.add(action.keysPressed)
                return list
            },
            audioFile: optionsPrompt && optionsPrompt.trim() != '' ? promptDirectory + '/' + optionsPrompt : ''
        ]

        return [
            action: 'PROMPT',
            promptBefore: !promptBefore || promptBefore.trim() == '' ? '' : promptDirectory + '/' + promptBefore,
            args: args
        ]
    }
}
