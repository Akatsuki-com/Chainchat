package com.example.myapplication.network

import android.util.Log
import com.example.myapplication.blockchain.Block
import com.example.myapplication.blockchain.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class P2PManager(private val onBlockReceived: (Block) -> Unit) {
    private var serverSocket: ServerSocket? = null
    private val peers = mutableListOf<String>()
    private var isRunning = false

    fun startServer(port: Int = 9922) {
        isRunning = true
        Thread {
            try {
                serverSocket = ServerSocket(port)
                Log.d("P2P", "Server started on port $port")
                while (isRunning) {
                    val client = serverSocket?.accept() ?: break
                    handleClient(client)
                }
            } catch (e: Exception) {
                Log.e("P2P", "Server error: ${e.message}")
            }
        }.start()
    }

    private fun handleClient(client: Socket) {
        Thread {
            try {
                val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                val message = reader.readLine()
                if (message != null) {
                    val json = JSONObject(message)
                    when (json.getString("type")) {
                        "NEW_BLOCK" -> {
                            val blockJson = json.getJSONObject("block")
                            val block = parseBlock(blockJson)
                            onBlockReceived(block)
                        }
                    }
                }
                client.close()
            } catch (e: Exception) {
                Log.e("P2P", "Client handle error: ${e.message}")
            }
        }.start()
    }

    suspend fun connectAndSend(ip: String, block: Block, port: Int = 9922) = withContext(Dispatchers.IO) {
        try {
            val socket = Socket(ip, port)
            val writer = PrintWriter(socket.getOutputStream(), true)
            val message = JSONObject().apply {
                put("type", "NEW_BLOCK")
                put("block", blockToJson(block))
            }
            writer.println(message.toString())
            socket.close()
            if (!peers.contains(ip)) peers.add(ip)
        } catch (e: Exception) {
            Log.e("P2P", "Connect error to $ip: ${e.message}")
        }
    }

    private fun blockToJson(block: Block): JSONObject {
        return JSONObject().apply {
            put("index", block.index)
            put("timestamp", block.timestamp)
            put("previousHash", block.previousHash)
            put("nonce", block.nonce)
            put("hash", block.hash)
            val txArray = JSONArray()
            block.transactions.forEach { tx ->
                txArray.put(JSONObject().apply {
                    put("senderAddress", tx.senderAddress)
                    put("recipientAddress", tx.recipientAddress)
                    put("ephemeralPublicKey", tx.ephemeralPublicKey)
                    put("ciphertext", tx.ciphertext)
                    put("iv", tx.iv)
                    put("signature", tx.signature)
                    put("timestamp", tx.timestamp)
                })
            }
            put("transactions", txArray)
        }
    }

    private fun parseBlock(json: JSONObject): Block {
        val txArray = json.getJSONArray("transactions")
        val transactions = mutableListOf<Transaction>()
        for (i in 0 until txArray.length()) {
            val txJson = txArray.getJSONObject(i)
            transactions.add(Transaction(
                txJson.getString("senderAddress"),
                txJson.getString("recipientAddress"),
                txJson.getString("ephemeralPublicKey"),
                txJson.getString("ciphertext"),
                txJson.getString("iv"),
                txJson.getString("signature"),
                txJson.getLong("timestamp")
            ))
        }
        return Block(
            json.getInt("index"),
            json.getLong("timestamp"),
            transactions,
            json.getString("previousHash"),
            json.getLong("nonce"),
            json.getString("hash")
        )
    }

    fun stopServer() {
        isRunning = false
        serverSocket?.close()
    }
}
