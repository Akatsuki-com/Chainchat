package com.example.myapplication.crypto

import android.util.Base64
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val KEY_ALGO = "EC"
    private const val SIGN_ALGO = "SHA256withECDSA"
    private const val ENCRYPT_ALGO = "AES/GCM/NoPadding"
    private const val CURVE = "secp256r1"

    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGO)
        keyPairGenerator.initialize(ECGenParameterSpec(CURVE))
        return keyPairGenerator.generateKeyPair()
    }

    fun deriveSharedKey(privateKey: PrivateKey, publicKeyBytes: ByteArray): SecretKeySpec {
        val keyFactory = KeyFactory.getInstance(KEY_ALGO)
        val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKeyBytes))
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(publicKey, true)
        val sharedSecret = keyAgreement.generateSecret()
        return SecretKeySpec(sharedSecret.take(32).toByteArray(), "AES")
    }

    fun encrypt(data: String, secretKey: SecretKeySpec): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(ENCRYPT_ALGO)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(data.toByteArray())
        return Pair(ciphertext, iv)
    }

    fun decrypt(ciphertext: ByteArray, iv: ByteArray, secretKey: SecretKeySpec): String {
        val cipher = Cipher.getInstance(ENCRYPT_ALGO)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return String(cipher.doFinal(ciphertext))
    }

    fun sign(data: ByteArray, privateKey: PrivateKey): ByteArray {
        val signer = Signature.getInstance(SIGN_ALGO)
        signer.initSign(privateKey)
        signer.update(data)
        return signer.sign()
    }

    fun verify(data: ByteArray, signature: ByteArray, publicKeyBytes: ByteArray): Boolean {
        val keyFactory = KeyFactory.getInstance(KEY_ALGO)
        val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKeyBytes))
        val verifier = Signature.getInstance(SIGN_ALGO)
        verifier.initVerify(publicKey)
        verifier.update(data)
        return verifier.verify(signature)
    }
    
    fun toBase64(data: ByteArray): String = Base64.encodeToString(data, Base64.NO_WRAP)
    fun fromBase64(data: String): ByteArray = Base64.decode(data, Base64.NO_WRAP)
}
