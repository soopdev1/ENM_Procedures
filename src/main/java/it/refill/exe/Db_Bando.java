
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.exe;

import static it.refill.exe.Constant.formatStringtoStringDateSQL;
import static it.refill.exe.Constant.parseIntR;
import it.refill.report.Utenti;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import static org.apache.commons.lang3.StringUtils.stripAccents;

/**
 *
 * @author raffaele
 */
public class Db_Bando {

    private Connection c = null;

    public Db_Bando(String host) {

        String driver = "com.mysql.cj.jdbc.Driver";
        String user = "bando";
        String password = "bando";

        try {
            Class.forName(driver).newInstance();
            Properties p = new Properties();
            p.put("user", user);
            p.put("password", password);
            p.put("characterEncoding", "UTF-8");
            p.put("passwordCharacterEncoding", "UTF-8");
            p.put("useSSL", "false");
            p.put("connectTimeout", "1000");
            p.put("useUnicode", "true");
            this.c = DriverManager.getConnection("jdbc:mysql://" + host, p);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (this.c != null) {
                try {
                    this.c.close();
                } catch (Exception ex1) {
                }
            }
            this.c = null;
        }
    }

    public void closeDB() {
        try {
            if (this.c != null) {
                this.c.close();
            }
        } catch (SQLException ex) {
        }
    }

    public Connection getConnection() {
        return c;
    }

