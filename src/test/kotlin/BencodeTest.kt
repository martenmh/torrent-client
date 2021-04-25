import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.charset.Charset

class BencodeTest : BencodeDecoder() {

    @Test
    fun `Encode Test`() {
        DEBUG = true
        val str = "d3:cow3:moo4:spam4:eggse"

        val bytes = str.toByteArray(Charset.defaultCharset())
        val decoder = BencodeDecoder()
        val decoded = decoder.decode(bytes)
        val encoder = BencodeEncoder()
        Assertions.assertArrayEquals(encoder.encode(decoded), bytes)

    }

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
        Assertions.assertEquals(
            "d6:lengthi20e4:name10:sample.txt12:piece lengthi65536e6:pieces20:\\\\��R�\\n\" +\n\"��x\\u0005�\\u0004d�� ���7:privatei1ee",
            encodedInfo
        )
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
        val testCase = { key: String, expected: String ->
            Assertions.assertTrue(value.containsKey(key))
            Assertions.assertTrue(value[key] is BencodedString)
            Assertions.assertEquals(expected, value[key]?.value)
        }
        // "publisher" => "bob"
        testCase("publisher", "bob")
        // "publisher-webpage" => "www.example.com"
        testCase("publisher-webpage", "www.example.com")
        // "publisher.location" => "home"
        testCase("publisher.location", "home")

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
        input = "8:announce".toByteArray()
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

    fun `Test too long string Size`() {

    }

    @Test
    fun `Decode Sample Torrent`() {
        val str =
            "d8:announce35:udp://tracker.openbittorrent.com:8013:creation datei1327049827e4:infod6:lengthi20e4:name10:sample.txt12:piece lengthi65536e6:pieces20:\\��R�\n" +
                    "��x\u0005�\u0004d�� ���7:privatei1eee"
        input = str.toByteArray()
    }
}