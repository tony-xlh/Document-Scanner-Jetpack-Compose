package com.tonyxlh.documentscanner

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.viewinterop.AndroidView
import com.dynamsoft.core.basic_structures.Quadrilateral
import com.dynamsoft.cvr.CaptureVisionRouter
import com.dynamsoft.cvr.EnumPresetTemplate
import com.dynamsoft.dce.DrawingItem
import com.dynamsoft.dce.DrawingLayer
import com.dynamsoft.dce.ImageEditorView
import com.dynamsoft.dce.QuadDrawingItem
import com.dynamsoft.ddn.DetectedQuadResultItem
import com.dynamsoft.ddn.DetectedQuadsResult
import com.tonyxlh.documentscanner.ui.theme.DocumentScannerTheme


class EditorActivity : ComponentActivity() {
    var bitmap: Bitmap? = null
    lateinit var editorView: ImageEditorView
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
                    LaunchedEffect(key1 = true){
                        detectQuad(bitmap!!)
                    }
                    AndroidView(factory = {context ->
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

    private fun detectQuad(bitmap:Bitmap){
        Log.d("DYM","detect")
        val router = CaptureVisionRouter(applicationContext);
        val result = router.capture(bitmap,EnumPresetTemplate.PT_DETECT_DOCUMENT_BOUNDARIES)
        if (result != null) {
            Log.d("DYM","size:"+result.items.size)
            result.items.forEach{
                val quad: DetectedQuadResultItem = it as DetectedQuadResultItem
                addQuadDrawingItem(quad.location)
            }
        }
    }

    private fun addQuadDrawingItem(quad:Quadrilateral){
        val drawingItems = ArrayList<DrawingItem<*>>()
        drawingItems.add(QuadDrawingItem(quad))
        editorView.getDrawingLayer(DrawingLayer.DDN_LAYER_ID).setDrawingItems(drawingItems)
        Log.d("DYM","top:"+quad.boundingRect.top)
        Log.d("DYM","add drawing item")
    }
}
