package com.sup.dev.java_pc.google

import com.sun.mail.smtp.SMTPTransport
import java.security.Security
import java.util.*
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class GoogleMail(private val username: String, private val password: String) {
    //private val props: Properties

    init {

       /* Security.addProvider(com.sun.net.ssl.internal.ssl.Provider())
        val SSL_FACTORY = "javax.net.ssl.SSLSocketFactory"
        props = System.getProperties()
        props.setProperty("mail.smtps.host", "smtp.gmail.com")
        props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY)
        props.setProperty("mail.smtp.socketFactory.fallback", "false")
        props.setProperty("mail.smtp.port", "465")
        props.setProperty("mail.smtp.socketFactory.port", "465")
        props.setProperty("mail.smtps.auth", "true")
        props["mail.smtps.quitwait"] = "false"*/

    }

    @Throws(MailException::class)
    fun send(recipientEmail: String, title: String, message: String) {
       /* val session = Session.getInstance(props, null)

        val msg = MimeMessage(session)

        try {
            msg.setFrom(InternetAddress("$username@gmail.com"))
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail, false))

            msg.subject = title
            msg.setText(message, "utf-8")
            msg.sentDate = Date()

            val t = session.getTransport("smtps") as SMTPTransport

            t.connect("smtp.gmail.com", username, password)
            t.sendMessage(msg, msg.allRecipients)
            t.close()
        } catch (e: MessagingException) {
            throw MailException(e)
        }*/

    }

    class MailException(e: Exception) : Exception(e)

}