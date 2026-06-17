package com.doku.sdkcheckoutandroid.db

import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.doku.sdkcheckoutandroid.dao.TransactionDao
import com.doku.sdkcheckoutandroid.entity.Transaction
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import java.util.concurrent.Executors

@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class
TransactionDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao



    companion object {
        @Volatile
        private var INSTANCE: TransactionDatabase? = null
        fun getInstance(): TransactionDatabase {
            return INSTANCE ?: synchronized(this) {
                val ctx = DokuConfig.appContext

                val instance = Room.databaseBuilder(
                    ctx,
                    TransactionDatabase::class.java,
                    "transaction_db"
                )
                    .fallbackToDestructiveMigration()
                    .setQueryCallback(
                        { sqlQuery, bindArgs ->
                            Log.d("ROOM_QUERY", "SQL: $sqlQuery | ARGS: $bindArgs")
                        },
                        Executors.newSingleThreadExecutor()
                    )
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }

}