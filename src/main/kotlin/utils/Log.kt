package utils

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


object Log {
    var debug: Boolean = true
    var timestamp: Boolean = true
    private val ANSI_RESET = "\u001B[0m"
    private val ANSI_BLACK = "\u001B[30m"
    private val ANSI_RED = "\u001B[31m"
    private val ANSI_GREEN = "\u001B[32m"
    private val ANSI_YELLOW = "\u001B[33m"
    private val ANSI_BLUE = "\u001B[34m"
    private val ANSI_PURPLE = "\u001B[35m"
    private val ANSI_CYAN = "\u001B[36m"
    private val ANSI_WHITE = "\u001B[37m"

    private fun getTimestamp(): String {
        return DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())
    }

    fun debug(tag: String, str: String) {
        if (debug) {
            log("", tag, str)
        }
    }

    fun info(tag: String, str: String) {
        log("", tag, str)
    }

    private fun log(color: String, tag: String, str: String) {
        val timestampStr = if (timestamp) { "${getTimestamp()} " } else { "" }
        val tagStr = if (tag.isNotEmpty()) { "[${tag}]:\t" } else { "" }
        val resetStr = if (color.isNotEmpty()) { ANSI_RESET } else { "" }
        println(color + timestampStr + tagStr + str + resetStr)
    }

    fun error(tag: String, str: String) {
        log(ANSI_RED, tag, str)
    }

    fun warn(tag: String, str: String) {
        log(ANSI_YELLOW, tag, str)
    }
}
