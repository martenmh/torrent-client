import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test

import kotlin.reflect.typeOf

class BencodeTest {
    private val parser = BencodeDecoder()

    @Test
    fun `Decode Type`() {
        var decodedTypes = parser.decodeType(
            "d"
        )
        Assertions.assertEquals(decodedTypes[0])
    }

    @Test
    fun `Decode Bencode Dictionary`() {

    }

    @Test
    fun `Decode Bencode List`() {

    }

    @Test
    fun `Decode Bencode String`() {
        var decodedData = parser.decode("8:announce")
    }

    @Test
    fun `Decode Bencode Integer`() {
        var decodedData = parser.decode("i453450e")

        Assertions.assertEquals(decodedData.size, 3)
        Assertions.assertEquals(BencodedInt(453450), decodedData[0])
        Assertions.assertEquals(BencodedInt(-543), decodedData[1])
        Assertions.assertEquals(BencodedInt(0), decodedData[2])

//        try {
//            decodedData = parser.decode(
//                listOf("i03e")
//            )
//        } catch(exception: error){
//
//        }
    }
}