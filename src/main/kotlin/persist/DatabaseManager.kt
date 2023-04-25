package me.hbj.bikkuri.persist

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import me.hbj.bikkuri.utils.absPath
import me.hbj.bikkuri.utils.resolveWorkDirectory
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseManager {
  val database: Database
    get() = databaseInstance ?: error("Database instance is not prepared")

  private var databaseInstance: Database? = null

  /**
   * load database, should be invoked before any table operations
   */
  fun loadDatabase(): Database {
    val config = HikariConfig().apply {
      jdbcUrl = "jdbc:sqlite:${resolveWorkDirectory("data.db").absPath}?foreign_keys=on"
      driverClassName = "org.sqlite.JDBC"
      maximumPoolSize = 1
      transactionIsolation = "TRANSACTION_SERIALIZABLE"
    }
    val dataSource = HikariDataSource(config)
    val db = Database.connect(dataSource)
    TransactionManager.manager.defaultIsolationLevel =
      IsolationLevel.TRANSACTION_SERIALIZABLE.levelId
    databaseInstance = db
    return db
  }

  /**
   * load tables and add missing schema for compatible
   *
   * should be invoked before table use
   */
  fun <T : Table> loadTables(vararg table: T) = transaction(database) {
    SchemaUtils.createMissingTablesAndColumns(*table)
  }

  fun loadTables(table: List<Table>) = transaction(database) {
    SchemaUtils.createMissingTablesAndColumns(*(table.toTypedArray()))
  }
}
