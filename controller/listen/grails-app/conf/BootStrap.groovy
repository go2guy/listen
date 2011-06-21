import com.interact.listen.*
import com.interact.listen.attendant.*
import com.interact.listen.conferencing.*
import com.interact.listen.pbx.*
import com.interact.listen.voicemail.Voicemail
import com.interact.listen.history.*
import org.codehaus.groovy.grails.commons.ApplicationAttributes
import org.joda.time.Duration
import org.joda.time.DateTime

class BootStrap {

    def customMailMessageBuilderFactory
    def mailService
    def springSecurityService
    def userCreationService
    def licenseService

    def init = { servletContext ->

        // override mail service factory bean
        mailService.mailMessageBuilderFactory = customMailMessageBuilderFactory

        def ctx = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT)
        def dataSource = ctx.dataSourceUnproxied
        dataSource.properties.each { k, v ->
            log.debug "dataSource.$k = [$v]"
        }

        // custodian roles
        createRole('ROLE_CUSTODIAN')

        // operator/administrative roles
        createRole('ROLE_ATTENDANT_ADMIN')
        createRole('ROLE_ORGANIZATION_ADMIN')

        // application roles
        createRole('ROLE_CONFERENCE_USER')
        createRole('ROLE_FINDME_USER')
        createRole('ROLE_VOICEMAIL_USER')

        // TODO should custodian creation be part of the bootstrap? if so, should the email address be changed?
        def custodian = createCustodian('Custodian', 'Custodian', 'custodian@example.com', 'super')

