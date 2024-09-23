package com.example.aplikacija.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplikacija.classes.PacketClass
import com.example.aplikacija.repository.PacketRepository
import com.example.aplikacija.ui.theme.PlavaLight
import com.example.aplikacija.ui.theme.PlavaMain
import com.example.aplikacija.ui.theme.fontInria
import com.example.aplikacija.ui.viewmodels.PacketListVM
import com.example.aplikacija.ui.viewmodels.PacketListVMFactory
import com.example.aplikacija.ui.viewmodels.ViewPacketVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacketListScreen(
    packetRepository: PacketRepository,
    onNavigateToViewPacket: (Any?) -> Unit,
    onNavigateToMain: () -> Unit,
    viewPacketVM: ViewPacketVM
) {
    val viewModel: PacketListVM = viewModel(factory = PacketListVMFactory(packetRepository))
    val packetList by viewModel.packetList.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val isAscending by viewModel.isAscending.collectAsState()


    if (viewModel.isLoading.collectAsState().value) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            // Button to toggle sorting order
            Button(
                onClick = { viewModel.toggleSortOrder() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAscending) PlavaMain else PlavaLight
                )
            ) {
                Text(if (isAscending) "Sort by Date Ascending" else "Sort by Date Descending")
            }

            Spacer(modifier = Modifier.height(16.dp))


            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { onNavigateToMain() },
                        containerColor = PlavaMain,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, // Back arrow icon
                            contentDescription = "Back to Main"
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Lista Paketa",
                        fontSize = 24.sp,
                        color = PlavaMain,
                        fontFamily = fontInria
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(packetList) { packet ->
                        PacketItem(packet, onClick = {
                            viewPacketVM.loadPacketDetails(packet.id)
                            onNavigateToViewPacket(packet.id)
                        })
                    }
                }
            }
        }
    }

    viewModel.errorMessage.collectAsState().value?.let { errorMessage ->
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        Log.w("Problem", "Ne radi ovde 108ln PacketList")
    }
}

@Composable
fun PacketItem(packet: PacketClass, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Opis: ${packet.opis}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Datum: ${packet.DatumPostavljanja}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
