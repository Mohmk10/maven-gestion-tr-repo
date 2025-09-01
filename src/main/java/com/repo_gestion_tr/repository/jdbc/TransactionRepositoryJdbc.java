package com.repo_gestion_tr.repository.jdbc;

import com.repo_gestion_tr.data.MysqlData;
import com.repo_gestion_tr.entity.Compte;
import com.repo_gestion_tr.entity.Transaction;
import com.repo_gestion_tr.entity.TypeTransaction;
import com.repo_gestion_tr.repository.TransactionRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class TransactionRepositoryJdbc implements TransactionRepository {

    private Transaction map(ResultSet rs, Compte compte) throws SQLException {
        int id = rs.getInt("id");
        double montant = rs.getDouble("montant");
        LocalDate date = rs.getDate("date").toLocalDate();
        TypeTransaction type = TypeTransaction.valueOf(rs.getString("type"));
        return new Transaction(id, montant, date, type, compte);
    }

    @Override
    public Transaction save(Transaction t) {
        boolean insert = (t.getId() == null || t.getId() == 0);

        final String sqlInsert = "INSERT INTO `transaction` (montant, `date`, `type`, compte_id) VALUES (?,?,?,?)";
        final String sqlUpdate = "UPDATE `transaction` SET montant=?, `date`=?, `type`=?, compte_id=? WHERE id=?";

        try (Connection cn = MysqlData.getConnection()) {
            if (insert) {
                try (PreparedStatement ps = cn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setDouble(1, t.getMontant());
                    ps.setDate(2, Date.valueOf(t.getDate()));
                    ps.setString(3, t.getType().name());
                    ps.setInt(4, t.getCompte().getId());
                    ps.executeUpdate();
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) return findById(gk.getInt(1)); // ← relit après insert
                    }
                    throw new SQLException("INSERT `transaction` : pas de clé générée");
                }
            } else {
                try (PreparedStatement ps = cn.prepareStatement(sqlUpdate)) {
                    ps.setDouble(1, t.getMontant());
                    ps.setDate(2, Date.valueOf(t.getDate()));
                    ps.setString(3, t.getType().name());
                    ps.setInt(4, t.getCompte().getId());
                    ps.setInt(5, t.getId());
                    ps.executeUpdate();
                    return findById(t.getId());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur JDBC save(Transaction)", e);
        }
    }

    @Override
    public Transaction findById(int id) {
        final String sql = "SELECT t.* FROM `transaction` t WHERE t.id=?";
        try (Connection cn = MysqlData.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                int compteId = rs.getInt("compte_id");
                CompteRepositoryJdbc helper = new CompteRepositoryJdbc();
                Compte c = helper.findById(compteId);
                return map(rs, c);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur JDBC findById(Transaction)", e);
        }
    }

    @Override
    public List<Transaction> findAll() {
        final String sql = "SELECT id FROM `transaction` ORDER BY id";
        List<Transaction> out = new ArrayList<>();
        try (Connection cn = MysqlData.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Transaction t = findById(rs.getInt(1));
                if (t != null) out.add(t);
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur JDBC findAll(Transaction)", e);
        }
    }

    @Override
    public List<Transaction> findByCompteId(int compteId) {
        final String sql = "SELECT t.* FROM `transaction` t WHERE t.compte_id=? ORDER BY t.id";
        List<Transaction> out = new ArrayList<>();
        try (Connection cn = MysqlData.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, compteId);
            try (ResultSet rs = ps.executeQuery()) {
                CompteRepositoryJdbc helper = new CompteRepositoryJdbc();
                Compte compte = helper.findById(compteId);
                while (rs.next()) out.add(map(rs, compte));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur JDBC findByCompteId", e);
        }
    }
}
