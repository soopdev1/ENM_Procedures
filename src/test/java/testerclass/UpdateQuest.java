/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package testerclass;

import static rc.so.exe.Constant.conf;
import rc.so.exe.Db_Bando;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Statement;
import java.util.stream.Stream;

/**
 *
 * @author Administrator
 */
public class UpdateQuest {

    public static void main(String[] args) {

        try {
            String field = "surveyout";
            File txt = new File("C:\\Users\\Administrator\\Desktop\\da caricare\\ded_out.txt");

            String host_DED = conf.getString("db.host") + ":3306/enm_gestione_dd_prod";
//            String host_NEET = conf.getString("db.host") + ":3306/enm_gestione_neet_prod";

            Db_Bando db0 = new Db_Bando(host_DED);
            try (Stream<String> stream = Files.lines(Paths.get(txt.getPath()))) {
                stream.forEach(mail -> {
                    try {
                        String update = "UPDATE allievi SET " + field + " = 1 WHERE email='" + mail + "'";
                        try (Statement st = db0.getConnection().createStatement()) {
                            int x = st.executeUpdate(update);
                            System.out.println(update + " OK " + (x > 0));
                        }
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                    }
                });
            }
            db0.closeDB();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
