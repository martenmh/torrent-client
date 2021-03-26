import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.nio.charset.Charset
import kotlin.test.assertEquals

class BencodeTest : BencodeDecoder() {
    @Test
    fun `Get Bencoded Part`() {
        val sampleTorrent =
            "d8:announce35:udp://tracker.openbittorrent.com:8013:creation datei1327049827e4:infod6:lengthi20e4:name10:sample.txt12:piece lengthi65536e6:pieces20:\\��R�\n" +
                    "��x\u0005�\u0004d�� ���7:privatei1eee"
        val decoder = BencodeDecoder(sampleTorrent.toByteArray())

        // get the value of the encoded "piece length"
        val encodedPieceLength = decoder.getBencodedPart("piece length").toString(Charset.defaultCharset())
        Assertions.assertEquals(encodedPieceLength, "i65536e")

        val encodedInfo = decoder.getBencodedPart("info").toString(Charset.defaultCharset())
        Assertions.assertEquals("d6:lengthi20e4:name10:sample.txt12:piece lengthi65536e6:pieces20:\\\\��R�\\n\" +\n\"��x\\u0005�\\u0004d�� ���7:privatei1ee",
            encodedInfo)
    }

    @Test
    fun `Decode Bencode Dictionary`() {
        var dictionary = BencodedDictionary(mapOf())
        var value = dictionary.value

        val decodeDict = { str: String ->
            input = str.toByteArray()
            dictionary = decodeDictionary()
            dictionary.value
        }

        // { "cow" => "moo", "spam" => "eggs" }
        value = decodeDict("d3:cow3:moo4:spam4:eggse")
        Assertions.assertEquals(value.keys.size, 2)
        // test "cow" => "moo"
        Assertions.assertTrue(value.containsKey("cow"))
        Assertions.assertTrue(value["cow"] is BencodedString)
        Assertions.assertEquals("moo", value["cow"]?.value)
        // test "spam" => "eggs"
        Assertions.assertTrue(value.containsKey("spam"))
        Assertions.assertTrue(value["spam"] is BencodedString)
        Assertions.assertEquals("eggs", value["spam"]?.value)

        // { "spam" => [ "a", "b" ] }
        value = decodeDict("d4:spaml1:a1:be")
        Assertions.assertTrue(value.containsKey("spam"))
        Assertions.assertTrue(value["spam"] is BencodedList)
        Assertions.assertEquals(listOf(BencodedString("a"), BencodedString("b")), value["spam"]?.value)

        // { "publisher" => "bob", "publisher-webpage" => "www.example.com", "publisher.location" => "home" }
        value = decodeDict("d9:publisher3:bob17:publisher-webpage15:www.example.com18:publisher.location4:homee")
        // "publisher" => "bob"
        Assertions.assertTrue(value.containsKey("publisher"))
        Assertions.assertTrue(value["publisher"] is BencodedString)
        Assertions.assertEquals("bob", value["publisher"]?.value)
        // "publisher-webpage" => "www.example.com"
        Assertions.assertTrue(value.containsKey("publisher-webpage"))
        Assertions.assertTrue(value["publisher-webpage"] is BencodedString)
        Assertions.assertEquals("www.example.com", value["publisher"]?.value)
        // "publisher.location" => "home"
        Assertions.assertTrue(value.containsKey("publisher.location"))
        Assertions.assertTrue(value["publisher.location"] is BencodedString)
        Assertions.assertEquals("home", value["publisher"]?.value)

        // {}
        value = decodeDict("de")
        Assertions.assertTrue(value.isEmpty())
    }

    @Test
    fun `Decode Bencode List`() {
        input = "l4:spam4:eggse".toByteArray() // [ "spam", "eggs" ]
        var decodedData = decodeList()
        Assertions.assertEquals(2, decodedData.value.size)
        Assertions.assertEquals(
            listOf(
                BencodedString("spam"),
                BencodedString("eggs")
            ),
            decodedData.value
        )

        input = "le".toByteArray() // []
        decodedData = decodeList()
        Assertions.assertTrue(decodedData.value.isEmpty())
    }

    @Test
    fun `Decode Bencode String`() {
        val input = "8:announce".toByteArray()
        val decodedData = decodeString()
        Assertions.assertEquals(8, decodedData.value.length)
        Assertions.assertEquals("announce", decodedData.value)
    }

    @Test
    fun `Decode Bencode Integer`() {
        input = "i453450e".toByteArray()
        var decodedData = decodeInteger()
        Assertions.assertEquals(453450, decodedData.value)

        input = "i-543e".toByteArray()
        decodedData = decodeInteger()
        Assertions.assertEquals(-543, decodedData.value)

        input = "i0e".toByteArray()
        decodedData = decodeInteger()
        Assertions.assertEquals(0, decodedData.value)

        /** Test invalid cases **/
        try {
            input = "i-0e".toByteArray()
//            Assertions.assertThrows(InvalidNumberException.class, {
//
//            })
            input = "i03e".toByteArray()
            decodedData = decodeInteger()
        } catch (e: Exception) {
            println(e.message)
        }
    }

    @Test
    fun `Decode Sample Torrent`() {
        val str =
            "d8:announce35:udp://tracker.openbittorrent.com:8013:creation datei1327049827e4:infod6:lengthi20e4:name10:sample.txt12:piece lengthi65536e6:pieces20:\\��R�\n" +
                    "��x\u0005�\u0004d�� ���7:privatei1eee"
        input = str.toByteArray()
    }
}