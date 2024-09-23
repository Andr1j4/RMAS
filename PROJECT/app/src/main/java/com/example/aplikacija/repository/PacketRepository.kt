package com.example.aplikacija.repository

import android.util.Log
import com.example.aplikacija.classes.PacketClass
import com.example.aplikacija.ds.PacketDS
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import java.io.File

class PacketRepository(private val packetDS: PacketDS) {

    suspend fun createPacket(
        DatumPostavljanja: String,
        opis: String,
        slika: File?,
        lokacija: LatLng?
    ) : Boolean {
        Log.w("TESTIRANJE", "U PACKETREP")
        return packetDS.addPacket(DatumPostavljanja, opis, slika, lokacija)
    }

    suspend fun updatePacketImage(packetId: String, imageUrl: String) {
        try {
            val packetRef = FirebaseFirestore.getInstance().collection("packets").document(packetId)

            // Update the image URL
            packetRef.update("slikaURL", imageUrl).await()
        } catch (e: Exception) {
            Log.e("Firestore", "Error updating image URL: ${e.message}")
            throw e
        }
    }

    /*
    suspend fun postPacket(packet: PacketClass, userLocation: LatLng): Boolean {
        // Check if the packet is within a 10-meter radius from the user’s location
        val distance = calculateDistance(
            userLocation.latitude,
            userLocation.longitude,
            packet.lokacija?.latitude ?: 0.0,
            packet.lokacija?.longitude ?: 0.0
        )

        if (distance <= 10) {
            packetDS.postPacket(packet) // Post the packet to the server
            return true
        }
        return false
    }

     */
    /*
    suspend fun postPacket(packet: PacketClass, userLocation: LatLng): Boolean {
        // This version directly assigns the user's location to the packet's location,
        // ensuring it is exactly at the user's location.
        val packetWithUserLocation = packet.copy(
            lokacija = userLocation // Use the exact user's location
        )

        // Post the packet to the server with the exact user's location
        return try {
            packetDS.postPacket(packetWithUserLocation)
            true
        } catch (e: Exception) {
            Log.e("PacketRepository", "Error posting packet: ${e.message}")
            false
        }
    }

     */

    suspend fun addFriend(currentUserUID: String, friendUID: String) {
        val db = FirebaseFirestore.getInstance()
        val friendsRef = db.collection("friends")

        val currentUserFriends = friendsRef.document(currentUserUID)
        val friendUserFriends = friendsRef.document(friendUID)

        // Add friend UID to the current user's friend list
        currentUserFriends.update("friendsList", FieldValue.arrayUnion(friendUID))
            .addOnSuccessListener {
                Log.d("Firestore", "Friend added successfully")
            }.addOnFailureListener { e ->
                Log.w("Firestore", "Error adding friend", e)
            }

        // Add current user UID to the friend's friend list
        friendUserFriends.update("friendsList", FieldValue.arrayUnion(currentUserUID))
            .addOnSuccessListener {
                Log.d("Firestore", "Mutual friend added successfully")
            }.addOnFailureListener { e ->
                Log.w("Firestore", "Error adding mutual friend", e)
            }
    }

    //suspend fun getFriendsPackets(userId: String): List<PacketClass>? {
        //return packetDS.getPacketsFromFriends(userId)
    //}

    suspend fun getPacketDetails1(packetId: String): PacketClass? {
        return packetDS.getPacketById(packetId)
    }

   suspend fun getPacketDetails2(packetId: String): PacketClass? {
       return try {
           val snapshot = FirebaseFirestore.getInstance()
               .collection("packets")
               .whereEqualTo("id", packetId.toInt())
               .get()
               .await()

           if (!snapshot.isEmpty) {
               val document = snapshot.documents.first()
               Log.d("Firestore", "Fetched packet data: ${document.data}")
               document.toObject(PacketClass::class.java)
           } else {
               Log.e("Firestore", "No document found for packetId: $packetId")
               null
           }
       } catch (e: Exception) {
           Log.e("Firestore", "Error fetching packet details: ${e.message}")
           null
       }
   }
suspend fun getPacketDetails(packetId: String): PacketClass? {
    return try {

        val snapshot = FirebaseFirestore.getInstance()
            .collection("packets")
            .whereEqualTo("id", packetId)
            .get()
            .await()


        if (!snapshot.isEmpty) {

            val document = snapshot.documents.first()


            val id = document.getString("id") ?: ""
            val opis = document.getString("opis") ?: ""
            val slikaURL = document.getString("slika") ?: ""
            val datumPostavljanja = document.getString("DatumPostavljanja") ?: ""
            val autor = document.getString("autor") ?: ""
            val lat = document.getDouble("lat") ?: 0.0
            val lng = document.getDouble("lng") ?: 0.0

            Log.d("Firestore", "Data for packetId $packetId: $id, $opis, $slikaURL, $lat, $lng")


            PacketClass(
                id = id,
                opis = opis,
                slikaURL = slikaURL,
                DatumPostavljanja = datumPostavljanja,
                autor = autor,
                lat = lat,
                lng = lng
            )
        } else {
            Log.e("Firestore", "No document found for packetId: $packetId")
            null
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Error fetching packet details: ${e.message}")
        null
    }
}

    suspend fun deletePacket(packetId: String): Boolean {
        return packetDS.deletePacket(packetId)
    }

    suspend fun likePacket(packetId: String) {
       packetDS.likePacket(packetId)
   }

    suspend fun updateOpis(packetId: String, newOpis: String){
        packetDS.updateOpis(packetId, newOpis)
    }

    suspend fun updateImage(packetId: String, newImageURL: File?){
        if (newImageURL != null) {
            packetDS.updateImage(packetId, newImageURL)
        }
    }



   suspend fun getAllPackets(): List<PacketClass> {
       return packetDS.getAllPackets()
   }

    suspend fun getAllPackets1(): QuerySnapshot? {
        return packetDS.getAllPackets1()
    }


   suspend fun updatePacket(packetId: String, newOpis: String, newImageFile: File?){
       return packetDS.updatePacket(packetId, newOpis, newImageFile)
   }


}
