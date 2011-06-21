package com.interact.listen

import org.joda.time.DateTime

class User {

	boolean accountExpired
	boolean accountLocked
    boolean isActiveDirectory = false
    String emailAddress
	boolean enabled
    DateTime lastLogin
    Organization organization // TODO should user belongsTo organization? (maybe be tricky if organization is nullable)
	String password
    String realName
	boolean passwordExpired
	String username

    String pass
    String confirm

    static transients = ['pass', 'confirm'] // used for confirming a password

    static hasMany = [phoneNumbers: PhoneNumber]

    // TODO should organization be nullable (below)?
	static constraints = {
        emailAddress blank: false, email: true, maxSize: 100
        lastLogin nullable: true
        organization nullable: true
		password blank: false
        realName blank: false, maxSize: 50
		username blank: false, unique: 'organization', maxSize: 50, matches: '^[^:]+$'

        // transient pass and confirm validation for screen entry
        pass nullable: true, blank: true, validator: { val, obj ->
            return ((val?.trim()?.length() > 0 || obj?.confirm?.trim()?.length() > 0) && val != obj.confirm) ? 'does.not.match' : true
        }
        confirm nullable: true, blank: true
	}

	static mapping = {
		password column: '`password`'
	}

	Set<Role> getAuthorities() {
		UserRole.findAllByUser(this).collect { it.role } as Set
	}

    boolean hasRole(def role) {
        def roleStrings = getAuthorities().collect { it.authority }
        return roleStrings.contains(role)
    }

    static def lookupByPhoneNumber(def number) {
        PhoneNumber.findByNumber(number)?.owner
    }

    boolean canDial(def destination) {
        //Check global restrictions first, which do not have exceptions
        def globalRestrictions = GlobalOutdialRestriction.findAll().inject([:]) { globalMap, restriction ->
            globalMap[restriction.pattern] = restriction
            return globalMap
        }

        def matcher = new WildcardNumberMatcher()
        def globalMatch = matcher.findMatch(destination, globalRestrictions)
        if(globalMatch) {
            // global match found, no exceptions allowed at global level, deny
            log.warn "User [${this}] cannot dial [${destination}], matched global restriction [${globalMatch.pattern}]"
            return false
        }
        
        def restrictions = OutdialRestriction.withCriteria {
            or {
                eq('target', this)
                and {
                    isNull('target')
                    eq('organization', this.organization)
                }
            }
        }.inject([:]) { map, restriction ->
            map[restriction.pattern] = restriction
            return map
        }

        def match = matcher.findMatch(destination, restrictions)
        if(!match) {
            // no restrictions, destination is okay
            log.debug "User [${this}] is allowed to dial [${destination}], no organization restrictions matched"
            return true
        }

        // if exception found, allow; otherwise deny
        boolean hasException = OutdialRestrictionException.countByTargetAndRestriction(this, match) > 0
        if(!hasException) {
            log.warn "User [${this}] cannot dial [${destination}], matched organization restriction [${match.pattern}]"
            return false
        }

        log.debug "User [${this}] is allowed to dial [${destination}], user has an exception for an organization restriction"
        return true
    }

    def friendlyName() {
        realName ?: username
    }

    String toString() {
        username
    }
}