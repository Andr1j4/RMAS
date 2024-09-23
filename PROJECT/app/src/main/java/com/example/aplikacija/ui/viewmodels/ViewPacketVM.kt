package com.example.aplikacija.ui.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aplikacija.classes.PacketClass
import com.example.aplikacija.repository.PacketRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

class ViewPacketVM(
    private val packetRepository: PacketRepository
) : ViewModel() {

    private val _packetDetails = MutableStateFlow<PacketClass?>(null)
    val packetDetails: StateFlow<PacketClass?> = _packetDetails

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _opisPacket = MutableLiveData<String>()
    val opisPacket: LiveData<String> = _opisPacket

    private val _autor = MutableLiveData<String>()
    val autor: LiveData<String> = _autor

    private val _slikaURL = MutableLiveData<String>()
    val slikaURL: LiveData<String> = _slikaURL

    private val _datumPostavljanja = MutableLiveData<String>()
    val datumPostavljanja: LiveData<String> = _datumPostavljanja

    private val _bodovi = MutableLiveData<Int>()
    val bodovi: LiveData<Int> = _bodovi

    private val _id = MutableStateFlow("")
    val id: StateFlow<String> = _id.asStateFlow()
    fun updateID(id: String) {
        _id.value = id
    }

    fun updateOpisPacket(opis: String) {
        _opisPacket.value = opis
    }

    fun updateAutor(autor: String) {
        _autor.value = autor
    }

    fun updateSlikaURL(slikaURL: String) {
        _slikaURL.value = slikaURL
    }

    fun updateDatumPostavljanja(datum: String) {
        _datumPostavljanja.value = datum
    }

    fun updateBodovi(bodovi: Int) {
        _bodovi.value = bodovi
    }

    fun loadPacketDetails(packetId: String) {
        viewModelScope.launch {
            Log.d("ViewPacketVM", "Fetching details for packetId: $packetId")
            _isLoading.value = true
            try {
                val packet = packetRepository.getPacketDetails(packetId)
                if (packet != null) {
                    Log.d("ViewPacketVM", "Successfully fetched packet: ${packet.id}")
                    _packetDetails.value = packet
                } else {
                    _errorMessage.value = "No packet found with ID: $packetId"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error fetching packet details: ${e.message}"
                Log.e("Firestore", "Error fetching packet details: ${e.message}")
            }
        }
    }
    fun loadPacketDetails3(packetId: String) {
        viewModelScope.launch {
            try {
                val packetDetails = packetRepository.getPacketDetails(packetId) // Adjust this to your actual method
                _packetDetails.value = packetDetails
            } catch (e: Exception) {
                Log.e("ViewPacketVM", "Error loading packet details: ${e.message}")
            }
        }
    }

    fun loadPacketDetails1(packetId: String) {
        viewModelScope.launch {
            Log.d("ViewPacketVM", "Fetching details for packetId: $packetId")
            _isLoading.value = true
            try {
                val packet = packetRepository.getPacketDetails(packetId)
                if (packet != null) {
                    _packetDetails.value = packet
                } else {
                    _errorMessage.value = "No packet found with ID: $packetId"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Ovde pravi problem: ${e.message}"
                Log.e("Firestore", "Error fetching packet details: ${e.message}")
            }
        }
    }

    fun loadPacketDetails2(packetId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val packet = packetRepository.getPacketDetails(packetId)
                _packetDetails.value = packet
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error fetching packet details: ${e.message}"
                Log.e("Firestore", "Error fetching packet details: ${e.message}")
            }
        }
    }

    fun deletePacket(packetId: String) {
        viewModelScope.launch {
            try {
                packetRepository.deletePacket(packetId)
                Log.d("Firestore", "Packet deleted with ID: $packetId")
            } catch (e: Exception) {
                Log.e("Firestore", "Failed to delete packet: ${e.message}")
            }
        }
    }

    fun likePacket(packetId: String) {
        viewModelScope.launch {
            try {
                packetRepository.likePacket(packetId)
                _errorMessage.value = "Packet liked successfully!"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to like packet: ${e.message}"
            }
        }
    }

    fun updatePacket(packetId: String, newOpis: String, newImageFile: File?) {
        viewModelScope.launch {
            try {
                packetRepository.updatePacket(packetId, newOpis, newImageFile)
                _errorMessage.value = "Packet updated successfully!"
                loadPacketDetails(packetId)  // Refresh packet details after update
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update packet: ${e.message}"
            }
        }
    }

    fun updateImageUri(imageUri: Uri?, packetId: String) {
        viewModelScope.launch {
            if (imageUri != null) {
                try {
                    // Upload the image to Firebase Storage
                    val imageUrl = uploadImageToFirebase(imageUri)

                    // Once uploaded, update the Firestore document with the new image URL
                    if (imageUrl != null) {
                        packetRepository.updatePacketImage(packetId, imageUrl)
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to upload image: ${e.message}"
                }
            } else {
                _errorMessage.value = "No image selected."
            }
        }
    }

    private suspend fun uploadImageToFirebase(imageUri: Uri): String? {
        return try {
            // Create a reference to Firebase Storage
            val storageReference = FirebaseStorage.getInstance().reference.child("packet_images/${imageUri.lastPathSegment}")

            // Upload the image
            storageReference.putFile(imageUri).await()

            // Get the download URL
            storageReference.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("Firebase", "Error uploading image: ${e.message}")
            null
        }
    }

}

class ViewPacketVMFactory(
    private val packetRepository: PacketRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewPacketVM::class.java)) {
            return ViewPacketVM(packetRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

