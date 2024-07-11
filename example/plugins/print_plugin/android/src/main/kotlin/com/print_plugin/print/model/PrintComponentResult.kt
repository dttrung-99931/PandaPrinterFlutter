package com.print_plugin.print.model

data class PrintComponentResult(
    val listData: List<ByteArray>,
    val height: Int,
    val currentX: Int = 0,
)