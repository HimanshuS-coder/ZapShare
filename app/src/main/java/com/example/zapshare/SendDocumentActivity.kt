package com.example.zapshare

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.IOException

class SendDocumentActivity : AppCompatActivity() {

    private var selectedFilePath: String = ""
    //private var selectedFileContent: ByteArray? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { handleSelectedFile(it) }
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_document)
        val selectDocument = findViewById<Button>(R.id.SelectDocument)

        selectDocument.setOnClickListener{
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            getContent.launch(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun handleSelectedFile(uri: android.net.Uri) {
        // Get the file name
        selectedFilePath = uri.path.toString()
        val fileName = selectedFilePath.substring(selectedFilePath.lastIndexOf("/") + 1)


        Log.d("FILE PATH",selectedFilePath)

        // Display file name
        val fileNameTextView: TextView = findViewById(R.id.DisplaySelectedDocument)
        fileNameTextView.text = "Selected File: $fileName"

        // Read file content into byte array
        val inputStream = contentResolver.openInputStream(uri)
        MyHostApduService.selectedFileContent = inputStream?.readBytes()

        Log.d("Selected FIle Content",MyHostApduService.selectedFileContent.toString())
        Log.d("Selected FIle Content Size", MyHostApduService.selectedFileContent?.size.toString())

        // Set the mimeType of the File and convert it to String first then into byte array
        val mimeTypeInString = contentResolver.getType(uri).toString()
        val mimeTypeSubString = mimeTypeInString.substring(mimeTypeInString.lastIndexOf("/")+1)
        val mimeType = mimeTypeSubString.toByteArray(Charsets.UTF_8)

        Log.d("Mime TYpe in Byte Array stored",String(mimeType, Charsets.UTF_8))
        Log.d("Mime TYpe in Byte Array stored size",mimeType.size.toString())

        // Concatenating the File Content and mimetype (at the end)
        MyHostApduService.selectedFileContent =MyHostApduService.selectedFileContent?.let { Utils.ConcatArrays(it,mimeType) }

        Log.d("Selected FIle Content after mimetype",MyHostApduService.selectedFileContent.toString())
        Log.d("Selected FIle Content Size after mimetype", MyHostApduService.selectedFileContent?.size.toString())

        val mimeTypeInByteArray = MyHostApduService.selectedFileContent?.copyOfRange(MyHostApduService.selectedFileContent!!.size-3,MyHostApduService.selectedFileContent!!.size)
        val mim = mimeTypeInByteArray?.let { String(it, Charsets.UTF_8) }
        if (mim != null) {
            Log.d("Received MimeType",mim)
        }


        // Set dataType as "FILE" in my hostapduservice class
        MyHostApduService.dataType = "F"


        //Log.d("FileContent", "File Content: ${selectedFileContent?.joinToString()}")

        // Save the content to a new file
        //saveByteArrayToFile(selectedFileContent, fileName, mimeType, this)


    }

    override fun onDestroy() {
        super.onDestroy()
        MyHostApduService.dataType = "T"
        Log.d("Sab Destroy","DESTROYYYYYYYYYYYYYYYYYYYYYYYYYYYY")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveByteArrayToFile(byteArray: ByteArray?, fileName: String?, mimeType: String?, context: Context) {
        byteArray?.let {
            try {
                // Create a File object for the destination
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

                // Create a new file with the original file name and appropriate extension
                val fileExtension = getFileExtension(mimeType)
                val newFileName = "SampleFile.$fileExtension"
                val file = File(downloadsDir, newFileName)

                // Use MediaStore for saving the file
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, newFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(byteArray)
                    }
                }

                Log.d("SaveFile", "File saved successfully: ${file.absolutePath}")

            } catch (e: IOException) {
                Log.e("SaveFile", "Error saving file: ${e.message}")
            }
        }
    }

    private fun getFileExtension(mimeType: String?): String {
        return when {
            mimeType == null -> "dat" // default to dat if MIME type is unknown
            mimeType.contains("pdf") -> "pdf"
            mimeType.contains("doc") || mimeType.contains("docx") -> "docx"
            mimeType.contains("xls") || mimeType.contains("xlsx") -> "xlsx"
            mimeType.contains("ppt") || mimeType.contains("pptx") -> "pptx"
            mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpg"
            mimeType.contains("png") -> "png"
            mimeType.contains("txt") -> "txt"
            mimeType.contains("html") -> "html"
            mimeType.contains("zip") -> "zip"
            mimeType.contains("mp3") -> "mp3"
            mimeType.contains("mp4") -> "mp4"
            else -> "dat" // default to dat if MIME type is not recognized
        }
    }
}