package com.example.aplikacija.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplikacija.repository.UserRepository
import com.example.aplikacija.ui.theme.PlavaLight
import com.example.aplikacija.ui.theme.PlavaMain
import com.example.aplikacija.ui.theme.fontInria
import com.example.aplikacija.ui.viewmodels.UserListVM
import com.example.aplikacija.ui.viewmodels.UserListVMFactory
import compose.icons.FeatherIcons
import compose.icons.feathericons.User

//Pored prikazivanja svih korisnika moci ce da se filtrira da prikazuje prijatelje
@Composable
fun UserListScreen(
    onNavigateToMain: () -> Unit,
    userRep: UserRepository
) {
    val vwModel: UserListVM = viewModel(factory = UserListVMFactory(userRep))
    val users by vwModel.users.collectAsState()
    val currUID by vwModel.usrUID.collectAsState()
    val filterType by vwModel.filterType.collectAsState()
    val friends by vwModel.friends.collectAsState()

    LaunchedEffect(Unit) {
        vwModel.getAllUsers()
        vwModel.getCurrentUID()
        vwModel.getFriends()
        Log.w("TEST", "RADI")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Add a dropdown menu or buttons for filtering
        FilterDropdownMenu(
            selectedFilter = filterType,
            onFilterChange = { vwModel.changeFilter(it) },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FloatingActionButton(
                        containerColor = Color.White,
                        contentColor = PlavaMain,
                        shape = CircleShape,
                        modifier = Modifier.padding(20.dp),
                        onClick = { onNavigateToMain() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "goBack")
                    }
                    Text(
                        text = "Lista korisnika",
                        fontFamily = fontInria,
                        fontSize = 30.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    users?.forEachIndexed { index, user ->
                        if (user.uid == currUID) {
                            Surface(
                                color = PlavaMain,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .heightIn(50.dp, 50.dp),
                                shape = RoundedCornerShape(20)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${index + 1}.",
                                        color = Color.White,
                                        fontFamily = fontInria,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = user.ime + " " + user.prezime,
                                        color = Color.White,
                                        fontFamily = fontInria,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "broj bodova:",
                                        color = Color.White,
                                        fontFamily = fontInria,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = user.bod.toString(),
                                        color = Color.White,
                                        fontFamily = fontInria,
                                        fontSize = 15.sp
                                    )
                                    if (user.uid != currUID && friends?.none { it.uid == user.uid } == true) {
                                        Button(
                                            onClick = {
                                                vwModel.DodajPrijatelja(
                                                    user.ime,
                                                    user.prezime
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                contentColor = Color.White,
                                                containerColor = PlavaLight
                                            )
                                        ) {
                                            Icon(
                                                imageVector = FeatherIcons.User,
                                                contentDescription = "Dodaj prijatelja"
                                            )
                                        }
                                   }
                                }
                            }
                        } else {
                            Surface(
                                border = BorderStroke(1.dp, color = PlavaMain),
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .heightIn(50.dp, 50.dp),
                                shape = RoundedCornerShape(20)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${index + 1}.",
                                        color = PlavaMain,
                                        fontFamily = fontInria,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = user.ime + " " + user.prezime,
                                        color = PlavaMain,
                                        fontFamily = fontInria,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "broj bodova:",
                                        color = PlavaMain,
                                        fontFamily = fontInria,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = user.bod.toString(),
                                        color = PlavaMain,
                                        fontFamily = fontInria,
                                        fontSize = 15.sp
                                    )
                                    if (friends?.any { it.uid == user.uid } == false) {
                                        Button(
                                            onClick = {
                                                vwModel.DodajPrijatelja(
                                                    user.ime,
                                                    user.prezime
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                contentColor = Color.White,
                                                containerColor = PlavaLight
                                            )
                                        ) {
                                            Icon(
                                                imageVector = FeatherIcons.User,
                                                contentDescription = "Dodaj prijatelja"
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdownMenu(
    selectedFilter: UserListVM.FilterType,
    onFilterChange: (UserListVM.FilterType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }  // Track dropdown menu state
    val filterOptions = listOf(
        "Bodovi Ascending" to UserListVM.FilterType.POINTS_ASCENDING,
        "Bodovi Descending" to UserListVM.FilterType.POINTS_DESCENDING,
        "Prikazi Prijatelje" to UserListVM.FilterType.FRIENDS
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.background(Color.White)
        //colors = ExposedDropdownMenuDefaults.textFieldColors(focusedTextColor = Color.Red, disabledPlaceholderColor = Color.Green)
    ) {
        TextField(
            readOnly = true,
            value = when (selectedFilter) {
                UserListVM.FilterType.POINTS_ASCENDING -> "Bodovi Ascending"
                UserListVM.FilterType.POINTS_DESCENDING -> "Bodovi Descending"
                UserListVM.FilterType.FRIENDS -> "Prikazi Prijatelje"
                else -> ""
            },
            onValueChange = {},
            label = {
                Text("Filtriraj",
                color = PlavaMain,
                fontFamily = fontInria
                ) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .background(Color.White)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            filterOptions.forEach { (label, filterType) ->
                DropdownMenuItem(
                    text = { Text(text = label, color = PlavaMain, fontFamily = fontInria) },
                    //colors = MenuItemColors(disabledTextColor = PlavaMain, disabledLeadingIconColor = PlavaLight, disabledTrailingIconColor = PlavaLight, textColor = PlavaMain, leadingIconColor = PlavaLight, trailingIconColor = PlavaLight),
                    modifier = Modifier.background(Color.White),
                    onClick = {
                        onFilterChange(filterType)
                        expanded = false
                    }
                )
            }
        }
    }
}

