/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.refill.exe;

import it.refill.exe.Constant;
import static it.refill.exe.Constant.checkPDF;
import static it.refill.exe.Constant.conf;
import static it.refill.exe.Constant.estraiEccezione;
import it.refill.exe.Db_Bando;
import it.refill.report.Complessivo;
import it.refill.report.FaseA;
import it.refill.report.FaseB;
import it.refill.report.Lezione;
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
 * @author Administrator
 */
public class Generapdfanpalmod {

    private static final Logger log = Constant.createLog("TesterNewANPAL", "/mnt/mcn/test/log/", true);
    public String host = conf.getString("db.host") + ":3306/enm_gestione_dd_prod";

    public void crea_pdf_unico_ANPAL() {
        try {
            List<Integer> elenco = new ArrayList<>();
            elenco.add(151);
            elenco.add(122);
            elenco.add(170);
            elenco.add(142);
            elenco.add(130);
            elenco.add(196);
            elenco.add(192);
            elenco.add(198);
            elenco.add(135);
            elenco.add(164);
            elenco.add(116);
            elenco.add(188);
            elenco.add(190);
            elenco.add(132);
            elenco.add(118);
            elenco.add(126);
            elenco.add(162);
            elenco.add(174);
            elenco.add(153);
            elenco.add(222);
            elenco.add(168);
            elenco.add(112);
            elenco.add(124);
            elenco.add(137);

            Long hh36 = new Long(129600000);
            elenco.forEach(pf -> {
                List<String> pathfiledaunire = new LinkedList<>();
                Db_Bando db2 = new Db_Bando(this.host);
                try {
                    // Tabella All. B1 e relativo CV e Carta identita' Docente
                    String sql1 = "SELECT d.iddocenti,d.codicefiscale,d.curriculum,d.docid,d.tipo_inserimento,d.richiesta_accr,d.idsoggetti_attuatori,s.piva "
                            + "FROM docenti d,soggetti_attuatori s WHERE d.idsoggetti_attuatori=s.idsoggetti_attuatori AND d.stato='A' "
                            + "AND d.iddocenti IN (SELECT p.iddocenti FROM progetti_docenti p WHERE p.idprogetti_formativi= " + pf + ")";
                    try ( Statement st1 = db2.getConnection().createStatement();  ResultSet rs1 = st1.executeQuery(sql1)) {
                        while (rs1.next()) {
//                            int SA = rs1.getInt("d.idsoggetti_attuatori");
                            String piva = rs1.getString("s.piva");
                            String cfdocente = rs1.getString("d.codicefiscale");
                            String docid = rs1.getString("d.docid");
                            String curriculum = rs1.getString("d.curriculum");
                            String b1 = "NONE";
                            if (rs1.getString("d.tipo_inserimento") == null) {
                                String hostbando = StringUtils.remove(this.host, "gestione_");
                                String sql2_A = "SELECT b.accreditato FROM bando_dd_mcn b WHERE b.pivacf='" + piva + "'";
                                Db_Bando db_dd = new Db_Bando(hostbando);
                                try ( Statement st2_A = db_dd.getConnection().createStatement();  ResultSet rs2_A = st2_A.executeQuery(sql2_A)) {
                                    if (rs2_A.next()) {
                                        if (rs2_A.getString(1).equals("SI")) { //NEET
                                            String hostbandoneet = StringUtils.replace(this.host, "gestione_dd", "neet");
                                            String sql2 = "SELECT c.allegatob1 FROM bando_neet_mcn b, allegato_b a, allegato_b1 c "
                                                    + "WHERE b.pivacf='" + piva + "' AND a.username=b.username AND a.cf='" + cfdocente + "' "
                                                    + "AND c.username=a.username AND a.id=c.idallegato_b1";
                                            Db_Bando db_neet = new Db_Bando(hostbandoneet);
                                            try ( Statement st2 = db_neet.getConnection().createStatement();  ResultSet rs2 = st2.executeQuery(sql2)) {
                                                if (rs2.next()) {
                                                    b1 = rs2.getString("c.allegatob1");
                                                }
                                            }
                                            db_neet.closeDB();
                                        } else { //D&D
                                            String sql2 = "SELECT c.allegatob1 FROM bando_dd_mcn b, allegato_b a, allegato_b1 c "
                                                    + "WHERE b.pivacf='" + piva + "' AND a.username=b.username AND a.cf='" + cfdocente + "' "
                                                    + "AND c.username=a.username AND a.id=c.idallegato_b1";
                                            try ( Statement st2 = db_dd.getConnection().createStatement();  ResultSet rs2 = st2.executeQuery(sql2)) {
                                                if (rs2.next()) {
                                                    b1 = rs2.getString("c.allegatob1");
                                                }
                                            }
                                        }

                                    }
                                }
                                db_dd.closeDB();

                            } else {
                                b1 = rs1.getString("d.richiesta_accr");
                            }
                            pathfiledaunire.add(docid);
                            pathfiledaunire.add(curriculum);
                            pathfiledaunire.add(b1);
                        }
                    }

                    FaseA FA = new FaseA(false, false);
                    FaseB FB = new FaseB(false, false);
                    List<Lezione> ca = FA.calcolaegeneraregistrofasea(pf, FA.getHost(), false, false, false);
                    List<Lezione> cb = FB.calcolaegeneraregistrofaseb(pf, FA.getHost(), false, false, false);
                    Complessivo c1 = new Complessivo(FA.getHost());
                    String registro = c1.registro_complessivo(pf, c1.getHost(), ca, cb, false, false).getPath();

                    // ESTRAZIONE NEET RENDICONTABILI
                    String sql3 = "SELECT sum(totaleorerendicontabili) as totOre,idutente FROM registro_completo "
                            + "WHERE fase = 'A' AND ruolo LIKE 'ALLIEVO%' AND idprogetti_formativi = " + pf + " GROUP BY idutente";

                    try ( Statement st3 = db2.getConnection().createStatement();  ResultSet rs3 = st3.executeQuery(sql3)) {
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
                                try ( Statement st4 = db2.getConnection().createStatement();  ResultSet rs4 = st4.executeQuery(sql4)) {
                                    while (rs4.next()) {
                                        int tipodoc = rs4.getInt("d.tipo");
                                        String pathdoc = rs4.getString("d.path");
                                        switch (tipodoc) {
                                            case 20:
                                                modello5 = pathdoc;
                                                break;
                                            case 3:
                                                modello1 = pathdoc;
                                                break;
                                            case 4:
                                                pattoserv = pathdoc;
                                                break;
                                            case 11:
                                                tesserasanitaria = pathdoc;
                                                break;
                                            default:
                                                break;
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
                                try ( Statement st5 = db2.getConnection().createStatement();  ResultSet rs5 = st5.executeQuery(sql5)) {
                                    if (rs5.next()) {
                                        docid = rs5.getString(1);
                                    }
                                }
                                pathfiledaunire.add(docid);

                                String domanda_ammissione = "";
                                String sql7 = "SELECT domanda_ammissione_presente,domanda_ammissione FROM maschera_m5 m WHERE m.allievo=" + id_neet;
                                try ( Statement st7 = db2.getConnection().createStatement();  ResultSet rs7 = st7.executeQuery(sql7)) {
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
                    String pathin = "/mnt/mcn/yisu_ded/estrazioni/pdfunici/newversion/";
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
                    log.log(Level.WARNING, "{0} RILASCIATO - OK", pdfdest);
                } catch (Exception e) {
                    log.severe(estraiEccezione(e));
                }
            });
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

    public static void main(String[] args) {
        new Generapdfanpalmod().crea_pdf_unico_ANPAL();
    }
}
