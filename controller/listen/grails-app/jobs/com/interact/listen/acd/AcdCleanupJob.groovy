package com.interact.listen.acd

/**
 * Created with IntelliJ IDEA.
 * User: knovak
 * Date: 12/13/13
 * Time: 10:08 AM
 * To change this template use File | Settings | File Templates.
 */
class AcdCleanupJob
{
    static triggers =
    {
        simple startDelay: 1000, repeatInterval: 30000
    }

    def group = "acd"

    def acdService

    def execute()
    {
        //Determine if any users show "onacall" when they aren't

    }
}
