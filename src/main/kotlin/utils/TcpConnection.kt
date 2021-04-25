package utils

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

/**
 * A TCP Connection that can serve as both a Client or Server
 *
 */
open class TcpConnection(ip: ByteArray, port: Int, val maxReceiveSize: Int = 1024) {
    protected var socket = Socket()
    protected val buffer = ByteArray(maxReceiveSize)

    init {
        val ipAddr = InetAddress.getByAddress(ip)
        val addr = InetSocketAddress(ipAddr, port)
        socket.connect(addr, 2000)
    }

    public fun endpoint() = socket.inetAddress

    fun send(data: ByteArray) {
        val os = socket.getOutputStream()
        os.write(data)
    }

    fun recvN(n: Int): ByteArray {
        if (n > maxReceiveSize)
            throw Exception("n of value: ${n}, may not be larger than the maxReceiveSize of value: ${maxReceiveSize}. Extend the maxReceiveSize or reduce n.")
        val iss = socket.getInputStream()
        return iss.readNBytes(n)
    }


}
