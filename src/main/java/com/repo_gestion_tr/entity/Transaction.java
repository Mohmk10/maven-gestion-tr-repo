package com.repo_gestion_tr.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

import com.repo_gestion_tr.utils.DateFormat;

@Entity
@Table(name = "\"transaction\"")

public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private double montant;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TypeTransaction type;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte;

    protected Transaction() {}

    public Transaction(Integer id, double montant, LocalDate date, TypeTransaction type, Compte compte) {
        this.id = id;
        this.montant = montant;
        this.date = date;
        this.type = type;
        this.compte = compte;
    }

    public Integer getId() { return id; }
    public double getMontant() { return montant; }
    public LocalDate getDate() { return date; }
    public TypeTransaction getType() { return type; }
    public Compte getCompte() { return compte; }

    @Override
    public String toString() {
        return "\nID: " + id
             + "\nMontant: " + montant
             + "\nType de transaction: " + type
             + "\nDate de transaction: " + DateFormat.formatDate(date);
    }
}
