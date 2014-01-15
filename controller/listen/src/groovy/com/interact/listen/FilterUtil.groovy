package com.interact.listen

final class FilterUtil {
    static boolean shouldLog(def controller, def action) {
        if(controller.equals('messages') && ['pollingList', 'newCount', 'newAcdCount'].contains(action)) {
            return false
        }
        if(controller.equals('login') && action == 'authAjax') {
            return false
        }
        if(controller.equals('conferencing') && ['polledConference', 'ajaxPagination'].contains(action)) {
            return false
        }
        if(controller.equals('fax') && action == 'prepareStatus') {
            return false
        }
        if(controller.equals('administration') && action == 'callsData') {
            return false
        }
        return true
    }

    static boolean shouldExtractOrganization(def controller, def action) {
        if(['spotApi', 'callRouting', 'faxApi'].contains(controller)) {
            return false
        }
        return true
    }
}
