package rc.so.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AtomicDouble;
import static rc.so.exe.Constant.cf_soggetto_DD;
import static rc.so.exe.Constant.codice_bb;
import static rc.so.exe.Constant.codice_yisu_neet;
import static rc.so.exe.Constant.codice_yisu_ded;
import static rc.so.exe.Constant.coeff_ddr_dd;
import static rc.so.exe.Constant.coeff_docfasciaA;
import static rc.so.exe.Constant.coeff_docfasciaB;
import static rc.so.exe.Constant.coeff_faseA;
import static rc.so.exe.Constant.coeff_faseB;
import static rc.so.exe.Constant.contoallievifaseA;
import static rc.so.exe.Constant.contoallievifaseA_DD;
import static rc.so.exe.Constant.contoallievifaseB;
import static rc.so.exe.Constant.contoallievifaseB_DD;
import static rc.so.exe.Constant.contodocentiA;
import static rc.so.exe.Constant.contodocentiA_DD;
import static rc.so.exe.Constant.contodocentiB;
import static rc.so.exe.Constant.contodocentiB_DD;
import static rc.so.exe.Constant.getCell;
import static rc.so.exe.Constant.getRow;
import static rc.so.exe.Constant.percentuale_attribuzioneDD;
import static rc.so.exe.Constant.setCell;
import static rc.so.exe.Constant.tipologia_costo;
import static rc.so.exe.Constant.tipologia_giustificativo;
import static rc.so.exe.Constant.zipListFiles;
import rc.so.exe.Db_Bando;
import rc.so.exe.Items;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;

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

    private static final String separator = "|";
    private static final SimpleDateFormat sdfHHmm = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat sdfHHmmss = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat sdfITA = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat sdfSQL = new SimpleDateFormat("yyyy-MM-dd");
    private static final String formatdataCell = "#,#.00";
    private static final String formatdataCellint = "#,#";
    private static final byte[] bianco = {(byte) 255, (byte) 255, (byte) 255};
    private static final byte[] color1 = {(byte) 49, (byte) 134, (byte) 155};
    private static final byte[] color2 = {(byte) 83, (byte) 141, (byte) 213};
    private static final byte[] color3 = {(byte) 197, (byte) 217, (byte) 241};
    private static final byte[] color4 = {(byte) 238, (byte) 30, (byte) 30};
    private static final byte[] color5 = {(byte) 0, (byte) 204, (byte) 0};
    private static final XSSFColor myColor1 = new XSSFColor(color1, new DefaultIndexedColorMap());
    private static final XSSFColor myColor2 = new XSSFColor(color2, new DefaultIndexedColorMap());
    private static final XSSFColor myColor3 = new XSSFColor(color3, new DefaultIndexedColorMap());
    private static final XSSFColor myColor4 = new XSSFColor(color4, new DefaultIndexedColorMap());
    private static final XSSFColor myColor5 = new XSSFColor(color5, new DefaultIndexedColorMap());
    private static final XSSFColor white = new XSSFColor(bianco, new DefaultIndexedColorMap());
    private static final Long hh36 = Long.valueOf(129600000);

    public static File prospetto_riepilogo_ded(int idestrazione, List<Integer> list_idpr, Db_Bando db1) { // DA FARE
        List<File> output = new ArrayList<>();
        File output_xlsx = null;

        String nomerend_cod = "R" + idestrazione;
        String nomerend = nomerend_cod + "_" + new DateTime().toString("ddMMyyyy");
        String data_giustificativo = new DateTime().toString("dd/MM/yyyy");

        try {

            String pathdest = db1.getPath("output_excel_archive");
            String pathtemp = db1.getPath("pathTemp");

            String filezip = pathdest + "/" + nomerend + ".zip";
            String fileing = pathdest + "/TEMPLATE PROSPETTO RIEPILOGO.xlsx";

            File ddr = new File(pathtemp + "/DDR.txt");
            File sd01 = new File(pathtemp + "/SD01.txt");
            File sd03 = new File(pathtemp + "/SD03.txt");
            File sd07 = new File(pathtemp + "/SD07.txt");

            BufferedWriter sd01_W = new BufferedWriter(new FileWriter(sd01));
            BufferedWriter sd03_W = new BufferedWriter(new FileWriter(sd03));
            BufferedWriter sd07_W = new BufferedWriter(new FileWriter(sd07));
            BufferedWriter ddr_W = new BufferedWriter(new FileWriter(ddr));

            AtomicDouble total_rend = new AtomicDouble(0.0);
            DateTime start_rend = null;
            DateTime end_rend = null;

            try (Connection conn = db1.getConnection()) {
                if (conn != null) {

                    try (XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(new File(fileing)))) {
                        XSSFSheet sh1 = wb.getSheet("Prospetto di riepilogo DdR L66");
                        wb.setSheetName(sh1.getWorkbook().getSheetIndex(sh1.getSheetName()), "Prospetto di riepilogo DdR " + nomerend_cod);

                        XSSFFont font_total = wb.createFont();
                        font_total.setFontHeightInPoints((short) 12);
                        font_total.setBold(true);

                        XSSFFont font_white = wb.createFont();
                        font_white.setFontHeightInPoints((short) 14);
                        font_white.setBold(true);
                        font_white.setColor(white);
                        XSSFFont font_int = wb.createFont();
                        font_int.setFontHeightInPoints((short) 14);
                        font_int.setBold(true);

                        XSSFCellStyle intestazione_1 = wb.createCellStyle();
                        intestazione_1.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_1.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_1.setBorderBottom(BorderStyle.THIN);
                        intestazione_1.setBorderTop(BorderStyle.THIN);
                        intestazione_1.setBorderRight(BorderStyle.THIN);
                        intestazione_1.setBorderLeft(BorderStyle.THIN);
                        intestazione_1.setFillForegroundColor(myColor1);
                        intestazione_1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_1.setFont(font_white);

                        XSSFCellStyle intestazione_2 = wb.createCellStyle();
                        intestazione_2.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_2.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_2.setBorderBottom(BorderStyle.THIN);
                        intestazione_2.setBorderTop(BorderStyle.THIN);
                        intestazione_2.setBorderRight(BorderStyle.THIN);
                        intestazione_2.setBorderLeft(BorderStyle.THIN);
                        intestazione_2.setFillForegroundColor(myColor2);
                        intestazione_2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_2.setFont(font_white);

                        XSSFCellStyle intestazione_3 = wb.createCellStyle();
                        intestazione_3.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_3.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_3.setBorderBottom(BorderStyle.THIN);
                        intestazione_3.setBorderTop(BorderStyle.THIN);
                        intestazione_3.setBorderRight(BorderStyle.THIN);
                        intestazione_3.setBorderLeft(BorderStyle.THIN);
                        intestazione_3.setFillForegroundColor(myColor3);
                        intestazione_3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_3.setFont(font_int);

                        XSSFCellStyle intestazione_4 = wb.createCellStyle();
                        intestazione_4.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_4.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_4.setBorderBottom(BorderStyle.THIN);
                        intestazione_4.setBorderTop(BorderStyle.THIN);
                        intestazione_4.setBorderRight(BorderStyle.THIN);
                        intestazione_4.setBorderLeft(BorderStyle.THIN);
                        intestazione_4.setFillForegroundColor(myColor4);
                        intestazione_4.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_4.setFont(font_white);
                        XSSFCellStyle intestazione_5 = wb.createCellStyle();
                        intestazione_5.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_5.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_5.setBorderBottom(BorderStyle.THIN);
                        intestazione_5.setBorderTop(BorderStyle.THIN);
                        intestazione_5.setBorderRight(BorderStyle.THIN);
                        intestazione_5.setBorderLeft(BorderStyle.THIN);
                        intestazione_5.setFillForegroundColor(myColor5);
                        intestazione_5.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_5.setFont(font_white);

                        XSSFDataFormat xssfDataFormat = wb.createDataFormat();
                        XSSFCellStyle cellStyle_int = wb.createCellStyle();
                        cellStyle_int.setBorderBottom(BorderStyle.THIN);
                        cellStyle_int.setBorderTop(BorderStyle.THIN);
                        cellStyle_int.setBorderRight(BorderStyle.THIN);
                        cellStyle_int.setBorderLeft(BorderStyle.THIN);
                        cellStyle_int.setVerticalAlignment(VerticalAlignment.CENTER);
                        cellStyle_int.setDataFormat(xssfDataFormat.getFormat(formatdataCellint));

                        XSSFCellStyle cellStyle_double = wb.createCellStyle();
                        cellStyle_double.setBorderBottom(BorderStyle.THIN);
                        cellStyle_double.setBorderTop(BorderStyle.THIN);
                        cellStyle_double.setBorderRight(BorderStyle.THIN);
                        cellStyle_double.setBorderLeft(BorderStyle.THIN);
                        cellStyle_double.setVerticalAlignment(VerticalAlignment.CENTER);
                        cellStyle_double.setDataFormat(xssfDataFormat.getFormat(formatdataCell));

                        XSSFCellStyle cs = wb.createCellStyle();
                        cs.setVerticalAlignment(VerticalAlignment.CENTER);
                        cs.setAlignment(HorizontalAlignment.CENTER);
                        cs.setBorderBottom(BorderStyle.THIN);
                        cs.setBorderTop(BorderStyle.THIN);
                        cs.setBorderRight(BorderStyle.THIN);
                        cs.setBorderLeft(BorderStyle.THIN);

                        XSSFCellStyle cstotal = wb.createCellStyle();
                        cstotal.setVerticalAlignment(VerticalAlignment.CENTER);
                        cstotal.setAlignment(HorizontalAlignment.CENTER);
                        cstotal.setBorderBottom(BorderStyle.THIN);
                        cstotal.setBorderTop(BorderStyle.THIN);
                        cstotal.setBorderRight(BorderStyle.THIN);
                        cstotal.setBorderLeft(BorderStyle.THIN);
                        cstotal.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                        cstotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cstotal.setFont(font_total);

                        XSSFCellStyle cstotal_double = wb.createCellStyle();

                        cstotal_double.setVerticalAlignment(VerticalAlignment.CENTER);
                        cstotal_double.setAlignment(HorizontalAlignment.CENTER);
                        cstotal_double.setBorderBottom(BorderStyle.THIN);
                        cstotal_double.setBorderTop(BorderStyle.THIN);
                        cstotal_double.setBorderRight(BorderStyle.THIN);
                        cstotal_double.setBorderLeft(BorderStyle.THIN);
                        cstotal_double.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                        cstotal_double.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cstotal_double.setDataFormat(xssfDataFormat.getFormat(formatdataCell));
                        cstotal_double.setFont(font_total);

                        XSSFCellStyle cstotal_int = wb.createCellStyle();

                        cstotal_int.setVerticalAlignment(VerticalAlignment.CENTER);
                        cstotal_int.setAlignment(HorizontalAlignment.CENTER);
                        cstotal_int.setBorderBottom(BorderStyle.THIN);
                        cstotal_int.setBorderTop(BorderStyle.THIN);
                        cstotal_int.setBorderRight(BorderStyle.THIN);
                        cstotal_int.setBorderLeft(BorderStyle.THIN);
                        cstotal_int.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                        cstotal_int.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cstotal_int.setDataFormat(xssfDataFormat.getFormat(formatdataCellint));
                        cstotal_int.setFont(font_total);

                        AtomicInteger indicitxt = new AtomicInteger(1);

                        AtomicInteger index_row = new AtomicInteger(9);
                        AtomicDouble oretotali = new AtomicDouble(0.0);
                        AtomicInteger indice = new AtomicInteger(1);

                        for (int ss = 0; ss < list_idpr.size(); ss++) {

                            int idpr = list_idpr.get(ss);

                            String sql1 = "SELECT p.idprogetti_formativi,p.cip,s.ragionesociale,c.regione,p.start,p.end "
                                    + "FROM progetti_formativi p, soggetti_attuatori s,comuni c WHERE p.stato='CO' "
                                    + "AND p.idsoggetti_attuatori=s.idsoggetti_attuatori AND c.idcomune=s.comune AND p.idprogetti_formativi = " + idpr;

                            try (Statement st1 = conn.createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                                if (rs1.next()) {

                                    String cip = rs1.getString(2).toUpperCase();
                                    String ragionesociale = rs1.getString(3).toUpperCase();
                                    String regione = rs1.getString(4).toUpperCase();
                                    String start = sdfITA.format(rs1.getDate(5));
                                    String end = sdfITA.format(rs1.getDate(6));

                                    if (start_rend == null || start_rend.isAfter(new DateTime(rs1.getDate(5).getTime()))) {
                                        start_rend = new DateTime(rs1.getDate(5).getTime());
                                    }
                                    if (end_rend == null || end_rend.isBefore(new DateTime(rs1.getDate(6).getTime()))) {
                                        end_rend = new DateTime(rs1.getDate(6).getTime());
                                    }

                                    Map<Long, Long> oreRendicontabili_faseA = OreRendicontabiliAlunni_faseA(conn, idpr);
//                                Map<Long, Long> oreRendicontabili_faseB = OreRendicontabiliAlunni_faseB(conn, idpr);
                                    Map<Long, Long> oreRendicontabili_docenti = OreRendicontabiliDocentiFASEA(conn, idpr);

                                    //CALCOLO PARTECIPANTI
                                    AtomicInteger numpartecipanti = new AtomicInteger(0);
                                    oreRendicontabili_faseA.forEach((idal, value) -> {
                                        if (value >= hh36) {
                                            numpartecipanti.addAndGet(1);
                                        }
                                    });

                                    StringBuilder list_outputfaseA = new StringBuilder("");

                                    String sql1A = "SELECT m.tab_completezza_output_neet FROM checklist_finale m, progetti_formativi p "
                                            + "WHERE p.id_checklist_finale=m.id AND p.idprogetti_formativi =" + idpr;
                                    try (Statement st2 = conn.createStatement(); ResultSet rs1A = st2.executeQuery(sql1A)) {
                                        if (rs1A.next()) {
                                            list_outputfaseA.append(rs1A.getString(1));
//                                        list_outputfaseA = Arrays.asList(new ObjectMapper().readValue(rs1A.getString(1), OutputId[].class));
                                        }
                                    }

                                    //imposta foglio progetto, crea intestazione 
                                    XSSFSheet sh_pr = wb.createSheet(cip);

                                    List<Items> calendario = calendario(idpr, conn);

                                    AtomicInteger indici_docenti = new AtomicInteger(15 + numpartecipanti.get());

                                    //docenti
                                    XSSFRow row_docenti = getRow(sh_pr, indici_docenti.get());
                                    CellRangeAddress region_20 = new CellRangeAddress(row_docenti.getRowNum(), row_docenti.getRowNum(), 5, 26);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_20, cs);
                                    sh_pr.addMergedRegion(region_20);

                                    setCell(getCell(row_docenti, 5), intestazione_1, "DOCENTI CORSO - FASE A", false, false);

                                    indici_docenti.addAndGet(1);
                                    XSSFRow row_docenti2 = getRow(sh_pr, indici_docenti.get());

                                    setCell(getCell(row_docenti2, 5), cs, "N.", false, false);
                                    CellRangeAddress region_21 = new CellRangeAddress(row_docenti2.getRowNum(), row_docenti2.getRowNum(), 6, 7);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_21, cs);
                                    sh_pr.addMergedRegion(region_21);
                                    setCell(getCell(row_docenti2, 6), cs, "COGNOME", false, false);

                                    setCell(getCell(row_docenti2, 8), cs, "NOME", false, false);
                                    setCell(getCell(row_docenti2, 9), cs, "FASCIA", false, false);
                                    setCell(getCell(row_docenti2, 10), cs, "€/h", false, false);

                                    for (int x = 0; x < calendario.size(); x++) {
                                        Items cal1 = calendario.get(x);
                                        if (cal1.getFase().equals("A")) {
                                            setCell(getCell(row_docenti2, 11 + x), cs, sdfITA.format(sdfSQL.parse(cal1.getData()).getTime()), false, false);
                                        }
                                    }

                                    setCell(getCell(row_docenti2, 23), cs, "TOTALE ORE\n(MAX 60h)", false, false);
                                    setCell(getCell(row_docenti2, 24), cs, "TOTALE", false, false);
                                    setCell(getCell(row_docenti2, 25), cs, "A", false, false);
                                    setCell(getCell(row_docenti2, 26), cs, "B", false, false);

                                    indici_docenti.addAndGet(1);
                                    AtomicInteger numdocenti = new AtomicInteger(0);
                                    AtomicDouble tot_docenti = new AtomicDouble(0.0);

                                    AtomicDouble tot_ore_docenti_FA = new AtomicDouble(0.0);
                                    AtomicDouble tot_ore_docenti_FB = new AtomicDouble(0.0);
                                    AtomicDouble tot_docenti_FA = new AtomicDouble(0.0);
                                    AtomicDouble tot_docenti_FB = new AtomicDouble(0.0);

                                    oreRendicontabili_docenti.forEach((iddoc, value) -> {

                                        try {
                                            String sql3 = "SELECT d.cognome,d.nome,d.codicefiscale,d.fascia "
                                                    + "FROM docenti d,progetti_docenti p  WHERE d.iddocenti = p.iddocenti "
                                                    + "AND d.iddocenti=" + iddoc + " AND p.idprogetti_formativi=" + idpr;

                                            try (Statement st3 = conn.createStatement(); ResultSet rs3 = st3.executeQuery(sql3)) {
                                                if (rs3.next()) {
                                                    numdocenti.addAndGet(1);
                                                    String cognome = rs3.getString(1).toUpperCase();
                                                    String nome = rs3.getString(2).toUpperCase();
                                                    String fascia = rs3.getString(4).toUpperCase().equals("FA") ? "A" : "B";

                                                    XSSFRow row_d = getRow(sh_pr, indici_docenti.get());
                                                    setCell(getCell(row_d, 5), cs, String.valueOf(numdocenti.get()), true, false);

                                                    CellRangeAddress region_22 = new CellRangeAddress(row_d.getRowNum(), row_d.getRowNum(), 6, 7);
                                                    cleanBeforeMergeOnValidCells(sh_pr, region_22, cs);
                                                    sh_pr.addMergedRegion(region_22);

                                                    setCell(getCell(row_d, 6), cs, cognome, false, false);

                                                    setCell(getCell(row_d, 8), cs, nome, false, false);
                                                    setCell(getCell(row_d, 9), cs, fascia, false, false);
                                                    double coeff_docA = fascia.equals("A") ? coeff_docfasciaA : coeff_docfasciaB;
                                                    setCell(getCell(row_d, 10), cellStyle_double, String.valueOf(coeff_docA), false, true);

                                                    String sql2A = "SELECT r.data,r.totaleorerendicontabili FROM registro_completo r "
                                                            + "WHERE r.idutente=" + iddoc + " AND r.ruolo LIKE 'DOCENTE' AND r.idprogetti_formativi=" + idpr;
                                                    HashMap<String, Double> presenza_doc = new HashMap<>();

                                                    try (Statement st2A = conn.createStatement(); ResultSet rs2A = st2A.executeQuery(sql2A)) {
                                                        while (rs2A.next()) {
                                                            presenza_doc.put(rs2A.getString(1), roundFloatAndFormat(rs2A.getLong(2)));
                                                        }
                                                    }

                                                    AtomicDouble tot_ore_fase_A = new AtomicDouble(0.0);

                                                    for (int x = 0; x < calendario.size(); x++) {
                                                        Items cal1 = calendario.get(x);
                                                        if (cal1.getFase().equals("A")) {
                                                            if (presenza_doc.get(cal1.getData()) != null) {
                                                                setCell(getCell(row_d, 11 + x),
                                                                        cellStyle_double,
                                                                        String.valueOf(presenza_doc.get(cal1.getData())),
                                                                        false, true);
                                                                tot_ore_fase_A.addAndGet(presenza_doc.get(cal1.getData()));
                                                            } else {
                                                                setCell(getCell(row_d, 11 + x),
                                                                        cellStyle_double,
                                                                        "0.00",
                                                                        false, true);
                                                            }
                                                        }
                                                    }

                                                    setCell(getCell(row_d, 23), cellStyle_double,
                                                            String.valueOf(tot_ore_fase_A.get()),
                                                            false, true);

                                                    String totali_fascia_A = "0.00";
                                                    String totali_fascia_B = "0.00";

                                                    double tot_d1 = tot_ore_fase_A.get() * coeff_docA;

                                                    if (fascia.equals("A")) {
                                                        totali_fascia_A = String.valueOf(tot_d1);
                                                        tot_docenti_FA.addAndGet(tot_d1);
                                                        tot_ore_docenti_FA.addAndGet(tot_ore_fase_A.get());
                                                    } else {
                                                        totali_fascia_B = String.valueOf(tot_d1);
                                                        tot_docenti_FB.addAndGet(tot_d1);
                                                        tot_ore_docenti_FB.addAndGet(tot_ore_fase_A.get());
                                                    }
                                                    tot_docenti.addAndGet(tot_d1);

                                                    setCell(getCell(row_d, 24), cellStyle_double, String.valueOf(tot_d1), false, true);
                                                    setCell(getCell(row_d, 25), cellStyle_double, totali_fascia_A, false, true);
                                                    setCell(getCell(row_d, 26), cellStyle_double, totali_fascia_B, false, true);

                                                    indici_docenti.addAndGet(1);
                                                }
                                            }
                                        } catch (Exception ex3) {
                                            ex3.printStackTrace();
                                        }

                                    });

                                    //TOTALI DOCENTE 
                                    XSSFRow row_total_d = getRow(sh_pr, indici_docenti.get());

                                    setCell(getCell(row_total_d, 24), cstotal_double, String.valueOf(tot_docenti.get()), false, true);
                                    setCell(getCell(row_total_d, 25), cstotal_double, String.valueOf(tot_docenti_FA.get()), false, true);
                                    setCell(getCell(row_total_d, 26), cstotal_double, String.valueOf(tot_docenti_FB.get()), false, true);

                                    //ALLIEVI
                                    XSSFRow row_intest = getRow(sh_pr, 2);
                                    XSSFRow row_intest2 = getRow(sh_pr, 3);
                                    XSSFRow row_intest3 = getRow(sh_pr, 4);
                                    XSSFRow row_intest4 = getRow(sh_pr, 5);
                                    XSSFRow row_intest5 = getRow(sh_pr, 6);

                                    CellRangeAddress region_1 = new CellRangeAddress(2, 3, 1, 6);
                                    CellRangeAddress region_2 = new CellRangeAddress(2, 3, 7, 10);
                                    CellRangeAddress region_3 = new CellRangeAddress(2, 2, 11, 28);
                                    CellRangeAddress region_4 = new CellRangeAddress(4, 6, 1, 1);
                                    CellRangeAddress region_5 = new CellRangeAddress(4, 6, 2, 2);
                                    CellRangeAddress region_6 = new CellRangeAddress(4, 6, 3, 3);
                                    CellRangeAddress region_7 = new CellRangeAddress(4, 6, 4, 4);
                                    CellRangeAddress region_8 = new CellRangeAddress(4, 6, 5, 5);
                                    CellRangeAddress region_9 = new CellRangeAddress(4, 6, 6, 6);
                                    CellRangeAddress region_10 = new CellRangeAddress(4, 6, 7, 7);
                                    CellRangeAddress region_11 = new CellRangeAddress(4, 6, 8, 8);
                                    CellRangeAddress region_12 = new CellRangeAddress(4, 6, 9, 9);
                                    CellRangeAddress region_13 = new CellRangeAddress(4, 6, 10, 10);

                                    cleanBeforeMergeOnValidCells(sh_pr, region_1, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_2, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_3, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_4, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_5, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_6, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_7, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_8, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_9, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_10, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_11, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_12, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_13, cs);

                                    sh_pr.addMergedRegion(region_1);
                                    sh_pr.addMergedRegion(region_2);
                                    sh_pr.addMergedRegion(region_3);
                                    sh_pr.addMergedRegion(region_4);
                                    sh_pr.addMergedRegion(region_5);
                                    sh_pr.addMergedRegion(region_6);
                                    sh_pr.addMergedRegion(region_7);
                                    sh_pr.addMergedRegion(region_8);
                                    sh_pr.addMergedRegion(region_9);
                                    sh_pr.addMergedRegion(region_10);
                                    sh_pr.addMergedRegion(region_11);
                                    sh_pr.addMergedRegion(region_12);
                                    sh_pr.addMergedRegion(region_13);

                                    setCell(getCell(row_intest, 1), intestazione_1, "ANAGRAFICA \nPERCORSO", false, false);
                                    setCell(getCell(row_intest, 7), intestazione_2, "ANAGRAFICA \nPARTECIPANTI", false, false);
                                    setCell(getCell(row_intest, 11), intestazione_3, "FASE A\nDATE", false, false);
                                    setCell(getCell(row_intest3, 1), cs, "N.", false, false);
                                    setCell(getCell(row_intest3, 2), cs, "ID", false, false);
                                    setCell(getCell(row_intest3, 3), cs, "CIP", false, false);
                                    setCell(getCell(row_intest3, 4), cs, "SA", false, false);
                                    setCell(getCell(row_intest3, 5), cs, "DATA\nINIZIO CORSO", false, false);
                                    setCell(getCell(row_intest3, 6), cs, "DATA\nFINE CORSO", false, false);
                                    setCell(getCell(row_intest3, 7), cs, "COGNOME", false, false);
                                    setCell(getCell(row_intest3, 8), cs, "NOME", false, false);
                                    setCell(getCell(row_intest3, 9), cs, "CODICE FISCALE", false, false);
                                    setCell(getCell(row_intest3, 10), cs, "OUTPUT (SI/NO)", false, false);

                                    String datainizioA = "";
                                    String datafineA = "";

                                    boolean okA = true;
                                    for (int x = 0; x < calendario.size(); x++) {
                                        Items cal1 = calendario.get(x);
                                        if (cal1.getFase().equals("A")) {
                                            if (datainizioA.equals("") || okA) {
                                                datainizioA = sdfITA.format(sdfSQL.parse(cal1.getData()).getTime());
                                                okA = false;
                                            }
                                            datafineA = sdfITA.format(sdfSQL.parse(cal1.getData()).getTime());

                                            setCell(getCell(row_intest3, 11 + x), cs, sdfITA.format(sdfSQL.parse(cal1.getData()).getTime()), false, false);
                                            setCell(getCell(row_intest4, 11 + x), cs, sdfHHmm.format(sdfHHmmss.parse(cal1.getOrainizio()).getTime()), false, false);
                                            setCell(getCell(row_intest5, 11 + x), cs, sdfHHmm.format(sdfHHmmss.parse(cal1.getOrafine()).getTime()), false, false);

                                        }

                                    }

                                    CellRangeAddress region_14 = new CellRangeAddress(3, 3, 11, 28);
                                    CellRangeAddress region_15 = new CellRangeAddress(4, 6, 23, 23);
                                    CellRangeAddress region_16 = new CellRangeAddress(4, 6, 24, 24);
                                    CellRangeAddress region_17 = new CellRangeAddress(4, 6, 25, 25);
                                    CellRangeAddress region_18 = new CellRangeAddress(4, 6, 26, 26);
                                    CellRangeAddress region_19 = new CellRangeAddress(4, 6, 27, 27);
                                    CellRangeAddress region_19A = new CellRangeAddress(4, 6, 28, 28);

                                    cleanBeforeMergeOnValidCells(sh_pr, region_14, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_15, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_16, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_17, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_18, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_19, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_19A, cs);

                                    sh_pr.addMergedRegion(region_14);
                                    sh_pr.addMergedRegion(region_15);
                                    sh_pr.addMergedRegion(region_16);
                                    sh_pr.addMergedRegion(region_17);
                                    sh_pr.addMergedRegion(region_18);
                                    sh_pr.addMergedRegion(region_19);
                                    sh_pr.addMergedRegion(region_19A);

                                    setCell(getCell(row_intest2, 11), intestazione_3, "DAL " + datainizioA + " AL " + datafineA, false, false);
                                    setCell(getCell(row_intest3, 23), cs, "TOTALE ORE\n(MAX 60h)", false, false);
                                    setCell(getCell(row_intest3, 24), cs, "TOTALE ORE\n * \n0,80€", false, false);
                                    setCell(getCell(row_intest3, 25), cs, "Quota parte totale docente\n(totale/n. partecipanti)", false, false);
                                    setCell(getCell(row_intest3, 26), cs, "Quota parte totale docente\n(totale/n. partecipanti)\nFASCIA A", false, false);
                                    setCell(getCell(row_intest3, 27), cs, "Quota parte totale docente\n(totale/n. partecipanti)\nFASCIA B", false, false);
                                    setCell(getCell(row_intest3, 28), cs, "TOTALE FASE A", false, false);

                                    List<String> elencogruppi_B = calendario.stream().filter(it1 -> it1.getFase().equals("B"))
                                            .map(Items::getGruppo).distinct().sorted().collect(Collectors.toList());

//                                System.out.println("it.refill.testingarea.Excel.main() "+elencogruppi_B.toString());
                                    AtomicInteger iniziofaseb = new AtomicInteger(29);
                                    int iniziofaseb_i;

                                    HashMap<String, Integer> indicifaseB = new HashMap<>();

                                    for (int ix = 0; ix < elencogruppi_B.size(); ix++) {
                                        String numerogruppo = elencogruppi_B.get(ix);
                                        String datainizioB = "";
                                        String datafineB = "";
                                        boolean ok_B = true;

                                        iniziofaseb_i = iniziofaseb.get();

                                        List<Items> calendario2 = calendario.stream().filter(c1 -> c1.getGruppo().equals(numerogruppo)).collect(Collectors.toList());

                                        for (int x = 0; x < calendario2.size(); x++) {
                                            Items cal1 = calendario2.get(x);
                                            if (cal1.getFase().equals("B") && cal1.getGruppo().equals(numerogruppo)) {
                                                if (datainizioB.equals("") || ok_B) {
                                                    indicifaseB.put(numerogruppo, iniziofaseb_i);
                                                    datainizioB = sdfITA.format(sdfSQL.parse(cal1.getData()).getTime());
                                                    ok_B = false;
                                                }
                                                datafineB = sdfITA.format(sdfSQL.parse(cal1.getData()).getTime());

                                                setCell(getCell(row_intest3, iniziofaseb.get()), cs, sdfITA.format(sdfSQL.parse(cal1.getData()).getTime()), false, false);
                                                setCell(getCell(row_intest4, iniziofaseb.get()), cs, sdfHHmm.format(sdfHHmmss.parse(cal1.getOrainizio()).getTime()), false, false);
                                                setCell(getCell(row_intest5, iniziofaseb.get()), cs, sdfHHmm.format(sdfHHmmss.parse(cal1.getOrafine()).getTime()), false, false);

                                                //popola caselle con 0.00
                                                for (int xx = 1; xx <= numpartecipanti.get(); xx++) {
                                                    setCell(getCell(getRow(sh_pr, 6 + xx), iniziofaseb.get()),
                                                            cellStyle_double,
                                                            "0.00",
                                                            false, true);
                                                }

                                                iniziofaseb.addAndGet(1);

                                            }
                                        }
                                        for (int xx = 1; xx <= numpartecipanti.get(); xx++) {
                                            setCell(getCell(getRow(sh_pr, 6 + xx), iniziofaseb.get()),
                                                    cellStyle_double,
                                                    "0.00",
                                                    false, true);
                                        }
                                        iniziofaseb.addAndGet(1);
                                        for (int xx = 1; xx <= numpartecipanti.get(); xx++) {
                                            setCell(getCell(getRow(sh_pr, 6 + xx), iniziofaseb.get()),
                                                    cellStyle_double,
                                                    "0.00",
                                                    false, true);
                                        }
                                        iniziofaseb.addAndGet(1);

                                        CellRangeAddress region_B1 = new CellRangeAddress(2, 2, iniziofaseb_i, iniziofaseb.get() - 1);
                                        cleanBeforeMergeOnValidCells(sh_pr, region_B1, cs);
                                        sh_pr.addMergedRegion(region_B1);
                                        CellRangeAddress region_B2 = new CellRangeAddress(3, 3, iniziofaseb_i, iniziofaseb.get() - 1);
                                        cleanBeforeMergeOnValidCells(sh_pr, region_B2, cs);
                                        sh_pr.addMergedRegion(region_B2);
                                        setCell(getCell(row_intest, iniziofaseb_i), intestazione_2, "FASE B GRUPPO " + numerogruppo + "\nDATE", false, false);
                                        setCell(getCell(row_intest2, iniziofaseb_i), intestazione_2, "DAL " + datainizioB + " AL " + datafineB, false, false);

                                        CellRangeAddress region_B3 = new CellRangeAddress(4, 6, iniziofaseb.get() - 2, iniziofaseb.get() - 2);
                                        cleanBeforeMergeOnValidCells(sh_pr, region_B3, cs);
                                        sh_pr.addMergedRegion(region_B3);
                                        setCell(getCell(row_intest3, iniziofaseb.get() - 2), cs, "TOTALE ORE\n(MAX 20h)", false, false);
                                        CellRangeAddress region_B4 = new CellRangeAddress(4, 6, iniziofaseb.get() - 1, iniziofaseb.get() - 1);
                                        cleanBeforeMergeOnValidCells(sh_pr, region_B4, cs);
                                        sh_pr.addMergedRegion(region_B4);
                                        setCell(getCell(row_intest3, iniziofaseb.get() - 1), cs, "TOTALE ORE FASE B\n * \n40€", false, false);
                                    }

//                                System.out.println(cip + " : " + indicifaseB.toString());
                                    AtomicInteger index_allievo = new AtomicInteger(7);
                                    AtomicDouble total_allievo_1 = new AtomicDouble(0.0);
                                    AtomicDouble totale_fa_allievi = new AtomicDouble(0.0);

                                    AtomicDouble totale_pr = new AtomicDouble(0.0);
                                    AtomicDouble totale_pr_70 = new AtomicDouble(0.0);
                                    AtomicDouble totale_pr_30 = new AtomicDouble(0.0);

                                    double quota_parte_totale = tot_docenti.get() / Double.valueOf(String.valueOf(numpartecipanti.get()));
                                    double quota_parte_totale_FA = tot_docenti_FA.get() / Double.valueOf(String.valueOf(numpartecipanti.get()));
                                    double quota_parte_totale_FB = tot_docenti_FB.get() / Double.valueOf(String.valueOf(numpartecipanti.get()));

                                    HashMap<String, Double> totali_gruppiB = new HashMap<>();

                                    oreRendicontabili_faseA.forEach((idal, value) -> {

                                        if (value >= hh36) {
                                            try {

                                                //creazione righe allievo
                                                String sql2 = "SELECT a.cognome,a.nome,a.codicefiscale,a.gruppo_faseB FROM allievi a WHERE a.idallievi=" + idal + " ";
                                                try (Statement st2 = conn.createStatement(); ResultSet rs2 = st2.executeQuery(sql2)) {
                                                    if (rs2.next()) {

                                                        String cognome = rs2.getString(1).toUpperCase();
                                                        String nome = rs2.getString(2).toUpperCase();
                                                        String codicefiscale = rs2.getString(3).toUpperCase();
                                                        String gruppo_faseB = rs2.getString(4);

//                                                    System.out.println(cognome + " it.refill.testingarea.Excel.main() " + gruppo_faseB);
                                                        int indicepartenzafaseBallievo = 0;

                                                        XSSFRow row_allievo = getRow(sh_pr, index_allievo.get());
                                                        setCell(getCell(row_allievo, 1), cellStyle_int, String.valueOf(index_allievo.get() - 6), true, false);
                                                        setCell(getCell(row_allievo, 2), cellStyle_int, String.valueOf(idpr), true, false);
                                                        setCell(getCell(row_allievo, 3), cs, cip, false, false);
                                                        setCell(getCell(row_allievo, 4), cs, ragionesociale, false, false);
                                                        setCell(getCell(row_allievo, 5), cs, start, false, false);
                                                        setCell(getCell(row_allievo, 6), cs, end, false, false);
                                                        setCell(getCell(row_allievo, 7), cs, cognome, false, false);
                                                        setCell(getCell(row_allievo, 8), cs, nome, false, false);
                                                        setCell(getCell(row_allievo, 9), cs, codicefiscale, false, false);
                                                        OutputId output_a = Arrays.asList(new ObjectMapper().readValue(list_outputfaseA.toString(), OutputId[].class)).stream()
                                                                .filter(a1 -> a1.getId()
                                                                .equals(String.valueOf(idal))).findAny().orElse(null);
                                                        if (output_a == null || output_a.getOutput().equals("0")) {
                                                            setCell(getCell(row_allievo, 10), cs, "NO", false, false);
                                                        } else {
                                                            setCell(getCell(row_allievo, 10), cs, "SI", false, false);
                                                        }

                                                        long la = 0L;
                                                        long lb = 0L;

                                                        String sql2A = "SELECT r.data,r.totaleorerendicontabili,r.fase FROM registro_completo r "
                                                                + "WHERE r.idutente=" + idal + " AND r.ruolo LIKE 'ALLIEVO%' AND r.idprogetti_formativi=" + idpr;
                                                        HashMap<String, Double> presenza = new HashMap<>();

                                                        try (Statement st2A = conn.createStatement(); ResultSet rs2A = st2A.executeQuery(sql2A)) {
                                                            while (rs2A.next()) {
                                                                if (rs2A.getString(3).equals("A")) {
                                                                    la += (rs2A.getLong(2));
                                                                } else {
                                                                    lb += (rs2A.getLong(2));
                                                                }
                                                                presenza.put(rs2A.getString(1), roundFloatAndFormat(rs2A.getLong(2)));
                                                            }
                                                        }

//                                                    AtomicDouble tot_A = new AtomicDouble(0.0);
//                                                    AtomicDouble tot_B = new AtomicDouble(0.0);
                                                        int yb = 0;
                                                        for (int x = 0; x < calendario.size(); x++) {
                                                            Items cal1 = calendario.get(x);
                                                            if (cal1.getFase().equals("A")) {

                                                                if (presenza.get(cal1.getData()) != null) {

                                                                    BigDecimal bigDecimal = new BigDecimal(presenza.get(cal1.getData()));
                                                                    bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_EVEN);

                                                                    setCell(getCell(row_allievo, 11 + x),
                                                                            cellStyle_double,
                                                                            String.valueOf(bigDecimal.toString()),
                                                                            false, true);
//                                                                tot_A.addAndGet(presenza.get(cal1.getData()));
                                                                } else {
                                                                    setCell(getCell(row_allievo, 11 + x),
                                                                            cellStyle_double,
                                                                            "0.00",
                                                                            false, true);
                                                                }

                                                            } else {
                                                                if (cal1.getGruppo().equals(gruppo_faseB)) {

                                                                    indicepartenzafaseBallievo = indicifaseB.get(gruppo_faseB);

                                                                    if (presenza.get(cal1.getData()) != null) {
                                                                        setCell(getCell(row_allievo, indicepartenzafaseBallievo + yb),
                                                                                cellStyle_double,
                                                                                String.valueOf(presenza.get(cal1.getData())),
                                                                                false, true);
//                                                                    tot_B.addAndGet(presenza.get(cal1.getData()));

                                                                    } else {
                                                                        setCell(getCell(row_allievo, indicepartenzafaseBallievo + yb),
                                                                                cellStyle_double,
                                                                                "0.00",
                                                                                false, true);
                                                                    }
                                                                    yb++;
                                                                }
                                                            }

                                                        }

                                                        BigDecimal bigDecimal_ta = new BigDecimal(roundFloatAndFormat(la));
                                                        bigDecimal_ta = bigDecimal_ta.setScale(2, RoundingMode.HALF_EVEN);
                                                        BigDecimal bigDecimal_tb = new BigDecimal(roundFloatAndFormat(lb));
                                                        bigDecimal_tb = bigDecimal_tb.setScale(2, RoundingMode.HALF_EVEN);

                                                        double tot_A = bigDecimal_ta.doubleValue();
                                                        double tot_B = bigDecimal_tb.doubleValue();

                                                        double tot_single = tot_A * coeff_faseA;

                                                        setCell(getCell(row_allievo, 23),
                                                                cellStyle_double,
                                                                bigDecimal_ta.toString(),
                                                                false, true);
                                                        setCell(getCell(row_allievo, 24),
                                                                cellStyle_double,
                                                                String.valueOf(tot_single),
                                                                false, true);

                                                        setCell(getCell(row_allievo, 25),
                                                                cellStyle_double,
                                                                String.valueOf(quota_parte_totale),
                                                                false, true);
                                                        setCell(getCell(row_allievo, 26),
                                                                cellStyle_double,
                                                                String.valueOf(quota_parte_totale_FA),
                                                                false, true);

                                                        setCell(getCell(row_allievo, 27),
                                                                cellStyle_double,
                                                                String.valueOf(quota_parte_totale_FB),
                                                                false, true);

                                                        double total_fa_allievo = tot_single + quota_parte_totale;

                                                        setCell(getCell(row_allievo, 28),
                                                                cellStyle_double,
                                                                String.valueOf(total_fa_allievo),
                                                                false, true);

                                                        total_allievo_1.addAndGet(tot_single);
                                                        totale_fa_allievi.addAndGet(total_fa_allievo);

                                                        double tot_single_row = total_fa_allievo;

                                                        //b
                                                        double tot_single_B = 0.0;
                                                        if (indicepartenzafaseBallievo > 0) {
                                                            tot_single_B = tot_B * coeff_faseB;
                                                            tot_single_row += tot_single_B;
                                                            setCell(getCell(row_allievo, indicepartenzafaseBallievo + 4),
                                                                    cellStyle_double,
                                                                    String.valueOf(tot_B),
                                                                    false, true);
                                                            setCell(getCell(row_allievo, indicepartenzafaseBallievo + 5),
                                                                    cellStyle_double,
                                                                    String.valueOf(tot_single_B),
                                                                    false, true);

                                                            if (totali_gruppiB.get(gruppo_faseB) == null) {
                                                                totali_gruppiB.put(gruppo_faseB, tot_single_B);
                                                            } else {
                                                                totali_gruppiB.put(gruppo_faseB, totali_gruppiB.get(gruppo_faseB) + tot_single_B);
                                                            }

                                                        }

                                                        setCell(getCell(row_allievo, iniziofaseb.get()),
                                                                cellStyle_double,
                                                                String.valueOf(tot_A + tot_B), false, true);

                                                        setCell(getCell(row_allievo, iniziofaseb.get() + 1),
                                                                cellStyle_double,
                                                                String.valueOf(tot_single_row), false, true);

                                                        double tot_single_row_30 = tot_single_row * 30.00 / 100.00;
                                                        double tot_single_row_70 = tot_single_row - tot_single_row_30;

                                                        setCell(getCell(row_allievo, iniziofaseb.get() + 2),
                                                                cellStyle_double,
                                                                String.valueOf(tot_single_row_70),
                                                                false, true);
                                                        setCell(getCell(row_allievo, iniziofaseb.get() + 3),
                                                                cellStyle_double,
                                                                String.valueOf(tot_single_row_30),
                                                                false, true);

                                                        totale_pr.addAndGet(tot_single_row);
                                                        totale_pr_30.addAndGet(tot_single_row_30);
                                                        totale_pr_70.addAndGet(tot_single_row_70);

                                                        index_allievo.addAndGet(1);

                                                        double oreprogetto = tot_A + tot_B;
                                                        oretotali.addAndGet(oreprogetto);
                                                        XSSFRow row = getRow(sh1, index_row.get());
                                                        setCell(getCell(row, 0), cellStyle_int, String.valueOf(indice.get()), true, false);
                                                        setCell(getCell(row, 1), cellStyle_int, String.valueOf(idpr), true, false);
                                                        setCell(getCell(row, 2), cs, cip, false, false);
                                                        setCell(getCell(row, 3), cs, ragionesociale, false, false);
                                                        setCell(getCell(row, 4), cs, regione, false, false);
                                                        setCell(getCell(row, 5), cs, cognome, false, false);
                                                        setCell(getCell(row, 6), cs, nome, false, false);
                                                        setCell(getCell(row, 7), cs, codicefiscale, false, false);
                                                        setCell(getCell(row, 8), cs, start, false, false);
                                                        setCell(getCell(row, 9), cs, end, false, false);
                                                        setCell(getCell(row, 10),
                                                                cellStyle_double,
                                                                String.valueOf(oreprogetto),
                                                                false, true);

                                                        index_row.addAndGet(1);
                                                        indice.addAndGet(1);

                                                        if (tot_ore_docenti_FA.get() > 0) {
                                                            String indice_dainserire = get_incremental(indicitxt.get());
                                                            indicitxt.addAndGet(1);

                                                            sd01_W.write(
                                                                    indice_dainserire + separator
                                                                    + codicefiscale + separator
                                                                    + codice_yisu_ded + separator
                                                                    + indice_dainserire + separator
                                                                    + contodocentiA_DD + separator
                                                                    + getDoubleforTXT(tot_ore_docenti_FA.get()) + separator
                                                                    + getDoubleforTXT(quota_parte_totale_FA) + separator
                                                                    + percentuale_attribuzioneDD + separator
                                                                    + tipologia_costo + separator
                                                                    + cf_soggetto_DD + separator
                                                                    + "3" + separator
                                                                    + "1" + separator
                                                                    + separator
                                                                    + nomerend + separator
                                                                    + separator
                                                                    + "S" + separator
                                                                    + "2021" + separator
                                                                    + "0" + separator + separator + separator + separator + separator + separator + separator
                                                            );
                                                            sd01_W.newLine();

                                                            sd03_W.write(indice_dainserire + separator + codicefiscale + "_" + cip + ".pdf" + separator + "G" + separator + "RP"
                                                                    + separator + indice_dainserire + separator + "CV Docente; Doc. accompagnamento Neet; Domanda iscrizione Neet; Patto di servizio o PIP; SAP; Documentazione neet; Resgistri A e B; Output neet");
                                                            sd03_W.newLine();

                                                            sd07_W.write(
                                                                    indice_dainserire + separator
                                                                    + separator
                                                                    + tipologia_giustificativo + separator
                                                                    + "Documentazione relativa allo svolgimento del percorso formativo" + separator
                                                                    + codicefiscale + separator
                                                                    + data_giustificativo + separator
                                                                    + getDoubleforTXT(quota_parte_totale_FA) + separator
                                                                    + "Documentazione percorso formativo: CV Docente; Doc. accompagnamento Neet; Domanda iscrizione Neet; Patto di servizio o PIP; SAP; Documentazione neet; Registri A e B; Output neet"
                                                            );
                                                            sd07_W.newLine();

                                                        }
                                                        if (tot_ore_docenti_FB.get() > 0) {
                                                            String indice_dainserire = get_incremental(indicitxt.get());
                                                            indicitxt.addAndGet(1);
                                                            sd01_W.write(
                                                                    indice_dainserire + separator
                                                                    + codicefiscale + separator
                                                                    + codice_yisu_ded + separator
                                                                    + indice_dainserire + separator
                                                                    + contodocentiB_DD + separator
                                                                    + getDoubleforTXT(tot_ore_docenti_FB.get()) + separator
                                                                    + getDoubleforTXT(quota_parte_totale_FB) + separator
                                                                    + percentuale_attribuzioneDD + separator
                                                                    + tipologia_costo + separator
                                                                    + cf_soggetto_DD + separator
                                                                    + "3" + separator
                                                                    + "3" + separator
                                                                    + separator
                                                                    + nomerend + separator
                                                                    + separator
                                                                    + "S" + separator
                                                                    + "2021" + separator
                                                                    + "0" + separator + separator + separator + separator + separator + separator + separator
                                                            );

                                                            sd01_W.newLine();

                                                            sd03_W.write(indice_dainserire + separator + codicefiscale + "_" + cip + ".pdf" + separator + "G" + separator + "RP"
                                                                    + separator + indice_dainserire + separator + "CV Docente; Doc. accompagnamento Neet; Domanda iscrizione Neet; Patto di servizio o PIP; SAP; Documentazione neet; Resgistri A e B; Output neet");
                                                            sd03_W.newLine();

                                                            sd07_W.write(
                                                                    indice_dainserire + separator
                                                                    + separator
                                                                    + tipologia_giustificativo + separator
                                                                    + "Documentazione relativa allo svolgimento del percorso formativo" + separator
                                                                    + codicefiscale + separator
                                                                    + data_giustificativo + separator
                                                                    + getDoubleforTXT(quota_parte_totale_FB) + separator
                                                                    + "Documentazione percorso formativo: CV Docente; Doc. accompagnamento Neet; Domanda iscrizione Neet; Patto di servizio o PIP; SAP; Documentazione neet; Registri A e B; Output neet"
                                                            );
                                                            sd07_W.newLine();

                                                        }
                                                        String indice_dainserire1 = get_incremental(indicitxt.get());
                                                        indicitxt.addAndGet(1);
                                                        sd01_W.write(
                                                                indice_dainserire1 + separator
                                                                + codicefiscale + separator
                                                                + codice_yisu_ded + separator
                                                                + indice_dainserire1 + separator
                                                                + contoallievifaseA_DD + separator
                                                                + getDoubleforTXT(tot_A) + separator
                                                                + getDoubleforTXT(tot_single) + separator
                                                                + percentuale_attribuzioneDD + separator
                                                                + tipologia_costo + separator
                                                                + cf_soggetto_DD + separator
                                                                + "3" + separator
                                                                + "3" + separator
                                                                + separator
                                                                + nomerend + separator
                                                                + separator
                                                                + "S" + separator
                                                                + "2021" + separator
                                                                + "0" + separator + separator + separator + separator + separator + separator + separator
                                                        );
                                                        sd01_W.newLine();

                                                        sd03_W.write(indice_dainserire1 + separator + codicefiscale + "_" + cip + ".pdf" + separator + "G" + separator + "RP"
                                                                + separator + indice_dainserire1 + separator + "CV Docente; Doc. accompagnamento Neet; Domanda iscrizione Neet; Patto di servizio o PIP; SAP; Documentazione neet; Resgistri A e B; Output neet");
                                                        sd03_W.newLine();

                                                        sd07_W.write(
                                                                indice_dainserire1 + separator
                                                                + separator
                                                                + tipologia_giustificativo + separator
                                                                + "Documentazione relativa allo svolgimento del percorso formativo" + separator
                                                                + codicefiscale + separator
                                                                + data_giustificativo + separator
                                                                + getDoubleforTXT(tot_single) + separator
                                                                + "Documentazione percorso formativo: CV Docente; Doc. accompagnamento Neet; Domanda iscrizione Neet; Patto di servizio o PIP; SAP; Documentazione neet; Registri A e B; Output neet"
                                                        );
                                                        sd07_W.newLine();

                                                        String indice_dainserire2 = get_incremental(indicitxt.get());
                                                        indicitxt.addAndGet(1);
                                                        sd01_W.write(
                                                                indice_dainserire2 + separator
                                                                + codicefiscale + separator
                                                                + codice_yisu_ded + separator
                                                                + indice_dainserire2 + separator
                                                                + contoallievifaseB_DD + separator
                                                                + getDoubleforTXT(tot_B) + separator
                                                                + getDoubleforTXT(tot_single_B) + separator
                                                                + percentuale_attribuzioneDD + separator
                                                                + tipologia_costo + separator
                                                                + cf_soggetto_DD + separator
                                                                + "3" + separator
                                                                + "3" + separator
                                                                + separator
                                                                + nomerend + separator
                                                                + separator
                                                                + "S" + separator
                                                                + "2021" + separator
                                                                + "0" + separator + separator + separator + separator + separator + separator + separator
                                                        );
                                                        sd01_W.newLine();

                                                        sd03_W.write(indice_dainserire2 + separator + codicefiscale + "_" + cip + ".pdf" + separator + "G" + separator + "RP"
                                                                + separator + indice_dainserire2 + separator + "CV Docente; Doc. accompagnamento Neet; Domanda iscrizione Neet; Patto di servizio o PIP; SAP; Documentazione neet; Resgistri A e B; Output neet");
                                                        sd03_W.newLine();

                                                        sd07_W.write(
                                                                indice_dainserire2 + separator
                                                                + separator
                                                                + tipologia_giustificativo + separator
                                                                + "Documentazione relativa allo svolgimento del percorso formativo" + separator
                                                                + codicefiscale + separator
                                                                + data_giustificativo + separator
                                                                + getDoubleforTXT(tot_single_B) + separator
                                                                + "Documentazione percorso formativo: CV Docente; Doc. accompagnamento Neet; Domanda iscrizione Neet; Patto di servizio o PIP; SAP; Documentazione neet; Registri A e B; Output neet"
                                                        );
                                                        sd07_W.newLine();

                                                    }
                                                }
                                            } catch (Exception ex2) {
                                                ex2.printStackTrace();
                                            }
                                        }

                                    });

                                    CellRangeAddress region_C1 = new CellRangeAddress(2, 3, iniziofaseb.get(), iniziofaseb.get());
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C1, cs);
                                    sh_pr.addMergedRegion(region_C1);
                                    CellRangeAddress region_C2 = new CellRangeAddress(4, 6, iniziofaseb.get(), iniziofaseb.get());
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C2, cs);
                                    sh_pr.addMergedRegion(region_C2);

                                    setCell(getCell(row_intest, iniziofaseb.get()), intestazione_1, "TOTALE ORE", false, false);
                                    setCell(getCell(row_intest3, iniziofaseb.get()), cs, "FASE A\n+\nFASE B", false, false);

                                    CellRangeAddress region_C3 = new CellRangeAddress(2, 3, iniziofaseb.get() + 1, iniziofaseb.get() + 3);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C3, cs);
                                    sh_pr.addMergedRegion(region_C3);
                                    CellRangeAddress region_C4 = new CellRangeAddress(4, 6, iniziofaseb.get() + 1, iniziofaseb.get() + 1);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C4, cs);
                                    sh_pr.addMergedRegion(region_C4);
                                    setCell(getCell(row_intest, iniziofaseb.get() + 1), intestazione_4, "TOTALE RIMBORSO", false, false);
                                    setCell(getCell(row_intest3, iniziofaseb.get() + 1), cs, "TOTALE\nFASE A + FASE B", false, false);

                                    CellRangeAddress region_C5 = new CellRangeAddress(4, 6, iniziofaseb.get() + 2, iniziofaseb.get() + 2);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C5, cs);
                                    sh_pr.addMergedRegion(region_C5);
                                    setCell(getCell(row_intest3, iniziofaseb.get() + 2), cs, "TOTALE\n70%", false, false);
                                    CellRangeAddress region_C6 = new CellRangeAddress(4, 6, iniziofaseb.get() + 3, iniziofaseb.get() + 3);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C6, cs);
                                    sh_pr.addMergedRegion(region_C6);
                                    setCell(getCell(row_intest3, iniziofaseb.get() + 3), cs, "TOTALE\n30%", false, false);

                                    //RIGA TOTALI
                                    XSSFRow totaliALL = getRow(sh_pr, index_allievo.get());
                                    for (int i = 1; i < 24; i++) {
                                        setCell(getCell(totaliALL, i), cstotal, "", false, false);
                                    }
                                    setCell(getCell(totaliALL, 24), cstotal_double, String.valueOf(total_allievo_1.get()), false, true);
                                    setCell(getCell(totaliALL, 25), cstotal_double, String.valueOf(tot_docenti.get()), false, true);
                                    setCell(getCell(totaliALL, 26), cstotal_double, String.valueOf(tot_docenti_FA.get()), false, true);
                                    setCell(getCell(totaliALL, 27), cstotal_double, String.valueOf(tot_docenti_FB.get()), false, true);
                                    setCell(getCell(totaliALL, 28), cstotal_double, String.valueOf(totale_fa_allievi.get()), false, true);

                                    for (int ix = 0; ix < elencogruppi_B.size(); ix++) {
                                        String numerogruppo = elencogruppi_B.get(ix);
                                        if (indicifaseB.get(numerogruppo) != null) {
                                            int indicedainiziare = indicifaseB.get(numerogruppo);

                                            setCell(getCell(totaliALL, indicedainiziare), cstotal, "", false, false);
                                            setCell(getCell(totaliALL, indicedainiziare + 1), cstotal, "", false, false);
                                            setCell(getCell(totaliALL, indicedainiziare + 2), cstotal, "", false, false);
                                            setCell(getCell(totaliALL, indicedainiziare + 3), cstotal, "", false, false);
                                            setCell(getCell(totaliALL, indicedainiziare + 4), cstotal, "", false, false);
                                            setCell(getCell(totaliALL, indicedainiziare + 5), cstotal_double, String.valueOf(totali_gruppiB.get(numerogruppo)), false, true);
                                        }
                                    }
                                    setCell(getCell(totaliALL, iniziofaseb.get()), cstotal, "", false, false);

                                    setCell(getCell(totaliALL, iniziofaseb.get() + 1), cstotal_double, String.valueOf(totale_pr.get()), false, true);
                                    setCell(getCell(totaliALL, iniziofaseb.get() + 2), cstotal_double, String.valueOf(totale_pr_70.get()), false, true);
                                    setCell(getCell(totaliALL, iniziofaseb.get() + 3), cstotal_double, String.valueOf(totale_pr_30.get()), false, true);

                                    iniziofaseb.addAndGet(1);

                                    index_allievo.addAndGet(2);
                                    XSSFRow numpart = getRow(sh_pr, index_allievo.get());
                                    setCell(getCell(numpart, 7), intestazione_5, "TOTALE PARTECIPANTI", false, false);
                                    setCell(getCell(numpart, 8), cstotal_int, String.valueOf(numpartecipanti), true, false);

                                    XSSFRow totalecorso = getRow(sh_pr, index_allievo.get() + 2);
                                    setCell(getCell(totalecorso, 7), intestazione_5, "TOTALE IMPORTO CORSO", false, false);
                                    setCell(getCell(totalecorso, 8), cstotal_double, String.valueOf(totale_pr.get()), false, true);

                                    //
                                    total_rend.addAndGet(totale_pr.get());

