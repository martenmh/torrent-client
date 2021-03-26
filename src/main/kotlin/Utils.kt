@file:JvmName("Utils")
@file:JvmMultifileClass

import java.io.UnsupportedEncodingException

import java.security.NoSuchAlgorithmException

import java.security.MessageDigest
import kotlin.experimental.and


class MissingRequiredStringException(message: String, val data: BencodedData? = null) : Exception(message)
class InvalidNumberException(message: String) : Exception(message)

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

sealed class Either<out A, out B> {
    class Left<A>(val value: A) : Either<A, Nothing>()
    class Right<B>(val value: B) : Either<Nothing, B>()
}

inline fun <L, R, T> Either<L, R>.fold(left: (L) -> T, right: (R) -> T): T =
    when (this) {
        is Either.Left -> left(value)
        is Either.Right -> right(value)
    }
