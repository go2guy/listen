package com.interact.listen.pbx

class NumberRouteTests extends GroovyTestCase {

    void testPatternInvalidConstraints() {
        def route

        route = new NumberRoute(type: NumberRoute.Type.EXTERNAL, pattern: null)
        assertFalse route.validate()
        assertEquals 'nullable', route.errors.getFieldError('pattern').code

        route = new NumberRoute(type: NumberRoute.Type.EXTERNAL, pattern: ' ')
        assertFalse route.validate()
        assertEquals 'blank', route.errors.getFieldError('pattern').code

        route = new NumberRoute(type: NumberRoute.Type.EXTERNAL, pattern: String.format('%051d', 0))
        assertFalse route.validate()
        assertEquals 'maxSize.exceeded', route.errors.getFieldError('pattern').code

        route = new NumberRoute(type: NumberRoute.Type.EXTERNAL, pattern: 'a')
        assertFalse route.validate()
        assertEquals 'matches.invalid', route.errors.getFieldError('pattern').code

        // wildcard in middle
        route = new NumberRoute(pattern: '0*1')
        assertFalse route.validate()
        assertEquals 'matches.invalid', route.errors.getFieldError('pattern').code

        // TODO unique constraint tests
    }

    void testPatternValidConstraints() {
        def route

        // numeric
        route = new NumberRoute(type: NumberRoute.Type.EXTERNAL, pattern: '14024768786')
        route.validate()
        assertFalse route.errors.hasFieldErrors('pattern')

        // numeric
        route = new NumberRoute(pattern: '770')
        route.validate()
        assertFalse route.errors.hasFieldErrors('pattern')

        // wildcard at end
        route = new NumberRoute(pattern: '00*')
        route.validate()
        assertFalse route.errors.hasFieldErrors('pattern')

        // lone wildcard
        route = new NumberRoute(pattern: '*')
        route.validate()
        assertFalse route.errors.hasFieldErrors('pattern')
    }

    void testDestinationInvalidConstraints() {
        def route

        route = new NumberRoute(type: NumberRoute.Type.EXTERNAL, destination: null)
        assertFalse route.validate()
        assertEquals 'nullable', route.errors.getFieldError('destination').code

        route = new NumberRoute(type: NumberRoute.Type.EXTERNAL, destination: ' ')
        assertFalse route.validate()
        assertEquals 'blank', route.errors.getFieldError('destination').code

        route = new NumberRoute(type: NumberRoute.Type.EXTERNAL, destination: String.format('%0101d', 0))
        assertFalse route.validate()
        assertEquals 'maxSize.exceeded', route.errors.getFieldError('destination').code
    }

    void testOrganizationInvalidConstraints() {
        def route

        route = new NumberRoute(type: NumberRoute.Type.EXTERNAL, organization: null)
        assertFalse route.validate()
        assertEquals 'nullable', route.errors.getFieldError('organization').code
    }
}
