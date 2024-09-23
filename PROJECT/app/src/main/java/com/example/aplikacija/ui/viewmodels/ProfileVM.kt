package com.example.aplikacija.ui.viewmodels

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aplikacija.classes.PacketClass
import com.example.aplikacija.repository.UserRepository
import com.example.aplikacija.services.NearbyService
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.getField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileVM(
    private val userRep: UserRepository?) : ViewModel() {


    private val _imeProf = MutableStateFlow("Ime")
    val imeProf: StateFlow<String> = _imeProf.asStateFlow()
    fun updateImeProf(nT: String) {
        _imeProf.value = nT
    }

    private val _prezimeProf = MutableStateFlow("Prezime")
    val prezimeProf: StateFlow<String> = _prezimeProf.asStateFlow()
    fun updatePrezmeProf(nT: String) {
        _prezimeProf.value = nT
    }

    private val _emailProf = MutableStateFlow("email")
    val emailProf: StateFlow<String> = _emailProf.asStateFlow()
    fun updateEmailProf(nT: String) {
        _emailProf.value = nT
    }

    private val _brojTelefonaProf = MutableStateFlow("brojTelefona")
    val brojTelefonaProf: StateFlow<String> = _brojTelefonaProf.asStateFlow()
    fun updateBrojTelefonaPRof(nT: String) {
        _brojTelefonaProf.value = nT
    }

    private val _brojBodovaProf = MutableStateFlow(150)
    val brojBodovaProf: StateFlow<Int> = _brojBodovaProf.asStateFlow()
    fun updateBrojBodovaProf(nb: Int) {
        _brojBodovaProf.value = nb
    }


    private val _serviceCheckedProf = MutableStateFlow(false)
    val serviceCheckedProf: StateFlow<Boolean> = _serviceCheckedProf.asStateFlow()
    fun updateServiceCheckedProf(bl: Boolean) {
        _serviceCheckedProf.value = bl
    }

    private val _imageURL = MutableStateFlow("///")
    val imageURL: StateFlow<String> = _imageURL.asStateFlow()
    fun updateImgURL(nT: String) {
        _imageURL.value = nT
    }

    private val sharedPreferencesKey = "ServiceStatus"

    fun saveServiceStatus(context: Context, isRunning: Boolean) {
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isServiceRunning", isRunning)
        editor.apply()
    }

    fun getSavedServiceStatus(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isServiceRunning", false)
    }

    suspend fun getUserInfo() {
        viewModelScope.launch {
            val userSnap: QuerySnapshot? = userRep?.getUser()
            val user = userSnap?.documents?.firstOrNull()
            if (user != null) {
                _emailProf.value = user.getString("email").toString()
                _imeProf.value = user.getString("ime").toString()
                _prezimeProf.value = user.getString("prezime").toString()
                _brojTelefonaProf.value = user.getString("brTel").toString()
                _imageURL.value = user.get("slika").toString()
                //_serviceCheckedProf.value = user.getBoolean("paket")!!
                _brojBodovaProf.value = user.getField<Int>("bod") ?: 0
            }
        }

    }

    fun getUserTF(): FirebaseUser? {
        return userRep?.getUserTF()
    }

    suspend fun getServiceAllowed(): Boolean? {
        return userRep?.getServiceAllowed()
    }

    fun serviceFunction(context: Context) {
        viewModelScope.launch {
            userRep?.changeServiceAllowed(_serviceCheckedProf.value)
        }
        val intent = Intent(context, NearbyService::class.java)
        if (_serviceCheckedProf.value) {
            //poslati false u FIRESTORE
            //ne stopira
            context.stopService(intent)
        } else {
            //poslati true u FIRESTORE
            ContextCompat.startForegroundService(context, intent)
        }
    }


    fun startNearbyService(context: Context) {
        val intent = Intent(context, NearbyService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopNearbyService(context: Context) {
        val serviceIntent = Intent(context, NearbyService::class.java)
        context.stopService(serviceIntent)
    }

    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private val _postedPackets = MutableStateFlow<List<PacketClass>>(emptyList())
    val postedPackets: StateFlow<List<PacketClass>> = _postedPackets.asStateFlow()

    private val _openedPackets = MutableStateFlow<List<PacketClass>>(emptyList())
    val openedPackets: StateFlow<List<PacketClass>> = _openedPackets.asStateFlow()


}

class ProfileVMFactory(private val userRep: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileVM::class.java)) {
            return ProfileVM(userRep) as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}