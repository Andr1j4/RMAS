package com.example.aplikacija.ds

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.File

data class UserDS(
    private var auth: FirebaseAuth = Firebase.auth,
    private var db: FirebaseFirestore = Firebase.firestore
)
{

    suspend fun CreateAcc(
        email: String,
        password: String,
        ime: String,
        prezime: String,
        brTel: String,
        image: File?
    ) : FirebaseUser? {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val imageUrl = uploadImageToFirebase(image)
            val data = hashMapOf(
                "uid" to auth.currentUser?.uid.toString(),
                "email" to email,
                "password" to password,
                "ime" to ime,
                "prezime" to prezime,
                "brTel" to brTel,
                "slika" to imageUrl,
                "brkutija" to 0,
                "bod" to 0
            )
            db.collection("users").document().set(data)

            return authResult.user
        }
        catch (e: Exception){
            Log.w("Firestore", "Error creating account or uploading data!")
        }
        return null
    }

    private val storageReference: StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }

    suspend fun uploadImageToFirebase(file: File?): String? {
        return try {
            file?.let {
                val fileUri = Uri.fromFile(it)
                val imageRef = storageReference.child("images/${it.name}")

                // Upload the file to Firebase
                val uploadtask = imageRef.putFile(fileUri).await()

                // Get the download URL
                if (uploadtask.task.isSuccessful) {
                    val downloadUrl = imageRef.downloadUrl.await().toString()
                    Log.d("UploadImage", "Upload successful. Download URL: $downloadUrl")
                    downloadUrl
                }
                else{
                    Log.w("UploadImage", "Upload failed.")
                    null
                }
            }
        } catch (e: Exception) {
            Log.w("UploadImage", "Upload encountered an error: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun login(email: String, password: String): FirebaseUser? {
        try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            return authResult.user
        } catch (e: Exception) {
            Log.w("ERROR", e)
        }
        return null
    }

    suspend fun getCurrentUser(): QuerySnapshot? {
        val usersRef = db.collection("users")
        val query = usersRef.whereEqualTo("uid", auth.currentUser?.uid.toString())

        return try {
            query.get().await()
        }
        catch (e: Exception){
            Log.w("Firestore", "Error getting documents: ", e)
            null
        }
    }

    suspend fun getUserByUID(uid: String): QuerySnapshot? {
        val usersRef = db.collection("users")
        val query = usersRef.whereEqualTo("uid", uid)

        return try {
            query.get().await()
        } catch (e: Exception) {
            Log.w("Firestore", "Error getting documents: ", e)
            null
        }

    }

    fun logout() {
        auth.signOut()
    }

    fun getUserTF(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun getAllUsers(): QuerySnapshot? {
        val usersRef = db.collection("users")

        return try {
            usersRef.get().await()
        } catch (e: Exception) {
            Log.w("Firestore", "Error getting users from db: ", e)
            null
        }
    }

    suspend fun getServiceAllowed(): Boolean? {
        val usersRef = db.collection("users")
        val query = usersRef.whereEqualTo("uid", auth.currentUser?.uid.toString())

        return try {
            val snap = query.get().await()
            val user = snap.documents.firstOrNull()
            if (user != null) {
                return user.getBoolean("packets")
            } else {
                return false
            }
        } catch (e: Exception) {
            Log.w("Firestore", "Error getting documents: ", e)
            false
        }
    }

    suspend fun changeServiceAllowed(bl: Boolean) {
        val usrsRef = db.collection("users")
        val query = usrsRef.whereEqualTo("uid", auth.currentUser!!.uid)
        val querySnap = query.get().await()
        if (!querySnap.isEmpty) {
            val doc = querySnap.documents[0]
            doc.reference.update("packets", !bl)
        }

    }

    suspend fun addFriend(currentUserUID: String, friendUID: String) {
        try {
            val friendsCollection = db.collection("friends")

            // Create a map to store in Firestore for the current user adding the friend
            val friendData = hashMapOf(
                "UserUID" to currentUserUID,
                "FriendUID" to friendUID
            )

            // Add currentUserUID and friendUID to Firestore
            friendsCollection.add(friendData).await()

            // Now add the reverse (friend adding current user)
            val reverseFriendData = hashMapOf(
                "UserUID" to friendUID,
                "FriendUID" to currentUserUID
            )

            friendsCollection.add(reverseFriendData).await()

            Log.d("AddFriend", "Friendship added successfully between $currentUserUID and $friendUID")
        } catch (e: Exception) {
            Log.e("AddFriend", "Error adding friend: ${e.message}")
        }
    }

    suspend fun getFriends(currentUserUID: String): QuerySnapshot? {
        return try {
            val friendsCollection = db.collection("friends")

            // Query to get documents where UserUID matches the current user
            friendsCollection.whereEqualTo("UserUID", currentUserUID).get().await()
        } catch (e: Exception) {
            Log.e("getFriends", "Error fetching friends: ${e.message}")
            null
        }
    }

    //Ovde treba da dodam za pakete
}
