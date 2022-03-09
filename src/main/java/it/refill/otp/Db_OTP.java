/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.otp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 *
 * @author rcosco
 */
public class Db_OTP {

    private Connection conn = null;

    public Db_OTP(boolean test) {

        String driver = "com.mysql.cj.jdbc.Driver";
        String user = "bando";
        String password = "bando";
        String host = "clustermicrocredito.cluster-c6m6yfqeypv3.eu-south-1.rds.amazonaws.com:3306/enm_otp";

        if (test) {
            //host = "clustermicrocredito.cluster-c6m6yfqeypv3.eu-south-1.rds.amazonaws.com:3306/enm_neet";

            driver = "org.mariadb.jdbc.Driver";
            host = "172.31.224.56:3306/enm_otp";
            user = "bando";
            password = "bando";

        }

        try {
            Class.forName(driver).newInstance();
            Properties p = new Properties();
            p.put("user", user);
            p.put("password", password);
            p.put("characterEncoding", "UTF-8");
            p.put("passwordCharacterEncoding", "UTF-8");
            p.put("useSSL", "false");
            p.put("connectTimeout", "1000");
            p.put("useUnicode", "true");
            this.conn = DriverManager.getConnection("jdbc:mysql://" + host, p);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (this.conn != null) {
                try {
                    this.conn.close();
                } catch (Exception ex1) {
                }
            }
            this.conn = null;
        }
    }

    public Db_OTP(Connection conn) {
        try {
            this.conn = conn;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Connection getConnectionDB() {
        return conn;
    }

    public void closeDB() {
        try {
            if (conn != null) {
                this.conn.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean cambiastato(String codProgetto, String user, int idsms, String statodest) {
        try {
            String update = "UPDATE ctrlotp SET stato = '" + statodest + "'"
                    + " WHERE stato='A' "
                    + "AND codprogetto = '" + codProgetto + "' "
                    + "AND user = '" + user + "' "
                    + "AND idsms = " + idsms;
            try (Statement st = this.conn.createStatement()) {
                st.executeUpdate(update);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insOtp(String codProgetto, String user, String codOtp, String numcell, int idsms) {
        boolean out;
        try {
            cambiastato(codProgetto, user, idsms, "KO");
            String upd = "insert into ctrlotp (codprogetto,user,codotp,numcell,idsms) values (?,?,?,?,?)";
            try (PreparedStatement ps = this.conn.prepareStatement(upd)) {
                ps.setString(1, codProgetto);
                ps.setString(2, user);
                ps.setString(3, codOtp);
                ps.setString(4, numcell);
                ps.setInt(5, idsms);
                out = ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            out = false;
        }
        return out;
    }

    public String getSMS(String codprogetto, int codMsg) {
        String msg = null;
        try {
            String sql = "Select msg from sms where codprogetto = ? and idsms = ?";
            try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
                ps.setString(1, codprogetto);
                ps.setInt(2, codMsg);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        msg = rs.getString("msg");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    public boolean isOK(String codprogetto, String user, String otp, int idsms) {
        boolean out = false;
        try {
            String sql = "Select codprogetto from ctrlotp where codprogetto = ? and user = ? and codotp = ? and stato = ? AND idsms = ?";
            try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
                ps.setString(1, codprogetto);
                ps.setString(2, user);
                ps.setString(3, otp);
                ps.setString(4, "A");
                ps.setInt(5, idsms);
                try (ResultSet rs = ps.executeQuery()) {
                    out = rs.next();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public String getPath(String id) {
        String out = null;
        try {
            String sql = "select url from path where id = ?";
            try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        out = rs.getString("url");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }
}
