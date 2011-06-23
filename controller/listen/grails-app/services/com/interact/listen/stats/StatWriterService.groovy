package com.interact.listen.stats

import com.interact.insa.client.StatsPublisher
import com.interact.insa.client.StatsPublisher.Operator
import org.springframework.beans.factory.InitializingBean

class StatWriterService implements InitializingBean {
    static scope = 'singleton'
    static transactional = false

    void send(Stat stat) {
        send(stat, null)
    }

    void send(Stat stat, def value) {
        def operation = (value == null ? Operator.INCREMENT : Operator.SET)
        def v = (value ?: 1)
        log.debug "Writing stat ${stat.value()} with value $v and operation ${operation == Operator.INCREMENT ? 'Increment' : 'Set'}"
        StatsPublisher.send(stat.value(), v, operation)
    }

    void afterPropertiesSet() {
        StatsPublisher.setSource('LISTEN')
    }
}
