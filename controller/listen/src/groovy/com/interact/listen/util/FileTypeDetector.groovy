package com.interact.listen.util

import java.io.FileInputStream
import java.io.IOException

import org.apache.tika.exception.TikaException
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.Parser
import org.apache.tika.sax.BodyContentHandler
import org.xml.sax.ContentHandler
import org.xml.sax.SAXException

class FileTypeDetector {

    public String detectContentType(File file) throws FileTypeDetectionException {
        def is = null
        try {
            is = new FileInputStream(file)
            
            def contentHandler = new BodyContentHandler()
            def metadata = new Metadata()
            metadata.set(Metadata.RESOURCE_NAME_KEY, file.name)
            def parser = new AutoDetectParser()
            parser.parse(is, contentHandler, metadata)

            return metadata.get(Metadata.CONTENT_TYPE)
        } catch(IOException e) {
            throw new FileTypeDetectionException(e)
        } catch(SAXException e) {
            throw new FileTypeDetectionException(e)
        } catch(TikaException e) {
            throw new FileTypeDetectionException(e)
        } finally {
            if(is) is.close()
        }
    }
}
