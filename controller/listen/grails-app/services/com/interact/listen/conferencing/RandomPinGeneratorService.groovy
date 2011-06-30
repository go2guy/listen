package com.interact.listen.conferencing

import java.security.SecureRandom

class RandomPinGeneratorService {
    static scope = 'singleton'
    static transactional = false

    def grailsApplication

    def generate(int length) {
        if(length <= 0) {
            throw new IllegalArgumentException('Cannot generate pin with length <= 0')
        }

        def random = new SecureRandom()
        def result = new StringBuilder()
        length.times {
            result.append(random.nextInt(10))
        }
        return result.toString()
    }

    def createConferencePin(Conference conference, PinType type) {
        def configuration = ConferencingConfiguration.findByOrganization(conference.owner.organization)
        def length = configuration ? configuration.pinLength : grailsApplication.config.com.interact.listen.conferencing.defaultPinLength
        def number = generate(length)
        while(Pin.countByNumber(number) != 0) {
            // avoid duplicates
            // TODO should probably put some sort of limit on the number of retries here
            number = generate(length)
        }

        return new Pin(pinType: type,
                       conference: conference,
                       number: number)
    }
}
