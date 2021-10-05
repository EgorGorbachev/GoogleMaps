package com.example.google_maps_gorbachev.viewModels

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.google_maps_gorbachev.Models.Loc
import com.example.google_maps_gorbachev.repository.MapsRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class MapViewModel @Inject constructor(
	application: Application,
	private val repository: MapsRepository
) : AndroidViewModel(application) {
	
	private var address: Address? = null
	private var addressName: Address? = null
	
	var currentLoc: LatLng? = null
	
	val allData: LiveData<List<Loc>> = repository.allLoc
	
	fun deleteFromDatabase(loc: Loc) {
		GlobalScope.launch {
			repository.delete(loc)
		}
	}
	
	fun insertDatabase(loc: Loc) {
		GlobalScope.launch {
			repository.add(loc)
		}
	}
	
	fun bitmapDescriptorFromVector(
		context: Context,
		vectorResId: Int
	): BitmapDescriptor {
		val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
		vectorDrawable!!.setBounds(
			0,
			0,
			vectorDrawable.intrinsicHeight,
			vectorDrawable.intrinsicWidth
		)
		val bitmap = Bitmap.createBitmap(
			vectorDrawable.intrinsicWidth,
			vectorDrawable.intrinsicHeight,
			Bitmap.Config.ARGB_8888
		)
		val canvas = Canvas(bitmap)
		vectorDrawable.draw(canvas)
		return BitmapDescriptorFactory.fromBitmap(bitmap)
	}
	
	private fun geoLocateFromName(searchString: String, geocoder: Geocoder) {
		var list = listOf<Address>()
		try {
			list = geocoder.getFromLocationName(searchString, 1)
		} catch (e: IOException) {
			Log.e("geocoder", "${e.message}")
		}
		
		address = if (list.isNotEmpty()) {
			list[0]
		} else {
			null
		}
	}
	
	private fun geoLocateFromLoc(latLng: LatLng, geocoder: Geocoder) {
		var list = listOf<Address>()
		try {
			list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
		} catch (e: IOException) {
			Log.e("geocoder", "${e.message}")
		}
		addressName = if (list.isNotEmpty()) {
			list[0]
		} else {
			null
		}
	}
	
	fun getAddressLatLng(searchString: String, geocoder: Geocoder): LatLng? {
		geoLocateFromName(searchString, geocoder)
		return if (address != null) {
			LatLng(address!!.latitude, address!!.longitude)
		} else {
			null
		}
	}
	
	fun getTitle(searchString: String, geocoder: Geocoder): String? {
		geoLocateFromName(searchString, geocoder)
		return if (address != null) {
			address!!.getAddressLine(0)
		} else {
			null
		}
	}
	
	fun getAddressLine(latLng: LatLng, geocoder: Geocoder): String? {
		geoLocateFromLoc(latLng, geocoder)
		return if (addressName != null) {
			addressName!!.getAddressLine(0)
		} else {
			null
		}
	}
	
	
}