package com.tonyxlh.documentscanner

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tonyxlh.docscan4j.DynamsoftService
import com.tonyxlh.documentscanner.ui.theme.DocumentScannerTheme

class ScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LaunchedEffect(key1 = true){
                try {
                    val service = DynamsoftService("http://192.168.8.65:18622","t0068MgAAAEm8KzOlKD/AG56RuTf2RSTo4ajLgVpDBfQkmIJYY7yrDj3jbzQpRfQRzGnACr7S1F/7Da6REO20jmF3QR4VDXI=")
                    val scanners = service.getScanners();
                    Log.d("DM",scanners.size.toString())
                }catch (e:Exception){
                    Log.d("DM",e.stackTraceToString())
                }
            }
            DocumentScannerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DocumentScannerTheme {
        Greeting("Android")
    }
}