@file:JvmName("Utils")
@file:JvmMultifileClass

package utils

import java.io.*
import java.lang.StringBuilder

import java.security.NoSuchAlgorithmException

import java.security.MessageDigest
import kotlin.experimental.and


fun urlEncode(input: String): String{
    return java.net.URLEncoder.encode(input, "utf-8")
}

fun sha1hash(input: ByteArray): ByteArray? {
    val digest = MessageDigest.getInstance("SHA-1")
    return digest.digest(input)
}

inline fun ByteArray.consumeWhile(predicate: (Char) -> Boolean): Pair<ByteArray, ByteArray> {
    for (index in this.indices) {
        if (!predicate(this[index].toChar())) {
            val consumed: ByteArray = this.copyOfRange(0, index)
            val rest: ByteArray = this.copyOfRange(index, this.size)
            return Pair(consumed, rest)
        }
    }
    return Pair(ByteArray(0), ByteArray(0))
}

fun ByteArray.indexOf(arr: ByteArray): Int {
    if(arr.isEmpty()) return -1
    if(arr.size == 1) return indexOf(arr[0])

    outer@
    for(i in 0 until size){
        if(get(i) == arr[0]){
            for(j in 1 until arr.size){
                if(get(i + j) != arr[j]) continue@outer
            }
            return i
        }
    }
    return -1
}
inline fun String.consumeWhile(predicate: (Char) -> Boolean): Pair<String, String> {
    for (index in this.indices) {
        if (!predicate(this[index])) {
            val consumed = take(index)
            val rest = drop(index)
            return Pair(consumed, rest)
        }
    }
    return Pair("", "")
}
fun ByteArray.drop(n: Int): ByteArray {
    return this.copyOfRange(n, this.size)
}

@Throws(java.lang.Exception::class)
fun encodeURL(hexString: String?): String? {
    if (hexString == null || hexString.isEmpty) {
        return ""
    }
    if (hexString.length % 2 != 0) {
        throw java.lang.Exception("String is not hex, length NOT divisible by 2: $hexString")
    }
    val len = hexString.length
    val output = CharArray(len + len / 2)
    var i = 0
    var j = 0
    while (i < len) {
        output[j++] = '%'
        output[j++] = hexString[i++]
        output[j++] = hexString[i++]
    }
    return String(output)
}

fun readContentIntoByteArray(file: File): ByteArray? {
    var fileInputStream: FileInputStream? = null
    val bFile = ByteArray(file.length().toInt())
    try {
        //convert file into array of bytes
        fileInputStream = FileInputStream(file)
        fileInputStream.read(bFile)
        fileInputStream.close()

    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return bFile
}

@Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
fun sha1Hex2(input: ByteArray): ByteArray {
    val sha1 = MessageDigest.getInstance("SHA1")
    return sha1.digest(input)
}


@Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
fun sha1Hex(input: ByteArray): String? {
    val sha1 = MessageDigest.getInstance("SHA1")
    sha1.update(input, 0, input.size - 1)
    val digest = sha1.digest()
    return digest.toHexString()
}


fun ByteArray.toHexString(): String {
    return this.joinToString("") {
        java.lang.String.format("%02x", it)
    }
}

fun sha1Hash(toHash: ByteArray): ByteArray? {
    val sha1 = MessageDigest.getInstance("SHA1")
    sha1.update(toHash, 0, toHash.size - 1)

    return sha1.digest()
}


fun getInfoHash(file: File): ByteArray {
    var inputs: InputStream? = null
    try {
        val sha1 = MessageDigest.getInstance("SHA-1")
        inputs = FileInputStream(file)
        val builder = StringBuilder()
        while (!builder.toString().endsWith("4:info")) {
            builder.append(inputs.read().toChar()) // It's ASCII anyway.
        }
        val output = ByteArrayOutputStream()
        var data: Int
        while (inputs.read().also { data = it } > -1) {
            output.write(data)
        }

        sha1.update(output.toByteArray(), 0, output.size() - 1)
        val digest = sha1.digest()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        if (inputs != null) try {
            inputs.close()
        } catch (ignore: IOException) {
        }
    }
    return byteArrayOf()
}

fun unpack(bytes: Int): ByteArray? {
    return byteArrayOf(
        (bytes ushr 24 and 0xff).toByte(),
        (bytes ushr 16 and 0xff).toByte(),
        (bytes ushr 8 and 0xff).toByte(),
        (bytes and 0xff).toByte()
    )
}

fun ByteArray.toInt(): Int {
    if (size != 4)
        throw Exception("The length of the byte array must be at least 4 bytes long.")

    var result = 0
    var shift = 24
    for (byte in this) {
        result = result or (byte.toInt() shl shift)
        shift -= 8
    }
    return result
}

fun Int.toBytes(): ByteArray {
    val bytes = ByteArray(4)
    bytes[3] = (this and 0xFFFF).toByte()
    bytes[2] = ((this ushr 8) and 0xFFFF).toByte()
    bytes[1] = ((this ushr 16) and 0xFFFF).toByte()
    bytes[0] = ((this ushr 24) and 0xFFFF).toByte()
    return bytes
}

