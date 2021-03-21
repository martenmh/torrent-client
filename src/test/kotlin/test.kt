import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UtilTest {
    fun `Test Consume While`() {
        val (int, rest) = "1245abc".consumeWhile { it.isDigit() }
        Assertions.assertEquals(1234, int.toInt())
    }
}

class BencodeTest : BencodeDecoder() {

    @Test
    fun `Decode Bencode Dictionary`() {
        input = "d3:cow3:moo4:spam4:eggse" // { "cow" => "moo", "spam" => "eggs" }
        val dictionary: BencodedDictionary = decodeDictionary()
        val value = dictionary.value
        // { "cow" => "moo", "spam" => "eggs" }
        Assertions.assertEquals(value.keys.size, 2)
        println("dictionary: $value")

        input = "d4:spaml1:a1:bee"
        // { "spam" => [ "a", "b" ] }
        input = "d9:publisher3:bob17:publisher-webpage15:www.example.com18:publisher.location4:homee"
        // { "publisher" => "bob", "publisher-webpage" => "www.example.com", "publisher.location" => "home" }
        input = "de"
        // {}

        //Assertions.assertEquals(, "cow")
    }

    @Test
    fun `Decode Bencode List`() {
        input = "l4:spam4:eggse" // [ "spam", "eggs" ]
        var decodedData = decodeList()
        Assertions.assertEquals(2, decodedData.value.size)
        Assertions.assertEquals(
            listOf(
                BencodedString("spam"),
                BencodedString("eggs")
            ),
            decodedData.value
        )

        println(decodedData.value.toString())

        input = "le" // []
        decodedData = decodeList()
        println(decodedData.value.toString())

    }

    @Test
    fun `Decode Bencode String`() {
        input = "662:This content hosted at the Internet Archive at https://archive.org/details/tagalogenglishen00niggrich\n" +
                "Files may have changed, which prevents torrents from downloading correctly or completely; please check for an updated torrent at https://archive.org/download/tagalogenglishen00niggrich/tagalogenglishen00niggrich_archive.torrent\n" +
                "Note: retrieval usually requires a client that supports webseeding (GetRight style).\n" +
                "Note: many Internet Archive torrents contain a 'pad file' directory. This directory and the files within it may be erased once retrieval completes.\n" +
                "Note: the file tagalogenglishen00niggrich_meta.xml contains metadata about this torrent's contents."
        var decodedData = decodeString()
        println(decodedData)

        input = "8:announce"
        decodedData = decodeString()
        Assertions.assertEquals(8, decodedData.value.length)
        Assertions.assertEquals("announce", decodedData.value)
    }

    @Test
    fun `Decode Bencode Integer`() {
        input = "i453450e"
        var decodedData = decodeInteger()
        Assertions.assertEquals(453450, decodedData.value)
        input = "i-543e"
        decodedData = decodeInteger()
        Assertions.assertEquals(-543, decodedData.value)
        input = "i0e"
        decodedData = decodeInteger()
        Assertions.assertEquals(0, decodedData.value)

        /** Test invalid cases **/
        try {
            input = "i-0e"
            decodedData = decodeInteger()
            input = "i03e"
            decodedData = decodeInteger()
        } catch (e: Exception) {
            println(e.message)
        }

    }
}