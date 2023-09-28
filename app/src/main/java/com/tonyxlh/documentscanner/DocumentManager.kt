package com.tonyxlh.documentscanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream

class DocumentManager {
    private val context:Context
    constructor(context:Context){
        this.context = context
    }
    fun listDocuments(): MutableList<Long> {
        var externalFilesDir = context.getExternalFilesDir("")
        var documentTimestamps = mutableListOf<Long>()
        if (externalFilesDir != null) {
            externalFilesDir.listFiles().forEach {
                if (it.name.startsWith("doc-")) {
                    val date:Long = it.name.replace("doc-","").toLong()
                    documentTimestamps.add(date)
                }
            }
        }
        return documentTimestamps
    }
    fun saveDocument(doc:Document){
        var externalFilesDir = context.getExternalFilesDir("")
        var documentFolder = File(externalFilesDir,"doc-"+doc.date.toString())
        if (documentFolder.exists()) {
            deleteFilesWithin(documentFolder)
            documentFolder.delete()
        }
        documentFolder.mkdir()
        for (i in 0..doc.images.size-1){
            val imageFile = File(documentFolder,i.toString()+".jpg")
            val bitmap = doc.images.get(i).asAndroidBitmap()
            imageFile.createNewFile()
            try {
                val outputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getDocument(date:Long):Document{
        var externalFilesDir = context.getExternalFilesDir("")
        var documentFolder = File(externalFilesDir,"doc-"+date.toString())
        if (documentFolder.exists()) {
            val images = mutableListOf<ImageBitmap>();
            documentFolder.listFiles().forEach {
                if (it.name.endsWith(".jpg")) {
                    val bytes = it.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    images.add(bitmap.asImageBitmap())
                }
            }
            return Document(date, images)
        }else{
            throw Exception("Not exist")
        }
    }

    fun removeDocument(date:Long){
        var externalFilesDir = context.getExternalFilesDir("")
        var documentFolder = File(externalFilesDir,"doc-"+date.toString())
        if (documentFolder.exists()) {
            deleteFilesWithin(documentFolder)
            documentFolder.delete()
        }
    }

    private fun deleteFilesWithin(folder:File){
        folder.listFiles().forEach {
            it.delete()
        }
    }

    fun getFirstDocumentImage(date:Long):ImageBitmap?{
        var externalFilesDir = context.getExternalFilesDir("")
        var documentFolder = File(externalFilesDir,"doc-"+date.toString())
        if (documentFolder.exists()) {
            documentFolder.listFiles().forEach {
                if (it.name.endsWith(".jpg")) {
                    val bytes = it.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    return bitmap.asImageBitmap()
                }
            }
        }
        return null
    }
}