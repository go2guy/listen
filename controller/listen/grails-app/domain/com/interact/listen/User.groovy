package com.interact.listen

import com.interact.listen.acd.AcdCallHistory
import com.interact.listen.acd.AcdUserStatus
import com.interact.listen.pbx.Extension
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import com.interact.listen.acd.Skill
import grails.plugin.multitenant.core.Tenant;

class User implements Tenant
{
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

    /* ACD user settings */
    //static hasMany = [phoneNumbers: PhoneNumber, skills: Skill]
    static hasMany = [phoneNumbers: PhoneNumber, acdCallHistory: AcdCallHistory]
    static hasOne = [acdUserStatus: AcdUserStatus]

    static transients = ['pass', 'confirm', 'enabledForLogin']
    /* pass & confirm are solely for password confirmation,
       enabledForLogin is used for ??? */

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

    @Override
    Integer tenantId()
    {
        return organization.id;
    }
    
    Set<Role> getAuthorities() {
        UserRole.findAllByUser(this).collect { it.role } as Set
    }
    
    boolean hasRole(String authority) {
        def roleStrings = getAuthorities().collect { it.authority }
        return roleStrings.contains(authority)
    }

    static def lookupByPhoneNumber(def number)
    {
        return (PhoneNumber.findByNumber(number)?.owner);
    }
    
    static def lookupByPhoneNumberAndOrganization(String number, Organization organization)
    {
        Log LOG = LogFactory.getLog(this);
        if(LOG.isDebugEnabled())
        {
            LOG.debug("Looking up User by number[" + number + "], organization[" + organization.getName() + "]");
        }

        if(number.size() == organization.extLength + 1)
        {
            number = number.substring(1);

            if(LOG.isDebugEnabled())
            {
                LOG.debug("Number truncated to [" + number + "]");
            }
        }

        User user = createCriteria().get {
            phoneNumbers {
                eq('number', number)
            }
            eq('organization', organization)
        }

        if(LOG.isDebugEnabled() && user != null)
        {
            LOG.debug("User is [" + user.getRealName() + "]");
        }

        return user;
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
    
    def enabled() {
        enabled && (!organization || organization.enabled)
    }
    
    // create a couple of 'fake' properties so our authentication can use them
    // to determine if the user is enabled, considering both the user and organization
    // 'enabled' values
    void setEnabledForLogin(boolean value) { /* no-op */ }
    boolean getEnabledForLogin() {
        enabled()
    }
    
    String toString() {
        username
    }
}
