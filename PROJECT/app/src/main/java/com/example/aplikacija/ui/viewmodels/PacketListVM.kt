package com.example.aplikacija.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aplikacija.classes.PacketClass
import com.example.aplikacija.repository.PacketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PacketListVM(
    private val packetRepository: PacketRepository
) : ViewModel() {

    private val _packetList = MutableStateFlow<List<PacketClass>>(emptyList())
    val packetList: StateFlow<List<PacketClass>> = _packetList

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isAscending = MutableStateFlow(true)
    val isAscending: StateFlow<Boolean> = _isAscending

    init {
        loadPackets()
    }

    private fun loadPackets() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val packets = packetRepository.getAllPackets()
                _packetList.value = packets
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "NE UZIMA PAKETE: ${e.message}"
            }
        }
    }

    fun filterPacketsByDate(ascending: Boolean) {
        viewModelScope.launch {
            val sortedPackets = if (ascending) {
                _packetList.value.sortedBy { packet ->
                    SimpleDateFormat("dd. MMMM yyyy.", Locale("bs", "BS")).parse(packet.DatumPostavljanja)
                }
            } else {
                _packetList.value.sortedByDescending { packet ->
                    SimpleDateFormat("dd. MMMM yyyy.", Locale("bs", "BS")).parse(packet.DatumPostavljanja)
                }
            }
            _packetList.value = sortedPackets
        }
    }

    fun toggleSortOrder() {
        val currentOrder = _isAscending.value
        _isAscending.value = !currentOrder
        filterPacketsByDate(_isAscending.value)
    }
}

class PacketListVMFactory(
    private val packetRepository: PacketRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PacketListVM::class.java)) {
            return PacketListVM(packetRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