    public String getPath(String id) {
        String path = "-";
        try {
            String sql = "SELECT url FROM path WHERE id = ?";
            PreparedStatement ps = this.c.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                path = rs.getString(1);
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return path;
    }

    public boolean insertTracking(String idUser, String azione) {
        try {
            String ins = "INSERT INTO tracking (idUser,azione) VALUES (?,?)";
            PreparedStatement ps = this.c.prepareStatement(ins);
            ps.setString(1, idUser);
            ps.setString(2, azione);
            ps.execute();
            ps.close();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public ArrayList<Comuni_rc> query_comuni_rc() {
        ArrayList<Comuni_rc> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM comuni_rc";
            try (PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE); ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    out.add(new Comuni_rc(rs1.getInt(1), rs1.getString(2),
                            rs1.getString(3),
                            rs1.getString(4), rs1.getString(5), rs1.getString(6), rs1.getString(7)));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public String formatStatoDomanda(String statoDomanda) {
        try {

            switch (statoDomanda) {
                case "S":
                    return "NON PROCESSATA";
                case "R":
                    return "RIGETTATA";
                case "A":
                    return "APPROVATA";
                case "A1":
                    return "CONVENZIONE SA";
                case "A2":
                    return "SA ATTIVO";
                case "A3":
                    return "IN ATTESA FIRMA ENM";
                default:
                    break;
            }
        } catch (Exception e) {
        }
        return "";
    }

    public int countDocumentConvenzioni(String username) {
        int var = 0;
        try {
            String query = "select count(*) from docuserconvenzioni where username='" + username + "'";
            try (PreparedStatement ps = this.c.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    var = rs.getInt(1);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return var;
    }

    public String getInvioEmailROMA(String username) {
        String out = "0";
        try {
            String query = "select username,sendmail from docuserconvenzioni where username='" + username + "' and codicedoc='CONV'";
            try (PreparedStatement ps = this.c.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out = rs.getString("sendmail");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public String getConvenzioneROMA(String username) {
        String pathRoma = "";
        try {
            String query = "select path from convenzioniroma where username = '" + username + "' order by timestamp desc limit 1";
            try (PreparedStatement ps = this.c.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pathRoma = rs.getString("path");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pathRoma;
    }

    public List<ExcelDomande> listaconsegnate(String table) {
        List<ExcelDomande> out = new LinkedList<>();
        try {
            ArrayList<Comuni_rc> comuni_rc = query_comuni_rc();
            String sql = "SELECT * FROM " + table + " ORDER BY dataconsegna";
            PreparedStatement ps = this.c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                String USERNAME = rs.getString("username");
                String CODICEDOMANDA = rs.getString("coddomanda");
                String DATACONSEGNA = formatStringtoStringDateSQL(rs.getString("dataconsegna").split(" ")[0]);
                String ORACONSEGNA = rs.getString("dataconsegna").split(" ")[1].substring(0, 8);
                String RAGIONESOCIALE = rs.getString("societa");
                String PIVA = rs.getString("pivacf");
                String PEC = rs.getString("pec");
                String NPROTOCOLLO = rs.getString("protocollo");

                ExcelDomande ex1 = new ExcelDomande();
                ex1.setUSERNAME(USERNAME);
                ex1.setCODICEDOMANDA(CODICEDOMANDA);
                ex1.setDATACONSEGNA(DATACONSEGNA);
                ex1.setORACONSEGNA(ORACONSEGNA);
                ex1.setRAGIONESOCIALE(RAGIONESOCIALE);
                ex1.setRAGIONESOCIALE(RAGIONESOCIALE);
                ex1.setPIVA(PIVA);
                ex1.setPEC(PEC);
                ex1.setNPROTOCOLLO(NPROTOCOLLO);

                boolean convenzionedainviare = countDocumentConvenzioni(USERNAME) == 3;
                boolean convenzioneinviataROMA = getInvioEmailROMA(USERNAME).equals("1");
                boolean convenzionecaricatacontrofirmata = !getConvenzioneROMA(USERNAME).trim().equals("");

                if (convenzionedainviare) {
                    if (convenzioneinviataROMA) {
                        if (convenzionecaricatacontrofirmata) {
                            ex1.setSTATODOMANDA(formatStatoDomanda("A2"));
                        } else {
                            ex1.setSTATODOMANDA(formatStatoDomanda("A3"));
                        }
                    } else {
                        ex1.setSTATODOMANDA(formatStatoDomanda("A1"));
                    }
                } else {
                    ex1.setSTATODOMANDA(formatStatoDomanda(rs.getString("stato_domanda")));
                }

                String sql2 = "SELECT * FROM usersvalori WHERE username= '" + USERNAME + "'";
                PreparedStatement ps2 = this.c.prepareStatement(sql2);
                ResultSet rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    String campo = rs2.getString("campo");
                    String valore = rs2.getString("valore").toUpperCase().trim();
                    String valore1 = rs2.getString("valore").toUpperCase().trim();

                    if (campo.equals("sedeindirizzo")) {
                        ex1.setSEDELEGALEINDIRIZZO(valore.toUpperCase());
                    } else if (campo.equals("sedecap")) {
                        ex1.setSEDELEGALECAP(valore.toUpperCase());
                    } else if (campo.equals("sedecomune")) {
                        if (!valore.equals("")) {
                            Comuni_rc c0 = comuni_rc.stream().filter(c1 -> (c1.getId() == parseIntR(valore))).findAny().orElse(null);
                            if (c0 != null) {
                                valore1 = c0.getNome();
                            }
                        }
                        ex1.setSEDELEGALECOMUNE(valore1.toUpperCase());
                        
                    } else if (campo.equals("sedeprov")) {
                        if (!valore.equals("")) {
                            Comuni_rc c0 = comuni_rc.stream().filter(c1 -> c1.getCodiceprovincia().equals(valore)).findAny().orElse(null);
                            if (c0 != null) {
                                valore1 = c0.getProvincia();
                            }
                        }
                        ex1.setSEDELEGALEPROVINCIA(valore1.toUpperCase());
                    } else if (campo.equals("sederegione")) {
                        if (!valore.equals("")) {
                            Comuni_rc c0 = comuni_rc.stream().filter(c1 -> c1.getCodiceregione().equals(valore)).findAny().orElse(null);
                            if (c0 != null) {
                                valore1 = c0.getRegione();
                            }
                        }
                        ex1.setSEDELEGALEREGIONE(valore1.toUpperCase());
                    } else if (campo.equals("email")) {
                        ex1.setEMAIL(valore.toUpperCase());
                    } else if (campo.equals("cell")) {
                        ex1.setTELEFONO(valore.toUpperCase());
                    }

                }

                HashMap<String, String> allegato_a = getAllegatoA(USERNAME);
                ex1.setNSEDI(getMapValue(allegato_a, "numaule"));

                ex1.setSEDE1INDIRIZZO(getMapValue(allegato_a, "indirizzo1"));
                ex1.setSEDE1COMUNE(getMapValue(allegato_a, "citta1"));
                ex1.setSEDE1PROVINCIA(getMapValue(allegato_a, "provincia1"));
                ex1.setSEDE1REGIONE(getMapValue(allegato_a, "regioneaula1"));
                ex1.setSEDE1TITOLODISP(getMapValue(allegato_a, "titolo1"));
                ex1.setSEDE1MQ(getMapValue(allegato_a, "estremi1"));

                ex1.setSEDE2INDIRIZZO(getMapValue(allegato_a, "indirizzo2"));
                ex1.setSEDE2COMUNE(getMapValue(allegato_a, "citta2"));
                ex1.setSEDE2PROVINCIA(getMapValue(allegato_a, "provincia2"));
                ex1.setSEDE2REGIONE(getMapValue(allegato_a, "regioneaula2"));
                ex1.setSEDE2TITOLODISP(getMapValue(allegato_a, "titolo2"));
                ex1.setSEDE2MQ(getMapValue(allegato_a, "estremi2"));

                ex1.setSEDE3INDIRIZZO(getMapValue(allegato_a, "indirizzo3"));
                ex1.setSEDE3COMUNE(getMapValue(allegato_a, "citta3"));
                ex1.setSEDE3PROVINCIA(getMapValue(allegato_a, "provincia3"));
                ex1.setSEDE3REGIONE(getMapValue(allegato_a, "regioneaula3"));
                ex1.setSEDE3TITOLODISP(getMapValue(allegato_a, "titolo3"));
                ex1.setSEDE3MQ(getMapValue(allegato_a, "estremi3"));

                ex1.setSEDE4INDIRIZZO(getMapValue(allegato_a, "indirizzo4"));
                ex1.setSEDE4COMUNE(getMapValue(allegato_a, "citta4"));
                ex1.setSEDE4PROVINCIA(getMapValue(allegato_a, "provincia4"));
                ex1.setSEDE4REGIONE(getMapValue(allegato_a, "regioneaula4"));
                ex1.setSEDE4TITOLODISP(getMapValue(allegato_a, "titolo4"));
                ex1.setSEDE4MQ(getMapValue(allegato_a, "estremi4"));

                ex1.setSEDE5INDIRIZZO(getMapValue(allegato_a, "indirizzo5"));
                ex1.setSEDE5COMUNE(getMapValue(allegato_a, "citta5"));
                ex1.setSEDE5PROVINCIA(getMapValue(allegato_a, "provincia5"));
                ex1.setSEDE5REGIONE(getMapValue(allegato_a, "regioneaula5"));
                ex1.setSEDE5TITOLODISP(getMapValue(allegato_a, "titolo5"));
                ex1.setSEDE5MQ(getMapValue(allegato_a, "estremi5"));

                ex1.setNDOCENTI(getMapValue(allegato_a, "numdocenti"));

                String sql3 = "select * from allegato_b where username = '" + USERNAME + "' ORDER BY id";

                PreparedStatement ps3 = this.c.prepareStatement(sql3);
                ResultSet rs3 = ps3.executeQuery();
                while (rs3.next()) {

                    if (rs3.getInt("id") == 1) {
                        ex1.setNOMEDOCENTE1(rs3.getString("nome").toUpperCase());
                        ex1.setCOGNOMEDOCENTE1(rs3.getString("cognome").toUpperCase());
                        ex1.setCFDOCENTE1(rs3.getString("cf").toUpperCase());
                        ex1.setFASCIAPROPOSTADOCENTE1(rs3.getString("fascia").toUpperCase());
                    } else if (rs3.getInt("id") == 2) {
                        ex1.setNOMEDOCENTE2(rs3.getString("nome").toUpperCase());
                        ex1.setCOGNOMEDOCENTE2(rs3.getString("cognome").toUpperCase());
                        ex1.setCFDOCENTE2(rs3.getString("cf").toUpperCase());
                        ex1.setFASCIAPROPOSTADOCENTE2(rs3.getString("fascia").toUpperCase());
                    } else if (rs3.getInt("id") == 3) {
                        ex1.setNOMEDOCENTE3(rs3.getString("nome").toUpperCase());
                        ex1.setCOGNOMEDOCENTE3(rs3.getString("cognome").toUpperCase());
                        ex1.setCFDOCENTE3(rs3.getString("cf").toUpperCase());
                        ex1.setFASCIAPROPOSTADOCENTE3(rs3.getString("fascia").toUpperCase());
                    } else if (rs3.getInt("id") == 4) {
                        ex1.setNOMEDOCENTE4(rs3.getString("nome").toUpperCase());
                        ex1.setCOGNOMEDOCENTE4(rs3.getString("cognome").toUpperCase());
                        ex1.setCFDOCENTE4(rs3.getString("cf").toUpperCase());
                        ex1.setFASCIAPROPOSTADOCENTE4(rs3.getString("fascia").toUpperCase());
                    } else if (rs3.getInt("id") == 5) {
                        ex1.setNOMEDOCENTE5(rs3.getString("nome").toUpperCase());
                        ex1.setCOGNOMEDOCENTE5(rs3.getString("cognome").toUpperCase());
                        ex1.setCFDOCENTE5(rs3.getString("cf").toUpperCase());
                        ex1.setFASCIAPROPOSTADOCENTE5(rs3.getString("fascia").toUpperCase());
                    }
                }

                out.add(ex1);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public static String getMapValue(HashMap<String, String> map, String nome) {
        try {
            String valore = map.get(nome);
            if (valore != null) {
                return valore.toUpperCase().trim();
            }
        } catch (Exception e) {
        }
        return "";

    }

    public ArrayList<Items> query_disponibilita_rc() {
        ArrayList<Items> out = new ArrayList<>();
        try {
            String sql = "SELECT * FROM disponibilita_rc";
            try (PreparedStatement ps1 = this.c.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE); ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    out.add(new Items(rs1.getInt(1), rs1.getString(2)));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public HashMap<String, String> getAllegatoA(String username) {
        HashMap<String, String> map = new HashMap<>();
        String query = "SELECT * FROM allegato_a WHERE username = ?";
        try {
            ArrayList<Comuni_rc> comuni_rc = query_comuni_rc();
            ArrayList<Items> disponibilita = query_disponibilita_rc();

            try (PreparedStatement ps = this.c.prepareStatement(query)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String name = rsmd.getColumnName(i);
                        map.put(name, "");
                    }
                    if (rs.next()) {
                        SortedSet<String> indici = new TreeSet<>(map.keySet());
                        indici.forEach(ind -> {
                            try {
                                String valore = rs.getString(ind).toUpperCase().trim();
                                String valore1 = rs.getString(ind).toUpperCase().trim();
                                if (ind.startsWith("citta") && !valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> (c1.getId() == parseIntR(valore))).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getNome();
                                    }
                                } else if (ind.startsWith("regione") && !valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> c1.getCodiceregione().equals(valore)).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getRegione();
                                    }
                                } else if (ind.startsWith("provincia") && !valore.equals("")) {
                                    Comuni_rc c0 = comuni_rc.stream().filter(c1 -> c1.getCodiceprovincia().equals(valore)).findAny().orElse(null);
                                    if (c0 != null) {
                                        valore1 = c0.getProvincia();
                                    }
                                } else if (ind.startsWith("titolo") && !valore.equals("")) {
                                    Items it1 = disponibilita.stream().filter(c1 -> (c1.getCodice() == parseIntR(valore))).findAny().orElse(new Items(valore, valore));
                                    valore1 = it1.getDescrizione();
                                }
                                map.replace(ind, valore1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public boolean insertReportExcel(String data, String base64, String timestamp) {
        try {
            if (base64 != null) {

                String insert = "INSERT INTO excelreport VALUES(?,?,?)";
                PreparedStatement ps = this.c.prepareStatement(insert);
                ps.setString(1, data);
                ps.setString(2, base64);
                ps.setString(3, timestamp);
                try {
                    ps.executeUpdate();
                    return true;
                } catch (Exception e) {
                    if (e.getMessage().toLowerCase().contains("duplicate")) {
                        insert = "UPDATE excelreport SET content = ?, aggiornamento = ? WHERE giorno = ?";
                        ps = this.c.prepareStatement(insert);
                        ps.setString(1, base64);
                        ps.setString(2, timestamp);
                        ps.setString(3, data);
                        ps.executeUpdate();
                        return true;
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //REPORT
    public List<Utenti> list_Allievi_noAccento(int idpr) {
        List<Utenti> out = new ArrayList<>();
        try {
            String sql = "SELECT idallievi,nome,cognome,codicefiscale,email FROM allievi WHERE id_statopartecipazione='01' AND idprogetti_formativi = " + idpr;
            try (Statement st = this.c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Utenti u = new Utenti(rs.getInt("idallievi"),
                            stripAccents(rs.getString("cognome").toUpperCase().trim()),
                            stripAccents(rs.getString("nome").toUpperCase().trim()),
                            rs.getString("codicefiscale").toUpperCase(), "ALLIEVO NEET",
                            rs.getString("email").toLowerCase());
                    out.add(u);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public List<Utenti> list_Allievi_noAccento(int idpr, int gruppo) {
        List<Utenti> out = new ArrayList<>();
        try {
            String sql = "SELECT idallievi,nome,cognome,codicefiscale,email FROM allievi WHERE id_statopartecipazione='01' AND idprogetti_formativi = " + idpr + " AND gruppo_faseB = " + gruppo;
            try (Statement st = this.c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Utenti u = new Utenti(rs.getInt("idallievi"),
                            stripAccents(rs.getString("cognome").toUpperCase().trim()),
                            stripAccents(rs.getString("nome").toUpperCase().trim()),
                            rs.getString("codicefiscale").toUpperCase(), "ALLIEVO NEET",
                            rs.getString("email").toLowerCase());
                    out.add(u);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public List<Utenti> list_Allievi(int idpr) {
        List<Utenti> out = new ArrayList<>();
        try {
            String sql = "SELECT idallievi,nome,cognome,codicefiscale,email FROM allievi WHERE id_statopartecipazione='01' AND idprogetti_formativi = " + idpr;
            try (Statement st = this.c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Utenti u = new Utenti(rs.getInt("idallievi"),
                            (rs.getString("cognome").toUpperCase().trim()),
                            (rs.getString("nome").toUpperCase().trim()),
                            rs.getString("codicefiscale").toUpperCase(), "ALLIEVO NEET",
                            rs.getString("email").toLowerCase());
                    out.add(u);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public List<Utenti> list_Docenti(int idpr) {
        List<Utenti> out = new ArrayList<>();
        try {
            String sql = "SELECT iddocenti,nome,cognome,codicefiscale,email FROM docenti WHERE iddocenti IN "
                    + "(SELECT iddocenti FROM progetti_docenti WHERE idprogetti_formativi = " + idpr + ")";
            try (Statement st = this.c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Utenti u = new Utenti(rs.getInt("iddocenti"),
                            (rs.getString("cognome").toUpperCase().trim()),
                            (rs.getString("nome").toUpperCase().trim()),
                            rs.getString("codicefiscale").toUpperCase(), "DOCENTE",
                            rs.getString("email").toLowerCase());
                    out.add(u);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public List<Utenti> list_Docenti_noAccento(int idpr) {
        List<Utenti> out = new ArrayList<>();
        try {
            String sql = "SELECT iddocenti,nome,cognome,codicefiscale,email FROM docenti WHERE iddocenti IN "
                    + "(SELECT iddocenti FROM progetti_docenti WHERE idprogetti_formativi = " + idpr + ")";
            try (Statement st = this.c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Utenti u = new Utenti(rs.getInt("iddocenti"),
                            stripAccents(rs.getString("cognome").toUpperCase().trim()),
                            stripAccents(rs.getString("nome").toUpperCase().trim()),
                            rs.getString("codicefiscale").toUpperCase(), "DOCENTE",
                            rs.getString("email").toLowerCase());
                    out.add(u);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public String[] sa_cip(int idpr) {
        try {

            String sql0 = "SELECT cip,idsoggetti_attuatori FROM progetti_formativi WHERE idprogetti_formativi = " + idpr;
            try (Statement st0 = this.c.createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
                if (rs0.next()) {
                    String cip = rs0.getString(1);
                    String sql1 = "SELECT ragionesociale FROM soggetti_attuatori WHERE idsoggetti_attuatori = " + rs0.getInt(2);
                    try (Statement st1 = this.c.createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                        if (rs1.next()) {
                            String[] out = {rs1.getString(1).trim().toUpperCase(), cip, rs0.getString(2)};
                            return out;
                        }
                    }

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public List<DatiNeet> getDatiNeet() {
        List<DatiNeet> out = new ArrayList<>();
        try {
            String sql0 = "SELECT username,pivacf,cf,protocollo,decreto,datadecreto FROM bando_neet_mcn WHERE stato_domanda='A'";
            try (Statement st0 = this.c.createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
                while (rs0.next()) {
                    out.add(new DatiNeet(rs0.getString(1), rs0.getString(2), rs0.getString(3), rs0.getString(4), rs0.getString(5), rs0.getString(6)));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return out;
    }
    
    

}


