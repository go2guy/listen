package com.interact.listen.android

import com.interact.listen.android.GoogleAuthService.Error

class GoogleAuthConfiguration {
    String authUser = ''
    String authPass = ''
    String authToken = ''
    boolean isEnabled = false
    Error lastError
    Date nextRetry
    long retryTimeout = 1000

    static constraints = {
        authUser blank: true
        authPass blank: true
        authToken blank: true, maxSize: 2048
        lastError nullable: true
        nextRetry nullable: true
    }
}
