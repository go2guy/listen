package com.interact.listen

import com.interact.listen.history.CallHistory
import grails.plugins.springsecurity.Secured
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.format.ISODateTimeFormat

@Secured(['ROLE_ORGANIZATION_ADMIN'])
class ReportsController {
    static defaultAction = 'list'
    static allowedMethods = [
        list: 'GET',
        callVolumesByUser: 'GET'
    ]

    def list = {
        render(view: 'list')
    }

    def callVolumesByUser = {
        // this is inefficient, but will do for now
        // TODO optimize if it becomes slow

        def formatter = ISODateTimeFormat.date()
        def now = new DateTime()
        params.start = params.start ?: formatter.print(now.minusDays(7))
        params.end = params.end ?: formatter.print(now)

        def start = formatter.parseDateTime(params.start).withTime(0, 0, 0, 0)
        def end = formatter.parseDateTime(params.end).withTime(23, 59, 59, 999)

        if(start.isAfter(end)) {
            end = start
            params.end = formatter.print(end)
        }

        def users = [:]
        def totals = callReportMap()

        User.list().each { user ->
            users.put(user.id, [name: user.realName, numbers: [:]])

            CallHistory.withCriteria {
                eq('fromUser', user)
                ge('dateTime', start)
                le('dateTime', end)
            }.each { call ->
                if(!users[user.id]['numbers'].containsKey(call.ani)) {
                    users[user.id]['numbers'].put(call.ani, callReportMap())
//                    addEmptyCallReportStructure(users[user.id]['numbers'], call.ani)
                }

                def type = call.dnis.size() < 4 ? 'internal' : 'external'
                users[user.id]['numbers'][call.ani]['outbound'][type]['count']++
                users[user.id]['numbers'][call.ani]['outbound'][type]['duration'] = users[user.id]['numbers'][call.ani]['outbound'][type]['duration'].plus(call.duration)
                users[user.id]['numbers'][call.ani]['outbound']['total']['count']++
                users[user.id]['numbers'][call.ani]['outbound']['total']['duration'] = users[user.id]['numbers'][call.ani]['outbound']['total']['duration'].plus(call.duration)
                users[user.id]['numbers'][call.ani]['total']['count']++
                users[user.id]['numbers'][call.ani]['total']['duration'] = users[user.id]['numbers'][call.ani]['total']['duration'].plus(call.duration)

                totals['outbound'][type]['count']++
                totals['outbound'][type]['duration'] = totals['outbound'][type]['duration'].plus(call.duration)
                totals['outbound']['total']['count']++
                totals['outbound']['total']['duration'] = totals['outbound']['total']['duration'].plus(call.duration)
                totals['total']['count']++
                totals['total']['duration'] = totals['total']['duration'].plus(call.duration)
            }

            CallHistory.withCriteria {
                eq('toUser', user)
                ge('dateTime', start)
                le('dateTime', end)
            }.each { call ->
                if(!users[user.id]['numbers'].containsKey(call.dnis)) {
                    users[user.id]['numbers'].put(call.dnis, callReportMap())
//                    addEmptyCallReportStructure(users[user.id]['numbers'], call.dnis)
                }

                def type = call.dnis.size() < 4 ? 'internal' : 'external'
                users[user.id]['numbers'][call.dnis]['inbound'][type]['count']++
                users[user.id]['numbers'][call.dnis]['inbound'][type]['duration'] = users[user.id]['numbers'][call.dnis]['inbound'][type]['duration'].plus(call.duration)
                users[user.id]['numbers'][call.dnis]['inbound']['total']['count']++
                users[user.id]['numbers'][call.dnis]['inbound']['total']['duration'] = users[user.id]['numbers'][call.dnis]['inbound']['total']['duration'].plus(call.duration)
                users[user.id]['numbers'][call.dnis]['total']['count']++
                users[user.id]['numbers'][call.dnis]['total']['duration'] = users[user.id]['numbers'][call.dnis]['total']['duration'].plus(call.duration)

                totals['inbound'][type]['count']++
                totals['inbound'][type]['duration'] = totals['inbound'][type]['duration'].plus(call.duration)
                totals['inbound']['total']['count']++
                totals['inbound']['total']['duration'] = totals['inbound']['total']['duration'].plus(call.duration)
                totals['total']['count']++
                totals['total']['duration'] = totals['total']['duration'].plus(call.duration)
            }
        }

        users = users.sort { user ->
            def sum = user.value.numbers.inject(new Duration(0)) { duration, number -> return duration.plus(number.value.total.duration) }
            return sum.millis * -1
        }

        render(view: 'callVolumesByUser', model: [calls: users, totals: totals])
    }

    private def callReportMap() {
        return [
            inbound: [
                internal: [
                    count: 0,
                    duration: new Duration(0)
                ],
                external: [
                    count: 0,
                    duration: new Duration(0)
                ],
                total: [
                    count: 0,
                    duration: new Duration(0)
                ]
            ],
            outbound: [
                internal: [
                    count: 0,
                    duration: new Duration(0)
                ],
                external: [
                    count: 0,
                    duration: new Duration(0)
                ],
                total: [
                    count: 0,
                    duration: new Duration(0)
                ]
            ],
            total: [
                count: 0,
                duration: new Duration(0)
            ]
        ]
        //to.put(key, value)
    }
}
