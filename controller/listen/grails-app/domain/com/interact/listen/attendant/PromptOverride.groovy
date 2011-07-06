package com.interact.listen.attendant

import com.interact.listen.Organization
import org.joda.time.LocalDate

class PromptOverride {
    Menu menu
    LocalDate date
    String optionsPrompt

    static belongsTo = [organization: Organization]

    static constraints = {
        optionsPrompt: blank: false
    }
}
