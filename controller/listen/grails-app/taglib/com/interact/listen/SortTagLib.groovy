package com.interact.listen

class SortTagLib {
    static namespace = 'listen'

    def sortLink = { attrs ->
        if(!attrs.action) throwTagError 'Tag [sortLink] is missing required attribute [action]'
        if(!attrs.controller) throwTagError 'Tag [sortLink] is missing required attribute [controller]'
        if(!attrs.property) throwTagError 'Tag [sortLink] is missing required attribute [property]'
        if(!attrs.title) throwTagError 'Tag [sortLink] is missing required attribute [title]'

        def props = [:]
        props.sort = attrs.property
        props.order = 'asc'

        def currentSort = params.sort
        def currentOrder = params.order
        if(currentSort == attrs.property && currentOrder == 'asc') {
            props.order = 'desc'
        }

        out << g.link(controller: attrs.controller, action: attrs.action, params: props) { attrs.title }
    }
}
