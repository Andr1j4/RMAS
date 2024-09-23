package com.example.aplikacija.ui.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aplikacija.classes.PacketClass
import com.example.aplikacija.repository.PacketRepository
import com.example.aplikacija.repository.UserRepository
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.getField
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@SuppressLint("RestrictedApi")
class MainVM(
    private val packetRepository: PacketRepository?,
    private val userRepository: UserRepository?
) : ViewModel() {

    private val _packets = MutableStateFlow<List<PacketClass>?>(emptyList())
    val packets: StateFlow<List<PacketClass>?> = _packets

    private val _friends = MutableStateFlow<List<User>>(emptyList())

    val friends: StateFlow<List<User>> = _friends

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    private val _searchRadius = MutableStateFlow(100)
    val searchRadius: StateFlow<Int> = _searchRadius

    init {
        // Load initial data
        loadPackets()
        //loadFriends()
    }

    private fun loadPackets() {
        viewModelScope.launch {
            val allPackets = packetRepository?.getAllPackets()
            _packets.value = allPackets
        }
    }

    /*
    private fun loadFriends() {
        viewModelScope.launch {
            val friendsList = userRepository.getFriends()
            _friends.value = friendsList
        }
    }

     */

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context) {

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000
        ).build()

        if (locationCallback == null) {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location: Location? = locationResult.lastLocation
                    location?.let {
                        _userLocation.value = LatLng(it.latitude, it.longitude)
                        filterPackets()
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, null)
    }

    fun onSearchTextChanged(newText: String) {
        _searchText.value = newText
        searchPacketsOrFriends(newText)
    }

    private fun searchPacketsOrFriends(query: String) {
        viewModelScope.launch {
            //val packets = packetRepository.searchPackets(query)
            //val friends = userRepository.searchUsers(query)
            //_packets.value = packets
            //_friends.value = friends
        }
    }

    fun onRadiusChanged(newRadius: Int) {
        _searchRadius.value = newRadius
        loadPackets()
    }

    fun onPacketClick(packet: PacketClass) {
        // Handle the logic when the packet is clicked (e.g., open PacketScreen)
    }

    private val _sliderPosMain = MutableStateFlow(0f)
    val slidePosMain: StateFlow<Float> = _sliderPosMain.asStateFlow()
    fun updateSlidePos(pos: Float) {
        _sliderPosMain.value = pos
        Log.w("SLIDER", _sliderPosMain.value.toString())
    }

    private val _markerState = MutableStateFlow(MarkerState(position = LatLng(0.0, 0.0)))
    val markerState: StateFlow<MarkerState> = _markerState.asStateFlow()
    fun onUpdatePosition(position: LatLng) {
        _markerState.value.position = position
    }

    private val _openDialogMain = MutableStateFlow(false)
    val openDialogMain: StateFlow<Boolean> = _openDialogMain.asStateFlow()
    fun updateOpenDialog(bl: Boolean) {
        _openDialogMain.value = bl
    }

    private val _isExpandedMain = MutableStateFlow(false)
    val isExpandedMain: StateFlow<Boolean> = _isExpandedMain.asStateFlow()
    fun updateIsExpanded(bl: Boolean) {
        _isExpandedMain.value = bl
    }

    private val _selTextMain = MutableStateFlow("Izaberite Lokaciju")
    val selTextMain: StateFlow<String> = _selTextMain.asStateFlow()
    fun updateSelTextMain(nt: String) {
        _selTextMain.value = nt
    }

    private val _isExpandedProdMain = MutableStateFlow(false)
    val isExpandedProdMain: StateFlow<Boolean> = _isExpandedProdMain.asStateFlow()
    fun updateIsExpandedProd(bl: Boolean) {
        _isExpandedProdMain.value = bl
    }

    private val _selTextProdMain = MutableStateFlow("")
    val selTextProdMain: StateFlow<String> = _selTextProdMain.asStateFlow()
    fun updateSelTextProdMain(nt: String) {
        _selTextProdMain.value = nt
    }

    private val _showOnlyFriends = MutableStateFlow(false)
    val showOnlyFriends: StateFlow<Boolean> = _showOnlyFriends

    fun updateShowOnlyFriends(showFriends: Boolean) {
        _showOnlyFriends.value = showFriends
    }

    //Znaci ikonice da budu semi transperent kada se klikne na njih da ne budu transperent!!!

    private val _showDate = MutableStateFlow(false)
    val showDate: StateFlow<Boolean> = _showDate

    private val _selectedDate = MutableStateFlow<Long?>(null)
    val selectedDate: StateFlow<Long?> = _selectedDate

    private val _filteredPackets = MutableStateFlow<List<PacketClass>>(emptyList())
    val filteredPackets: StateFlow<List<PacketClass>> = _filteredPackets

    fun updateShowDate(show: Boolean) {
        _showDate.value = show
    }

    fun updateSelectedDate(date: Long) {
        _selectedDate.value = date
    }

    fun getFormattedDate1(): String {
        val formatter = SimpleDateFormat("dd. MMMM yyyy.", Locale("bs", "BS"))
        return _selectedDate.value?.let { formatter.format(Date(it)) } ?: "Izaberite datum"
    }

    fun filterPacketsByDate() {
        viewModelScope.launch {
            // Fetch packets from repository
            val packets = packetRepository?.getAllPackets()

            // Format the selected date
            val selectedDateFormatted = getFormattedDate()

            // Filter the packets by date
            val filteredPacketsList = packets?.filter { packet ->
                packet.DatumPostavljanja == selectedDateFormatted
            }

            // Update the state with the filtered packets
            if (filteredPacketsList != null) {
                _filteredPackets.value = filteredPacketsList
            }
        }
    }

    // Updated getAllPackets function
    fun getAllPackets() {
        viewModelScope.launch {
            val packets = packetRepository?.getAllPackets() ?: emptyList()

            val packetList = packets.map { packet ->
                val id = packet.id
                val lat = packet.lat ?: 0.0
                val lng = packet.lng ?: 0.0
                val lokConv = LatLng(lat, lng)

                PacketClass(
                    id = id,
                    opis = packet.opis,
                    lokacija = lokConv,  // Set the LatLng for the packet location
                    slikaURL = packet.slikaURL,
                    DatumPostavljanja = packet.DatumPostavljanja,
                    autor = packet.autor,
                    lat = lat,  // Include lat and lng for reference
                    lng = lng
                )
            }

            // Update state
            _packets.value = packetList
            _tempPackets.value = packetList

            filterPackets()  // If you have additional filtering logic
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun getAllPackets1() {
        viewModelScope.launch {
            val packets = packetRepository?.getAllPackets1()

            val packetList = packets?.documents?.map { packet ->
                // Assuming packet is already a PacketClass object
                val lat = packet.getField<Double>("lat") ?: 0.0
                val lng = packet.getField<Double>("lng") ?: 0.0
                val datum = packet.getString("DatumPostavljanja") ?: ""
                val opis = packet.getString("opis") ?: ""
                val autor = packet.getString("autor") ?: ""
                //val bodovi = packet.bodovi
                val slikaUrl = packet.getString("slika") ?: ""
                val id = packet.getString("id") ?: ""

                // Fetch author information from the userRepository
                val autorInfo = userRepository?.getUserByUID(autor)
                val user = autorInfo?.documents?.firstOrNull()
                var autorIme = ""
                var autorPrezime = ""
                if (user != null) {
                    autorIme = user.getString("ime").toString()
                    autorPrezime = user.getString("prezime").toString() ?: ""
                }

                val lokConv = LatLng(lat, lng)
                //Log.w("BODOVI", bodovi.toString())

                // Create and return the PacketClass
                PacketClass(
                    id = id,
                    opis = opis,
                    lokacija = lokConv,
                    slikaURL = slikaUrl,
                    DatumPostavljanja = datum,
                    autor = autorIme
                )
            } ?: emptyList()

            val todaysDateUNF = System.currentTimeMillis()
            val todaysDate = java.util.Date(todaysDateUNF)
            val dateFormat = SimpleDateFormat("dd. MMMM yyyy.", Locale("bs", "BS", "LAT"))

            val filteredPackets = packetList.filter { packet ->
                val packetDate = dateFormat.parse(packet.DatumPostavljanja)
                packetDate?.after(todaysDate) ?: false
            }

            // Update the state with the filtered packets
            _packets.value = filteredPackets
            _tempPackets.value = filteredPackets

            filterPackets()
        }
    }

    private val _cameraPositionState =
        MutableStateFlow<CameraPositionState>(
            CameraPositionState(CameraPosition(LatLng(43.321445, 21.896104), 18f, 0f, 0f))
        )
    val cameraPositionState: StateFlow<CameraPositionState> = _cameraPositionState.asStateFlow()

    fun updateCameraPosition(cps: CameraPositionState) {
        _cameraPositionState.value = cps
    }

    fun stopLocationUpdates(context: Context) {
        locationCallback?.let {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private val _dateState = mutableStateOf(
        DatePickerState(
            Locale("bs", "BA", "LAT"),
            initialSelectedDateMillis = System.currentTimeMillis()
        )
    )

    @OptIn(ExperimentalMaterial3Api::class)
    val dateState: MutableState<DatePickerState> = _dateState


    @OptIn(ExperimentalMaterial3Api::class)
    fun getFormattedDate(): String {
        val selectedDateMillis = dateState.value.selectedDateMillis
        return if (selectedDateMillis != null) {
            val sdf = SimpleDateFormat("dd. MMMM yyyy.", Locale("bs", "BS", "LAT"))
            sdf.format(selectedDateMillis)
        } else {
            val sdf = SimpleDateFormat("dd. MMMM yyyy.", Locale("bs", "BS", "LAT"))
            sdf.format(System.currentTimeMillis())
        }
    }

    private val _tempPackets = MutableStateFlow<List<PacketClass>?>(null)
    val tempPackets: StateFlow<List<PacketClass>?> = _tempPackets.asStateFlow()

    @OptIn(ExperimentalMaterial3Api::class)
    fun resetFilters() {
        viewModelScope.launch {
            _selTextProdMain.value = ""
            _dateState.value.selectedDateMillis = System.currentTimeMillis()
            _sliderPosMain.value = 0f
            getAllPackets()
            Log.w("RADI", "PROVERA")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun filterPackets() {
        viewModelScope.launch {
            _packets.value = _tempPackets.value
            val allPackets = _packets.value ?: return@launch

            val selectedDateMillis = dateState.value.selectedDateMillis
            val selectedDate = Date(selectedDateMillis!!)
            val dateFormat = SimpleDateFormat("dd. MMMM yyyy.", Locale("bs", "BS", "LAT"))

            val userLocation = _userLocation.value ?: return@launch
            val userLat = userLocation.latitude
            val userLng = userLocation.longitude

            val maxDistance = 0.05 * _sliderPosMain.value

            val filteredPackets = allPackets.filter { packet ->
                val packetDate = dateFormat.parse(packet.DatumPostavljanja)


                val packetLat = packet.lokacija?.latitude ?: return@filter false
                val packetLng = packet.lokacija?.longitude ?: return@filter false
                val distance = calculateDistance(userLat, userLng, packetLat, packetLng)
                val daljinaQuery = if (_sliderPosMain.value == 0f) {
                    true
                } else {
                    distance <= maxDistance
                }

                daljinaQuery
            }

            _packets.value = filteredPackets
        }
    }

    //Postoji funkcija u kotlinu za distancu
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
            sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(
                2
            )
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

     /**/
}

class MainVMFactory(
    private val packetRepository: PacketRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainVM::class.java)) {
            return MainVM(packetRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
