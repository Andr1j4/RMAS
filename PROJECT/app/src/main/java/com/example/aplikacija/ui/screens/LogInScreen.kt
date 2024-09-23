package com.example.aplikacija.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplikacija.repository.UserRepository
import com.example.aplikacija.ui.theme.PlavaMain
import com.example.aplikacija.ui.theme.fontInria
import com.example.aplikacija.ui.viewmodels.LoginVM
import com.example.aplikacija.ui.viewmodels.LoginVMFactory
import compose.icons.FeatherIcons
import compose.icons.feathericons.Eye
import compose.icons.feathericons.EyeOff

@Composable
fun LogIn(
    onNavigateToRegister: () -> Unit,
    onNavigateToStart: () -> Unit,
    onNavigateToMain: () -> Unit,
    userRep: UserRepository
){

    val context = LocalContext.current
    val lgModel : LoginVM = viewModel(factory = LoginVMFactory(userRep))
    val email by lgModel.emailLogin.collectAsState()
    val password by lgModel.passwordLogin.collectAsState()
    val showPassword by lgModel.showPasswordLogin.collectAsState()
    val okToLogin by lgModel.okToLogin.collectAsState()
    val firebaseOk by lgModel.firebaseOk.collectAsState()
    val loginAttempted by lgModel.loginAttempted.collectAsState()

    LaunchedEffect(loginAttempted) {
        if (loginAttempted) {
            if (firebaseOk) {
                onNavigateToMain()
            } else {
                Toast.makeText(context, "Greska u prijavljivanju", Toast.LENGTH_SHORT).show()
            }
            lgModel.resetLoginAttempt()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Maps&Friends",
            modifier = Modifier.padding(10.dp),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 50.sp,
            fontFamily = fontInria,
            color = PlavaMain
        )

        Text(
            text = "Uloguj se na svoj nalog",
            fontSize = 30.sp,
            fontFamily = fontInria,
            color = PlavaMain
        )
        Column(modifier = Modifier.padding(20.dp)) {
            TextField(
                placeholder = { Text("Unesi email", color = PlavaMain, fontFamily = fontInria) },
                value = email,
                onValueChange = { newtext ->
                    lgModel.updateEmailTp(newtext)
                },
                singleLine = true,
                modifier = Modifier
                    .padding(10.dp)
                    .widthIn(0.dp, 250.dp)
                    .shadow(3.dp, RoundedCornerShape(12.dp))
            )

            TextField(
                placeholder = { Text("Unesi lozinku", color = PlavaMain, fontFamily = fontInria) },
                value = password,
                onValueChange = {newtext ->
                    lgModel.updatePasswordTp(newtext)
                },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (showPassword)
                        FeatherIcons.Eye
                    else FeatherIcons.EyeOff

                    val description = if (showPassword) "Hide password" else "Show password"

                    IconButton(onClick = { lgModel.updateShowPasswordLogin(!showPassword) }) {
                        Icon(imageVector = image, description)
                    }
                }
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Button(
                enabled = okToLogin,
                onClick = {
                    lgModel.login()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PlavaMain
                ),
                shape = RoundedCornerShape(20)
            ) {
                Text("Prijavi se", fontFamily = fontInria, style = TextStyle(fontSize = 20.sp))
            }
            Button(
                onClick = onNavigateToRegister,
                colors = ButtonDefaults.buttonColors(containerColor = PlavaMain),
                shape = RoundedCornerShape(20),
                modifier = Modifier
                    .padding(top = 30.dp)

            ) {
                Text(
                    text = "Registruj se!",
                    fontFamily = fontInria,
                    style = TextStyle(fontSize = 20.sp)
                )
            }
            Button(
                onClick = onNavigateToStart,
                colors = ButtonDefaults.buttonColors(containerColor = PlavaMain),
                shape = RoundedCornerShape(20),
                modifier = Modifier
                    .padding(top = 30.dp)
            ) {
                Text(
                    "Nazad na pocetni ekran",
                    fontFamily = fontInria,
                    style = TextStyle(fontSize = 20.sp)
                )
            }
        }

    }


}