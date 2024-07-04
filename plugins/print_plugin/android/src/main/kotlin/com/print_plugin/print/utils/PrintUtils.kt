package com.print_plugin.print.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import com.print_plugin.PrintPlugin
import com.print_plugin.R
import com.print_plugin.print.PrintFunc
import com.print_plugin.utils.Conts
import kotlinx.coroutines.CompletableDeferred
import net.posprinter.posprinterface.UiExecute
import net.posprinter.utils.DataForSendToPrinterTSC
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

class PrintUtils {
    companion object {
        @JvmStatic
        fun printErrorExecute(delay: Int): UiExecute {
            return object : UiExecute {
                override fun onsucess() {
//              print succeed
                    PrintPlugin.updatePrintStatus(true, delay.toFloat().roundToLong())
                }

                override fun onfailed() {
//              print fail
                    PrintPlugin.updatePrintStatus(false, 0)
                }
            }
        }

        fun sendPrinterData(data: MutableList<ByteArray>): CompletableDeferred<Boolean> {
            var resultCompletableDeferred = CompletableDeferred<Boolean>()
            PrintFunc.binder!!.writeDataByYouself(object : UiExecute {
                override fun onsucess() {
//              print succeed
//                    PrintPlugin.updatePrintStatus(true, delay.toFloat().roundToLong())
                    resultCompletableDeferred.complete(true)
                }

                override fun onfailed() {
//              print fail
//                    PrintPlugin.updatePrintStatus(false, 0)
                    resultCompletableDeferred.complete(false)
                }
            }) {
                val list: MutableList<ByteArray> = data
                list
            }
            return resultCompletableDeferred
        }

        fun checkLocation(activity: Activity?) {
            val manager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoLocation(activity)
            } else {
                PrintPlugin.permissionState(Conts.ENABLE_LOCATION)
            }
        }

        private fun buildAlertMessageNoLocation(activity: Activity?) {
            val builder = AlertDialog.Builder(activity, R.style.AlertDialogTheme)
            builder.setMessage("Do you want to enable location service?").setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    activity?.startActivityForResult(
                        Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS
                        ), Conts.ENABLE_LOCATION
                    )
                }.setNegativeButton("No") { dialog, id -> dialog.cancel() }
            val alert = builder.create()
            alert.show()
        }

        fun getHighest(list: List<Int>): Int {
            var highest: Int = 0;
            for (item in list) {
                if (item > highest) {
                    highest = item;
                }
            }
            return highest;
        }

        //  Use for print japanese
        fun printTextByte(
            x: Int,
            y: Int,
            font: String,
            rotation: Int,
            xMultiplication: Int,
            yMultiplication: Int,
            alignment: Int,
            content: String,
        ): ByteArray {
            val str =
                "TEXT $x,$y,\"$font\",$rotation,$xMultiplication,$yMultiplication,$alignment,\"$content\"\n"
            return str.toByteArray(Charset.forName("utf-8"))
        }

        fun printBarCodeByte(
            x: Int,
            y: Int,
            codeType: String,
            height: Int,
            human: Int,
            rotation: Int,
            narrow: Int,
            wide: Int,
            alignment: Int,
            content: String,
        ): ByteArray {
            val str =
                "BARCODE $x,$y,\"$codeType\",$height,$human,$rotation,$narrow,$wide,$alignment,\"$content\"\n"
            return str.toByteArray(Charset.forName("utf-8"))
        }

        fun printQRCodeByte(
            x: Int,
            y: Int,
            cellWidth: Int = 5,
            content: String,
            showContent: Boolean = false,
        ): List<ByteArray> {
            val data: MutableList<ByteArray> = ArrayList()
            data.add(
                DataForSendToPrinterTSC.qrCode(
                    x,
                    y,
                    "H",
                    cellWidth,
                    "A",
                    0,
                    content,
                )
            )
            if (showContent) {
                val dx: Int = arrayOf(0, 28, 22, 16, 9, 1, 0, 0)[content.length] + 20
                var dy = cellWidth * 22
                data.add(printTextByte(x + dx, y + dy, "MGENP1M.TTF", 0, 8, 8, 0, content))

            }
            return data
        }

        fun formatDate(date: Date?): String {
            if (date == null) return ""
            val format = SimpleDateFormat("yyyy/MM/dd")
            return format.format(date)
        }

        fun formatDayOfWeek(date: Date?): String {
            if (date == null) return ""
            val format = SimpleDateFormat("(E)", Locale("ja", "JP"))
            return format.format(date)
        }

        fun printPart(): MutableList<ByteArray> {
            val list: MutableList<ByteArray> = ArrayList()
            list.add(DataForSendToPrinterTSC.print(1))
            list.add(DataForSendToPrinterTSC.eoj())
//            list.add(DataForSendToPrinterTSC.delay(1000))
            list.add(DataForSendToPrinterTSC.cls())
            return list
        }

        fun setupPrintPart(width: Int, height: Int): MutableList<ByteArray> {
            val list: MutableList<ByteArray> = ArrayList()
            list.add(DataForSendToPrinterTSC.codePage("UTF-8"))
            list.add(DataForSendToPrinterTSC.sizeBydot(width, height))
            list.add(DataForSendToPrinterTSC.offSetBydot(0))
            list.add(DataForSendToPrinterTSC.gapByinch(0.0, 0.0))
            list.add(DataForSendToPrinterTSC.direction(0))
            list.add(DataForSendToPrinterTSC.density(8))
            list.add(DataForSendToPrinterTSC.speed(5.0))
            return list
        }

        fun printLine(x: Int, y: Int, height: Int, width: Int): ByteArray {
            val dataM = ByteArray(0)
            return dataM.plus(DataForSendToPrinterTSC.bar(x, y, width, height))
        }

        fun confirmBox(x: Int, y: Int): MutableList<ByteArray> {
            val data: MutableList<ByteArray> = ArrayList()
            data.add(printTextByte(x = x + 36, y = y + 7, "MGENP1M.TTF", 0, 8, 8, 0, "確認"))
            data.add(printBox(x = x, y = y, width = 115, height = 132, thickness = 2))
            data.add(printLine(x = x, y = y + 34, height = 2, width = 115))
            return data
        }

        private fun printBox(
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            thickness: Int = 1,
        ): ByteArray {
            val dataM = ByteArray(0)
            return dataM.plus(DataForSendToPrinterTSC.box(x, y, x + width, y + height, thickness))
        }
    }

}