package com.tonyxlh.documentscanner

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.dynamsoft.dce.ImageEditorView
import com.tonyxlh.documentscanner.ui.theme.DocumentScannerTheme

class EditorActivity : ComponentActivity() {
    var bitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val date = intent.getLongExtra("date",0)
        val filename = intent.getStringExtra("filename")
        Log.d("DYM",date.toString())
        Log.d("DYM",filename!!)
        val manager = DocumentManager(applicationContext)
        bitmap = manager.readFileAsImageBitmapByName(date,filename!!).asAndroidBitmap()
        setContent {
            DocumentScannerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AndroidView(factory = {context ->
                        val editorView: ImageEditorView
                        editorView = ImageEditorView(context)
                        if (bitmap != null) {
                            editorView.setOriginalImage(bitmap)
                        }
                        editorView
                    })
                }
            }
        }
    }
}
