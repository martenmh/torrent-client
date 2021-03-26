import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class MetaInfoTest {
    lateinit var metaInfo: MetaInfo
    
    /**
     * Test a single file, if we look at https://wiki.theory.org/BitTorrentSpecification#Info_Dictionary
     * We can assert that this is indeed a single file by asserting it does not contain "files"
     */
    @Test
    fun `Meta Info Test Single File`() {
        val singleFileTorrent =
            "d8:announce35:udp://tracker.openbittorrent.com:8013:creation datei1327049827e4:infod6:lengthi20e4:name10:sample.txt12:piece lengthi65536e6:pieces20:\\��R�\n" +
                    "��x\u0005�\u0004d�� ���7:privatei1eee"

        // https://wiki.theory.org/BitTorrentSpecification#Info_Dictionary
        // assert that this is a single file by asserting it does not contain "files"
        Assertions.assertFalse(singleFileTorrent.contains("files"))

        val decoder = BencodeDecoder()
        val decodedTorrent = decoder.decode(singleFileTorrent.toByteArray())
        metaInfo = MetaInfo(decodedTorrent)

        Assertions.assertEquals(1, metaInfo.info.getFileCount())
    }

    @Test
    fun `Meta Info Test Multiple Files`() {
        val multiFileTorrent =
            "d13:creation datei1449730287842e8:encoding5:UTF-84:infod5:filesld6:lengthi1e4:pathl5:1.txteed6:lengthi2e4:pathl5:2.txteed6:lengthi3e4:pathl5:3.txteee4:name7:numbers12:piece lengthi16384e6:pieces20:\u001FtdŽP¦¦pŽÅJ³'¡cÕSk|íee"

        // https://wiki.theory.org/BitTorrentSpecification#Info_Dictionary
        // assert that this is a multi file by asserting it does contain "files"
        Assertions.assertTrue(multiFileTorrent.contains("files"))

        val decoder = BencodeDecoder()
        val decodedTorrent = decoder.decode(multiFileTorrent.toByteArray())
        metaInfo = MetaInfo(decodedTorrent)

        Assertions.assertEquals(3, metaInfo.info.getFileCount())
    }
}