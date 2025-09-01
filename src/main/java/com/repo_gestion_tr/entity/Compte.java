package com.repo_gestion_tr.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.repo_gestion_tr.utils.DateFormat;

@Entity
@Table(name = "compte")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type_compte", discriminatorType = DiscriminatorType.STRING, length = 16)

public abstract class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 64)
    private String numero;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal solde;

    @Column(name = "date_ouverture", nullable = false)
    private LocalDate dateOuverture;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_enum", nullable = false, length = 16)
    private TypeCompte type;

    protected Compte() {}

    public Compte(Integer id, String numero, BigDecimal solde, LocalDate dateOuverture, TypeCompte type) {
        this.id = id;
        this.numero = numero;
        this.solde = solde;
        this.dateOuverture = dateOuverture;
        this.type = type;
    }

    public Integer getId() { return id; }
    public String getNumero() { return numero; }
    public BigDecimal getSolde() { return solde; }
    public LocalDate getDateOuverture() { return dateOuverture; }
    public TypeCompte getType() { return type; }

    public abstract void depot(double montant);
    public abstract boolean retrait(double montant);

    @Override
    public String toString() {
        return "\nID: " + id
             + "\nNuméro: " + numero
             + "\nSolde: " + solde
             + "\nType de compte: " + type
             + "\nDate de création: " + DateFormat.formatDate(dateOuverture);
    }

    protected void setSolde(BigDecimal solde) { this.solde = solde; }
}
