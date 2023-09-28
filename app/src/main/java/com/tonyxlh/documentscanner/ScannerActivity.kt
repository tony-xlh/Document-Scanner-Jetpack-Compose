package com.tonyxlh.documentscanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tonyxlh.docscan4j.Capabilities
import com.tonyxlh.docscan4j.CapabilitySetup
import com.tonyxlh.docscan4j.DeviceConfiguration
import com.tonyxlh.docscan4j.DynamsoftService
import com.tonyxlh.docscan4j.Scanner
import com.tonyxlh.documentscanner.ui.theme.DocumentScannerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class ScannerActivity : ComponentActivity() {
    var scanConfig:ScanConfig? = null
    val service = DynamsoftService(
        "http://192.168.8.65:18622",
        "t0068MgAAAEm8KzOlKD/AG56RuTf2RSTo4ajLgVpDBfQkmIJYY7yrDj3jbzQpRfQRzGnACr7S1F/7Da6REO20jmF3QR4VDXI="
    )
    var date = Date().time
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var images by mutableStateOf(emptyList<String>())
        var openDialog = mutableStateOf(false)
        var scanners by mutableStateOf(emptyList<Scanner>())
        var selectedImageIndex:Int = -1
        setContent {
            val context = LocalContext.current
            val manager = DocumentManager(context)
            LaunchedEffect(key1 = true){
                Log.d("DM","request start")
                if (intent.hasExtra("date")) {
                    date = intent.getLongExtra("date",date)
                    val thread = Thread() {
                        images = manager.getDocument(date).images
                    }
                    thread.start()
                    thread.join()
                }

                var scannersFound = loadScanners()
                scanners = scannersFound
                if (scanners.size>0) {
                    var pixelType = CapabilitySetup()
                    pixelType.capability=257
                    pixelType.curValue=0
                    pixelType.exception="ignore"
                    scanConfig = ScanConfig(scanners.get(0),
                        DeviceConfiguration(),
                        pixelType
                    )
                }
            }
            DocumentScannerTheme {
                val deleteConfirmationAlertDialog = remember { mutableStateOf(false) }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Row (
                            modifier = Modifier.padding(5.dp),
                        ) {
                            Button(
                                onClick = {
                                    val scope = CoroutineScope(Job() + Dispatchers.IO)
                                    scope.launch {
                                        Log.d("DM",scanConfig.toString())
                                        val scanned = scan(manager)
                                        var newImages = mutableListOf<String>()
                                        images.forEach {
                                            newImages.add(it)
                                        }
                                        scanned.forEach {
                                            newImages.add(it)
                                        }
                                        images = newImages
                                        saveDocument(manager,images)
                                    }
                                }
                            ){
                                Text("Scan")
                            }
                            IconButton(
                                onClick = {
                                    openDialog.value = true;
                                }
                            ){
                                Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            when {
                                openDialog.value -> {
                                    ScannerSettingsDialog({
                                        if (it != null) {
                                            scanConfig = it
                                        }
                                        openDialog.value = false
                                    }, scanConfig, scanners)
                                }
                            }
                        }
                        LazyColumn {
                            items(images.size){
                                Image(
                                    bitmap = manager.readFileAsImageBitmapByName(date,images.get(it)),
                                    contentDescription = "",
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .clickable() {
                                            selectedImageIndex = it
                                            deleteConfirmationAlertDialog.value = true
                                        }
                                )
                            }
                        }
                        when {
                            deleteConfirmationAlertDialog.value -> {
                                ConfirmationAlertDialog(
                                    {
                                        deleteConfirmationAlertDialog.value = false
                                    },
                                    {

                                        deleteConfirmationAlertDialog.value = false
                                        var newImages = mutableStateListOf<String>()
                                        manager.deleteImage(date,images.get(selectedImageIndex))
                                        for (i in 0..images.size-1) {
                                            if (i != selectedImageIndex) {
                                                newImages.add(images.get(i))
                                            }
                                        }
                                        images = newImages
                                        saveDocument(manager,images)
                                    },"Alert","Delete this image?")
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun loadScanners():MutableList<Scanner>{
        var newScanners = mutableListOf<Scanner>()
        withContext(Dispatchers.IO) {
            service.getScanners().forEach {
                newScanners.add(it)
            }
        }
        return newScanners
    }

    suspend fun scan(manager: DocumentManager): MutableList<String> {
        var images = mutableListOf<String>();
        withContext(Dispatchers.IO) {
            var caps = Capabilities()
            caps.capabilities.add(scanConfig!!.pixelType)
            val jobID = service.createScanJob(scanConfig!!.scanner,scanConfig!!.deviceConfig,caps)
            var image = service.nextDocument(jobID)
            while (image != null) {
                val name = manager.saveOneImage(date, image)
                images.add(name)
                image = service.nextDocument(jobID)
            }
        }
        return images
    }

    fun saveDocument(manager: DocumentManager,images:List<String>){
        val thread = Thread(){
            manager.saveDocument(Document(date,images))
        }
        thread.start()
        thread.join()
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerSettingsDialog(onDismissRequest: (scanConfig:ScanConfig?) -> Unit,currentScanConfig:ScanConfig?, scanners:List<Scanner>) {
    var scannedExpanded by remember { mutableStateOf(false) }
    var pixelTypesExpanded by remember { mutableStateOf(false) }
    var selectedScannerName by remember { mutableStateOf("") }
    var selectedPixelType by remember { mutableStateOf("Black & White") }
    var selectedScanner:Scanner? = null
    var deviceConfig:DeviceConfiguration = DeviceConfiguration()
    var pixelType:CapabilitySetup = CapabilitySetup()
    if (currentScanConfig != null){
        selectedScanner = currentScanConfig.scanner
        selectedScannerName = selectedScanner.name
        deviceConfig = currentScanConfig.deviceConfig
        pixelType = currentScanConfig.pixelType
        if (pixelType.curValue == 1){
            selectedPixelType = "Gray"
        }else if (pixelType.curValue == 2) {
            selectedPixelType = "Color"
        }
    }else{
        pixelType.curValue = 0
        pixelType.exception = "ignore"
        pixelType.capability = 257
    }
    Dialog(onDismissRequest = { onDismissRequest(null) }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "Scanners:",
                )
                ExposedDropdownMenuBox(
                    expanded = scannedExpanded,
                    onExpandedChange = {
                        scannedExpanded = !scannedExpanded
                    }
                ) {
                    TextField(
                        value = selectedScannerName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = scannedExpanded) },
                        modifier = Modifier.menuAnchor(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = scannedExpanded,
                        onDismissRequest = { scannedExpanded = false }
                    ) {
                        scanners.forEach { scanner ->
                            DropdownMenuItem(
                                text = { Text(text = scanner.name) },
                                onClick = {
                                    selectedScanner = scanner
                                    selectedScannerName = scanner.name
                                    scannedExpanded = false
                                }
                            )
                        }
                    }
                }
                Text(
                    text = "Pixel Type:",
                )
                ExposedDropdownMenuBox(
                    expanded = pixelTypesExpanded,
                    onExpandedChange = {
                        pixelTypesExpanded = !pixelTypesExpanded
                    }
                ) {
                    TextField(
                        value = selectedPixelType,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pixelTypesExpanded) },
                        modifier = Modifier.menuAnchor(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = pixelTypesExpanded,
                        onDismissRequest = { pixelTypesExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = "Black & White") },
                            onClick = {
                                selectedPixelType = "Black & White"
                                pixelType.curValue = 0
                                pixelTypesExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Gray") },
                            onClick = {
                                selectedPixelType = "Gray"
                                pixelType.curValue = 1
                                pixelTypesExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Color") },
                            onClick = {
                                selectedPixelType = "Color"
                                pixelType.curValue = 2
                                pixelTypesExpanded = false
                            }
                        )
                    }
                }
                Button(onClick = {
                    if (selectedScanner != null) {
                        val scanConfig = ScanConfig(selectedScanner!!,deviceConfig,pixelType)
                        onDismissRequest(scanConfig)
                    }else{
                        onDismissRequest(null)
                    }
                }) {
                    Text("Save")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ConfirmationAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String
) {
    AlertDialog(
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}
