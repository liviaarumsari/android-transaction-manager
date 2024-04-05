package com.example.abe.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE transactions ADD COLUMN latitude REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE transactions ADD COLUMN longitude REAL NOT NULL DEFAULT 0.0")
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE transactions ADD COLUMN location TEXT NOT NULL DEFAULT ''")
    }
}

@Database(entities = [Transaction::class], version = 3)
@TypeConverters(Converters::class)
abstract class TransactionDatabase : RoomDatabase() {
    abstract fun transactionDAO(): TransactionDAO

    private class TransactionDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    val transactionDAO = database.transactionDAO()

                    transactionDAO.deleteAll()

                    var trx = Transaction(0, "a@gmail.com", "Shopping Spree", 1999, true, Date(), 0.0, 0.0, "")
                    transactionDAO.insert(trx)
                    trx = Transaction(0, "a@gmail.com", "Selling Spree", 2000, false, Date(), 0.0, 0.0, "")
                    transactionDAO.insert(trx)
                }
            }
        }
    }


    companion object {
        @Volatile
        private var INSTANCE: TransactionDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TransactionDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransactionDatabase::class.java,
                    "transaction_database"
                ).addCallback(TransactionDatabaseCallback(scope)).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}