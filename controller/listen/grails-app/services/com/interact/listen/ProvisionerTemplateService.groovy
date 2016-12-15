package com.interact.listen

import com.interact.listen.pbx.SipPhone

class ProvisionerTemplateService {

	def addField(ProvisionerTemplate template, String name, String defaultValue) {
		def sipPhones = SipPhone.findAllByProvisionerTemplate(template)

		def field = new ProvisionerTemplateField(name: name, defaultValue: defaultValue, provisionerTemplate: template).save()

		log.debug("Checking sipPhones with this template...")
		sipPhones.each { phone ->
			log.debug("Adding field for phone ${phone}")
			new ProvisionerTemplateFieldValue( provisionerTemplateField: field, fieldValue: defaultValue, sipPhone: phone).save(failOnError: true)
		}
	}

	def removeField(ProvisionerTemplateField field) {
		ProvisionerTemplateFieldValue.where { provisionerTemplateField == field }.deleteAll()
		field.delete()
	}

	def updateField(ProvisionerTemplateField field, String name, String defaultValue) {
		if (field.name != name || field.defaultValue != defaultValue) {
			field.name = name
			field.defaultValue = defaultValue
			field.save()

			def values = ProvisionerTemplateFieldValue.where { provisionerTemplateField == field && fieldValue == field.defaultValue }

			values.each {
				it.fieldValue = defaultValue
				it.save()
			}
		}
	}
}
