package bencode

class UnexpectedByteException(message: String, val input: ByteArray) : Exception(message)
class MissingRequiredStringException(message: String, val data: BencodedData? = null) : Exception(message)
class InvalidNumberException(message: String) : Exception(message)