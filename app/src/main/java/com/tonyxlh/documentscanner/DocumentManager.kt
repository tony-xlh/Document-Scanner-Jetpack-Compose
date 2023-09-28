package com.tonyxlh.documentscanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import java.io.File
import java.lang.StringBuilder
import java.util.Date

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
        if (!documentFolder.exists()) {
            documentFolder.mkdir()
        }
        saveFilesList(documentFolder,doc)
    }

    fun saveFilesList(documentFolder:File,doc: Document){
        val files = File(documentFolder,"files")
        if (files.exists()) {
            files.delete()
        }
        files.createNewFile()
        val sb:StringBuilder = StringBuilder()
        doc.images.forEach {
            sb.append(it)
            sb.append("\n")
        }
        files.writeText(sb.toString())
    }

    fun getFilesList(documentFolder:File):List<String>{
        val files = File(documentFolder,"files")
        if (files.exists()) {
            return files.readLines()
        }else {
            return listOf()
        }
    }

    fun getDocument(date:Long):Document{
        var externalFilesDir = context.getExternalFilesDir("")
        var documentFolder = File(externalFilesDir,"doc-"+date.toString())
        if (documentFolder.exists()) {
            return Document(date, getFilesList(documentFolder))
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

    fun getFirstDocumentImage(date:Long):ImageBitmap{
        return readFileAsImageBitmapByIndex(date,0)
    }

    fun readFileAsImageBitmapByIndex(date: Long, index: Int): ImageBitmap {
        var externalFilesDir = context.getExternalFilesDir("")
        var documentFolder = File(externalFilesDir, "doc-" + date.toString())
        if (documentFolder.exists()) {
            val filesList = getFilesList(documentFolder)
            if (filesList.size>index) {
                val name = filesList.get(index)
                val file = File(documentFolder, name)
                val bytes = file.readBytes()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                return bitmap.asImageBitmap()
            }
        }
        val db = ContextCompat.getDrawable(context, R.drawable.thumbnail)
        return Bitmap.createBitmap(
            db!!.intrinsicWidth, db.intrinsicHeight, Bitmap.Config.ARGB_8888
        ).asImageBitmap()
    }

    fun readFileAsImageBitmapByName(date: Long, name: String): ImageBitmap {
        var externalFilesDir = context.getExternalFilesDir("")
        var documentFolder = File(externalFilesDir, "doc-" + date.toString())
        if (documentFolder.exists()) {
            val file = File(documentFolder,name)
            val bytes = file.readBytes()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            return bitmap.asImageBitmap()
        }
        val db = ContextCompat.getDrawable(context, R.drawable.thumbnail)
        return Bitmap.createBitmap(
            db!!.intrinsicWidth, db.intrinsicHeight, Bitmap.Config.ARGB_8888
        ).asImageBitmap()
    }

    fun saveOneImage(date:Long,image:ByteArray):String{
        var externalFilesDir = context.getExternalFilesDir("")
        var documentFolder = File(externalFilesDir,"doc-"+date.toString())
        if (!documentFolder.exists()) {
            documentFolder.mkdir()
        }
        var imageFile = File(documentFolder, Date().time.toString()+".jpg")
        imageFile.createNewFile()
        imageFile.writeBytes(image)
        return imageFile.name
    }

    fun deleteImage(date:Long,name:String) {
        var externalFilesDir = context.getExternalFilesDir("")
        var documentFolder = File(externalFilesDir,"doc-"+date.toString())
        if (documentFolder.exists()) {
            var imageFile = File(documentFolder, name)
            imageFile.delete()
        }
    }
}