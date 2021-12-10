package testerclass;


import it.refill.exe.Db_Bando;
import it.refill.exe.DeD_gestione;
import it.refill.exe.Neet_gestione;
import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Administrator
 */
public class SostituisciModello {

    public static void main(String[] args) {
        try {
            //PARAMETRI
            int idmodello = 36;

            boolean testing = false;
            boolean neet = false;

            //SAVE
//            String sql = "SELECT modello FROM tipo_documenti WHERE idtipo_documenti=" + idmodello;
//            if (neet) {
//
//                Neet_gestione ne = new Neet_gestione(testing);
//                Db_Bando db1 = new Db_Bando(ne.host);
//                try (Statement st = db1.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
//                    if (rs.next()) {
//                        FileUtils.writeByteArrayToFile(new File("C:\\mnt\\mcn\\test\\Modello_" + idmodello + "_NE.pdf"), Base64.decodeBase64(rs.getString(1)));
//                    }
//                }
//                db1.closeDB();
//
//            } else {
//                DeD_gestione de = new DeD_gestione(testing);
//                Db_Bando db1 = new Db_Bando(de.host);
//                try (Statement st = db1.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
//                    if (rs.next()) {
//                        FileUtils.writeByteArrayToFile(new File("C:\\mnt\\mcn\\test\\Modello_" + idmodello + "_DD.pdf"), Base64.decodeBase64(rs.getString(1)));
//                    }
//                }
//                db1.closeDB();
//            }

////            //UPDATE
            File pdf = new File("C:\\Users\\Administrator\\Desktop\\da caricare\\DED_Modello_8_Esito_Valutazione.pdf");
            String update = "UPDATE tipo_documenti SET modello = '" + Base64.encodeBase64String(FileUtils.readFileToByteArray(pdf))
                    + "' WHERE idtipo_documenti=" + idmodello;
            if (neet) {
                Neet_gestione ne = new Neet_gestione(testing);
                Db_Bando db1 = new Db_Bando(ne.host);
                try (Statement st = db1.getConnection().createStatement()) {
                    int x = st.executeUpdate(update);
                    System.out.println("NEET UPDATE MODELLO ID " + idmodello + " -*- " + (x > 0));
                }
                db1.closeDB();
            } else {
                DeD_gestione de = new DeD_gestione(testing);
                Db_Bando db1 = new Db_Bando(de.host);
                try (Statement st = db1.getConnection().createStatement()) {
                    int x = st.executeUpdate(update);
                    System.out.println("DED UPDATE MODELLO ID " + idmodello + " -*- " + (x > 0));
                }
                db1.closeDB();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
