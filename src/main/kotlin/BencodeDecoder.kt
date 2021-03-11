class BencodeDecoder() {
//    lateinit var input: String
//    lateinit var it: CharIterator
//    var output: BencodedData

    /**
     * An integer starts with a [i] and ends with an [e]
     * * Example: i3e represents the integer "3"
     * * Example: i-3e represents the integer "-3"
    > i-0e is invalid. All encodings with a leading zero, such as i03e, are invalid, other than i0e, which of course corresponds to the integer "0".
    ---
    > NOTE: The maximum number of bit of this integer is unspecified, but to handle it as a signed 64bit integer is mandatory to handle "large files" aka .torrent for more that 4Gbyte.
     */
    private fun decodeInteger(input: String): BencodedInt {
        if(input.isNotEmpty() && input[0] == 'i') {
            val someThing: ByteArray = byteArrayOf()
            input[curCharOffset.toInt()]
            val value = input.substring(input.indexOf('i') + 1, input.indexOf('e'))
            return BencodedInt(value.toLong())
            return BencodedInt(3)
        }
    }

    /**
     * * Example: 4: spam represents the string "spam"
     * * Example: 0: represents the empty string ""
     */
    private fun decodeString(input: String): BencodedString {
        val stringSize = decodeInteger(input).value.toInt()
        return BencodedString(input.substring(0, stringSize).toByteArray())
    }

    /**
     * * Example: l4:spam4:eggse represents the list of two strings: [ "spam", "eggs" ]
     * * Example: le represents an empty list: []
     */
    private fun decodeList(input: String): BencodedList {
        return BencodedList(listOf())
    }

    /**
     * A dictionary starts with a [d] and ends with an [e], keys must be strings, the value may be any bencoded type
     * * Example: d3:cow3:moo4:spam4:eggse represents the dictionary { "cow" => "moo", "spam" => "eggs" }
     * * Example: d4:spaml1:a1:bee represents the dictionary { "spam" => [ "a", "b" ] }
     * * Example: d9:publisher3:bob17:publisher-webpage15:www.example.com18:publisher.location4:homee represents { "publisher" => "bob", "publisher-webpage" => "www.example.com", "publisher.location" => "home" }
     * * Example: [de] represents an empty dictionary {}
     */
    private fun decodeDictionary(input: String): BencodedDictionary {
        return BencodedDictionary(mapOf())
    }

    fun decode(input: String): BencodedData {
        if (input.isNotEmpty()) {
            when (val start = input[0]) {
                'i' -> return decodeInteger(input)
                'l' -> return decodeList(input)
                'd' -> return decodeDictionary(input)
                in '0'..'9' -> return decodeString(input)
                else -> return BencodedError("Unexpected start of input: $start.")
            }
        }
        return BencodedError("Error");
    }

}