package com.interact.listen

class AudioDownloadService {
    static scope = 'singleton'
    static transactional = false

    def download(Audio audio, def response, boolean mp3 = false) {
        def url = new URL(mp3 ? audio.mp3Uri() : audio.uri)
        def uri = new URI(url.protocol, url.userInfo, url.host, url.port, url.path, url.query, null)

        def connection = uri.toURL().openConnection()
        connection.requestMethod = 'GET'
        connection.connect()

        def input = connection.inputStream
        try {
            response.contentLength = connection.contentLength
        } catch(NumberFormatException e) {
            log.warn "File size [${audio.fileSize}] not a parseable integer for Audio [${audio}]"
        }

        response.contentType = mp3 ? 'audio/mpeg' : audio.detectContentType()
        response.setHeader('Content-disposition', "attachment;filename=${getFileName(audio)}")

        response.outputStream << input
        response.flushBuffer()
    }

    private String getFileName(Audio audio)
    {
        String uri = audio.getUri();
        return uri.substring(uri.lastIndexOf("/") + 1);
    }
}
