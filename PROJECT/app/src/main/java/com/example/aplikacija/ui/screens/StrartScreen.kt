package com.example.aplikacija.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplikacija.repository.UserRepository
import com.example.aplikacija.ui.theme.PlavaMain
import com.example.aplikacija.ui.theme.fontInria
import com.example.aplikacija.ui.viewmodels.ProfileVM
import com.example.aplikacija.ui.viewmodels.ProfileVMFactory

@Composable
fun StartScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToProfile: () -> Unit,
    userRep: UserRepository
) {
    val viewModel: ProfileVM = viewModel(factory = ProfileVMFactory(userRep))
    val context = LocalContext.current


    // Ako je prijavljen ide direktno na main
    LaunchedEffect(Unit) {
        if (viewModel.getUserTF() != null) {
            onNavigateToMain()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Map&Friends",
            fontWeight = FontWeight.ExtraBold,
            fontFamily = fontInria,
            color = PlavaMain,
            fontSize = 50.sp
        )
        Button(
            onClick = { onNavigateToRegister() },
            colors = ButtonDefaults.buttonColors(containerColor = PlavaMain),
            shape = RoundedCornerShape(20),
            modifier = Modifier.padding(30.dp)
        ) {
            Text("Register", fontFamily = fontInria, fontSize = 30.sp)
        }
        Button(
            onClick = { onNavigateToLogin() },
            colors = ButtonDefaults.buttonColors(containerColor = PlavaMain),
            shape = RoundedCornerShape(20),
            modifier = Modifier.padding(30.dp)
        ) {
            Text("Login", fontFamily = fontInria, fontSize = 30.sp)
        }
    }
}
