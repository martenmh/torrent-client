package bencode

import java.nio.charset.Charset

open class BencodeEncoder(){

    public fun encode(input: BencodedData): ByteArray {
        return when(input){
            is BencodedDictionary -> encodeDictionary(input)
            is BencodedList -> encodeList(input)
            is BencodedString -> encodeString(input)
            is BencodedInt -> encodeInt(input)
        }
    }

    private fun encodeString(input: BencodedData): ByteArray {
        if(input !is BencodedString){
        }

        val string = input as BencodedString
        return "${string.value.length}:${string.value}".toByteArray(Charset.defaultCharset())
    }

    private fun encodeInt(input: BencodedData): ByteArray {
        if(input !is BencodedInt){
        }

        val integer = input as BencodedInt
        return "i${integer.value}e".toByteArray(Charset.defaultCharset())
    }

    private fun encodeList(input: BencodedData): ByteArray {
        val list = input as BencodedList
        val encodedElements = mutableListOf<ByteArray>("l".toByteArray(Charset.defaultCharset()))
        for (element in list.value) {
            encodedElements.add(encode(element))
        }
        encodedElements.add("e".toByteArray(Charset.defaultCharset()))
        return encodedElements.reduce { a, b ->
            a + b
        }
    }

    private fun encodeDictionary(input: BencodedData): ByteArray {
        val dict = input as BencodedDictionary
        val encodedElements = mutableListOf<ByteArray>("d".toByteArray(Charset.defaultCharset()))
        for(element in dict.value){
            encodedElements.add(encode(BencodedString(element.key)))
            encodedElements.add(encode(element.value))
        }
        encodedElements.add("e".toByteArray(Charset.defaultCharset()))
        return encodedElements.reduce { a, b ->
            a + b
        }
    }
}