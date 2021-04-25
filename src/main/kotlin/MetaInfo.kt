import bencode.*
import utils.Either
import utils.fold

/**
 * The meta info contains all useful info read from the .torrent file
 **/
class MetaInfo {
    class Info {
        fun getFileCount(): Int {
            fileInfo.fold(
                { return 1 },
                { return it.files.size }
            )
        }

        data class SingleFileInfo(
            var name: String,  // filename
            var length: Int = 0,        // length of the file in bytes
            var md5sum: String? = null// the (optional) MD5 sum of the file)
        )

        data class FileInfo(
            var length: Int = 0,
            var md5sum: String? = null,
            var path: List<String> = listOf()
        )

        class MultipleFileInfo {
            var name: String
            var files: List<FileInfo>

            constructor(name: String, list: BencodedList) {
                this.name = name
                val fileList: MutableList<FileInfo> = mutableListOf()
                for (bencodedData in list.value) {
                    val dict = bencodedData as BencodedDictionary
                    fileList.add(
                        FileInfo(
                            length = dict.get<BencodedInt>("length")?.value ?: throw MissingRequiredStringException(
                                "length",
                                dict
                            ),
                            md5sum = dict.getString("md5"),
                            path = dict.getListOf<String>("path") ?: throw MissingRequiredStringException("path", dict)
                        )
                    )
                }
                files = fileList.toList()
            }
        }

        var fileInfo: Either<SingleFileInfo, MultipleFileInfo>
        var pieceLength: Int = 0    // number of bytes in each piece
        var pieces: String // concatenation of all 20-byte SHA1 hash values
        var private: Int? = null    // if set to 1 the client must publish its

        constructor(dict: BencodedDictionary?) {
            if (dict == null) throw MissingRequiredStringException("info")

            pieceLength =
                dict.get<BencodedInt>("piece length")?.value ?: throw MissingRequiredStringException(
                    "piece length",
                    dict
                )
            pieces = dict.getString("pieces") ?: throw MissingRequiredStringException("pieces", dict)
            private = dict.get<BencodedInt>("private")?.value

            if (dict.get<BencodedList>("files") != null) {
                fileInfo = Either.Right(
                    MultipleFileInfo(
                        dict.getString("name")!!,
                        dict.get<BencodedList>("files")!!
                    )
                )
            } else {
                fileInfo = Either.Left(
                    SingleFileInfo(
                        dict.getString("name") ?: throw MissingRequiredStringException("name", dict),
                        dict.get<BencodedInt>("length")?.value ?: throw MissingRequiredStringException("length", dict),
                        dict.getString("md5")
                    )
                )
            }

        }
    }

    var info: Info
    var announce: String

    /** Optional metadata **/
    var announceList: List<String>? = null
    var creationDate: Int? = null
    var comment: String? = null
    var createdBy: String? = null
    var encoding: String? = null

    constructor(data: BencodedData) {
        if (data !is BencodedDictionary) throw TypeCastException("Expected BencodedData of type BencodedDictionary, instead got ${data.javaClass}")

        announce = data.getString("announce") ?: throw MissingRequiredStringException("announce", data)

        comment = data.getString("comment")
        createdBy = data.getString("created by")
        encoding = data.getString("encoding")
        announceList = data.getListOf<String>("key")
//        announceList = data.get<BencodedList>("announce-list").value.map {
//            if(it !is BencodedString) throw Exception("Expected each element to be a string, got ${it.javaClass}")
//            it.value as String
//        }

        creationDate = data.get<BencodedInt>("creation date")?.value
        info = Info(data.get<BencodedDictionary>("info"))

    }
}
