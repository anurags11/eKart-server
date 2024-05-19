package com.infy.ekart.api;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.infy.ekart.dto.CardDTO;
import com.infy.ekart.dto.OrderDTO;
import com.infy.ekart.dto.TransactionDTO;
import com.infy.ekart.exception.EKartException;
import com.infy.ekart.service.PaymentService;


@CrossOrigin
@RestController
@Validated
@RequestMapping(value = "/payment-api")
public class PaymentAPI {
	@Autowired
	private PaymentService paymentService;
	@Autowired
	private Environment environment;

	@Autowired
	private RestTemplate template;

	Log logger = LogFactory.getLog(PaymentAPI.class);

	@PostMapping(value = "/customer/{customerEmailId:.+}/cards")
	public ResponseEntity<String> addNewCard(@RequestBody CardDTO cardDTO,
			@Pattern(regexp = "[a-zA-Z0-9._]+@[a-zA-Z]{2,}\\.[a-zA-Z][a-zA-Z.]+", message = "{invalid.email.format}") @PathVariable("customerEmailId") String customerEmailId)
			throws EKartException, NoSuchAlgorithmException {
		logger.info("Recieved request to add new  card for customer : " + customerEmailId);
		cardDTO.setCustomerEmailId(customerEmailId);

		int cardId;
		cardId = paymentService.addCustomerCard(customerEmailId, cardDTO);
		String message = environment.getProperty("PaymentAPI.NEW_CARD_ADDED_SUCCESS");
		String toReturn = message + cardId;
		toReturn = toReturn.trim();
		return new ResponseEntity<>(toReturn, HttpStatus.OK);

	}

	@PutMapping(value = "/update/card")
	public ResponseEntity<String> updateCustomerCard(@Valid @RequestBody CardDTO cardDTO)
			throws EKartException, NoSuchAlgorithmException {
		logger.info("Recieved request to update  card :" + cardDTO.getCardId() + " of customer : "
				+ cardDTO.getCustomerEmailId());

		paymentService.updateCustomerCard(cardDTO);
		String modificationSuccessMsg = environment.getProperty("PaymentAPI.UPDATE_CARD_SUCCESS");
		return new ResponseEntity<>(modificationSuccessMsg, HttpStatus.OK);

	}

	@DeleteMapping(value = "/customer/{customerEmailId:.+}/card/{cardID}/delete")
	public ResponseEntity<String> deleteCustomerCard(@PathVariable("cardID") Integer cardID,
			@Pattern(regexp = "[a-zA-Z0-9._]+@[a-zA-Z]{2,}\\.[a-zA-Z][a-zA-Z.]+", message = "{invalid.email.format}") @PathVariable("customerEmailId") String customerEmailId)
			throws EKartException {
		logger.info("Recieved request to delete  card :" + cardID + " of customer : " + customerEmailId);

		paymentService.deleteCustomerCard(customerEmailId, cardID);
		String modificationSuccessMsg = environment.getProperty("PaymentAPI.CUSTOMER_CARD_DELETED_SUCCESS");
		return new ResponseEntity<>(modificationSuccessMsg, HttpStatus.OK);

	}
	
	@GetMapping(value = "/customer/{customerEmailId}/card-type/{cardType}")
	public ResponseEntity<List<CardDTO>> getCardsOfCustomer(@PathVariable String customerEmailId,
			@PathVariable String cardType) throws EKartException {
		logger.info("Recieved request to fetch  cards of customer : " + customerEmailId + " having card type as: "
				+ cardType);

		List<CardDTO> list =  paymentService.getCustomerCardOfCardType(customerEmailId, cardType);
		return new ResponseEntity<>(list,org.springframework.http.HttpStatus.OK);
		
	}
	
	@PostMapping(value = "/customer/{customerEmailId}/order/{orderId}")
	public ResponseEntity<String> payForOrder(
			@Pattern(regexp = "[a-zA-Z0-9._]+@[a-zA-Z]{2,}\\.[a-zA-Z][a-zA-Z.]+", message = "{invalid.email.format}") @PathVariable("customerEmailId") String customerEmailId,
			@NotNull(message = "{orderId.absent") @PathVariable("orderId") Integer orderId,
			@Valid @RequestBody TransactionDTO transactionDTO) throws NoSuchAlgorithmException, EKartException {
		
		orderId = transactionDTO.getOrder().getOrderId();
		OrderDTO order = template.getForEntity("http://Customer/customerorder-api/order/"+orderId, OrderDTO.class).getBody();
		transactionDTO.setOrder(order);
		transactionDTO.setTotalPrice(order.getTotalPrice());
		transactionDTO.setTransactionDate(order.getDateOfOrder());
		transactionDTO = paymentService.authenticatePayment(customerEmailId, transactionDTO);
		paymentService.addTransaction(transactionDTO);
		String message = environment.getProperty("PaymentAPI.TRANSACTION_SUCCESSFULL_ONE")+transactionDTO.getTotalPrice()+" "+
		environment.getProperty("PaymentAPI.TRANSACTION_SUCCESSFULL_TWO")+orderId+
		environment.getProperty("PaymentAPI.TRANSACTION_SUCCESSFULL_THREE")+transactionDTO.getTransactionId();
		return new ResponseEntity<>(message,org.springframework.http.HttpStatus.OK);

	}

}