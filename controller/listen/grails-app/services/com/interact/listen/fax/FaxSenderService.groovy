package com.interact.listen.fax

import com.interact.listen.UserFile
import com.interact.listen.stats.Stat
import com.sun.media.jai.codec.*
import javax.media.jai.*
import org.joda.time.DateTime
import org.joda.time.LocalDate

class FaxSenderService {
    def backgroundService
    def historyService
    def statWriterService
    def springSecurityService
    def spotCommunicationService

    // merges multiple PDF files into a single TIFF.
    // this takes a some time, depending on the number of files (seconds to a minute or two).
    void prepareInBackground(OutgoingFax fax) {
        def user = springSecurityService.getCurrentUser()

        backgroundService.execute("Preparing outgoing fax [id: ${fax.id}]", {
            log.debug "Creating storage directory"
            def storageDirectory = createDirectories(user)

            fax.preparationStatus = 'Converting'
            fax.save(flush: true)

            log.debug "Converting PDF(s) to TIFF(s)"
            def convertedTifs = convertPdfToTif(fax.toMerge, storageDirectory)

            fax.preparationStatus = 'Combining'
            fax.save(flush: true)

            log.debug "Combining TIFF(s)"
            def mergeResult = combineToSingleTif(convertedTifs, storageDirectory)
            fax.merged = mergeResult.file
            fax.pages = mergeResult.pages
            fax.datePrepared = new DateTime()
            fax.preparationStatus = 'Complete'

            log.debug "Saving prepared fax with merged: ${fax.merged}"
            fax.save(flush: true)
        })
    }

    void send(OutgoingFax fax) {
        if(!fax.merged || !fax.merged.exists()) {
            throw new IllegalStateException("Cannot send outgoing fax with id [${fax.id}], it has not been prepared")
        }

        spotCommunicationService.sendFax(fax)
        fax.dateSent = new DateTime()
        if(fax.save()) {
            // TODO delete files?
            historyService.sentFax(fax)
            statWriterService.send(Stat.SENT_FAX)
        }
    }

    private def createDirectories(def user){
        // TODO fix hard-coded path
        def dir = new File("/interact/listen/artifacts/${user.id}/fax/")

        if(!dir.exists() && !dir.mkdirs()) {
            throw new FileNotFoundException('Could not user user-specific fax storage directory')
        }

        if(!dir.isDirectory()) {
            throw new IllegalStateException('User-specific fax storage path is not a directory')
        }

        log.debug "Using [${dir}] to store fax files"

        return dir.getAbsolutePath()
    }

    private def convertPdfToTif(List<UserFile> userFiles, def storageDirectory) {
        def tifList = []
        userFiles.collect { it.file }.each {
            String tif = storageDirectory + it.name
            int dpi = 300;
            int color = TIFConvert.CLR_RGB;
            int compression = TIFConvert.COMPRESSION_CCITT_T_6;
            float quality = 1f;

            TIFConvertPDF.convert(it.bytes, tif, dpi, color, compression, quality);
            tifList.add(new File(tif))
        }

        return tifList
    }

    private def combineToSingleTif(def convertedTifs, def storageDirectory) {
        def images = []
        def totalPages = 0
        convertedTifs.each {
            SeekableStream ss = new FileSeekableStream(it);
            ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", ss, null);
            
            int numPages = decoder.getNumPages();
            totalPages += numPages
            for(int j = 0; j < numPages; j++)
            {
                PlanarImage op = new NullOpImage(decoder.decodeAsRenderedImage(j), null, null, OpImage.OP_IO_BOUND);
                images.add(op.getAsBufferedImage());
            }
        }
        
        TIFFEncodeParam params = new TIFFEncodeParam();
        params.setCompression(TIFFEncodeParam.COMPRESSION_GROUP4);

        def saveLocation = storageDirectory + "/${new LocalDate().getLocalMillis()}.tif"
        OutputStream out = new FileOutputStream(saveLocation); 
        ImageEncoder encoder = ImageCodec.createImageEncoder("tiff", out, params);
        def imageList = []   

        //remove the first page from the list that gets set as extra images because the first page is passed to 
        //the encode() method.  do not want the first page twice
        for (int i = 1; i < images.size(); i++)
        {
            imageList.add(images.get(i)); 
        }
        params.setExtraImages(imageList.iterator()); 
        encoder.encode(images.get(0));
        out.close();

        return [
            file: new File(saveLocation),
            pages: totalPages
        ]
    }
}
