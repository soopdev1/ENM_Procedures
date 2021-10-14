package it.refill.testingarea;


import static it.refill.exe.Constant.getCell;
import static it.refill.exe.Constant.getCellValue;
import static it.refill.exe.Constant.getRow;
import static it.refill.exe.Constant.setCell;
import it.refill.exe.Db_Bando;
import it.refill.exe.ExcelDomande;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author rcosco
 */
public class Excel {

    
    public static Connection DATABASE(String name) {
        Connection conn = null;

        try {
            String driver = "com.mysql.cj.jdbc.Driver";
            String user = "bando";
            String password = "bando";
            String host = "clustermicrocredito.cluster-c6m6yfqeypv3.eu-south-1.rds.amazonaws.com:3306/" + name;

            Class.forName(driver).newInstance();
            Properties p = new Properties();
            p.put("user", user);
            p.put("password", password);
            p.put("characterEncoding", "UTF-8");
            p.put("passwordCharacterEncoding", "UTF-8");
            p.put("useSSL", "false");
            p.put("connectTimeout", "1000");
            p.put("useUnicode", "true");
            conn = DriverManager.getConnection("jdbc:mysql://" + host, p);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ex1) {
                }
            }
            conn = null;
        }
        return conn;

    }

    public static void inserttc16(File excelin) {
        try {
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(excelin));
            XSSFSheet sh1 = wb.getSheetAt(0);
            int rows = sh1.getPhysicalNumberOfRows();
//            System.out.println("Excel.inserttc16() "+rows);
            for (int r = 3; r < rows; r++) {
                XSSFRow row = getRow(sh1, r);

                String c1 = getCellValue(getCell(row, 0));
                String c2 = getCellValue(getCell(row, 1));
                String c3 = getCellValue(getCell(row, 2));
                String c4 = getCellValue(getCell(row, 3));
                String c5 = getCellValue(getCell(row, 4));
                String c6 = getCellValue(getCell(row, 5));
                String c7 = getCellValue(getCell(row, 6));
                String c8 = getCellValue(getCell(row, 7));
                String c9 = getCellValue(getCell(row, 8));
                String c10 = getCellValue(getCell(row, 9));
                String c11 = getCellValue(getCell(row, 10));

                Connection conn = DATABASE("enm_gestione_neet");
                if (conn != null) {
                    String ins = "INSERT INTO TC16 VALUES (?,?,?,?,?,?,?,?,?,?,?)";

                    try (PreparedStatement ps = conn.prepareStatement(ins)) {
                        ps.setString(1, c1);
                        ps.setString(2, c2);
                        ps.setString(3, c3);
                        ps.setString(4, c4);
                        ps.setString(5, c5);
                        ps.setString(6, c6);
                        ps.setString(7, c7);
                        ps.setString(8, c8);
                        ps.setString(9, c9);
                        ps.setString(10, c10);
                        ps.setString(11, c11);
                        ps.executeUpdate();
                        System.out.println("OK --> " + c10);
                    }

                    conn.close();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static String formatStatoNascita(String stato_nascita, List<Nazioni_rc> nascitaconCF) {
//        try {
//            if (stato_nascita.equals("100")) {
//                return "ITALIA";
//            } else {
//                Nazioni_rc nn = nascitaconCF.stream().filter(n1 -> n1.getCodicefiscale().equalsIgnoreCase(stato_nascita)).findAny().orElse(null);
//                if (nn != null) {
//                    return nn.getNome().toUpperCase();
//                }
//            }
//        } catch (Exception e) {
//        }
//        return stato_nascita;
//    }
    

    

   public static void ReportDocentiBANDO() {
        try {

            Connection c = DATABASE("enm_neet_prod");

            Db_Bando db1 = new Db_Bando("clustermicrocredito.cluster-c6m6yfqeypv3.eu-south-1.rds.amazonaws.com:3306/enm_neet_prod");
            List<ExcelDomande> list = db1.listaconsegnate("bando_neet_mcn");
            db1.closeDB();

            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(new File("C:\\Users\\rcosco\\Downloads\\MCN\\BANDO NEET\\Report Docenti.xlsx")));
            XSSFSheet sh1 = wb.getSheetAt(0);

            AtomicInteger indice = new AtomicInteger(1);

            list.forEach(v1 -> {

                try {

                    XSSFRow row = getRow(sh1, indice.get());

                    setCell(getCell(row, 0), v1.getCODICEDOMANDA());
                    setCell(getCell(row, 1), v1.getDATACONSEGNA());
                    setCell(getCell(row, 2), v1.getORACONSEGNA());
                    setCell(getCell(row, 3), v1.getRAGIONESOCIALE());
                    setCell(getCell(row, 4), v1.getPIVA());
                    setCell(getCell(row, 5), v1.getSEDELEGALEREGIONE().toUpperCase());

                    setCell(getCell(row, 6), v1.getEMAIL().toLowerCase());
                    setCell(getCell(row, 7), v1.getTELEFONO());
                    setCell(getCell(row, 8), v1.getNDOCENTI());
                    setCell(getCell(row, 9), v1.getNOMEDOCENTE1());
                    setCell(getCell(row, 10), v1.getCOGNOMEDOCENTE1());
                    setCell(getCell(row, 11), v1.getCFDOCENTE1());
                    setCell(getCell(row, 12), v1.getFASCIAPROPOSTADOCENTE1());
                    setCell(getCell(row, 15), v1.getNOMEDOCENTE2());
                    setCell(getCell(row, 16), v1.getCOGNOMEDOCENTE2());
                    setCell(getCell(row, 17), v1.getCFDOCENTE2());
                    setCell(getCell(row, 18), v1.getFASCIAPROPOSTADOCENTE2());
                    setCell(getCell(row, 21), v1.getNOMEDOCENTE3());
                    setCell(getCell(row, 22), v1.getCOGNOMEDOCENTE3());
                    setCell(getCell(row, 23), v1.getCFDOCENTE3());
                    setCell(getCell(row, 24), v1.getFASCIAPROPOSTADOCENTE3());
                    setCell(getCell(row, 27), v1.getNOMEDOCENTE4());
                    setCell(getCell(row, 28), v1.getCOGNOMEDOCENTE4());
                    setCell(getCell(row, 29), v1.getCFDOCENTE4());
                    setCell(getCell(row, 30), v1.getFASCIAPROPOSTADOCENTE4());
                    setCell(getCell(row, 33), v1.getNOMEDOCENTE5());
                    setCell(getCell(row, 34), v1.getCOGNOMEDOCENTE5());
                    setCell(getCell(row, 35), v1.getCFDOCENTE5());
                    setCell(getCell(row, 36), v1.getFASCIAPROPOSTADOCENTE5());
                    setCell(getCell(row, 39), v1.getNPROTOCOLLO());
                    setCell(getCell(row, 42), v1.getSTATODOMANDA());

                    String sql0 = "SELECT decreto,datadecreto,rigetto FROM bando_neet_mcn WHERE username ='"
                            + v1.getUSERNAME() + "'";

                    try (ResultSet rs0 = c.createStatement().executeQuery(sql0)) {
                        if (rs0.next()) {
                            setCell(getCell(row, 40), rs0.getString(1));
                            setCell(getCell(row, 41), rs0.getString(2));
                            setCell(getCell(row, 43), rs0.getString(3));
                        }
                    }

                    String sql1 = "SELECT id,mail,tel FROM allegato_b WHERE username='" + v1.getUSERNAME() + "'";

                    try (ResultSet rs1 = c.createStatement().executeQuery(sql1)) {
                        while (rs1.next()) {
                            if (rs1.getInt("id") == 1) {
                                setCell(getCell(row, 13), rs1.getString(2));
                                setCell(getCell(row, 14), rs1.getString(3));
                            } else if (rs1.getInt("id") == 2) {
                                setCell(getCell(row, 19), rs1.getString(2));
                                setCell(getCell(row, 20), rs1.getString(3));
                            } else if (rs1.getInt("id") == 3) {
                                setCell(getCell(row, 25), rs1.getString(2));
                                setCell(getCell(row, 26), rs1.getString(3));
                            } else if (rs1.getInt("id") == 4) {
                                setCell(getCell(row, 31), rs1.getString(2));
                                setCell(getCell(row, 32), rs1.getString(3));
                            } else if (rs1.getInt("id") == 5) {
                                setCell(getCell(row, 37), rs1.getString(2));
                                setCell(getCell(row, 38), rs1.getString(3));
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                indice.addAndGet(1);
//                
//                setCell(getCell(row, 41), datadecreto);
//                setCell(getCell(row, 42), stato_domanda);
//                setCell(getCell(row, 43), rigetto);
//
//                if (rs3.getInt("id") == 1) {
//
//                    setCell(getCell(row, 13), v1.get);
//                    setCell(getCell(row, 14), tel);
//
//                } else if (rs3.getInt("id") == 2) {
//
//                    setCell(getCell(row, 19), mail);
//                    setCell(getCell(row, 20), tel);
//                } else if (rs3.getInt("id") == 3) {
//
//                    setCell(getCell(row, 25), mail);
//                    setCell(getCell(row, 26), tel);
//                } else if (rs3.getInt("id") == 4) {
//
//                    setCell(getCell(row, 31), mail);
//                    setCell(getCell(row, 32), tel);
//                } else if (rs3.getInt("id") == 5) {
//
//                    setCell(getCell(row, 37), mail);
//                    setCell(getCell(row, 38), tel);
//                }
            });

            for (int i = 0; i < 45; i++) {
                sh1.autoSizeColumn(i);
            }

            FileOutputStream outputStream = new FileOutputStream(new File("C:\\Users\\rcosco\\Downloads\\MCN\\BANDO NEET\\Report Docenti_MOD.xlsx"));
            wb.write(outputStream);
            wb.close();
            outputStream.close();

//            
//           
//             String c1 = getCellValue(getCell(row, 0));
//            System.out.println("Excel.main() "+c1);
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    

}
