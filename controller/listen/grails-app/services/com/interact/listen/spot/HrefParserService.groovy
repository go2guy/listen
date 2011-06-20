package com.interact.listen.spot

class HrefParserService {
    static scope = 'singleton'
    static transactional = false

    def idFromHref(def href) {
        if(!href) {
            return null
        }

        if(!href.matches("\\/[^\\/]+\\/[^\\/]+")) {
            return null
        }

        return href.substring(href.lastIndexOf('/') + 1) as long
    }
}
