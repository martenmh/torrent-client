/**
 * A sum type that encompasses all Bencode data types
 */
sealed class BencodedData {
    abstract val value: Any
}

/**
 * A bencode int
 */
data class BencodedInt(
    override val value: Int
) : BencodedData()

data class BencodedString(
    override val value: String
) : BencodedData()

data class BencodedList(
    override val value: List<BencodedData>
) : BencodedData()

data class BencodedDictionary(
    override val value: Map<String, BencodedData>
) : BencodedData()

//fun <T : BencodedData> getListOf(key: String) = (value.get

/** Useful dictionary util functions **/
/**
 * Get a string from the value of the [key]
 */
fun BencodedDictionary.getString(key: String) = (value[key] as BencodedString?)?.value

/**
 *
 */
@Suppress("UNCHECKED_CAST")
fun <T : BencodedData> BencodedDictionary.get(key: String) = (value.get(key) as T?)

inline fun <reified T> BencodedDictionary.getListOf(key: String): List<T>? {
    return (value.get(key) as BencodedList?)?.value?.map {
        if (it.value !is T) throw Exception("Expected each element to be a <T>, got ${it.javaClass}")
        it.value as T
    }
}
