package com.interact.listen.acd

import com.interact.listen.acd.*
import org.apache.log4j.Logger

class AcdTagLib {
    static namespace = 'listen'

    def promptFileService
    def springSecurityService

    // TODO fix hard-coded path
    static final File storageLocation = new File('/interact/listen/artifacts/acd')
    
    def acdPromptSelect = { attrs ->
        def value = attrs.value

        def user = springSecurityService.getCurrentUser()

        def prompts = promptFileService.listNames(storageLocation, user.organization.id)
        out << '<select class="' + attrs.class + '" name="' + attrs.name + '">'
        out << '<option>-- No Prompt --</option>'
        prompts.each { prompt ->
            out << '<option' + (value && prompt == value ? ' selected="selected"' : '') + ">${prompt.encodeAsHTML()}</option>"
        }
        out << '<option>-- Upload New Prompt --</option>'
        out << '</select>'
    }

    def rawAcdPromptSelect = { attrs ->
        def user = springSecurityService.getCurrentUser()
        def prompts = promptFileService.listNames(storageLocation, user.organization.id)
        attrs.from = prompts
        out << g.select(attrs)
    }

}
