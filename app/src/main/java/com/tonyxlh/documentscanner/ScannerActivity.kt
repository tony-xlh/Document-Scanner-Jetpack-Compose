package com.tonyxlh.documentscanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tonyxlh.docscan4j.Capabilities
import com.tonyxlh.docscan4j.CapabilitySetup
import com.tonyxlh.docscan4j.DeviceConfiguration
import com.tonyxlh.docscan4j.DynamsoftService
import com.tonyxlh.docscan4j.Scanner
import com.tonyxlh.documentscanner.ui.theme.DocumentScannerTheme


class ScannerActivity : ComponentActivity() {
    var scanConfig:ScanConfig? = null
    val service = DynamsoftService(
        "http://192.168.8.65:18622",
        "t0068MgAAAEm8KzOlKD/AG56RuTf2RSTo4ajLgVpDBfQkmIJYY7yrDj3jbzQpRfQRzGnACr7S1F/7Da6REO20jmF3QR4VDXI="
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var images by mutableStateOf(emptyList<Bitmap>())
        var openDialog = mutableStateOf(false)
        var scanners by mutableStateOf(emptyList<Scanner>())
        setContent {
            LaunchedEffect(key1 = true){
                    Log.d("DM","request start")
                    Thread {
                        try {
                            var newScanners = mutableStateListOf<Scanner>()
                            service.getScanners().forEach {
                                newScanners.add(it)
                            }
                            scanners = newScanners
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
                            Log.d("DM", scanners.size.toString())
                        }catch (e:Exception){
                            Log.d("DM",e.stackTraceToString())
                        }
                    }.start()
            }
            DocumentScannerTheme {

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Button(
                            onClick = {
                                Log.d("DM",scanConfig.toString())
                                val scanned = scan()
                                var newImages = mutableStateListOf<Bitmap>()
                                scanned.forEach {
                                    newImages.add(it)
                                }
                                images.forEach {
                                    newImages.add(it)
                                }
                                images = newImages

                            }
                        ){
                            Text("Scan")
                        }
                        Button(
                            onClick = {
                                openDialog.value = true;
                            }
                        ){
                            Text("Scan Settings")
                        }
                        when {
                            openDialog.value -> {
                                ScannerSettingsDialog({
                                    if (it != null) {
                                        scanConfig = it
                                    }
                                    openDialog.value = false
                                },scanConfig,scanners)
                            }
                        }
                        images.forEach { image ->
                            Image(
                                bitmap = image.asImageBitmap(),
                                contentDescription = ""
                            )
                        }
                    }
                }
            }
        }
    }

    fun scan(): MutableList<Bitmap> {
        var images = mutableListOf<Bitmap>();
        val t = Thread {
            var caps = Capabilities()
            caps.capabilities.add(scanConfig!!.pixelType)
            val jobID = service.createScanJob(scanConfig!!.scanner,scanConfig!!.deviceConfig,caps)
            var image = service.nextDocument(jobID)
            while (image != null) {
                val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
                images.add(bitmap)
                image = service.nextDocument(jobID)
            }
        }
        t.start()
        t.join()
        return images
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerSettingsDialog(onDismissRequest: (scanConfig:ScanConfig?) -> Unit,currentScanConfig:ScanConfig?, scanners:List<Scanner>) {
    var expanded by remember { mutableStateOf(false) }
    var selectedScannerName by remember { mutableStateOf("") }
    var selectedScanner:Scanner? = null
    var deviceConfig:DeviceConfiguration = DeviceConfiguration()
    var pixelType:CapabilitySetup = CapabilitySetup()
    if (currentScanConfig != null){
        selectedScanner = currentScanConfig.scanner
        selectedScannerName = selectedScanner.name
        deviceConfig = currentScanConfig.deviceConfig
        pixelType = currentScanConfig.pixelType
    }
    Dialog(onDismissRequest = { onDismissRequest(null) }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {

                Text(
                    text = "Scanners:",
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = !expanded
                    }
                ) {
                    TextField(
                        value = selectedScannerName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(),
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        scanners.forEach { scanner ->
                            DropdownMenuItem(
                                text = { Text(text = scanner.name) },
                                onClick = {
                                    selectedScanner = scanner
                                    selectedScannerName = scanner.name
                                    expanded = false
                                }
                            )
                        }
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