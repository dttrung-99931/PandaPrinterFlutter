package com.print_plugin.print.components

import com.print_plugin.print.model.Breakdown
import com.print_plugin.print.model.Material
import com.print_plugin.print.model.PrintComponentResult
import com.print_plugin.print.model.Product
import com.print_plugin.print.utils.PrintUtils
import kotlin.math.roundToInt

class PrintComponentBarcode {
    companion object {
        fun printProductItemBarcode(
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
                PrintComponent.printGroups(
                    currentX,
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
                maxWidth * 5 / 11,
                nextLineX = x
            )
            list.addAll(productName.listData)
            var productCodeBarcode = PrintUtils.printBarCodeByte(
                maxWidth * 7 / 11,
                y,
                "39",
                40,
                2,
                0,
                2,
                4,
                2,
                product?.productCode ?: breakdown?.code ?: ""
            )
            list.add(productCodeBarcode)

            var quantity = PrintComponent.printText(
                product?.quantity?.toString() ?: breakdown?.quantity.toString(),
                (maxWidth * 9 / 11),
                y + 25 - fontSize / 2,
                fontSize,
                maxWidth * 10 / 11,
                2
            )
            list.addAll(quantity.listData)

            var quantityBarcode = PrintUtils.printBarCodeByte(
                (maxWidth * 10.5 / 11).roundToInt(),
                y,
                "39",
                40,
                2,
                0,
                2,
                4,
                2,
                product?.quantity?.toString() ?: breakdown?.quantity.toString()
            )
            list.add(quantityBarcode)
            val height = PrintUtils.getHighest(listOf(productName.height, 70, quantity.height))
            return PrintComponentResult(list, height)
        }

        fun printMaterialItemBarcode(
            material: Material,
            x: Int,
            y: Int,
            fontSize: Int,
            maxWidth: Int,
        ): PrintComponentResult {
            val list: MutableList<ByteArray> = ArrayList()
            var currentX: Int = x;

            var productName = PrintComponent.printText(
                material?.name ?: "", currentX, y + 20, fontSize, maxWidth * 5 / 11
            )
            list.addAll(productName.listData)
            var productCodeBarcode = PrintUtils.printBarCodeByte(
                maxWidth * 7 / 11, y, "39", 40, 2, 0, 2, 4, 2, material?.code ?: ""
            )
            list.add(productCodeBarcode)

            var quantity = PrintComponent.printText(
                material?.quantity?.toString() ?: material?.weight?.toString() ?: "-",
                (maxWidth * 9 / 11),
                y + 25 - fontSize / 2,
                fontSize,
                maxWidth * 10 / 11,
                2
            )
            list.addAll(quantity.listData)

            var quantityBarcode = PrintUtils.printBarCodeByte(
                (maxWidth * 10.5 / 11).roundToInt(),
                y,
                "39",
                40,
                2,
                0,
                2,
                4,
                2,
                material?.quantity?.toString() ?: material?.weight?.toString() ?: ""
            )
            list.add(quantityBarcode)
            val height = PrintUtils.getHighest(listOf(productName.height, 70, quantity.height))
            return PrintComponentResult(list, height)
        }
    }
}