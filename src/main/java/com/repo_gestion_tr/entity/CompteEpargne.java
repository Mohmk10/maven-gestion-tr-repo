package com.repo_gestion_tr.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("EPARGNE")

public class CompteEpargne extends Compte {

    @Column(name = "date_debut_blocage")
    private LocalDate dateDebut;

    @Column(name = "duree_blocage_mois")
    private Integer dureeBlocage;

    protected CompteEpargne() {}

    public CompteEpargne(Integer id, String numero, BigDecimal solde, LocalDate dateOuverture, LocalDate dateDebut, Integer dureeBlocage) {
        super(id, numero, solde, dateOuverture, TypeCompte.EPARGNE);
        this.dateDebut = dateDebut;
        this.dureeBlocage = dureeBlocage;
    }

    public LocalDate getDateDebut() { return dateDebut; }
    public Integer getDureeBlocage() { return dureeBlocage; }

    @Override
    public void depot(double montant) {
        setSolde(getSolde().add(BigDecimal.valueOf(montant)));
    }

    @Override
    public boolean retrait(double montant) {
        LocalDate fin = dateDebut != null && dureeBlocage != null ? dateDebut.plusMonths(dureeBlocage) : null;
        if (fin != null && LocalDate.now().isAfter(fin) && getSolde().doubleValue() >= montant) {
            setSolde(getSolde().subtract(BigDecimal.valueOf(montant)));
            return true;
        }
        return false;
    }
}
