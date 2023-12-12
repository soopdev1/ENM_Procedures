package testerclass;




import static rc.so.exe.Constant.conf;
import rc.so.exe.Db_Bando;
import rc.so.exe.Domande;
import static rc.so.exe.Neet_Engine.bando;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author rcosco
 */
public class Updateexistant {

    public String host;

    public Updateexistant(boolean test) {
        this.host = conf.getString("db.host") + ":3306/enm_neet_prod";
        if (test) {
            this.host = conf.getString("db.host") + ":3306/enm_neet";
        }
        System.out.println("HOST: " + this.host);
    }

    private void update_domande_fase1() {
        Db_Bando db1 = new Db_Bando(this.host);
        try {
            String sql1 = "SELECT username FROM bando_neet_mcn a WHERE stato_domanda = 'A' AND decreto <> '-'";
            Statement st1 = db1.getConnection().createStatement();
            ResultSet rs1 = st1.executeQuery(sql1);
            while (rs1.next()) {
                Domande d1 = new Domande();
//                d1.setCodicedomanda(rs1.getString("id"));
//                d1.setDataconsegna(rs1.getString("dataconsegna"));
//                d1.setStato(rs1.getString("stato"));

                boolean ok = false;
                String sql2 = "SELECT * FROM usersvalori WHERE username= '" + rs1.getString("username") + "'";
                Statement st2 = db1.getConnection().createStatement();
                ResultSet rs2 = st2.executeQuery(sql2);
                while (rs2.next()) {
                    ok = true;
                    String nomecampo = rs2.getString("campo");
                    String valorecampo = rs2.getString("valore").toUpperCase().trim();
                    if (nomecampo.equals("sedecomune")) {
                        d1.setSedeComune(valorecampo);
                    } else if (nomecampo.equals("sedecap")) {
                        d1.setSedeCap(valorecampo);
                    } else if (nomecampo.equals("cell")) {
                        d1.setCellulare(valorecampo);
                    } else if (nomecampo.equals("data")) {
                        d1.setDataNascita(valorecampo);
                    } else if (nomecampo.equals("email")) {
                        d1.setEmail(valorecampo);
                    } else if (nomecampo.equals("sedeindirizzo")) {
                        d1.setSedeIndirizzo(valorecampo);
                    } else if (nomecampo.equals("docric1")) {
                        d1.setNumeroDocumento(valorecampo);
                    } else if (nomecampo.equals("datasc1")) {
                        d1.setScadenzaDoc(valorecampo);
                    } else if (nomecampo.equals("caricasoc")) {
                        d1.setCaricaSoc(valorecampo);
                    }
                }
                rs2.close();
                st2.close();
                if (ok) {
                    String insert = "UPDATE bando_neet_mcn SET sedecomune = ?,sedecap = ?,cellulare = ?,data = ?,mail = ?,sedeindirizzo = ?,docric = ?,scadenzadoc = ?,caricasoc = ?  where username = ?";
                    PreparedStatement ps1 = db1.getConnection().prepareStatement(insert);
                    ps1.setString(1, d1.getSedeComune());
                    ps1.setString(2, d1.getSedeCap());
                    ps1.setString(3, d1.getCellulare());
                    ps1.setString(4, d1.getDataNascita());
                    ps1.setString(5, d1.getEmail());
                    ps1.setString(6, d1.getSedeIndirizzo());
                    ps1.setString(7, d1.getNumeroDocumento());
                    ps1.setString(8, d1.getScadenzaDoc());
                    ps1.setString(9, d1.getCaricaSoc());
                    ps1.setString(10, rs1.getString("username"));
                    ps1.execute();
                    ps1.close();
                    System.out.println("*** " + d1.toString());
                }
            }
            rs1.close();
            st1.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        db1.closeDB();
    }

//    public static void main(String[] args) {
//        new Updateexistant(false).update_domande_fase1();
//    }

}
