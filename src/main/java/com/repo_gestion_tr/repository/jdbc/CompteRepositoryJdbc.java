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

    private Compte mapCompte(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String numero = rs.getString("numero");
        BigDecimal solde = rs.getBigDecimal("solde");
        LocalDate dateOuverture = rs.getDate("date_ouverture").toLocalDate();
        String disc = rs.getString("type_compte");

        if ("EPARGNE".equalsIgnoreCase(disc)) {
            Date d = rs.getDate("date_debut_blocage");
            LocalDate dateDebut = (d != null) ? d.toLocalDate() : null;
            Integer duree = (Integer) rs.getObject("duree_blocage_mois");
            return new CompteEpargne(id, numero, solde, dateOuverture, dateDebut, duree);
        } else {
            return new CompteCheque(id, numero, solde, dateOuverture);
        }
    }

    @Override
    public Compte save(Compte c) {
        boolean insert = (c.getId() == null || c.getId() == 0);

        final String sqlInsert =
            "INSERT INTO compte (numero, solde, date_ouverture, type_compte, type_enum, date_debut_blocage, duree_blocage_mois) " +
            "VALUES (?,?,?,?,?,?,?)";
        final String sqlUpdate =
            "UPDATE compte SET numero=?, solde=?, date_ouverture=?, type_compte=?, type_enum=?, " +
            "date_debut_blocage=?, duree_blocage_mois=? WHERE id=?";

        try (Connection cn = MysqlData.getConnection()) {
            if (insert) {
                try (PreparedStatement ps = cn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                    fill(ps, c);
                    ps.executeUpdate();
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) return findById(gk.getInt(1));
                    }
                    throw new SQLException("INSERT compte : pas de clé générée");
                }
            } else {
                try (PreparedStatement ps = cn.prepareStatement(sqlUpdate)) {
                    int idx = fill(ps, c);
                    ps.setInt(idx, c.getId());
                    ps.executeUpdate();
                    return findById(c.getId());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur JDBC save(Compte)", e);
        }
    }


    private int fill(PreparedStatement ps, Compte c) throws SQLException {
        ps.setString(1, c.getNumero());
        ps.setBigDecimal(2, c.getSolde());
        ps.setDate(3, Date.valueOf(c.getDateOuverture()));
        String disc = (c instanceof CompteEpargne) ? "EPARGNE" : "CHEQUE";
        ps.setString(4, disc);
        ps.setString(5, c.getType().name());

        if (c instanceof CompteEpargne ce) {
            if (ce.getDateDebut() != null) ps.setDate(6, Date.valueOf(ce.getDateDebut())); else ps.setNull(6, Types.DATE);
            if (ce.getDureeBlocage() != null) ps.setInt(7, ce.getDureeBlocage()); else ps.setNull(7, Types.INTEGER);
        } else {
            ps.setNull(6, Types.DATE);
            ps.setNull(7, Types.INTEGER);
        }
        return 8;
    }

    @Override
    public boolean existsByNumero(String numero) {
        final String sql = "SELECT 1 FROM compte WHERE numero=? LIMIT 1";
        try (Connection cn = MysqlData.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
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
        final String sql = "SELECT * FROM compte WHERE id=?";
        try (Connection cn = MysqlData.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapCompte(rs) : null; }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur JDBC findById", e);
        }
    }

    @Override
    public List<Compte> findAll() {
        final String sql = "SELECT * FROM compte ORDER BY id";
        List<Compte> list = new ArrayList<>();
        try (Connection cn = MysqlData.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapCompte(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur JDBC findAll", e);
        }
    }
}
