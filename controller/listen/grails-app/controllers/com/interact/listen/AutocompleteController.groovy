package com.interact.listen

import grails.converters.JSON
import grails.plugins.springsecurity.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class AutocompleteController {
    static allowedMethods = [
        contacts: 'GET'
    ]

    def contacts = {
        def user = authenticatedUser

        def result = [
            my: [
                phones: [],
                mobiles: []
            ],
            all: [
                phones: [],
                direct: [],
                emails: []
            ]
        ]

        PhoneNumber.withCriteria() {
            owner {
                eq('organization', user.organization)
            }
            order('number', 'asc')
        }.each { phone ->
            if(phone.owner == user) {
                addPhone(phone, result.my.phones)
                if(isMobile(phone)) {
                    addPhone(phone, result.my.mobiles)
                }
            }
            
            if(!isMobile(phone) || phone.isPublic) {
                addPhone(phone, result.all.phones)
                if(isDirect(phone)) {
                    addPhone(phone, result.all.direct)
                }
            }
        }

        User.findAllByOrganization(user.organization, [sort: 'emailAddress', order: 'asc']).each { u ->
            result.all.emails << [
                value: u.emailAddress,
                label: "${u.realName} (${u.emailAddress})",
                name: u.realName
            ]
        }

        render result as JSON
    }

    private void addPhone(PhoneNumber phone, def list) {
        def value = [
            value: phone.number,
            label: "${phone.number} (${phone.owner.realName})",
            name: phone.owner.realName
            
        ]

        if(isMobile(phone)) {
            value.put('provider', phone.smsDomain)
        }

        list << value
    }

    private boolean isMobile(PhoneNumber phone) {
        phone.instanceOf(MobilePhone)
    }

    private boolean isDirect(PhoneNumber phone) {
        phone.instanceOf(DirectMessageNumber)
    }
}
