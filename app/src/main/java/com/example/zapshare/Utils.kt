package com.example.zapshare

import java.util.Arrays




class Utils {

    // Companion object is used to make the methods static in kotlin
    companion object{

        // TAG Name
        val TAG = "DataShare"

        // AID for this application.
        val OUR_APPLICATION_AID = "F0ABCDEF0000"
        //val OUR_APPLICATION_AID = "A0000000000001"

        // Select apdu that contains first 4 mandatory bytes and then the application aid
        // val SELECT_APDU : ByteArray = BuildSelectApdu(OUR_APPLICATION_AID)
        val SELECT_APD : ByteArray = byteArrayOf(0x00.toByte(),0xA4.toByte(),0x04.toByte(),0x00.toByte(),0x06.toByte(),0xF0.toByte(),0xAB.toByte(),0xCD.toByte(),0xEF.toByte(),0x00.toByte(),0x00.toByte())

        // "OK" status word sent in response to SELECT AID command (0x9000)
        //val SELECT_OK_SW : ByteArray? = HexStringToByteArray("9000")
        val SELECT_OK_SW : ByteArray = byteArrayOf(0x90.toByte(),0x00.toByte())

        // Reader will ask the sender device to send the data
        val SEND_BASIC_DATA_INFORMATION : ByteArray = HexStringToByteArray("80010000")

        val SEND_DATA : ByteArray = HexStringToByteArray("70020000")

        // "UNKNOWN" status word sent in response to invalid APDU command (0x0000)
        val UNKNOWN_CMD_SW = HexStringToByteArray("0000")



        // ISO-DEP command HEADER for selecting an AID.
        // Format: [Class | Instruction | Parameter 1 | Parameter 2]
        private val SELECT_APDU_HEADER = "00A40400"

        fun ByteArrayToHexString(bytes: ByteArray): String {
            val hexArray = charArrayOf(
                '0',
                '1',
                '2',
                '3',
                '4',
                '5',
                '6',
                '7',
                '8',
                '9',
                'A',
                'B',
                'C',
                'D',
                'E',
                'F'
            )
            val hexChars = CharArray(bytes.size * 2)
            var v: Int
            for (j in bytes.indices) {
                v = bytes[j].toInt() and 0xFF
                hexChars[j * 2] = hexArray[v ushr 4]
                hexChars[j * 2 + 1] = hexArray[v and 0x0F]
            }
            return String(hexChars)
        }

        fun HexStringToByteArray(s: String): ByteArray {
            val len = s.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
//                data[i / 2] = ((((s[i].digitToIntOrNull(16) ?: (-1 shl 4)) + s[i + 1].digitToIntOrNull(
//                    16
//                )!!) ?: -1)).toByte()
//                i += 2

                val highNibble = s[i].digitToIntOrNull(16) ?: 0
                val lowNibble = s[i + 1].digitToIntOrNull(16) ?: 0
                data[i / 2] = ((highNibble shl 4) + lowNibble).toByte()
                i += 2
            }
            return data
        }

        /**
         * Build APDU for SELECT AID command. This command indicates which service a reader is
         * interested in communicating with. See ISO 7816-4.
         *
         * @param aid Application ID (AID) to select
         * @return APDU for SELECT AID command
         */
        fun BuildSelectApdu(aid: String): ByteArray {
            // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
            return Utils.HexStringToByteArray(
                SELECT_APDU_HEADER + String.format("%02X",aid.length / 2) + aid
            )
        }

        /**
         * Utility method to concatenate two byte arrays.
         * @param first First array
         * @param rest Any remaining arrays
         * @return Concatenated copy of input arrays
         */
        fun ConcatArrays(first: ByteArray, vararg rest: ByteArray): ByteArray {
            var totalLength = first.size
            for (array in rest) {
                totalLength += array.size
            }
            val result = Arrays.copyOf(first, totalLength)
            var offset = first.size
            for (array in rest) {
                System.arraycopy(array, 0, result, offset, array.size)
                offset += array.size
            }
            return result
        }

    }
}