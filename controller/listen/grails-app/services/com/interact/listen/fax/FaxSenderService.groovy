package com.interact.listen.fax

import com.interact.listen.UserFile
import com.interact.listen.stats.Stat
import org.joda.time.DateTime
import org.joda.time.LocalDate

class FaxSenderService {
    def backgroundService
    def historyService
    def statWriterService
    def springSecurityService
    def spotCommunicationService

    void send(OutgoingFax fax) {
        if(fax.sourceFiles.size() < 1) {
            throw new IllegalStateException("Cannot send outgoing fax with id [${fax.id}], no files have been uploaded")
        }

        spotCommunicationService.sendFax(fax)
        fax.dateSent = new DateTime()
        if(fax.save()) {
            // TODO delete files?
            historyService.sentFax(fax)
            statWriterService.send(Stat.SENT_FAX)
        }
    }
}
