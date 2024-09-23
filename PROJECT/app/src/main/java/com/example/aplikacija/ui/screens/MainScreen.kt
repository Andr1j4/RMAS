package com.example.aplikacija.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplikacija.repository.PacketRepository
import com.example.aplikacija.repository.UserRepository
import com.example.aplikacija.ui.theme.Ikonice
import com.example.aplikacija.ui.theme.PlavaLight
import com.example.aplikacija.ui.theme.PlavaMain
import com.example.aplikacija.ui.theme.fontInria
import com.example.aplikacija.ui.viewmodels.MainVM
import com.example.aplikacija.ui.viewmodels.MainVMFactory
import com.example.aplikacija.ui.viewmodels.ViewPacketVM
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import compose.icons.FeatherIcons
import compose.icons.feathericons.Anchor
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.Check
import compose.icons.feathericons.Disc
import compose.icons.feathericons.List
import compose.icons.feathericons.Plus
import compose.icons.feathericons.User
import compose.icons.feathericons.X

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToAddPacket: () -> Unit,
    onNavigateToUserList: () -> Unit,
    onNavigateToViewPacket: (String?) -> Unit,
    viewPacketVM: ViewPacketVM,
    packetRepository: PacketRepository,
    userRepository: UserRepository
) {

    val vwModel: MainVM = viewModel(factory = MainVMFactory(packetRepository, userRepository))

    val openDialog by vwModel.openDialogMain.collectAsState()
    val showDate by vwModel.showDate.collectAsState()
    val isExpandedProd by vwModel.isExpandedProdMain.collectAsState()
    val dateState = vwModel.dateState
    val usrLocation by vwModel.userLocation.collectAsState()
    val sliderPos by vwModel.slidePosMain.collectAsState()
    val markerState by vwModel.markerState.collectAsState()
    val context = LocalContext.current
    val packets by vwModel.packets.collectAsState(initial = emptyList())
    val cameraPositionState by vwModel.cameraPositionState.collectAsState()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.DESTROYED -> {}
            Lifecycle.State.INITIALIZED -> {}
            Lifecycle.State.CREATED -> {
                vwModel.getAllPackets()
                Log.d("MainScreen", "Packets: ${packets}")
            }

            Lifecycle.State.STARTED -> {}
            Lifecycle.State.RESUMED -> {
                vwModel.getAllPackets()
                Log.d("MainScreen", "Packets: ${packets}")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            vwModel.stopLocationUpdates(context)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            vwModel.getCurrentLocation(context)
        } else {
            Toast.makeText(context, "Location permission is required to use this feature", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                vwModel.getCurrentLocation(context)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {

                Toast.makeText(context, "Nagovestavam da lokacija nije uzela vrednost i da ne radi.", Toast.LENGTH_LONG).show()
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    val nis = LatLng(43.321445, 21.896104)

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = false,
            )
        )
    }

    val properties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL))
    }

    Box(modifier = Modifier.fillMaxSize()){
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = properties,
            uiSettings = uiSettings
        ){
            usrLocation?.let { location ->
                val userLatLng = LatLng(location.latitude, location.longitude)

                vwModel.onUpdatePosition(location)
                Marker(
                    state = markerState,
                    title = "Vasa lokacija",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
                Circle(
                    center = userLatLng,
                    radius = (sliderPos * 50.0),
                    strokeColor = PlavaMain,
                    fillColor = Color(0x2C0088FF),
                    strokeWidth = 2f
                )
            }

            //val viewPacketVM: ViewPacketVM = viewModel(factory = ViewPacketVMFactory(packetRepository))

            packets?.forEach { packet ->
                Log.d("PacketsList", "Packet ID: ${packet.id}")  // Ensure the ID is not null
                Marker(
                    state = MarkerState(position = LatLng(packet.lat ?: 0.0, packet.lng ?: 0.0)),
                    title = "${packet.id}: ${packet.opis}",
                    onInfoWindowClick = {
                        Log.d("MarkerClick", "Clicked on packet with ID: ${packet.id}")
                        viewPacketVM.updateOpisPacket(packet.opis)
                        viewPacketVM.updateAutor(packet.autor)
                        viewPacketVM.updateSlikaURL(packet.slikaURL)
                        viewPacketVM.updateDatumPostavljanja(packet.DatumPostavljanja)
                        viewPacketVM.updateID(packet.id ?: "")  // Use safe call in case ID is null
                        viewPacketVM.loadPacketDetails(packet.id ?: "")
                        onNavigateToViewPacket(packet.id ?: "")
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                FloatingActionButton(
                    onClick = { onNavigateToProfile() },
                    containerColor = Ikonice,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = FeatherIcons.User,
                        contentDescription = "Profile"
                    )
                }
                Column {
                    FloatingActionButton(
                        onClick = { onNavigateToUserList() },
                        containerColor = Ikonice,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(10.dp)
                    ) {
                        Icon(imageVector = FeatherIcons.List, contentDescription = "User_List")
                    }
                }

            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                FloatingActionButton(
                    containerColor = Ikonice,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .padding(10.dp),
                    onClick = { vwModel.updateOpenDialog(true) },
                ) {
                    Icon(imageVector = FeatherIcons.Disc, contentDescription = "List_radius")
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxSize()
                ) {

                    FloatingActionButton(
                        containerColor = Ikonice,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(10.dp),
                        onClick = {
                            try {
                                vwModel.updateCameraPosition(
                                    CameraPositionState(
                                        CameraPosition(
                                            usrLocation!!,
                                            18f,
                                            0f,
                                            0f
                                        )
                                    )
                                )
                            } catch (e: Exception) {
                                Log.w("ERROR", "Cant recenter!")
                            }
                        },
                    ) {
                        Icon(
                            imageVector = FeatherIcons.Anchor,
                            contentDescription = "Location"
                        )
                    }

                    FloatingActionButton(
                        containerColor = Ikonice,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(10.dp),
                        onClick = {
                            onNavigateToAddPacket()
                        },
                    ) {
                        Icon(imageVector = FeatherIcons.Plus, contentDescription = "Add packet")
                    }

                    when {
                        openDialog -> {
                            Dialog(onDismissRequest = { /*TODO*/ }) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(600.dp)
                                        .padding(16.dp)
                                        .verticalScroll(rememberScrollState()),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        FloatingActionButton(
                                            shape = RoundedCornerShape(20.dp),
                                            onClick = { vwModel.updateOpenDialog(false) },
                                            containerColor = Color.White,
                                            modifier = Modifier
                                                .padding(10.dp)
                                                .shadow(4.dp, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = FeatherIcons.ArrowLeft,
                                                contentDescription = "Back",
                                                tint = PlavaMain
                                            )
                                        }

                                        Surface(
                                            modifier = Modifier
                                                .height(400.dp)
                                                .fillMaxWidth(),
                                            color = Color.White
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(10.dp)
                                            ) {
                                                //OVDE AKO IMAM VREMENA DA NAPRAVIM DA PRIKAZUJE SAMO PAKETE
                                                // KOJE JE IZBACIO PRIJATELJ ILI SVE PAKETE
                                                ExposedDropdownMenuBox(
                                                    expanded = isExpandedProd,
                                                    onExpandedChange = {
                                                        vwModel.updateIsExpandedProd(!isExpandedProd)
                                                    }) {
                                                }
                                                Text(
                                                    text = "Prikaz paketa do dana:",
                                                    fontFamily = fontInria
                                                )
                                                FloatingActionButton(
                                                    onClick = { vwModel.updateShowDate(true) },
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier
                                                        .padding(10.dp)
                                                        .widthIn(250.dp, 250.dp),
                                                    containerColor = Color.White
                                                ) {
                                                    Text(
                                                        text = vwModel.getFormattedDate(),
                                                        textAlign = TextAlign.Start,
                                                        fontFamily = fontInria,
                                                        color = Color.Black
                                                    )
                                                }
                                                Text(
                                                    text = "Obim pretrage",
                                                    fontFamily = fontInria
                                                )
                                                Slider(
                                                    value = sliderPos,
                                                    onValueChange = { vwModel.updateSlidePos(it) },
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = PlavaMain,
                                                        activeTrackColor = PlavaMain,
                                                        inactiveTrackColor = PlavaLight,
                                                    ),
                                                    steps = 3,
                                                    valueRange = 0f..50f
                                                )

                                            }
                                            /*
                                            if (showDate) {
                                                DatePickerDialog(
                                                    colors = DatePickerDefaults.colors(
                                                        containerColor = Color.White,
                                                    ),
                                                    modifier = Modifier.verticalScroll(
                                                        rememberScrollState()
                                                    ),
                                                    onDismissRequest = { vwModel.updateShowDate(true) },
                                                    confirmButton = {
                                                        Button(
                                                            colors = ButtonDefaults.buttonColors(
                                                                contentColor = Color.White,
                                                                containerColor = Ikonice
                                                            ),
                                                            onClick = {
                                                                vwModel.updateShowDate(false)
                                                                vwModel.filterPacketsByDate()
                                                            }
                                                        ) {
                                                            Text(text = "Prihvati", fontFamily = fontInria)
                                                        }
                                                    },
                                                    dismissButton = {
                                                        Button(
                                                            colors = ButtonDefaults.buttonColors(
                                                                contentColor = Color.White,
                                                                containerColor = Ikonice
                                                            ),
                                                            onClick = { vwModel.updateShowDate(false) }
                                                        ) {
                                                            Text(text = "Odustani", fontFamily = fontInria)
                                                        }
                                                    },
                                                ) {
                                                    DatePicker(
                                                        state = dateState.value,
                                                        showModeToggle = true,
                                                        colors = DatePickerDefaults.colors(
                                                            containerColor = Color.White,
                                                            titleContentColor = PlavaMain,
                                                            headlineContentColor = PlavaMain,
                                                            weekdayContentColor = Color.Black,
                                                            subheadContentColor = Color.Black,
                                                            dayContentColor = Color.Black,
                                                            selectedDayContainerColor = PlavaMain,
                                                            selectedDayContentColor = PlavaLight,
                                                            todayContentColor = PlavaLight,
                                                            todayDateBorderColor = PlavaLight,
                                                            dayInSelectionRangeContentColor = Color.White,
                                                            dayInSelectionRangeContainerColor = PlavaLight,
                                                            disabledDayContentColor = Color.Gray,
                                                        )
                                                    )

                                                }
                                            }*/
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(100.dp),
                                            verticalAlignment = Alignment.Bottom,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Button(
                                                onClick = {
                                                    vwModel.resetFilters()
                                                    vwModel.updateOpenDialog(false)
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = PlavaMain
                                                ),
                                                modifier = Modifier.padding(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = FeatherIcons.X,
                                                    contentDescription = "Close"
                                                )
                                            }
                                            Button(
                                                onClick = {
                                                    vwModel.filterPackets()
                                                    vwModel.updateOpenDialog(false)
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = PlavaMain
                                                ),
                                                modifier = Modifier.padding(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = FeatherIcons.Check,
                                                    contentDescription = "Done"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

