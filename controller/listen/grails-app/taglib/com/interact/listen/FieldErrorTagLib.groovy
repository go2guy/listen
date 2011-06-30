package com.interact.listen

class FieldErrorTagLib {
    static namespace = 'listen'

    def validationClass = { attrs, body ->
        if(!attrs.bean || !attrs.field) return
        if(attrs.bean.errors.hasFieldErrors(attrs.field)) {
            out << 'validation-error'
        }
    }
}
