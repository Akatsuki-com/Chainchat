package com.example.myapplication.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.blockchain.Block
import com.example.myapplication.blockchain.BlockchainEngine
import com.example.myapplication.blockchain.Transaction
import com.example.myapplication.crypto.CryptoManager
import com.example.myapplication.network.P2PManager
import kotlinx.coroutines.launch
import java.security.KeyPair
import javax.crypto.spec.SecretKeySpec

class ChatViewModel : ViewModel() {
    val blockchain = mutableStateListOf<Block>()
    val messages = mutableStateListOf<DisplayMessage>()
    val isMining = mutableStateOf(false)
    
    private var myKeyPair: KeyPair = CryptoManager.generateKeyPair()
    val myAddress = CryptoManager.toBase64(myKeyPair.public.encoded)
    
    private val p2pManager = P2PManager { newBlock ->
        onNewBlockReceived(newBlock)
    }

    init {
        blockchain.add(BlockchainEngine.createGenesisBlock())
        p2pManager.startServer()
    }

    data class DisplayMessage(val sender: String, val text: String, val isMe: Boolean)

    fun sendMessage(recipientAddress: String, text: String, peerIp: String?) {
        viewModelScope.launch {
            isMining.value = true
            
            // For E2EE, we'd normally derive a shared key. 
            // Simplified here: encrypt with a dummy shared key if recipient is known
            // or just demonstrate the structure.
            val ephemeralKeyPair = CryptoManager.generateKeyPair()
            val (ciphertext, iv) = CryptoManager.encrypt(text, SecretKeySpec(ByteArray(32), "AES")) // Dummy for demo
            
            val tx = Transaction(
                senderAddress = myAddress,
                recipientAddress = recipientAddress,
                ephemeralPublicKey = CryptoManager.toBase64(ephemeralKeyPair.public.encoded),
                ciphertext = CryptoManager.toBase64(ciphertext),
                iv = CryptoManager.toBase64(iv),
                signature = CryptoManager.toBase64(CryptoManager.sign(ciphertext, myKeyPair.private)),
                timestamp = System.currentTimeMillis()
            )

            val newBlock = Block(
                index = blockchain.size,
                timestamp = System.currentTimeMillis(),
                transactions = listOf(tx),
                previousHash = blockchain.last().hash
            )

            val minedBlock = BlockchainEngine.mineBlock(newBlock)
            blockchain.add(minedBlock)
            updateMessages()
            
            peerIp?.let {
                p2pManager.connectAndSend(it, minedBlock)
            }
            
            isMining.value = false
        }
    }

    private fun onNewBlockReceived(block: Block) {
        if (BlockchainEngine.isValidBlock(block, blockchain.lastOrNull())) {
            blockchain.add(block)
            updateMessages()
        }
    }

    private fun updateMessages() {
        // In a real app, we'd scan the blockchain and decrypt messages for us
        // Here we just update a list for the UI
        val allMsgs = blockchain.flatMap { it.transactions }.map { tx ->
            DisplayMessage(
                sender = if (tx.senderAddress == myAddress) "Me" else tx.senderAddress.take(8),
                text = if (tx.senderAddress == myAddress) "Message Sent (Secured)" else "Message Received (Encrypted)",
                isMe = tx.senderAddress == myAddress
            )
        }
        messages.clear()
        messages.addAll(allMsgs)
    }

    override fun onCleared() {
        super.onCleared()
        p2pManager.stopServer()
    }
}
