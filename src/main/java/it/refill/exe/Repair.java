/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.exe;

import static it.refill.exe.Constant.checkPDF;
import static it.refill.exe.Constant.estraiEccezione;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import static org.apache.commons.io.FileUtils.readFileToByteArray;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

/**
 *
 * @author rcosco
 */
public class Repair {

    public String host;
    private boolean test;
    private static Logger log = Constant.createLog("ProceduraMCN", "/mnt/mcn/test/log/", true);

    ////////////////////////////////////////////////////////////////////////////
    public Repair(boolean test, boolean neet) {
        this.test = test;
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

    public void imposta_progetti_finettivita() {

        try {
            Db_Bando db1 = new Db_Bando(this.host);

            String sql0 = "SELECT p.idprogetti_formativi FROM progetti_formativi p WHERE p.stato = 'ATB' AND CURDATE()>p.end";
            try (Statement st0 = db1.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
                while (rs0.next()) {
                    int idpr = rs0.getInt(1);
                    String up0 = "UPDATE progetti_formativi SET stato = 'F' WHERE idprogetti_formativi=" + idpr;
                    try (Statement st = db1.getConnection().createStatement()) {
                        boolean es = st.executeUpdate(up0) > 0;
                        log.log(Level.WARNING, "{0} -- {1}", new Object[]{up0, es});

                    }
                }
            }
            db1.closeDB();
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }

    }

    public void copiadocumentidocenti() {
        try {
            Db_Bando db1 = new Db_Bando(this.host);

            String sql1 = "SELECT d.iddocumenti_progetti,d.iddocente,d.tipo,d.path FROM documenti_progetti d WHERE d.deleted=0 AND d.iddocente IS NOT NULL";

            try (Statement st1 = db1.getConnection().createStatement();
                    ResultSet rs1 = st1.executeQuery(sql1)) {
                while (rs1.next()) {
                    int id_docente = rs1.getInt("d.iddocente");
                    int tipodoc = rs1.getInt("d.tipo");
                    String filepath = rs1.getString("d.path");
                    try {
                        File pdf = new File(filepath);
                        if (!pdf.exists() || !pdf.canRead()) {
                            String sql2 = "SELECT d.curriculum,d.docId FROM docenti d WHERE d.iddocenti=" + id_docente;
                            try (Statement st2 = db1.getConnection().createStatement();
                                    ResultSet rs2 = st2.executeQuery(sql2)) {
                                if (rs2.next()) {
                                    String path_curriculum = rs2.getString("d.curriculum"); //21
                                    String path_doc = rs2.getString("d.docId"); //20
                                    switch (tipodoc) {
                                        case 20:
                                            boolean copy1 = Constant.copyR(path_doc, filepath);
                                            if (!copy1) {
                                                log.log(Level.SEVERE, "ERRORE: {0}  - {1}", new Object[]{path_doc, filepath});
                                            }
                                            break;
                                        case 21:
                                            boolean copy2 = Constant.copyR(path_curriculum, filepath);
                                            if (!copy2) {
                                                log.log(Level.SEVERE, "ERRORE: {0}  - {1}", new Object[]{path_curriculum, filepath});
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.severe(estraiEccezione(e));
                    }
                }
            }

            db1.closeDB();
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

    public void crea_pdf_unico_ANPAL(boolean neet) {
        try {
            List<Integer> elenco = new ArrayList<>();
            Db_Bando db1 = new Db_Bando(this.host);
            try {
                String sql0 = "SELECT p.idprogetti_formativi FROM progetti_formativi p, stati_progetto s "
                        + "WHERE p.stato=s.idstati_progetto AND s.ordine_processo IS NOT NULL AND s.ordine_processo > 7 "
                        + "AND p.pdfunico IS NULL ORDER BY p.idprogetti_formativi";
                try (Statement st0 = db1.getConnection().createStatement();
                        ResultSet rs0 = st0.executeQuery(sql0)) {
                    while (rs0.next()) {
                        elenco.add(rs0.getInt(1));
                    }
                }
            } catch (Exception e) {
                log.severe(estraiEccezione(e));
            }
            db1.closeDB();
            Long hh36 = new Long(129600000);
            elenco.forEach(pf -> {
                List<String> pathfiledaunire = new LinkedList<>();
                Db_Bando db2 = new Db_Bando(this.host);
                try {
                    // Tabella All. B1 e relativo CV e Carta identita' Docente
                    String sql1 = "SELECT d.iddocenti,d.codicefiscale,d.curriculum,d.docid,d.tipo_inserimento,d.richiesta_accr,d.idsoggetti_attuatori,s.piva "
                            + "FROM docenti d,soggetti_attuatori s WHERE d.idsoggetti_attuatori=s.idsoggetti_attuatori AND d.stato='A' "
                            + "AND d.iddocenti IN (SELECT p.iddocenti FROM progetti_docenti p WHERE p.idprogetti_formativi= " + pf + ")";
                    try (Statement st1 = db2.getConnection().createStatement();
                            ResultSet rs1 = st1.executeQuery(sql1)) {
                        while (rs1.next()) {
//                            int SA = rs1.getInt("d.idsoggetti_attuatori");
                            String piva = rs1.getString("s.piva");
                            String cfdocente = rs1.getString("d.codicefiscale");
                            String docid = rs1.getString("d.docid");
                            String curriculum = rs1.getString("d.curriculum");
                            String b1 = "NONE";
                            if (rs1.getString("d.tipo_inserimento") == null) {

                                if (neet) {
                                    String sql2 = "SELECT c.allegatob1 FROM bando_neet_mcn b, allegato_b a, allegato_b1 c "
                                            + "WHERE b.pivacf='" + piva + "' AND a.username=b.username AND a.cf='" + cfdocente + "' "
                                            + "AND c.username=a.username AND a.id=c.idallegato_b1";
                                    String hostbando = StringUtils.remove(this.host, "gestione_");
                                    Db_Bando dbb = new Db_Bando(hostbando);
                                    try (Statement st2 = dbb.getConnection().createStatement();
                                            ResultSet rs2 = st2.executeQuery(sql2)) {
                                        if (rs2.next()) {
                                            b1 = rs2.getString("c.allegatob1");
                                        }
                                    }
                                    dbb.closeDB();
                                } else {
                                    String hostbando = StringUtils.remove(this.host, "gestione_");
                                    String sql2_A = "SELECT b.accreditato FROM bando_dd_mcn b WHERE b.pivacf='" + piva + "'";
                                    Db_Bando db_dd = new Db_Bando(hostbando);
                                    try (Statement st2_A = db_dd.getConnection().createStatement();
                                            ResultSet rs2_A = st2_A.executeQuery(sql2_A)) {
                                        if (rs2_A.next()) {
                                            if (rs2_A.getString(1).equals("SI")) { //NEET
                                                String hostbandoneet = StringUtils.replace(this.host, "gestione_dd", "neet");
                                                String sql2 = "SELECT c.allegatob1 FROM bando_neet_mcn b, allegato_b a, allegato_b1 c "
                                                        + "WHERE b.pivacf='" + piva + "' AND a.username=b.username AND a.cf='" + cfdocente + "' "
                                                        + "AND c.username=a.username AND a.id=c.idallegato_b1";
                                                Db_Bando db_neet = new Db_Bando(hostbandoneet);
                                                try (Statement st2 = db_neet.getConnection().createStatement();
                                                        ResultSet rs2 = st2.executeQuery(sql2)) {
                                                    if (rs2.next()) {
                                                        b1 = rs2.getString("c.allegatob1");
                                                    }
                                                }
                                                db_neet.closeDB();
                                            } else { //D&D
                                                String sql2 = "SELECT c.allegatob1 FROM bando_dd_mcn b, allegato_b a, allegato_b1 c "
                                                        + "WHERE b.pivacf='" + piva + "' AND a.username=b.username AND a.cf='" + cfdocente + "' "
                                                        + "AND c.username=a.username AND a.id=c.idallegato_b1";
                                                try (Statement st2 = db_dd.getConnection().createStatement();
                                                        ResultSet rs2 = st2.executeQuery(sql2)) {
                                                    if (rs2.next()) {
                                                        b1 = rs2.getString("c.allegatob1");
                                                    }
                                                }
                                            }

                                        }
                                    }
                                    db_dd.closeDB();
                                }
                            } else {
                                b1 = rs1.getString("d.richiesta_accr");
                            }
                            pathfiledaunire.add(docid);
                            pathfiledaunire.add(curriculum);
                            pathfiledaunire.add(b1);
                        }
                    }

                    String registro = "";
                    //REGISTRO COMPLESSIVO
                    String sql6 = "SELECT path FROM documenti_progetti d WHERE d.idprogetto=" + pf + " AND d.tipo=33";

                    try (Statement st6 = db2.getConnection().createStatement();
                            ResultSet rs6 = st6.executeQuery(sql6)) {
                        if (rs6.next()) {
                            registro = rs6.getString(1);
                        }
                    }

                    // ESTRAZIONE NEET RENDICONTABILI
                    String sql3 = "SELECT sum(totaleorerendicontabili) as totOre,idutente FROM registro_completo "
                            + "WHERE fase = 'A' AND ruolo LIKE 'ALLIEVO%' AND idprogetti_formativi = " + pf + " GROUP BY idutente";

                    try (Statement st3 = db2.getConnection().createStatement();
                            ResultSet rs3 = st3.executeQuery(sql3)) {
                        while (rs3.next()) {
                            Long rerendicontabilifaseA = rs3.getLong(1);
                            if (rerendicontabilifaseA >= hh36) {
                                int id_neet = rs3.getInt("idutente");

                                String modello5 = "";
                                String modello1 = "";
                                String pattoserv = "";
                                String tesserasanitaria = "";
                                String docid = "";

                                // RECUPERO DOCUMENTI NEET
                                String sql4 = "SELECT d.iddocumenti_allievi,d.path,d.tipo FROM documenti_allievi d WHERE d.idallievo = "
                                        + id_neet + " ORDER BY d.tipo,d.iddocumenti_allievi";
                                try (Statement st4 = db2.getConnection().createStatement();
                                        ResultSet rs4 = st4.executeQuery(sql4)) {
                                    while (rs4.next()) {
                                        int tipodoc = rs4.getInt("d.tipo");
                                        String pathdoc = rs4.getString("d.path");
                                        if (tipodoc == 20) {
                                            modello5 = pathdoc;
                                        } else if (tipodoc == 3) {
                                            modello1 = pathdoc;
                                        } else if (tipodoc == 4) {
                                            pattoserv = pathdoc;
                                        } else if (tipodoc == 11) {
                                            tesserasanitaria = pathdoc;
                                        }
                                    }

                                }

                                // Documento accompagnamento Neet - MODELLO 5
                                pathfiledaunire.add(modello5);

                                //Domanda di iscrizione al percorso Neet  - MODELLO 1
                                pathfiledaunire.add(modello1);

                                //Patto di Servizio Neet
                                pathfiledaunire.add(pattoserv);

                                //Tessera Sanitaria Neet
                                pathfiledaunire.add(tesserasanitaria);

                                //DOCUMENTO IDENTITA NEET
                                String sql5 = "SELECT docid FROM allievi WHERE idallievi=" + id_neet;
                                try (Statement st5 = db2.getConnection().createStatement();
                                        ResultSet rs5 = st5.executeQuery(sql5)) {
                                    if (rs5.next()) {
                                        docid = rs5.getString(1);
                                    }
                                }
                                pathfiledaunire.add(docid);

                                String domanda_ammissione = "";
                                String sql7 = "SELECT domanda_ammissione_presente,domanda_ammissione FROM maschera_m5 m WHERE m.allievo=" + id_neet;
                                try (Statement st7 = db2.getConnection().createStatement();
                                        ResultSet rs7 = st7.executeQuery(sql7)) {
                                    if (rs7.next()) {
                                        if (rs7.getInt("domanda_ammissione_presente") == 1) {
                                            if (rs7.getString("domanda_ammissione") != null) {
                                                domanda_ammissione = rs7.getString("domanda_ammissione");
                                            }
                                        }
                                    }
                                }
                                if (!domanda_ammissione.equals("")) {
                                    pathfiledaunire.add(domanda_ammissione);
                                }
                            }
                        }
                    }
                    pathfiledaunire.add(registro);
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
                db2.closeDB();

                List<byte[]> elencocompleto = new LinkedList<>();

                pathfiledaunire.forEach(file1 -> {

                    if (file1.trim().equals("")) {

                    } else if (file1.startsWith("/")) {

                        if (file1.toLowerCase().endsWith(".pdf")) {
                            try {
                                File pdf = new File(file1);
                                if (checkPDF(pdf)) {
                                    elencocompleto.add(readFileToByteArray(pdf));
                                } else {
                                    log.log(Level.SEVERE, "ERRORE NEL FILE {0} - NON TROVATO", file1);
                                }
                            } catch (Exception e) {
                                log.log(Level.SEVERE, "ERRORE NEL FILE {0} - {1}", new Object[]{file1, estraiEccezione(e)});
                            }
                        } else if (file1.toLowerCase().endsWith(".p7m")) {
                            try {
                                File p7m = new File(file1);
                                if (p7m.exists()) {
                                    byte[] content = Constant.extractSignatureInformation_P7M(readFileToByteArray(p7m));
                                    if (content != null) {
                                        elencocompleto.add(content);
                                    } else {
                                        log.log(Level.SEVERE, "ERRORE NEL FILE {0}", file1);
                                    }
                                } else {
                                    log.log(Level.SEVERE, "ERRORE NEL FILE {0} - NON TROVATO", file1);
                                }
                            } catch (Exception e) {
                                log.log(Level.SEVERE, "ERRORE NEL FILE {0} - {1}", new Object[]{file1, estraiEccezione(e)});
                            }
                        } else {
                            log.log(Level.SEVERE, "{0} NON IDENTIFICATO {1}", new Object[]{pf, file1});
                        }

                    } else {
                        String nome = file1.split("###")[0];
                        String base64 = file1.split("###")[2];

                        if (nome.toLowerCase().endsWith(".pdf")) {
                            byte[] content = Base64.decodeBase64(base64);
                            if (content != null) {
                                elencocompleto.add(content);
                            } else {
                                log.log(Level.SEVERE, "ERRORE NEL FILE {0} CONTENUTO ERRATO", file1);
                            }
                        } else {
                            log.log(Level.SEVERE, "{0} ????? BASE 64 {1}", new Object[]{pf, nome});
                        }
                    }

                });
                try {
                    String pathin = "/mnt/mcn/yisu_ded/estrazioni/pdfunici/";
                    if (neet) {
                        pathin = "/mnt/mcn/yisu_neet/estrazioni/pdfunici/";
                    }
                    Constant.createDir(pathin);
                    String pdfdest = pathin + pf + ".pdf";
                    PDFMergerUtility obj = new PDFMergerUtility();
                    obj.setDestinationFileName(pdfdest);
                    elencocompleto.forEach(pdf1 -> {
                        try {
                            InputStream is = new ByteArrayInputStream(pdf1);
                            obj.addSource(is);
                        } catch (Exception ex) {
                            log.severe(estraiEccezione(ex));
                        }
                    });
                    obj.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
                    Db_Bando db3 = new Db_Bando(this.host);
                    db3.getConnection().createStatement().executeUpdate("UPDATE progetti_formativi SET pdfunico = '" + pdfdest + "' WHERE idprogetti_formativi = " + pf);
                    db3.closeDB();
                    log.log(Level.WARNING, "{0} RILASCIATO - OK", pdfdest);
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
            });
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

    public void impostaritiratounder36oreA() {
        try {
            Long hh36 = new Long(129600000);
            Db_Bando db1 = new Db_Bando(this.host);

            String sql1 = "SELECT a.idallievi,a.idprogetti_formativi,a.idsoggetto_attuatore,a.cognome,a.nome "
                    + "FROM allievi a WHERE a.idprogetti_formativi IS NOT NULL AND a.id_statopartecipazione='01' "
                    + "AND a.idprogetti_formativi IN(SELECT p.idprogetti_formativi FROM progetti_formativi p "
                    + "WHERE p.stato IN (SELECT s.idstati_progetto FROM stati_progetto s "
                    + "WHERE s.ordine_processo IS NOT NULL AND s.ordine_processo>4));";
            try (Statement st1 = db1.getConnection().createStatement();
                    ResultSet rs1 = st1.executeQuery(sql1)) {
                while (rs1.next()) {
                    long idallievi = rs1.getLong(1);
                    long idprogetti_formativi = rs1.getLong(2);
                    long idsoggetto_attuatore = rs1.getLong(3);

                    String sql2 = "SELECT sum(totaleorerendicontabili) as totOre FROM registro_completo "
                            + "WHERE fase = 'A' AND ruolo LIKE 'ALLIEVO%' "
                            + "AND idprogetti_formativi = " + idprogetti_formativi + " AND idutente = " + idallievi + " AND idsoggetti_attuatori = " + idsoggetto_attuatore;

                    try (Statement st2 = db1.getConnection().createStatement();
                            ResultSet rs2 = st2.executeQuery(sql2)) {
                        if (rs2.next()) {
                            if (rs2.getLong(1) >= hh36) {
//                                System.out.println(idallievi + " OK");
                            } else {
                                String upd1 = "UPDATE allievi SET id_statopartecipazione='02' WHERE idallievi=" + idallievi;
                                try (Statement st3 = db1.getConnection().createStatement()) {
                                    int x = st3.executeUpdate(upd1);
                                    System.out.println(upd1 + " -- " + (x > 0));
                                }
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
//        new Repair(false, true).impostaritiratounder36oreA();
//    }

}
