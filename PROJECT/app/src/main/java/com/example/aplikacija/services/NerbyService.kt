package com.example.aplikacija.services

import android.R
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.aplikacija.MainActivity
import com.example.aplikacija.classes.PacketClass
import com.example.aplikacija.ds.PacketDS
import com.example.aplikacija.repository.PacketRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NearbyService() : Service() {

    //CHANNEL ID za NOTIFCATION CHANNEL
    private val CHANNEL_ID = "ProximityServiceChannel"

    //ZA PRACENJE LOKACIJE
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var notifiedPackets = mutableSetOf<String>()


    //Foreground Servis pa ne treba onBind
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotifChannel()

        notifiedPackets.clear()

        val notif = createNotif()
        startForeground(1, notif)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Toast.makeText(this, "Servis pokrenut", Toast.LENGTH_SHORT).show()
        pratiLokaciju()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()

        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        Toast.makeText(this, "Servis iskljucen", Toast.LENGTH_SHORT).show()
    }

    //Kreiranje kanala za notifikacije
    @SuppressLint("ObsoleteSdkInt")
    private fun createNotifChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Kanal NearbyService-a"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = "Kanal za servis NearbyService"
                }
            val notifManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notifManager.createNotificationChannel(channel)
        }
    }

    private fun createNotif(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NearbyService ukljucen")
            .setContentText("Pracenje lokacije")
            .setSmallIcon(R.drawable.sym_def_app_icon)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

/*
    private fun loadNotifiedPackets() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val notifiedPacketsStringSet = prefs.getStringSet(KEY_NOTIFIED_PACKETS, emptySet())
        notifiedPackets = notifiedPacketsStringSet?.toMutableSet() ?: mutableSetOf()
        Log.d("NearbyService", "Loaded notified packets: $notifiedPackets")
    }

    // Save notified packets to SharedPreferences
    private fun saveNotifiedPackets() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        with(prefs.edit()) {
            putStringSet(KEY_NOTIFIED_PACKETS, notifiedPackets)
            apply()
        }
        Log.d("NearbyService", "Saved notified packets: $notifiedPackets")
    }

 */

    @SuppressLint("MissingPermission")
    private fun pratiLokaciju() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //prati lokaciju
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location = locationResult.lastLocation!!
                Log.w("Lokacija", "Posle ovoga treba da pozove proveri blizinu")
                proveriBlizinu(location)
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }


    private fun proveriBlizinu(userLocation: Location) {
        val packetRepository = PacketRepository(packetDS = PacketDS())
        CoroutineScope(Dispatchers.IO).launch {
            val packets = packetRepository.getAllPackets() // Get packets from repository

            Log.d("NearbyService", "Retrieved ${packets.size} packets.")

            val todaysDateUNF = System.currentTimeMillis()
            val todaysDate = Date(todaysDateUNF)
            val dateFormat = SimpleDateFormat("dd. MMMM yyyy.", Locale("bs", "BS", "LAT"))

            /*
            // Filter packets based on the date (assuming the packet has a "datumPostavljanja" field)
            val filteredPackets = packets.filter { packet ->
                val packetDate = dateFormat.parse(packet.DatumPostavljanja)
                packetDate?.after(todaysDate) ?: false

            }

             */

            // Loop through the filtered packets and check distance
            packets.forEach { packet ->
                Log.w("PROVERA", "Video je pakete")
                val packetLocation = Location("").apply {
                    latitude = packet.lat ?: 0.0
                    longitude = packet.lng ?: 0.0
                }

                Log.d("NearbyServiceProvera", "User Location: ${userLocation.latitude}, ${userLocation.longitude}")
                Log.d("NearbyServicePROVERA", "Packet Location: ${packet.lat}, ${packet.lng}")

                // Racunamo distancu
                val distance = userLocation.distanceTo(packetLocation) / 1000.0

                // Ako je manje od 100m posalji notofokaciju
                if (distance <= 0.1 && !notifiedPackets.contains(packet.id)) {
                    pushNotification(packet)
                    notifiedPackets.add(packet.id) // Mark the packet as notified
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun pushNotification(packet: PacketClass) {

        if (notifiedPackets.contains(packet.id)) {
            // Necemo da saljemo ako smo vec poslali za paket
            return
        }

        notifiedPackets.add(packet.id)

        Log.d("NearbyService", "Preparing to send notification for packet ID: ${packet.id}")

        val id = packet.id.hashCode()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("packet_id", packet.id)
        }

        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        var notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Paket je blizu!")
            .setContentText("Nalazite se u blizini nekog paketa.")
            .setSmallIcon(R.drawable.sym_def_app_icon)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(id, notification)
    }
}
