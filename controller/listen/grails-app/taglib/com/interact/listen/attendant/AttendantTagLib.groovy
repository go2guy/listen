package com.interact.listen.attendant

import com.interact.listen.attendant.action.*

class AttendantTagLib {
    static namespace = 'listen'

    def promptFileService
    def springSecurityService

    def promptSelect = { attrs ->
        def value = attrs.value

        def user = springSecurityService.getCurrentUser()

        def prompts = promptFileService.listNames(user.organization.id)
        out << '<select class="' + attrs.class + '">'
        out << '<option>-- No Prompt --</option>'
        prompts.each { prompt ->
            out << '<option' + (value && prompt == value ? ' selected="selected"' : '') + ">${prompt.encodeAsHTML()}</option>"
        }
        out << '<option>Upload New Prompt...</option>'
        out << '</select>'
    }

    def actionSelect = { attrs ->
        def action = attrs.action
        out << '<select class="action-select">'
        out << '<option' + (action?.instanceOf(DialNumberAction) ? ' selected="selected"' : '') + '>Dial A Number...</option>'
        out << '<option' + (action?.instanceOf(DialPressedNumberAction) ? ' selected="selected"' : '') + '>Dial What They Pressed</option>'
        out << '<option' + (action?.instanceOf(EndCallAction) ? ' selected="selected"' : '') + '>End The Call</option>'
        out << '<option' + (action?.instanceOf(GoToMenuAction) ? ' selected="selected"' : '') + '>Go To A Menu...</option>'
        out << '<option' + (action?.instanceOf(LaunchApplicationAction) ? ' selected="selected"' : '') + '>Launch An Application...</option>'
        out << '<option' + (!action || action.instanceOf(ReplayMenuAction) ? ' selected="selected"' : '') + '>Replay This Menu</option>'
        out << '</select>'
    }

    def attendantApplicationSelect = { attrs ->
        def action = attrs.action

        def hide = !action?.instanceOf(LaunchApplicationAction)
        def value = action?.instanceOf(LaunchApplicationAction) && action?.applicationName ? action?.applicationName : ''

        out << listen.applicationSelect(value: value, hide: hide)
    }

    def dialNumberInput = { attrs ->
        def action = attrs.action

        out << '<input'
        out << ' type="text"'
        out << ' class="number-to-dial"'
        if(action?.instanceOf(DialNumberAction)) {
            out << ' value="' + fieldValue(bean: action, field: 'number') + '"'
        } else {
            out << ' style="display: none;"'
        }
        out << ' placeholder="Enter number..."'
        out << '/>'
    }

    def menuSelect = { attrs ->
        def action = attrs.action
        def group = attrs.group

        out << '<select class="menu-select"' + (action?.instanceOf(GoToMenuAction) ? '' : ' style="display: none;"') + '>'
        out << '<option>-- Select A Menu --</option>'
        group?.menusInDisplayOrder()?.each { menu ->
            out << '<option' + (action?.instanceOf(GoToMenuAction) && action?.destinationMenuName == menu.name ? ' selected="selected"' : '') + ">${fieldValue(bean: menu, field: 'name')}</option>"
        }
        out << '<option>Create New Menu...</option>'
        out << '</select>'
    }

    def entryMenuSelect = { attrs ->
        def group = attrs.group

        out << '<select class="entry-menu-select">'
        group?.menusInDisplayOrder()?.each { menu ->
            out << '<option' + (menu?.isEntry ? ' selected="selected"' : '') + ">${fieldValue(bean: menu, field: 'name')}</option>"
        }
        out << '</select>'
    }
}
