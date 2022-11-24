package com.invoice.api.repository;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.invoice.api.entity.Invoice;

@Repository
public interface RepoInvoice extends JpaRepository<Invoice, Integer>{

	List<Invoice> findByRfcAndStatus(String rfc, Integer status);

	@Modifying
	@Transactional
	@Query(value ="UPDATE invoice SET subtotal = :subtotal, taxes = :taxes, total = :total WHERE invoice_id = :invoice_id AND status = 1", nativeQuery = true)
	void updateValues(Double subtotal, Double taxes, Double total, Integer invoice_id);

	@Query(value ="SELECT * FROM invoice WHERE rfc = :rfc AND created_at = :created_at", nativeQuery = true)
	Invoice findByRfcAndCreated_at(String rfc, LocalDateTime created_at);

	@Query(value ="SELECT * FROM invoice WHERE rfc = :rfc AND total = -1.0", nativeQuery = true)
	Invoice findInvoiceCreated(String rfc);

}
