package com.webapp.mail.service;

import com.webapp.mail.model.ProductOrder;

public interface OrderService
{
	public void sendOrderConfirmation(ProductOrder productOrder);
}
