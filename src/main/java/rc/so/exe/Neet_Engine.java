/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.so.exe;

import static rc.so.exe.Constant.conf;
import static rc.so.exe.Constant.createDir;
import static rc.so.exe.Constant.estraiEccezione;
import static rc.so.exe.Constant.formatStringtoStringDate;
import static rc.so.exe.Constant.getCell;
import static rc.so.exe.Constant.getRow;
import static rc.so.exe.Constant.patternITA;
import static rc.so.exe.Constant.patternSql;
import static rc.so.exe.Constant.setCell;
import static rc.so.exe.Constant.timestamp;
import static rc.so.exe.Constant.timestampSQL;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;

/**
 *
 * @author rcosco
 */
public class Neet_Engine {

    public static final String pathTemp = "/mnt/mcn/test/temp/";
    public static final String bando = "BAEN1";
//    public static final boolean test = true;

    public String host;

    public Neet_Engine(boolean test) {
        this.host = conf.getString("db.host") + ":3306/enm_neet_prod";
        if (test) {
            this.host = conf.getString("db.host") + ":3306/enm_neet";
        }
    }

    public void crea_report() {
        try {
            DateTime dt1 = new DateTime();

            Db_Bando db1 = new Db_Bando(this.host);
            String contentb64 = db1.getPath("excel.templatereport");
//            String pathtemp = db1.getPath("pathtemp");
            List<ExcelDomande> list = db1.listaconsegnate("bando_neet_mcn");
            String sq1;
            String sq2;
            File output;
            try (InputStream is = new ByteArrayInputStream(decodeBase64(contentb64)); XSSFWorkbook wb = new XSSFWorkbook(is)) {
                XSSFSheet foglio1 = wb.getSheet("ELENCO DOMANDE INVIATE");
                XSSFSheet foglio2 = wb.getSheet("SCHEDA_SINT_SA");
                AtomicInteger indice = new AtomicInteger(1);
                list.forEach(v1 -> {

                    XSSFRow rigaprimofoglio = getRow(foglio1, indice.get());
                    setCell(getCell(rigaprimofoglio, 0), v1.getCODICEDOMANDA());
                    setCell(getCell(rigaprimofoglio, 1), v1.getDATACONSEGNA());
                    setCell(getCell(rigaprimofoglio, 2), v1.getORACONSEGNA());
                    setCell(getCell(rigaprimofoglio, 3), v1.getRAGIONESOCIALE());
                    setCell(getCell(rigaprimofoglio, 4), v1.getPIVA());
                    setCell(getCell(rigaprimofoglio, 5), v1.getSEDELEGALEINDIRIZZO());
                    setCell(getCell(rigaprimofoglio, 6), v1.getSEDELEGALECAP());
                    setCell(getCell(rigaprimofoglio, 7), v1.getSEDELEGALECOMUNE());
                    setCell(getCell(rigaprimofoglio, 8), v1.getSEDELEGALEPROVINCIA());
                    setCell(getCell(rigaprimofoglio, 9), v1.getSEDELEGALEREGIONE());
                    setCell(getCell(rigaprimofoglio, 10), v1.getPEC());
                    setCell(getCell(rigaprimofoglio, 11), v1.getEMAIL());
                    setCell(getCell(rigaprimofoglio, 12), v1.getTELEFONO());
                    setCell(getCell(rigaprimofoglio, 13), v1.getNPROTOCOLLO());
                    setCell(getCell(rigaprimofoglio, 14), v1.getSTATODOMANDA());

                    XSSFRow rigasecondofoglio = getRow(foglio2, indice.get());
                    setCell(getCell(rigasecondofoglio, 0), v1.getCODICEDOMANDA());
                    setCell(getCell(rigasecondofoglio, 1), v1.getDATACONSEGNA());
                    setCell(getCell(rigasecondofoglio, 2), v1.getORACONSEGNA());
                    setCell(getCell(rigasecondofoglio, 3), v1.getRAGIONESOCIALE());
                    setCell(getCell(rigasecondofoglio, 4), v1.getPIVA());
                    setCell(getCell(rigasecondofoglio, 5), v1.getNPROTOCOLLO());

                    setCell(getCell(rigasecondofoglio, 6), v1.getNSEDI());

                    setCell(getCell(rigasecondofoglio, 7), v1.getSEDE1INDIRIZZO());
                    setCell(getCell(rigasecondofoglio, 8), v1.getSEDE1COMUNE());
                    setCell(getCell(rigasecondofoglio, 9), v1.getSEDE1PROVINCIA());
                    setCell(getCell(rigasecondofoglio, 10), v1.getSEDE1REGIONE());
                    setCell(getCell(rigasecondofoglio, 11), v1.getSEDE1TITOLODISP());
                    setCell(getCell(rigasecondofoglio, 12), v1.getSEDE1MQ());

                    setCell(getCell(rigasecondofoglio, 13), v1.getSEDE2INDIRIZZO());
                    setCell(getCell(rigasecondofoglio, 14), v1.getSEDE2COMUNE());
                    setCell(getCell(rigasecondofoglio, 15), v1.getSEDE2PROVINCIA());
                    setCell(getCell(rigasecondofoglio, 16), v1.getSEDE2REGIONE());
                    setCell(getCell(rigasecondofoglio, 17), v1.getSEDE2TITOLODISP());
                    setCell(getCell(rigasecondofoglio, 18), v1.getSEDE2MQ());

                    setCell(getCell(rigasecondofoglio, 19), v1.getSEDE3INDIRIZZO());
                    setCell(getCell(rigasecondofoglio, 20), v1.getSEDE3COMUNE());
                    setCell(getCell(rigasecondofoglio, 21), v1.getSEDE3PROVINCIA());
                    setCell(getCell(rigasecondofoglio, 22), v1.getSEDE3REGIONE());
                    setCell(getCell(rigasecondofoglio, 23), v1.getSEDE3TITOLODISP());
                    setCell(getCell(rigasecondofoglio, 24), v1.getSEDE3MQ());

                    setCell(getCell(rigasecondofoglio, 25), v1.getSEDE4INDIRIZZO());
                    setCell(getCell(rigasecondofoglio, 26), v1.getSEDE4COMUNE());
                    setCell(getCell(rigasecondofoglio, 27), v1.getSEDE4PROVINCIA());
                    setCell(getCell(rigasecondofoglio, 28), v1.getSEDE4REGIONE());
                    setCell(getCell(rigasecondofoglio, 29), v1.getSEDE4TITOLODISP());
                    setCell(getCell(rigasecondofoglio, 30), v1.getSEDE4MQ());

                    setCell(getCell(rigasecondofoglio, 31), v1.getSEDE5INDIRIZZO());
                    setCell(getCell(rigasecondofoglio, 32), v1.getSEDE5COMUNE());
                    setCell(getCell(rigasecondofoglio, 33), v1.getSEDE5PROVINCIA());
                    setCell(getCell(rigasecondofoglio, 34), v1.getSEDE5REGIONE());
                    setCell(getCell(rigasecondofoglio, 35), v1.getSEDE5TITOLODISP());
                    setCell(getCell(rigasecondofoglio, 36), v1.getSEDE5MQ());

                    setCell(getCell(rigasecondofoglio, 37), v1.getNDOCENTI());

                    setCell(getCell(rigasecondofoglio, 38), v1.getNOMEDOCENTE1());
                    setCell(getCell(rigasecondofoglio, 39), v1.getCOGNOMEDOCENTE1());
                    setCell(getCell(rigasecondofoglio, 40), v1.getCFDOCENTE1());
                    setCell(getCell(rigasecondofoglio, 41), v1.getFASCIAPROPOSTADOCENTE1());

                    setCell(getCell(rigasecondofoglio, 42), v1.getNOMEDOCENTE2());
                    setCell(getCell(rigasecondofoglio, 43), v1.getCOGNOMEDOCENTE2());
                    setCell(getCell(rigasecondofoglio, 44), v1.getCFDOCENTE2());
                    setCell(getCell(rigasecondofoglio, 45), v1.getFASCIAPROPOSTADOCENTE2());

                    setCell(getCell(rigasecondofoglio, 46), v1.getNOMEDOCENTE3());
                    setCell(getCell(rigasecondofoglio, 47), v1.getCOGNOMEDOCENTE3());
                    setCell(getCell(rigasecondofoglio, 48), v1.getCFDOCENTE3());
                    setCell(getCell(rigasecondofoglio, 49), v1.getFASCIAPROPOSTADOCENTE3());

                    setCell(getCell(rigasecondofoglio, 50), v1.getNOMEDOCENTE4());
                    setCell(getCell(rigasecondofoglio, 51), v1.getCOGNOMEDOCENTE4());
                    setCell(getCell(rigasecondofoglio, 52), v1.getCFDOCENTE4());
                    setCell(getCell(rigasecondofoglio, 53), v1.getFASCIAPROPOSTADOCENTE4());

                    setCell(getCell(rigasecondofoglio, 54), v1.getNOMEDOCENTE5());
                    setCell(getCell(rigasecondofoglio, 55), v1.getCOGNOMEDOCENTE5());
                    setCell(getCell(rigasecondofoglio, 56), v1.getCFDOCENTE5());
                    setCell(getCell(rigasecondofoglio, 57), v1.getFASCIAPROPOSTADOCENTE5());

                    indice.addAndGet(1);

                });
                int maxfoglio1 = foglio1.getRow(0).getLastCellNum();
                int maxfoglio2 = foglio2.getRow(0).getLastCellNum();
                for (int i = 0; i < Math.max(maxfoglio1, maxfoglio2); i++) {
                    if (i < maxfoglio1) {
                        foglio1.autoSizeColumn(i);
                    }
                    if (i < maxfoglio2) {
                        foglio2.autoSizeColumn(i);
                    }
                }
                String ts = dt1.toString(timestamp);
                sq1 = dt1.toString(patternSql);
                sq2 = dt1.toString(timestampSQL);
                createDir(pathTemp);
                output = new File(pathTemp + "Domande_consegnate_" + ts + ".xlsx");
                try (FileOutputStream fos = new FileOutputStream(output)) {
                    wb.write(fos);
                }
            }

            db1.insertReportExcel(
                    sq1,
                    Base64.encodeBase64String(FileUtils.readFileToByteArray(output)),
                    sq2);

            db1.closeDB();
            output.delete();

        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

    public void aggiorna_dataconvenzione_fase1() {
        Db_Bando db1 = new Db_Bando(this.host);
        try {
            String sql1 = "SELECT a.username FROM bando_neet_mcn a WHERE a.dataupconvenzionefinale ='-'";
            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1);) {
                while (rs1.next()) {
                    String sql2 = "SELECT timestamp FROM convenzioniroma WHERE username = '" + rs1.getString("a.username") + "' ORDER BY timestamp DESC LIMIT 1";
                    try (Statement st2 = db1.getConnection().createStatement(); ResultSet rs2 = st2.executeQuery(sql2);) {
                        if (rs2.next()) {
                            String data = formatStringtoStringDate(rs2.getString(1), timestampSQL, patternITA, true);
                            if (!data.equals("DATA ERRATA")) {
                                String upd = "UPDATE bando_neet_mcn SET dataupconvenzionefinale = ? where username = ?";
                                try (PreparedStatement ps1 = db1.getConnection().prepareStatement(upd)) {
                                    ps1.setString(1, data);
                                    ps1.setString(2, rs1.getString("a.username"));
                                    ps1.executeUpdate();
                                }
                            } else {
                                log.log(Level.SEVERE, "DATA ERRATA: {0}", rs1.getString("a.username"));
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

    public void elenco_domande_fase1() {
        Db_Bando db1 = new Db_Bando(this.host);
        try {

            String sql1 = "SELECT username,id,stato,datainvio FROM domandecomplete WHERE stato = '1' AND id NOT IN (SELECT DISTINCT(coddomanda) FROM bando_neet_mcn) GROUP BY id";
            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {

                while (rs1.next()) {
                    Domande d1 = new Domande();
                    d1.setCodicedomanda(rs1.getString("id"));
                    d1.setDataconsegna(rs1.getString("datainvio"));
                    d1.setStato(rs1.getString("stato"));
                    boolean ok = false;
                    String sql2 = "SELECT * FROM usersvalori WHERE username= '" + rs1.getString("username") + "'";
                    try (Statement st2 = db1.getConnection().createStatement(); ResultSet rs2 = st2.executeQuery(sql2)) {
                        while (rs2.next()) {
                            ok = true;
                            String nomecampo = rs2.getString("campo");
                            String valorecampo = rs2.getString("valore").toUpperCase().trim();
                            switch (nomecampo) {
                                case "nome":
                                    d1.setNome(valorecampo);
                                    break;
                                case "cognome":
                                    d1.setCognome(valorecampo);
                                    break;
                                case "cfuser":
                                    d1.setCodiceFiscale(valorecampo);
                                    break;
                                case "pec":
                                    d1.setPEC(valorecampo.toLowerCase());
                                    break;
                                case "societa":
                                    d1.setRagioneSociale(valorecampo);
                                    break;
                                case "piva":
                                    d1.setPartitaIVA(valorecampo);
                                    break;
                                case "sedecomune":
                                    d1.setSedeComune(valorecampo);
                                    break;
                                case "sedecap":
                                    d1.setSedeCap(valorecampo);
                                    break;
                                case "cell":
                                    d1.setCellulare(valorecampo);
                                    break;
                                case "data":
                                    d1.setDataNascita(valorecampo);
                                    break;
                                case "email":
                                    d1.setEmail(valorecampo);
                                    break;
                                case "sedeindirizzo":
                                    d1.setSedeIndirizzo(valorecampo);
                                    break;
                                case "docric1":
                                    d1.setNumeroDocumento(valorecampo);
                                    break;
                                case "datasc1":
                                    d1.setScadenzaDoc(valorecampo);
                                    break;
                                case "caricasoc":
                                    d1.setCaricaSoc(valorecampo);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    if (ok) {
                        String insert = "INSERT INTO bando_neet_mcn (codbando,username,nome,cognome,cf,pivacf,pec,societa,dataconsegna,coddomanda,sedecomune,sedecap,cellulare,data,mail,sedeindirizzo,docric,scadenzadoc,caricasoc)"
                                + " VALUES ("
                                + "?,?,?,?,?,?,?,?,?,?," //10
                                + "?,?,?,?,?,?,?,?,?"
                                + ")";
                        try (PreparedStatement ps1 = db1.getConnection().prepareStatement(insert)) {
                            ps1.setString(1, bando);
                            ps1.setString(2, rs1.getString("username"));
                            ps1.setString(3, d1.getNome());
                            ps1.setString(4, d1.getCognome());
                            ps1.setString(5, d1.getCodiceFiscale());
                            ps1.setString(6, d1.getPartitaIVA());
                            ps1.setString(7, d1.getPEC());
                            ps1.setString(8, d1.getRagioneSociale());
                            ps1.setString(9, d1.getDataconsegna());
                            ps1.setString(10, d1.getCodicedomanda());
                            ps1.setString(11, d1.getSedeComune());
                            ps1.setString(12, d1.getSedeCap());
                            ps1.setString(13, d1.getCellulare());
                            ps1.setString(14, d1.getDataNascita());
                            ps1.setString(15, d1.getEmail());
                            ps1.setString(16, d1.getSedeIndirizzo());
                            ps1.setString(17, d1.getNumeroDocumento());
                            ps1.setString(18, d1.getScadenzaDoc());
                            ps1.setString(19, d1.getCaricaSoc());
                            ps1.execute();
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }

        db1.closeDB();
    }

    public void aggiorna_reportistica() {
        Db_Bando db1 = new Db_Bando(this.host);
        try {

            String sql1 = "SELECT count(*),stato_domanda FROM bando_neet_mcn GROUP BY stato_domanda";
            AtomicInteger count_dc0;
            AtomicInteger count_dc1;
            AtomicInteger count_dc2;
            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                count_dc0 = new AtomicInteger(0);
                count_dc1 = new AtomicInteger(0);
                count_dc2 = new AtomicInteger(0);
                while (rs1.next()) {
                    String stato = rs1.getString(2);
                    int count = rs1.getInt(1);
                    if (stato.equals("S")) {
                        count_dc1.addAndGet(count);
                    } else {
                        count_dc2.addAndGet(count);
                    }
                    count_dc0.addAndGet(count);
                }
            }

            String upd0 = "UPDATE reportistica SET valore = '" + count_dc0.get() + "' WHERE codice = 'dc0'";
            String upd1 = "UPDATE reportistica SET valore = '" + count_dc1.get() + "' WHERE codice = 'dc1'";
            String upd2 = "UPDATE reportistica SET valore = '" + count_dc2.get() + "' WHERE codice = 'dc2'";
            try (Statement st2 = db1.getConnection().createStatement()) {
                st2.executeUpdate(upd0);
                st2.executeUpdate(upd1);
                st2.executeUpdate(upd2);
            }
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }

        db1.closeDB();
    }

    public void update_domande_fase1() {
        Db_Bando db1 = new Db_Bando(this.host);
        try {
            String sql1 = "SELECT username FROM bando_neet_mcn a WHERE decreto <> '-'";
            try (Statement st1 = db1.getConnection().createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                while (rs1.next()) {
                    Domande d1 = new Domande();
                    boolean ok = false;
                    String sql2 = "SELECT * FROM usersvalori WHERE username= '" + rs1.getString("username") + "'";
                    try (Statement st2 = db1.getConnection().createStatement(); ResultSet rs2 = st2.executeQuery(sql2)) {
                        while (rs2.next()) {
                            ok = true;
                            String nomecampo = rs2.getString("campo");
                            String valorecampo = rs2.getString("valore").toUpperCase().trim();
                            switch (nomecampo) {
                                case "sedecomune":
                                    d1.setSedeComune(valorecampo);
                                    break;
                                case "sedecap":
                                    d1.setSedeCap(valorecampo);
                                    break;
                                case "cell":
                                    d1.setCellulare(valorecampo);
                                    break;
                                case "data":
                                    d1.setDataNascita(valorecampo);
                                    break;
                                case "email":
                                    d1.setEmail(valorecampo);
                                    break;
                                case "sedeindirizzo":
                                    d1.setSedeIndirizzo(valorecampo);
                                    break;
                                case "docric1":
                                    d1.setNumeroDocumento(valorecampo);
                                    break;
                                case "datasc1":
                                    d1.setScadenzaDoc(valorecampo);
                                    break;
                                case "caricasoc":
                                    d1.setCaricaSoc(valorecampo);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    if (ok) {
                        String UPDATE = "UPDATE bando_neet_mcn SET sedecomune = ?,sedecap = ?,cellulare = ?,data = ?,mail = ?,sedeindirizzo = ?,docric = ?,scadenzadoc = ?,caricasoc = ?  where username = ?";
                        try (PreparedStatement ps1 = db1.getConnection().prepareStatement(UPDATE)) {
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
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }

        db1.closeDB();
    }

    private static final Logger log = Constant.createLog("Procedura", "/mnt/mcn/test/log/");

    
    
//    public static void main(String[] args) {
//         new Neet_Engine(false).crea_report();
//    }
}
