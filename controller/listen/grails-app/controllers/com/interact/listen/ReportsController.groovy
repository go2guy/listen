package com.interact.listen

import com.interact.listen.history.CallHistory
import grails.plugins.springsecurity.Secured
import jxl.*
import jxl.write.*
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

    private def normal = new WritableCellFormat(new WritableFont(WritableFont.ARIAL, 11))
    private def bold = new WritableCellFormat(new WritableFont(WritableFont.ARIAL, 11, WritableFont.BOLD))

    def list = {
        render(view: 'list')
    }

    // TODO move the bulk of the report generation into a service

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

        withFormat {
            html {
                render(view: 'callVolumesByUser', model: [calls: users, totals: totals])
            }
            xls {
                def filename = "callVolumesByUser_${params.start}_to_${params.end}.xls"
                def report = callVolumesByUserXlsReport(users, totals, params)
                response.contentLength = report.size()
                response.contentType = 'application/vnd.ms-excel'
                response.setHeader('Content-disposition', "attachment;filename=${filename}")

                response.outputStream << report.newInputStream()
                response.flushBuffer()
            }
        }
    }

    private def callVolumesByUserXlsReport(def users, def totals, def params) {
        def file = File.createTempFile('callVolumesByUser', 'xls')
        file.deleteOnExit()

        def settings = new WorkbookSettings()
        settings.setLocale(Locale.default)

        def workbook = Workbook.createWorkbook(file, settings)
//        def sheet = workbook.createSheet("Call Volumes By User, ${params.start} to ${params.end}", 0)
        def sheet = workbook.createSheet("Call Volumes By User", 0)

        // header row
        sheet.addCell(new Label(0, 0, 'Name', bold))
        sheet.addCell(new Label(1, 0, 'Number', bold))

        sheet.addCell(new Label(2, 0, 'Outbound Internal #', bold))
        sheet.addCell(new Label(3, 0, 'Outbound Internal Duration', bold))
        sheet.addCell(new Label(4, 0, 'Outbound External #', bold))
        sheet.addCell(new Label(5, 0, 'Outbound External Duration', bold))
        sheet.addCell(new Label(6, 0, 'Outbound Total #', bold))
        sheet.addCell(new Label(7, 0, 'Outbound Total Duration', bold))

        sheet.addCell(new Label(8, 0, 'Inbound Internal #', bold))
        sheet.addCell(new Label(9, 0, 'Inbound Internal Duration', bold))
        sheet.addCell(new Label(10, 0, 'Inbound External #', bold))
        sheet.addCell(new Label(11, 0, 'Inbound External Duration', bold))
        sheet.addCell(new Label(12, 0, 'Inbound Total #', bold))
        sheet.addCell(new Label(13, 0, 'Inbound Total Duration', bold))

        sheet.addCell(new Label(14, 0, 'Total #', bold))
        sheet.addCell(new Label(15, 0, 'Total Duration', bold))

        def row = 1
        users.each { userid, user ->
            user.numbers.each { number, calls ->
                addCell(sheet, 0, row, user.name)
                addCell(sheet, 1, row, number)

                addCell(sheet, 2, row, calls.outbound.internal.count)
                addCell(sheet, 3, row, calls.outbound.internal.duration)
                addCell(sheet, 4, row, calls.outbound.external.count)
                addCell(sheet, 5, row, calls.outbound.external.duration)
                addCell(sheet, 6, row, calls.outbound.total.count)
                addCell(sheet, 7, row, calls.outbound.total.duration)

                addCell(sheet, 8, row, calls.inbound.internal.count)
                addCell(sheet, 9, row, calls.inbound.internal.duration)
                addCell(sheet, 10, row, calls.inbound.external.count)
                addCell(sheet, 11, row, calls.inbound.external.duration)
                addCell(sheet, 12, row, calls.inbound.total.count)
                addCell(sheet, 13, row, calls.inbound.total.duration)

                addCell(sheet, 14, row, calls.total.count)
                addCell(sheet, 15, row, calls.total.duration)

                row++
            }
        }

        addCell(sheet, 0, row, 'Total')
        addCell(sheet, 1, row, '')

        addCell(sheet, 2, row, totals.outbound.internal.count)
        addCell(sheet, 3, row, totals.outbound.internal.duration)
        addCell(sheet, 4, row, totals.outbound.external.count)
        addCell(sheet, 5, row, totals.outbound.external.duration)
        addCell(sheet, 6, row, totals.outbound.total.count)
        addCell(sheet, 7, row, totals.outbound.total.duration)

        addCell(sheet, 8, row, totals.inbound.internal.count)
        addCell(sheet, 9, row, totals.inbound.internal.duration)
        addCell(sheet, 10, row, totals.inbound.external.count)
        addCell(sheet, 11, row, totals.inbound.external.duration)
        addCell(sheet, 12, row, totals.inbound.total.count)
        addCell(sheet, 13, row, totals.inbound.total.duration)

        addCell(sheet, 14, row, totals.total.count)
        addCell(sheet, 15, row, totals.total.duration)

        workbook.write()
        workbook.close()

        log.debug "Generated 'Call Volume By Users' report with file size [${file.size()}]"

        return file
    }

    private void addCell(def sheet, def col, def row, String data) {
        sheet.addCell(new Label(col, row, data as String, normal))
    }

    private void addCell(def sheet, def col, def row, Duration duration) {
        sheet.addCell(new Label(col, row, listen.formatduration(duration: duration, zeroes: false, millis: false) as String, normal))
    }

    private void addCell(def sheet, def col, def row, int data) {
        sheet.addCell(new jxl.write.Number(col, row, data, normal))
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
