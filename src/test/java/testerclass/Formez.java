package testerclass;

//package it.refill.testingarea;
//
//
//import it.refill.exe.Constant;
//import static it.refill.exe.Constant.estraiEccezione;
//import static it.refill.exe.Constant.patternITA;
//import static it.refill.exe.Constant.patternSql;
//import it.refill.exe.Db_Bando;
//import static it.refill.otp.SendMailJet.sendMail;
//import java.sql.ResultSet;
//import java.sql.Statement;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.apache.commons.codec.digest.DigestUtils;
//import org.apache.commons.lang3.RandomStringUtils;
//import org.apache.commons.lang3.StringUtils;
//import static org.apache.commons.lang3.StringUtils.remove;
//import static org.apache.commons.lang3.StringUtils.removeEnd;
//import org.joda.time.DateTime;
//
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
///**
// *
// * @author rcosco
// */
//public class Formez {
//
//    private static String host = conf.getString("db.host") + ":3306/enm_gestione_neet";
//
//    private static Logger log = Constant.createLog("FormezFadTest", "/mnt/mcn/test/log/");
//
//    private static String mailsender = "Corso di formazione 0033 PQM";
//
//    private static void fad_allievi(int idprogetti_formativi) {
//        Db_Bando db1 = new Db_Bando(host);
//        try {
//
//            String linkweb = db1.getPath("linkfad");
//            String linknohttpweb = remove(linkweb, "https://");
//            linknohttpweb = remove(linknohttpweb, "http://");
//            linknohttpweb = removeEnd(linknohttpweb, "/");
//
//            String dataoggi = new DateTime().toString(patternSql);
//            String datainvito = new DateTime().toString(patternITA);
//
//            String sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
//                    + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
//                    + " AND mp.id_progettoformativo=" + idprogetti_formativi
//                    + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
//                    + " AND lm.giorno = '" + dataoggi + "' GROUP BY f.nomestanza";
//
//            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
//
//                while (rs1.next()) {
//
//                    String fase = rs1.getString("ud.fase");
//                    String nomestanza = rs1.getString("f.nomestanza");
//                    String ud = rs1.getString("ud.codice");
//                    String sql3;
//                    if (fase.endsWith("A")) {
//                        sql3 = "SELECT idallievi,email,nome,cognome FROM allievi WHERE stato='A' AND idprogetti_formativi = " + idprogetti_formativi;
//                    } else if (fase.endsWith("B")) {
//                        int gruppo_faseB = rs1.getInt("lm.gruppo_faseB");
//                        sql3 = "SELECT idallievi,email,nome,cognome FROM allievi WHERE stato='A' AND idprogetti_formativi = " + idprogetti_formativi + " AND gruppo_faseB = " + gruppo_faseB;
//
//                    } else {
//                        continue;
//                    }
//
//                    System.out.println(sql3);
//
//                    String sql1A = "SELECT lm.orario_start,lm.orario_end FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
//                            + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
//                            + " AND mp.id_progettoformativo=" + idprogetti_formativi
//                            + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
//                            + " AND f.nomestanza = '" + nomestanza + "'"
//                            + " AND lm.giorno = '" + dataoggi + "' ORDER BY lm.orario_start";
//
//                    StringBuilder orainvitosb = new StringBuilder("");
//                    try (Statement st1A = db1.getConnection().createStatement(); ResultSet rs1A = st1A.executeQuery(sql1A)) {
//                        while (rs1A.next()) {
//                            orainvitosb.append(StringUtils.substring(rs1A.getString(1), 0, 5)).append("-").append(StringUtils.substring(rs1A.getString(2), 0, 5)).append("<br>");
//                        }
//                    }
//                    String orainvito = StringUtils.removeEnd(orainvitosb.toString(), "<br>");
//                    try (Statement st3 = db1.getConnection().createStatement(); ResultSet rs3 = st3.executeQuery(sql3)) {
//                        while (rs3.next()) {
//                            String nomecognome = rs3.getString("nome").toUpperCase() + " " + rs3.getString("cognome").toUpperCase();
//                            int idsoggetto = rs3.getInt("idallievi");
//                            String email = rs3.getString("email").toLowerCase();
//
//                            //VERIFICA
//                            String sql4 = "SELECT user FROM fad_access WHERE type='S' "
//                                    + "AND idprogetti_formativi = " + idprogetti_formativi + " "
//                                    + "AND idsoggetto = " + idsoggetto + " "
//                                    + "AND data ='" + dataoggi + "' "
//                                    + "AND ud ='" + ud + "' "
//                                    + "AND room = '" + nomestanza + "'";
//                            try (Statement st4 = db1.getConnection().createStatement(); ResultSet rs4 = st4.executeQuery(sql4)) {
//                                String user = RandomStringUtils.randomAlphabetic(8);
//                                String psw = RandomStringUtils.randomAlphanumeric(6);
//                                String md5psw = DigestUtils.md5Hex(psw);
//                                if (!rs4.next()) {
//                                    try (Statement st5 = db1.getConnection().createStatement()) {
//                                        String ins = "INSERT INTO fad_access VALUES (" + idprogetti_formativi + "," + idsoggetto + ",'" + dataoggi
//                                                + "','S','" + nomestanza + "','" + user + "','" + md5psw + "','" + ud + "')";
//                                        st5.executeUpdate(ins);
//                                    }
//                                    log.log(Level.INFO, "NUOVE CREDENZIALI NEET ) {0}", nomecognome);
//                                } else {
//                                    user = rs4.getString(1);
//                                    try (Statement st5 = db1.getConnection().createStatement()) {
//                                        String upd = "UPDATE fad_access SET psw = '" + md5psw + "' WHERE idsoggetto = " + idsoggetto + " AND data = '" + dataoggi + "' AND ud='" + ud + "' AND type = 'S' ";
//                                        st5.executeUpdate(upd);
//                                    }
//                                    log.log(Level.INFO, "RECUPERO CREDENZIALI NEET ) {0}", nomecognome);
//                                }
//
//                                //INVIO MAIL
//                                String sql5 = "SELECT oggetto,testo FROM email WHERE chiave ='fad_formez'";
//                                try (Statement st5 = db1.getConnection().createStatement();
//                                        ResultSet rs5 = st5.executeQuery(sql5)) {
//                                    if (rs5.next()) {
//                                        String emailtesto = rs5.getString(2);
//                                        String emailoggetto = rs5.getString(1);
//
//                                        emailtesto = StringUtils.replace(emailtesto, "@nomecognome", nomecognome);
//                                        emailtesto = StringUtils.replace(emailtesto, "@username", user);
//                                        emailtesto = StringUtils.replace(emailtesto, "@password", psw);
//                                        emailtesto = StringUtils.replace(emailtesto, "@datainvito", datainvito);
//                                        emailtesto = StringUtils.replace(emailtesto, "@orainvito", orainvito);
//                                        emailtesto = StringUtils.replace(emailtesto, "@nomestanza", nomestanza);
//                                        emailtesto = StringUtils.replace(emailtesto, "@linkweb", linkweb);
//                                        emailtesto = StringUtils.replace(emailtesto, "@linknohttpweb", linknohttpweb);
//
//                                        boolean es = sendMail(mailsender, new String[]{email}, new String[]{"raffaele.cosco@faultless.it"}, emailtesto, emailoggetto, db1, log);
//                                        if (es) {
//                                            log.log(Level.INFO, "MAIL STUDENTE INVIATA A : {0}", email);
//
////                                        break;
//                                        } else {
//                                            log.log(Level.SEVERE, "MAIL NEET ERROR {0}", email);
//                                        }
//                                    }
//                                }
//
//                            }
//
//                        }
//                    }
//
//                }
//            }
//        } catch (Exception e) {
//            log.severe(estraiEccezione(e));
//        }
//        db1.closeDB();
//    }
//
//    private static void fad_docenti(int idprogetti_formativi) {
//        Db_Bando db1 = new Db_Bando(host);
//        try {
//            String dataoggi = new DateTime().toString(patternSql);
//            String datainvito = new DateTime().toString(patternITA);
//
//            String sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice,lm.id_docente FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
//                    + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
//                    + " AND mp.id_progettoformativo=" + idprogetti_formativi
//                    + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
//                    + " AND lm.giorno = '" + dataoggi + "' GROUP BY f.nomestanza";
//
//            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
//                while (rs1.next()) {
//
//                    int id_docente = rs1.getInt("lm.id_docente");
//                    String nomestanza = rs1.getString("f.nomestanza");
//                    String ud = rs1.getString("ud.codice");
//
//                    String sql1A = "SELECT lm.orario_start,lm.orario_end FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
//                            + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
//                            + " AND mp.id_progettoformativo=" + idprogetti_formativi
//                            + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
//                            + " AND f.nomestanza = '" + nomestanza + "'"
//                            + " AND lm.giorno = '" + dataoggi + "' ORDER BY lm.orario_start";
//                    StringBuilder orainvitosb = new StringBuilder("");
//                    try (Statement st1A = db1.getConnection().createStatement(); ResultSet rs1A = st1A.executeQuery(sql1A)) {
//                        while (rs1A.next()) {
//                            orainvitosb.append(StringUtils.substring(rs1A.getString(1), 0, 5)).append("-").append(StringUtils.substring(rs1A.getString(2), 0, 5)).append("<br>");
//                        }
//                    }
//                    String orainvito = StringUtils.removeEnd(orainvitosb.toString(), "<br>");
//
//                    String sql4 = "SELECT iddocenti,email,nome,cognome FROM docenti WHERE iddocenti = " + id_docente;
//                    try (Statement st4 = db1.getConnection().createStatement(); ResultSet rs4 = st4.executeQuery(sql4)) {
//                        if (rs4.next()) {
//                            String nomecognome = rs4.getString("nome").toUpperCase() + " " + rs4.getString("cognome").toUpperCase();
//                            int idsoggetto = rs4.getInt("iddocenti");
//                            String email = rs4.getString("email").toLowerCase();
//                            String sql5 = "SELECT user FROM fad_access WHERE type='D' "
//                                    + "AND idprogetti_formativi = " + idprogetti_formativi + " "
//                                    + "AND idsoggetto = " + idsoggetto + " "
//                                    + "AND data ='" + dataoggi + "' "
//                                    + "AND room = '" + nomestanza + "'";
//                            try (Statement st5 = db1.getConnection().createStatement(); ResultSet rs5 = st5.executeQuery(sql5)) {
//                                String user = RandomStringUtils.randomAlphabetic(8);
//                                String psw = RandomStringUtils.randomAlphanumeric(6);
//                                String md5psw = DigestUtils.md5Hex(psw);
//
//                                if (!rs5.next()) {
//                                    //CREO CREDENZIALI
//                                    try (Statement st6 = db1.getConnection().createStatement()) {
//                                        String ins = "INSERT INTO fad_access VALUES (" + idprogetti_formativi + "," + idsoggetto + ",'" + dataoggi
//                                                + "','D','" + nomestanza + "','" + user + "','" + md5psw + "','" + ud + "')";
//                                        st6.executeUpdate(ins);
//                                    }
//                                    log.log(Level.INFO, "NUOVE CREDENZIALI DOCENTE ) {0}", nomecognome);
//                                } else { //CREDENZIALI GIA presenti
//                                    user = rs5.getString(1);
//                                    try (Statement st6 = db1.getConnection().createStatement()) {
//                                        String upd = "UPDATE fad_access SET psw = '" + md5psw + "' WHERE idsoggetto = " + idsoggetto + " AND data = '" + dataoggi + "' AND ud='" + ud
//                                                + "' AND type = 'D' ";
//                                        st6.executeUpdate(upd);
//                                    }
//                                    log.log(Level.INFO, "RECUPERO CREDENZIALI DOCENTE ) {0}", nomecognome);
//
//                                }
//
//                                //INVIO MAIL
//                                String sql6 = "SELECT oggetto,testo FROM email WHERE chiave ='fad_formez_DOCENTE'";
//                                try (Statement st6 = db1.getConnection().createStatement(); ResultSet rs6 = st6.executeQuery(sql6)) {
//                                    if (rs6.next()) {
//                                        String emailtesto = rs6.getString(2);
//                                        String emailoggetto = rs6.getString(1);
//
//                                        String linkweb = db1.getPath("linkfad");
//                                        String linknohttpweb = remove(linkweb, "https://");
//                                        linknohttpweb = remove(linknohttpweb, "http://");
//                                        linknohttpweb = removeEnd(linknohttpweb, "/");
////
//                                        emailtesto = StringUtils.replace(emailtesto, "@nomecognome", nomecognome);
//                                        emailtesto = StringUtils.replace(emailtesto, "@username", user);
//                                        emailtesto = StringUtils.replace(emailtesto, "@password", psw);
//                                        emailtesto = StringUtils.replace(emailtesto, "@datainvito", datainvito);
//                                        emailtesto = StringUtils.replace(emailtesto, "@orainvito", orainvito);
//                                        emailtesto = StringUtils.replace(emailtesto, "@nomestanza", nomestanza);
//                                        emailtesto = StringUtils.replace(emailtesto, "@linkweb", linkweb);
//                                        emailtesto = StringUtils.replace(emailtesto, "@linknohttpweb", linknohttpweb);
////
////                                        boolean es = sendMail(mailsender, new String[]{"raffaele.cosco@faultless.it"}, new String[]{}, emailtesto, emailoggetto, db1, log);
//                                        boolean es = sendMail(mailsender, new String[]{email}, new String[]{"raffaele.cosco@faultless.it"}, emailtesto, emailoggetto, db1, log);
//                                        if (es) {
//                                            log.log(Level.INFO, "MAIL DOCENTE INVIATA A : {0}", email);
//                                        } else {
//                                            log.log(Level.SEVERE, "MAIL DOCENTE ERROR {0}", email);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.severe(estraiEccezione(e));
//        }
//        db1.closeDB();
//    }
//
//    public static void main(String[] args) {
////        fad_allievi(85);
//        fad_docenti(85);
//    }
//
//}
