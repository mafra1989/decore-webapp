package com.webapp.util;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class SendEmail {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	@Bean
	public JavaMailSender getJavaMailSender() {
	    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	    mailSender.setHost("smtp.mailtrap.io");
	    mailSender.setPort(2525);
	     
	    mailSender.setUsername("1a2b3v4d5e6f7g");
	    mailSender.setPassword("1a2b3v4d5e6f7g");
	     
	    Properties props = mailSender.getJavaMailProperties();
	    props.put("mail.transport.protocol", "smtp");
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.starttls.enable", "true");
	    props.put("mail.debug", "true");
	     
	    return mailSender;
	}

}
