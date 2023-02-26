/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.exe;

import com.google.common.base.Splitter;
import static it.refill.exe.Constant.bando_SE;
import static it.refill.exe.Constant.bando_SUD;
import static it.refill.exe.Constant.calcoladurata;
import static it.refill.exe.Constant.conf;
import static it.refill.exe.Constant.estraiEccezione;
import static it.refill.exe.Constant.formatStatoDocente;
import static it.refill.exe.Constant.getCell;
import static it.refill.exe.Constant.getRow;
import static it.refill.exe.Constant.no_agenvolazione;
import static it.refill.exe.Constant.parseIntR;
import static it.refill.exe.Constant.patternITA;
import static it.refill.exe.Constant.patternSql;
import static it.refill.exe.Constant.setCell;
import static it.refill.exe.Constant.timestamp;
import static it.refill.exe.SendMailJet.sendMail;
import it.refill.sso.DbSSO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.remove;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.Years;

public class DeD_gestione {

    ////////////////////////////////////////////////////////////////////////////
    private static final String startroom = "FADMCNDD_";
    public String host;
    boolean test;
    private static final Logger log = Constant.createLog("ProceduraFAD_DD", "/mnt/mcn/test/log/", false);

    ////////////////////////////////////////////////////////////////////////////
    public DeD_gestione(boolean test) {
        this.host = conf.getString("db.host") + ":3306/enm_gestione_dd_prod";
        this.test = test;
        if (test) {
            this.host = conf.getString("db.host") + ":3306/enm_gestione_dd";
        }
        log.log(Level.INFO, "HOST: {0}", this.host);
    }

    ////////////////////////////////////////////////////////////////////////////
    public void verifica_stanze(int idprogetti_formativi) {
        Db_Bando db1 = new Db_Bando(this.host);
        try {
            String dataoggi = new DateTime().toString(patternSql);
            String sql1 = "SELECT ud.fase,lm.gruppo_faseB FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, progetti_formativi pf"
                    + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                    + " AND mp.id_progettoformativo=" + idprogetti_formativi
                    + " AND pf.idprogetti_formativi=mp.id_progettoformativo AND ((pf.stato='ATA' AND ud.fase='Fase A') OR (pf.stato='ATB' AND ud.fase='Fase B'))"
                    + " AND lm.giorno = '" + dataoggi + "' ORDER BY lm.gruppo_faseB,lm.orario_start";
            try ( Statement st1 = db1.getConnection().createStatement();  ResultSet rs1 = st1.executeQuery(sql1)) {
                while (rs1.next()) {
                    String fase = rs1.getString("ud.fase");
                    if (fase.endsWith("A")) {
                        String sql2 = "SELECT nomestanza,stato FROM fad_multi WHERE idprogetti_formativi = " + idprogetti_formativi + " AND numerocorso = 1";
                        try ( Statement st2 = db1.getConnection().createStatement();  ResultSet rs2 = st2.executeQuery(sql2)) {
                            String nomestanza = startroom + idprogetti_formativi + "_A1";
                            if (test) {
                                nomestanza = "TESTING_" + nomestanza;
                            }
                            if (rs2.next()) {//VERIFICO SE ATTIVA
                                nomestanza = rs2.getString("nomestanza");
                                String stato = rs2.getString("stato");
                                if (stato.equals("0")) {
                                    //UPDATE ATTIVA
                                    try ( Statement st3 = db1.getConnection().createStatement()) {
                                        String upd = "UPDATE fad_multi SET stato = '1' WHERE nomestanza = '" + nomestanza + "'";
                                        st3.executeUpdate(upd);
                                    }
                                }
                            } else { //INSERISCO
                                try ( Statement st3 = db1.getConnection().createStatement()) {
                                    String ins = "INSERT INTO fad_multi VALUES ('" + nomestanza + "'," + idprogetti_formativi + ",'1','" + new DateTime().toString("yyyy-MM-dd HH:mm:ss") + "','1')";
                                    st3.executeUpdate(ins);
                                }
                            }
                        }

                    } else if (fase.endsWith("B")) {
                        int gruppo_faseB = rs1.getInt("lm.gruppo_faseB");

                        String sql2 = "SELECT nomestanza,stato FROM fad_multi WHERE idprogetti_formativi = " + idprogetti_formativi + " AND numerocorso = " + gruppo_faseB;

                        try ( Statement st2 = db1.getConnection().createStatement();  ResultSet rs2 = st2.executeQuery(sql2)) {

                            String nomestanza = startroom + idprogetti_formativi + "_B" + gruppo_faseB;
                            if (test) {
                                nomestanza = "TESTING_" + nomestanza;
                            }
                            if (rs2.next()) {//VERIFICO SE ATTIVA
                                nomestanza = rs2.getString("nomestanza");
                                String stato = rs2.getString("stato");
                                if (stato.equals("0")) {
                                    //UPDATE ATTIVA
                                    try ( Statement st3 = db1.getConnection().createStatement()) {
                                        String upd = "UPDATE fad_multi SET stato = '1' WHERE nomestanza = '" + nomestanza + "'";
                                        st3.executeUpdate(upd);
                                    }
                                }
                            } else { //INSERISCO
                                try ( Statement st3 = db1.getConnection().createStatement()) {
                                    String ins = "INSERT INTO fad_multi VALUES ('" + nomestanza + "'," + idprogetti_formativi + ",'" + gruppo_faseB + "','" + new DateTime().toString("yyyy-MM-dd HH:mm:ss") + "','1')";
                                    st3.executeUpdate(ins);
                                }
                            }
                        }
                    }

                }
            }

        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
        db1.closeDB();
    }

    ////////////////////////////////////////////////////////////////////////////
    public void fad_allievi(int idprogetti_formativi, boolean manual) {
        DbSSO dbs = new DbSSO();
        Db_Bando db1 = new Db_Bando(this.host);
        try {
            String mailsender = db1.getPath("mailsender");
            String dataoggi = new DateTime().toString(patternSql);
            String datainvito = new DateTime().toString(patternITA);
            String sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f, progetti_formativi pf"
                    + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                    + " AND mp.id_progettoformativo=" + idprogetti_formativi
                    + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso)"
                    + " AND pf.idprogetti_formativi=f.idprogetti_formativi"
                    + " AND ((pf.stato='ATA' AND ud.fase='Fase A') OR (pf.stato='ATB' AND ud.fase='Fase B'))"
                    + " AND lm.giorno = '" + dataoggi + "' GROUP BY f.nomestanza";

            if (manual) {
                sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                        + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                        + " AND mp.id_progettoformativo=" + idprogetti_formativi
                        + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso)"
                        + " AND lm.giorno = '" + dataoggi + "' GROUP BY f.nomestanza";
            }
            System.out.println(sql1);
            try ( Statement st1 = db1.getConnection().createStatement();  ResultSet rs1 = st1.executeQuery(sql1)) {

                while (rs1.next()) {

                    String fase = rs1.getString("ud.fase");
                    String nomestanza = rs1.getString("f.nomestanza");
                    String ud = rs1.getString("ud.codice");
                    String sql3;
                    if (fase.endsWith("A")) {
                        sql3 = "SELECT idallievi,email,nome,cognome,codicefiscale FROM allievi WHERE id_statopartecipazione='01' AND idprogetti_formativi = " + idprogetti_formativi;
                    } else if (fase.endsWith("B")) {
                        int gruppo_faseB = rs1.getInt("lm.gruppo_faseB");
                        sql3 = "SELECT idallievi,email,nome,cognome,codicefiscale FROM allievi WHERE id_statopartecipazione='01' AND idprogetti_formativi = " + idprogetti_formativi + " AND gruppo_faseB = " + gruppo_faseB;
                    } else {
                        continue;
                    }

                    String sql1A = "SELECT lm.orario_start,lm.orario_end FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                            + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                            + " AND mp.id_progettoformativo=" + idprogetti_formativi
                            + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
                            + " AND f.nomestanza = '" + nomestanza + "'"
                            + " AND lm.giorno = '" + dataoggi + "' ORDER BY lm.orario_start";
                    StringBuilder orainvitosb = new StringBuilder("");
                    try ( Statement st1A = db1.getConnection().createStatement();  ResultSet rs1A = st1A.executeQuery(sql1A)) {
                        while (rs1A.next()) {
                            orainvitosb.append(StringUtils.substring(rs1A.getString(1), 0, 5)).append("-").append(StringUtils.substring(rs1A.getString(2), 0, 5)).append("<br>");
                        }
                    }
                    String orainvito = StringUtils.removeEnd(orainvitosb.toString(), "<br>");
                    try ( Statement st3 = db1.getConnection().createStatement();  ResultSet rs3 = st3.executeQuery(sql3)) {
                        while (rs3.next()) {
                            String codicefiscale = rs3.getString("codicefiscale").toUpperCase();
                            String nomecognome = rs3.getString("nome").toUpperCase() + " " + rs3.getString("cognome").toUpperCase();
                            int idsoggetto = rs3.getInt("idallievi");
                            String email = rs3.getString("email").toLowerCase();
                            //VERIFICA
                            String sql4 = "SELECT user FROM fad_access WHERE type='S' "
                                    + "AND idprogetti_formativi = " + idprogetti_formativi + " "
                                    + "AND idsoggetto = " + idsoggetto + " "
                                    + "AND data ='" + dataoggi + "' "
                                    + "AND ud ='" + ud + "' "
                                    + "AND room = '" + nomestanza + "'";
                            try ( Statement st4 = db1.getConnection().createStatement();  ResultSet rs4 = st4.executeQuery(sql4)) {
                                String user = RandomStringUtils.randomAlphabetic(8);
                                String psw = RandomStringUtils.randomAlphanumeric(6);
                                String md5psw = DigestUtils.md5Hex(psw);
                                if (!rs4.next()) {
                                    try ( Statement st5 = db1.getConnection().createStatement()) {
                                        String ins = "INSERT INTO fad_access VALUES (" + idprogetti_formativi + "," + idsoggetto + ",'" + dataoggi
                                                + "','S','" + nomestanza + "','" + user + "','" + md5psw + "','" + ud + "')";
                                        st5.executeUpdate(ins);
                                        String ins_SSO = "INSERT INTO fad_access VALUES (" + idprogetti_formativi + "," + idsoggetto + ",'" + dataoggi
                                                + "','S','" + nomestanza + "','" + user + "','" + md5psw + "','" + codicefiscale + "')";
                                        log.log(Level.INFO, "SSO ALLIEVO ) {0} : {1}", new Object[]{nomecognome, dbs.executequery(ins_SSO)});
                                        log.log(Level.INFO, "NUOVE CREDENZIALI ALLIEVO ) {0}", nomecognome);
                                    }
                                } else {
                                    user = rs4.getString(1);
                                    try ( Statement st5 = db1.getConnection().createStatement()) {
                                        String upd = "UPDATE fad_access SET psw = '" + md5psw + "' WHERE idsoggetto = " + idsoggetto + " AND data = '" + dataoggi + "' AND ud='" + ud + "' AND type = 'S' ";
                                        st5.executeUpdate(upd);
                                        log.log(Level.INFO, "SSO ALLIEVO ) {0} : {1}", new Object[]{nomecognome, dbs.executequery(upd)});
                                        log.log(Level.INFO, "RECUPERO CREDENZIALI ALLIEVO ) {0}", nomecognome);
                                    }
                                }

                                //INVIO MAIL
                                String sql5 = "SELECT oggetto,testo FROM email WHERE chiave ='fad3.0'";
                                try ( Statement st5 = db1.getConnection().createStatement();  ResultSet rs5 = st5.executeQuery(sql5)) {
                                    if (rs5.next()) {
                                        String emailtesto = rs5.getString(2);
                                        String emailoggetto = rs5.getString(1);
                                        String linkweb = db1.getPath("linkfad");
                                        String linknohttpweb = remove(linkweb, "https://");
                                        linknohttpweb = remove(linknohttpweb, "http://");
                                        linknohttpweb = removeEnd(linknohttpweb, "/");

                                        emailtesto = StringUtils.replace(emailtesto, "@nomecognome", nomecognome);
                                        emailtesto = StringUtils.replace(emailtesto, "@username", user);
                                        emailtesto = StringUtils.replace(emailtesto, "@password", psw);
                                        emailtesto = StringUtils.replace(emailtesto, "@datainvito", datainvito);

                                        emailtesto = StringUtils.replace(emailtesto, "@orainvito", orainvito);
                                        emailtesto = StringUtils.replace(emailtesto, "@nomestanza", nomestanza);
                                        emailtesto = StringUtils.replace(emailtesto, "@linkweb", linkweb);
                                        emailtesto = StringUtils.replace(emailtesto, "@linknohttpweb", linknohttpweb);
                                        boolean es = sendMail(mailsender, new String[]{email}, new String[]{}, emailtesto, emailoggetto, db1, log);
                                        if (es) {
                                            log.log(Level.INFO, "MAIL ALLIEVO INVIATA A : {0}", email);
                                        } else {
                                            log.log(Level.SEVERE, "MAIL ALLIEVO ERROR {0}", email);
                                        }
                                    }
                                }

                            }

                        }
                    }

                }
            }
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
        db1.closeDB();
        dbs.closeDB();
    }

