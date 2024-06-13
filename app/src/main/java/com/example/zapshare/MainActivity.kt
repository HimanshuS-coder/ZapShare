package com.example.zapshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val senderButton = findViewById<Button>(R.id.SenderButton)
        val readerButton = findViewById<Button>(R.id.ReceiverButton)

        senderButton.setOnClickListener(View.OnClickListener {
            val intent = Intent (this,SendChooser::class.java)
            startActivity(intent)
        })

        readerButton.setOnClickListener(View.OnClickListener {
            val intent = Intent (this,ReaderActivity::class.java)
            startActivity(intent)
        })
    }
}