import java.lang.Exception

open class BencodeDecoder() {
    protected lateinit var input: String
//    var output: BencodedData


    /**
     * Consume the first character if it contains the correct type
     */
    protected fun consumeType(typeChar: Char): Boolean {
        if (input.isNotEmpty() && input[0] == typeChar) {
            input = input.drop(1) // remove first character
            return true
        }
        return false
    }
    protected fun consumeEnd() = consumeType('e')


//    public inline fun String.dropWhileGet(predicate: (Char) -> Boolean): String {
//        for (index in this.indices) {
//            if (!predicate(this[index]))
//                this.drop(index)
//                return this
//        }
//        return ""
//    }

    /**
     * Consume a number
     */
    protected fun consumeNumber(): Long {

        when(input[0]){
            '-' -> {
                if(input[1] == '0' || input[1] == '-')
                    throw Exception("Expected valid number after negative sign, got ${input[1]} instead.")
                input = input.drop(1) // consume sign
                return -consumeNumber()
            }
            '0' -> {
                if(input[1].isDigit())
                    throw Exception("Expected end of number after 0, got ${input[1]}.")
                return 0
            }
        }

        input.consumeWhile { it.isDigit() }.also {
            input = it.second
            return it.first.toLong()
        }
    }

    /**
     * An integer starts with a [i] and ends with an [e]
     * * example: i3e represents the integer "3"
     * * example: i-3e represents the integer "-3"
    > i-0e is invalid. All encodings with a leading zero, such as i03e, are invalid, other than i0e, which of course corresponds to the integer "0".
    ---
    > NOTE: The maximum number of bit of this integer is unspecified, but to handle it as a signed 64bit integer is mandatory to handle "large files" aka .torrent for more that 4Gbyte.
     */
    protected fun decodeInteger(): BencodedInt {
        if (consumeType('i')) {
            return BencodedInt(consumeNumber().toInt()).also { consumeEnd() }
        }
        return BencodedInt(0)
    }

    open fun countLines(str: String?): Int {
        if (str == null || str.isEmpty) {
            return 0
        }
        var lines = 1
        var pos = 0
        while (str.indexOf(System.lineSeparator(), pos) + 1.also { pos = it } != 0) {
            lines++
        }
        return lines
    }

    /**
     * * Example: 4: spam represents the string "spam"
     * * Example: 0: represents the empty string ""
     */
    protected fun decodeString(): BencodedString {
        val stringSize = consumeNumber()
        if(input[0] != ':')
            throw Exception("Expected ':' after string size number, got ${input[0]} instead.")
        println("length: ${input.length}, size: $stringSize")
        val result = BencodedString(input.substring(1, stringSize.toInt()+1))
        println("decoded: $result")
        input = input.substring(stringSize.toInt()+1) // consume string
        return result
    }

    /**
     * * Example: l4:spam4:eggse represents the list of two strings: [ "spam", "eggs" ]
     * * Example: le represents an empty list: []
     */
    protected fun decodeList(): BencodedList {
        if (consumeType('l')) {
            // input. // remove first char
            var decodedValue: BencodedData = BencodedInt(0)
            val listValue: MutableList<BencodedData> = mutableListOf()
            while (input.isNotEmpty() && input[0] != 'e') {
                decode(input).also { decodedValue = it }
                println(decodedValue)
                listValue.add(decodedValue)
            }
            consumeEnd()
            return BencodedList(listValue.toList())
        }
        return BencodedList(listOf())
    }

    /**
     * A dictionary starts with a 'd' and ends with an 'e', keys must be strings, the value may be any bencoded type
     * @return the decoded dictionairy containing all data
     * * Example: d3:cow3:moo4:spam4:eggse represents the dictionary { "cow" => "moo", "spam" => "eggs" }
     * * Example: d4:spaml1:a1:bee represents the dictionary { "spam" => [ "a", "b" ] }
     * * Example: d9:publisher3:bob17:publisher-webpage15:www.example.com18:publisher.location4:homee represents { "publisher" => "bob", "publisher-webpage" => "www.example.com", "publisher.location" => "home" }
     * * Example: [de] represents an empty dictionary {}
     */
    protected fun decodeDictionary(): BencodedDictionary {
        if (consumeType('d')) {
            val resultMap: MutableMap<BencodedString, BencodedData> = mutableMapOf()
            while (input[0] != 'e') {
                val name = decodeString()
                val value = decode()
                resultMap[name] = value
            }
            consumeEnd()
            return BencodedDictionary(resultMap.toMap())
        }
        return BencodedDictionary(mapOf())
    }

    protected fun decode(): BencodedData {
        if (input.isNotEmpty()) {
            return when (val start = input[0]) {
                'i' -> decodeInteger()
                'l' -> decodeList()
                'd' -> decodeDictionary()
                in '0'..'9' -> decodeString()
                else -> throw Exception("Unexpected start of input: $start.")
            }
        }
        throw Exception("Error");
    }

    /**
     * Initialization of the decoding process
     * The [input] is the bencoded string that needs to be decoded
     *
     * @return the decoded data
     */
    fun decode(input: String): BencodedData {
        this.input = input
        return decode()
    }
}