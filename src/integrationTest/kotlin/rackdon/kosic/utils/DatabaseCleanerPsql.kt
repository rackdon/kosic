package rackdon.kosic.utils

import javax.persistence.EntityManager

class DatabaseCleanerPsql(
    private val entityManager: EntityManager,
    private val versionControlTableNames: List<String> = listOf("databasechangelog", "databasechangeloglock")
) : DatabaseCleaner {

    override fun getTableNames(): List<String> {

        val versionControlName = versionControlTableNames.joinToString(", ") { "'$it'" }
        return entityManager.createNativeQuery(
            "SELECT table_name FROM information_schema.tables\n" +
                    "WHERE table_schema = 'public' AND table_name NOT IN($versionControlName)"
        )
            .resultList
            .map { it.toString() }
    }

    override fun truncate(tables: List<String>, excludedTables: List<String>): Unit {
        val finalTables = tables.filter { !excludedTables.contains(it) }
        entityManager.flush()
        entityManager.createNativeQuery("TRUNCATE  ${finalTables.joinToString(", ")} CASCADE")
            .executeUpdate()
    }
}
