package com.repo_gestion_tr.repository;

import java.util.List;

import com.repo_gestion_tr.entity.Transaction;

public interface TransactionRepository extends Repository<Transaction>{
    
    Transaction save(Transaction transaction);
    Transaction findById(int id);
    List<Transaction> findAll();
    List<Transaction> findByCompteId(int compteId);
    
}
