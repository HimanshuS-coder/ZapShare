package com.example.zapshare

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class SenderActivity : AppCompatActivity() {

    //private var mNfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sender)

        val textToSend = findViewById<EditText>(R.id.TextToSend)
        val sendButton = findViewById<Button>(R.id.SendButton)
        val sizeInBytes = findViewById<TextView>(R.id.SizeInBytes)

        //mNfcAdapter = NfcAdapter.getDefaultAdapter(applicationContext)
        // Don't need NFC adapter in sender class , it is needed in Reader class, as through it we can enable reader mode which disables the host card emulation mode
        // and vice versa

//        if (nfcAdapter == null) {
//            // NFC is not supported on this device
//            // Handle accordingly, e.g., show a message or close the app
//        } else if (!nfcAdapter.isEnabled()) {
//            // NFC is supported, but not enabled
//            // Prompt the user to enable NMainActivityFC
//            startActivity(Intent(Settings.ACTION_NFC_SETTINGS));
//        }
        // Button to send the data to the hostApduService Class
        sendButton.setOnClickListener(View.OnClickListener {
            val dataToSend = textToSend.text.toString()

            if(dataToSend.isNotEmpty()){

//                val intent = Intent(this,MyHostApduService::class.java)
//                intent.putExtra("dataToSend",dataToSend)
//                Toast.makeText(this,"The data is send to commandApdu",Toast.LENGTH_SHORT).show()
//                startService(intent) // call the onStartCommand method in the hostApduService class

                val sizeOfString = "${dataToSend.length} Bytes"
                sizeInBytes.text = sizeOfString
                MyHostApduService.dataReceived = dataToSend

            }else{
                Toast.makeText(this, "Enter data to send", Toast.LENGTH_SHORT).show()
            }
        })

    }
}