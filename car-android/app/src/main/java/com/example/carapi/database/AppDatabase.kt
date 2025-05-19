package com.example.carapi.database


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.carapi.database.converters.DateConverters
import com.example.carapi.database.dao.UserLocationDao
import com.example.carapi.database.model.UserLocation

@Database(entities = [UserLocation::class], version = 2, exportSchema = true)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userLocationDao(): UserLocationDao
}