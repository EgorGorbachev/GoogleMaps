package com.example.google_maps_gorbachev.presentation.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geophoto.presentation.base.BaseFragment
import com.example.google_maps_gorbachev.Models.Loc
import com.example.google_maps_gorbachev.R
import com.example.google_maps_gorbachev.presentation.adapters.PlacesRecyclerAdapter
import com.example.google_maps_gorbachev.viewModels.MapViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet.bottomSheet
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_map.*
import java.lang.Exception
import java.util.*


const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 102
const val REQUEST_CODE = 101

@AndroidEntryPoint
class MapFragment : BaseFragment(R.layout.fragment_map), OnMapReadyCallback,
	PlacesRecyclerAdapter.OnItemClick, PlacesRecyclerAdapter.OnItemLongClick {
	
	private val viewModel by viewModels<MapViewModel>()
	
	//Maps
	private lateinit var fusedLocationClient: FusedLocationProviderClient
	private lateinit var googleMap: GoogleMap
	private var marker: Marker? = null
	private var yourLocationMarker: Marker? = null
	private lateinit var geocoder: Geocoder
	
	//Search View
	private var timer: Timer? = null
	private lateinit var bottomSheetDialog: BottomSheetDialog
	private lateinit var bottomSheetView: View
	
	//Adapters
	private var adapter = PlacesRecyclerAdapter(this, this)
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		getLocationPermission()
		
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
		geocoder = Geocoder(requireContext())
		
		bottomSheetDialog = BottomSheetDialog(requireActivity())
		bottomSheetView =
			LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet, bottomSheet)
		
		val supportMapFragment =
			childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
		supportMapFragment!!.getMapAsync(this)
		
		currentLocationBtn.setOnClickListener {
			getCurrentLoc()
			hideKeyboard()
		}
		
		input_search.isIconifiedByDefault = false
		input_search.clearFocus()
		
		
		val recyclerView: RecyclerView = bottomSheetView.findViewById(R.id.placesRecycler)
		recyclerView.adapter = adapter
		val layoutManager = LinearLayoutManager(requireContext())
		recyclerView.layoutManager = layoutManager
		showPlaces()
		
		showOptionsBtn.setOnClickListener {
			bottomSheetDialog.setContentView(bottomSheetView)
			bottomSheetDialog.show()
			
			bottomSheetView.addLocBtn.setOnClickListener {
				if (viewModel.currentLoc != null) {
					val curLoc = viewModel.currentLoc
					viewModel.insertDatabase(
						Loc(
							0,
							viewModel.getAddressLine(curLoc!!, geocoder)!!,
							viewModel.currentLoc!!.latitude,
							viewModel.currentLoc!!.longitude
						)
					)
					toast("Your loc added to list!")
				} else {
					toast("You have no current request")
				}
				showPlaces()
			}
		}
		
		searchLocationViewWatcher()
	}
	
	private fun showPlaces() {
		viewModel.allData.observe(viewLifecycleOwner, {
			adapter.submitList(it)
		})
	}
	
	override fun onItemClick(loc: Loc) {
		val mLatLng = LatLng(loc.lat, loc.lng)
		moveCamera(mLatLng, 10f)
		addMarker(mLatLng, loc.locName)
		bottomSheetDialog.dismiss()
	}
	
	
	override fun onItemLongClick(loc: Loc) {
		viewModel.deleteFromDatabase(loc)
		showPlaces()
	}
	
	override fun onMapReady(p0: GoogleMap) {
		googleMap = p0
		googleMap.setOnMapClickListener {
			try {
				val onMapClickAddress =
					viewModel.getAddressLine(LatLng(it.latitude, it.longitude), geocoder)!!
				addMarker(it, onMapClickAddress)
				searchViewClearFocus(input_search)
				viewModel.currentLoc = it
			} catch (e: Exception) {
				Log.e("exception", e.toString())
				toast("This place have no address!")
			}
		}
		
		googleMap.uiSettings.isMyLocationButtonEnabled = false
		googleMap.uiSettings.isZoomControlsEnabled = true
		
		if (viewModel.currentLoc == null) {
			val NYLoc = LatLng(40.730610, -73.935242)
			googleMap.addMarker(
				MarkerOptions().position(NYLoc).title("New York")
					.icon(
						viewModel.bitmapDescriptorFromVector(
							requireContext(),
							R.drawable.ic_baseline_flag_24
						)
					)
			)
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NYLoc, 10f))
		} else {
			googleMap.addMarker(
				MarkerOptions().position(viewModel.currentLoc!!).title("Your Location")
			)
			googleMap.animateCamera(
				CameraUpdateFactory.newLatLngZoom(
					viewModel.currentLoc!!,
					10f
				)
			)
		}
	}
	
	private fun searchLocationViewWatcher() {
		input_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(loc: String?): Boolean {
				timer?.cancel()
				if (loc != null) {
					geoLocate(loc)
				}
				return true
			}
			
			override fun onQueryTextChange(newText: String?): Boolean {
				if (timer != null) {
					timer?.cancel()
				}
				if (!newText.isNullOrEmpty()) {
					timer = Timer()
					timer?.schedule(object : TimerTask() {
						override fun run() {
							Handler(Looper.getMainLooper()).postDelayed({
								geoLocate(newText)
							}, 0)
						}
					}, 2000)
				}
				return true
			}
		})
	}
	
	private fun geoLocate(loc: String) {
		val geocoder = Geocoder(requireContext())
		val addressLatLng = viewModel.getAddressLatLng(loc, geocoder)
		val title = viewModel.getTitle(loc, geocoder)
		if (addressLatLng != null) {
			moveCamera(addressLatLng, 5f)
			addMarker(addressLatLng, title!!)
			viewModel.currentLoc = addressLatLng
		} else {
			toast("You wrote wrong location!")
		}
	}
	
	private fun getLocationPermission() {
		if (ContextCompat.checkSelfPermission(
				requireContext(),
				Manifest.permission.ACCESS_FINE_LOCATION
			)
			== PackageManager.PERMISSION_GRANTED
		) {
			Log.e(TAG, "Permission granted")
		} else {
			ActivityCompat.requestPermissions(
				requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
				PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
			)
		}
	}
	
	
	private fun getCurrentLoc() {
		if (ActivityCompat.checkSelfPermission(
				requireContext(),
				Manifest.permission.ACCESS_FINE_LOCATION
			) != PackageManager.PERMISSION_GRANTED
		) {
			ActivityCompat.requestPermissions(
				requireActivity(),
				arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
				REQUEST_CODE
			)
			googleMap.isMyLocationEnabled = true
			fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
				if (location != null) {
					val currentLocation = LatLng(location.latitude, location.longitude)
					viewModel.currentLoc = currentLocation
					
					moveCamera(currentLocation, 10f)
					addYourLocationMarker(currentLocation)
				}
			}
			return
		} else {
			googleMap.isMyLocationEnabled = true
			fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
				
				if (location != null) {
					val currentLocation = LatLng(location.latitude, location.longitude)
					viewModel.currentLoc = currentLocation
					
					moveCamera(currentLocation, 10f)
					addYourLocationMarker(currentLocation)
				}
			}
		}
	}
	
	private fun moveCamera(latLng: LatLng, zoom: Float) {
		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
		hideKeyboard()
		activity?.window?.decorView?.clearFocus()
	}
	
	private fun addMarker(currentLocation: LatLng, title: String) {
		marker?.remove()
		marker = googleMap.addMarker(
			MarkerOptions().position(currentLocation).title(title)
		)
	}
	
	private fun addYourLocationMarker(currentLocation: LatLng) {
		yourLocationMarker?.remove()
		yourLocationMarker = googleMap.addMarker(
			MarkerOptions().position(currentLocation).title("Your current location")
		)
	}
	
	private fun Fragment.hideKeyboard() {
		view?.let { activity?.hideKeyboard(it) }
	}
	
	private fun Context.hideKeyboard(view: View) {
		val inputMethodManager =
			getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
		inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
	}
	
	private fun searchViewClearFocus(searchView: SearchView) = searchView.clearFocus()
}