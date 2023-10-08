package com.tonyxlh.documentscanner

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
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
import com.dynamsoft.ddn.NormalizedImageResultItem
import com.dynamsoft.ddn.NormalizedImagesResult
import com.tonyxlh.documentscanner.ui.theme.DocumentScannerTheme
import java.io.ByteArrayOutputStream


class EditorActivity : ComponentActivity() {
    var bitmap: Bitmap? = null
    lateinit var editorView: ImageEditorView
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val date = intent.getLongExtra("date",0)
        val filename = intent.getStringExtra("filename")
        val manager = DocumentManager(applicationContext)
        bitmap = manager.getOriginalImage(date,filename!!).asAndroidBitmap()
        setContent {
            DocumentScannerTheme {
                LaunchedEffect(key1 = true){
                    detectQuad(bitmap!!)
                }
                Scaffold(
                    bottomBar = {
                        BottomAppBar(
                            actions = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Close the editor",
                                    )
                                }
                                IconButton(onClick = {
                                    setResult(RESULT_OK)
                                    saveNormalizedAndOriginalImage(normalizedImage(), bitmap!!)
                                    finish()
                                }) {
                                    Icon(Icons.Filled.Done, contentDescription = "Complete the editing")
                                }
                            }
                        )
                    },
                ) { innerPadding ->
                    Text(
                        modifier = Modifier.padding(innerPadding),
                        text = "You can adjust the detected polygon."
                    )
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

    private fun normalizedImage():Bitmap{
        val quadDrawingItem = editorView.selectedDrawingItem as QuadDrawingItem
        val router = CaptureVisionRouter(applicationContext);
        val settings = router.getSimplifiedSettings(EnumPresetTemplate.PT_NORMALIZE_DOCUMENT)
        settings.roi = quadDrawingItem.quad
        settings.roiMeasuredInPercentage = false
        router.updateSettings(EnumPresetTemplate.PT_NORMALIZE_DOCUMENT,settings)
        val result = router.capture(bitmap,EnumPresetTemplate.PT_NORMALIZE_DOCUMENT)
        val normalizedResult = result.items[0] as NormalizedImageResultItem
        return normalizedResult.imageData.toBitmap()
    }
    private fun saveNormalizedAndOriginalImage(normalized:Bitmap,original:Bitmap){
        val manager = DocumentManager(applicationContext)
        val date = intent.getLongExtra("date",0)
        val filename = intent.getStringExtra("filename")
        manager.replaceOneImage(date,filename!!,Bitmap2ByteArray(normalized))
        manager.saveOriginalImage(date,filename!!,Bitmap2ByteArray(original))
    }

    private fun Bitmap2ByteArray(image:Bitmap):ByteArray{
        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
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