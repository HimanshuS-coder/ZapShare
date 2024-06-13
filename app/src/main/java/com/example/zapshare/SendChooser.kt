package com.example.zapshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class SendChooser : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_chooser)

        val sendTextButton = findViewById<Button>(R.id.SendTextButton)
        val sendDocumentButton = findViewById<Button>(R.id.SendDocumentButton)

        sendTextButton.setOnClickListener(View.OnClickListener {
            val intent = Intent (this,SenderActivity::class.java)
            startActivity(intent)
        })

        sendDocumentButton.setOnClickListener(View.OnClickListener {
            val intent = Intent (this,SendDocumentActivity::class.java)
            startActivity(intent)
        })
    }
}