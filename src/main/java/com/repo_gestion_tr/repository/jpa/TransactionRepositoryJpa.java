package com.repo_gestion_tr.repository.jpa;

import com.repo_gestion_tr.entity.Transaction;
import com.repo_gestion_tr.repository.TransactionRepository;
import com.repo_gestion_tr.jpa.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public final class TransactionRepositoryJpa implements TransactionRepository {

    @Override
    public Transaction save(Transaction entity) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (entity.getId() == null || entity.getId() == 0) {
                em.persist(entity);
                em.flush();
            } else {
                entity = em.merge(entity);
            }
            tx.commit();
            return findById(entity.getId());
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Transaction findById(int id) {
        EntityManager em = JpaUtil.createEntityManager();
        try { return em.find(Transaction.class, id); }
        finally { em.close(); }
    }

    @Override
    public List<Transaction> findAll() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("select t from Transaction t order by t.id", Transaction.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Transaction> findByCompteId(int compteId) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("select t from Transaction t where t.compte.id=:cid order by t.id",Transaction.class)
                     .setParameter("cid", compteId).getResultList();
        } finally {
            em.close();
        }
    }
}
