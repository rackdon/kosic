package rackdon.kosic.utils

interface DatabaseCleaner {
    /**
     * Get table names from the database.
     * @return a list of table names
     */
    fun getTableNames(): List<String>

    /**
     * Utility method that truncates tables
     * @param tables - Tables to truncate. All tables as default
     * @param excludedTables - Tables to exclude. None as default
     */
    fun truncate(tables: List<String> = getTableNames(), excludedTables: List<String> = emptyList()): Unit
}
