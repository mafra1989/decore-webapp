package com.webapp.mail.configuration;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

@Configuration
@ComponentScan(basePackages = "com.webapp.mail")
public class AppConfig
{

	// Put Other Application configuration here.
	@Bean
	public JavaMailSender getMailSender()
	{
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		// Using Gmail SMTP configuration.
		mailSender.setHost("smtp.gmail.com");
		mailSender.setPort(587);

		/*
		 * Use your gmail id and password
		 */
		mailSender.setUsername("evandro.ms2016@gmail.com");
		mailSender.setPassword("tjam2019");

		Properties javaMailProperties = new Properties();
		javaMailProperties.put("mail.smtp.starttls.enable", "true");
		javaMailProperties.put("mail.smtp.auth", "true");
		javaMailProperties.put("mail.transport.protocol", "smtp");
		javaMailProperties.put("mail.debug", "true");
		//javaMailProperties.put("mail.smtp.connectiontimeout", 10000);
		
		javaMailProperties.put("mail.host", "smtp.gmail.com");
		javaMailProperties.put("mail.port", "587");
		javaMailProperties.put("mail.smtp.auth", "true");
		javaMailProperties.put("mail.smtp.socketFactory.port", "587");
		javaMailProperties.put("mail.smtp.socketFactory.fallback", "true");
		javaMailProperties.put("mail.smtp.starttls.required", "true");
		javaMailProperties.put("mail.smtp.ssl.enable", "false");	
			
		mailSender.setJavaMailProperties(javaMailProperties);
		return mailSender;
	}

	/*
	 * FreeMarker configuration.
	 */
	@Bean
	public FreeMarkerConfigurationFactoryBean getFreeMarkerConfiguration()
	{
		FreeMarkerConfigurationFactoryBean bean = new FreeMarkerConfigurationFactoryBean();
		bean.setDefaultEncoding("UTF-8");
		bean.setTemplateLoaderPath("/fmtemplates/");
		return bean;
	}

}
