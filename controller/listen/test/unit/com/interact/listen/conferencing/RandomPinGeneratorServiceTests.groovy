package com.interact.listen.conferencing

class RandomPinGeneratorServiceTests extends GroovyTestCase {

    def service

    void setUp() {
        service = new RandomPinGeneratorService()
    }

    // 10-digit number generation
    void testGenerate0() {
        def result = service.generate(10)
        assertTrue result.length() == 10
        assertTrue result ==~ /[0-9]+/
    }

    // negative length argument passed yields exception
    void testGenerate1() {
        shouldFail(IllegalArgumentException) {
            service.generate(-1)
        }
    }

    // passed length of zero yields exception
    void testGenerate2() {
        shouldFail(IllegalArgumentException) {
            service.generate(0)
        }
    }
}
