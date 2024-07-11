package com.print_plugin.print.utils

import android.util.Log
import com.print_plugin.PrintPlugin
import com.print_plugin.print.components.PrintComponent
import com.print_plugin.print.components.PrintComponent.Companion.printProcessItem
import com.print_plugin.print.PrintFunc
import com.print_plugin.print.components.PrintComponentBarcode.Companion.printMaterialItemBarcode
import com.print_plugin.print.components.PrintComponentBarcode.Companion.printProductItemBarcode
import com.print_plugin.print.components.PrintComponentQR.Companion.printMaterialItemQrcode
import com.print_plugin.print.components.PrintComponentQR.Companion.printProductItemQRCode
import com.print_plugin.print.components.PrintComponentQR.Companion.printQrCode
import com.print_plugin.print.components.PrintComponentQR.Companion.printQrCodeTitle
import com.print_plugin.print.model.PrintOrderParams
import com.print_plugin.print.model.QrCodeLoginParams
import com.print_plugin.print.utils.PrintUtils.Companion.confirmBox
import com.print_plugin.print.utils.PrintUtils.Companion.printPart
import com.print_plugin.print.utils.PrintUtils.Companion.printTextByte
import com.print_plugin.print.utils.PrintUtils.Companion.sendPrinterData
import net.posprinter.utils.DataForSendToPrinterTSC
import kotlin.math.roundToInt

