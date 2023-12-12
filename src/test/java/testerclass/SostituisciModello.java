package testerclass;

import rc.so.exe.Db_Bando;
import rc.so.exe.DeD_gestione;
import rc.so.exe.Neet_gestione;
import java.io.File;
import java.sql.Statement;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Administrator
 */
public class SostituisciModello {

    public static void main(String[] args) {
        try {
            //PARAMETRI
            int idmodello = 21;
            String table = "tipo_documenti_allievi";
            String idname = "idtipodocumenti_allievi";
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
            File pdf = new File("C:\\Users\\Administrator\\Desktop\\da caricare\\12.2021_Modello_5_2.pdf");
            String update = "UPDATE " + table + " SET modello = '" 
                    + Base64.encodeBase64String(FileUtils.readFileToByteArray(pdf))
                    + "' WHERE " + idname + "=" + idmodello;
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
