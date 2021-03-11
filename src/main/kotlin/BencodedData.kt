/**
 * A sum type that encompasses all Bencode data types
 */
sealed class BencodedData

/**
 * A bencode int
 */
data class BencodedInt(
    val value: Long
) : BencodedData()

data class BencodedString(
    val value: ByteArray
) : BencodedData()

data class BencodedList(
    val value: List<BencodedData>
) : BencodedData()

data class BencodedDictionary(
    val value: Map<BencodedString, BencodedData>
) : BencodedData()

data class BencodedError(
    val message: String
) : BencodedData()

abstract class PeekIterator<T> : Iterator<T> {
}