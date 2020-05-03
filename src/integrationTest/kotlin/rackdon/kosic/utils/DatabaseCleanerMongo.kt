package rackdon.kosic.utils

import org.springframework.data.mongodb.core.ReactiveMongoTemplate

class DatabaseCleanerMongo(private val template: ReactiveMongoTemplate): DatabaseCleaner {

    override fun getTableNames(): List<String> {
        return template.collectionNames.collectList().block()!!//.k().unsafeRunSync()!!
    }

    override fun truncate(tables: List<String>, excludedTables: List<String>): Unit {
         tables.filter { !excludedTables.contains(it) }
            .forEach { template.dropCollection(it).block() }
    }
}