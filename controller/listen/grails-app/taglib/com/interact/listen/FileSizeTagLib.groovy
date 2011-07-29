package com.interact.listen

class FileSizeTagLib {
    static namespace = 'listen'

    static final def BYTES_PER_MEGABYTE = 1024 * 1024
    static final def BYTES_PER_KILOBYTE = 1024

    def megabytes = { attrs ->
        def bytes
        if(attrs.file && attrs.file.exists()) {
            bytes = attrs.file.length()
        } else if(attrs.bytes) {
            bytes = attrs.bytes as int
        }

        if(bytes && bytes < BYTES_PER_MEGABYTE) {
            def kilo = bytes / BYTES_PER_KILOBYTE
            out << g.formatNumber(number: kilo, format: '#,##0.0') + ' KB'
        } else if (bytes && bytes >= BYTES_PER_MEGABYTE) {
            def mega = bytes / BYTES_PER_MEGABYTE
            out << g.formatNumber(number: mega, format: '#,##0.0') + ' MB'
        } else {
            out << attrs.unavailable ?: '-'
        }
    }
}
