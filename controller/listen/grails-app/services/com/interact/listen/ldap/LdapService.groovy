package com.interact.listen.ldap

import com.interact.listen.DirectMessageNumber
import com.interact.listen.MobilePhone
import com.interact.listen.Organization
import com.interact.listen.User
import com.interact.listen.license.ListenFeature
import com.interact.listen.pbx.Extension
import org.apache.directory.shared.ldap.entry.ModificationOperation
import org.apache.directory.shared.ldap.entry.client.ClientModification
import org.apache.directory.shared.ldap.entry.client.DefaultClientAttribute
import org.apache.directory.shared.ldap.exception.LdapAttributeInUseException
import org.apache.directory.shared.ldap.exception.LdapNoSuchAttributeException
import org.apache.directory.shared.ldap.name.LdapDN

class LdapService {
    def grailsApplication
    def licenseService
    def listenLdapServer

    // TODO actions yet to finish:
    // - update username (involves moving the DN, might be tricky)
    // - update user real name (coded, but not working)
    // - update organizaton name
    // - update organization context path (involves moving many DNs, which might be tricky)

    // Optional, but we probably need an LDAP schema change to accommodate them
    // - add mobile number
    // - update mobile number
    // - remove mobile number
    // - add other number
    // - update other number
    // - remove other number
    // - role management?

    // loads all information from the database into the directory
    void init() {
        log.debug 'Initializing LDAP server from database'
        Organization.list().each { organization ->
            addOrganization(organization)
        }

        User.list().each { user ->
            addUser(user)
        }

        Extension.list().each { extension ->
            addExtension(extension.owner, extension.number)
        }

        DirectMessageNumber.list().each { directMessageNumber ->
            if(licenseService.canAccess(ListenFeature.FAX, directMessageNumber.owner.organization)) {
                addFaxNumber(directMessageNumber.owner, directMessageNumber.number)
            }
        }

        MobilePhone.list().each { mobilePhone ->
            addMobileNumber(mobilePhone.owner, mobilePhone.number)
        }
    }

    void addOrganization(Organization organization) {
        addLdif("""
dn: dc=${organization.contextPath},${baseDn()}
ou: ${organization.contextPath}
description: ${organization.name}
objectclass: organizationalunit
""")

        addLdif("""
dn: ou=people,dc=${organization.contextPath},${baseDn()}
ou: people
description: People
objectclass: organizationalunit
""")
    }

    void addUser(User user) {
        if(!user.organization) {
            return
        }

        // TODO sn is required by the schema, but we do not store it
        addLdif("""
dn: uid=${user.username},ou=people,dc=${user.organization.contextPath},${baseDn()}
uid: ${user.username}
objectclass: inetOrgPerson
cn: ${user.realName}
displayName: ${user.realName}
sn: ${user.realName}
mail: ${user.emailAddress}
o: ${user.organization.name}
""")
    }

    void addExtension(User user, def number) {
        addAttribute(userDn(user), 'telephoneNumber', number)
    }

    void changeExtension(User user, def fromNumber, def toNumber) {
        replaceAttribute(userDn(user), 'telephoneNumber', fromNumber, toNumber)
    }

    void removeExtension(User user, def number) {
        removeAttribute(userDn(user), 'telephoneNumber', number)
    }

    void addFaxNumber(User user, def number) {
        addAttribute(userDn(user), 'facsimileTelephoneNumber', number)
    }

    void changeFaxNumber(User user, def fromNumber, def toNumber) {
        replaceAttribute(userDn(user), 'facsimileTelephoneNumber', fromNumber, toNumber)
    }

    void removeFaxNumber(User user, def number) {
        removeAttribute(userDn(user), 'facsimileTelephoneNumber', number)
    }

    void addMobileNumber(User user, def number) {
        addAttribute(userDn(user), 'mobile', number)
    }

    void changeMobileNumber(User user, def fromNumber, def toNumber) {
        replaceAttribute(userDn(user), 'mobile', fromNumber, toNumber)
    }

    void removeMobileNumber(User user, def number) {
        removeAttribute(userDn(user), 'mobile', number)
    }

    void changeEmailAddress(User user, def from, def to) {
        replaceAttribute(userDn(user), 'mail', from, to)
    }

    // FIXME not working; no errors but values dont change
    void changeName(User user, def name) {
        changeAttribute(userDn(user), 'cn', name)
        changeAttribute(userDn(user), 'displayName', name)
    }

    private void addAttribute(String dn, def attribute, def value) {
        def modifications = [
            new ClientModification(ModificationOperation.ADD_ATTRIBUTE, new DefaultClientAttribute(attribute, value))
        ] as List
        log.debug "Adding attribute [${attribute}] = [${value}] to dn [${dn}]"
        modify(new LdapDN(dn), modifications)
    }

    // does a remove followed by an add (good for attributes that support multiple values)
    private void replaceAttribute(String dn, def attribute, def oldValue, def newValue) {
        def modifications = [
            new ClientModification(ModificationOperation.REMOVE_ATTRIBUTE, new DefaultClientAttribute(attribute, oldValue)),
            new ClientModification(ModificationOperation.ADD_ATTRIBUTE, new DefaultClientAttribute(attribute, newValue))
        ] as List
        log.debug "Replacing attribute [${attribute}] value [${oldValue}] with [${newValue}] for dn [${dn}]"
        modify(new LdapDN(dn), modifications)
    }

    // does a straight replace (good for attributes that cannot be removed); should not be used for multi-value attributes
    private void changeAttribute(String dn, def attribute, def value) {
        def modifications = [
            new ClientModification(ModificationOperation.REPLACE_ATTRIBUTE, new DefaultClientAttribute(attribute, value))
        ] as List
        log.debug "Changing attribute [${attribute}] value to [${value}] for dn [${dn}]"
    }

    private void removeAttribute(String dn, def attribute, def value) {
        def modifications = [
            new ClientModification(ModificationOperation.REMOVE_ATTRIBUTE, new DefaultClientAttribute(attribute, value))
        ]
        log.debug "Removing attribute [${attribute}] with value [${value}] from dn [${dn}]"
        modify(new LdapDN(dn), modifications)
    }

    private void modify(LdapDN dn, List modifications) {
        try {
            listenLdapServer.directoryService.adminSession.modify(dn, modifications)
        } catch(LdapAttributeInUseException e) {
            log.error e
        } catch(LdapNoSuchAttributeException e) {
            log.error e
        }
    }

    private def addLdif(String ldif) {
        log.debug "Adding LDIF: ${ldif}"
        listenLdapServer.loadLdif(ldif)
    }

    private def baseDn() {
        grailsApplication.config.com.interact.listen.ldap.basedn
    }

    private def userDn(User user) {
        if(!user.organization) {
            throw new AssertionError("Unable to build dn for user [${user.username}] without an organization")
        }

        "uid=${user.username},ou=people,dc=${user.organization.contextPath},${baseDn()}"
    }
}
