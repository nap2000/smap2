package org.smap.notifications.interfaces;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

/*****************************************************************************

This file is part of SMAP.

Copyright Smap Consulting Pty Ltd

 ******************************************************************************/

/*
 * Manage the table that stores details on the forwarding of data onto other systems
 */
public class EmitAwsSES {
	
	private static Logger log =
			 Logger.getLogger(EmitAwsSES.class.getName());
	
	Properties properties = new Properties();
	AmazonSimpleEmailService client;
	
	public EmitAwsSES(String region, String basePath) {
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(basePath + "_bin/resources/properties/aws.properties");
			properties.load(fis);
		}
		catch (Exception e) { 
			log.log(Level.SEVERE, "Error reading properties", e);
		} finally {
			try {fis.close();} catch (Exception e) {}
		}
		
		//create a new SES client
		client = AmazonSimpleEmailServiceClient.builder()
				.withRegion(region)
				.withCredentials(new DefaultAWSCredentialsProviderChain())
				.build();
	}
	
	// Send an email
	public void sendSES(InternetAddress[] recipients, String subject, 
			String emailId,
			String content,
			String filePath,
			String filename) throws Exception  {
		
		// Add email ID to make subject unique and to allow replies
		String subject2 = subject + " " + emailId;

        log.info("Send");
        send(client, "Cases Smap Server <smap@server.smap.com.au>", recipients, subject2, content,
        		filePath,
        		filename);
        log.info("Done Email Sent");
		
	}
	
	public static void send(AmazonSimpleEmailService client,
            String sender,
            InternetAddress[] recipients,
            String subject,
            String bodyHTML,
            String filePath,
			String filename) throws Exception {

        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage msg = new MimeMessage(session);
        msg.setSubject(subject, "UTF-8");
        msg.setFrom(new InternetAddress(sender));
        msg.setRecipients(Message.RecipientType.TO, recipients);

        MimeMultipart mmp = new MimeMultipart("mixed");
        msg.setContent(mmp);
        
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(bodyHTML,"text/html; charset=UTF-8");
        mmp.addBodyPart(htmlPart);
        
        // Add file attachments if they exist
     	if(filePath != null) {	
     		log.info("Adding file: " + filePath);
     		MimeBodyPart attBodyPart = new MimeBodyPart();
     		DataSource source = new FileDataSource(filePath);
     		attBodyPart.setDataHandler(new DataHandler(source));
     		attBodyPart.setFileName(filename);
     		mmp.addBodyPart(attBodyPart);
     	}
     			
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        msg.writeTo(outputStream);
        RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
        SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);

        log.info("Attempting to send an email through Amazon SES " 
            		+ "using the AWS SDK for Java...");
        log.info("Sending AWS email from: " + sender + " with subject " + subject);
        client.sendRawEmail(rawEmailRequest);

    }

}


