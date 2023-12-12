/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.so.exe;

import static rc.so.exe.Constant.sdfITA;
import java.io.File;
import java.io.StringWriter;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Paths.get;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author rcosco
 */
public class RSS_gestione {

    private static Logger log = Constant.createLog("Corsi_In_Partenza_NEET_", "/mnt/mcn/test/log/");
    
    public static void createDir(String path) {
        try {
            createDirectories(get(path));
        } catch (Exception e) {
        }
    }
    
//    public static void main(String[] args) {
//        boolean print = false;
//        boolean neet = true;
//        Db_Bando db1 = new Db_Bando(conf.getString("db.host") + ":3306/enm_gestione_neet_prod");
//        if (!neet) {
//            db1.closeDB();
//            db1 = new Db_Bando(conf.getString("db.host") + ":3306/enm_gestione_dd_prod");
//        }
//        String pathtemp = db1.getPath("pathTemp");
//        createDir(pathtemp);
//        try {
//            String sql0 = "SELECT idprogetti_formativi,start,idsoggetti_attuatori FROM progetti_formativi WHERE stato = 'P' OR stato = 'DV' OR stato = 'DC'";
//            try (Statement st0 = db1.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
//                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
//                Document doc = docBuilder.newDocument();
//                Element userList = doc.createElement("elencocorsi");
//                boolean presenti = false;
//                while (rs0.next()) {
//                    int idcorso = rs0.getInt(1);
//                    String datacorso = sdfITA.format(rs0.getDate(2));
//                    String TipoCorso = "Neet";
//                    String sql1 = "SELECT ragionesociale,cell_sa,email,comune FROM soggetti_attuatori WHERE idsoggetti_attuatori = " + rs0.getInt(3);
//                    try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
//                        if (rs1.next()) {
//                            String SoggettoAttuatoreNome = rs1.getString(1);
//                            String TelSA = rs1.getString(2);
//                            String MailSA = rs1.getString(3);
//                            String sql2 = "SELECT nome_provincia,nome FROM comuni WHERE idcomune = " + rs1.getInt(4);
//                            try (Statement st2 = db1.getConnection().createStatement(); ResultSet rs2 = st2.executeQuery(sql2)) {
//                                if (rs2.next()) {
//                                    String place_Provincia = rs2.getString(1);
//                                    String place_Comune = rs2.getString(2);
//                                    String sql3 = "SELECT COUNT(*) FROM allievi WHERE idsoggetto_attuatore = " + rs0.getInt(3) + " AND id_statopartecipazione = '01'";
//                                    try (Statement st3 = db1.getConnection().createStatement(); ResultSet rs3 = st3.executeQuery(sql3)) {
//                                        if (rs3.next()) {
//                                            presenti = true;
//                                            int PostiDisponibili = 12 - rs3.getInt(1);
//                                            Element corso = doc.createElement("corso");
//                                            corso.setAttribute("idcorso", String.valueOf(idcorso));
//                                            Element datacorsoxml = doc.createElement("datacorso");
//                                            datacorsoxml.setTextContent(datacorso);
//                                            corso.appendChild(datacorsoxml);
//                                            Element TipoCorsoxml = doc.createElement("TipoCorso");
//                                            TipoCorsoxml.setTextContent(TipoCorso);
//                                            corso.appendChild(TipoCorsoxml);
//                                            Element place = doc.createElement("place");
//                                            Element Provincia = doc.createElement("Provincia");
//                                            Provincia.setTextContent(place_Provincia);
//                                            place.appendChild(Provincia);
//                                            Element Comune = doc.createElement("Comune");
//                                            Comune.setTextContent(place_Comune);
//                                            place.appendChild(Comune);
//                                            corso.appendChild(place);
//                                            Element about = doc.createElement("about");
//                                            Element SoggettoAttuatoreNomexml = doc.createElement("SoggettoAttuatoreNome");
//                                            SoggettoAttuatoreNomexml.setTextContent(SoggettoAttuatoreNome);
//                                            about.appendChild(SoggettoAttuatoreNomexml);
//                                            Element PostiDisponibilixml = doc.createElement("PostiDisponibili");
//                                            PostiDisponibilixml.setTextContent(String.valueOf(PostiDisponibili));
//                                            about.appendChild(PostiDisponibilixml);
//                                            Element TelSAxml = doc.createElement("TelSA");
//                                            TelSAxml.setTextContent(TelSA);
//                                            about.appendChild(TelSAxml);
//                                            Element MailSAxml = doc.createElement("MailSA");
//                                            MailSAxml.setTextContent(MailSA);
//                                            about.appendChild(MailSAxml);
//                                            corso.appendChild(about);
//                                            userList.appendChild(corso);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                
//                if(presenti){
//                    doc.appendChild(userList);
//                }
//                
//                if (doc.hasChildNodes()) {
//                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
//                    Transformer transformer = transformerFactory.newTransformer();
//                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//                    DOMSource source = new DOMSource(doc);
//                    StringWriter stringWriter = new StringWriter();
//                    File f1 = new File(pathtemp + RandomStringUtils.randomNumeric(15) + "_pfinpartenza.xml");
//                    StreamResult result;
//                    if (print) {
//                        result = new StreamResult(stringWriter);
//                        transformer.transform(source, result);
//                        log.warning(stringWriter.toString());
//                        f1.delete();
//                    } else {
//                        result = new StreamResult(f1);
//                        transformer.transform(source, result);
//                        log.log(Level.WARNING, "{0} RILASCIATO.", f1.getPath());
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.severe(Constant.estraiEccezione(e));
//        }
//        db1.closeDB();
//    }

}
