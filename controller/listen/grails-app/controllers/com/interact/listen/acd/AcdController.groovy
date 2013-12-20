package com.interact.listen.acd

import com.interact.listen.acd.AcdCall
import com.interact.listen.acd.AcdUserStatus
import com.interact.listen.PhoneNumber

import grails.plugin.springsecurity.annotation.Secured

@Secured(['ROLE_ACD_USER'])
class AcdController {
    static allowedMethods = [
      index: 'GET',
      status: 'GET',
      updateStatus: 'POST'
    ]

    def index = {
      redirect(action: 'status')
    }

    def callQueue = {
      def calls = AcdCall.findAll()
      render(view: 'callQueue', model: [calls: calls])
    }

    def pollQueue = {
      def json = [:]
      json.calls = AcdCall.findAll()
      
      render(contentType: 'application/json') {
        json
      }
    }

    def status = {
      def acd_user_status = AcdUserStatus.findByOwner(authenticatedUser)
      def status = acd_user_status?.acdQueueStatus?.toString()
      def contactNumber = acd_user_status?.contactNumber?.number
      def phoneNumbers = []

      def optionNames = AcdQueueStatus.values()

      PhoneNumber.findAllByOwner(authenticatedUser).each() { number ->
        phoneNumbers.add(number.number)
      }

      def model = [
        status: status,
        optionNames: optionNames,
        phoneNumbers: phoneNumbers,
        contactNumber: contactNumber
      ]

      log.debug "Rendering view [status] with model [${model}]"
      render(view: 'status', model: model)
    }

    def updateStatus = {
      def acd_user_status = AcdUserStatus.findByOwner(authenticatedUser)
      acd_user_status.acdQueueStatus = AcdQueueStatus.fromString(params.status)
      acd_user_status.contactNumber = PhoneNumber.findByNumber(params.contactNumber)

      try {
        if (acd_user_status.validate()) {
          if (!acd_user_status.save(failOnError: true, flush: true)) {
            log.debug "Could not update user acd status."
          }
        }
      }
      catch (Exception e) {
        log.debug "Caught exception saving acd user status [${e}]"
      }

      redirect(action: 'status')
    }

}
