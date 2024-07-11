package com.print_plugin.print.components

import com.print_plugin.print.model.Breakdown
import com.print_plugin.print.model.Material
import com.print_plugin.print.model.PrintComponentResult
import com.print_plugin.print.model.Product
import com.print_plugin.print.utils.PrintUtils
import net.posprinter.utils.DataForSendToPrinterTSC
import java.nio.charset.Charset

class PrintComponentQR {
    companion object {
        fun printQrCodeTitle(
            x: Int,
            y: Int,
            content: String = "",
            title: String = "医院コード",
            cellWidth: Int = 7,
            fontSize: Int = 10,
        ): List<ByteArray> {
            val list: MutableList<ByteArray> = ArrayList()
            if (!content.isNullOrEmpty()) {
                list.add(
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
            }

            list.add(
                PrintUtils.printTextByte(
                    x + (if (!content.isNullOrEmpty()) 155 else 0),
                    y + 45,
                    "MGENP1M.TTF",
                    0,
                    fontSize,
                    fontSize,
                    1,
                    title,
                ),
            )
            list.add(
                PrintUtils.printTextByte(
                    x + 155,
                    y + 80,
                    "MGENP1M.TTF",
                    0,
                    fontSize,
                    fontSize,
                    1,
                    content,
                ),
            )
            return list
        }

        fun printQrCode(x: Int, y: Int, content: String, cellWidth: Int = 7): ByteArray {
            return DataForSendToPrinterTSC.qrCode(
                x,
                y,
                "H",
                cellWidth,
                "A",
                0,
                content,
            )
        }

        fun printProductItemQRCode(
            product: Product?,
            breakdown: Breakdown?,
            x: Int,
            y: Int,
            fontSize: Int,
            maxWidth: Int,
        ): PrintComponentResult {
            val list: MutableList<ByteArray> = ArrayList()
            var currentX: Int = x;

            if (breakdown != null) {
                var breakdownText = PrintComponent.printReverse(
                    "内訳", currentX, y + 20, fontSize
                )
                list.addAll(breakdownText.listData)
                currentX = breakdownText.currentX + 10
            }
            list.addAll(
                PrintComponent.printGroups(currentX,
                    y + 20,
                    product?.teeth ?: breakdown?.teeth,
                    fun(x) {
                        currentX = x
                    })
            )
            var productName = PrintComponent.printText(
                "${product?.productName ?: breakdown?.name ?: ""}${product?.markLabel ?: ""}",
                currentX,
                y + 20,
                fontSize,
                maxWidth * 6 / 11,
                nextLineX = x
            )
            list.addAll(productName.listData)
            var contentProductCode = product?.productCode ?: breakdown?.code ?: ""
            if (contentProductCode.isNotEmpty()) {
                var productCodeQrCode =
                    PrintUtils.printQRCodeByte(x + 530, y - 10, 5, contentProductCode, true)
                list.addAll(productCodeQrCode)
            }


            var quantityContent = product?.quantity?.toString() ?: breakdown?.quantity.toString();
            if (quantityContent.isNotEmpty()) {
                var quantityQrcode =
                    PrintUtils.printQRCodeByte(x + 685, y - 10, 5, quantityContent, true)
                list.addAll(quantityQrcode)
            }


            val height = PrintUtils.getHighest(listOf(productName.height, 150))
            return PrintComponentResult(list, height)
        }

        fun printMaterialItemQrcode(
            material: Material,
            x: Int,
            y: Int,
            fontSize: Int,
            maxWidth: Int,
        ): PrintComponentResult {
            val list: MutableList<ByteArray> = ArrayList()
            var currentX: Int = x;
            var productName = PrintComponent.printText(
                material?.name ?: "", currentX, y + 20, fontSize, maxWidth * 6 / 11
            )
            list.addAll(productName.listData)
            if ((material?.code ?: "").isNotEmpty()) {
                var productCodeQrCode =
                    PrintUtils.printQRCodeByte(x + 530, y - 10, 5, material?.code ?: "", true)

                list.addAll(productCodeQrCode)
            }
            var contentQuantity =
                material?.quantity?.toString() ?: material?.weight?.toString() ?: ""
            if (contentQuantity.isNotEmpty()) {
                var quantityQrcode =
                    PrintUtils.printQRCodeByte(x + 685, y - 10, 5, contentQuantity, true)
                list.addAll(quantityQrcode)
            }

            val height = PrintUtils.getHighest(listOf(productName.height, 150))
            return PrintComponentResult(list, height)
        }
    }
}