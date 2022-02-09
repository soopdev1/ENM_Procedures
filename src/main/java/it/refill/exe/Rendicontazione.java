/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.refill.exe;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import static it.refill.exe.Constant.estraiEccezione;
import static it.refill.report.Excel.prospetto_riepilogo_ded;
import static it.refill.report.Excel.prospetto_riepilogo_neet;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Administrator
 */
public class Rendicontazione {

    public String host;
    private static final Logger log = Constant.createLog("RendicontazioneMCN", "/mnt/mcn/test/log/", true);

    ////////////////////////////////////////////////////////////////////////////
    public Rendicontazione(boolean test, boolean neet) {
        if (neet) {
            this.host = "clustermicrocredito.cluster-c6m6yfqeypv3.eu-south-1.rds.amazonaws.com:3306/enm_gestione_neet_prod";
            if (test) {
                this.host = "clustermicrocredito.cluster-c6m6yfqeypv3.eu-south-1.rds.amazonaws.com:3306/enm_gestione_neet";
            }
        } else {
            this.host = "clustermicrocredito.cluster-c6m6yfqeypv3.eu-south-1.rds.amazonaws.com:3306/enm_gestione_dd_prod";
            if (test) {
                this.host = "clustermicrocredito.cluster-c6m6yfqeypv3.eu-south-1.rds.amazonaws.com:3306/enm_gestione_dd";
            }
        }
        log.log(Level.INFO, "HOST: {0}", this.host);
    }

    public void generaRendicontazione(boolean neet) {

        try {
            Db_Bando db1 = new Db_Bando(this.host);
            Gson gson = new Gson();
            String sql0 = "SELECT e.idestrazione,e.progetti FROM estrazioni e WHERE e.path IS NULL";
            try (Statement st0 = db1.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
                while (rs0.next()) {
                    int idestrazione = rs0.getInt(1);

                    List<Integer> idpr = new ArrayList<>();

                    List<String> progetti = gson.fromJson(rs0.getString(2), new TypeToken<List<String>>() {
                    }.getType());

                    progetti.forEach(cip -> {
                        try {
                            String sql1 = "SELECT e.idprogetti_formativi FROM progetti_formativi e WHERE e.cip= '" + cip + "'";
                            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                                if (rs1.next()) {
                                    idpr.add(rs1.getInt(1));
                                }
                            }
                        } catch (SQLException ex) {
                            log.severe(estraiEccezione(ex));
                        }
                    });

                    Db_Bando dbz = new Db_Bando(this.host);
                    File zipped;

                    if (neet) {
                        zipped = prospetto_riepilogo_neet(idestrazione, idpr, dbz);
                    } else {
                        zipped = prospetto_riepilogo_ded(idestrazione, idpr, dbz);
                    }

                    dbz.closeDB();
                    if (zipped != null) {
                        String update1 = "UPDATE estrazioni SET path = '" + StringUtils.replace(zipped.getPath(), "\\", "/") + "' WHERE idestrazione=" + idestrazione;
                        try (Statement st1 = db1.getConnection().createStatement()) {
                            st1.executeUpdate(update1);
                        }
                        try (Statement st2 = db1.getConnection().createStatement()) {
                            for (int i = 0; i < idpr.size(); i++) {
                                String update2 = "UPDATE progetti_formativi SET extract = 1 WHERE idprogetti_formativi = " + idpr.get(i);
                                st2.executeUpdate(update2);
                            }
                        }
                    }

                }
            }
            db1.closeDB();
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

//    public static void main(String[] args) {
//        Rendicontazione re = new Rendicontazione(false, false);
//        
//        
//        List<Integer> idpr = new ArrayList<>();
//        idpr.add(120);
//        Db_Bando dbz = new Db_Bando(re.host);
//        File zipped;
//
//        zipped = prospetto_riepilogo_ded(1, idpr, dbz);
//
//        dbz.closeDB();
//        
//        System.out.println(zipped.getPath());
//        
//    }
}
