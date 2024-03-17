package com.example.abe.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date

@Database(entities = [Transaction::class], version = 1)
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

                    var trx = Transaction(0, "a@gmail.com", "Shopping Spree", 1999, true, Date())
                    transactionDAO.insert(trx)
                    trx = Transaction(0, "a@gmail.com", "Selling Spree", 2000, false, Date())
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
                ).addCallback(TransactionDatabaseCallback(scope)).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}