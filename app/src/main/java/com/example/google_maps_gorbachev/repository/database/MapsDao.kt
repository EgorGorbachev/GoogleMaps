package com.example.google_maps_gorbachev.repository.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.google_maps_gorbachev.Models.Loc

@Dao
interface MapsDao {
	@Query("SELECT * FROM loc_table")
	fun readAllData(): LiveData<List<Loc>>
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun add(photoLocation: Loc)
	
	@Delete
	suspend fun delete(photoLocation: Loc)
	
	@Update
	suspend fun update(photoLocation: Loc)
}