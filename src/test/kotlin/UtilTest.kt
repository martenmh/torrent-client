import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.io.UnsupportedEncodingException

import java.security.NoSuchAlgorithmException
import kotlin.experimental.and
import java.math.BigInteger


class UtilTest {
    @Test
    fun `URL encode Test`(){
        val str = "Hello Günter"
        Assertions.assertEquals(URLEncoder.encode(str, StandardCharsets.UTF_8.toString()), urlEncode(str))
        Assertions.assertEquals(URLEncoder.encode(str, StandardCharsets.UTF_8.toString()), "Hello%20G%C3%BCnter")
    }

    @Test
    fun `Test SHA1 Hash`(){
        val value = "Hello World!"
        val res = sha1Hash(value)

        Assertions.assertNotNull(res)
        Assertions.assertEquals("2EF7BDE608CE5404E97D5F042F95F89F1C232871".toLowerCase(), res?.toLowerCase())
    }

    @Test
    fun `Test Consume While`() {
        val (int, rest) = "1245abc".consumeWhile { it.isDigit() }
        Assertions.assertEquals(1234, int.toInt())
    }

    @Test
    fun `Test indexOf`() {
        val sampleTorrent =
            "d8:announce35:udp://tracker.openbittorrent.com:8013:creation datei1327049827e4:infod6:lengthi20e4:name10:sample.txt12:piece lengthi65536e6:pieces20:\\��R�\n" +
                    "��x\u0005�\u0004d�� ���7:privatei1eee"
        val bytes = sampleTorrent.toByteArray()
        var index = bytes.indexOf("announce".toByteArray())
        println(index)
    }
}