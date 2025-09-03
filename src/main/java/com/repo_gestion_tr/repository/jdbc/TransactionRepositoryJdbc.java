package com.repo_gestion_tr.repository.jdbc;

import com.repo_gestion_tr.data.MysqlData;
import com.repo_gestion_tr.entity.*;
import com.repo_gestion_tr.repository.TransactionRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TransactionRepositoryJdbc implements TransactionRepository {

    @Override
    public Transaction save(Transaction t) {
        final String sql = """
            INSERT INTO `transaction` (montant, `date`, `type`, compte_id)
            VALUES (?, ?, ?, ?)
            """;
        try (Connection cn = MysqlData.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDouble(1, t.getMontant());
            ps.setDate(2, Date.valueOf(t.getDate()));
            ps.setString(3, t.getType().name());
            ps.setInt(4, t.getCompte().getId());
            ps.executeUpdate();

            int newId;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) return null;
                newId = keys.getInt(1);
            }
            return findById(newId);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur JDBC save(Transaction)", e);
        }
    }

    @Override
    public Transaction findById(int id) {
        final String sql = "SELECT id, montant, `date`, `type`, compte_id FROM `transaction` WHERE id=?";
        try (Connection cn = MysqlData.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                int trId = rs.getInt("id");
                double montant = rs.getDouble("montant");
                LocalDate date = rs.getDate("date").toLocalDate();
                String typeStr = rs.getString("type");
                int compteId = rs.getInt("compte_id");

                Compte compte = fetchCompteById(cn, compteId);
                TypeTransaction type = TypeTransaction.valueOf(typeStr.toUpperCase(Locale.ROOT));
                return new Transaction(trId, montant, date, type, compte);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur JDBC findById(Transaction)", e);
        }
    }

    @Override
    public List<Transaction> findAll() {
        final String sql = "SELECT id, montant, `date`, `type`, compte_id FROM `transaction` ORDER BY id";
        List<Transaction> out = new ArrayList<>();
        try (Connection cn = MysqlData.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int trId = rs.getInt("id");
                double montant = rs.getDouble("montant");
                LocalDate date = rs.getDate("date").toLocalDate();
                String typeStr = rs.getString("type");
                int compteId = rs.getInt("compte_id");

                Compte compte = fetchCompteById(cn, compteId);
                TypeTransaction type = TypeTransaction.valueOf(typeStr.toUpperCase(Locale.ROOT));
                out.add(new Transaction(trId, montant, date, type, compte));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur JDBC findAll(Transaction)", e);
        }
    }

    @Override
    public List<Transaction> findByCompteId(int compteId) {
        final String sql = "SELECT id, montant, `date`, `type`, compte_id FROM `transaction` WHERE compte_id=? ORDER BY id";
        List<Transaction> out = new ArrayList<>();
        try (Connection cn = MysqlData.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, compteId);
            try (ResultSet rs = ps.executeQuery()) {
                Compte compte = fetchCompteById(cn, compteId);
                while (rs.next()) {
                    int trId = rs.getInt("id");
                    double montant = rs.getDouble("montant");
                    LocalDate date = rs.getDate("date").toLocalDate();
                    String typeStr = rs.getString("type");
                    TypeTransaction type = TypeTransaction.valueOf(typeStr.toUpperCase(Locale.ROOT));
                    out.add(new Transaction(trId, montant, date, type, compte));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur JDBC findByCompteId(Transaction)", e);
        }
    }

    private Compte fetchCompteById(Connection cn, int id) throws SQLException {
        final String sql = """
            SELECT id, numero, solde, date_ouverture, type_compte, date_debut_blocage, duree_blocage_mois
            FROM compte WHERE id=?
            """;
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                int cid = rs.getInt("id");
                String numero = rs.getString("numero");
                java.math.BigDecimal solde = rs.getBigDecimal("solde");
                LocalDate dateOuverture = rs.getDate("date_ouverture").toLocalDate();
                String type = rs.getString("type_compte");
                Date dDebut = rs.getDate("date_debut_blocage");
                Integer duree = (Integer) rs.getObject("duree_blocage_mois");

                if ("EPARGNE".equalsIgnoreCase(type)) {
                    LocalDate debut = dDebut == null ? null : dDebut.toLocalDate();
                    return new CompteEpargne(cid, numero, solde, dateOuverture, debut, duree);
                } else {
                    return new CompteCheque(cid, numero, solde, dateOuverture);
                }
            }
        }
    }
}
