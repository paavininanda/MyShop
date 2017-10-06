package com.nupurbaghel.myshop;

import android.os.AsyncTask;
import android.util.Log;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static javax.mail.internet.InternetAddress.parse;

public class GMailSender  extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... params) {
        Log.i("Starting async Task","to send mail");
        //index 0 email id of customer
        //index 1 subject
        //index 2 message
        final String username = params[3];
        final String password = params[4];

        String from = username;
        String to= username;
        String cc= params[0];

        String host = "smtp.gmail.com";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,
                    parse(to));
            message.setRecipients(Message.RecipientType.CC, parse(cc));

            // Set Subject: header field
            message.setSubject(params[1]);

            // Now set the actual message
            //message.setText(params[2]);
            message.setContent(params[2],"text/html");
            // Send message
            Transport.send(message);
            Log.i("Mail","Successfully sent");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

}