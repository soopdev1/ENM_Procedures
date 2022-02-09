/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.report;

import com.google.common.base.Splitter;
import it.refill.exe.Constant;
import static it.refill.exe.Constant.calcoladurata;
import static it.refill.exe.Constant.estraiEccezione;
import static it.refill.exe.Constant.timestampSQL;
import it.refill.exe.Db_Bando;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import static org.joda.time.format.DateTimeFormat.forPattern;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author rcosco
 */
public class Create {

    public static final Logger log = Constant.createLog("FADReport_", "/mnt/mcn/test/log/", true);

    public static void crea(boolean neet, boolean testing) {
        boolean print = false;
        boolean save = true;

        log.log(Level.INFO, "PRINT: {0}", print);
        log.log(Level.INFO, "SAVE: {0}", save);

        List<Integer> list_id = new ArrayList<>();

        try {
            FaseA FA = new FaseA(testing, neet);
            Db_Bando db0 = new Db_Bando(FA.getHost());
//            String sql0 = "SELECT pf.idprogetti_formativi from progetti_formativi pf WHERE stato IN ('ATA','ATB','DVA','DVB')";

            String sql0 = "SELECT DISTINCT(mp.id_progettoformativo) "
                    + "FROM lezioni_modelli lm, modelli_progetti mp "
                    + "WHERE mp.id_modello=lm.id_modelli_progetto "
                    + "AND lm.giorno = DATE_SUB(CURDATE(), INTERVAL 1 DAY)";

            try (Statement st0 = db0.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
                while (rs0.next()) {
                    list_id.add(rs0.getInt(1));
                }
            }
            db0.closeDB();

            FaseB FB = new FaseB(testing, neet);

            list_id.forEach(idpr -> {

                //  FASE A
                try {
                    log.log(Level.INFO, "REPORT FASE A - IDPR {0}", idpr);
                    List<Lezione> calendar1 = FA.calcolaegeneraregistrofasea(idpr, FA.getHost(), print, save, false);

                    FA.registro_aula_FaseA(idpr, FA.getHost(), save, calendar1, neet);
                    log.log(Level.INFO, "COMPLETATO REPORT FASE A - IDPR {0}", idpr);
                } catch (Exception e1) {
                    log.severe(estraiEccezione(e1));
                }
                //  FASE B
                try {
                    log.log(Level.INFO, "REPORT FASE B - IDPR {0}", idpr);
                    List<Lezione> calendar2 = FB.calcolaegeneraregistrofaseb(idpr, FA.getHost(), print, save, false);

                    FB.registro_aula_FaseB(idpr, FA.getHost(), save, calendar2, neet);
                    log.log(Level.INFO, "COMPLETATO REPORT FASE A - IDPR {0}", idpr);
                } catch (Exception e1) {
                    log.severe(estraiEccezione(e1));
                }

            });

            List<Integer> list_id_conclusi = new ArrayList<>();

            //COMPLESSIVO
            Db_Bando dbA0 = new Db_Bando(FA.getHost());
            String sqlA0 = "SELECT idprogetti_formativi FROM progetti_formativi WHERE END < CURDATE() AND stato NOT LIKE '%E' "
                    + "AND idprogetti_formativi NOT IN (SELECT idprogetto FROM documenti_progetti WHERE tipo=33)";
            try (Statement st0 = dbA0.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sqlA0)) {
                while (rs0.next()) {
                    list_id_conclusi.add(rs0.getInt(1));
                }
            }
            dbA0.closeDB();

            Complessivo c1 = new Complessivo(FA.getHost());
            list_id_conclusi.forEach(idpr -> {
                try {
                    log.log(Level.INFO, "REPORT COMPLESSIVO - IDPR {0}", idpr);
                    List<Lezione> ca = FA.calcolaegeneraregistrofasea(idpr, c1.getHost(), false, false, false);
                    List<Lezione> cb = FB.calcolaegeneraregistrofaseb(idpr, c1.getHost(), false, false, false);
                    c1.registro_complessivo(idpr, c1.getHost(), ca, cb, save, neet);
                    log.log(Level.INFO, "COMPLETATO REPORT COMPLESSIVO - IDPR {0}", idpr);
                } catch (Exception e1) {
                    log.severe(estraiEccezione(e1));
                }
            });
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

//    public static void main(String[] args) {
//
//        boolean testing;
//        try {
//            testing = args[0].trim().equalsIgnoreCase("test");
//        } catch (Exception e) {
//            testing = false;
//        }
//
//        boolean print = false;
//        boolean save = true;
//
//        Create.log.log(Level.INFO, "PRINT: {0}", print);
//        Create.log.log(Level.INFO, "SAVE: {0}", save);
//
//        List<Integer> list_id = new ArrayList<>();
//
//        try {
//            FaseA FA = new FaseA(testing, neet);
//            Db_Bando db0 = new Db_Bando(FA.getHost());
////            String sql0 = "SELECT pf.idprogetti_formativi from progetti_formativi pf WHERE stato IN ('ATA','ATB','DVA','DVB')";
//
//            String sql0 = "SELECT DISTINCT(mp.id_progettoformativo) "
//                    + "FROM lezioni_modelli lm, modelli_progetti mp "
//                    + "WHERE mp.id_modello=lm.id_modelli_progetto "
//                    + "AND lm.giorno = DATE_SUB(CURDATE(), INTERVAL 1 DAY)";
//
//            try (Statement st0 = db0.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sql0)) {
//                while (rs0.next()) {
//                    list_id.add(rs0.getInt(1));
//                }
//            }
//            db0.closeDB();
//
//            FaseB FB = new FaseB(testing, neet);
//
//            list_id.forEach(idpr -> {
//
//                //  FASE A
//                try {
//                    log.log(Level.INFO, "REPORT FASE A - IDPR {0}", idpr);
//                    List<Lezione> calendar1 = FA.calcolaegeneraregistrofasea(idpr, FA.getHost(), print, save, false);
//                    FA.registro_aula_FaseA(idpr, FA.getHost(), save, calendar1);
//                    log.log(Level.INFO, "COMPLETATO REPORT FASE A - IDPR {0}", idpr);
//                } catch (Exception e1) {
//                    log.severe(estraiEccezione(e1));
//                }
//                //  FASE B
//                try {
//                    log.log(Level.INFO, "REPORT FASE B - IDPR {0}", idpr);
//                    List<Lezione> calendar2 = FB.calcolaegeneraregistrofaseb(idpr, FA.getHost(), print, save, false);
//                    FB.registro_aula_FaseB(idpr, FA.getHost(), save, calendar2);
//                    log.log(Level.INFO, "COMPLETATO REPORT FASE A - IDPR {0}", idpr);
//                } catch (Exception e1) {
//                    log.severe(estraiEccezione(e1));
//                }
//
//            });
//
//            List<Integer> list_id_conclusi = new ArrayList<>();
//
//            //COMPLESSIVO
//            Db_Bando dbA0 = new Db_Bando(FA.getHost());
//            String sqlA0 = "SELECT idprogetti_formativi FROM progetti_formativi WHERE END < CURDATE() "
//                    + "AND idprogetti_formativi NOT IN (SELECT idprogetto FROM documenti_progetti WHERE tipo=33)";
//            try (Statement st0 = dbA0.getConnection().createStatement(); ResultSet rs0 = st0.executeQuery(sqlA0)) {
//                while (rs0.next()) {
//                    list_id_conclusi.add(rs0.getInt(1));
//                }
//            }
//            dbA0.closeDB();
//
//            Complessivo c1 = new Complessivo(FA.getHost());
//            list_id_conclusi.forEach(idpr -> {
//                try {
//                    log.log(Level.INFO, "REPORT COMPLESSIVO - IDPR {0}", idpr);
//                    List<Lezione> ca = FA.calcolaegeneraregistrofasea(idpr, c1.getHost(), false, false, false);
//                    List<Lezione> cb = FB.calcolaegeneraregistrofaseb(idpr, c1.getHost(), false, false, false);
//                    c1.registro_complessivo(idpr, c1.getHost(), ca, cb, save);
//                    log.log(Level.INFO, "COMPLETATO REPORT COMPLESSIVO - IDPR {0}", idpr);
//                } catch (Exception e1) {
//                    log.severe(estraiEccezione(e1));
//                }
//            });
//        } catch (Exception e) {
//            log.severe(estraiEccezione(e));
//        }
//
//    }
    public static void gestisciorerendicontabili(LinkedList<Presenti> report, long ore) {

        try {
            DateTimeFormatter fmt = forPattern(timestampSQL);
            Presenti docente = report.stream().filter(pr1 -> pr1.getRuolo().equalsIgnoreCase("DOCENTE")).findAny().orElse(null);
            List<Presenti> allievi = report.stream().filter(pr1 -> !pr1.getRuolo().equalsIgnoreCase("DOCENTE")).collect(Collectors.toList());

            if (docente != null && !allievi.isEmpty()) {
                List<Interval> accessi_docente = new ArrayList<>();
                List<Interval> accessi_complessivi = new ArrayList<>();
                List<String> login_docente = Splitter.on("\n").splitToList(docente.getOradilogin());
                List<String> logout_docente = Splitter.on("\n").splitToList(docente.getOradilogout());
                for (int x = 0; x < login_docente.size(); x++) {
                    DateTime start1 = fmt.parseDateTime("2021-01-01 " + login_docente.get(x));
                    DateTime end1 = fmt.parseDateTime("2021-01-01 " + logout_docente.get(x));
                    if (end1.isAfter(start1)) {
                        accessi_docente.add(new Interval(start1, end1));
                    }
                }

                AtomicLong millis_rendicontabili_DOCENTE = new AtomicLong(0L);

                allievi.forEach(cnsmr -> {
                    AtomicLong millis_rendicontabili = new AtomicLong(0L);
                    List<Interval> accessi = new ArrayList<>();
                    List<String> login = Splitter.on("\n").splitToList(cnsmr.getOradilogin());
                    List<String> logout = Splitter.on("\n").splitToList(cnsmr.getOradilogout());

                    for (int x = 0; x < login.size(); x++) {
                        DateTime start2 = fmt.parseDateTime("2021-01-01 " + login.get(x));
                        DateTime end2 = fmt.parseDateTime("2021-01-01 " + logout.get(x));
                        if (end2.isAfter(start2)) {
                            accessi.add(new Interval(start2, end2));
                            accessi_complessivi.add(new Interval(start2, end2));
                        }
                    }
                    accessi.forEach(intervallo2 -> {
                        accessi_docente.forEach(intervallo1 -> {
                            if (intervallo2.overlaps(intervallo1)) {
                                millis_rendicontabili.addAndGet(intervallo2.overlap(intervallo1).toDurationMillis());
                            }
                        });

                    });

                    if (millis_rendicontabili.get() >= ore) {
                        cnsmr.setTotaleorerendicontabili(calcoladurata(ore));
                        cnsmr.setMillistotaleorerendicontabili(ore);
                    } else if (millis_rendicontabili.get() >= cnsmr.getMillistotaleore()) {
                        cnsmr.setTotaleorerendicontabili(cnsmr.getTotaleore());
                        cnsmr.setMillistotaleorerendicontabili(cnsmr.getMillistotaleore());
                    } else {
                        cnsmr.setTotaleorerendicontabili(calcoladurata(millis_rendicontabili.get()));
                        cnsmr.setMillistotaleorerendicontabili(millis_rendicontabili.get());
                    }

                });

                accessi_docente.forEach(intervallo1 -> {
                    DateTime start = intervallo1.getStart();
                    while (start.isBefore(intervallo1.getEnd())) {
                        for (int i = 0; i < accessi_complessivi.size(); i++) {
                            Interval ac1 = accessi_complessivi.get(i);

                            if (ac1.getStart().isBefore(start) || ac1.getStart().isEqual(start)) {
                                if (ac1.getEnd().isAfter(start) || ac1.getEnd().isEqual(start)) {
                                    millis_rendicontabili_DOCENTE.addAndGet(1000);
                                    break;
                                }
                            }
                        }
                        start = start.plusSeconds(1);
                    }
                });

                if (millis_rendicontabili_DOCENTE.get() >= ore) {
                    docente.setTotaleorerendicontabili(calcoladurata(ore));
                    docente.setMillistotaleorerendicontabili(ore);
                } else if (millis_rendicontabili_DOCENTE.get() >= docente.getMillistotaleore()) {
                    docente.setTotaleorerendicontabili(docente.getTotaleore());
                    docente.setMillistotaleorerendicontabili(docente.getMillistotaleore());
                } else {
                    docente.setTotaleorerendicontabili(calcoladurata(millis_rendicontabili_DOCENTE.get()));
                    docente.setMillistotaleorerendicontabili(millis_rendicontabili_DOCENTE.get());
                }
            }
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

    public static void manage(Db_Bando db0, int idpr) {
        try {
            switch (idpr) {
                case 82:
                    db0.getConnection().createStatement().executeUpdate("UPDATE fad_track f SET action = REPLACE(action,'-- Giorgia','-- GIORGIA GUIDALDI') "
                            + "WHERE f.room LIKE 'FADMCN_" + idpr + "%' AND action LIKE'%-- Giorgia'");
                    break;
                case 123:
                    db0.getConnection().createStatement().executeUpdate("UPDATE fad_track f SET action = REPLACE(action,'-- Manuela','-- MANUELA CAPUTO') "
                            + "WHERE f.room LIKE 'FADMCN_" + idpr + "%' AND action LIKE'%-- Manuela'");
                    break;
                case 338:
                    db0.getConnection().createStatement().executeUpdate("UPDATE fad_track f SET action = REPLACE(action,'IANNO&#39;','IANNO\\'') "
                            + "WHERE f.room LIKE 'FADMCNDD_" + idpr + "%' AND action LIKE '%IANNO&#39;'");
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

}
