package com.example.sampleandroidsdk

import android.util.Log
import androidx.room.TypeConverter
import com.nanorep.sdkcore.model.StatementScope
import java.util.*

class Converters {

    @TypeConverter
    fun toScope(scope: Int): StatementScope? {

        return try {
            StatementScope.values()[scope]

        } catch (e: IllegalArgumentException){
            Log.e("ScopeTypeConverter", "Illegal scope value")
            null
        }
    }

    @TypeConverter
    fun toDate(dateLong: Long?): Date? {
        return dateLong?.let { Date(it) }
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
}