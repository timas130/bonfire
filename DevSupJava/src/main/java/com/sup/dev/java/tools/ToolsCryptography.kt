package com.sup.dev.java.tools

import com.sup.dev.java.libs.bcrypt.BCrypt
import com.sup.dev.java.libs.debug.err
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.experimental.xor

object ToolsCryptography {

    fun generateString(length: Int): String {
        val random = Random()
        val text = CharArray(length)
        for (i in 0 until length) {
            // ascii-only
            val c = (random.nextInt(95) + 32).toChar()
            text[i] = c
        }
        return String(text)
    }

    fun md5(st: String): String {
        var digest = ByteArray(0)
        try {
            val messageDigest = MessageDigest.getInstance("MD5")
            messageDigest.reset()
            messageDigest.update(ToolsText.toBytes(st))
            digest = messageDigest.digest()
        } catch (e: NoSuchAlgorithmException) {
            err(e)
        }

        val bigInt = BigInteger(1, digest)
        var md5Hex = bigInt.toString(16)
        while (md5Hex.length < 32) {
            md5Hex = "0$md5Hex"
        }
        return md5Hex
    }

    fun decode(pText: ByteArray, pKey: String): String {
        val res = ByteArray(pText.size)
        val key = ToolsText.toBytes(pKey)

        for (i in pText.indices)
            res[i] = (pText[i] xor key[i % key.size])
        return ToolsText.toString(res)
    }

    fun bcrypt(st: String, salt:String=bcryptSalt()): String {
        return BCrypt.hashpw(st, salt)
    }

    fun bcryptCheck(st: String, stBCrypt: String): Boolean {
        return BCrypt.checkpw(st, stBCrypt)
    }

    fun bcryptSalt(logRounds:Int = 12): String {
        return BCrypt.gensalt(logRounds)
    }

    fun getSHA512(input:String):String{
        val md: MessageDigest = MessageDigest.getInstance("SHA-512")
        val messageDigest = md.digest(input.toByteArray())

        val no = BigInteger(1, messageDigest)

        var hashtext: String = no.toString(16)

        while (hashtext.length < 32) {
            hashtext = "0$hashtext"
        }

        return hashtext
    }

}
