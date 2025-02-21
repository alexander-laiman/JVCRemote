package com.example.jvcremote

import kotlinx.coroutines.*
import java.io.*
import java.net.*


class ProjectorController {
    private var socket: Socket? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun connect(ip: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            socket = Socket(ip, port).apply { soTimeout = 5000 }  // 5-second timeout for handshake
            println("Connected to $ip:$port")

            // Send PJREQ immediately after connecting
            sendRawData("PJREQ", isHex = false)
            println("Sent PJREQ, waiting for PJ_OK...")

            // Read the first response (expecting PJ_OK)
            val firstResponse = readResponse() ?: ""
            if (!firstResponse.contains("PJ_OKPJACK")){
                println("Handshake handled in one packet, connected!")
                return@withContext true
            } else {
                if (!firstResponse.contains("PJ_OK")) {
                    println("Handshake failed: Expected PJ_OK but got $firstResponse")
                    disconnect()
                    return@withContext false
                }
                println("Received PJ_OK, waiting for PJACK...")

                // Read the second response (expecting PJACK)
                val secondResponse = readResponse() ?: ""
                if (secondResponse.contains("PJACK")) {
                    println("Handshake successful.")
                    return@withContext true
                } else {
                    println("Handshake failed: Expected PJACK but got $secondResponse")
                    disconnect()
                    return@withContext false
                }
            }

        } catch (e: SocketTimeoutException) {
            println("Connection timeout.")
            disconnect()
            return@withContext false
        } catch (e: IOException) {
            println("Connection error: ${e.message}")
            disconnect()
            return@withContext false
        }

    }




    private fun sendRawData(data: String, isHex: Boolean = true) {
        val bytes = if (isHex) {
            data.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        } else {
            data.toByteArray(Charsets.UTF_8)
        }
        socket?.getOutputStream()?.write(bytes)
    }
    suspend fun sendOperatingCommand(command: OperatingCommand): Boolean = withContext(Dispatchers.IO) {
        try {
            if (socket == null) {
                println("Not connected to the projector.")
                return@withContext false
            }

            // Format and send operating command: "21 89 01 {command.code} {command.data} 0A"
            val fullCommand = "218901${command.code}${command.data}0A"
            sendRawData(fullCommand)
            println("Sent operating command: ${command.name}")

            // Read and parse response
            val response = readResponse()
            return@withContext response != null && response.isNotEmpty()
        } catch (e: IOException) {
            println("Error sending operating command: ${e.message}")
            return@withContext false
        }
    }
    suspend fun sendRemoteControlCommand(command: RemoteControlCommand): Boolean = withContext(Dispatchers.IO) {
        try {
            if (socket == null) {
                println("Not connected to the projector.")
                return@withContext false
            }

            // Format and send remote control command: "21 89 01 52 43 {command.code} 0A"
            val fullCommand = "2189015243${command.code}0A"
            sendRawData(fullCommand)
            println("Sent remote control command: ${command.name}")

            // Read and parse response
            val response = readResponse()
            return@withContext response != null && response.isNotEmpty()
        } catch (e: IOException) {
            println("Error sending remote control command: ${e.message}")
            return@withContext false
        }
    }

    private fun readResponse(): String? {
        return try {
            val inputStream = socket?.getInputStream() ?: return null
            val buffer = ByteArray(1024) // Buffer to hold data
            val bytesRead = inputStream.read(buffer) // Read into buffer

            if (bytesRead != -1) {
                val response = buffer.copyOf(bytesRead).toString(Charsets.US_ASCII)
                println("Received response: $response")
                parseResponse(response.toByteArray()) // Parse the response
                return response
            } else {
                println("No data received.")
                return null
            }
        } catch (e: IOException) {
            println("Error reading response: ${e.message}")
            null
        }
    }


    private fun parseResponse(response: ByteArray) {
        if (response.isEmpty()) {
            println("Received an empty response.")
            return
        }

        // Convert the response to a hex string for easier debugging
        val hexResponse = response.joinToString("") { "%02X".format(it) }
        println("Received raw response: $hexResponse")

        // Check if the first byte is 0x06 (ACK)
        if (response[0] == 0x06.toByte()) {
            println("ACK: Command accepted.")
        } else {
            println("NAK: Command not acknowledged.")
        }

        // Extract and print Unit ID if available (bytes 1 and 2)
        if (response.size >= 3) {
            val unitId = response.sliceArray(1..2).joinToString("") { "%02X".format(it) }
            println("Unit ID: $unitId")
        }

        // Extract and print Command ID (bytes 3 and 4) if they exist
        if (response.size >= 5) {
            val commandId = response.sliceArray(3..4)
            val commandIdAscii = commandId.toString(Charsets.US_ASCII).trim()
            println("Command ID (ASCII): $commandIdAscii")
        }

        // Check for the end-of-response marker (0x0A - newline)
        if (response.last() == 0x0A.toByte()) {
            println("End of response (newline detected).")
        } else {
            println("Warning: Expected newline at end of response.")
        }
    }


    fun disconnect() {
        try {
            socket?.close()
            println("Connection closed.")
        } catch (e: IOException) {
            println("Error closing connection: ${e.message}")
        } finally {
            socket = null
        }
    }
}
