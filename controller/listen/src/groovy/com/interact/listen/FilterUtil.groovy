package com.interact.listen

final class FilterUtil
{
    public boolean shouldLog(def controller, def action)
    {
        if(controller.equals('messages') && ['pollingList', 'newCount', 'newAcdCount'].contains(action)) {
            return false
        }
        if(controller.equals('login') && action.equals('authAjax')) {
            return false
        }
        if(controller.equals('conferencing') && ['polledConference', 'ajaxPagination'].contains(action)) {
            return false
        }
        if(controller.equals('fax') && action.equals('prepareStatus')) {
            return false
        }
        if(controller.equals('administration') && action.equals('callsData')) {
            return false
        }
        if(controller.equals('acd') && ['polledCalls', 'pollQueue', 'pollStatus','agentStatus','status','callQueue','callHistory'].contains(action))
        {
            return false
        }
        if(controller.equals('profile') && ['index', 'settings','phones'].contains(action)) {
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
