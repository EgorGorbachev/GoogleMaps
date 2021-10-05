package com.example.google_maps_gorbachev.Models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "loc_table")
data class Loc(
	@PrimaryKey(autoGenerate = true)
	val id:Int,
	val locName: String,
	val lat: Double,
	val lng: Double
):Parcelable