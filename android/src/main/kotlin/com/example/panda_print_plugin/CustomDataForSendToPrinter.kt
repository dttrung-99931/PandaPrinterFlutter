package com.example.panda_print_plugin

import net.posprinter.utils.DataForSendToPrinterTSC
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class CustomDataForSendToPrinter {
    companion object {
        fun textAlign(
            x: Int,
            y: Int,
            font: String,
            rotation: Int,
            x_multiplication: Int,
            y_multiplication: Int,
            content: String,
            alignment: Int = 1, // 1 left, 2 center, 3 right
        ): ByteArray {
            val str =
                "TEXT $x,$y,\"$font\",$rotation,$x_multiplication,$y_multiplication,$alignment,\"$content\"\n"
            return str.toByteArray()
        }

    }

}