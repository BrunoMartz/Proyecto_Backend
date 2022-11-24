package com.invoice.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.DtoProduct;
import com.invoice.api.entity.Cart;
import com.invoice.api.entity.Invoice;
import com.invoice.api.entity.Item;
import com.invoice.api.repository.RepoCart;
import com.invoice.api.repository.RepoInvoice;
import com.invoice.api.repository.RepoItem;
import com.invoice.configuration.client.ProductClient;
import com.invoice.exception.ApiException;

@Service
public class SvcInvoiceImp implements SvcInvoice {

	@Autowired
	RepoInvoice repo;
	
	@Autowired
	RepoItem repoItem;
	
	@Autowired
	RepoCart repoCart;
	
	@Autowired
	ProductClient productCl;

	@Override
	public List<Invoice> getInvoices(String rfc) {
		return repo.findByRfcAndStatus(rfc, 1);
	}

	@Override
	public List<Item> getInvoiceItems(Integer invoice_id) {
		return repoItem.getInvoiceItems(invoice_id);
	}

	@Override
	public ApiResponse generateInvoice(String rfc) {
		/*
		 * Requerimiento 5
		 * Implementar el método para generar una factura 
		 */
		List<Cart> cartSaved = repoCart.findByRfcAndStatus(rfc,1);
		
		if(cartSaved.size() == 0) 
			throw new ApiException(HttpStatus.NOT_FOUND, "cart has no items");
		
		Invoice invoice = new Invoice();
		List<Item> items = new ArrayList<>();
		Double total = -1.0, taxes = 0.0, subtotal = 0.0;
		
		invoice.setRfc(rfc);
		invoice.setSubtotal(subtotal);
		invoice.setTaxes(taxes);
		invoice.setTotal(total);
		invoice.setCreated_at(LocalDateTime.now());
		invoice.setStatus(1);
		repo.save(invoice);
		
		total = 0.0;
		
		Invoice invoiceCreated = repo.findInvoiceCreated(rfc);
		
		for(Cart productCart : cartSaved) {
			Item item = new Item();
			item.setId_invoice(invoiceCreated.getInvoice_id());
			item.setGtin(productCart.getGtin());
			item.setQuantity(productCart.getQuantity());
			item.setUnit_price(getProductPrice(productCart.getGtin()));
			item.setTotal(item.getUnit_price()*item.getQuantity());
			item.setTaxes(item.getTotal()*.16);
			item.setSubtotal(item.getTotal()-item.getTaxes());
			item.setStatus(1);
			
			items.add(item);
			
			repoItem.save(item);
			
			productCl.updateProductStock(item.getGtin(), item.getQuantity());
		}
		
		repoCart.clearCart(rfc);
		
		for(Item item : items) {
			total += item.getTotal();
			taxes += item.getTaxes();
			subtotal += item.getSubtotal();
		}
		
		invoice.setSubtotal(subtotal);
		invoice.setTaxes(taxes);
		invoice.setTotal(total);
		
		repo.updateValues(subtotal,taxes,total,invoiceCreated.getInvoice_id());
		
		
		return new ApiResponse("invoice generated");
	}
	
	private Double getProductPrice(String gtin) {
		try {
			DtoProduct product = productCl.findByGtin(gtin);
			return product.getPrice();
		}catch(Exception e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "unable to retrieve product information");
		}
	}

}