//                                index_allievo.addAndGet(4);
                                    for (int ix = 1; ix < iniziofaseb.get() + 4; ix++) {
                                        sh_pr.autoSizeColumn(ix);
                                    }

                                }
                            }

                        }

                        XSSFRow row_total = getRow(sh1, index_row.get());

                        XSSFCellStyle row_totalCellStyle = wb.createCellStyle();
                        row_totalCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                        row_totalCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        row_totalCellStyle.setFont(font_total);
                        setCell(getCell(row_total, 0), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 1), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 2), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 3), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 4), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 5), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 6), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 7), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 8), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 9), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 10), row_totalCellStyle, String.valueOf(oretotali.get()), false, true);
                        index_row.addAndGet(1);

                        String contentlast = "LUOGO                                                    DATA                                         firma del legale rappresentante  o suo delegato                                                             timbro";
                        XSSFRow row_LAST = getRow(sh1, index_row.get());
                        setCell(getCell(row_LAST, 0), contentlast.toUpperCase());
                        sh1.addMergedRegion(new CellRangeAddress(index_row.get(), index_row.get(), 0, 10));

                        for (int i = 0; i < 13; i++) {
                            sh1.autoSizeColumn(i);
                        }

                        output_xlsx = new File(pathdest + "/Prospetto_Riepilogo_" + new DateTime().toString("yyyyMMdd") + ".xlsx");

                        try (FileOutputStream outputStream = new FileOutputStream(output_xlsx)) {
                            wb.write(outputStream);
                        }

                        sd01_W.close();
                        sd03_W.close();
                        sd07_W.close();

                        double new_total_rend = coeff_ddr_dd * total_rend.get();

                        String def = nomerend + separator + codice_yisu_ded + separator + nomerend_cod + separator
                                + new DateTime().toString("dd/MM/yyyy") + separator + start_rend.toString("dd/MM/yyyy")
                                + separator + end_rend.toString("dd/MM/yyyy") + separator
                                + getDoubleforTXT(new_total_rend) + separator
                                + getDoubleforTXT(new_total_rend) + separator;

                        ddr_W.write(def);
                        ddr_W.close();

                    }
                }
            }

            output.add(output_xlsx);
            output.add(ddr);
            output.add(sd01);
            output.add(sd03);
            output.add(sd07);

            File zip = new File(filezip);

            if (zipListFiles(output, zip)) {
                return zip;
            }

        } catch (Exception ex1) {
            ex1.printStackTrace();
        }

        return null;
    }

    public static File prospetto_riepilogo_neet(int idestrazione, List<Integer> list_idpr, Db_Bando db1) {
        List<File> output = new ArrayList<>();

        File output_xlsx = null;

        String nomerend_cod = "R" + idestrazione;
        String nomerend = nomerend_cod + "_" + new DateTime().toString("ddMMyyyy");

        try {

            String pathdest = db1.getPath("output_excel_archive");
            String pathtemp = db1.getPath("pathTemp");

            String filezip = pathdest + "/" + nomerend + ".zip";
            String fileing = pathdest + "/TEMPLATE PROSPETTO RIEPILOGO.xlsx";

            
            File sd01 = new File(pathtemp + "/SD01.txt");
            File sd03 = new File(pathtemp + "/SD03.txt");
            File ddr = new File(pathtemp + "/DDR.txt");
            
            FileWriter fw01 = new FileWriter(sd01);
            BufferedWriter sd01_W = new BufferedWriter(fw01);

            FileWriter fw03 = new FileWriter(sd03);
            BufferedWriter sd03_W = new BufferedWriter(fw03);
            
            FileWriter fwddr = new FileWriter(ddr);
            BufferedWriter ddr_W = new BufferedWriter(fwddr);

            AtomicDouble total_rend = new AtomicDouble(0.0);
            DateTime start_rend = null;
            DateTime end_rend = null;

            try (Connection conn = db1.getConnection()) {
                if (conn != null) {

                    try (XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(new File(fileing)))) {
                        XSSFSheet sh1 = wb.getSheet("Prospetto di riepilogo DdR L66");
                        wb.setSheetName(sh1.getWorkbook().getSheetIndex(sh1.getSheetName()), "Prospetto di riepilogo DdR " + nomerend_cod);

                        XSSFFont font_total = wb.createFont();
                        font_total.setFontHeightInPoints((short) 12);
                        font_total.setBold(true);

                        XSSFFont font_white = wb.createFont();
                        font_white.setFontHeightInPoints((short) 14);
                        font_white.setBold(true);
                        font_white.setColor(white);
                        XSSFFont font_int = wb.createFont();
                        font_int.setFontHeightInPoints((short) 14);
                        font_int.setBold(true);

                        XSSFCellStyle intestazione_1 = wb.createCellStyle();
                        intestazione_1.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_1.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_1.setBorderBottom(BorderStyle.THIN);
                        intestazione_1.setBorderTop(BorderStyle.THIN);
                        intestazione_1.setBorderRight(BorderStyle.THIN);
                        intestazione_1.setBorderLeft(BorderStyle.THIN);
                        intestazione_1.setFillForegroundColor(myColor1);
                        intestazione_1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_1.setFont(font_white);

                        XSSFCellStyle intestazione_2 = wb.createCellStyle();
                        intestazione_2.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_2.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_2.setBorderBottom(BorderStyle.THIN);
                        intestazione_2.setBorderTop(BorderStyle.THIN);
                        intestazione_2.setBorderRight(BorderStyle.THIN);
                        intestazione_2.setBorderLeft(BorderStyle.THIN);
                        intestazione_2.setFillForegroundColor(myColor2);
                        intestazione_2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_2.setFont(font_white);

                        XSSFCellStyle intestazione_3 = wb.createCellStyle();
                        intestazione_3.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_3.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_3.setBorderBottom(BorderStyle.THIN);
                        intestazione_3.setBorderTop(BorderStyle.THIN);
                        intestazione_3.setBorderRight(BorderStyle.THIN);
                        intestazione_3.setBorderLeft(BorderStyle.THIN);
                        intestazione_3.setFillForegroundColor(myColor3);
                        intestazione_3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_3.setFont(font_int);

                        XSSFCellStyle intestazione_4 = wb.createCellStyle();
                        intestazione_4.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_4.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_4.setBorderBottom(BorderStyle.THIN);
                        intestazione_4.setBorderTop(BorderStyle.THIN);
                        intestazione_4.setBorderRight(BorderStyle.THIN);
                        intestazione_4.setBorderLeft(BorderStyle.THIN);
                        intestazione_4.setFillForegroundColor(myColor4);
                        intestazione_4.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_4.setFont(font_white);
                        XSSFCellStyle intestazione_5 = wb.createCellStyle();
                        intestazione_5.setVerticalAlignment(VerticalAlignment.CENTER);
                        intestazione_5.setAlignment(HorizontalAlignment.CENTER);
                        intestazione_5.setBorderBottom(BorderStyle.THIN);
                        intestazione_5.setBorderTop(BorderStyle.THIN);
                        intestazione_5.setBorderRight(BorderStyle.THIN);
                        intestazione_5.setBorderLeft(BorderStyle.THIN);
                        intestazione_5.setFillForegroundColor(myColor5);
                        intestazione_5.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        intestazione_5.setFont(font_white);

                        XSSFDataFormat xssfDataFormat = wb.createDataFormat();
                        XSSFCellStyle cellStyle_int = wb.createCellStyle();
                        cellStyle_int.setBorderBottom(BorderStyle.THIN);
                        cellStyle_int.setBorderTop(BorderStyle.THIN);
                        cellStyle_int.setBorderRight(BorderStyle.THIN);
                        cellStyle_int.setBorderLeft(BorderStyle.THIN);
                        cellStyle_int.setVerticalAlignment(VerticalAlignment.CENTER);
                        cellStyle_int.setDataFormat(xssfDataFormat.getFormat(formatdataCellint));

                        XSSFCellStyle cellStyle_double = wb.createCellStyle();
                        cellStyle_double.setBorderBottom(BorderStyle.THIN);
                        cellStyle_double.setBorderTop(BorderStyle.THIN);
                        cellStyle_double.setBorderRight(BorderStyle.THIN);
                        cellStyle_double.setBorderLeft(BorderStyle.THIN);
                        cellStyle_double.setVerticalAlignment(VerticalAlignment.CENTER);
                        cellStyle_double.setDataFormat(xssfDataFormat.getFormat(formatdataCell));

                        XSSFCellStyle cs = wb.createCellStyle();
                        cs.setVerticalAlignment(VerticalAlignment.CENTER);
                        cs.setAlignment(HorizontalAlignment.CENTER);
                        cs.setBorderBottom(BorderStyle.THIN);
                        cs.setBorderTop(BorderStyle.THIN);
                        cs.setBorderRight(BorderStyle.THIN);
                        cs.setBorderLeft(BorderStyle.THIN);

                        XSSFCellStyle cstotal = wb.createCellStyle();
                        cstotal.setVerticalAlignment(VerticalAlignment.CENTER);
                        cstotal.setAlignment(HorizontalAlignment.CENTER);
                        cstotal.setBorderBottom(BorderStyle.THIN);
                        cstotal.setBorderTop(BorderStyle.THIN);
                        cstotal.setBorderRight(BorderStyle.THIN);
                        cstotal.setBorderLeft(BorderStyle.THIN);
                        cstotal.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                        cstotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cstotal.setFont(font_total);

                        XSSFCellStyle cstotal_double = wb.createCellStyle();

                        cstotal_double.setVerticalAlignment(VerticalAlignment.CENTER);
                        cstotal_double.setAlignment(HorizontalAlignment.CENTER);
                        cstotal_double.setBorderBottom(BorderStyle.THIN);
                        cstotal_double.setBorderTop(BorderStyle.THIN);
                        cstotal_double.setBorderRight(BorderStyle.THIN);
                        cstotal_double.setBorderLeft(BorderStyle.THIN);
                        cstotal_double.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                        cstotal_double.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cstotal_double.setDataFormat(xssfDataFormat.getFormat(formatdataCell));
                        cstotal_double.setFont(font_total);

                        XSSFCellStyle cstotal_int = wb.createCellStyle();

                        cstotal_int.setVerticalAlignment(VerticalAlignment.CENTER);
                        cstotal_int.setAlignment(HorizontalAlignment.CENTER);
                        cstotal_int.setBorderBottom(BorderStyle.THIN);
                        cstotal_int.setBorderTop(BorderStyle.THIN);
                        cstotal_int.setBorderRight(BorderStyle.THIN);
                        cstotal_int.setBorderLeft(BorderStyle.THIN);
                        cstotal_int.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                        cstotal_int.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cstotal_int.setDataFormat(xssfDataFormat.getFormat(formatdataCellint));
                        cstotal_int.setFont(font_total);

                        AtomicInteger indicitxt = new AtomicInteger(1);

                        AtomicInteger index_row = new AtomicInteger(9);
                        AtomicDouble oretotali = new AtomicDouble(0.0);
                        AtomicInteger indice = new AtomicInteger(1);

                        for (int ss = 0; ss < list_idpr.size(); ss++) {

                            int idpr = list_idpr.get(ss);

                            String sql1 = "SELECT p.idprogetti_formativi,p.cip,s.ragionesociale,c.regione,p.start,p.end "
                                    + "FROM progetti_formativi p, soggetti_attuatori s,comuni c WHERE p.stato='CO' "
                                    + "AND p.idsoggetti_attuatori=s.idsoggetti_attuatori AND c.idcomune=s.comune AND p.idprogetti_formativi = " + idpr;

                            try (Statement st1 = conn.createStatement(); ResultSet rs1 = st1.executeQuery(sql1)) {
                                if (rs1.next()) {

                                    String cip = rs1.getString(2).toUpperCase();
                                    String ragionesociale = rs1.getString(3).toUpperCase();
                                    String regione = rs1.getString(4).toUpperCase();
                                    String start = sdfITA.format(rs1.getDate(5));
                                    String end = sdfITA.format(rs1.getDate(6));

                                    if (start_rend == null || start_rend.isAfter(new DateTime(rs1.getDate(5).getTime()))) {
                                        start_rend = new DateTime(rs1.getDate(5).getTime());
                                    }
                                    if (end_rend == null || end_rend.isBefore(new DateTime(rs1.getDate(6).getTime()))) {
                                        end_rend = new DateTime(rs1.getDate(6).getTime());
                                    }

                                    Map<Long, Long> oreRendicontabili_faseA = OreRendicontabiliAlunni_faseA(conn, idpr);
//                                Map<Long, Long> oreRendicontabili_faseB = OreRendicontabiliAlunni_faseB(conn, idpr);
                                    Map<Long, Long> oreRendicontabili_docenti = OreRendicontabiliDocentiFASEA(conn, idpr);

                                    //CALCOLO PARTECIPANTI
                                    AtomicInteger numpartecipanti = new AtomicInteger(0);
                                    oreRendicontabili_faseA.forEach((idal, value) -> {
                                        if (value >= hh36) {
                                            numpartecipanti.addAndGet(1);
                                        }
                                    });

                                    StringBuilder list_outputfaseA = new StringBuilder("");

                                    String sql1A = "SELECT m.tab_completezza_output_neet FROM checklist_finale m, progetti_formativi p "
                                            + "WHERE p.id_checklist_finale=m.id AND p.idprogetti_formativi =" + idpr;
                                    try (Statement st2 = conn.createStatement(); ResultSet rs1A = st2.executeQuery(sql1A)) {
                                        if (rs1A.next()) {
                                            list_outputfaseA.append(rs1A.getString(1));
//                                        list_outputfaseA = Arrays.asList(new ObjectMapper().readValue(rs1A.getString(1), OutputId[].class));
                                        }
                                    }

                                    //imposta foglio progetto, crea intestazione 
                                    XSSFSheet sh_pr = wb.createSheet(cip);

                                    List<Items> calendario = calendario(idpr, conn);

                                    AtomicInteger indici_docenti = new AtomicInteger(15 + numpartecipanti.get());

                                    //docenti
                                    XSSFRow row_docenti = getRow(sh_pr, indici_docenti.get());
                                    CellRangeAddress region_20 = new CellRangeAddress(row_docenti.getRowNum(), row_docenti.getRowNum(), 5, 26);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_20, cs);
                                    sh_pr.addMergedRegion(region_20);

                                    setCell(getCell(row_docenti, 5), intestazione_1, "DOCENTI CORSO - FASE A", false, false);

                                    indici_docenti.addAndGet(1);
                                    XSSFRow row_docenti2 = getRow(sh_pr, indici_docenti.get());

                                    setCell(getCell(row_docenti2, 5), cs, "N.", false, false);
                                    CellRangeAddress region_21 = new CellRangeAddress(row_docenti2.getRowNum(), row_docenti2.getRowNum(), 6, 7);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_21, cs);
                                    sh_pr.addMergedRegion(region_21);
                                    setCell(getCell(row_docenti2, 6), cs, "COGNOME", false, false);

                                    setCell(getCell(row_docenti2, 8), cs, "NOME", false, false);
                                    setCell(getCell(row_docenti2, 9), cs, "FASCIA", false, false);
                                    setCell(getCell(row_docenti2, 10), cs, "€/h", false, false);

                                    for (int x = 0; x < calendario.size(); x++) {
                                        Items cal1 = calendario.get(x);
                                        if (cal1.getFase().equals("A")) {
                                            setCell(getCell(row_docenti2, 11 + x), cs, sdfITA.format(sdfSQL.parse(cal1.getData()).getTime()), false, false);
                                        }
                                    }

                                    setCell(getCell(row_docenti2, 23), cs, "TOTALE ORE\n(MAX 60h)", false, false);
                                    setCell(getCell(row_docenti2, 24), cs, "TOTALE", false, false);
                                    setCell(getCell(row_docenti2, 25), cs, "A", false, false);
                                    setCell(getCell(row_docenti2, 26), cs, "B", false, false);

                                    indici_docenti.addAndGet(1);
                                    AtomicInteger numdocenti = new AtomicInteger(0);
                                    AtomicDouble tot_docenti = new AtomicDouble(0.0);

                                    AtomicDouble tot_ore_docenti_FA = new AtomicDouble(0.0);
                                    AtomicDouble tot_ore_docenti_FB = new AtomicDouble(0.0);
                                    AtomicDouble tot_docenti_FA = new AtomicDouble(0.0);
                                    AtomicDouble tot_docenti_FB = new AtomicDouble(0.0);

                                    oreRendicontabili_docenti.forEach((iddoc, value) -> {

                                        try {
                                            String sql3 = "SELECT d.cognome,d.nome,d.codicefiscale,d.fascia "
                                                    + "FROM docenti d,progetti_docenti p  WHERE d.iddocenti = p.iddocenti "
                                                    + "AND d.iddocenti=" + iddoc + " AND p.idprogetti_formativi=" + idpr;

                                            try (Statement st3 = conn.createStatement(); ResultSet rs3 = st3.executeQuery(sql3)) {
                                                if (rs3.next()) {
                                                    numdocenti.addAndGet(1);
                                                    String cognome = rs3.getString(1).toUpperCase();
                                                    String nome = rs3.getString(2).toUpperCase();
                                                    String fascia = rs3.getString(4).toUpperCase().equals("FA") ? "A" : "B";

                                                    XSSFRow row_d = getRow(sh_pr, indici_docenti.get());
                                                    setCell(getCell(row_d, 5), cs, String.valueOf(numdocenti.get()), true, false);

                                                    CellRangeAddress region_22 = new CellRangeAddress(row_d.getRowNum(), row_d.getRowNum(), 6, 7);
                                                    cleanBeforeMergeOnValidCells(sh_pr, region_22, cs);
                                                    sh_pr.addMergedRegion(region_22);

                                                    setCell(getCell(row_d, 6), cs, cognome, false, false);

                                                    setCell(getCell(row_d, 8), cs, nome, false, false);
                                                    setCell(getCell(row_d, 9), cs, fascia, false, false);
                                                    double coeff_docA = fascia.equals("A") ? coeff_docfasciaA : coeff_docfasciaB;
                                                    setCell(getCell(row_d, 10), cellStyle_double, String.valueOf(coeff_docA), false, true);

                                                    String sql2A = "SELECT r.data,r.totaleorerendicontabili FROM registro_completo r "
                                                            + "WHERE r.idutente=" + iddoc + " AND r.ruolo LIKE 'DOCENTE' AND r.idprogetti_formativi=" + idpr;
                                                    HashMap<String, Double> presenza_doc = new HashMap<>();

                                                    try (Statement st2A = conn.createStatement(); ResultSet rs2A = st2A.executeQuery(sql2A)) {
                                                        while (rs2A.next()) {
                                                            presenza_doc.put(rs2A.getString(1), roundFloatAndFormat(rs2A.getLong(2)));
                                                        }
                                                    }

                                                    AtomicDouble tot_ore_fase_A = new AtomicDouble(0.0);

                                                    for (int x = 0; x < calendario.size(); x++) {
                                                        Items cal1 = calendario.get(x);
                                                        if (cal1.getFase().equals("A")) {
                                                            if (presenza_doc.get(cal1.getData()) != null) {
                                                                setCell(getCell(row_d, 11 + x),
                                                                        cellStyle_double,
                                                                        String.valueOf(presenza_doc.get(cal1.getData())),
                                                                        false, true);
                                                                tot_ore_fase_A.addAndGet(presenza_doc.get(cal1.getData()));
                                                            } else {
                                                                setCell(getCell(row_d, 11 + x),
                                                                        cellStyle_double,
                                                                        "0.00",
                                                                        false, true);
                                                            }
                                                        }
                                                    }

                                                    setCell(getCell(row_d, 23), cellStyle_double,
                                                            String.valueOf(tot_ore_fase_A.get()),
                                                            false, true);

                                                    String totali_fascia_A = "0.00";
                                                    String totali_fascia_B = "0.00";

                                                    double tot_d1 = tot_ore_fase_A.get() * coeff_docA;

                                                    if (fascia.equals("A")) {
                                                        totali_fascia_A = String.valueOf(tot_d1);
                                                        tot_docenti_FA.addAndGet(tot_d1);
                                                        tot_ore_docenti_FA.addAndGet(tot_ore_fase_A.get());
                                                    } else {
                                                        totali_fascia_B = String.valueOf(tot_d1);
                                                        tot_docenti_FB.addAndGet(tot_d1);
                                                        tot_ore_docenti_FB.addAndGet(tot_ore_fase_A.get());
                                                    }
                                                    tot_docenti.addAndGet(tot_d1);

                                                    setCell(getCell(row_d, 24), cellStyle_double, String.valueOf(tot_d1), false, true);
                                                    setCell(getCell(row_d, 25), cellStyle_double, totali_fascia_A, false, true);
                                                    setCell(getCell(row_d, 26), cellStyle_double, totali_fascia_B, false, true);

                                                    indici_docenti.addAndGet(1);
                                                }
                                            }
                                        } catch (Exception ex3) {
                                            ex3.printStackTrace();
                                        }

                                    });

                                    //TOTALI DOCENTE 
                                    XSSFRow row_total_d = getRow(sh_pr, indici_docenti.get());

                                    setCell(getCell(row_total_d, 24), cstotal_double, String.valueOf(tot_docenti.get()), false, true);
                                    setCell(getCell(row_total_d, 25), cstotal_double, String.valueOf(tot_docenti_FA.get()), false, true);
                                    setCell(getCell(row_total_d, 26), cstotal_double, String.valueOf(tot_docenti_FB.get()), false, true);

                                    //ALLIEVI
                                    XSSFRow row_intest = getRow(sh_pr, 2);
                                    XSSFRow row_intest2 = getRow(sh_pr, 3);
                                    XSSFRow row_intest3 = getRow(sh_pr, 4);
                                    XSSFRow row_intest4 = getRow(sh_pr, 5);
                                    XSSFRow row_intest5 = getRow(sh_pr, 6);

                                    CellRangeAddress region_1 = new CellRangeAddress(2, 3, 1, 6);
                                    CellRangeAddress region_2 = new CellRangeAddress(2, 3, 7, 10);
                                    CellRangeAddress region_3 = new CellRangeAddress(2, 2, 11, 28);
                                    CellRangeAddress region_4 = new CellRangeAddress(4, 6, 1, 1);
                                    CellRangeAddress region_5 = new CellRangeAddress(4, 6, 2, 2);
                                    CellRangeAddress region_6 = new CellRangeAddress(4, 6, 3, 3);
                                    CellRangeAddress region_7 = new CellRangeAddress(4, 6, 4, 4);
                                    CellRangeAddress region_8 = new CellRangeAddress(4, 6, 5, 5);
                                    CellRangeAddress region_9 = new CellRangeAddress(4, 6, 6, 6);
                                    CellRangeAddress region_10 = new CellRangeAddress(4, 6, 7, 7);
                                    CellRangeAddress region_11 = new CellRangeAddress(4, 6, 8, 8);
                                    CellRangeAddress region_12 = new CellRangeAddress(4, 6, 9, 9);
                                    CellRangeAddress region_13 = new CellRangeAddress(4, 6, 10, 10);

                                    cleanBeforeMergeOnValidCells(sh_pr, region_1, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_2, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_3, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_4, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_5, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_6, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_7, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_8, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_9, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_10, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_11, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_12, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_13, cs);

                                    sh_pr.addMergedRegion(region_1);
                                    sh_pr.addMergedRegion(region_2);
                                    sh_pr.addMergedRegion(region_3);
                                    sh_pr.addMergedRegion(region_4);
                                    sh_pr.addMergedRegion(region_5);
                                    sh_pr.addMergedRegion(region_6);
                                    sh_pr.addMergedRegion(region_7);
                                    sh_pr.addMergedRegion(region_8);
                                    sh_pr.addMergedRegion(region_9);
                                    sh_pr.addMergedRegion(region_10);
                                    sh_pr.addMergedRegion(region_11);
                                    sh_pr.addMergedRegion(region_12);
                                    sh_pr.addMergedRegion(region_13);

                                    setCell(getCell(row_intest, 1), intestazione_1, "ANAGRAFICA \nPERCORSO", false, false);
                                    setCell(getCell(row_intest, 7), intestazione_2, "ANAGRAFICA \nPARTECIPANTI", false, false);
                                    setCell(getCell(row_intest, 11), intestazione_3, "FASE A\nDATE", false, false);
                                    setCell(getCell(row_intest3, 1), cs, "N.", false, false);
                                    setCell(getCell(row_intest3, 2), cs, "ID", false, false);
                                    setCell(getCell(row_intest3, 3), cs, "CIP", false, false);
                                    setCell(getCell(row_intest3, 4), cs, "SA", false, false);
                                    setCell(getCell(row_intest3, 5), cs, "DATA\nINIZIO CORSO", false, false);
                                    setCell(getCell(row_intest3, 6), cs, "DATA\nFINE CORSO", false, false);
                                    setCell(getCell(row_intest3, 7), cs, "COGNOME", false, false);
                                    setCell(getCell(row_intest3, 8), cs, "NOME", false, false);
                                    setCell(getCell(row_intest3, 9), cs, "CODICE FISCALE", false, false);
                                    setCell(getCell(row_intest3, 10), cs, "OUTPUT (SI/NO)", false, false);

                                    String datainizioA = "";
                                    String datafineA = "";

                                    boolean okA = true;
                                    for (int x = 0; x < calendario.size(); x++) {
                                        Items cal1 = calendario.get(x);
                                        if (cal1.getFase().equals("A")) {
                                            if (datainizioA.equals("") || okA) {
                                                datainizioA = sdfITA.format(sdfSQL.parse(cal1.getData()).getTime());
                                                okA = false;
                                            }
                                            datafineA = sdfITA.format(sdfSQL.parse(cal1.getData()).getTime());

                                            setCell(getCell(row_intest3, 11 + x), cs, sdfITA.format(sdfSQL.parse(cal1.getData()).getTime()), false, false);
                                            setCell(getCell(row_intest4, 11 + x), cs, sdfHHmm.format(sdfHHmmss.parse(cal1.getOrainizio()).getTime()), false, false);
                                            setCell(getCell(row_intest5, 11 + x), cs, sdfHHmm.format(sdfHHmmss.parse(cal1.getOrafine()).getTime()), false, false);

                                        }

                                    }

                                    CellRangeAddress region_14 = new CellRangeAddress(3, 3, 11, 28);
                                    CellRangeAddress region_15 = new CellRangeAddress(4, 6, 23, 23);
                                    CellRangeAddress region_16 = new CellRangeAddress(4, 6, 24, 24);
                                    CellRangeAddress region_17 = new CellRangeAddress(4, 6, 25, 25);
                                    CellRangeAddress region_18 = new CellRangeAddress(4, 6, 26, 26);
                                    CellRangeAddress region_19 = new CellRangeAddress(4, 6, 27, 27);
                                    CellRangeAddress region_19A = new CellRangeAddress(4, 6, 28, 28);

                                    cleanBeforeMergeOnValidCells(sh_pr, region_14, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_15, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_16, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_17, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_18, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_19, cs);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_19A, cs);

                                    sh_pr.addMergedRegion(region_14);
                                    sh_pr.addMergedRegion(region_15);
                                    sh_pr.addMergedRegion(region_16);
                                    sh_pr.addMergedRegion(region_17);
                                    sh_pr.addMergedRegion(region_18);
                                    sh_pr.addMergedRegion(region_19);
                                    sh_pr.addMergedRegion(region_19A);

                                    setCell(getCell(row_intest2, 11), intestazione_3, "DAL " + datainizioA + " AL " + datafineA, false, false);
                                    setCell(getCell(row_intest3, 23), cs, "TOTALE ORE\n(MAX 60h)", false, false);
                                    setCell(getCell(row_intest3, 24), cs, "TOTALE ORE\n * \n0,80€", false, false);
                                    setCell(getCell(row_intest3, 25), cs, "Quota parte totale docente\n(totale/n. partecipanti)", false, false);
                                    setCell(getCell(row_intest3, 26), cs, "Quota parte totale docente\n(totale/n. partecipanti)\nFASCIA A", false, false);
                                    setCell(getCell(row_intest3, 27), cs, "Quota parte totale docente\n(totale/n. partecipanti)\nFASCIA B", false, false);
                                    setCell(getCell(row_intest3, 28), cs, "TOTALE FASE A", false, false);

                                    List<String> elencogruppi_B = calendario.stream().filter(it1 -> it1.getFase().equals("B"))
                                            .map(Items::getGruppo).distinct().sorted().collect(Collectors.toList());

//                                System.out.println("it.refill.testingarea.Excel.main() "+elencogruppi_B.toString());
                                    AtomicInteger iniziofaseb = new AtomicInteger(29);
                                    int iniziofaseb_i;

                                    HashMap<String, Integer> indicifaseB = new HashMap<>();

                                    for (int ix = 0; ix < elencogruppi_B.size(); ix++) {
                                        String numerogruppo = elencogruppi_B.get(ix);
                                        String datainizioB = "";
                                        String datafineB = "";
                                        boolean ok_B = true;

                                        iniziofaseb_i = iniziofaseb.get();

                                        List<Items> calendario2 = calendario.stream().filter(c1 -> c1.getGruppo().equals(numerogruppo)).collect(Collectors.toList());

                                        for (int x = 0; x < calendario2.size(); x++) {
                                            Items cal1 = calendario2.get(x);
                                            if (cal1.getFase().equals("B") && cal1.getGruppo().equals(numerogruppo)) {
                                                if (datainizioB.equals("") || ok_B) {
                                                    indicifaseB.put(numerogruppo, iniziofaseb_i);
                                                    datainizioB = sdfITA.format(sdfSQL.parse(cal1.getData()).getTime());
                                                    ok_B = false;
                                                }
                                                datafineB = sdfITA.format(sdfSQL.parse(cal1.getData()).getTime());

                                                setCell(getCell(row_intest3, iniziofaseb.get()), cs, sdfITA.format(sdfSQL.parse(cal1.getData()).getTime()), false, false);
                                                setCell(getCell(row_intest4, iniziofaseb.get()), cs, sdfHHmm.format(sdfHHmmss.parse(cal1.getOrainizio()).getTime()), false, false);
                                                setCell(getCell(row_intest5, iniziofaseb.get()), cs, sdfHHmm.format(sdfHHmmss.parse(cal1.getOrafine()).getTime()), false, false);

                                                //popola caselle con 0.00
                                                for (int xx = 1; xx <= numpartecipanti.get(); xx++) {
                                                    setCell(getCell(getRow(sh_pr, 6 + xx), iniziofaseb.get()),
                                                            cellStyle_double,
                                                            "0.00",
                                                            false, true);
                                                }

                                                iniziofaseb.addAndGet(1);

                                            }
                                        }
                                        for (int xx = 1; xx <= numpartecipanti.get(); xx++) {
                                            setCell(getCell(getRow(sh_pr, 6 + xx), iniziofaseb.get()),
                                                    cellStyle_double,
                                                    "0.00",
                                                    false, true);
                                        }
                                        iniziofaseb.addAndGet(1);
                                        for (int xx = 1; xx <= numpartecipanti.get(); xx++) {
                                            setCell(getCell(getRow(sh_pr, 6 + xx), iniziofaseb.get()),
                                                    cellStyle_double,
                                                    "0.00",
                                                    false, true);
                                        }
                                        iniziofaseb.addAndGet(1);

                                        CellRangeAddress region_B1 = new CellRangeAddress(2, 2, iniziofaseb_i, iniziofaseb.get() - 1);
                                        cleanBeforeMergeOnValidCells(sh_pr, region_B1, cs);
                                        sh_pr.addMergedRegion(region_B1);
                                        CellRangeAddress region_B2 = new CellRangeAddress(3, 3, iniziofaseb_i, iniziofaseb.get() - 1);
                                        cleanBeforeMergeOnValidCells(sh_pr, region_B2, cs);
                                        sh_pr.addMergedRegion(region_B2);
                                        setCell(getCell(row_intest, iniziofaseb_i), intestazione_2, "FASE B GRUPPO " + numerogruppo + "\nDATE", false, false);
                                        setCell(getCell(row_intest2, iniziofaseb_i), intestazione_2, "DAL " + datainizioB + " AL " + datafineB, false, false);

                                        CellRangeAddress region_B3 = new CellRangeAddress(4, 6, iniziofaseb.get() - 2, iniziofaseb.get() - 2);
                                        cleanBeforeMergeOnValidCells(sh_pr, region_B3, cs);
                                        sh_pr.addMergedRegion(region_B3);
                                        setCell(getCell(row_intest3, iniziofaseb.get() - 2), cs, "TOTALE ORE\n(MAX 20h)", false, false);
                                        CellRangeAddress region_B4 = new CellRangeAddress(4, 6, iniziofaseb.get() - 1, iniziofaseb.get() - 1);
                                        cleanBeforeMergeOnValidCells(sh_pr, region_B4, cs);
                                        sh_pr.addMergedRegion(region_B4);
                                        setCell(getCell(row_intest3, iniziofaseb.get() - 1), cs, "TOTALE ORE FASE B\n * \n40€", false, false);
                                    }

//                                System.out.println(cip + " : " + indicifaseB.toString());
                                    AtomicInteger index_allievo = new AtomicInteger(7);
                                    AtomicDouble total_allievo_1 = new AtomicDouble(0.0);
                                    AtomicDouble totale_fa_allievi = new AtomicDouble(0.0);

                                    AtomicDouble totale_pr = new AtomicDouble(0.0);
                                    AtomicDouble totale_pr_70 = new AtomicDouble(0.0);
                                    AtomicDouble totale_pr_30 = new AtomicDouble(0.0);

                                    double quota_parte_totale = tot_docenti.get() / Double.valueOf(String.valueOf(numpartecipanti.get()));
                                    double quota_parte_totale_FA = tot_docenti_FA.get() / Double.valueOf(String.valueOf(numpartecipanti.get()));
                                    double quota_parte_totale_FB = tot_docenti_FB.get() / Double.valueOf(String.valueOf(numpartecipanti.get()));

                                    HashMap<String, Double> totali_gruppiB = new HashMap<>();

                                    oreRendicontabili_faseA.forEach((idal, value) -> {

                                        if (value >= hh36) {
                                            try {

                                                //creazione righe allievo
                                                String sql2 = "SELECT a.cognome,a.nome,a.codicefiscale,a.gruppo_faseB FROM allievi a WHERE a.idallievi=" + idal + " ";
                                                try (Statement st2 = conn.createStatement(); ResultSet rs2 = st2.executeQuery(sql2)) {
                                                    if (rs2.next()) {

                                                        String cognome = rs2.getString(1).toUpperCase();
                                                        String nome = rs2.getString(2).toUpperCase();
                                                        String codicefiscale = rs2.getString(3).toUpperCase();
                                                        String gruppo_faseB = rs2.getString(4);

//                                                    System.out.println(cognome + " it.refill.testingarea.Excel.main() " + gruppo_faseB);
                                                        int indicepartenzafaseBallievo = 0;

                                                        XSSFRow row_allievo = getRow(sh_pr, index_allievo.get());
                                                        setCell(getCell(row_allievo, 1), cellStyle_int, String.valueOf(index_allievo.get() - 6), true, false);
                                                        setCell(getCell(row_allievo, 2), cellStyle_int, String.valueOf(idpr), true, false);
                                                        setCell(getCell(row_allievo, 3), cs, cip, false, false);
                                                        setCell(getCell(row_allievo, 4), cs, ragionesociale, false, false);
                                                        setCell(getCell(row_allievo, 5), cs, start, false, false);
                                                        setCell(getCell(row_allievo, 6), cs, end, false, false);
                                                        setCell(getCell(row_allievo, 7), cs, cognome, false, false);
                                                        setCell(getCell(row_allievo, 8), cs, nome, false, false);
                                                        setCell(getCell(row_allievo, 9), cs, codicefiscale, false, false);
                                                        OutputId output_a = Arrays.asList(new ObjectMapper().readValue(list_outputfaseA.toString(), OutputId[].class)).stream()
                                                                .filter(a1 -> a1.getId()
                                                                .equals(String.valueOf(idal))).findAny().orElse(null);
                                                        if (output_a == null || output_a.getOutput().equals("0")) {
                                                            setCell(getCell(row_allievo, 10), cs, "NO", false, false);
                                                        } else {
                                                            setCell(getCell(row_allievo, 10), cs, "SI", false, false);
                                                        }

                                                        long la = 0L;
                                                        long lb = 0L;

                                                        String sql2A = "SELECT r.data,r.totaleorerendicontabili,r.fase FROM registro_completo r "
                                                                + "WHERE r.idutente=" + idal + " AND r.ruolo LIKE 'ALLIEVO%' AND r.idprogetti_formativi=" + idpr;
                                                        HashMap<String, Double> presenza = new HashMap<>();

                                                        try (Statement st2A = conn.createStatement(); ResultSet rs2A = st2A.executeQuery(sql2A)) {
                                                            while (rs2A.next()) {
                                                                if (rs2A.getString(3).equals("A")) {
                                                                    la += (rs2A.getLong(2));
                                                                } else {
                                                                    lb += (rs2A.getLong(2));
                                                                }
                                                                presenza.put(rs2A.getString(1), roundFloatAndFormat(rs2A.getLong(2)));
                                                            }
                                                        }

//                                                    AtomicDouble tot_A = new AtomicDouble(0.0);
//                                                    AtomicDouble tot_B = new AtomicDouble(0.0);
                                                        int yb = 0;
                                                        for (int x = 0; x < calendario.size(); x++) {
                                                            Items cal1 = calendario.get(x);
                                                            if (cal1.getFase().equals("A")) {

                                                                if (presenza.get(cal1.getData()) != null) {

                                                                    BigDecimal bigDecimal = new BigDecimal(presenza.get(cal1.getData()));
                                                                    bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_EVEN);

                                                                    setCell(getCell(row_allievo, 11 + x),
                                                                            cellStyle_double,
                                                                            String.valueOf(bigDecimal.toString()),
                                                                            false, true);
//                                                                tot_A.addAndGet(presenza.get(cal1.getData()));
                                                                } else {
                                                                    setCell(getCell(row_allievo, 11 + x),
                                                                            cellStyle_double,
                                                                            "0.00",
                                                                            false, true);
                                                                }

                                                            } else {
                                                                if (cal1.getGruppo().equals(gruppo_faseB)) {

                                                                    indicepartenzafaseBallievo = indicifaseB.get(gruppo_faseB);

                                                                    if (presenza.get(cal1.getData()) != null) {
                                                                        setCell(getCell(row_allievo, indicepartenzafaseBallievo + yb),
                                                                                cellStyle_double,
                                                                                String.valueOf(presenza.get(cal1.getData())),
                                                                                false, true);
//                                                                    tot_B.addAndGet(presenza.get(cal1.getData()));

                                                                    } else {
                                                                        setCell(getCell(row_allievo, indicepartenzafaseBallievo + yb),
                                                                                cellStyle_double,
                                                                                "0.00",
                                                                                false, true);
                                                                    }
                                                                    yb++;
                                                                }
                                                            }

                                                        }

                                                        BigDecimal bigDecimal_ta = new BigDecimal(roundFloatAndFormat(la));
                                                        bigDecimal_ta = bigDecimal_ta.setScale(2, RoundingMode.HALF_EVEN);
                                                        BigDecimal bigDecimal_tb = new BigDecimal(roundFloatAndFormat(lb));
                                                        bigDecimal_tb = bigDecimal_tb.setScale(2, RoundingMode.HALF_EVEN);

                                                        double tot_A = bigDecimal_ta.doubleValue();
                                                        double tot_B = bigDecimal_tb.doubleValue();

                                                        double tot_single = tot_A * coeff_faseA;

                                                        setCell(getCell(row_allievo, 23),
                                                                cellStyle_double,
                                                                bigDecimal_ta.toString(),
                                                                false, true);
                                                        setCell(getCell(row_allievo, 24),
                                                                cellStyle_double,
                                                                String.valueOf(tot_single),
                                                                false, true);

                                                        setCell(getCell(row_allievo, 25),
                                                                cellStyle_double,
                                                                String.valueOf(quota_parte_totale),
                                                                false, true);
                                                        setCell(getCell(row_allievo, 26),
                                                                cellStyle_double,
                                                                String.valueOf(quota_parte_totale_FA),
                                                                false, true);

                                                        setCell(getCell(row_allievo, 27),
                                                                cellStyle_double,
                                                                String.valueOf(quota_parte_totale_FB),
                                                                false, true);

                                                        double total_fa_allievo = tot_single + quota_parte_totale;

                                                        setCell(getCell(row_allievo, 28),
                                                                cellStyle_double,
                                                                String.valueOf(total_fa_allievo),
                                                                false, true);

                                                        total_allievo_1.addAndGet(tot_single);
                                                        totale_fa_allievi.addAndGet(total_fa_allievo);

                                                        double tot_single_row = total_fa_allievo;

                                                        //b
                                                        double tot_single_B = 0.0;
                                                        if (indicepartenzafaseBallievo > 0) {
                                                            tot_single_B = tot_B * coeff_faseB;
                                                            tot_single_row += tot_single_B;
                                                            setCell(getCell(row_allievo, indicepartenzafaseBallievo + 4),
                                                                    cellStyle_double,
                                                                    String.valueOf(tot_B),
                                                                    false, true);
                                                            setCell(getCell(row_allievo, indicepartenzafaseBallievo + 5),
                                                                    cellStyle_double,
                                                                    String.valueOf(tot_single_B),
                                                                    false, true);

                                                            if (totali_gruppiB.get(gruppo_faseB) == null) {
                                                                totali_gruppiB.put(gruppo_faseB, tot_single_B);
                                                            } else {
                                                                totali_gruppiB.put(gruppo_faseB, totali_gruppiB.get(gruppo_faseB) + tot_single_B);
                                                            }

                                                        }

                                                        setCell(getCell(row_allievo, iniziofaseb.get()),
                                                                cellStyle_double,
                                                                String.valueOf(tot_A + tot_B), false, true);

                                                        setCell(getCell(row_allievo, iniziofaseb.get() + 1),
                                                                cellStyle_double,
                                                                String.valueOf(tot_single_row), false, true);

                                                        double tot_single_row_30 = tot_single_row * 30.00 / 100.00;
                                                        double tot_single_row_70 = tot_single_row - tot_single_row_30;

                                                        setCell(getCell(row_allievo, iniziofaseb.get() + 2),
                                                                cellStyle_double,
                                                                String.valueOf(tot_single_row_70),
                                                                false, true);
                                                        setCell(getCell(row_allievo, iniziofaseb.get() + 3),
                                                                cellStyle_double,
                                                                String.valueOf(tot_single_row_30),
                                                                false, true);

                                                        totale_pr.addAndGet(tot_single_row);
                                                        totale_pr_30.addAndGet(tot_single_row_30);
                                                        totale_pr_70.addAndGet(tot_single_row_70);

                                                        index_allievo.addAndGet(1);

                                                        double oreprogetto = tot_A + tot_B;
                                                        oretotali.addAndGet(oreprogetto);
                                                        XSSFRow row = getRow(sh1, index_row.get());
                                                        setCell(getCell(row, 0), cellStyle_int, String.valueOf(indice.get()), true, false);
                                                        setCell(getCell(row, 1), cellStyle_int, String.valueOf(idpr), true, false);
                                                        setCell(getCell(row, 2), cs, cip, false, false);
                                                        setCell(getCell(row, 3), cs, ragionesociale, false, false);
                                                        setCell(getCell(row, 4), cs, regione, false, false);
                                                        setCell(getCell(row, 5), cs, cognome, false, false);
                                                        setCell(getCell(row, 6), cs, nome, false, false);
                                                        setCell(getCell(row, 7), cs, codicefiscale, false, false);
                                                        setCell(getCell(row, 8), cs, start, false, false);
                                                        setCell(getCell(row, 9), cs, end, false, false);
                                                        setCell(getCell(row, 10),
                                                                cellStyle_double,
                                                                String.valueOf(oreprogetto),
                                                                false, true);

                                                        index_row.addAndGet(1);
                                                        indice.addAndGet(1);

                                                        if (tot_ore_docenti_FA.get() > 0) {
                                                            String indice_dainserire = get_incremental(indicitxt.get());
                                                            indicitxt.addAndGet(1);

                                                            sd01_W.write(indice_dainserire + separator + codicefiscale + separator
                                                                    + codice_yisu_neet + separator + contodocentiA + separator + getDoubleforTXT(tot_ore_docenti_FA.get()) + separator
                                                                    + getDoubleforTXT(quota_parte_totale_FA) + separator + "1" + separator + codice_bb + separator
                                                                    + separator + nomerend + separator + separator + "l'importo è quota parte della docenza rispetto al numero dei partecipanti_vedasi scheda allegata alla DDR");
                                                            sd01_W.newLine();

                                                            sd03_W.write(indice_dainserire + separator + codicefiscale + "_" + cip + ".pdf" + separator + "S" + separator + "AL"
                                                                    + separator + separator + separator + indice_dainserire + separator + "CV Docente; Doc. accompagnamento Neet; Domanda iscrizione Neet; Patto di servizio o PIP; SAP; Documentazione neet; Resgistri A e B; Output neet");
                                                            sd03_W.newLine();

                                                        }
                                                        if (tot_ore_docenti_FB.get() > 0) {
                                                            String indice_dainserire = get_incremental(indicitxt.get());
                                                            indicitxt.addAndGet(1);
                                                            sd01_W.write(indice_dainserire + separator + codicefiscale + separator
                                                                    + codice_yisu_neet + separator + contodocentiB + separator + getDoubleforTXT(tot_ore_docenti_FB.get()) + separator
                                                                    + getDoubleforTXT(quota_parte_totale_FB) + separator + "1" + separator + codice_bb + separator
                                                                    + separator + nomerend + separator + separator + "l'importo è quota parte della docenza rispetto al numero dei partecipanti_vedasi scheda allegata alla DDR");
                                                            sd01_W.newLine();

                                                            sd03_W.write(indice_dainserire + separator + codicefiscale + "_" + cip + ".pdf" + separator + "S" + separator + "AL"
                                                                    + separator + separator + separator + indice_dainserire + separator + "CV Docente; Doc. accompagnamento Neet; Domanda iscrizione Neet; Patto di servizio o PIP; SAP; Documentazione neet; Resgistri A e B; Output neet");
                                                            sd03_W.newLine();

                                                        }
                                                        String indice_dainserire1 = get_incremental(indicitxt.get());
                                                        indicitxt.addAndGet(1);
                                                        sd01_W.write(indice_dainserire1 + separator + codicefiscale + separator
                                                                + codice_yisu_neet + separator + contoallievifaseA + separator + getDoubleforTXT(tot_A) + separator
                                                                + getDoubleforTXT(tot_single) + separator + "1" + separator + codice_bb + separator
                                                                + separator + nomerend + separator + separator);
                                                        sd01_W.newLine();

                                                        sd03_W.write(indice_dainserire1 + separator + codicefiscale + "_" + cip + ".pdf" + separator + "S" + separator + "AL"
                                                                + separator + separator + separator + indice_dainserire1 + separator + "CV Docente; Doc. accompagnamento Neet; Domanda iscrizione Neet; Patto di servizio o PIP; SAP; Documentazione neet; Resgistri A e B; Output neet");
                                                        sd03_W.newLine();

                                                        String indice_dainserire2 = get_incremental(indicitxt.get());
                                                        indicitxt.addAndGet(1);
                                                        sd01_W.write(indice_dainserire2 + separator + codicefiscale + separator
                                                                + codice_yisu_neet + separator + contoallievifaseB + separator + getDoubleforTXT(tot_B) + separator
                                                                + getDoubleforTXT(tot_single_B) + separator + "1" + separator + codice_bb + separator
                                                                + separator + nomerend + separator + separator);
                                                        sd01_W.newLine();

                                                        sd03_W.write(indice_dainserire2 + separator + codicefiscale + "_" + cip + ".pdf" + separator + "S" + separator + "AL"
                                                                + separator + separator + separator + indice_dainserire2 + separator + "CV Docente; Doc. accompagnamento Neet; Domanda iscrizione Neet; Patto di servizio o PIP; SAP; Documentazione neet; Resgistri A e B; Output neet");
                                                        sd03_W.newLine();

                                                    }
                                                }
                                            } catch (Exception ex2) {
                                                ex2.printStackTrace();
                                            }
                                        }

                                    });

                                    CellRangeAddress region_C1 = new CellRangeAddress(2, 3, iniziofaseb.get(), iniziofaseb.get());
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C1, cs);
                                    sh_pr.addMergedRegion(region_C1);
                                    CellRangeAddress region_C2 = new CellRangeAddress(4, 6, iniziofaseb.get(), iniziofaseb.get());
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C2, cs);
                                    sh_pr.addMergedRegion(region_C2);

                                    setCell(getCell(row_intest, iniziofaseb.get()), intestazione_1, "TOTALE ORE", false, false);
                                    setCell(getCell(row_intest3, iniziofaseb.get()), cs, "FASE A\n+\nFASE B", false, false);

                                    CellRangeAddress region_C3 = new CellRangeAddress(2, 3, iniziofaseb.get() + 1, iniziofaseb.get() + 3);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C3, cs);
                                    sh_pr.addMergedRegion(region_C3);
                                    CellRangeAddress region_C4 = new CellRangeAddress(4, 6, iniziofaseb.get() + 1, iniziofaseb.get() + 1);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C4, cs);
                                    sh_pr.addMergedRegion(region_C4);
                                    setCell(getCell(row_intest, iniziofaseb.get() + 1), intestazione_4, "TOTALE RIMBORSO", false, false);
                                    setCell(getCell(row_intest3, iniziofaseb.get() + 1), cs, "TOTALE\nFASE A + FASE B", false, false);

                                    CellRangeAddress region_C5 = new CellRangeAddress(4, 6, iniziofaseb.get() + 2, iniziofaseb.get() + 2);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C5, cs);
                                    sh_pr.addMergedRegion(region_C5);
                                    setCell(getCell(row_intest3, iniziofaseb.get() + 2), cs, "TOTALE\n70%", false, false);
                                    CellRangeAddress region_C6 = new CellRangeAddress(4, 6, iniziofaseb.get() + 3, iniziofaseb.get() + 3);
                                    cleanBeforeMergeOnValidCells(sh_pr, region_C6, cs);
                                    sh_pr.addMergedRegion(region_C6);
                                    setCell(getCell(row_intest3, iniziofaseb.get() + 3), cs, "TOTALE\n30%", false, false);

                                    //RIGA TOTALI
                                    XSSFRow totaliALL = getRow(sh_pr, index_allievo.get());
                                    for (int i = 1; i < 24; i++) {
                                        setCell(getCell(totaliALL, i), cstotal, "", false, false);
                                    }
                                    setCell(getCell(totaliALL, 24), cstotal_double, String.valueOf(total_allievo_1.get()), false, true);
                                    setCell(getCell(totaliALL, 25), cstotal_double, String.valueOf(tot_docenti.get()), false, true);
                                    setCell(getCell(totaliALL, 26), cstotal_double, String.valueOf(tot_docenti_FA.get()), false, true);
                                    setCell(getCell(totaliALL, 27), cstotal_double, String.valueOf(tot_docenti_FB.get()), false, true);
                                    setCell(getCell(totaliALL, 28), cstotal_double, String.valueOf(totale_fa_allievi.get()), false, true);

                                    for (int ix = 0; ix < elencogruppi_B.size(); ix++) {
                                        String numerogruppo = elencogruppi_B.get(ix);
                                        if (indicifaseB.get(numerogruppo) != null) {
                                            int indicedainiziare = indicifaseB.get(numerogruppo);

                                            setCell(getCell(totaliALL, indicedainiziare), cstotal, "", false, false);
                                            setCell(getCell(totaliALL, indicedainiziare + 1), cstotal, "", false, false);
                                            setCell(getCell(totaliALL, indicedainiziare + 2), cstotal, "", false, false);
                                            setCell(getCell(totaliALL, indicedainiziare + 3), cstotal, "", false, false);
                                            setCell(getCell(totaliALL, indicedainiziare + 4), cstotal, "", false, false);
                                            setCell(getCell(totaliALL, indicedainiziare + 5), cstotal_double, String.valueOf(totali_gruppiB.get(numerogruppo)), false, true);
                                        }
                                    }
                                    setCell(getCell(totaliALL, iniziofaseb.get()), cstotal, "", false, false);

                                    setCell(getCell(totaliALL, iniziofaseb.get() + 1), cstotal_double, String.valueOf(totale_pr.get()), false, true);
                                    setCell(getCell(totaliALL, iniziofaseb.get() + 2), cstotal_double, String.valueOf(totale_pr_70.get()), false, true);
                                    setCell(getCell(totaliALL, iniziofaseb.get() + 3), cstotal_double, String.valueOf(totale_pr_30.get()), false, true);

                                    iniziofaseb.addAndGet(1);

                                    index_allievo.addAndGet(2);
                                    XSSFRow numpart = getRow(sh_pr, index_allievo.get());
                                    setCell(getCell(numpart, 7), intestazione_5, "TOTALE PARTECIPANTI", false, false);
                                    setCell(getCell(numpart, 8), cstotal_int, String.valueOf(numpartecipanti), true, false);

                                    XSSFRow totalecorso = getRow(sh_pr, index_allievo.get() + 2);
                                    setCell(getCell(totalecorso, 7), intestazione_5, "TOTALE IMPORTO CORSO", false, false);
                                    setCell(getCell(totalecorso, 8), cstotal_double, String.valueOf(totale_pr.get()), false, true);

                                    //
                                    total_rend.addAndGet(totale_pr.get());

