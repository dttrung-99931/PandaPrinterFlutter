package com.example.panda_print_plugin.models

enum class PrinterManagerStatusCode {
    DISCOVERING,
    DISCOVER_COMPLETE,
    CONNECTING,
    CONNECTED,
}

class PrinterManagerStatus(val code: PrinterManagerStatusCode, val message: String? = null){
    companion object {
        val DISCOVERING = PrinterManagerStatus(PrinterManagerStatusCode.DISCOVERING)
        val DISCOVER_COMPLETE = PrinterManagerStatus(PrinterManagerStatusCode.DISCOVER_COMPLETE)
    }
}
