package com.project.applicationa.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoHelper {
    private const val KEY_ALIAS = "AppSymmetricKey"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    @get:Throws(Exception::class)
    val orCreateKey: SecretKey
        get() {
            val keyStore =
                KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGen = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                keyGen.init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build()
                )
                return keyGen.generateKey()
            }
            return (keyStore.getKey(KEY_ALIAS, null) as SecretKey)
        }

    @Throws(Exception::class)
    fun encrypt(plainText: String): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = orCreateKey
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

        val buffer = ByteBuffer.allocate(4 + iv.size + ciphertext.size)
        buffer.putInt(iv.size)
        buffer.put(iv)
        buffer.put(ciphertext)

        return buffer.array()
    }

    @Throws(Exception::class)
    fun decrypt(encryptedData: ByteArray): String {
        val buffer = ByteBuffer.wrap(encryptedData)
        val ivLength = buffer.getInt()
        val iv = ByteArray(ivLength)
        buffer[iv]
        val cipherText = ByteArray(buffer.remaining())
        buffer[cipherText]

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            orCreateKey, GCMParameterSpec(128, iv)
        )
        val plainText = cipher.doFinal(cipherText)

        return String(plainText, StandardCharsets.UTF_8)
    }

    fun String.performEncryption(): ByteArray {
        return encrypt(this)
    }
}