    ////////////////////////////////////////////////////////////////////////////
    public void fad_docenti(int idprogetti_formativi, boolean manual) {
        DbSSO dbs = new DbSSO();
        Db_Bando db1 = new Db_Bando(this.host);
        try {
            String mailsender = db1.getPath("mailsender");
            String dataoggi = new DateTime().toString(patternSql);
            String datainvito = new DateTime().toString(patternITA);

            String sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice,lm.id_docente FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f, progetti_formativi pf"
                    + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                    + " AND mp.id_progettoformativo=" + idprogetti_formativi
                    + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso)"
                    + " AND pf.idprogetti_formativi=f.idprogetti_formativi AND ((pf.stato='ATA' AND ud.fase='Fase A') OR (pf.stato='ATB' AND ud.fase='Fase B'))"
                    + " AND lm.giorno = '" + dataoggi + "' GROUP BY lm.id_docente,f.nomestanza";
            if (manual) {
                sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice,lm.id_docente FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                        + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                        + " AND mp.id_progettoformativo=" + idprogetti_formativi
                        + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso)"
                        + " AND lm.giorno = '" + dataoggi + "' GROUP BY lm.id_docente,f.nomestanza";
            }
            try ( Statement st1 = db1.getConnection().createStatement();  ResultSet rs1 = st1.executeQuery(sql1)) {
                while (rs1.next()) {

                    int id_docente = rs1.getInt("lm.id_docente");
                    String nomestanza = rs1.getString("f.nomestanza");
                    String ud = rs1.getString("ud.codice");

                    String sql1A = "SELECT lm.orario_start,lm.orario_end FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                            + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                            + " AND mp.id_progettoformativo=" + idprogetti_formativi
                            + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
                            + " AND f.nomestanza = '" + nomestanza + "'"
                            + " AND lm.giorno = '" + dataoggi + "' AND lm.id_docente = " + id_docente + " ORDER BY lm.orario_start";
                    StringBuilder orainvitosb = new StringBuilder("");
                    try ( Statement st1A = db1.getConnection().createStatement();  ResultSet rs1A = st1A.executeQuery(sql1A)) {
                        while (rs1A.next()) {
                            orainvitosb.append(StringUtils.substring(rs1A.getString(1), 0, 5)).append("-").append(StringUtils.substring(rs1A.getString(2), 0, 5)).append("<br>");
                        }
                    }
                    String orainvito = StringUtils.removeEnd(orainvitosb.toString(), "<br>");

                    String sql4 = "SELECT iddocenti,email,nome,cognome,codicefiscale FROM docenti WHERE iddocenti = " + id_docente;
                    try ( Statement st4 = db1.getConnection().createStatement();  ResultSet rs4 = st4.executeQuery(sql4)) {
                        if (rs4.next()) {

                            String codicefiscale = rs4.getString("codicefiscale").toUpperCase();
                            String nomecognome = rs4.getString("nome").toUpperCase() + " " + rs4.getString("cognome").toUpperCase();
                            int idsoggetto = rs4.getInt("iddocenti");
                            String email = rs4.getString("email").toLowerCase();
                            String sql5 = "SELECT user FROM fad_access WHERE type='D' "
                                    + "AND idprogetti_formativi = " + idprogetti_formativi + " "
                                    + "AND idsoggetto = " + idsoggetto + " "
                                    + "AND data ='" + dataoggi + "' "
                                    + "AND room = '" + nomestanza + "'";
                            try ( Statement st5 = db1.getConnection().createStatement();  ResultSet rs5 = st5.executeQuery(sql5)) {
                                String user = RandomStringUtils.randomAlphabetic(8);
                                String psw = RandomStringUtils.randomAlphanumeric(6);
                                String md5psw = DigestUtils.md5Hex(psw);

                                if (!rs5.next()) {
                                    //CREO CREDENZIALI
                                    try ( Statement st6 = db1.getConnection().createStatement()) {

                                        String ins = "INSERT INTO fad_access VALUES (" + idprogetti_formativi + "," + idsoggetto + ",'" + dataoggi
                                                + "','D','" + nomestanza + "','" + user + "','" + md5psw + "','" + ud + "')";
                                        st6.executeUpdate(ins);

                                        String ins_SSO = "INSERT INTO fad_access VALUES (" + idprogetti_formativi + "," + idsoggetto + ",'" + dataoggi
                                                + "','D','" + nomestanza + "','" + user + "','" + md5psw + "','" + codicefiscale + "')";
                                        log.log(Level.INFO, "SSO DOCENTE ) {0} : {1}", new Object[]{nomecognome, dbs.executequery(ins_SSO)});

                                        log.log(Level.INFO, "NUOVE CREDENZIALI DOCENTE ) {0}", nomecognome);
                                    }
                                } else { //CREDENZIALI GIA presenti
                                    user = rs5.getString(1);
                                    try ( Statement st6 = db1.getConnection().createStatement()) {
                                        String upd = "UPDATE fad_access SET psw = '" + md5psw + "' WHERE idsoggetto = "
                                                + idsoggetto + " AND data = '" + dataoggi + "' AND ud='" + ud
                                                + "' AND type = 'D' ";
                                        st6.executeUpdate(upd);
                                        log.log(Level.INFO, "SSO DOCENTE ) {0} : {1}", new Object[]{nomecognome, dbs.executequery(upd)});
                                        log.log(Level.INFO, "RECUPERO CREDENZIALI DOCENTE ) {0}", nomecognome);
                                    }

                                }

                                //INVIO MAIL
                                String sql6 = "SELECT oggetto,testo FROM email WHERE chiave ='fad3.0_DOCENTE'";
                                try ( Statement st6 = db1.getConnection().createStatement();  ResultSet rs6 = st6.executeQuery(sql6)) {
                                    if (rs6.next()) {
                                        String emailtesto = rs6.getString(2);
                                        String emailoggetto = rs6.getString(1);

                                        String linkweb = db1.getPath("linkfad");
                                        String linknohttpweb = remove(linkweb, "https://");
                                        linknohttpweb = remove(linknohttpweb, "http://");
                                        linknohttpweb = removeEnd(linknohttpweb, "/");
//
                                        emailtesto = StringUtils.replace(emailtesto, "@nomecognome", nomecognome);
                                        emailtesto = StringUtils.replace(emailtesto, "@username", user);
                                        emailtesto = StringUtils.replace(emailtesto, "@password", psw);
                                        emailtesto = StringUtils.replace(emailtesto, "@datainvito", datainvito);
                                        emailtesto = StringUtils.replace(emailtesto, "@orainvito", orainvito);
                                        emailtesto = StringUtils.replace(emailtesto, "@nomestanza", nomestanza);
                                        emailtesto = StringUtils.replace(emailtesto, "@linkweb", linkweb);
                                        emailtesto = StringUtils.replace(emailtesto, "@linknohttpweb", linknohttpweb);
//
                                        boolean es = sendMail(mailsender, new String[]{email}, new String[]{}, emailtesto, emailoggetto, db1, log);
                                        if (es) {
                                            log.log(Level.INFO, "MAIL DOCENTE INVIATA A : {0}", email);
                                        } else {
                                            log.log(Level.SEVERE, "MAIL DOCENTE ERROR {0}", email);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
        db1.closeDB();
        dbs.closeDB();
    }

    ////////////////////////////////////////////////////////////////////////////
    public void fad_ospiti(int idprogetti_formativi, boolean manual) {
        Db_Bando db1 = new Db_Bando(this.host);
        try {
            String mailsender = db1.getPath("mailsender");
            String dataoggi = new DateTime().toString(patternSql);
            String datainvito = new DateTime().toString(patternITA);

            String sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice,lm.id_docente FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f, progetti_formativi pf"
                    + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                    + " AND mp.id_progettoformativo=" + idprogetti_formativi
                    + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
                    + " AND pf.idprogetti_formativi=f.idprogetti_formativi AND ((pf.stato='ATA' AND ud.fase='Fase A') OR (pf.stato='ATB' AND ud.fase='Fase B'))"
                    + " AND lm.giorno = '" + dataoggi + "' GROUP BY f.nomestanza";
            if (manual) {
                sql1 = "SELECT ud.fase,lm.gruppo_faseB,f.nomestanza,ud.codice,lm.id_docente FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                        + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                        + " AND mp.id_progettoformativo=" + idprogetti_formativi
                        + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
                        + " AND lm.giorno = '" + dataoggi + "' GROUP BY f.nomestanza";
            }
            try ( Statement st1 = db1.getConnection().createStatement();  ResultSet rs1 = st1.executeQuery(sql1)) {
                while (rs1.next()) {

                    String nomestanza = rs1.getString("f.nomestanza");
                    String ud = rs1.getString("ud.codice");

                    String sql1A = "SELECT lm.orario_start,lm.orario_end FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud, fad_multi f"
                            + " WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud"
                            + " AND mp.id_progettoformativo=" + idprogetti_formativi
                            + " AND f.idprogetti_formativi=mp.id_progettoformativo AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso)"
                            + " AND f.nomestanza = '" + nomestanza + "'"
                            + " AND lm.giorno = '" + dataoggi + "' ORDER BY lm.orario_start";
                    StringBuilder orainvitosb = new StringBuilder("");
                    try ( Statement st1A = db1.getConnection().createStatement();  ResultSet rs1A = st1A.executeQuery(sql1A)) {
                        while (rs1A.next()) {
                            orainvitosb.append(StringUtils.substring(rs1A.getString(1), 0, 5)).append("-").append(StringUtils.substring(rs1A.getString(2), 0, 5)).append("<br>");
                        }
                    }
                    String orainvito = StringUtils.removeEnd(orainvitosb.toString(), "<br>");

                    String sql4O = "SELECT id_staff, nome, cognome, email FROM staff_modelli WHERE id_progettoformativo = " + idprogetti_formativi;
                    try ( Statement st4 = db1.getConnection().createStatement();  ResultSet rs4 = st4.executeQuery(sql4O)) {
                        while (rs4.next()) {
                            String nomecognome = rs4.getString("nome").toUpperCase() + " " + rs4.getString("cognome").toUpperCase();
                            int idsoggetto = rs4.getInt("id_staff");
                            String email = rs4.getString("email").toLowerCase();
                            String sql5 = "SELECT user FROM fad_access WHERE type='O' "
                                    + "AND idprogetti_formativi = " + idprogetti_formativi + " "
                                    + "AND idsoggetto = " + idsoggetto + " "
                                    + "AND data ='" + dataoggi + "' "
                                    + "AND room = '" + nomestanza + "'";
                            try ( Statement st5 = db1.getConnection().createStatement();  ResultSet rs5 = st5.executeQuery(sql5)) {
                                String user = RandomStringUtils.randomAlphabetic(8);
                                String psw = RandomStringUtils.randomAlphanumeric(6);
                                String md5psw = DigestUtils.md5Hex(psw);
                                if (!rs5.next()) {
                                    //CREO CREDENZIALI
                                    try ( Statement st6 = db1.getConnection().createStatement()) {
                                        String ins = "INSERT INTO fad_access VALUES ("
                                                + idprogetti_formativi + "," + idsoggetto + ",'" + dataoggi
                                                + "','O','" + nomestanza + "','" + user + "','"
                                                + md5psw + "','" + ud + "')";

                                        st6.executeUpdate(ins);
                                    }
                                    log.log(Level.INFO, "NUOVE CREDENZIALI OSPITE ) {0}", nomecognome);
                                } else { //CREDENZIALI GIA presenti
                                    user = rs5.getString(1);
                                    try ( Statement st6 = db1.getConnection().createStatement()) {
                                        String upd = "UPDATE fad_access SET psw = '"
                                                + md5psw + "' WHERE idsoggetto = "
                                                + idsoggetto + " AND data = '"
                                                + dataoggi + "' AND ud='" + ud + "' AND type = 'O'";
                                        st6.executeUpdate(upd);
                                    }
                                    log.log(Level.INFO, "RECUPERO CREDENZIALI OSPITE ) {0}", nomecognome);
                                }
                                //INVIO MAIL
                                String sql6 = "SELECT oggetto,testo FROM email WHERE chiave ='fad3.0'";
                                try ( Statement st6 = db1.getConnection().createStatement();  ResultSet rs6 = st6.executeQuery(sql6)) {
                                    if (rs6.next()) {
                                        String emailtesto = rs6.getString(2);
                                        String emailoggetto = rs6.getString(1);

                                        String linkweb = db1.getPath("linkfad");
                                        String linknohttpweb = remove(linkweb, "https://");
                                        linknohttpweb = remove(linknohttpweb, "http://");
                                        linknohttpweb = removeEnd(linknohttpweb, "/");

                                        emailtesto = StringUtils.replace(emailtesto, "@nomecognome", "OSPITE " + nomecognome);
                                        emailtesto = StringUtils.replace(emailtesto, "@username", user);
                                        emailtesto = StringUtils.replace(emailtesto, "@password", psw);
                                        emailtesto = StringUtils.replace(emailtesto, "@datainvito", datainvito);
                                        emailtesto = StringUtils.replace(emailtesto, "@orainvito", orainvito);
                                        emailtesto = StringUtils.replace(emailtesto, "@nomestanza", nomestanza);
                                        emailtesto = StringUtils.replace(emailtesto, "@linkweb", linkweb);
                                        emailtesto = StringUtils.replace(emailtesto, "@linknohttpweb", linknohttpweb);

                                        boolean es = sendMail(mailsender, new String[]{email}, new String[]{}, emailtesto, emailoggetto, db1, log);
                                        if (es) {
                                            log.log(Level.INFO, "MAIL OSPITE INVIATA A : {0}", email);
                                        } else {
                                            log.log(Level.SEVERE, "MAIL OSPITE ERROR {0}", email);
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
        db1.closeDB();
    }

    ////////////////////////////////////////////////////////////////////////////
    public void fad_gestione() {
        List<Integer> elenco = new ArrayList<>();
        Db_Bando db1 = new Db_Bando(this.host);
        try {
            String sql0 = "SELECT idprogetti_formativi FROM progetti_formativi "
                    + "WHERE CURDATE()>=start AND CURDATE()<=end "
                    + "AND (stato='ATA' OR stato = 'ATB')";
            try ( Statement st0 = db1.getConnection().createStatement();  ResultSet rs0 = st0.executeQuery(sql0)) {
                while (rs0.next()) {
                    elenco.add(rs0.getInt("idprogetti_formativi"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db1.closeDB();
        elenco.forEach(pf -> {
            try {
                log.log(Level.WARNING, "VERIFICA STANZE PF -> {0}", pf);
                this.verifica_stanze(pf);
            } catch (Exception e) {
                log.log(Level.SEVERE, "ERRORE VERIFICA STANZE PF ->{0}", pf);
                log.severe(estraiEccezione(e));
            }
            try {
                log.log(Level.WARNING, "FAD ALLIEVI -> {0}", pf);
                this.fad_allievi(pf, false);
            } catch (Exception e) {
                log.log(Level.SEVERE, "ERRORE FAD ALLIEVI PF ->{0}", pf);
                log.severe(estraiEccezione(e));
            }
            try {
                log.log(Level.WARNING, "FAD DOCENTI -> {0}", pf);
                this.fad_docenti(pf, false);
            } catch (Exception e) {
                log.log(Level.SEVERE, "ERRORE FAD DOCENTI PF ->{0}", pf);
                log.severe(estraiEccezione(e));
            }
            try {
                log.log(Level.WARNING, "FAD OSPITI -> {0}", pf);
                this.fad_ospiti(pf, false);
            } catch (Exception e) {
                log.log(Level.SEVERE, "ERRORE FAD OSPITI PF ->{0}", pf);
                log.severe(estraiEccezione(e));
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////
    public void mail_questionario_USCITA() {

        try {
            Db_Bando db0 = new Db_Bando(this.host);
            String mailsender = db0.getPath("mailsender");
            String questionario1link = db0.getPath("questionario2");
            List<String> lista_id = new ArrayList<>();
            String sql0 = "SELECT DISTINCT(mp.id_progettoformativo) FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud "
                    + "WHERE mp.id_modello=lm.id_modelli_progetto "
                    + "AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud "
                    + "AND ud.codice = 'UD14' "
                    + "AND CURDATE()>DATE_ADD(giorno,INTERVAL 5 DAY) "
                    + "AND CURDATE()<DATE_ADD(giorno,INTERVAL 8 DAY) "
                    + "AND ud.fase = 'Fase A'";
            try ( Statement st0 = db0.getConnection().createStatement();  ResultSet rs0 = st0.executeQuery(sql0)) {
                while (rs0.next()) {
                    lista_id.add(rs0.getString(1));
                }
            }
            String datainvito = new DateTime().toString(Constant.patternITA);
            StringBuilder emailoggetto = new StringBuilder("");
            StringBuilder testomail = new StringBuilder("");
            String sql1 = "SELECT oggetto,testo FROM email WHERE chiave ='questionario2'";
            try ( Statement st1 = db0.getConnection().createStatement();  ResultSet rs1 = st1.executeQuery(sql1)) {
                if (rs1.next()) {
                    emailoggetto.append(rs1.getString(1));
                    testomail.append(rs1.getString(2));
                }
            }

            db0.closeDB();

//            System.out.println(lista_id);
//            if (true) {
//                return false;
//            }
            lista_id.forEach(value -> {

                Db_Bando db1 = new Db_Bando(this.host);
                String idpr = value.split(";")[0];
                try {
//                    //ELENCO ALLIEVI
                    String sql2 = "SELECT sum(totaleorerendicontabili),idutente "
                            + "FROM registro_completo WHERE fase = 'A' AND ruolo LIKE 'ALLIEVO%' "
                            + "AND idprogetti_formativi = " + idpr + " GROUP BY idutente";
                    try ( Statement st2 = db1.getConnection().createStatement();  ResultSet rs2 = st2.executeQuery(sql2)) {
                        while (rs2.next()) {
                            String iduser = rs2.getString(2);
                            long millis = rs2.getLong(1);
                            if (millis >= 129600000) {
                                String sql3 = "SELECT idallievi,email,nome,cognome FROM allievi WHERE id_statopartecipazione='01' AND idallievi = " + iduser + " AND idprogetti_formativi = " + idpr;
                                try ( Statement st3 = db1.getConnection().createStatement();  ResultSet rs3 = st3.executeQuery(sql3)) {
                                    while (rs3.next()) {
                                        String nomecognome = rs3.getString("nome").toUpperCase() + " " + rs3.getString("cognome").toUpperCase();
                                        String emailtesto = testomail.toString();
                                        emailtesto = StringUtils.replace(emailtesto, "@nomecognome", nomecognome);
                                        emailtesto = StringUtils.replace(emailtesto, "@datainvito", datainvito);
                                        emailtesto = StringUtils.replace(emailtesto, "@linkquest", questionario1link + "?ut=" + rs3.getString("idallievi"));

                                        String email = rs3.getString("email").toLowerCase();

                                        boolean es = sendMail(mailsender,
                                                new String[]{email},
                                                new String[]{"lucia.cavola@microcredito.gov.it"},
                                                new String[]{},
                                                emailtesto, emailoggetto.toString(), db1, log);

                                        if (es) {
                                            log.log(Level.INFO, "MAIL QUSTIONARIO INGRESSO INVIATA A : {0}", email);
                                        }
                                    }
                                }
                            } else {
                                System.out.println(iduser + "  ) MINORE DI 36 ORE");
                            }

                        }
                    }
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
                db1.closeDB();
            });
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    public void mail_questionario_INGRESSO() {

        try {

            Db_Bando db0 = new Db_Bando(this.host);
            String mailsender = db0.getPath("mailsender");
            String questionario1link = db0.getPath("questionario1");
            List<String> lista_id = new ArrayList<>();
            String sql0 = "SELECT DISTINCT(mp.id_progettoformativo) FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud "
                    + "WHERE mp.id_modello=lm.id_modelli_progetto "
                    + "AND giorno = CURDATE() "
                    + "AND lc.id_lezionecalendario=lm.id_lezionecalendario AND ud.codice=lc.codice_ud "
                    + "AND ud.fase = 'Fase A' AND lc.lezione < 3 ";
            try ( Statement st0 = db0.getConnection().createStatement();  ResultSet rs0 = st0.executeQuery(sql0)) {
                while (rs0.next()) {
                    lista_id.add(rs0.getString(1));
                }
            }

            String datainvito = new DateTime().toString(Constant.patternITA);

            StringBuilder emailoggetto = new StringBuilder("");
            StringBuilder testomail = new StringBuilder("");
            String sql1 = "SELECT oggetto,testo FROM email WHERE chiave ='questionario1'";
            try ( Statement st1 = db0.getConnection().createStatement();  ResultSet rs1 = st1.executeQuery(sql1)) {
                if (rs1.next()) {
                    emailoggetto.append(rs1.getString(1));
                    testomail.append(rs1.getString(2));
                }
            }

            db0.closeDB();

            lista_id.forEach(value -> {

                Db_Bando db1 = new Db_Bando(this.host);
                String idpr = value.split(";")[0];
                try {
                    //ELENCO ALLIEVI
                    String sql3 = "SELECT idallievi,email,nome,cognome FROM allievi WHERE id_statopartecipazione='01' AND idprogetti_formativi = " + idpr;
                    try ( Statement st3 = db1.getConnection().createStatement();  ResultSet rs3 = st3.executeQuery(sql3)) {
                        while (rs3.next()) {
                            String nomecognome = rs3.getString("nome").toUpperCase() + " " + rs3.getString("cognome").toUpperCase();
                            String emailtesto = testomail.toString();
                            emailtesto = StringUtils.replace(emailtesto, "@nomecognome", nomecognome);
                            emailtesto = StringUtils.replace(emailtesto, "@datainvito", datainvito);
                            emailtesto = StringUtils.replace(emailtesto, "@linkquest", questionario1link + "?ut=" + rs3.getString("idallievi"));

                            String email = rs3.getString("email").toLowerCase();

                            boolean es = sendMail(mailsender,
                                    new String[]{email},
                                    new String[]{"lucia.cavola@microcredito.gov.it"},
                                    new String[]{},
                                    emailtesto, emailoggetto.toString(), db1, log);
                            if (es) {
                                log.log(Level.INFO, "MAIL QUSTIONARIO INGRESSO INVIATA A : {0}", email);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }

                db1.closeDB();
            });
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    public void mail_remind(int day) {

        try {
            Db_Bando db0 = new Db_Bando(this.host);
            String mailsender = db0.getPath("mailsender");
            String mailmcn = db0.getPath("email");
            List<String> lista_id = new ArrayList<>();
            StringBuilder datainvito = new StringBuilder("");
            String sql0
                    = "SELECT mp.id_progettoformativo,lm.id_docente, DATE_ADD(CURDATE(), INTERVAL " + day + " DAY) AS datainvito FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, unita_didattiche ud "
                    + "WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario "
                    + "AND ud.codice=lc.codice_ud AND ud.fase = 'Fase A' AND lm.giorno = DATE_ADD(CURDATE(), INTERVAL " + day + " DAY) AND lc.lezione = 1 GROUP BY mp.id_progettoformativo,lm.id_docente";
            try ( Statement st0 = db0.getConnection().createStatement();  ResultSet rs0 = st0.executeQuery(sql0)) {
                int i = 0;
                while (rs0.next()) {
                    if (i == 0) {
                        datainvito.append(new DateTime(rs0.getDate("datainvito").getTime()).toString("dd/MM/yyyy"));
                        i++;
                    }
                    lista_id.add(rs0.getInt(1) + ";" + rs0.getInt(2));
                }
            }

            StringBuilder emailoggetto = new StringBuilder("");
            StringBuilder testomail = new StringBuilder("");
            String sql1 = "SELECT oggetto,testo FROM email WHERE chiave ='fad3.0_avviso'";
            try ( Statement st1 = db0.getConnection().createStatement();  ResultSet rs1 = st1.executeQuery(sql1)) {
                if (rs1.next()) {
                    emailoggetto.append(rs1.getString(1));
                    testomail.append(rs1.getString(2));
                }
            }
            db0.closeDB();

//            System.out.println(lista_id);
//
//            if (true) {
//                return;
//            }
            lista_id.forEach(value -> {
                Db_Bando db1 = new Db_Bando(this.host);
                String idpr = value.split(";")[0];
                String id_docente = value.split(";")[1];
                String mailsa = "";
                try {
                    //EMAIL SA
                    String sql2 = "SELECT email from soggetti_attuatori sa, progetti_formativi pf WHERE sa.idsoggetti_attuatori=pf.idsoggetti_attuatori AND pf.idprogetti_formativi = " + idpr;
                    try ( Statement st2 = db1.getConnection().createStatement();  ResultSet rs2 = st2.executeQuery(sql2)) {
                        if (rs2.next()) {
                            mailsa = rs2.getString(1);
                        }
                    }

                    //ELENCO ALLIEVI
                    String sql3 = "SELECT idallievi,email,nome,cognome FROM allievi WHERE id_statopartecipazione='01' AND idprogetti_formativi = " + idpr;
                    try ( Statement st3 = db1.getConnection().createStatement();  ResultSet rs3 = st3.executeQuery(sql3)) {
                        while (rs3.next()) {
                            String nomecognome = rs3.getString("nome").toUpperCase() + " " + rs3.getString("cognome").toUpperCase();
                            String emailtesto = testomail.toString();
                            emailtesto = StringUtils.replace(emailtesto, "@nomecognome", nomecognome);
                            emailtesto = StringUtils.replace(emailtesto, "@datainvito", datainvito.toString());
                            String email = rs3.getString("email").toLowerCase();
                            boolean es = sendMail(mailsender, new String[]{email}, new String[]{mailsa}, new String[]{mailmcn},
                                    emailtesto, emailoggetto.toString(), db1, log);
                            if (es) {
                                log.log(Level.INFO, "MAIL AVVISO {0} GIORNI PRIMA INVIATA A : {1}", new Object[]{day, email});
                            }

                        }
                    }
                    String sql4 = "SELECT iddocenti,email,nome,cognome FROM docenti WHERE iddocenti = " + id_docente;
                    try ( Statement st4 = db1.getConnection().createStatement();  ResultSet rs4 = st4.executeQuery(sql4)) {
                        while (rs4.next()) {
                            String nomecognome = rs4.getString("nome").toUpperCase() + " " + rs4.getString("cognome").toUpperCase();
                            String emailtesto = testomail.toString();
                            emailtesto = StringUtils.replace(emailtesto, "@nomecognome", "DOCENTE " + nomecognome);
                            emailtesto = StringUtils.replace(emailtesto, "@datainvito", datainvito.toString());
                            String email = rs4.getString("email").toLowerCase();
                            boolean es = sendMail(mailsender, new String[]{email}, new String[]{mailsa}, new String[]{mailmcn},
                                    emailtesto, emailoggetto.toString(), db1, log);
                            if (es) {
                                log.log(Level.INFO, "MAIL AVVISO {0} GIORNI PRIMA INVIATA A DOCENTE: {1}", new Object[]{day, email});
                            }
                        }
                    }
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
                db1.closeDB();
            });
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

    ////////////////////////////////////////////////////////////////////////////    
    public int get_allievi_conformi(int idpr, Db_Bando db1) {
        int out = 0;
        try {
            String sql = "SELECT c.tot_output_conformi FROM progetti_formativi p , checklist_finale c WHERE p.id_checklist_finale=c.id AND p.idprogetti_formativi = " + idpr;
            try ( ResultSet rs = db1.getConnection().createStatement().executeQuery(sql)) {
                if (rs.next()) {
                    out = rs.getInt(1);
                }
            }
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
            out = 0;
        }
        return out;
    }

    public int get_allievi_accreditati_36orefasea(int idpr, Db_Bando db1) {
        int out = 0;
        Long hh36 = new Long(129600000);
        try {
            String sql = "SELECT SUM(r.totaleorerendicontabili) "
                    + " FROM registro_completo r "
                    + " WHERE r.idprogetti_formativi = " + idpr
                    + " AND r.ruolo LIKE 'ALLIEVO%' AND r.fase = 'A' "
                    + " AND r.idutente IN (SELECT a.idallievi FROM allievi a WHERE a.idprogetti_formativi= " + idpr
                    + " AND a.id_statopartecipazione='01') "
                    + " GROUP BY r.idutente";
//            String sql = "SELECT SUM(r.totaleorerendicontabili) FROM registro_completo r "
//                    + "WHERE r.idprogetti_formativi = " + idpr + " AND r.ruolo LIKE 'ALLIEVO%' AND r.fase = 'A' GROUP BY r.idutente";
            try ( ResultSet rs = db1.getConnection().createStatement().executeQuery(sql)) {
                while (rs.next()) {
                    if (rs.getLong(1) >= hh36) {
                        out++;
                    }
                }
            }
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
            out = 0;
        }

        return out;
    }

    public int get_allievi_accreditati(int idpr, Db_Bando db1) {
        int out = 0;
        try {
            String sql = "SELECT COUNT(a.idallievi) FROM allievi a WHERE a.id_statopartecipazione='01' AND a.idprogetti_formativi=" + idpr;
            try ( ResultSet rs = db1.getConnection().createStatement().executeQuery(sql)) {
                if (rs.next()) {
                    out = rs.getInt(1);
                }
            }
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
            out = 0;
        }

        return out;
    }

    public void report_pf() {
        try {
            Long hh36 = new Long(129600000);
            SimpleDateFormat sdita = new SimpleDateFormat("dd/MM/yyyy");
            Db_Bando db1 = new Db_Bando(this.host);
            String fileout;
            FileOutputStream outputStream;
            try ( XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(new File("/mnt/mcn/yisu_ded/estrazioni/TEMPLATE_PROGETTI.xlsx")))) {

                //FOGLIO 1
                XSSFSheet sh1 = wb.getSheetAt(0);

                String sql0_foglio1 = "SELECT sa.ragionesociale,sa.piva,co.regione,sa.idsoggetti_attuatori FROM soggetti_attuatori sa, comuni co "
                        + "WHERE co.idcomune=sa.comune";
                AtomicInteger indice1 = new AtomicInteger(2);
                try ( ResultSet rs0 = db1.getConnection().createStatement().executeQuery(sql0_foglio1)) {
                    while (rs0.next()) {
                        int idsa = rs0.getInt("sa.idsoggetti_attuatori");
                        XSSFRow row = getRow(sh1, indice1.get());
                        indice1.addAndGet(1);
                        setCell(getCell(row, 0), rs0.getString("sa.ragionesociale").toUpperCase());
                        setCell(getCell(row, 1), rs0.getString("sa.piva").toUpperCase());
                        setCell(getCell(row, 2), rs0.getString("co.regione").toUpperCase());

                        int docenti = 0;
                        String sql1_foglio1 = "SELECT COUNT(iddocenti) FROM docenti d WHERE stato='A' AND d.idsoggetti_attuatori=" + idsa;
                        try ( ResultSet rs1 = db1.getConnection().createStatement().executeQuery(sql1_foglio1)) {
                            if (rs1.next()) {
                                docenti = rs1.getInt(1);
                            }
                        }

//                        String sql2_foglio1 = "SELECT p.stato,COUNT(DISTINCT(p.idprogetti_formativi)) "
//                                + ",COUNT(DISTINCT(a.idallievi)) FROM progetti_formativi p, allievi a WHERE "
//                                + "p.idsoggetti_attuatori=" + idsa + " AND a.idprogetti_formativi=p.idprogetti_formativi  "
//                                + "GROUP BY p.stato";
                        String sql2_foglio1 = "SELECT p.idprogetti_formativi,p.stato FROM progetti_formativi p WHERE p.idsoggetti_attuatori=" + idsa;

                        int DAVALIDARE_p = 0, DAVALIDARE_a = 0;
                        int PROGRAMMATO_p = 0, PROGRAMMATO_a = 0;
                        int DACONFERMARE_p = 0, DACONFERMARE_a = 0;
                        int FASEA_p = 0, FASEA_a = 0;
                        int FASEB_p = 0, FASEB_a = 0;
                        int SOSPESO_p = 0, SOSPESO_a = 0;
                        int RIGETTATO_p = 0, RIGETTATO_a = 0;
                        int FINEATTIVITA_p = 0, FINEATTIVITA_a = 0;
                        int DAVALIDAREMODELLO6_p = 0, DAVALIDAREMODELLO6_a = 0;
                        int INATTESADIMAPPATURA_p = 0, INATTESADIMAPPATURA_a = 0;
                        int INVERIFICA_p = 0, INVERIFICA_a = 0;
                        int ESITOVERIFICACONCLUSO_p = 0, ESITOVERIFICACONCLUSO_a = 0;
                        int ESITOVERIFICAINVIATO_p = 0, ESITOVERIFICAINVIATO_a = 0;
                        int CONCLUSO_p = 0, CONCLUSO_a = 0;

                        try ( ResultSet rs2 = db1.getConnection().createStatement().executeQuery(sql2_foglio1)) {

                            while (rs2.next()) {
                                String stato = rs2.getString("p.stato");
                                int idpr = rs2.getInt("p.idprogetti_formativi");

//                                int count_progetti = rs2.getInt(2);
//                                int count_allievi = rs2.getInt(3);
                                switch (stato) {

                                    //per i corsi programmati, da confermare, in attuazione Fase A, in attuazione Fase B, sospesi: gli allievi dei corsi devono essere quelli VALIDATI da ENM;
                                    case "DV":
                                        DAVALIDARE_p++;
                                        DAVALIDARE_a += get_allievi_accreditati(idpr, db1);
                                        break;
                                    case "P":
                                        PROGRAMMATO_p++;
                                        PROGRAMMATO_a += get_allievi_accreditati(idpr, db1);
                                        break;
                                    case "DC":
                                        DACONFERMARE_p++;
                                        DACONFERMARE_a += get_allievi_accreditati(idpr, db1);
                                        break;
                                    case "ATA":
                                        FASEA_p++;
                                        FASEA_a += get_allievi_accreditati(idpr, db1);
                                        break;
                                    case "ATB":
                                        FASEB_p++;
                                        FASEB_a += get_allievi_accreditati(idpr, db1);
                                        break;
                                    case "SOA":
                                    case "SOB":
                                        SOSPESO_p++;
                                        SOSPESO_a += get_allievi_accreditati(idpr, db1);
                                        break;
                                    case "DCE":
                                    case "DVE":
                                    case "ATAE":
                                    case "DVAE":
                                    case "ATBE":
                                    case "DVBE":
                                        RIGETTATO_p++;
                                        RIGETTATO_a += get_allievi_accreditati(idpr, db1);
                                        break;
                                    /////////    
                                    //per i corsi in fine attività, da validare Mod 6, in attesa mappatura e in verifica: gli allievi devono essere quelli che hanno svolto almeno 36 ore in Fase A. 
                                    case "F":
                                        FINEATTIVITA_p++;
                                        FINEATTIVITA_a += get_allievi_accreditati_36orefasea(idpr, db1);
                                        break;
                                    case "DVB":
                                        DAVALIDAREMODELLO6_p++;
                                        DAVALIDAREMODELLO6_a += get_allievi_accreditati_36orefasea(idpr, db1);
                                        break;
                                    case "MA":
                                        INATTESADIMAPPATURA_p++;
                                        INATTESADIMAPPATURA_a += get_allievi_accreditati_36orefasea(idpr, db1);
                                        break;
                                    case "IV":
                                        INVERIFICA_p++;
                                        INVERIFICA_a += get_allievi_accreditati_36orefasea(idpr, db1);
                                        break;

                                    // per i corsi in esito verifica concluso e in esito verifica inviato: gli allievi devono essere quelli verificati e validati da ENM a seguito dei controlli finali    
                                    case "CK":
                                        ESITOVERIFICACONCLUSO_p++;
                                        ESITOVERIFICACONCLUSO_a += get_allievi_accreditati_36orefasea(idpr, db1);
                                        break;
                                    case "EVI":
                                        ESITOVERIFICAINVIATO_p++;
                                        ESITOVERIFICAINVIATO_a += get_allievi_accreditati_36orefasea(idpr, db1);
                                        break;
                                    case "CO":
                                        CONCLUSO_p++;
                                        CONCLUSO_a += get_allievi_accreditati_36orefasea(idpr, db1);
                                        break;
                                }
                            }
                        }
                        //NUOVI 3 campi
                        setCell(getCell(row, 3), String.valueOf(FINEATTIVITA_p + DAVALIDAREMODELLO6_p + INATTESADIMAPPATURA_p + INVERIFICA_p + ESITOVERIFICACONCLUSO_p + ESITOVERIFICAINVIATO_p + CONCLUSO_p));
                        setCell(getCell(row, 4), String.valueOf(FINEATTIVITA_a + DAVALIDAREMODELLO6_a + INATTESADIMAPPATURA_a + INVERIFICA_a
                                + ESITOVERIFICACONCLUSO_a + ESITOVERIFICAINVIATO_a + CONCLUSO_a));

                        setCell(getCell(row, 5), String.valueOf(FASEA_p + FASEB_p + SOSPESO_p));
                        setCell(getCell(row, 6), String.valueOf(FASEA_a + FASEB_a + SOSPESO_a));

                        setCell(getCell(row, 7), String.valueOf(DAVALIDARE_p + PROGRAMMATO_p + DACONFERMARE_p));
                        setCell(getCell(row, 8), String.valueOf(DAVALIDARE_a + PROGRAMMATO_a + DACONFERMARE_a));

                        setCell(getCell(row, 9), String.valueOf(docenti));

                        setCell(getCell(row, 10), String.valueOf(DAVALIDARE_p));
                        setCell(getCell(row, 11), String.valueOf(DAVALIDARE_a));

                        setCell(getCell(row, 12), String.valueOf(PROGRAMMATO_p));
                        setCell(getCell(row, 13), String.valueOf(PROGRAMMATO_a));

                        setCell(getCell(row, 14), String.valueOf(DACONFERMARE_p));
                        setCell(getCell(row, 15), String.valueOf(DACONFERMARE_a));

                        setCell(getCell(row, 16), String.valueOf(FASEA_p));
                        setCell(getCell(row, 17), String.valueOf(FASEA_a));

                        setCell(getCell(row, 18), String.valueOf(FASEB_p));
                        setCell(getCell(row, 19), String.valueOf(FASEB_a));

                        setCell(getCell(row, 20), String.valueOf(SOSPESO_p));
                        setCell(getCell(row, 21), String.valueOf(SOSPESO_a));

                        setCell(getCell(row, 22), String.valueOf(RIGETTATO_p));
                        setCell(getCell(row, 23), String.valueOf(RIGETTATO_a));

                        setCell(getCell(row, 24), String.valueOf(FINEATTIVITA_p));
                        setCell(getCell(row, 25), String.valueOf(FINEATTIVITA_a));

                        setCell(getCell(row, 26), String.valueOf(DAVALIDAREMODELLO6_p));
                        setCell(getCell(row, 27), String.valueOf(DAVALIDAREMODELLO6_a));

                        setCell(getCell(row, 28), String.valueOf(INATTESADIMAPPATURA_p));
                        setCell(getCell(row, 29), String.valueOf(INATTESADIMAPPATURA_a));

                        setCell(getCell(row, 30), String.valueOf(INVERIFICA_p));
                        setCell(getCell(row, 31), String.valueOf(INVERIFICA_a));

                        setCell(getCell(row, 32), String.valueOf(ESITOVERIFICACONCLUSO_p));
                        setCell(getCell(row, 33), String.valueOf(ESITOVERIFICACONCLUSO_a));

                        setCell(getCell(row, 34), String.valueOf(ESITOVERIFICAINVIATO_p));
                        setCell(getCell(row, 35), String.valueOf(ESITOVERIFICAINVIATO_a));

                        setCell(getCell(row, 36), String.valueOf(CONCLUSO_p));
                        setCell(getCell(row, 37), String.valueOf(CONCLUSO_a));

                    }

                }

                //FOGLIO 2
                XSSFSheet sh2 = wb.getSheetAt(1);

                String sql0_foglio2 = "SELECT sa.ragionesociale,sa.piva,co.regione,pf.cip,st.descrizione,pf.start,"
                        + "pf.end,pf.idprogetti_formativi,sa.idsoggetti_attuatori,pf.extract "
                        + "FROM progetti_formativi pf, soggetti_attuatori sa, comuni co, stati_progetto st "
                        + "WHERE sa.idsoggetti_attuatori=pf.idsoggetti_attuatori AND co.idcomune=sa.comune "
                        + "AND st.idstati_progetto=pf.stato";

                AtomicInteger indice2 = new AtomicInteger(1);
                try ( ResultSet rs0 = db1.getConnection().createStatement().executeQuery(sql0_foglio2)) {
                    while (rs0.next()) {
                        int idsa = rs0.getInt("sa.idsoggetti_attuatori");
                        XSSFRow row = getRow(sh2, indice2.get());
                        indice2.addAndGet(1);
                        setCell(getCell(row, 0), rs0.getString("sa.ragionesociale").toUpperCase());
                        setCell(getCell(row, 1), rs0.getString("sa.piva").toUpperCase());
                        setCell(getCell(row, 2), rs0.getString("co.regione").toUpperCase());

                        String cip = "";
                        if (rs0.getString("pf.cip") != null) {
                            cip = rs0.getString("pf.cip").toUpperCase();
                        }
                        setCell(getCell(row, 3), cip);
                        setCell(getCell(row, 4), rs0.getString("st.descrizione").toUpperCase());

                        String rendicontato = "NO";
                        if (rs0.getInt("pf.extract") != 0) {
                            switch (rs0.getInt("pf.extract")) {
                                case 1:
                                    rendicontato = "SI";
                                    break;
                                case 2:
                                    rendicontato = "IN ATTESA";
                                    break;
                                default:
                                    rendicontato = "NO";
                            }

                        }
                        setCell(getCell(row, 5), rendicontato);

                        String datainizio = "";
                        if (rs0.getString("pf.start") != null) {
                            datainizio = sdita.format(rs0.getDate("pf.start"));
                        }
                        String datafine = "";
                        if (rs0.getString("pf.end") != null) {
                            datafine = sdita.format(rs0.getDate("pf.end"));
                        }

                        setCell(getCell(row, 6), datainizio);
                        setCell(getCell(row, 7), datafine);

                        AtomicInteger ALLIEVIISCRITTI = new AtomicInteger(0);
                        AtomicInteger ALLIEVIVALIDATI = new AtomicInteger(0);
                        AtomicInteger ALLIEVIMAGGIORE36 = new AtomicInteger(0);

                        int idpr = rs0.getInt("pf.idprogetti_formativi");
                        String sql1_foglio2 = "SELECT a.idallievi,a.id_statopartecipazione FROM allievi a WHERE a.idprogetti_formativi=" + idpr;

                        try ( ResultSet rs1 = db1.getConnection().createStatement().executeQuery(sql1_foglio2)) {
                            while (rs1.next()) {
                                int idallievo = rs1.getInt("a.idallievi");
                                String statoallievo = rs1.getString("a.id_statopartecipazione");
                                if (statoallievo.equals("01")) {
                                    ALLIEVIVALIDATI.addAndGet(1);

                                    AtomicLong of = new AtomicLong(0L);
                                    String sql11 = "SELECT totaleorerendicontabili FROM registro_completo "
                                            + "WHERE idutente = " + idallievo + " AND idprogetti_formativi = " + idpr + " AND idsoggetti_attuatori = " + idsa
                                            + " AND ruolo LIKE 'ALLIEVO%' AND fase = 'A'";

                                    try ( ResultSet rs11 = db1.getConnection().createStatement().executeQuery(sql11)) {
                                        while (rs11.next()) {
                                            of.addAndGet(rs11.getLong("totaleorerendicontabili"));
                                        }
                                    }
                                    if (of.get() >= hh36) {
                                        ALLIEVIMAGGIORE36.addAndGet(1);
                                    }
                                }
                                ALLIEVIISCRITTI.addAndGet(1);
                            }
                        }

                        setCell(getCell(row, 8), String.valueOf(ALLIEVIISCRITTI.get()));
                        setCell(getCell(row, 9), String.valueOf(ALLIEVIVALIDATI.get()));
                        setCell(getCell(row, 10), String.valueOf(ALLIEVIMAGGIORE36.get()));

                    }
                }

//                for (int i = 0; i < 4; i++) {
//                    sh1.autoSizeColumn(i);
//                }
//                for (int i = 0; i < 7; i++) {
//                    sh2.autoSizeColumn(i);
//                }
                fileout = "/mnt/mcn/yisu_ded/estrazioni/Report_pf_" + new DateTime().toString(timestamp) + ".xlsx";
                outputStream = new FileOutputStream(new File(fileout));
                wb.write(outputStream);
            }
            outputStream.close();
            log.log(Level.WARNING, "{0} RILASCIATO CORRETTAMENTE.", fileout);

            String upd = "UPDATE estrazioni SET path = '" + fileout + "' WHERE idestrazione=3";
            db1.getConnection().createStatement().executeUpdate(upd);
            db1.closeDB();
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

    public void report_docenti() {

        try {

            String hostdd = conf.getString("db.host") + ":3306/enm_dd_prod";
            if (test) {
                hostdd = conf.getString("db.host") + ":3306/enm_dd";
            }
            SimpleDateFormat sdita = new SimpleDateFormat("dd/MM/yyyy");
            Db_Bando db1 = new Db_Bando(this.host);
//            String sql0 = "SELECT s.ragionesociale,s.piva,s.protocollo,d.nome,d.cognome,d.codicefiscale,d.fascia,d.stato FROM docenti d, soggetti_attuatori s WHERE d.idsoggetti_attuatori=s.idsoggetti_attuatori";
            String sql0 = "SELECT s.ragionesociale,s.piva,s.protocollo,d.nome,d.cognome,d.codicefiscale,d.fascia,d.stato,d.tipo_inserimento,d.datawebinair,d.motivo FROM docenti d, soggetti_attuatori s WHERE d.idsoggetti_attuatori=s.idsoggetti_attuatori";
            String fileout;
            FileOutputStream outputStream;
            try ( XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(new File("/mnt/mcn/yisu_ded/estrazioni/TEMPLATE DOCENTI.xlsx")))) {
                XSSFSheet sh1 = wb.getSheetAt(0);
                AtomicInteger indice = new AtomicInteger(1);
                try ( ResultSet rs0 = db1.getConnection().createStatement().executeQuery(sql0)) {
                    while (rs0.next()) {
                        XSSFRow row = getRow(sh1, indice.get());
                        setCell(getCell(row, 0), rs0.getString("s.ragionesociale").toUpperCase());
                        setCell(getCell(row, 1), rs0.getString("s.piva").toUpperCase());
                        setCell(getCell(row, 2), rs0.getString("s.protocollo").toUpperCase());
                        setCell(getCell(row, 3), rs0.getString("d.nome").toUpperCase());
                        setCell(getCell(row, 4), rs0.getString("d.cognome").toUpperCase());
                        setCell(getCell(row, 5), rs0.getString("d.codicefiscale").toUpperCase());

                        setCell(getCell(row, 7), rs0.getString("d.fascia").toUpperCase());
                        if (rs0.getString("d.datawebinair") == null) {
                            setCell(getCell(row, 8), "");
                        } else {
                            setCell(getCell(row, 8), sdita.format(rs0.getDate("d.datawebinair")));
                        }
                        setCell(getCell(row, 9), formatStatoDocente(rs0.getString("d.stato").toUpperCase()));
                        if (rs0.getString("d.tipo_inserimento") == null || rs0.getString("d.tipo_inserimento").trim().equals("") || rs0.getString("d.tipo_inserimento").trim().equals("-")) {
                            setCell(getCell(row, 10), "ACCREDITAMENTO");
                            Db_Bando db2 = new Db_Bando(hostdd);
                            String sql2 = "SELECT CONCAT('F',fascia) "
                                    + " FROM allegato_b a , bando_dd_mcn b WHERE a.username=b.username "
                                    + " AND b.protocollo = '" + rs0.getString("s.protocollo") + "' AND a.cf = '" + rs0.getString("d.codicefiscale") + "'";
                            try ( ResultSet rs2 = db2.getConnection().createStatement().executeQuery(sql2)) {
                                if (rs2.next()) {
                                    setCell(getCell(row, 6), rs2.getString(1));
                                } else {
                                    setCell(getCell(row, 6), rs0.getString("d.fascia").toUpperCase());
                                }
                            }
                            db2.closeDB();
                        } else {
                            setCell(getCell(row, 10), rs0.getString("d.tipo_inserimento").toUpperCase());
                            setCell(getCell(row, 6), rs0.getString("d.fascia").toUpperCase());
                        }

                        if (rs0.getString("d.motivo") == null) {
                            setCell(getCell(row, 11), "");
                        } else {
                            setCell(getCell(row, 11), rs0.getString("d.motivo").toUpperCase());
                        }

                        indice.addAndGet(1);
                    }
                }
                for (int i = 0; i < 12; i++) {
                    sh1.autoSizeColumn(i);
                }
                fileout = "/mnt/mcn/yisu_ded/estrazioni/Report_docenti_" + new DateTime().toString(timestamp) + ".xlsx";
                outputStream = new FileOutputStream(new File(fileout));
                wb.write(outputStream);
            }
            outputStream.close();
            log.log(Level.WARNING, "{0} RILASCIATO CORRETTAMENTE.", fileout);

            String upd = "UPDATE estrazioni SET path = '" + fileout + "' WHERE idestrazione=1";
            db1.getConnection().createStatement().executeUpdate(upd);
            db1.closeDB();
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

    public void report_allievi() {

        try {

            SimpleDateFormat sdita = new SimpleDateFormat("dd/MM/yyyy");
            Db_Bando db1 = new Db_Bando(this.host);
            String sql0 = "SELECT a.idallievi,a.idsoggetto_attuatore,a.nome,a.cognome,a.datanascita,a.stato_nascita,a.comune_nascita,a.codicefiscale,a.telefono,a.email,"
                    + "a.indirizzoresidenza,a.civicoresidenza,a.comune_residenza,a.capresidenza,"
                    + "a.indirizzodomicilio,a.civicodomicilio,a.comune_domicilio,a.capdomicilio,"
                    + "a.sesso,a.cittadinanza,a.target,a.titolo_studio,a.idcondizione_lavorativa,"
                    + "a.idcanale,a.motivazione,a.privacy2,a.privacy3,a.id_statopartecipazione,a.idprogetti_formativi,a.data_anpal FROM allievi a;";

            String fileout;
            FileOutputStream outputStream;
            try ( XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(new File("/mnt/mcn/yisu_ded/estrazioni/TEMPLATE ALLIEVI.xlsx")))) {
                XSSFSheet sh1 = wb.getSheetAt(0);
                AtomicInteger indice = new AtomicInteger(1);
                HashMap<Integer, String> lista_istat = new HashMap<>();
//                HashMap<String, String> lista_cittadinanza = new HashMap<>();
                try ( ResultSet rs0 = db1.getConnection().createStatement().executeQuery(sql0)) {
                    while (rs0.next()) {
                        int idallievo = rs0.getInt("a.idallievi");
                        int idsa = rs0.getInt("a.idsoggetto_attuatore");
                        String sa = "";
                        String sql01 = "SELECT ragionesociale FROM soggetti_attuatori WHERE idsoggetti_attuatori = " + idsa;
                        try ( ResultSet rs01 = db1.getConnection().createStatement().executeQuery(sql01)) {
                            if (rs01.next()) {
                                sa = rs01.getString(1).toUpperCase();
                            }
                        }

                        int idpr = rs0.getInt("a.idprogetti_formativi");
                        String nome = rs0.getString("a.nome").toUpperCase();
                        String cognome = rs0.getString("a.cognome").toUpperCase();
                        String data_anpal = rs0.getString("a.data_anpal").trim();

                        String eta = String.valueOf(Years.yearsBetween(new DateTime(rs0.getDate("a.datanascita").getTime()), new DateTime()).getYears());
                        String datanascita = sdita.format(rs0.getDate("a.datanascita"));

                        String statonascita = rs0.getString("a.stato_nascita");
                        if (statonascita.equals("100")) {
                            statonascita = "ITALIA";
                        } else {
                            String sql1 = "SELECT nome FROM nazioni_rc WHERE codicefiscale = '" + statonascita + "'";
                            try ( ResultSet rs1 = db1.getConnection().createStatement().executeQuery(sql1)) {
                                if (rs1.next()) {
                                    statonascita = rs1.getString(1).toUpperCase();
                                }
                            }
                        }

                        String comune_nascita = "";
                        String provincia_nascita = "";
                        String comune_residenza = "";
                        String regione_residenza = "";
                        String provincia_residenza = "";
                        String codice_istat_residenza = "";

                        String comune_domicilio = "";
                        String provincia_domicilio = "";
                        String regione_domicilio = "";
                        String codice_istat_domicilio = "";

                        String sql2 = "SELECT nome,idcomune,nome_provincia,regione FROM comuni WHERE idcomune IN (" + rs0.getInt("a.comune_nascita") + "," + rs0.getInt("a.comune_residenza") + "," + rs0.getInt("a.comune_domicilio") + ")";
                        try ( ResultSet rs2 = db1.getConnection().createStatement().executeQuery(sql2)) {
                            while (rs2.next()) {
                                if (rs2.getInt(2) == rs0.getInt("a.comune_nascita")) {
                                    comune_nascita = rs2.getString(1).toUpperCase();
                                    provincia_nascita = rs2.getString(3).toUpperCase();
                                }
                                if (rs2.getInt(2) == rs0.getInt("a.comune_residenza")) {
                                    comune_residenza = rs2.getString(1).toUpperCase();
                                    provincia_residenza = rs2.getString(3).toUpperCase();
                                    regione_residenza = rs2.getString(4).toUpperCase();

                                    if (lista_istat.get(rs2.getInt(2)) == null) {
                                        String sql2A = "SELECT t.COD_ISTAT FROM comuni c, TC16 t WHERE c.nome=t.DESCRIZIONE_COMUNE AND c.regione=t.DESCRIZIONE_REGIONE AND c.cittadinanza=0 AND c.idcomune =" + rs2.getInt(2);
                                        try ( ResultSet rs2A = db1.getConnection().createStatement().executeQuery(sql2A)) {
                                            if (rs2A.next()) {
                                                codice_istat_residenza = rs2A.getString(1);
                                                lista_istat.put(rs2.getInt(2), rs2A.getString(1));
                                            }
                                        }
                                    } else {
                                        codice_istat_residenza = lista_istat.get(rs2.getInt(2));
                                    }
                                }
                                if (rs2.getInt(2) == rs0.getInt("a.comune_domicilio")) {
                                    comune_domicilio = rs2.getString(1).toUpperCase();
                                    provincia_domicilio = rs2.getString(3).toUpperCase();
                                    regione_domicilio = rs2.getString(4).toUpperCase();

                                    if (lista_istat.get(rs2.getInt(2)) == null) {
                                        String sql2A = "SELECT t.COD_ISTAT FROM comuni c, TC16 t WHERE c.nome=t.DESCRIZIONE_COMUNE AND c.regione=t.DESCRIZIONE_REGIONE AND c.cittadinanza=0 AND c.idcomune =" + rs2.getInt(2);
                                        try ( ResultSet rs2A = db1.getConnection().createStatement().executeQuery(sql2A)) {
                                            if (rs2A.next()) {
                                                codice_istat_domicilio = rs2A.getString(1);
                                                lista_istat.put(rs2.getInt(2), rs2A.getString(1));
                                            }
                                        }
                                    } else {
                                        codice_istat_domicilio = lista_istat.get(rs2.getInt(2));
                                    }

                                }
                            }
                        }

                        String codicefiscale = rs0.getString("a.codicefiscale").toUpperCase();
                        String telefono = rs0.getString("a.telefono").toUpperCase();
                        String email = rs0.getString("a.email").toLowerCase();

                        String indirizzoresidenza = rs0.getString("a.indirizzoresidenza").toUpperCase();
                        String civicoresidenza = rs0.getString("a.civicoresidenza").toUpperCase();
                        String capresidenza = rs0.getString("a.capresidenza").toUpperCase();

                        String indirizzodomicilio = rs0.getString("a.indirizzodomicilio").toUpperCase();
                        String civicodomicilio = rs0.getString("a.civicodomicilio").toUpperCase();
//                    String capdomicilio = rs0.getString("a.capdomicilio").toUpperCase();

                        String domiciliouguale = "NO";
                        boolean checkdomiciliouguale = rs0.getString("a.indirizzodomicilio").equalsIgnoreCase(rs0.getString("a.indirizzoresidenza"))
                                && rs0.getString("a.civicodomicilio").equalsIgnoreCase(rs0.getString("a.civicoresidenza"))
                                && rs0.getInt("a.comune_domicilio") == rs0.getInt("a.comune_residenza");

                        if (checkdomiciliouguale) {
                            domiciliouguale = "SI";
                        }

                        String sesso = rs0.getString("a.sesso").toUpperCase();
                        if (sesso.equals("M")) {
                            sesso = "UOMO";
                        } else if (sesso.equals("F")) {
                            sesso = "DONNA";
                        }
                        String cittadinanza = rs0.getString("a.cittadinanza").toUpperCase();
                        String istat_cittadinanza = rs0.getString("a.cittadinanza").toUpperCase();

                        String sql3 = "SELECT nome,istat FROM nazioni_rc WHERE idnazione = " + cittadinanza;
                        try ( ResultSet rs3 = db1.getConnection().createStatement().executeQuery(sql3)) {
                            if (rs3.next()) {
                                cittadinanza = rs3.getString(1).toUpperCase();
                                istat_cittadinanza = rs3.getString(2).toUpperCase();
                            }
                        }

                        String target = rs0.getString("a.target");
                        if (target.equals("D1")) {
                            target = "Donna inattiva";
                        } else if (target.equals("D2")) {
                            target = "Disoccupato/a di lunga durata";
                        }

                        String titolo_studio = rs0.getString("a.titolo_studio");
                        String sql5 = "SELECT descrizione FROM titoli_studio WHERE codice = '" + titolo_studio + "'";
                        try ( ResultSet rs5 = db1.getConnection().createStatement().executeQuery(sql5)) {
                            if (rs5.next()) {
                                titolo_studio = rs5.getString(1).toUpperCase();
                            }
                        }
                        String condizione_lavorativa = rs0.getString("a.idcondizione_lavorativa");
                        String sql6 = "SELECT descrizione FROM condizione_lavorativa WHERE idcondizione_lavorativa = '" + condizione_lavorativa + "'";
                        try ( ResultSet rs6 = db1.getConnection().createStatement().executeQuery(sql6)) {
                            if (rs6.next()) {
                                condizione_lavorativa = rs6.getString(1).toUpperCase();
                            }
                        }

                        String canale = rs0.getString("a.idcanale");
                        String sql7 = "SELECT descrizione FROM canale WHERE idcanale = '" + canale + "'";
                        try ( ResultSet rs7 = db1.getConnection().createStatement().executeQuery(sql7)) {
                            if (rs7.next()) {
                                canale = rs7.getString(1).toUpperCase();
                            }
                        }

                        String motivazione = rs0.getString("a.motivazione");
                        String sql8 = "SELECT descrizione FROM motivazione WHERE idmotivazione = '" + motivazione + "'";
                        try ( ResultSet rs8 = db1.getConnection().createStatement().executeQuery(sql8)) {
                            if (rs8.next()) {
                                motivazione = rs8.getString(1).toUpperCase();
                            }
                        }

                        String privacy1 = "SI";
                        String privacy2 = rs0.getString("a.privacy2");
                        String privacy3 = rs0.getString("a.privacy3");

                        String statopartecipazione = rs0.getString("a.id_statopartecipazione");
                        String sql9 = "SELECT descrizione FROM stato_partecipazione WHERE codice = '" + statopartecipazione + "'";
                        try ( ResultSet rs9 = db1.getConnection().createStatement().executeQuery(sql9)) {
                            if (rs9.next()) {
                                statopartecipazione = rs9.getString(1).toUpperCase();
                            }
                        }

                        String cip = "";
                        String statopr = "";
                        String dataavvio = "";
                        String datachiusura = "";
                        String assegnazione = "";

                        String sql10 = "SELECT p.cip,p.start,p.END,s.descrizione,p.assegnazione FROM progetti_formativi p, stati_progetto s "
                                + "WHERE p.stato=s.idstati_progetto AND idprogetti_formativi=" + idpr;
                        try ( ResultSet rs10 = db1.getConnection().createStatement().executeQuery(sql10)) {
                            if (rs10.next()) {
                                if (rs10.getString(1) != null) {
                                    cip = rs10.getString(1).toUpperCase();
                                }
                                if (rs10.getDate(2) != null) {
                                    dataavvio = sdita.format(rs10.getDate(2));
                                }
                                if (rs10.getDate(3) != null) {
                                    if (new DateTime().withMillisOfDay(0).isAfter(new DateTime(rs10.getDate(3).getTime()))) {
                                        datachiusura = sdita.format(rs10.getDate(3));
                                    }
                                }
                                if (rs10.getString(4) != null) {
                                    statopr = rs10.getString(4).toUpperCase();
                                }
                                if (rs10.getString(5) != null) {
                                    assegnazione = rs10.getString(5).toUpperCase();
                                }
                            }
                        }

                        AtomicLong of = new AtomicLong(0L);
                        String sql11 = "SELECT totaleorerendicontabili FROM registro_completo "
                                + "WHERE idutente = " + idallievo + " AND idprogetti_formativi = " + idpr + " AND idsoggetti_attuatori = " + idsa
                                + " AND ruolo LIKE 'ALLIEVO%' ORDER BY data";

                        try ( ResultSet rs11 = db1.getConnection().createStatement().executeQuery(sql11)) {
                            while (rs11.next()) {
//                            datachiusura = sdita.format(rs11.getDate("data"));
                                of.addAndGet(rs11.getLong("totaleorerendicontabili"));
                            }
                        }

                        String orefrequenza = calcoladurata(of.get());

                        String domandaammissionepresente = "NO";
                        String formagiuridica = "";
                        String sedeindividuata = "";
                        String dispocolloquio = "";
                        String ideaimpresa = "";
                        String ateco = "";
                        String comunelocalizzazione = "";
                        String regionelocalizzazione = "";
                        String motivazioneatti = "";
                        String fabbisognofinanz = "";
                        String finanzrich = "";
                        String bandose = "";
                        String tipomc = "";
                        String bandorestosud = "";
                        String motivazrestosud = "";
                        String bandoreg = "";
                        String nomebandoreg = "";
                        String motivnobando = "";
                        String punteggio1 = "";
                        String punteggio1_P = "";
                        String punteggio2 = "";
                        String punteggio2_P = "";
                        String punteggio3 = "";
                        String punteggio3_P = "";
                        String punteggio4 = "";
                        String punteggio4_P = "";
                        String punteggioATTR = "";
                        String premialita = "";

                        String sql13 = "SELECT * FROM maschera_m5 where allievo = " + idallievo + " AND progetto_formativo=" + idpr;
                        try ( ResultSet rs13 = db1.getConnection().createStatement().executeQuery(sql13)) {
                            if (rs13.next()) {

                                if (rs13.getInt("domanda_ammissione_presente") == 1) {
                                    domandaammissionepresente = "SI";

                                    String sql13A = "SELECT descrizione FROM formagiuridica WHERE idformagiuridica=" + rs13.getInt("forma_giuridica");
                                    try ( ResultSet rs13A = db1.getConnection().createStatement().executeQuery(sql13A)) {
                                        if (rs13A.next()) {
                                            formagiuridica = rs13A.getString(1).toUpperCase();
                                        }
                                    }

                                    if (rs13.getInt("sede") == 1) {
                                        sedeindividuata = "SI";
                                    } else {
                                        sedeindividuata = "NO";
                                    }

                                    if (rs13.getInt("colloquio") == 1) {
                                        dispocolloquio = "SI";
                                    } else {
                                        dispocolloquio = "NO";
                                    }

                                    ideaimpresa = rs13.getString("idea_impresa").toUpperCase();
                                    ateco = rs13.getString("ateco").toUpperCase();

                                    String sql13B = "SELECT nome,regione FROM comuni WHERE idcomune = " + rs13.getString("comune_localizzazione");

                                    try ( ResultSet rs13B = db1.getConnection().createStatement().executeQuery(sql13B)) {
                                        if (rs13B.next()) {
                                            comunelocalizzazione = rs13B.getString(1).toUpperCase();
                                            regionelocalizzazione = rs13B.getString(2).toUpperCase();
                                        }
                                    }

                                    motivazioneatti = rs13.getString("motivazione").toUpperCase();
                                    fabbisognofinanz = Constant.roundDoubleAndFormatCurrency(rs13.getDouble("fabbisogno_finanziario"));
                                    finanzrich = Constant.roundDoubleAndFormatCurrency(rs13.getDouble("finanziamento_richiesto_agevolazione"));

                                    if (rs13.getInt("bando_se") == 1) {
                                        bandose = "SI";
                                        if (rs13.getString("bando_se_opzione") != null) {
                                            if (bando_SE().get(rs13.getInt("bando_se_opzione")) != null) {
                                                tipomc = bando_SE().get(rs13.getInt("bando_se_opzione")).toUpperCase();
                                            }
                                        }
                                    } else if (rs13.getInt("bando_sud") == 1) {
                                        bandorestosud = "SI";
                                        if (rs13.getString("bando_sud_opzione") != null) {
                                            String bandosudOpzione = rs13.getString("bando_sud_opzione");
                                            List<String> bandosudOpzioneValori = Splitter.on(";").splitToList(bandosudOpzione);
                                            for (int x = 0; x < bandosudOpzioneValori.size(); x++) {
                                                if (bando_SUD().get(parseIntR(bandosudOpzioneValori.get(x).trim())) != null) {
                                                    motivazrestosud += bando_SUD().get(parseIntR(bandosudOpzioneValori.get(x).trim())).toUpperCase() + "; ";
                                                }
                                            }
                                        }
                                    } else if (rs13.getInt("bando_reg") == 1) {
                                        bandoreg = "SI";
                                        nomebandoreg = rs13.getString("bando_reg_nome");
                                    } else if (rs13.getInt("no_agevolazione") == 1) {
                                        if (rs13.getString("no_agevolazione_opzione") != null) {
                                            if (no_agenvolazione().get(rs13.getInt("no_agevolazione_opzione")) != null) {
                                                motivnobando = no_agenvolazione().get(rs13.getInt("no_agevolazione_opzione")).toUpperCase();
                                            }
                                        }
                                    }

                                    try {
                                        List<String> riepilogopunteggi = Splitter.on(";").splitToList(rs13.getString("tabella_valutazionefinale_val"));
                                        for (String compl : riepilogopunteggi) {
                                            List<String> valoriinterni = Splitter.on("=").splitToList(compl);
                                            switch (valoriinterni.get(0)) {
                                                case "1":
                                                    punteggio1 = Constant.roundDoubleAndFormat(new BigDecimal(valoriinterni.get(1)).doubleValue());
                                                    punteggio1_P = Constant.roundDoubleAndFormat(new BigDecimal(valoriinterni.get(2)).doubleValue());
                                                    break;
                                                case "2":
                                                    punteggio2 = Constant.roundDoubleAndFormat(new BigDecimal(valoriinterni.get(1)).doubleValue());
                                                    punteggio2_P = Constant.roundDoubleAndFormat(new BigDecimal(valoriinterni.get(2)).doubleValue());
                                                    break;
                                                case "3":
                                                    punteggio3 = Constant.roundDoubleAndFormat(new BigDecimal(valoriinterni.get(1)).doubleValue());
                                                    punteggio3_P = Constant.roundDoubleAndFormat(new BigDecimal(valoriinterni.get(2)).doubleValue());
                                                    break;
                                                case "4":
                                                    punteggio4 = Constant.roundDoubleAndFormat(new BigDecimal(valoriinterni.get(1)).doubleValue());
                                                    punteggio4_P = Constant.roundDoubleAndFormat(new BigDecimal(valoriinterni.get(2)).doubleValue());
                                                    break;
                                                default:
                                                    break;
                                            }
                                        }

                                        punteggioATTR = Constant.roundDoubleAndFormat(rs13.getDouble("tabella_valutazionefinale_punteggio"));

                                        if (rs13.getInt("tabella_premialita") == 1) {
                                            premialita = "SI";
                                        } else {
                                            premialita = "NO";
                                        }

                                    } catch (Exception e) {

                                    }
                                } else {
                                    domandaammissionepresente = "NO";
                                    premialita = "NO";
                                }

                            }
                        }

                        XSSFRow row = getRow(sh1, indice.get());

                        setCell(getCell(row, 0), String.valueOf(idallievo));
                        setCell(getCell(row, 1), sa);
                        setCell(getCell(row, 2), nome);
                        setCell(getCell(row, 3), cognome);
                        setCell(getCell(row, 4), datanascita);
                        setCell(getCell(row, 5), eta);
                        setCell(getCell(row, 6), statonascita);
                        setCell(getCell(row, 7), comune_nascita);
                        setCell(getCell(row, 8), provincia_nascita);
                        setCell(getCell(row, 9), codicefiscale);
                        setCell(getCell(row, 10), telefono);
                        setCell(getCell(row, 11), email);
                        setCell(getCell(row, 12), indirizzoresidenza);
                        setCell(getCell(row, 13), civicoresidenza);
                        setCell(getCell(row, 14), comune_residenza);
                        setCell(getCell(row, 15), codice_istat_residenza);
                        setCell(getCell(row, 16), capresidenza);
                        setCell(getCell(row, 17), provincia_residenza);
                        setCell(getCell(row, 18), regione_residenza);
                        setCell(getCell(row, 19), domiciliouguale);
                        setCell(getCell(row, 20), indirizzodomicilio);
                        setCell(getCell(row, 21), civicodomicilio);
                        setCell(getCell(row, 22), comune_domicilio);
                        setCell(getCell(row, 23), codice_istat_domicilio);
                        setCell(getCell(row, 24), provincia_domicilio);
                        setCell(getCell(row, 25), regione_domicilio);
                        setCell(getCell(row, 26), sesso);
                        setCell(getCell(row, 27), cittadinanza);
                        setCell(getCell(row, 28), istat_cittadinanza);
                        setCell(getCell(row, 29), target);
                        setCell(getCell(row, 30), titolo_studio);
                        setCell(getCell(row, 31), condizione_lavorativa);
                        setCell(getCell(row, 32), canale);
                        setCell(getCell(row, 33), motivazione);
                        setCell(getCell(row, 34), privacy1);
                        setCell(getCell(row, 35), privacy2);
                        setCell(getCell(row, 36), privacy3);
                        setCell(getCell(row, 37), statopartecipazione);
                        setCell(getCell(row, 38), cip);
                        setCell(getCell(row, 39), statopr);

                        setCell(getCell(row, 40), dataavvio);
                        setCell(getCell(row, 41), datachiusura);
                        setCell(getCell(row, 42), orefrequenza);

                        setCell(getCell(row, 43), domandaammissionepresente);
                        setCell(getCell(row, 44), formagiuridica);
                        setCell(getCell(row, 45), sedeindividuata);
                        setCell(getCell(row, 46), dispocolloquio);
                        setCell(getCell(row, 47), ideaimpresa);
                        setCell(getCell(row, 48), ateco);
                        setCell(getCell(row, 49), comunelocalizzazione);
                        setCell(getCell(row, 50), regionelocalizzazione);
                        setCell(getCell(row, 51), motivazioneatti);
                        setCell(getCell(row, 52), fabbisognofinanz);
                        setCell(getCell(row, 53), finanzrich);
                        setCell(getCell(row, 54), bandose);
                        setCell(getCell(row, 55), tipomc);
                        setCell(getCell(row, 56), bandorestosud);
                        setCell(getCell(row, 57), motivazrestosud.trim());
                        setCell(getCell(row, 58), bandoreg);
                        setCell(getCell(row, 59), nomebandoreg);
                        setCell(getCell(row, 60), motivnobando);
                        setCell(getCell(row, 61), punteggio1);
                        setCell(getCell(row, 62), punteggio1_P);
                        setCell(getCell(row, 63), punteggio2);
                        setCell(getCell(row, 64), punteggio2_P);
                        setCell(getCell(row, 65), punteggio3);
                        setCell(getCell(row, 66), punteggio3_P);
                        setCell(getCell(row, 67), punteggio4);
                        setCell(getCell(row, 68), punteggio4_P);
                        setCell(getCell(row, 69), punteggioATTR);
                        setCell(getCell(row, 70), premialita);

                        setCell(getCell(row, 71), data_anpal);
                        setCell(getCell(row, 72), assegnazione);

                        indice.addAndGet(1);

                    }
                }
                for (int i = 0; i < 72; i++) {
                    sh1.autoSizeColumn(i);
                }
                fileout = "/mnt/mcn/yisu_ded/estrazioni/Report_allievi_" + new DateTime().toString(timestamp) + ".xlsx";
                outputStream = new FileOutputStream(new File(fileout));
                wb.write(outputStream);
            }
            outputStream.close();
            log.log(Level.WARNING, "{0} RILASCIATO CORRETTAMENTE.", fileout);
            String upd = "UPDATE estrazioni SET path = '" + fileout + "' WHERE idestrazione=2";
            db1.getConnection().createStatement().executeUpdate(upd);
            db1.closeDB();
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }

    }

//    public static void main(String[] args) {
//        DeD_gestione ne = new DeD_gestione(false);
//        ne.report_allievi();
//    }
}
