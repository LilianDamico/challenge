package com.challenge.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.challenge.domain.transaction.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{

}
