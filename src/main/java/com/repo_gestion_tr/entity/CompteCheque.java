package com.repo_gestion_tr.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("CHEQUE")

public class CompteCheque extends Compte {

    private static final BigDecimal FRAIS = BigDecimal.valueOf(0.08);

    protected CompteCheque() {}

    public CompteCheque(Integer id, String numero, BigDecimal solde, LocalDate dateOuverture) {
        super(id, numero, solde, dateOuverture, TypeCompte.CHEQUE);
    }

    @Override
    public void depot(double montant) {
        BigDecimal fact = BigDecimal.valueOf(montant).multiply(FRAIS);
        setSolde(getSolde().add(BigDecimal.valueOf(montant).subtract(fact)));
    }

    @Override
    public boolean retrait(double montant) {
        BigDecimal fact = BigDecimal.valueOf(montant).multiply(FRAIS);
        BigDecimal total = BigDecimal.valueOf(montant).add(fact);
        if (getSolde().compareTo(total) >= 0) {
            setSolde(getSolde().subtract(total));
            return true;
        }
        return false;
    }
}
