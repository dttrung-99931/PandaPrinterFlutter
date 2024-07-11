package com.print_plugin.print.components

import android.util.Log
import com.print_plugin.print.model.Breakdown
import com.print_plugin.print.model.Material
import com.print_plugin.print.utils.PrintUtils.Companion.getHighest
import com.print_plugin.print.utils.PrintUtils.Companion.printTextByte
import com.print_plugin.print.model.PrintComponentResult
import com.print_plugin.print.model.Process
import com.print_plugin.print.model.Teeth
import com.print_plugin.print.utils.PrintUtils
import com.print_plugin.print.utils.TextUtils
import net.posprinter.utils.DataForSendToPrinterTSC
import kotlin.math.roundToInt

class PrintComponent {
    companion object {
        fun printGroups(
            x: Int,
            y: Int,
            teeth: Teeth? = null,
            callBackX: (Int) -> Unit,
        ): List<ByteArray> {
            if (teeth == null) return listOf()
            var content: String = ""
            var isGroup: Boolean = false
            val dataM: MutableList<ByteArray> = ArrayList();
            var xTmp = x

            if (teeth.topLeft != null) {
                dataM += printGroup(
                    x = xTmp,
                    y = y,
                    content = teeth.topLeft!!,
                    isTopLeft = true,
                ) { result -> xTmp = result }
                xTmp += 15
            }
            if (teeth.topRight != null) {
                dataM += printGroup(
                    x = xTmp,
                    y = y,
                    content = teeth.topRight!!,
                    isTopRight = true,
                ) { result -> xTmp = result }
                xTmp += 15
            }
            if (teeth.bottomLeft != null) {
                dataM += printGroup(
                    x = xTmp,
                    y = y,
                    content = teeth.bottomLeft!!,
                    isBottomLeft = true,
                ) { result -> xTmp = result }
                xTmp += 15
            }
            if (teeth.bottomRight != null) {
                dataM += printGroup(
                    x = xTmp,
                    y = y,
                    content = teeth.bottomRight!!,
                    isBottomRight = true,
                ) { result -> xTmp = result }
                xTmp += 15
            }

            if (xTmp != x) {
                xTmp += 8
            }
            callBackX(xTmp);
            return dataM;
        }


        private fun printGroup(
            x: Int,
            y: Int,
            content: String,
            isTopLeft: Boolean = false,
            isTopRight: Boolean = false,
            isBottomLeft: Boolean = false,
            isBottomRight: Boolean = false,
            callBackX: (Int) -> Unit,
        ): List<ByteArray> {
            var isGroup: Boolean = false
            val dataM: MutableList<ByteArray> = ArrayList();
            var xTmp = x

            if (content.length > 1) {
                isGroup = true
            }
            var left: Boolean = false
            var right: Boolean = false
            var bottom: Boolean = false
            var top: Boolean = false
            var verticalLine: Boolean = false

            for (i in content.indices) {
                verticalLine = false
                if (isTopLeft) {
                    left = false
                    right = true
                    top = false
                    bottom = true
                    if (i == content.length - 1) {
                        verticalLine = true
                    }
                }
                if (isTopRight) {
                    left = true
                    right = false
                    top = false
                    bottom = true
                    if (i == 0) {
                        verticalLine = true
                    }
                }
                if (isBottomLeft) {
                    left = false
                    right = true
                    top = true
                    bottom = false
                    if (i == content.length - 1) {
                        verticalLine = true
                    }
                }
                if (isBottomRight) {
                    left = true
                    right = false
                    top = true
                    bottom = false
                    if (i == 0) {
                        verticalLine = true
                    }
                }
                dataM.addAll(
                    linePositionisGroup(
                        x = xTmp,
                        y = y,
                        content = content[i].toString(),
                        left = left && verticalLine,
                        right = right && verticalLine,
                        top = top,
                        bottom = bottom
                    )
                )
                xTmp += 15
            }

            callBackX(xTmp)
            return dataM
        }

        private fun linePositionisGroup(
            x: Int,
            y: Int,
            content: String,
            left: Boolean? = false,
            right: Boolean? = false,
            top: Boolean? = false,
            bottom: Boolean? = false,
        ): List<ByteArray> {
            val data: MutableList<ByteArray> = ArrayList()

            data.add(
                PrintUtils.printTextByte(
                    x, y, "MGENP1M.TTF", 0, 10, 10, 0, content
                )
            );
            if (left == true) {
                data.add(PrintUtils.printLine(x - 6, y - 6, 34, 2))
            }
            if (right == true) {
                data.add(PrintUtils.printLine(x + 20, y - 6, 34, 2))
            }
            if (top == true) {
                data.add(PrintUtils.printLine(x - 6, y - 6, 2, 28))
            }
            if (bottom == true) {
                data.add(PrintUtils.printLine(x - 7, y + 28, 2, 28))
            }
            return data;
        }

        fun printReverse(
            text: String,
            x: Int,
            y: Int,
            size: Int,
        ): PrintComponentResult {
            val listResult: MutableList<ByteArray> = ArrayList()
            val fontWidth = (2 * (size - 1))
            val fontHeight = (2.3 * (size - 1)).roundToInt()
            var listCharacter = text.trim().split("")
            var textWidth = 0
            val font = "MGENP1M.TTF"
            for (char in listCharacter) {
                val charWidth = if (TextUtils.isFullWidth(char)) fontWidth else (fontWidth / 2)
                textWidth += charWidth
            }
            listResult.add(
                printTextByte(
                    x, y, font, 0, size, size, 0, text
                )
            )
            listResult.add(
                DataForSendToPrinterTSC.reverse(
                    x - 6, y - 6, textWidth + 12, fontHeight + 12
                )
            )
            return PrintComponentResult(listResult, fontHeight + 6, x + textWidth + 6)
        }

        fun printText(
            text: String,
            x: Int,
            y: Int,
            size: Int,
            maxWidth: Int = 0,
            alignment: Int = 0,
            nextLineX: Int? = null,
        ): PrintComponentResult {
            val listResult: MutableList<ByteArray> = ArrayList()
            var listCharacter = text.trim().split("").filter { it != "" }.toMutableList()
            var currentX = x
            var currentY = y
            var currentRow = 0
            val font = "MGENP1M.TTF"
            val fontWidth = (size * 2.7).roundToInt()
            val fontHalfWidth = (fontWidth * 0.52).roundToInt()
            val fontHeight = (size * 2.7).roundToInt()
            val rowHeight = fontHeight + 5
            var widestX = 0
            while (listCharacter.isNotEmpty()) {
                val row: MutableList<String> = ArrayList()
                var rowWidth = 0
                var rowRemainWidth: Int =
                    if (maxWidth > 0 && maxWidth > currentX) (maxWidth - currentX) else 1000
                for (char in listCharacter) {
                    val charWidth =
                        if (TextUtils.isFullWidth(char)) fontWidth else fontHalfWidth
                    if (rowRemainWidth >= charWidth) {
                        rowRemainWidth -= charWidth
                        rowWidth += charWidth
                        row.add(char)
                    } else {
                        break
                    }
                }
                for (char in row) {
                    listCharacter.remove(char)
                }
                var rowString = row.joinToString("")
                listResult.add(
                    printTextByte(
                        currentX, currentY, font, 0, size, size, alignment, rowString
                    )
                )

                if (listCharacter.isNotEmpty()) {
                    currentRow += 1
                    if (nextLineX != null) currentX = nextLineX
                    currentY = y + (rowHeight * currentRow)
                }
                if (widestX < rowWidth) widestX = rowWidth
            }
            return PrintComponentResult(listResult, (currentRow + 1) * rowHeight, x + widestX)
        }

        fun printProcessItem(
            process: Process?,
            y: Int,
            fontSize: Int,
            maxWidth: Int,
        ): PrintComponentResult {
            val list: MutableList<ByteArray> = ArrayList()
//            var currentX: Int = x;

            var name = printText(
                process?.processName ?: "", maxWidth * 7 / 10, y, fontSize - 1, 0, alignment = 3
            )
            list.addAll(name.listData)
            var staffName = printText(
                process?.staffName ?: "", maxWidth, y, fontSize - 1, 0, alignment = 3
            )
            list.addAll(staffName.listData)

            val highest = getHighest(listOf(name.height, staffName.height))
            return PrintComponentResult(list, (highest * 2.2).roundToInt())
        }


    }
}