        environments {
            development {

                def organization = new Organization(name: 'Interact Incorporated')
                licenseService.licensableFeatures().each {
                    organization.addToEnabledFeatures(it)
                }
                organization.save(flush: true)

                def operator = createOperator('Operator', 'Operator McSillyPants', 'operator@example.com', 'super', organization)
                def user = createUser('User', 'User McSillypants', 'user@example.com', 'super', organization)
                def rob = createUser('Rob', 'Rob Hruska', 'hruskar@iivip.com', 'super', organization)
                def phillip = createUser('Phillip', 'Phillip Rapp', 'user@example.com', 'super', null)

                def greeting = new Audio(description: 'Greeting',
                                         duration: new Duration(1000),
                                         fileSize: '16252928',
                                         transcription: '',
                                         uri: 'http://www.example.com').save(flush: true)

                new ActionHistory(dateTime: new DateTime(),
                                  byUser: phillip,
                                  onUser: phillip,
                                  action: Action.DELETED_VOICEMAIL,
                                  description: 'Deleting Subscriber',
                                  channel: Channel.GUI).save(flush: true)

                new ActionHistory(dateTime: new DateTime(),
                                  byUser: phillip,
                                  onUser: phillip,
                                  action: Action.LISTENED_TO_VOICEMAIL,
                                  description: 'Deleting Subscriber',
                                  channel: Channel.GUI).save(flush: true)

                new PhoneNumber(greeting: greeting,
                                number: '378',
                                owner: user,
                                type: PhoneNumberType.EXTENSION).save(flush: true)

                for(i in 1..30) {
                    new PhoneNumber(greeting: greeting,
                                    number: "290-$i",
                                    owner: rob,
                                    isPublic: false,
                                    type: PhoneNumberType.EXTENSION).save(flush: true)
                }

                new PhoneNumber(greeting: greeting,
                                number: '101',
                                owner: operator,
                                type: PhoneNumberType.EXTENSION).save(flush: true)
                new PhoneNumber(greeting: greeting,
                                number: '102',
                                owner: operator,
                                type: PhoneNumberType.EXTENSION).save(flush: true)
                new PhoneNumber(greeting: greeting,
                                number: '4025604557',
                                owner: operator,
                                type: PhoneNumberType.VOICEMAIL).save(flush: true)

                def a0 = new Audio(description: 'Test Voicemail',
                                   duration: new Duration(1000000),
                                   fileSize: '1024',
                                   transcription: 'Transcription pending...',
                                   uri: 'http://www.example.com').save(flush: true)
                new Voicemail(ani: '4025604557',
                              audio: a0,
                              owner: user).save(flush: true)

                def a1 = new Audio(description: 'Second audio file',
                                   duration: new Duration(1000),
                                   fileSize: '1024',
                                   transcription: 'Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp Herp derp ',
                                   uri: 'http://www.example.com').save(flush: true)
                new Voicemail(ani: '378',
                              audio: a1,
                              owner: user).save(flush: true)

                for(i in 1..30) {
                    def a = new Audio(description: 'Foo',
                                      duration: new Duration(10000),
                                      fileSize: '1024',
                                      transcription: 'Hurrrrrrrrrr!!! LOLcats. meow. hey hey',
                                      uri: 'http://www.example.com').save(flush: true)
                    new Voicemail(ani: '14024768786',
                                  audio: a,
                                  isNew: false,
                                  owner: user).save(flush: true)
                }

                // some routing numbers
                new NumberRoute(type: NumberRoute.Type.EXTERNAL,
                                organization: organization,
                                number: '4024768786',
                                destination: 'Attendant').save(flush: true)
                new NumberRoute(type: NumberRoute.Type.EXTERNAL,
                                organization: organization,
                                number: '18002428649',
                                destination: 'Attendant').save(flush: true)
                new NumberRoute(type: NumberRoute.Type.EXTERNAL,
                                organization: organization,
                                number: '5125029969',
                                destination: 'Attendant').save(flush: true)
                new NumberRoute(type: NumberRoute.Type.INTERNAL,
                                organization: organization,
                                pattern: '770',
                                destination: 'Voicemail').save(flush: true)

                // some outdialing restrictions
                new OutdialRestriction(organization: organization,
                                       pattern: '411',
                                       target: user).save(flush: true)
                new OutdialRestriction(organization: organization,
                                       pattern: '1900*').save(flush: true)
                def r = new OutdialRestriction(organization: organization,
                                               pattern: '1800*').save(flush: true)
                new OutdialRestrictionException(restriction: r,
                                                target: user).save(flush: true)
                assert OutdialRestriction.count() == 3
                assert OutdialRestrictionException.count() == 1

                for(i in 1..100) {
                    def c = new CallHistory(ani: '123',
                                            dnis: '456',
                                            dateTime: new DateTime(),
                                            duration: new Duration(1000),
                                            fromUser: user,
                                            toUser: user,
                                            organization: organization)
                    c.validate()
                    c.save(flush: true)
                }

                for(i in 1..100) {
                    def a = new ActionHistory(byUser: user,
                                              onUser: user,
                                              action: Action.LOGGED_IN,
                                              description: 'Bootstrapped',
                                              channel: Channel.GUI,
                                              organization: organization)
                    a.validate()
                    a.save(flush: true)
                }

                new MenuGroup(name: 'Default',
                              organization: organization,
                              isDefault: true).save()

                def conference = Conference.findByOwner(user)
                assert conference != null

                conference.isStarted = true
                conference.startTime = new DateTime()
                conference.arcadeId = '1'
                conference.save(flush: true)

                new Recording(audio: greeting, conference: conference).save(flush: true)
                new Recording(audio: greeting, conference: conference).save(flush: true)

/*                new Participant(ani: '378',
                                conference: conference,
                                isAdmin: true,
                                recordedName: greeting,
                                sessionId: '1').save(flush: true)
                new Participant(ani: '342',
                                conference: conference,
                                recordedName: greeting,
                                sessionId: '2').save(flush: true)
                new Participant(ani: '359',
                                conference: conference,
                                isPassive: true,
                                recordedName: greeting,
                                sessionId: '3').save(flush: true)
                new Participant(ani: '347',
                                conference: conference,
                                isAdminMuted: true,
                                recordedName: greeting,
                                sessionId: '4').save(flush: true)*/

                for(i in 1..30) {
                    new Participant(ani: '100' + i,
                                    conference: conference,
                                    recordedName: greeting,
                                    sessionId: '100' + i).save(flush: true)
                }
            }
        }
    }

    def destroy = {
        // no-op
    }

    private def createRole(def authority) {
        def role = Role.findByAuthority(authority)
        if(!role) {
            role = new Role(authority: authority).save(flush: true)
        }
        return role
    }

    private def createCustodian(def username, def realName, def emailAddress, def password) {
        def user = User.findByUsername(username)
        if(user) return user

        def params = [
            username: username,
            realName: realName,
            emailAddress: emailAddress,
            pass: password,
            confirm: password
        ]
        return userCreationService.createCustodian(params)
    }

    private def createOperator(def username, def realName, def emailAddress, def password, def organization) {
        def user = User.findByUsername(username)
        if(user) return user

        def params = [
            username: username,
            realName: realName,
            emailAddress: emailAddress,
            pass: password,
            confirm: password
        ]
        return userCreationService.createOperator(params, organization)
    }

    private def createUser(def username, def realName, def emailAddress, def password, def organization) {
        def user = User.findByUsername(username)
        if(user) return user

        def params = [
            username: username,
            realName: realName,
            emailAddress: emailAddress,
            pass: password,
            confirm: password
        ]
        return userCreationService.createUser(params, organization)
    }
}