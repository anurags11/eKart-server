package com.infy.ekart.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infy.ekart.dto.ProductDTO;
import com.infy.ekart.entity.Product;
import com.infy.ekart.exception.EKartException;

import com.infy.ekart.repository.ProductRepository;

@Service (value ="customerProductService")
@Transactional
public class CustomerProductServiceImpl implements CustomerProductService {
	@Autowired
	private ProductRepository productRepository;
	
	@Override
	public List<ProductDTO> getAllProducts() throws EKartException {
		
		Iterable<Product> product = productRepository.findAll();
		List<ProductDTO> productList = new ArrayList<>();
		product.forEach(p -> {
			ProductDTO pdto = new ProductDTO();
			pdto.setAvailableQuantity(p.getAvailableQuantity());
			pdto.setBrand(p.getBrand());
			pdto.setCategory(p.getCategory());
			pdto.setDescription(p.getDescription());
			pdto.setName(p.getName());
			pdto.setPrice(p.getPrice());
			pdto.setProductId(p.getProductId());
			productList.add(pdto);
		});
		
		return productList;
		
	}

	@Override
	public ProductDTO getProductById(Integer productId) throws EKartException {

		Optional<Product> productOp = productRepository.findById(productId);
		Product product = productOp.orElseThrow(() -> new EKartException("ProductService.PRODUCT_NOT_AVAILABLE"));

		ProductDTO productDTO = new ProductDTO();
		productDTO.setBrand(product.getBrand());
		productDTO.setCategory(product.getCategory());
		productDTO.setDescription(product.getDescription());
		productDTO.setName(product.getName());
		productDTO.setPrice(product.getPrice());
		productDTO.setProductId(product.getProductId());
		productDTO.setAvailableQuantity(product.getAvailableQuantity());

		return productDTO;
	}

	@Override
	public void reduceAvailableQuantity(Integer productId, Integer quantity) throws EKartException {
		Optional<Product> productOp = productRepository.findById(productId);
		Product product = productOp.orElseThrow(() -> new EKartException("ProductService.PRODUCT_NOT_AVAILABLE"));
		product.setAvailableQuantity(product.getAvailableQuantity() - quantity);
	}
}