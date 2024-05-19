package com.infy.ekart.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infy.ekart.dto.CustomerDTO;
import com.infy.ekart.entity.Customer;
import com.infy.ekart.exception.EKartException;
import com.infy.ekart.repository.CustomerRepository;

@Service(value = "customerService")
@Transactional
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	private CustomerRepository customerRepository;

	@Override
	public CustomerDTO authenticateCustomer(String emailId, String password) throws EKartException {
		CustomerDTO customerDTO = null;

		Optional<Customer> optionalCustomer = customerRepository.findById(emailId.toLowerCase());
		Customer customer = optionalCustomer
				.orElseThrow(() -> new EKartException("CustomerService.CUSTOMER_NOT_FOUND"));
		if (!password.equals(customer.getPassword()))
			throw new EKartException("CustomerService.INVALID_CREDENTIALS");

		customerDTO = new CustomerDTO();
		customerDTO.setEmailId(customer.getEmailId());
		customerDTO.setName(customer.getName());
		customerDTO.setPhoneNumber(customer.getPhoneNumber());
		customerDTO.setPassword(customer.getPassword());
		customerDTO.setNewPassword(customer.getPassword());
		customerDTO.setAddress(customer.getAddress());
		return customerDTO;

	}

	@Override
	public String registerNewCustomer(CustomerDTO customerDTO) throws EKartException {
		String registeredWithEmailId = null;
		boolean isEmailNotAvailable = customerRepository.findById(customerDTO.getEmailId().toLowerCase()).isEmpty();
		boolean isPhoneNumberNotAvailable = customerRepository.findByPhoneNumber(customerDTO.getPhoneNumber())
				.isEmpty();
		if (isEmailNotAvailable) {
			if (isPhoneNumberNotAvailable) {
				Customer customer = new Customer();
				customer.setEmailId(customerDTO.getEmailId().toLowerCase());
				customer.setName(customerDTO.getName());
				customer.setPassword(customerDTO.getPassword());
				customer.setPhoneNumber(customerDTO.getPhoneNumber());
				customer.setAddress(customerDTO.getAddress());
				customerRepository.save(customer);
				registeredWithEmailId = customer.getEmailId();
			} else {
				throw new EKartException("CustomerService.PHONE_NUMBER_ALREADY_IN_USE");
			}
		} else {
			throw new EKartException("CustomerService.EMAIL_ID_ALREADY_IN_USE");
		}
		return registeredWithEmailId;

	}

	@Override
	public void updateShippingAddress(String customerEmailId, String address) throws EKartException {
		Optional<Customer> optionalCustomer = customerRepository.findById(customerEmailId.toLowerCase());
		Customer customer = optionalCustomer
				.orElseThrow(() -> new EKartException("CustomerService.CUSTOMER_NOT_FOUND"));
		customer.setAddress(address);
	}

	@Override
	public void deleteShippingAddress(String customerEmailId) throws EKartException {
		Optional<Customer> optionalCustomer = customerRepository.findById(customerEmailId.toLowerCase());
		Customer customer = optionalCustomer
				.orElseThrow(() -> new EKartException("CustomerService.CUSTOMER_NOT_FOUND"));
		customer.setAddress(null);
	}
	
	@Override
	public CustomerDTO getCustomerByEmailId(String emailId) throws EKartException {

		CustomerDTO customerDTO = null;
		
		//retrieving customer data from repository
		Optional<Customer> optionalCustomer = customerRepository.findById(emailId.toLowerCase());
		Customer customer = optionalCustomer.orElseThrow(() -> new EKartException("CustomerService.CUSTOMER_NOT_FOUND"));
		//comparing entered password with password stored in DB
		

		customerDTO = new CustomerDTO();
		customerDTO.setEmailId(customer.getEmailId());
		customerDTO.setName(customer.getName());
		customerDTO.setPhoneNumber(customer.getPhoneNumber());
		customerDTO.setAddress(customer.getAddress());
		return customerDTO;

	}

}