package com.interact.listen

class HelpTagLib {
    static namespace = 'listen'

    def infoSnippet = { attrs, body ->
        if(!attrs.summaryCode) throwTagError 'Tag [infoSnippet] is missing required attribute [summaryCode]'
        if(!attrs.contentCode) throwTagError 'Tag [infoSnippet] is missing required attribute [contentCode]'

        out << '<div class="info-snippet">'
        out << '  <div class="summary">' + g.message(code: attrs.summaryCode) + '</div>' + g.message(code: attrs.contentCode)
        out << '</div>'
    }
}
