package com.webapp.mail.service;

import com.webapp.mail.model.ProductOrder;

public interface OrderService
{
	public boolean sendOrderConfirmation(ProductOrder productOrder);
}
