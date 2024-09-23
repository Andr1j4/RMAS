package com.example.aplikacija.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplikacija.components.LoadImage
import com.example.aplikacija.repository.UserRepository
import com.example.aplikacija.services.NearbyService
import com.example.aplikacija.ui.theme.PlavaLight
import com.example.aplikacija.ui.theme.PlavaMain
import com.example.aplikacija.ui.theme.fontInria
import com.example.aplikacija.ui.viewmodels.ProfileVM
import com.example.aplikacija.ui.viewmodels.ProfileVMFactory
import compose.icons.FeatherIcons
import compose.icons.feathericons.Archive
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.LogOut

@Composable
fun ProfileScreen(
    onNavigateToMain: () -> Unit,
    onNavigateWhenLogout: () -> Unit,
    onNavigateToPacketList: () -> Unit,
    userRep: UserRepository
) {

    val vwModel: ProfileVM = viewModel(factory = ProfileVMFactory(userRep))
    val ime by vwModel.imeProf.collectAsState()
    val prezime by vwModel.prezimeProf.collectAsState()
    val email by vwModel.emailProf.collectAsState()
    val brTelefona by vwModel.brojTelefonaProf.collectAsState()
    val brojBodova by vwModel.brojBodovaProf.collectAsState()
    val imageUrl by vwModel.imageURL.collectAsState()
    val context = LocalContext.current

    var checked by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        vwModel.getUserInfo()
        checked = vwModel.getSavedServiceStatus(context)

        val isServiceRunning = vwModel.isServiceRunning(context, NearbyService::class.java)
        val checkedFromSuspend = vwModel.getServiceAllowed() ?: false

        if (checkedFromSuspend && !isServiceRunning) {
            Toast.makeText(context, "Servis je automatski pokrenut!", Toast.LENGTH_SHORT).show()
            vwModel.startNearbyService(context)
        } else {
            Toast.makeText(context, "Servis vec radi ili je zabranjen!", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        LazyColumn {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FloatingActionButton(
                        onClick = { onNavigateToMain() },
                        containerColor = PlavaMain,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            imageVector = FeatherIcons.ArrowLeft,
                            contentDescription = "goBack"
                        )
                    }
                    Text(
                        text = "Profil",
                        fontFamily = fontInria,
                        fontSize = 30.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,

                    ) {

                    LoadImage(
                        url = imageUrl,

                        )
                    Column {
                        Text(
                            text = ime,
                            fontFamily = fontInria,
                            fontSize = 40.sp,
                            color = PlavaMain,
                            fontWeight = FontWeight.Bold,

                            )
                        Text(
                            text = prezime,
                            fontFamily = fontInria,
                            fontSize = 40.sp,
                            color = PlavaMain,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Info",
                        color = PlavaMain,
                        fontFamily = fontInria,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(20.dp),
                        fontWeight = FontWeight.Thin
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(end = 20.dp),
                        thickness = 2.dp, color = PlavaMain
                    )
                }

            }
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Email:",
                            color = Color.Black,
                            fontFamily = fontInria,
                            fontSize = 20.sp,
                        )
                        Text(
                            text = email,
                            color = Color.Black,
                            fontFamily = fontInria,
                            fontSize = 20.sp
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Broj telefona:",
                            color = Color.Black,
                            fontFamily = fontInria,
                            fontSize = 20.sp
                        )
                        Text(
                            text = brTelefona,
                            color = Color.Black,
                            fontFamily = fontInria,
                            fontSize = 20.sp
                        )
                    }

                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Bodovi",
                        color = PlavaMain,
                        fontFamily = fontInria,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(20.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(end = 20.dp),
                        thickness = 2.dp, color = PlavaMain
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Broj bodova:",
                            color = Color.Black,
                            fontFamily = fontInria,
                            fontSize = 20.sp,
                        )
                        Text(
                            text = brojBodova.toString(),
                            color = Color.Black,
                            fontFamily = fontInria,
                            fontSize = 20.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Paketi",
                            color = PlavaMain,
                            fontFamily = fontInria,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .padding(20.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(end = 10.dp),
                            thickness = 2.dp, color = PlavaMain
                        )
                    }

                    Button(
                        onClick = { onNavigateToPacketList() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PlavaMain
                        ), shape = RoundedCornerShape(20)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = FeatherIcons.Archive,
                                contentDescription = "Saved",
                                tint = Color.White,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Text(
                                text = "Istorija postavljenih paketa",
                                fontFamily = fontInria,
                                fontSize = 20.sp,
                                color = Color.White
                            )

                        }
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Aktiviraj servis",
                            fontFamily = fontInria,
                            modifier = Modifier.padding(horizontal = 10.dp),
                            color = Color.Black,
                            fontSize = 20.sp
                        )
                        /*
                        Switch(
                            checked = checked,
                            onCheckedChange = {
                                vwModel.serviceFunction(context = context)
                                vwModel.updateServiceCheckedProf(it)
                            }
                        )
                         */
                        Switch(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    vwModel.startNearbyService(context)
                                } else {
                                    vwModel.stopNearbyService(context)
                                }
                                checked = isChecked
                                vwModel.updateServiceCheckedProf(isChecked)
                                vwModel.saveServiceStatus(context, isChecked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PlavaMain,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = PlavaLight,
                                uncheckedBorderColor = PlavaLight
                            )
                        )
                    }
                    Button(
                        onClick = {
                            userRep.logout()
                            onNavigateWhenLogout()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PlavaMain
                        ),
                        shape = RoundedCornerShape(20),
                        modifier = Modifier.padding(top = 20.dp)

                    ) {
                        Text(
                            text = "Odjavite se",
                            fontFamily = fontInria,
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp)
                        )
                        Icon(
                            imageVector = FeatherIcons.LogOut,
                            contentDescription = "Logout",
                            modifier = Modifier.padding(horizontal = 5.dp)
                        )
                    }
                }
            }
        }
    }
}