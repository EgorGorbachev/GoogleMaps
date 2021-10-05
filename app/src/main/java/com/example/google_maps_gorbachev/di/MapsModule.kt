package com.example.google_maps_gorbachev.di

import android.content.Context
import androidx.room.Room
import com.example.google_maps_gorbachev.repository.database.MapsDao
import com.example.google_maps_gorbachev.repository.database.MapsDatabase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapsModule {
	
	@Singleton
	@Provides
	fun provideSearchDatabase(@ApplicationContext context: Context) =
		Room.databaseBuilder(
			context.applicationContext,
			MapsDatabase::class.java,
			"loc_database"
		).build()
	
	@Provides
	fun providesDao(appDatabase: MapsDatabase): MapsDao {
		return appDatabase.MapsDao()
	}
	
//	@Singleton
//	@Provides
//	fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient):Retrofit =
//		Retrofit.Builder()
//			.baseUrl("https://maps.googleapis.com/maps/api/geocode/json")
//			.client(okHttpClient)
//			.addConverterFactory(GsonConverterFactory.create(gson))
//			.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//			.build()
//
//	@Singleton
//	@Provides
//	fun provideOkHttp():OkHttpClient {
//		val httpLoggingInterceptor = HttpLoggingInterceptor()
//		httpLoggingInterceptor.level =HttpLoggingInterceptor.Level.BODY
//		return OkHttpClient.Builder()
//			.addInterceptor(httpLoggingInterceptor)
//			.build()
//	}
//
//	@Singleton
//	@Provides
//	fun gson():Gson = GsonBuilder().setLenient().create()
}