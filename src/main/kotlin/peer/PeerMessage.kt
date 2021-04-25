package peer

import utils.toBytes
import utils.toInt

//enum class Message : Byte {
//
//}

class PeerMessage {
    var len: Int = 1;
    var id: Byte = 0;
    var payload: ByteArray = ByteArray(0)

    constructor(len: Int = 1, id: Byte = 0, payload: ByteArray = ByteArray(0)) {
        this.len = len
        this.id = id
        this.payload = payload
    }

    constructor(bytes: ByteArray) {
        if (bytes.size < 4) {
            throw Exception("Expected peer message to be at least 4 bytes, actual is ${bytes.size} bytes.")
        }
        len = bytes.copyOfRange(0, 4).toInt()
        if (len == 0) {
            return
        }

        id = bytes.copyOfRange(4, 5)[0]
        payload = if (len > 1) bytes.copyOfRange(5, len - 1) else ByteArray(0)
    }

    fun toBytes(): ByteArray {
        return len.toBytes() + id + payload
    }
}
