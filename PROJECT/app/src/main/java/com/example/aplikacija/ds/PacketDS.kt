package com.example.aplikacija.ds

import android.net.Uri
import android.util.Log
import com.example.aplikacija.classes.PacketClass
import com.example.aplikacija.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.File

class PacketDS(
    private var auth: FirebaseAuth = Firebase.auth,
    private var db: FirebaseFirestore = Firebase.firestore
) {

    private val firestore = FirebaseFirestore.getInstance()
    private val packetsCollection = firestore.collection("packets")
    private val user = UserRepository(userDS = UserDS())

    suspend fun getAllPackets(): List<PacketClass> {
        return try {
            val snapshot = packetsCollection.get().await()
            Log.d("Firestore", "Documents retrieved: ${snapshot.size()}")
            snapshot.documents.mapNotNull { doc ->
                Log.d("Firestore", "Document: ${doc.data}") // Add this log
                val packet = doc.toObject(PacketClass::class.java)
                packet?.apply {
                    id = doc.getString("id") ?: doc.id  // Ensure the id field is set from the document
                    Log.d("Firestore", "Packet ID: $id, Data: ${doc.data}")
                }
                packet
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching packets: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAllPackets1(): QuerySnapshot? {
        val salseRef = db.collection("sales")

        return try {
            salseRef.get().await()
        } catch (e: Exception) {
            Log.w("Firestore", "Error getting sales from db: ", e)
            null
        }
    }

    suspend fun addPacket(
        DatumPostavljanja: String,
        opis: String,
        slika: File?,
        lokacija: com.google.android.gms.maps.model.LatLng?
    ): Boolean {
        try {
            var lokLat: Double = 0.0
            var lokLng: Double = 0.0
            if (lokacija != null) {
                lokLat = lokacija.latitude
                lokLng = lokacija.longitude
            }

            val imgUrl = uploadImageToFirebase(slika)
            Log.w("SLIKA", imgUrl.toString())

            val autorUID = auth.currentUser!!.uid.toString()

            val lastPacketId = getNextPacketId()

            //var newId = lastPacketId

            val data = hashMapOf(
                "id" to lastPacketId,
                "DatumPostavljanja" to DatumPostavljanja,
                "slika" to imgUrl,
                "autor" to autorUID,
                "lat" to lokLat,
                "lng" to lokLng,
                "opis" to opis
            )

            db.collection("packets").add(data).await()

            incrementUserPoints(autorUID)

            return true
        } catch (e: Exception) {
            Log.w("Firestore", "Error creating packet or uploading data", e)
            return false
        }
    }

    fun updateOpis(packetId: String, newOpis: String) {
        // Find the document with the matching 'id' field
        db.collection("packets")
            .whereEqualTo("id", packetId)  // Assuming 'id' is the field name for packetId
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Loop through the results (though there should be only one document)
                for (document in querySnapshot) {
                    // Update the 'opis' field in the found document
                    db.collection("packets").document(document.id)
                        .update("opis", newOpis)  // Update only the 'opis' field
                        .addOnSuccessListener {
                            Log.w("UPDATE", "Opis successfully updated")
                        }
                        .addOnFailureListener { e ->
                            Log.w("UPDATE", "Failed to update opis: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("UPDATE", "Failed to find packet with id $packetId: ${e.message}")
            }
    }

    private suspend fun getNextPacketId(): String {
        return try {
            // Fetch all packets
            val querySnapshot = db.collection("packets")
                .get()
                .await()

            // Extract and parse the 'id' values, converting them to integers
            val ids = querySnapshot.documents.mapNotNull { doc ->
                doc.getString("id")?.toIntOrNull()  // Convert 'id' to integer
            }

            // Find the maximum id, then add 1 for the next packet's id
            val nextId = if (ids.isNotEmpty()) {
                ids.maxOrNull()?.plus(1) ?: 1 // Increment the maximum 'id'
            } else {
                1 // If no packets exist, start with '1'
            }

            nextId.toString() // Convert the next id back to string
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching last packet ID: ${e.message}")
            "1" // Fallback to '1' in case of an error
        }
    }

    suspend fun deletePacket(packetId: String): Boolean {
        return try {
            val packetsCollection = db.collection("packets")
            val querySnapshot = packetsCollection.whereEqualTo("id", packetId).get().await()

            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0]
                document.reference.delete().await()
                true
            } else {
                Log.w("Firestore", "No packet found with ID: $packetId")
                false
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error deleting packet: ${e.message}")
            false
        }
    }

    private suspend fun incrementUserPoints(autorUID: String) {
        try {
            val usersRef = db.collection("users")
            val query = usersRef.whereEqualTo("uid", autorUID).get().await()

            if (!query.isEmpty) {
                val document = query.documents[0]
                val documentRef = document.reference

                // Increment "bodovi" by 5
                documentRef.update("bod", FieldValue.increment(3)).await()
                Log.d("Firestore", "Points successfully updated")
            } else {
                Log.w("Firestore", "No user found with UID: $autorUID")
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error updating user points: ${e.message}")
        }
    }

    private val storageReference: StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }

    suspend fun updateImage(packetId: String, newImage: File?) {

        if (newImage == null) {
            Log.e("UPDATE", "New image file is null")
            return
        }

        try {
            val newImageUrl = uploadImageToFirebase1(newImage)
            Log.w("SLIKA UPDATE", "VRACA URL SLIKE KAO ${newImageUrl}")

            if (newImageUrl != null) {

                val querySnapshot = db.collection("packets")
                    .whereEqualTo("id", packetId)
                    .get()
                    .await()

                Log.w("UPDATE SLIKA", "U IF NAREDBI")


                for (document in querySnapshot) {

                    db.collection("packets").document(document.id)
                        .update("slika", newImageUrl)
                        .addOnSuccessListener {
                            Log.w("UPDATE", "Image successfully updated")
                        }
                        .addOnFailureListener { e ->
                            Log.w("UPDATE", "Failed to update image: ${e.message}")
                        }
                }
            } else {
                Log.e("UPDATE", "Failed to upload image: URL is null")
            }
        } catch (e: Exception) {
            Log.e("UPDATE", "Error updating image: ${e.message}")
        }
    }
    suspend fun uploadImageToFirebase1(imageFile: File?): String? {
        if (imageFile == null || !imageFile.exists()) {
            Log.e("Firebase", "Image file is null or does not exist")
            return null
        }

        return try {
            val fileUri = Uri.fromFile(imageFile)
            val storageReference = FirebaseStorage.getInstance().reference.child("packet_images/${imageFile.name}")

            val uploadTask = storageReference.putFile(fileUri).await()

            Log.w("UPLOADIND", "Upload Task: ${uploadTask.task}, Is Successful: ${uploadTask.task.isSuccessful}")

            if (uploadTask.task.isSuccessful) {
                val downloadUrl = storageReference.downloadUrl.await()
                Log.d("Firebase", "Image uploaded successfully, Download URL: ${downloadUrl?.toString()}")
                downloadUrl.toString()
            } else {
                Log.e("Firebase", "Upload failed with error: ${uploadTask.task.exception?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error uploading image: ${e.message}")
            null
        }
    }

    suspend fun uploadImageToFirebase(file: File?): String? {
        return try {
            val fileUri = Uri.fromFile(file)
            val imageRef = storageReference.child("places/${file?.name}")

            // Log to verify paths
            Log.d("UploadImage", "File URI: $fileUri")
            Log.d("UploadImage", "Storage Path: ${imageRef.path}")

            // Upload the file
            val uploadTask = imageRef.putFile(fileUri).await()

            // Check if upload was successful
            if (uploadTask.task.isSuccessful) {
                val dnwldURL = imageRef.downloadUrl.await().toString()
                Log.d("UploadImage", "Upload successful. Download URL: $dnwldURL")
                return dnwldURL
            } else {
                Log.w("UploadImage", "Upload failed.")
                null
            }
        } catch (e: Exception) {
            Log.w("UploadImage", "Upload encountered an error: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun likePacket(packetId: String): Boolean {
        return try {
            val packetRef = packetsCollection.document(packetId)
            packetRef.update("likes", FieldValue.increment(1)).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getPacketById(packetId: String): PacketClass? {
        return try {
            val snapshot = packetsCollection.document(packetId).get().await()
            snapshot.toObject(PacketClass::class.java)
        } catch (e: Exception) {
            Log.e("PacketRepository", "Error fetching packet details: ${e.message}")
            null
        }
    }

    suspend fun updatePacket(packetId: String, newOpis: String, newImageFile: File?) {
        try {
            val packetRef = db.collection("packets").document(packetId)
            val updates = hashMapOf<String, Any>("opis" to newOpis)

            // If a new image is provided, upload it and update the image URL
            if (newImageFile != null) {
                val newImageUrl = uploadImageToFirebase(newImageFile)

                // Only add the image URL if the upload was successful (newImageUrl is not null)
                newImageUrl?.let {
                    updates["slika"] = it  // Now it's safe to add
                }
            }

            // Update the packet in Firestore
            packetRef.update(updates).await()
        } catch (e: Exception) {
            Log.e("PacketRepository", "Failed to update packet: ${e.message}")
            throw e
        }
    }
}


