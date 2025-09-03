// src/main/java/com/repo_gestion_tr/repository/jpa/TransactionRepositoryJpa.java
package com.repo_gestion_tr.repository.jpa;

import com.repo_gestion_tr.entity.Compte;
import com.repo_gestion_tr.entity.Transaction;
import com.repo_gestion_tr.jpa.JpaUtil;
import com.repo_gestion_tr.repository.TransactionRepository;

import jakarta.persistence.EntityManager;
import java.util.List;

public final class TransactionRepositoryJpa implements TransactionRepository {

    @Override
    public Transaction save(Transaction t) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            em.getTransaction().begin();
            Integer compteId = t.getCompte().getId();
            Compte managedCompte = em.getReference(Compte.class, compteId);


            Transaction toPersist = new Transaction(
                    null,
                    t.getMontant(),
                    t.getDate(),
                    t.getType(),
                    managedCompte
            );
            em.persist(toPersist);

            em.getTransaction().commit();
            return toPersist;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Transaction findById(int id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.find(Transaction.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Transaction> findAll() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT t FROM Transaction t ORDER BY t.id", Transaction.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Transaction> findByCompteId(int compteId) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.compte.id = :id ORDER BY t.id",
                    Transaction.class
            ).setParameter("id", compteId).getResultList();
        } finally {
            em.close();
        }
    }
}
