package com.interact.listen

final class FilterUtil {
    static boolean shouldLog(def controller, def action) {
        if(controller == 'voicemail' && ['pollingList', 'newCount'].contains(action)) {
            return false
        }
        if(controller == 'login' && action == 'authAjax') {
            return false
        }
        if(controller == 'conferencing' && ['polledConference', 'ajaxPagination'].contains(action)) {
            return false
        }
        return true
    }

    static boolean shouldExtractOrganization(def controller, def action) {
        if(['spotApi', 'callRouting'].contains(controller)) {
            return false
        }
        return true
    }
}
