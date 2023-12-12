package testerclass;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import rc.so.exe.Constant;
import rc.so.exe.Db_Bando;
import static rc.so.exe.SendMailJet.sendMail;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

/**
 *
 * @author raf
 */
public class InviaQ {

    private static final Logger log = Constant.createLog("ProceduraSendQ", "/mnt/mcn/test/log/");

//    public static void main(String[] args) {
//
//        String host = conf.getString("db.host") + ":3306/enm_gestione_dd_prod";
//
//        String[] dest = {
//            "claudiacarella4@gmail.com",
//            "pescuma_carolina@libero.it",
//            "chrysoperla@damanhur.org",
//            "venerina.caserta@gmail.com",
//            "irma.fassitti@gmail.com",
//            "carlasusta@gmail.com",
//            "dadina04@hotmail.it",
//            "daniele_davino@libero.it",
//            "rosalinscro@gmail.com",
//            "gessicabarone20@gmail.com",
//            "francescarena88@hotmail.com"
//        };
//
//        try {
//            Db_Bando db0 = new Db_Bando(host);
//            String mailsender = db0.getPath("mailsender");
//            String questionario1link = db0.getPath("questionario2");
//            String datainvito = new DateTime().toString(Constant.patternITA);
//            StringBuilder emailoggetto = new StringBuilder("");
//            StringBuilder testomail = new StringBuilder("");
//            String sql1 = "SELECT oggetto,testo FROM email WHERE chiave ='questionario2'";
//            try (Statement st1 = db0.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
//                if (rs1.next()) {
//                    emailoggetto.append(rs1.getString(1));
//                    testomail.append(rs1.getString(2));
//                }
//            }
//
//            for (String destmail : dest) {
//                String sql2 = "SELECT idallievi,nome,cognome FROM allievi a WHERE a.email='" + destmail + "'";
//                try (Statement st2 = db0.getConnection().createStatement(); ResultSet rs2 = st2.executeQuery(sql2)) {
//                    if (rs2.next()) {
//                        String nomecognome = rs2.getString("nome").toUpperCase() + " " + rs2.getString("cognome").toUpperCase();
//                        String emailtesto = testomail.toString();
//                        emailtesto = StringUtils.replace(emailtesto, "@nomecognome", nomecognome);
//                        emailtesto = StringUtils.replace(emailtesto, "@datainvito", datainvito);
//                        emailtesto = StringUtils.replace(emailtesto, "@linkquest", questionario1link + "?ut=" + rs2.getString("idallievi"));
//                        boolean es = sendMail(mailsender,
//                                new String[]{destmail},
//                                new String[]{"lucia.cavola@microcredito.gov.it"},
//                                new String[]{"raffaele.cosco@faultless.it"},
//                                emailtesto, emailoggetto.toString(), db0, log);
//                        if (es) {
//                            log.log(Level.INFO, "MAIL QUSTIONARIO INGRESSO INVIATA A :{0}", destmail);
//                        } else {
//                            log.log(Level.SEVERE, "{0} ERRORE INVIO", destmail);
//                        }
//                    } else {
//                        log.log(Level.SEVERE, "{0} NON TROVATO ALLIEVO", destmail);
//                    }
//                }
//            }
//            db0.closeDB();
//        } catch (Exception e) {
//            log.severe(Constant.estraiEccezione(e));
//        }
//
//    }
}
