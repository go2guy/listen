package com.interact.listen

import com.interact.listen.pbx.SipPhone

class ProvisionerTemplateFieldValue {
	String fieldValue
	SipPhone sipPhone

	static belongsTo = [provisionerTemplateField: ProvisionerTemplateField]

    static constraints = {
	    fieldValue nullable: true
    }
}
