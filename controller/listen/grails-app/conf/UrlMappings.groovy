class UrlMappings {

    static mappings = {
        "/api/acdCall"(controller: 'spotApi')
        {
            action = [
                    POST: 'addAcdCall',
            ]
        }

        "/api/acdCall/voicemail"(controller: 'spotApi')
                {
                    action = [
                            GET: 'getAcdVoicemail'
                    ]
                }

        "/api/acdCall/$junk"(controller: 'spotApi')
        {
            action = [
                PUT: 'updateAcdCall'
            ]
        }



        "/api/accessNumbers"(controller: 'spotApi') {
            action = [
                    GET: 'listPhoneNumbers'
            ]
        }
        "/api/accessNumbers/$id"(controller: 'spotApi') {
            action = [
                    GET: 'getPhoneNumber',
                    PUT: 'updatePhoneNumber'
            ]
        }
        "/api/callDetailRecords"(controller: 'spotApi', action: 'addCallHistory')
        "/api/conferences/$id"(controller: 'spotApi') {
            action = [
                    'GET': 'getConference',
                    'PUT': 'updateConference'
            ]
        }
        "/api/participants"(controller: 'spotApi') {
            action = [
                    'GET': 'getParticipants',
                    'POST': 'addParticipant'
            ]
        }
        "/api/participants/$id"(controller: 'spotApi') {
            action = [
                    'DELETE': 'deleteParticipant',
                    'PUT': 'updateParticipant'
            ]
        }
        "/api/pins"(controller: 'spotApi') {
            action = [
                    'GET': 'getPin'
            ]
        }
        "/api/subscribers"(controller: 'spotApi') {
            action = [
                    'GET': 'listUsers'
            ]
        }
        "/api/subscribers/$id"(controller: 'spotApi') {
            action = [
                    'GET': 'getUser',
                    'PUT': 'updateUser'
            ]
        }
        "/api/voicemails"(controller: 'spotApi') {
            action = [
                    'GET': 'listVoicemails',
                    'POST': 'addVoicemail'
            ]
        }
        "/api/voicemails/$id"(controller: 'spotApi') {
            action = [
                    DELETE: 'deleteVoicemail',
                    GET: 'getVoicemail',
                    PUT: 'updateVoicemail'
            ]
        }
        "/api/routeCall"(controller: 'callRouting', action: 'routeCall')
        "/api/getBroadcastList"(controller: 'extension', action: 'getExtensionsByOrganization')

        "/meta/canAccessFeature"(controller: 'spotApi', action: 'canAccessFeature')
        "/meta/canDial"(controller: 'spotApi', action: 'canDial')
        "/meta/findMeNumbers"(controller: 'spotApi', action: 'dial')
        "/meta/getAccessNumber"(controller: 'spotApi', action: 'lookupAccessNumber')
        "/meta/getDnis"(controller: 'spotApi', action: 'dnisLookup')
        "/meta/getEnabledFeatureStatus"(controller: 'spotApi', action: 'getEnabledFeatureStatus')
        "/meta/getMenuAction"(controller: 'spotApi', action: 'menuAction')
        "/meta/registerSpotSystem"(controller: 'spotApi', action: 'register')
        "/meta/setPhoneNumber"(controller: 'spotApi', action: 'setPhoneNumber')

        "/meta/audio/file/$id"(controller: 'spotApi', action: 'androidVoicemailDownload')
        "/meta/contacts/emailContacts"(controller: 'spotApi', action: 'androidEmailContacts')
        "/meta/contacts/emailContacts/$id"(controller: 'spotApi', action: 'androidEmailContact')
        "/meta/contacts/numberContacts"(controller: 'spotApi', action: 'androidNumberContacts')
        "/meta/contacts/numberContacts/$id"(controller: 'spotApi', action: 'androidNumberContact')
        "/meta/registerDevice"(controller: 'spotApi') {
            action = [
                    GET: 'androidGetDeviceRegistration',
                    PUT: 'androidUpdateDeviceRegistration'
            ]
        }

        "/$organizationContext/$controller?/$action?/$id?"{
            controller = { controller ?: 'login' }
        }
        "/$controller/$action?/$id?"{
            constraints {
                // apply constraints here
            }
        }
/*		"/"(view:"/index")*/
        "/"(controller: "index")
        "500"(view:'/error')
    }
}

