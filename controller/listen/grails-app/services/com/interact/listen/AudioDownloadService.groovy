package com.interact.listen

class AudioDownloadService {
    static transactional = false

    def download(Audio audio, def response, boolean mp3 = false) {
        def file = mp3 ? audio.mp3File() : audio.file

        response.contentLength = file.length()
        response.contentType = audio.detectContentType()
        response.setHeader('Content-disposition', "attachment;filename=${file.name}")

        response.outputStream << file.newInputStream()
        response.flushBuffer()
    }
}
