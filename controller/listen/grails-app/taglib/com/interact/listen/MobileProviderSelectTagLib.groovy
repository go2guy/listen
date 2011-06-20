package com.interact.listen

class MobileProviderSelectTagLib {
    static namespace = 'listen'

    def mobileProviderSelect = { attrs ->
        if(!attrs.name) throwTagError 'Tag [mobileProviderSelect] is missing required attribute [name]'

        def providers = [
            'message.alltel.com'     : 'Alltel',
            'txt.att.net'            : 'AT&T',
            'myboostmobile.com'      : 'Boost Mobile',
            'sms.mycricket.com'      : 'Cricket',
            'qwestmp.com'            : 'Quest',
            'messaging.sprintpcs.com': 'Sprint',
            'tmomail.net'            : 'T-Mobile',
            'email.uscc.net'         : 'US Cellular',
            'vtext.com'              : 'Verizon',
            'vmobl.com'              : 'Virgin Mobile'
        ]

        out << g.select(name: attrs.name, value: attrs.value, from: providers, optionKey: 'key', optionValue: 'value')
    }
}
