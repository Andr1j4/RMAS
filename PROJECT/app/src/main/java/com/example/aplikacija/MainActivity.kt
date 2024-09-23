package com.example.aplikacija

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aplikacija.ds.PacketDS
import com.example.aplikacija.ds.UserDS
import com.example.aplikacija.repository.PacketRepository
import com.example.aplikacija.repository.UserRepository
import com.example.aplikacija.ui.screens.AddPacketScreen
import com.example.aplikacija.ui.screens.EditPacketScreen
import com.example.aplikacija.ui.screens.LogIn
import com.example.aplikacija.ui.screens.MainScreen
import com.example.aplikacija.ui.screens.PacketListScreen
import com.example.aplikacija.ui.screens.ProfileScreen
import com.example.aplikacija.ui.screens.Register
import com.example.aplikacija.ui.screens.StartScreen
import com.example.aplikacija.ui.screens.UserListScreen
import com.example.aplikacija.ui.screens.ViewPacketScreen
import com.example.aplikacija.ui.viewmodels.ViewPacketVM
import com.example.aplikacija.ui.viewmodels.ViewPacketVMFactory
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    "android.permission.POST_NOTIFICATIONS"
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf("android.permission.POST_NOTIFICATIONS"),
                    1
                )
            }
        }
        setContent {
            Navigation()
        }
    }
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Navigation() {

    val context = LocalContext.current
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted) {
            Toast.makeText(context, "Permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )

    val permissionStatus = requiredPermissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    //Kad budem napravio ViewPacketScreen i VM trebace ovde da se napravi

    val viewpacketVM: ViewPacketVM =
        viewModel(factory = ViewPacketVMFactory(packetRepository = PacketRepository(packetDS = PacketDS())))
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        if (!permissionStatus) {
            permissionsLauncher.launch(requiredPermissions)
        }
    }
    NavHost(navController = navController, startDestination = "start") {
        composable("start") {
            StartScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToMain = { navController.navigate("main") },
                onNavigateToProfile = {navController.navigate("profile")},
                userRep = UserRepository(userDS = UserDS())
            )
        }
        
        composable("user_list") {
            UserListScreen(
                onNavigateToMain = { navController.popBackStack("main", false) },
                userRep = UserRepository(userDS = UserDS())
            )
        }

        composable("main") {
            MainScreen(
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToAddPacket = { navController.navigate("add_packet") },
                onNavigateToUserList = {navController.navigate("user_list")},
                onNavigateToViewPacket = { packetId ->
                    navController.navigate("view_packet/$packetId")},
                viewPacketVM = viewpacketVM,
                packetRepository = PacketRepository(packetDS = PacketDS()),
                userRepository = UserRepository(userDS = UserDS())
            )
        }

        composable("profile") {
            ProfileScreen(
                onNavigateToMain = { navController.popBackStack("main", false) },
                userRep = UserRepository(userDS = UserDS()),
                onNavigateWhenLogout = { navController.popBackStack("start", false) },
                onNavigateToPacketList = { navController.navigate("packet_list")}
            )
        }

        composable("add_packet") {
            AddPacketScreen(
                onNavigateToMain = { navController.popBackStack("main", false) },
                packetRep = PacketRepository(packetDS = PacketDS())
            )
        }

        composable("view_packet/{packetId}") { backStackEntry ->
            val packetId = backStackEntry.arguments?.getString("packetId") ?: return@composable

            ViewPacketScreen(
                packetId = packetId,
                packetRepository = PacketRepository(packetDS = PacketDS()),
                onNavigateToMain = { navController.popBackStack("main", false) },
                onNavigateToPacketList = { navController.popBackStack("packet_list", false) },
                onNavigateToEditPacket = {
                    navController.navigate("edit_packet/$packetId")}
            )
        }

        composable("edit_packet/{packetId}") {backStackEntry ->
            val packetId = backStackEntry.arguments?.getString("packetId") ?: return@composable

            EditPacketScreen(
                packetId = packetId,
                onNavigateToPacketList = { navController.navigate("packet_list")},
                packetRepository = PacketRepository(packetDS = PacketDS())
            )
        }

        composable("packet_list"){
            PacketListScreen(
                packetRepository = PacketRepository(packetDS = PacketDS()),
                onNavigateToMain = { navController.popBackStack("main", false) },
                onNavigateToViewPacket = { packetId ->
                    navController.navigate("view_packet/$packetId")
                },
                viewPacketVM = ViewPacketVM(packetRepository = PacketRepository(packetDS = PacketDS()))
            )
        }

        composable("login") {
            LogIn(
                onNavigateToRegister = {
                    navController.popBackStack("landing", false)
                    navController.navigate("register")
                },
                onNavigateToStart = { navController.popBackStack("start", false) },
                onNavigateToMain = { navController.navigate("main") },
                userRep = UserRepository(userDS = UserDS())
            )
        }

        composable("register") {
            Register(
                onNavigateToLogin = {
                    navController.popBackStack("start", false)
                    navController.navigate("login")
                },
                onNavigateToStart = { navController.popBackStack("start", false) },
                onNavigateToMain = { navController.navigate("main") },
                userRep = UserRepository(userDS = UserDS()),
            )
        }
    }
}



