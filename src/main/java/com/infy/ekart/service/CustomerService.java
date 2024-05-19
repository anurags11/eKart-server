package com.infy.ekart.service;

import com.infy.ekart.dto.CustomerDTO;
import com.infy.ekart.exception.EKartException;

public interface CustomerService {

	CustomerDTO authenticateCustomer(String emailId, String password) throws EKartException;

	String registerNewCustomer(CustomerDTO customerDTO) throws EKartException;

	void updateShippingAddress(String customerEmailId, String address) throws EKartException;

	void deleteShippingAddress(String customerEmailId) throws EKartException;

	CustomerDTO getCustomerByEmailId(String emailId) throws EKartException;

}