package com.example.aplikacija.repository

import com.example.aplikacija.ds.UserDS
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.QuerySnapshot
import java.io.File

data class UserRepository(private val userDS: UserDS){

    suspend fun CreateUser(
        email : String,
        password : String,
        ime : String,
        prezime : String,
        brtel : String,
        image : File?
    ): FirebaseUser?{
        return userDS.CreateAcc(email, password, ime, prezime, brtel, image)
    }

    suspend fun login(email: String, password: String): FirebaseUser?{
        return userDS.login(email, password)
    }

    suspend fun getUser(): QuerySnapshot? {
        return userDS.getCurrentUser()
    }

    fun logout() {
        userDS.logout()
    }

    fun getUserTF(): FirebaseUser? {
        return userDS.getUserTF()
    }

    suspend fun getAllUsers(): QuerySnapshot? {
        return userDS.getAllUsers()
    }

    suspend fun getUserByUID(uid: String): QuerySnapshot? {
        return userDS.getUserByUID(uid)
    }

    suspend fun getServiceAllowed(): Boolean? {
        return userDS.getServiceAllowed()
    }

    suspend fun changeServiceAllowed(bl: Boolean) {
        userDS.changeServiceAllowed(bl)
    }

    suspend fun addFriend(currentUserUID: String, friendUID: String) {
        userDS.addFriend(currentUserUID, friendUID)
    }

    suspend fun getFriends(currentUserUID: String): QuerySnapshot? {
        return userDS.getFriends(currentUserUID)
    }
}
