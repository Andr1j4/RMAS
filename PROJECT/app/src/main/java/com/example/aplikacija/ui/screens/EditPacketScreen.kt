package com.example.aplikacija.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.aplikacija.repository.PacketRepository
import com.example.aplikacija.ui.theme.PlavaMain
import com.example.aplikacija.ui.theme.fontInria
import com.example.aplikacija.ui.viewmodels.EditPacketVM
import com.example.aplikacija.ui.viewmodels.EditPacketVMFactory
import compose.icons.FeatherIcons
import compose.icons.feathericons.Camera
import compose.icons.feathericons.Image
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditPacketScreen(
    packetId: String,
    onNavigateToPacketList: () -> Unit,
    packetRepository: PacketRepository,
) {

    val viewModel: EditPacketVM = viewModel(factory = EditPacketVMFactory(packetRepository))
    val packet by viewModel.packetDetails.collectAsState(initial = null)
    val context = LocalContext.current
    var newOpis by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraEvent by viewModel.cameraEvent.collectAsState()
    val okImage by viewModel.okImage.collectAsState()
    var selectedFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(packetId) {
        viewModel.loadPacketDetails(packetId)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val file = viewModel.uriToFile(uri, context)
            selectedFile = file
            if (file != null) {
                viewModel.updateFile1(file)
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val file = viewModel.bitmapToFile(context, bitmap)
            if (file != null) {
                val uri = file.toUri()
                imageUri = uri
                viewModel.updateFile(file)
            }
        }
    }

    val buttonColor = if (okImage) Color.White else Color.White
    val iconTint = if (okImage) Color.White else Color.Cyan

    if (cameraEvent) {
        launcher.launch(null)
        viewModel.onCameraEventHandled()
    }

    packet?.let { packetData ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Edit Packet with ID: ${packetData.id}", color = PlavaMain, fontFamily = fontInria, fontSize = 30.sp)

            OutlinedTextField(
                value = newOpis,
                onValueChange = { newOpis = it },
                label = { Text(text = "Opis", color = PlavaMain, fontFamily = fontInria)},
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))


            // Image selection
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    shape = RoundedCornerShape(20),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 10.dp,
                        disabledElevation = 0.dp,
                        hoveredElevation = 4.dp,
                        focusedElevation = 4.dp,
                    ),
                    onClick = { viewModel.onOpenCamera() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor
                    )
                ) {
                    Icon(
                        tint = iconTint,
                        imageVector = FeatherIcons.Camera,
                        contentDescription = "Fotografija",
                    )
                }
                Button(
                    shape = RoundedCornerShape(20),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 10.dp,
                        disabledElevation = 0.dp,
                        hoveredElevation = 4.dp,
                        focusedElevation = 4.dp,
                    ),
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor
                    )
                ) {
                    Icon(
                        tint = iconTint,
                        imageVector = FeatherIcons.Image,
                        contentDescription = "Fotografija",
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedFile != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedFile),
                    contentDescription = "Selected Image (File)",
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "New Packet Image (Uri)",
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(text = "No image selected", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save changes
            Button(onClick = {
                if (newOpis.isNotEmpty()) {
                    viewModel.updateOpis(packetId, newOpis)
                    onNavigateToPacketList()
                }
                if (selectedFile != null){
                    viewModel.updateImage1(packetId)
                }
                else{
                imageUri?.let {
                    viewModel.updateImage(packetId)
                }
            }
                onNavigateToPacketList()},
                colors = ButtonDefaults.buttonColors(containerColor = PlavaMain)
            ) {
                Text("Save Changes", fontFamily = fontInria)
            }

            // Back button
            Button(onClick = { onNavigateToPacketList() }, colors = ButtonDefaults.buttonColors(containerColor = PlavaMain)) {
                Text("Back", fontFamily = fontInria)
            }
        }
    } ?: run {
        //Toast.makeText(context, "Error loading packet", Toast.LENGTH_SHORT).show()
        Log.w("RADI", "VALJDA")
    }
}


fun pickImageFromGallery(launcher: ActivityResultLauncher<String>) {
    launcher.launch("image/*")
}

fun takePicture(launcher: ActivityResultLauncher<Uri>, imageUri: Uri) {
    launcher.launch(imageUri)
}

fun createImageFile(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
}