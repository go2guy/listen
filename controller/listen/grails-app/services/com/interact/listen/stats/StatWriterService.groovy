package com.interact.listen.stats

import com.interact.insa.client.StatsPublisher
import com.interact.insa.client.StatsPublisher.Operator
import org.springframework.beans.factory.InitializingBean

class StatWriterService implements InitializingBean {
    static scope = 'singleton'
    static transactional = false

    void send(def stat) {
        send(stat, null)
    }

    void send(def statId, def value) {
        def operation = (value == null ? Operator.INCREMENT : Operator.SET)
        def v = (value ?: 1)
        log.debug "Writing stat $statId with value $v and operation ${operation == Operator.INCREMENT ? 'Increment' : 'Set'}"
        StatsPublisher.send(statId, v, operation)
    }

    void afterPropertiesSet() {
        StatsPublisher.setSource('LISTEN')
    }
}
