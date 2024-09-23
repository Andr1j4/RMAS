package com.example.aplikacija.ui.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
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
import java.io.FileOutputStream
import java.io.IOException

class EditPacketVM(
    private val packetRepository: PacketRepository
) : ViewModel() {

    private val _packetDetails = MutableStateFlow<PacketClass?>(null)
    val packetDetails: StateFlow<PacketClass?> = _packetDetails

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Function to upload the image to Firebase and update the packet's image URL

    fun uriToFile(uri: Uri, context: Context): File? {
        val contentResolver = context.contentResolver
        val file = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        return file
    }

    private val _imageSelected = MutableStateFlow<File?>(null)
    val imageSelected: StateFlow<File?> = _imageSelected.asStateFlow()
    fun updateImageFile(file: File?) {
        _imageSelected.value = file
        _okImage.value = true
    }

    fun uploadImageToFirebase(packetId: String, imageUri: Uri) {
        viewModelScope.launch {
            try {
                val storageRef = FirebaseStorage.getInstance().reference.child("packets/$packetId.jpg")
                storageRef.putFile(imageUri).await()  // Upload the image
                val downloadUrl = storageRef.downloadUrl.await()  // Get the download URL
                //packetRepository.updateImageURL(packetId, downloadUrl.toString())  // Update Firestore with the image URL
            } catch (e: Exception) {
                Log.e("EditPacketVM", "Error uploading image: ${e.message}")
            }
        }
    }

    fun onImagePicked(uri: Uri, packetId: String) {
        viewModelScope.launch {
            try {
                // 1. Upload the image to Firebase Storage
                val storageRef = FirebaseStorage.getInstance().reference.child("packet_images/$packetId.jpg")
                val uploadTask = storageRef.putFile(uri).await()

                // 2. Get the download URL of the uploaded image
                val downloadUrl = storageRef.downloadUrl.await()

                // 3. Update the packet in Firestore with the image URL as a string
                //packetRepository.updateImageURL(packetId, downloadUrl.toString())

                Log.d("EditPacketVM", "Image uploaded successfully, URL: $downloadUrl")
            } catch (e: Exception) {
                Log.e("EditPacketVM", "Error uploading image: ${e.message}")
            }
        }
    }

    fun loadPacketDetails(packetId: String) {
        viewModelScope.launch {
            _packetDetails.value = packetRepository.getPacketDetails(packetId)
        }
    }

    fun loadPacketDetails1(packetId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val packet = packetRepository.getPacketDetails(packetId)
                if (packet != null) {
                    _packetDetails.value = packet
                } else {
                    _errorMessage.value = "No packet found with ID: $packetId"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateOpis(packetId: String, newOpis: String) {
        viewModelScope.launch {
            try {
                packetRepository.updateOpis(packetId, newOpis)
                _errorMessage.value = "Opis updated successfully!"
                loadPacketDetails(packetId)
            } catch (e: Exception) {
                _errorMessage.value = "Error updating Opis: ${e.message}"
            }
        }
    }

    private val _cameraEvent = MutableStateFlow(false)
    val cameraEvent: StateFlow<Boolean> = _cameraEvent

    fun bitmapToFile(context: Context, bitmap: Bitmap): File? {
        val file = File(context.cacheDir, "image_${System.currentTimeMillis()}.jpg")
        return try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Log.d("bitmapToFile", "File created successfully at: ${file.absolutePath}")
            file
        } catch (e: IOException) {
            Log.e("bitmapToFile", "Error creating file: ${e.message}")
            null
        }
    }

    fun onOpenCamera() {
        _cameraEvent.value = true
    }

    private val _imageTaken = MutableStateFlow<File?>(null)
    val imageTaken: StateFlow<File?> = _imageTaken.asStateFlow()
    fun updateFile(fl: File) {
        _imageTaken.value = fl
    }

    fun updateFile1(fl: File) {
        _imageSelected.value = fl
    }
    private val _okImage = MutableStateFlow<Boolean>(false)
    val okImage: StateFlow<Boolean> = _okImage.asStateFlow()

    fun onCameraEventHandled() {
        _cameraEvent.value = false
    }

    fun updateImage(packetId: String) {
        viewModelScope.launch {
            packetRepository.updateImage(packetId, imageTaken.value)
        }
    }

    fun updateImage1(packetId: String) {
        viewModelScope.launch {
            packetRepository.updateImage(packetId, imageSelected.value)
        }
    }
}

class EditPacketVMFactory(private val packetRepository: PacketRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditPacketVM::class.java)) {
            return EditPacketVM(packetRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
