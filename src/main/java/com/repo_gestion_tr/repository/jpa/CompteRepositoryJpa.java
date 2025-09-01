package com.repo_gestion_tr.repository.jpa;

import com.repo_gestion_tr.entity.Compte;
import com.repo_gestion_tr.repository.CompteRepository;
import com.repo_gestion_tr.jpa.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.math.BigDecimal;
import java.util.List;

public final class CompteRepositoryJpa implements CompteRepository {

    @Override
    public Compte save(Compte entity) {
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
    public boolean existsByNumero(String numero) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            Long count = em.createQuery(
                "select count(c) from Compte c where c.numero=:n", Long.class)
                .setParameter("n", numero)
                .getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    @Override
    public void updateSolde(int compteId, BigDecimal nouveauSolde) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("update Compte c set c.solde=:s where c.id=:id")
              .setParameter("s", nouveauSolde)
              .setParameter("id", compteId)
              .executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Compte findById(int id) {
        EntityManager em = JpaUtil.createEntityManager();
        try { return em.find(Compte.class, id); }
        finally { em.close(); }
    }

    @Override
    public List<Compte> findAll() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("select c from Compte c order by c.id", Compte.class).getResultList();
        } finally {
            em.close();
        }
    }
}

