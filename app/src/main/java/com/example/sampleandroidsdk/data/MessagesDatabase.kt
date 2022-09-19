package com.example.sampleandroidsdk.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sampleandroidsdk.Converters


@Database(entities = [Messages::class], version = 1)
@TypeConverters(Converters::class)
abstract class MessagesDatabase : RoomDatabase() {
    abstract fun messagesDao(): MessagesDao

    companion object {

        @Volatile
        private var INSTANCE: MessagesDatabase? = null

        fun getInstance(context: Context): MessagesDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MessagesDatabase::class.java,
                        "messagesDatabase"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}