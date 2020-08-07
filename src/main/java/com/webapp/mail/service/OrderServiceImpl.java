package com.webapp.mail.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webapp.mail.model.ProductOrder;

@Service("orderService")
public class OrderServiceImpl implements OrderService
{

	@Autowired
	private MailService mailService;

	public void sendOrderConfirmation(ProductOrder productOrder)
	{
		mailService.sendEmail(productOrder);
	}

}
