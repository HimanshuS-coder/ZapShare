package com.example.zapshare

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.util.Arrays
import kotlin.math.ceil
import kotlin.math.floor

class MyHostApduService : HostApduService(){




    companion object {

        private const val TAG = "MyHostApduService"
        var dataReceived : String = "Demo Data to send"
        var selectedFileContent: ByteArray? = null
        var dataType : String = "T"

    }


//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//
//        dataReceived = intent?.getStringExtra("dataToSend").toString()
//        Toast.makeText(this,"Received data" + dataReceived, Toast.LENGTH_SHORT).show()
//
//        Log.d("yahoo","wowowowowoaldsjflksajdf kwo")
//        return super.onStartCommand(intent, flags, startId)
//    }


    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        //Log.i(Utils.TAG, "Received APDU: " + commandApdu?.let { Utils.ByteArrayToHexString(it) });

        Log.d(TAG, "apdu: " + commandApdu.let { Utils.ByteArrayToHexString(it) })

        // To check whether COMMAND_APDU received == Our applicatoin ID
        if(Arrays.equals(commandApdu , Utils.SELECT_APD)) {

            // Sending RESPONSE_APDU = 0x9000 as status word in the first two mandatory bytes and REST BYTES EMPTY
            return Utils.SELECT_OK_SW

        // To check whether COMMAND_APDU first two bytes == 0x8001 (Code for SEND_BASIC_DATA_INFORMATION)
        }else if(commandApdu[0] == 0x80.toByte() && commandApdu[1] == 0x01.toByte()) {

            Log.d("what is the 4th byte", Integer.toHexString(commandApdu[3].toInt()))


            val packetInformation: ByteArray = packetInformation(1)
            Log.d("packet information received", packetInformation[1].toString())
            // Sending RESPONSE_APDU = Packet + dataType
            // Before converting the string to Byte you have to convert it into hexadecimal string first and then to Byte
            val dataTypeInByteArray : ByteArray = dataType.toByteArray(Charsets.UTF_8)
            val responseApdu = Utils.ConcatArrays(Utils.ConcatArrays(Utils.SELECT_OK_SW, packetInformation), dataTypeInByteArray)
            return responseApdu

        }else if(commandApdu[0] == 0x70.toByte() && commandApdu[1] == 0x02.toByte()){

            val packet = Utils.ConcatArrays(packetInformation(commandApdu[2].toInt() and 0xFF) , processData(commandApdu[2].toInt() and 0xFF))
            return Utils.ConcatArrays(Utils.SELECT_OK_SW , packet)

        }else{
            return Utils.UNKNOWN_CMD_SW
        }
    }

    fun packetInformation(packetNumber : Int) : ByteArray{

        lateinit var dataToSend : ByteArray
        if(dataType == "T"){
            dataToSend  = dataReceived.toByteArray(Charsets.UTF_8)
        }else{
            dataToSend = selectedFileContent!!
        }
        // DETERMINING TOTAL PACKETS THAT NEEDS TO BE FORWARDED
        //val dataToSend : ByteArray = dataReceived.toByteArray(Charsets.UTF_8)
        Log.d("data to send size", dataToSend.size.toString())
        Log.d("ceil value",ceil(dataToSend.size/255.toDouble()).toInt().toString())
//        Log.d("maxof",maxOf(1,dataToSend.size/255).toString())

        //val totalPackets = ceil(maxOf(1,dataToSend.size/255).toDouble()).toInt()
        val totalPackets = ceil(maxOf(1.0,dataToSend.size/255.toDouble())).toInt()

        Log.d("Packet no", packetNumber.toString())
        Log.d("generating packet information", totalPackets.toString())

        // Returning a Byte Array PACKET_INFORMATION = {CurrentPacketNumber , TotalPackets}
        // Here The integer -> converted to Hexadecimal String -> then parse them as integers -> then to Byte
//        return byteArrayOf(Integer.parseInt(Integer.toString(packetNumber, 16), 16).toByte(),
//            Integer.parseInt(Integer.toString(totalPackets, 16), 16).toByte())
        return byteArrayOf(packetNumber.toByte(),totalPackets.toByte())
    }

    fun processData(packetNumberToSend : Int) : ByteArray {
        Log.d("PROCESS DATA STARTED", "YES")
        lateinit var dataToSend : ByteArray
        if(dataType == "T"){
            dataToSend  = dataReceived.toByteArray(Charsets.UTF_8)
        }else{
            dataToSend = selectedFileContent!!
        }
        //val dataToSend : ByteArray = dataReceived.toByteArray(Charsets.UTF_8)
        val packetSize = 255
        var packetIndex = 0 + ((packetNumberToSend - 1) * 255)

        val packetEnd = minOf(packetIndex + packetSize, dataToSend.size)
        val chunkToSendInByteArray = dataToSend.slice(packetIndex until packetEnd).toByteArray()
        Log.d("PROCESS DATA ENDED", "NO")
        return chunkToSendInByteArray

    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "onDeactivated, reason=" + reason)
    }
}