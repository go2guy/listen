package com.interact.listen.attendant

import grails.validation.ValidationException
import org.springframework.validation.Errors

class MenuGroupValidationException extends ValidationException {
    private def groups

    public MenuGroupValidationException(String message, Errors errors, def groups) {
        super(message, errors)
        this.groups = groups
    }
}
