package com.example.aplikacija.classes

import com.google.android.gms.maps.model.LatLng

data class PacketClass(
    var id: String = "",
    val opis: String = "",
    val lokacija: LatLng = LatLng(0.0,0.0),
    val slikaURL: String = "",
    val DatumPostavljanja: String = "",
    val autor: String = "",
    val lat: Double? = null,
    val lng: Double? = null
)
/*
{
    // No-argument constructor needed for Firestore deserialization
    constructor() : this(
        id = "",
        opis = "",
        lokacija = LatLng(0.0, 0.0),  // Default LatLng if none is provided
        slikaURL = "",
        DatumPostavljanja = "",
        autor = "",
        lat = null,
        lng = null,
        bodovi = 0
    )
}


suspend fun addPacket(packet: PacketClass): Boolean {
    return try {
        packetsCollection.add(packet).await()
        true
    } catch (e: Exception) {
        false
    }
}
*/