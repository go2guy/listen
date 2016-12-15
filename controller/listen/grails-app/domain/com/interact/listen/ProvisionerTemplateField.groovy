package com.interact.listen

class ProvisionerTemplateField {
	String name
	String defaultValue

	static belongsTo = [provisionerTemplate: ProvisionerTemplate]

	static constraints = {
		defaultValue nullable: true
	}
}
