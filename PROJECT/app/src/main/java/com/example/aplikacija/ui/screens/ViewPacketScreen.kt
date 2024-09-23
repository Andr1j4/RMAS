package com.example.aplikacija.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.aplikacija.repository.PacketRepository
import com.example.aplikacija.ui.theme.PlavaMain
import com.example.aplikacija.ui.theme.fontInria
import com.example.aplikacija.ui.viewmodels.ViewPacketVM
import com.example.aplikacija.ui.viewmodels.ViewPacketVMFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun ViewPacketScreen(
    packetId: String,
    packetRepository: PacketRepository,
    onNavigateToMain: () -> Unit,
    onNavigateToPacketList: () -> Unit,
    onNavigateToEditPacket: (String) -> Unit
) {
    val viewModel: ViewPacketVM = viewModel(factory = ViewPacketVMFactory(packetRepository))
    val packet by viewModel.packetDetails.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val currentUserUID = Firebase.auth.currentUser?.uid

    Log.d("ViewPacketScreen", "Opening packet with ID: $packetId")

    LaunchedEffect(packetId) {
        if (packet == null) {
            viewModel.loadPacketDetails(packetId)
        }
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Maps&Friends",
                color = PlavaMain,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 40.sp,
                fontFamily = fontInria
            )

            Log.w("VIEWPACKETSCREEN", "PACKET.LET JE POSLE OVE PORUKE")

            packet?.let { packetData ->
                Log.w("VIEWPACKETSCREEN", "U PACKET.LET ${packet!!.id}")

                val isAuthor = currentUserUID == packetData.autor

                Text(
                    text = "Paket Details",
                    color = PlavaMain,
                    fontFamily = fontInria
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Opis: ${packetData.opis}", fontFamily = fontInria, color = PlavaMain)
                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = rememberAsyncImagePainter(packetData.slikaURL),
                    contentDescription = "Packet Image",
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (packetData.lat != null && packetData.lng != null) {
                    Text(text = "Lokacija: (${packetData.lat}, ${packetData.lng})", fontFamily = fontInria, color = PlavaMain)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isAuthor) {
                    Button(
                        onClick = { onNavigateToEditPacket(packetId) },
                        colors = ButtonDefaults.buttonColors(containerColor = PlavaMain)
                    ) {
                        Text("Edit Packet", fontFamily = fontInria)
                    }
                    Button(onClick = {
                        viewModel.deletePacket(packetId)
                            onNavigateToMain()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Delete Packet", fontFamily = fontInria)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Back buttons
                Button(
                    onClick = { onNavigateToMain() },
                    colors = ButtonDefaults.buttonColors(containerColor = PlavaMain)
                ) {
                    Text("Vrati se na Main", fontFamily = fontInria)
                }
                Button(
                    onClick = { onNavigateToPacketList() },
                    colors = ButtonDefaults.buttonColors(containerColor = PlavaMain)
                ) {
                    Text("Vrati se na listu paketa", fontFamily = fontInria)
                }
            } ?: run {
                // Show error message
                //Toast.makeText(context, "Nije uzeo podatke iz baze kako treba", Toast.LENGTH_SHORT).show()
                Log.w("Firestore", "NIJE UZEO PODATKE KAKO TREBA")
            }
        }
}

