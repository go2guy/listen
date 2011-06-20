dataSource {
    pooled = true
    driverClassName = "org.hsqldb.jdbcDriver"
    username = "sa"
    password = ""
    validationQuery = 'SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS'
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = 'create-drop'
            dialect = 'org.hibernate.dialect.MySQLInnoDBDialect'
            driverClassName = 'com.mysql.jdbc.Driver'
            url = System.getProperty('com.interact.listen.db.url', 'jdbc:mysql://localhost/listen2dev?zeroDateTimeBehavior=convertToNull')
            username = System.getProperty('com.interact.listen.db.username', 'root')
            password = System.getProperty('com.interact.listen.db.password', '')
            validationQuery = 'SELECT 1'
        }
//        dataSource {
//            dbCreate = "create-drop" // one of 'create', 'create-drop','update'
//            url = "jdbc:hsqldb:mem:devDB"
//        }
    }
    test {
        dataSource {
            dbCreate = "create-drop"
            url = "jdbc:hsqldb:mem:testDb"
        }
    }
    production {
        dataSource {
            dialect = 'org.hibernate.dialect.MySQLInnoDBDialect'
            driverClassName = 'com.mysql.jdbc.Driver'
            url = System.getProperty('com.interact.listen.db.url', 'jdbc:mysql://localhost/listen2?zeroDateTimeBehavior=convertToNull')
            username = System.getProperty('com.interact.listen.db.username', 'root')
            password = System.getProperty('com.interact.listen.db.password', '')
            validationQuery = 'SELECT 1'
        }
    }
}
