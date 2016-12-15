package com.interact.listen

class ProvisionerTemplate {
	String name
	String template

	static hasMany = [provisionerTemplateFields: ProvisionerTemplateField]

	static constraints = {
		provisionerTemplateFields nullable: true
	}
}
