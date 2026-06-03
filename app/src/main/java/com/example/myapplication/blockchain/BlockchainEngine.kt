package com.example.myapplication.blockchain

import java.security.MessageDigest

data class Transaction(
    val senderAddress: String, // Public Key Base64
    val recipientAddress: String,
    val ephemeralPublicKey: String,
    val ciphertext: String,
    val iv: String,
    val signature: String,
    val timestamp: Long
)

data class Block(
    val index: Int,
    val timestamp: Long,
    val transactions: List<Transaction>,
    val previousHash: String,
    var nonce: Long = 0,
    var hash: String = ""
)

object BlockchainEngine {
    private const val DIFFICULTY = 2
    private val PREFIX = "0".repeat(DIFFICULTY)

    fun calculateHash(block: Block): String {
        val input = "${block.index}${block.timestamp}${block.transactions}${block.previousHash}${block.nonce}"
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun mineBlock(block: Block): Block {
        while (!block.hash.startsWith(PREFIX)) {
            block.nonce++
            block.hash = calculateHash(block)
        }
        return block
    }

    fun isValidBlock(block: Block, previousBlock: Block?): Boolean {
        if (previousBlock != null && block.previousHash != previousBlock.hash) return false
        if (block.hash != calculateHash(block)) return false
        if (!block.hash.startsWith(PREFIX)) return false
        return true
    }

    fun createGenesisBlock(): Block {
        val genesis = Block(0, System.currentTimeMillis(), emptyList(), "0")
        return mineBlock(genesis)
    }
}
