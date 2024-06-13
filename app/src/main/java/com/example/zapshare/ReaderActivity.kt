package com.example.zapshare

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.IOException
import java.util.Arrays

class ReaderActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {


    private lateinit var receivedData : TextView
    private lateinit var packetsReceived : TextView
    private var payload : ByteArray = byteArrayOf()
    private var currentPacketNumber = 1
    private var totalPackets = 1
    lateinit var mimeType : String
    lateinit var dataType : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        receivedData = findViewById<TextView>(R.id.ReceivedData)
        packetsReceived = findViewById<TextView>(R.id.PacketInformation)

        Log.d("Please WORK","Reader mode")
        NfcAdapter.getDefaultAdapter(this)
            .disableReaderMode(this)

        val adapter = NfcAdapter.getDefaultAdapter(this)
        adapter.enableReaderMode(
            this,
            this,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null)

    }

    override fun onResume() {
        super.onResume()
        val adapter = NfcAdapter.getDefaultAdapter(this)
        adapter.enableReaderMode(
            this,
            this,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null)
    }

    override fun onPause() {
        super.onPause()

        Log.d("data finished","done")
        NfcAdapter.getDefaultAdapter(this)
            .disableReaderMode(this)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onTagDiscovered(tag: Tag?) {
        Log.d("FOUND IT FINALLY","TAG FOUND")

        // Resetting the TextView and emptying the payload
        if(currentPacketNumber<2) {
            runOnUiThread {
                receivedData.text = ""
                payload = byteArrayOf()
            }
        }


        val isoDep : IsoDep? = IsoDep.get(tag)

        if(isoDep != null){
            try {

                isoDep.connect()

                //isoDep.setTimeout(3600);

                var selCommand : ByteArray = Utils.SELECT_APD

                // ************************* 1st COMMAND APDU -> SELECT AID whether he matches with the application id or not ************************** //
                var result  = isoDep.transceive(Utils.SELECT_APD)
                // ************************************************************************************************************************************* //

                // Log to verify the result that is received.
                Log.d("Inside 6",Utils.ByteArrayToHexString(result))


                // Created a variable which will always contain the received status
                var status : ByteArray =  byteArrayOf(result[0],result[1])

                // Log to verify the status and check it with the original status
                Log.d("Inside 7",Utils.ByteArrayToHexString(status))
                Log.d("Inside 7",Utils.ByteArrayToHexString(Utils.SELECT_OK_SW))

                // Checking the returned Status whether it is equal to the original status that is 0x9000
                if (Arrays.equals(status,Utils.SELECT_OK_SW)){

                    Log.d("Second response received successfully",Utils.ByteArrayToHexString(status))
                    // Loading the selCommand ByteArray with the command of SEND BASIC DATA INFORMATION
                    selCommand = Utils.SEND_BASIC_DATA_INFORMATION

                    //selCommand[3] = 0x09.toByte()

                    // ********************** 2nd COMMAND APDU -> SEND_BASIC_DATA_INFORMATION asking him to send basic data **************************** //
                    result = isoDep.transceive(selCommand)
                    // ********************************************************************************************************************************* //

                    // Extracting the status from the response apdu , status is of two bytes
                    status =  byteArrayOf(result[0],result[1])

                    Log.d("Inside 8",Utils.ByteArrayToHexString(status))
                    Log.d("Inside 8",result[2].toInt().toString())
                    Log.d("Inside 8",result[3].toInt().toString())

                    if (Arrays.equals(status,Utils.SELECT_OK_SW)){

                        // currentPacketNumber = result[2].toInt()

                        // Retrieving original total packets as byte has a range of -128 to 127
                        totalPackets = result[3].toInt() and 0xFF
                        // Setting what type of data will we be receiving
                        val dataTypeInByte = byteArrayOf(result[4])
                        dataType = String(dataTypeInByte,Charsets.UTF_8)

                        Log.d("data type is :",dataType)

                        // Starting the timer
                        val startTime = System.currentTimeMillis()

                        if (currentPacketNumber<2) {
                            for (i in 1..totalPackets) {
                                currentPacketNumber = i;

                                selCommand = Utils.SEND_DATA
                                selCommand[2] = i.toByte()

                                runOnUiThread {
                                    val setTextToDisplay = "Packets Received : $i / $totalPackets"
                                    packetsReceived.text = setTextToDisplay
                                }

                                result = isoDep.transceive(selCommand)

                                Log.d("Inside 9", result[2].toInt().toString())

                                lateinit var dataPacketReceived : ByteArray

                                if (dataType == "T"){
                                    dataPacketReceived =result.copyOfRange(4, result.size)
                                }else if (dataType == "F"){
                                    if(i == totalPackets){
                                        Log.d("Size of the last packet",result.size.toString())
                                        dataPacketReceived =result.copyOfRange(4, result.size-3)

                                        val mimeTypeInByteArray = result.copyOfRange(result.size-3,result.size)
                                        mimeType = String(mimeTypeInByteArray, Charsets.UTF_8)
                                        Log.d("Received MimeType",mimeType)
                                    }else{
                                        dataPacketReceived =result.copyOfRange(4, result.size)
                                    }
                                }

                                payload = Utils.ConcatArrays(payload, dataPacketReceived)


                            }
                        }else{
                            for (i in currentPacketNumber..totalPackets) {
                                currentPacketNumber = i;

                                selCommand = Utils.SEND_DATA
                                selCommand[2] = i.toByte()

                                runOnUiThread {
                                    val setTextToDisplay = "Packets Received : $i / $totalPackets"
                                    packetsReceived.text = setTextToDisplay
                                }

                                result = isoDep.transceive(selCommand)

                                Log.d("Inside 9", result[2].toInt().toString())

                                lateinit var dataPacketReceived : ByteArray

                                if (dataType == "T"){
                                    dataPacketReceived =result.copyOfRange(4, result.size)
                                }else if (dataType == "F"){
                                    if(i == totalPackets){
                                        Log.d("Size of the last packet",result.size.toString())
                                        dataPacketReceived =result.copyOfRange(4, result.size-3)

                                        val mimeTypeInByteArray = result.copyOfRange(result.size-3,result.size)
                                        mimeType = String(mimeTypeInByteArray, Charsets.UTF_8)
                                        Log.d("Received MimeType",mimeType)
                                    }else{
                                        dataPacketReceived =result.copyOfRange(4, result.size)
                                    }
                                }

                                payload = Utils.ConcatArrays(payload, dataPacketReceived)
                            }
                        }
                        // Capture end time
                        val endTime = System.currentTimeMillis()
                        val elapsedTime = (endTime - startTime)/1000.0

                        runOnUiThread {
                            Toast.makeText(this,"The data is transferred in " + elapsedTime + " sec", Toast.LENGTH_SHORT).show()

                        }
                        if (dataType == "T") {
                            currentPacketNumber = 1
                            setDataToTextView(payload)
                        }else if(dataType == "F"){
                            Log.d("Selected FIle Content",payload.toString())
                            Log.d("Selected FIle Content Size", payload.size.toString())
                            currentPacketNumber = 1
                            mimeType = getMimeType(mimeType)
                            saveByteArrayToFile(payload,mimeType,this)
                        }
                    }

                }else{
                    Log.d("Not Made it","damn")
                }


            }catch (e : IOException) {
                Log.d("error isodep","Not Working")
                Log.e(Utils.TAG, "Error communicating with card: " + e.toString());
            }
        }
    }

    fun setDataToTextView (payload : ByteArray){
        Log.d("Reached Destination","YES")

        runOnUiThread {
            val receivedDataInString = String(payload, Charsets.UTF_8)
            Log.d("payload length","${receivedDataInString.length} Bytes")
            receivedData.text = receivedDataInString
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveByteArrayToFile(byteArray: ByteArray?, miMeType: String?, context: Context) {
        byteArray?.let {
            try {
                // Create a File object for the destination
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

                // Create a new file with the original file name and appropriate extension
                val fileExtension = getFileExtension(miMeType)
                val newFileName = "SampleFile.$fileExtension"
                val file = File(downloadsDir, newFileName)

                // Use MediaStore for saving the file
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, newFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, miMeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(byteArray)
                    }

                    // Open the file using an Intent
                    val openFileIntent = Intent(Intent.ACTION_VIEW)
                    openFileIntent.setDataAndType(uri, miMeType)
                    openFileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(openFileIntent)

                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "The File has been Downloaded and Opened Successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                runOnUiThread {
                    Toast.makeText(this,"The File has been Downloaded Successfully in the Downloads Directory.", Toast.LENGTH_SHORT).show()

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
            mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpeg"
            mimeType.contains("png") -> "png"
            mimeType.contains("txt") -> "txt"
            mimeType.contains("html") -> "html"
            mimeType.contains("zip") -> "zip"
            mimeType.contains("mp3") -> "mp3"
            mimeType.contains("mp4") -> "mp4"
            else -> "dat" // default to dat if MIME type is not recognized
        }
    }

    private fun getMimeType(mimeType: String?): String {
        return when {
            mimeType == null -> "dat" // default to dat if MIME type is unknown
            mimeType.contains("pdf") -> "application/pdf"
            mimeType.contains("doc") || mimeType.contains("docx") -> "application/docx"
            mimeType.contains("xls") || mimeType.contains("xlsx") -> "xlsx"
            mimeType.contains("ppt") || mimeType.contains("pptx") -> "pptx"
            mimeType.contains("jpeg") || mimeType.contains("jpg") -> "image/jpeg"
            mimeType.contains("png") -> "image/png"
            mimeType.contains("txt") -> "txt"
            mimeType.contains("html") -> "html"
            mimeType.contains("zip") -> "zip"
            mimeType.contains("mp3") -> "mp3"
            mimeType.contains("mp4") -> "mp4"
            else -> "dat" // default to dat if MIME type is not recognized
        }
    }

}