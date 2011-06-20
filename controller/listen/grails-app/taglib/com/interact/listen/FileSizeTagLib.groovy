package com.interact.listen

class FileSizeTagLib {
    static namespace = 'listen'

    static final def BYTES_PER_MEGABYTE = 1024 * 1024

    def megabytes = { attrs ->
        def bytes = attrs.bytes as int
        def mega = bytes / BYTES_PER_MEGABYTE

        out << g.formatNumber(number: mega, format: '#,##0.0') + ' MB'
    }
}