class PrintOrderUtils {
    companion object {

        fun printQrCodeLogin(qrCodeLoginParams: QrCodeLoginParams) {
            PrintFunc.binder!!.writeDataByYouself(PrintUtils.printErrorExecute(3500)) {
                val list: MutableList<ByteArray> = ArrayList()
                val paperWidth = 860
                list.add(DataForSendToPrinterTSC.cls())
                list.add(DataForSendToPrinterTSC.codePage("UTF-8"))
                list.add(DataForSendToPrinterTSC.offSetBydot(0))
                list.add(DataForSendToPrinterTSC.gapByinch(0.0, 0.0))
                list.add(DataForSendToPrinterTSC.direction(0))
                list.add(DataForSendToPrinterTSC.density(8))
                list.add(DataForSendToPrinterTSC.speed(6.0))
                list.add(DataForSendToPrinterTSC.cls())
                list.add(DataForSendToPrinterTSC.backFeed(200))
                list.add(DataForSendToPrinterTSC.eoj())
                list.add(
                    printTextByte(
                        paperWidth / 2,
                        0,
                        "MGENP1M.TTF",
                        0,
                        16,
                        16,
                        2,
                        qrCodeLoginParams.title ?: ""
                    )
                );
                list.add(
                    printTextByte(
                        paperWidth / 2,
                        50,
                        "MGENP1M.TTF",
                        0,
                        16,
                        16,
                        2,
                        qrCodeLoginParams.content ?: ""
                    )
                );
                list.add(printQrCode((paperWidth / 2) - 125, 125, qrCodeLoginParams.data ?: ""));
                list.add(1, DataForSendToPrinterTSC.sizeBydot(850, 500))
                list.addAll(printPart())
                list.add(DataForSendToPrinterTSC.cut())
                list
            }
        }

        fun printLabBoxQR(printOrderParams: PrintOrderParams) {
            Log.d("Print-params", printOrderParams.toString())
            PrintFunc.binder!!.writeDataByYouself(PrintUtils.printErrorExecute(2000)) {
                val list: MutableList<ByteArray> = ArrayList()
                val paperWidth = 860
                var currentY = 0
                val paddingLeft = 40
                val fontSize = 10

                list.add(DataForSendToPrinterTSC.cls())
                list.add(DataForSendToPrinterTSC.codePage("UTF-8"))
                list.add(DataForSendToPrinterTSC.offSetBydot(0))
                list.add(DataForSendToPrinterTSC.gapByinch(0.0, 0.0))
                list.add(DataForSendToPrinterTSC.direction(0))
                list.add(DataForSendToPrinterTSC.density(8))
                list.add(DataForSendToPrinterTSC.speed(6.0))
                list.add(DataForSendToPrinterTSC.cls())
                list.add(DataForSendToPrinterTSC.backFeed(200))
                list.add(DataForSendToPrinterTSC.eoj())

                var data = printQrCodeTitle(
                    paddingLeft, currentY, printOrderParams.data?.clinicCode ?: ""
                )
                list.addAll(data)


                var x: Int = if (printOrderParams?.data?.clinicCode.isNullOrEmpty()) 175 else 327

                var data1 = printQrCodeTitle(
                    x + paddingLeft,
                    currentY,
                    printOrderParams.data?.orderCode ?: "",
                    "指示書番号",
                    6,
                )
                list.addAll(data1)

                currentY = +170
                val clinicName = PrintComponent.printText(
                    printOrderParams.data?.clinicName ?: "",
                    paddingLeft,
                    currentY,
                    fontSize,
                    paperWidth - paddingLeft
                )
                list.addAll(clinicName.listData)
                currentY += clinicName.height + 30

                val products = PrintComponent.printText(
                    printOrderParams.data?.products?.map { it.productName }?.joinToString("/")
                        ?: "", paddingLeft, currentY, fontSize, paperWidth - paddingLeft
                )
                list.addAll(products.listData)
                currentY += products.height

                list.add(1, DataForSendToPrinterTSC.sizeBydot(paperWidth, currentY + 100))
                list.addAll(printPart())
                list.add(DataForSendToPrinterTSC.cut())


                list
            }
        }

        suspend fun printOrderQR(printOrderParams: PrintOrderParams) {
            Log.d("Print-params", printOrderParams.toString())
            val list: MutableList<ByteArray> = ArrayList()
            val paperWidth = 860
            var currentY = 0
            var totalBillHeight = 0
            val paddingLeft = 40
            val fontSize = 10

            list.add(DataForSendToPrinterTSC.cls())
            list.add(DataForSendToPrinterTSC.codePage("UTF-8"))
            list.add(DataForSendToPrinterTSC.offSetBydot(0))
            list.add(DataForSendToPrinterTSC.gapByinch(0.0, 0.0))
            list.add(DataForSendToPrinterTSC.direction(0))
            list.add(DataForSendToPrinterTSC.density(8))
            list.add(DataForSendToPrinterTSC.speed(6.0))
            list.add(DataForSendToPrinterTSC.cls())
            list.add(DataForSendToPrinterTSC.backFeed(180))
            list.add(DataForSendToPrinterTSC.eoj())


            var data = printQrCodeTitle(
                paddingLeft,
                currentY,
                printOrderParams.data?.clinicCode ?: "",
            )
            list.addAll(data)


            var x: Int = if (printOrderParams?.data?.clinicCode.isNullOrEmpty()) 175 else 327

            var data1 = printQrCodeTitle(
                x + paddingLeft,
                currentY,
                printOrderParams.data?.orderCode ?: "",
                "指示書番号",
                6,
            )
            list.addAll(data1)

            currentY = +180
            list.add(1, DataForSendToPrinterTSC.sizeBydot(paperWidth, currentY))
            list.addAll(printPart())
            val result = sendPrinterData(list).await()
            list.clear()
            if (!result) {
                PrintPlugin.updatePrintStatus(false)
                return
            }
            totalBillHeight += currentY
            currentY = 0
//              Clinic name
            val clinicName = PrintComponent.printText(
                printOrderParams.data?.clinicName ?: "",
                paddingLeft,
                currentY,
                fontSize,
                paperWidth - paddingLeft
            )
            currentY += clinicName.height + 20

            val bar = DataForSendToPrinterTSC.bar(0, currentY, 860, 2)

            currentY += 20
            list.add(bar)
            list.add(DataForSendToPrinterTSC.sizeBydot(paperWidth, currentY))
            list.addAll(clinicName.listData)

            list.addAll(printPart())
            val result2 = sendPrinterData(list).await()
            list.clear()
            if (!result2) {
                PrintPlugin.updatePrintStatus(false)
                return
            }

            totalBillHeight += currentY
            currentY = 0


            if (!printOrderParams.newIssued) {
//              Patient name
                val patientNameTitle = PrintComponent.printText(
                    "患者名", paddingLeft, currentY, fontSize, paperWidth / 2
                )
                val setDateTitle = PrintComponent.printText(
                    "セット日", paperWidth / 2, currentY, fontSize, paperWidth - paddingLeft
                )
                currentY += PrintUtils.getHighest(
                    listOf(
                        patientNameTitle.height, setDateTitle.height
                    )
                ) + 10

                val patientName = PrintComponent.printText(
                    printOrderParams.data?.patientName ?: "",
                    paddingLeft,
                    currentY + 20,
                    fontSize,
                    paperWidth / 2 - 20
                )
//                  Set date
                val setDate = PrintComponent.printText(
                    PrintUtils.formatDate(printOrderParams.data?.setDate) + " ",
                    paperWidth / 2,
                    currentY,
                    fontSize * 2,
                    paperWidth - paddingLeft
                )
                val setDateE = PrintComponent.printText(
                    PrintUtils.formatDayOfWeek(
                        printOrderParams.data?.setDate
                    ),
                    setDate.currentX + 10,
                    currentY + fontSize,
                    fontSize,
                    paperWidth - paddingLeft
                )
                currentY += PrintUtils.getHighest(
                    listOf(
                        patientName.height, setDate.height, setDateE.height
                    )
                ) + 10
                currentY += 20
                val bar2 = DataForSendToPrinterTSC.bar(0, currentY, 860, 2)
                currentY += 40

                list.add(DataForSendToPrinterTSC.sizeBydot(paperWidth, currentY))
                list.addAll(patientNameTitle.listData)
                list.addAll(setDateTitle.listData)
                list.addAll(patientName.listData)
                list.addAll(setDate.listData)
                list.addAll(setDateE.listData)
                list.add(bar2)
                list.addAll(printPart())
                val result3 = sendPrinterData(list).await()
                list.clear()
                if (!result3) {
                    PrintPlugin.updatePrintStatus(false)
                    return
                }
                totalBillHeight += currentY
                currentY = 0
            }

//              Product Table Header
            val productNameTitle = PrintComponent.printText(
                "技工物", paddingLeft, currentY, fontSize, ((paperWidth - paddingLeft) * 5 / 11)
            )
            val productCodeTitle = PrintComponent.printText(
                "コード",
                ((paperWidth - paddingLeft) * 8 / 11) + 27,
                currentY,
                fontSize,
                ((paperWidth - paddingLeft) * 10 / 11),
                2
            )
            val productQuantityTitle = PrintComponent.printText(
                "数量",
                ((paperWidth - paddingLeft) * 10 / 11) + 33,
                currentY,
                fontSize,
                paperWidth,
                2
            )
            currentY += PrintUtils.getHighest(
                listOf(
                    productNameTitle.height, productCodeTitle.height, productQuantityTitle.height
                )
            ) + 10
            val bar3 = DataForSendToPrinterTSC.bar(0, currentY, 860, 2)
            currentY += 25

//              Product List
            var listProductItem: MutableList<ByteArray> = ArrayList()
            for (product in printOrderParams.data?.products ?: listOf()) {
                val productItem = printProductItemQRCode(
                    product,
                    null,
                    paddingLeft,
                    currentY,
                    fontSize,
                    paperWidth - paddingLeft,
                )
                listProductItem.addAll(
                    productItem.listData
                )
                currentY += productItem.height

                //breakdown
                if (product.breakdowns.isNotEmpty()) {
                    for (breakdown in product.breakdowns) {
                        val productItem = printProductItemQRCode(
                            null,
                            breakdown,
                            paddingLeft,
                            currentY,
                            fontSize,
                            paperWidth - paddingLeft,
                        )
                        listProductItem.addAll(
                            productItem.listData
                        )
                        currentY += productItem.height
                    }
                }
            }
            list.add(DataForSendToPrinterTSC.sizeBydot(paperWidth, currentY))
            list.addAll(productNameTitle.listData)
            list.addAll(productCodeTitle.listData)
            list.addAll(productQuantityTitle.listData)
            list.add(bar3)
            list.addAll(listProductItem)
            list.addAll(printPart())
            val result4 = sendPrinterData(list).await()
            list.clear()
            if (!result4) {
                PrintPlugin.updatePrintStatus(false)
                return
            }
            totalBillHeight += currentY
            currentY = 0

            val bar4 = DataForSendToPrinterTSC.bar(0, currentY, 860, 2)
            currentY += 60

//              Material Table Header
            val materialNameTitle = PrintComponent.printText(
                "材料", paddingLeft, currentY, fontSize, ((paperWidth - paddingLeft) * 6 / 11)
            )
            val materialCodeTitle = PrintComponent.printText(
                "コード",
                ((paperWidth - paddingLeft) * 8 / 11) + 27,
                currentY,
                fontSize,
                ((paperWidth - paddingLeft) * 10 / 11),
                2
            )
            val materialQuantityTitle = PrintComponent.printText(
                "数量",
                ((paperWidth - paddingLeft) * 10 / 11) + 33,
                currentY,
                fontSize,
                paperWidth,
                2
            )
            currentY += PrintUtils.getHighest(
                listOf(
                    materialNameTitle.height, materialCodeTitle.height, materialQuantityTitle.height
                )
            ) + 10
            val bar5 = DataForSendToPrinterTSC.bar(0, currentY, 860, 2)
            currentY += 25

//              Material list
            var listMaterialItem: MutableList<ByteArray> = ArrayList()
            for (material in printOrderParams.data?.materials ?: listOf()) {
                val materialItem = printMaterialItemQrcode(
                    material,
                    paddingLeft,
                    currentY,
                    fontSize,
                    paperWidth - paddingLeft,
                )
                listMaterialItem.addAll(
                    materialItem.listData
                )
                currentY += materialItem.height
            }

            list.add(DataForSendToPrinterTSC.sizeBydot(paperWidth, currentY))
            list.add(bar4)
            list.addAll(materialNameTitle.listData)
            list.addAll(materialCodeTitle.listData)
            list.addAll(materialQuantityTitle.listData)
            list.add(bar5)
            list.addAll(listMaterialItem)
            list.addAll(printPart())
            val result5 = sendPrinterData(list).await()
            list.clear()
            if (!result5) {
                PrintPlugin.updatePrintStatus(false)
                return
            }
            totalBillHeight += currentY
            currentY = 20

            //List process
            var listProcessItem: MutableList<ByteArray> = ArrayList()
            var processListHeight = 0
            for (process in printOrderParams.data?.processes ?: listOf()) {
                val processItem = printProcessItem(
                    process,
                    currentY + processListHeight,
                    fontSize,
                    paperWidth - paddingLeft,
                )
                listProcessItem.addAll(
                    processItem.listData
                )
                processListHeight += processItem.height
            }
            var confirmBoxY = currentY
            if (processListHeight >= (132 + 40)) {
                currentY = +processListHeight
                confirmBoxY = currentY - (132 + 40)
            } else {
                currentY += 160
            }
            val confirmBox = confirmBox(
                paddingLeft + 5, confirmBoxY
            )
            list.add(DataForSendToPrinterTSC.sizeBydot(paperWidth, currentY + 100))
            list.addAll(listProcessItem)
            list.addAll(confirmBox)
            list.addAll(printPart())
            list.add(DataForSendToPrinterTSC.cut())
            val result6 = sendPrinterData(list).await()
            list.clear()
            if (!result6) {
                PrintPlugin.updatePrintStatus(false)
                return
            }
            totalBillHeight += currentY
            //End of Content
            PrintPlugin.updatePrintStatus(true, (totalBillHeight * 5).toLong())
        }


        fun printLabBoxBarcode(printOrderParams: PrintOrderParams) {
            Log.d("Print-params", printOrderParams.toString())
            PrintFunc.binder!!.writeDataByYouself(PrintUtils.printErrorExecute(2000)) {
                val list: MutableList<ByteArray> = ArrayList()
                val paperWidth = 860
                var currentY = 0
                val paddingLeft = 40
                val fontSize = 10

                list.add(DataForSendToPrinterTSC.cls())
                list.add(DataForSendToPrinterTSC.codePage("UTF-8"))
                list.add(DataForSendToPrinterTSC.offSetBydot(0))
                list.add(DataForSendToPrinterTSC.gapByinch(0.0, 0.0))
                list.add(DataForSendToPrinterTSC.direction(0))
                list.add(DataForSendToPrinterTSC.density(8))
                list.add(DataForSendToPrinterTSC.speed(6.0))
                list.add(DataForSendToPrinterTSC.cls())
                list.add(DataForSendToPrinterTSC.backFeed(200))
                list.add(DataForSendToPrinterTSC.eoj())

                //              Content
                //              Clinic code barcode
                val clinicCodeBarcode = PrintUtils.printBarCodeByte(
                    paperWidth - paddingLeft,
                    currentY,
                    "39",
                    50,
                    2,
                    0,
                    2,
                    6,
                    3,
                    printOrderParams.data?.clinicCode ?: ""
                )
                list.add(clinicCodeBarcode)
                currentY += 120
                // clinic Name
                val clinicName = PrintComponent.printText(
                    printOrderParams.data?.clinicName ?: "",
                    paddingLeft,
                    currentY,
                    fontSize,
                    paperWidth - paddingLeft
                )
                list.addAll(clinicName.listData)
                currentY += clinicName.height + 20
                //Order code
                var orderCodeBarcode = PrintUtils.printBarCodeByte(
                    paperWidth / 2,
                    currentY,
                    "39",
                    60,
                    2,
                    0,
                    2,
                    6,
                    2,
                    printOrderParams.data?.orderCode ?: ""
                )
                currentY += 100

                val products = PrintComponent.printText(
                    printOrderParams.data?.products?.map { it.productName }?.joinToString("/")
                        ?: "", paddingLeft, currentY, fontSize, paperWidth - paddingLeft
                )
                currentY += products.height

                list.add(DataForSendToPrinterTSC.sizeBydot(paperWidth, currentY + 100))
                list.addAll(clinicName.listData)
                list.add(orderCodeBarcode)
                list.addAll(products.listData)
                list.addAll(printPart())
                list.add(DataForSendToPrinterTSC.cut())
                list
            }
        }

        suspend fun printOrderBarcode(printOrderParams: PrintOrderParams) {
            Log.d("Print-params", printOrderParams.toString())
            val list: MutableList<ByteArray> = ArrayList()
            val paperWidth = 860
            var currentY = 0
            var totalBillHeight = 0
            val paddingLeft = 40
            val fontSize = 10

            list.add(DataForSendToPrinterTSC.cls())
            list.add(DataForSendToPrinterTSC.codePage("UTF-8"))
            list.add(DataForSendToPrinterTSC.offSetBydot(0))
            list.add(DataForSendToPrinterTSC.gapByinch(0.0, 0.0))
            list.add(DataForSendToPrinterTSC.direction(0))
            list.add(DataForSendToPrinterTSC.density(8))
            list.add(DataForSendToPrinterTSC.speed(6.0))
            list.add(DataForSendToPrinterTSC.cls())
            list.add(DataForSendToPrinterTSC.backFeed(180))
            list.add(DataForSendToPrinterTSC.eoj())

//              Content
//              Clinic code barcode
            val clinicCodeBarcode = PrintUtils.printBarCodeByte(
                paperWidth - paddingLeft,
                currentY,
                "39",
                50,
                2,
                0,
                2,
                6,
                3,
                printOrderParams.data?.clinicCode ?: ""
            )
            currentY += 120
//              Clinic name
            val clinicName = PrintComponent.printText(
                printOrderParams.data?.clinicName ?: "",
                paddingLeft,
                currentY,
                fontSize,
                paperWidth - paddingLeft
            )
            currentY += clinicName.height + 20
//              Order code
            var orderCodeBarcode = PrintUtils.printBarCodeByte(
                paperWidth / 2,
                currentY,
                "39",
                60,
                2,
                0,
                2,
                6,
                2,
                printOrderParams.data?.orderCode ?: ""
            )
            currentY += 170
            val bar = DataForSendToPrinterTSC.bar(0, currentY, 860, 2)
            currentY += 20

            list.add(DataForSendToPrinterTSC.sizeBydot(paperWidth, currentY))
            list.add(clinicCodeBarcode)
            list.addAll(clinicName.listData)
            list.add(orderCodeBarcode)
            list.add(bar)
            list.addAll(printPart())
            val result1 = sendPrinterData(list).await()
            list.clear()
            if (!result1) {
                PrintPlugin.updatePrintStatus(false)
                return
            }
            totalBillHeight += currentY
            currentY = 0


            if (!printOrderParams.newIssued) {
//              Patient name
                val patientNameTitle = PrintComponent.printText(
                    "患者名", paddingLeft, currentY, fontSize, paperWidth / 2
                )
                val setDateTitle = PrintComponent.printText(
                    "セット日", paperWidth / 2, currentY, fontSize, paperWidth - paddingLeft
                )
                currentY += PrintUtils.getHighest(
                    listOf(
                        patientNameTitle.height, setDateTitle.height
                    )
                ) + 10

                val patientName = PrintComponent.printText(
                    printOrderParams.data?.patientName ?: "",
                    paddingLeft,
                    currentY,
                    fontSize,
                    (paperWidth / 2) - 20
                )
//                  Set date
                val setDate = PrintComponent.printText(
                    PrintUtils.formatDate(printOrderParams.data?.setDate) + " ",
                    paperWidth / 2,
                    currentY,
                    fontSize * 2,
                    paperWidth - paddingLeft
                )
                val setDateE = PrintComponent.printText(
                    PrintUtils.formatDayOfWeek(
                        printOrderParams.data?.setDate
                    ),
                    setDate.currentX + 10,
                    currentY + fontSize,
                    fontSize,
                    paperWidth - paddingLeft
                )
                currentY += PrintUtils.getHighest(
                    listOf(
                        patientName.height, setDate.height, setDateE.height
                    )
                ) + 10
                currentY += 20
                val bar2 = DataForSendToPrinterTSC.bar(0, currentY, 860, 2)
                currentY += 40

                list.add(DataForSendToPrinterTSC.sizeBydot(paperWidth, currentY))
                list.addAll(patientNameTitle.listData)
                list.addAll(setDateTitle.listData)
                list.addAll(patientName.listData)
                list.addAll(setDate.listData)
                list.addAll(setDateE.listData)
                list.add(bar2)
                list.addAll(printPart())
                val result = sendPrinterData(list).await()
                list.clear()
                if (!result) {
                    PrintPlugin.updatePrintStatus(false)
                    return
                }
                totalBillHeight += currentY
                currentY = 0
            }

//            Product Table Header
            val productNameTitle = PrintComponent.printText(
                "技工物", paddingLeft, currentY, fontSize, ((paperWidth - paddingLeft) * 5 / 11)
            )
            val productCodeTitle = PrintComponent.printText(
                "コード",
                ((paperWidth - paddingLeft) * 7 / 11),
                currentY,
                fontSize,
                ((paperWidth - paddingLeft) * 9 / 11),
                2
            )
            val productQuantityTitle = PrintComponent.printText(
                "数量",
                ((paperWidth - paddingLeft) * 9 / 11),
                currentY,
                fontSize,
                ((paperWidth - paddingLeft) * 10 / 11),
                2
            )
            currentY += PrintUtils.getHighest(
                listOf(
                    productNameTitle.height, productCodeTitle.height, productQuantityTitle.height
                )
            ) + 10
            val bar3 = DataForSendToPrinterTSC.bar(0, currentY, 860, 2)
            currentY += 25

//              Product List
            var listProductItem: MutableList<ByteArray> = ArrayList()
            for (product in printOrderParams.data?.products ?: listOf()) {
                val productItem = printProductItemBarcode(
                    product,
                    null,
                    paddingLeft,
                    currentY,
                    fontSize,
                    paperWidth - paddingLeft,
                )
                listProductItem.addAll(
                    productItem.listData
                )
                currentY += productItem.height + 15

                //breakdown
                if (product.breakdowns.isNotEmpty()) {
                    for (breakdown in product.breakdowns) {
                        val productItem = printProductItemBarcode(
                            null,
                            breakdown,
                            paddingLeft,
                            currentY,
                            fontSize,
                            paperWidth - paddingLeft,
                        )
                        listProductItem.addAll(
                            productItem.listData
                        )
                        currentY += productItem.height + 15
                    }
                }
            }
            list.add(DataForSendToPrinterTSC.sizeBydot(paperWidth, currentY))
            list.addAll(productNameTitle.listData)
            list.addAll(productCodeTitle.listData)
            list.addAll(productQuantityTitle.listData)
            list.add(bar3)
            list.addAll(listProductItem)
            list.addAll(printPart())
            val result2 = sendPrinterData(list).await()
            list.clear()
            if (!result2) {
                PrintPlugin.updatePrintStatus(false)
                return
            }
            totalBillHeight += currentY
            currentY = 0

            val bar4 = DataForSendToPrinterTSC.bar(0, currentY, 860, 2)
            currentY += 60

//              Material Table Header
            val materialNameTitle = PrintComponent.printText(
                "材料", paddingLeft, currentY, fontSize, ((paperWidth - paddingLeft) * 6 / 11)
            )
            val materialCodeTitle = PrintComponent.printText(
                "コード",
                ((paperWidth - paddingLeft) * 7 / 11),
                currentY,
                fontSize,
                ((paperWidth - paddingLeft) * 9 / 11),
                2
            )
            val materialQuantityTitle = PrintComponent.printText(
                "数量",
                ((paperWidth - paddingLeft) * 9 / 11),
                currentY,
                fontSize,
                ((paperWidth - paddingLeft) * 10 / 11),
                2
            )
            currentY += PrintUtils.getHighest(
                listOf(
                    materialNameTitle.height, materialCodeTitle.height, materialQuantityTitle.height
                )
            ) + 10
            val bar5 = DataForSendToPrinterTSC.bar(0, currentY, 860, 2)
            currentY += 25

//              Material list
            var listMaterialItem: MutableList<ByteArray> = ArrayList()
            for (material in printOrderParams.data?.materials ?: listOf()) {
                val materialItem = printMaterialItemBarcode(
                    material,
                    paddingLeft,
                    currentY,
                    fontSize,
                    paperWidth - paddingLeft,
                )
                listMaterialItem.addAll(
                    materialItem.listData
                )
                currentY += materialItem.height + 15
            }

            list.add(DataForSendToPrinterTSC.sizeBydot(paperWidth, currentY))
            list.add(bar4)
            list.addAll(materialNameTitle.listData)
            list.addAll(materialCodeTitle.listData)
            list.addAll(materialQuantityTitle.listData)
            list.add(bar5)
            list.addAll(listMaterialItem)
            list.addAll(printPart())

            val result3 = sendPrinterData(list).await()
            list.clear()
            if (!result3) {
                PrintPlugin.updatePrintStatus(false)
                return
            }
            totalBillHeight += currentY
            currentY = 20

            //List process
            var listProcessItem: MutableList<ByteArray> = ArrayList()
            var processListHeight = 0
            for (process in printOrderParams.data?.processes ?: listOf()) {
                val processItem = printProcessItem(
                    process,
                    currentY + processListHeight,
                    fontSize,
                    paperWidth - paddingLeft,
                )
                listProcessItem.addAll(
                    processItem.listData
                )
                processListHeight += processItem.height
            }
            var confirmBoxY = currentY
            if (processListHeight >= (132 + 40)) {
                currentY = +processListHeight
                confirmBoxY = currentY - (132 + 40)
            } else {
                currentY += 160
            }
            val confirmBox = confirmBox(
                paddingLeft + 5, confirmBoxY
            )
            list.add(DataForSendToPrinterTSC.sizeBydot(paperWidth, currentY + 100))
            list.addAll(listProcessItem)
            list.addAll(confirmBox)
            list.addAll(printPart())
            list.add(DataForSendToPrinterTSC.cut())

            val result4 = sendPrinterData(list).await()
            list.clear()
            if (!result4) {
                PrintPlugin.updatePrintStatus(false)
                return
            }
            totalBillHeight += currentY
//            End of Content
            PrintPlugin.updatePrintStatus(true, (totalBillHeight * 5).toLong())
        }
    }
}