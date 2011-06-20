package com.interact.listen

class PaginateTagLib {
    static namespace = 'listen'

    def paginateTotal = { attrs ->
        def total = (attrs.total ?: 0) as int
        def prefix = attrs.messagePrefix ?: 'paginate.total.generic'
        def number = g.formatNumber(number: total)

        out << '<div class="listTotal">'
        out << g.message(code: "${prefix}.${total == 1 ? 'singular' : 'plural'}", args: [number])
        out << '</div>'
    }
}
