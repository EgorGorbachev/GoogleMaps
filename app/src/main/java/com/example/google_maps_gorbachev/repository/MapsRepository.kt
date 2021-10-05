package com.example.google_maps_gorbachev.repository

import androidx.lifecycle.LiveData
import com.example.google_maps_gorbachev.Models.Loc
import com.example.google_maps_gorbachev.repository.database.MapsDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapsRepository @Inject constructor(
	private val mapsDao: MapsDao
) {
	
	val allLoc: LiveData<List<Loc>> = mapsDao.readAllData()
	
	suspend fun add(photoLocation: Loc) {
		mapsDao.add(photoLocation)
	}
	
	suspend fun delete(photoLocation: Loc) {
		mapsDao.delete(photoLocation)
	}
	
	suspend fun update(photoLocation: Loc) {
		mapsDao.update(photoLocation)
	}
	
}