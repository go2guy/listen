package com.interact.listen.voicemail.afterhours

import com.interact.listen.httpclient.HttpClientImpl

class RealizeAlertUpdateService {
    static scope = 'singleton'
    static transactional = false

    def backgroundService

    def sendUpdate(AfterHoursConfiguration afterHours, def originalNumber) {
        backgroundService.execute('Updating realize alerts', {
            log.debug "Updating realize alerts named [${afterHours.realizeAlertName}] at url [${afterHours.realizeUrl}], changing number [${originalNumber}] to [${afterHours.alternateNumber}]"

            if(afterHours.realizeUrl != '' && afterHours.realizeAlertName != '') {
                def url = afterHours.realizeUrl
                if(!url.endsWith('/')) {
                    url += '/'
                }
                url += 'alert/replaceEmailAddress'

                def httpClient = new HttpClientImpl()
                httpClient.setSocketTimeout(3000)
                def params = [
                    name: afterHours.realizeAlertName
                ]
                if(originalNumber && !originalNumber.trim().equals('')) {
                    params.remove = originalNumber
                }
                if(afterHours.alternateNumber != null && !afterHours.alternateNumber.trim().equals('')) {
                    params.add = afterHours.alternateNumber
                }

                httpClient.post(url, params)
            } else {
                log.warn 'Incomplete after hours configuration for realize system, alerts will not be updated'
            }
        })
    }
}
