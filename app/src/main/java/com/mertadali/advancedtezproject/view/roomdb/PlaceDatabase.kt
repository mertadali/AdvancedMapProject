package com.mertadali.advancedtezproject.view.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mertadali.advancedtezproject.view.model.Place

@Database(entities = [Place::class], version = 1)
abstract class PlaceDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao


}