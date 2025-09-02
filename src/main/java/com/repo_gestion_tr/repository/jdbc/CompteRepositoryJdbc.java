package com.repo_gestion_tr.repository.jdbc;

import com.repo_gestion_tr.data.MysqlData;
import com.repo_gestion_tr.entity.*;
import com.repo_gestion_tr.repository.CompteRepository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class CompteRepositoryJdbc implements CompteRepository {

    @Override
    public Compte save(Compte c) {
        final String sql = """
            INSERT INTO compte (numero, solde, date_ouverture, type_compte, date_debut_blocage, duree_blocage_mois)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection cn = MysqlData.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNumero());
            ps.setBigDecimal(2, c.getSolde());
            ps.setDate(3, Date.valueOf(c.getDateOuverture()));
            ps.setString(4, c.getType().name());
            if (c instanceof CompteEpargne e) {
                ps.setDate(5, e.getDateDebut() == null ? null : Date.valueOf(e.getDateDebut()));
                ps.setObject(6, e.getDureeBlocage(), Types.INTEGER);
            } else {
                ps.setNull(5, Types.DATE);
                ps.setNull(6, Types.INTEGER);
            }
            ps.executeUpdate();

            int newId;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) return null;
                newId = keys.getInt(1);
            }
            return findById(newId);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur JDBC save(Compte)", e);
        }
    }

    @Override
    public boolean existsByNumero(String numero) {
        final String sql = "SELECT 1 FROM compte WHERE numero=? LIMIT 1";
        try (Connection cn = MysqlData.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur JDBC existsByNumero", e);
        }
    }

    @Override
    public void updateSolde(int compteId, BigDecimal nouveauSolde) {
        final String sql = "UPDATE compte SET solde=? WHERE id=?";
        try (Connection cn = MysqlData.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, nouveauSolde);
            ps.setInt(2, compteId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur JDBC updateSolde", e);
        }
    }

    @Override
    public Compte findById(int id) {
        final String sql = """
            SELECT id, numero, solde, date_ouverture, type_compte, date_debut_blocage, duree_blocage_mois
            FROM compte WHERE id=?
            """;
        try (Connection cn = MysqlData.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                int cid = rs.getInt("id");
                String numero = rs.getString("numero");
                BigDecimal solde = rs.getBigDecimal("solde");
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
        } catch (SQLException e) {
            throw new RuntimeException("Erreur JDBC findById(Compte)", e);
        }
    }

    @Override
    public List<Compte> findAll() {
        final String sql = """
            SELECT id, numero, solde, date_ouverture, type_compte, date_debut_blocage, duree_blocage_mois
            FROM compte ORDER BY id
            """;
        List<Compte> out = new ArrayList<>();
        try (Connection cn = MysqlData.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int cid = rs.getInt("id");
                String numero = rs.getString("numero");
                BigDecimal solde = rs.getBigDecimal("solde");
                LocalDate dateOuverture = rs.getDate("date_ouverture").toLocalDate();
                String type = rs.getString("type_compte");
                Date dDebut = rs.getDate("date_debut_blocage");
                Integer duree = (Integer) rs.getObject("duree_blocage_mois");

                if ("EPARGNE".equalsIgnoreCase(type)) {
                    LocalDate debut = dDebut == null ? null : dDebut.toLocalDate();
                    out.add(new CompteEpargne(cid, numero, solde, dateOuverture, debut, duree));
                } else {
                    out.add(new CompteCheque(cid, numero, solde, dateOuverture));
                }
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur JDBC findAll(Compte)", e);
        }
    }
}
