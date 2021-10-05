package com.example.google_maps_gorbachev.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.google_maps_gorbachev.Models.Loc

@Database(entities = [Loc::class], version = 2, exportSchema = false)
abstract class MapsDatabase: RoomDatabase() {
	abstract fun MapsDao() : MapsDao
}