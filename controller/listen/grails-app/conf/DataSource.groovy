dataSource {
    pooled = true
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = false
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
        dataSource {
            dialect = 'org.hibernate.dialect.MySQLInnoDBDialect'
            driverClassName = 'org.mariadb.jdbc.Driver'
            url = System.getProperty('com.interact.listen.db.url', 'jdbc:mysql://coreylisten212.newnet.local/listen2?zeroDateTimeBehavior=convertToNull')
            username = System.getProperty('com.interact.listen.db.username', 'root')
            password = System.getProperty('com.interact.listen.db.password', '')
            validationQuery = 'SELECT 1'
        }
    }
    test {
        dataSource {
            dbCreate = "update"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
    production {
        dataSource {
            dialect = 'org.hibernate.dialect.MySQLInnoDBDialect'
            driverClassName = 'org.mariadb.jdbc.Driver'
            url = System.getProperty('com.interact.listen.db.url', 'jdbc:mysql://localhost/listen2?zeroDateTimeBehavior=convertToNull')
            username = System.getProperty('com.interact.listen.db.username', 'root')
            password = System.getProperty('com.interact.listen.db.password', '')
            validationQuery = 'SELECT 1'
        }
    }
}