//                                index_allievo.addAndGet(4);
                                    for (int ix = 1; ix < iniziofaseb.get() + 4; ix++) {
                                        sh_pr.autoSizeColumn(ix);
                                    }

                                }
                            }

                        }

                        XSSFRow row_total = getRow(sh1, index_row.get());

                        XSSFCellStyle row_totalCellStyle = wb.createCellStyle();
                        row_totalCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                        row_totalCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        row_totalCellStyle.setFont(font_total);
                        setCell(getCell(row_total, 0), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 1), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 2), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 3), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 4), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 5), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 6), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 7), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 8), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 9), row_totalCellStyle, "", false, false);
                        setCell(getCell(row_total, 10), row_totalCellStyle, String.valueOf(oretotali.get()), false, true);
                        index_row.addAndGet(1);

                        String contentlast = "LUOGO                                                    DATA                                         firma del legale rappresentante  o suo delegato                                                             timbro";
                        XSSFRow row_LAST = getRow(sh1, index_row.get());
                        setCell(getCell(row_LAST, 0), contentlast.toUpperCase());
                        sh1.addMergedRegion(new CellRangeAddress(index_row.get(), index_row.get(), 0, 10));

                        for (int i = 0; i < 13; i++) {
                            sh1.autoSizeColumn(i);
                        }

                        output_xlsx = new File(pathdest + "/Prospetto_Riepilogo_" + new DateTime().toString("yyyyMMdd") + ".xlsx");

                        try (FileOutputStream outputStream = new FileOutputStream(output_xlsx)) {
                            wb.write(outputStream);
                        }

                        sd01_W.close();
                        fw01.close();
                        sd03_W.close();
                        fw03.close();

                        String def = nomerend + separator + codice_yisu_neet + separator + nomerend_cod + separator
                                + new DateTime().toString("dd/MM/yyyy") + separator + start_rend.toString("dd/MM/yyyy")
                                + separator + end_rend.toString("dd/MM/yyyy") + separator
                                + getDoubleforTXT(total_rend.get()) + separator
                                + getDoubleforTXT(total_rend.get()) + separator;

                        ddr_W.write(def);
                        ddr_W.close();
                        fwddr.close();

                    }
                }
            }
            output.add(output_xlsx);
            output.add(ddr);
            output.add(sd01);
            output.add(sd03);

            File zip = new File(filezip);

            if (zipListFiles(output, zip)) {
                return zip;
            }
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }

        return null;
    }

    private static String get_incremental(int index) {

        try {

            String end = String.valueOf(index);
            String start = new DateTime().toString("YYMMdd");
            int mancanti = 10 - end.length() - start.length();
            String middle = StringUtils.leftPad("", mancanti, "0");
            return start + middle + end;
        } catch (Exception e) {
        }
        return "000000000";
    }

    private static String getDoubleforTXT(double ing) {
        try {
            BigDecimal bd = new BigDecimal(ing).setScale(2, RoundingMode.HALF_EVEN);

            if (bd.doubleValue() % 1 == 0) {
                return String.valueOf(bd.intValue());
            } else {
                return (String.format(Locale.ITALIAN, "%.2f", bd.doubleValue()));
            }
        } catch (Exception e) {
            return (String.format(Locale.ITALIAN, "%.2f", ing));
        }
    }

    private static void cleanBeforeMergeOnValidCells(XSSFSheet sheet, CellRangeAddress region, XSSFCellStyle cellStyle) {
        try {
            for (int rowNum = region.getFirstRow(); rowNum <= region.getLastRow(); rowNum++) {
                XSSFRow row = getRow(sheet, rowNum);
                for (int colNum = region.getFirstColumn(); colNum <= region.getLastColumn(); colNum++) {
                    XSSFCell currentCell = getCell(row, colNum);
                    currentCell.setCellStyle(cellStyle);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static List<Items> calendario(int idpr, Connection conn) {

        List<Items> out = new ArrayList<>();
        List<Items> temp = new ArrayList<>();
        try {

            String sql = "SELECT lc.lezione,lm.giorno,lm.orario_start,lm.orario_end,ud.fase,lm.gruppo_faseB "
                    + "FROM lezioni_modelli lm, modelli_progetti mp, lezione_calendario lc, "
                    + "unita_didattiche ud, fad_multi f, "
                    + "progetti_formativi p  "
                    + "WHERE mp.id_modello=lm.id_modelli_progetto AND lc.id_lezionecalendario=lm.id_lezionecalendario "
                    + "AND ud.codice=lc.codice_ud "
                    + "AND p.idprogetti_formativi=f.idprogetti_formativi AND f.idprogetti_formativi=mp.id_progettoformativo "
                    + "AND (lm.gruppo_faseB = 0 OR lm.gruppo_faseB=f.numerocorso) "
                    + "AND mp.id_progettoformativo=" + idpr
                    + " GROUP BY lm.gruppo_faseB,lm.giorno,lm.id_lezionecalendario "
                    + " ORDER BY lm.gruppo_faseB,lm.giorno";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {

                    String fase = rs.getString("ud.fase").endsWith("A") ? "A" : "B";
                    String gruppo = fase.equals("A") ? "1" : rs.getString("lm.gruppo_faseB");
                    Items itm = new Items(fase, rs.getString("lm.giorno"), rs.getString("lm.orario_start"), rs.getString("lm.orario_end"), gruppo);
                    temp.add(itm);
                }
            }
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }

        for (int i = 0; i < temp.size(); i++) {
            if (i == temp.size() - 1) {
                out.add(temp.get(i));
            } else {
                if (temp.get(i).getData().equals(temp.get(i + 1).getData())) {
                    out.add(new Items(temp.get(i).getFase(), temp.get(i).getData(), temp.get(i).getOrainizio(), temp.get(i + 1).getOrafine(), temp.get(i).getGruppo()));
                    i++;
                } else {
                    out.add(temp.get(i));
                }
            }
        }

        return out;
    }

    private static Map<Long, Long> OreRendicontabiliAlunni_faseA(Connection conn, int pf) {
        Map result = new HashMap();
        try {

            String sql = "SELECT sum(totaleorerendicontabili) as totOre,idutente FROM registro_completo WHERE fase = 'A' AND ruolo like 'ALLIEVO%' "
                    + " AND idutente IN (SELECT a.idallievi FROM allievi a WHERE a.idprogetti_formativi = ? AND a.id_statopartecipazione='01')"
                    + "AND idprogetti_formativi = ? GROUP BY idutente";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, pf);
            ps.setInt(2, pf);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getLong("idutente"), rs.getLong("totOre"));
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static Map<Long, Long> OreRendicontabiliDocentiFASEA(Connection conn, int pf) {
        Map result = new HashMap();
        try {

            String sql = "SELECT sum(totaleorerendicontabili) as totOre,idutente FROM registro_completo WHERE ruolo = 'DOCENTE' "
                    + "AND fase='A' AND idprogetti_formativi = ? GROUP BY idutente";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, pf);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getLong("idutente"), rs.getLong("totOre"));
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }

    private static double roundFloatAndFormat(float f) {
        try {

            double hours = f / 1000.0 / 60.0 / 60.0;
            BigDecimal bigDecimal = new BigDecimal(hours);
            bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_EVEN);
            return bigDecimal.doubleValue();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;

    }

}
