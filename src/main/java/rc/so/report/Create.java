/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.so.report;

import com.google.common.base.Splitter;
import rc.so.exe.Constant;
import static rc.so.exe.Constant.calcoladurata;
import static rc.so.exe.Constant.estraiEccezione;
import static rc.so.exe.Constant.timestampSQL;
import rc.so.exe.Db_Bando;
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

    public static final Logger log = Constant.createLog("FADReport_", "/mnt/mcn/test/log/");

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

    public static void gestisciorerendicontabili(LinkedList<Presenti> report, long ore, int idpr, String host) {

        try {
            DateTimeFormatter fmt = forPattern(timestampSQL);
            List<Presenti> allievi = report.stream().filter(pr1 -> !pr1.getRuolo().equalsIgnoreCase("DOCENTE")).collect(Collectors.toList());
//            List<Presenti> docenti = report.stream().filter(pr1 -> pr1.getRuolo().equalsIgnoreCase("DOCENTE")).collect(Collectors.toList());

//            if (docenti.size() == 1) {

                Presenti docente = report.stream().filter(pr1 -> pr1.getRuolo().equalsIgnoreCase("DOCENTE")).findAny().orElse(null);

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

                        long millischeck = nuova_rendicontazione_ore(millis_rendicontabili.get(), idpr, host);
                        long millischeck1 = nuova_rendicontazione_ore(cnsmr.getMillistotaleore(), idpr, host);

                        if (millischeck >= ore) {
                            cnsmr.setTotaleorerendicontabili(calcoladurata(ore));
                            cnsmr.setMillistotaleorerendicontabili(ore);
                        } else if (millischeck >= millischeck1) {
                            cnsmr.setTotaleorerendicontabili(calcoladurata(millischeck1));
                            cnsmr.setMillistotaleorerendicontabili(millischeck1);
                        } else {
                            cnsmr.setTotaleorerendicontabili(calcoladurata(millischeck));
                            cnsmr.setMillistotaleorerendicontabili(millischeck);
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

                    long millischeck = nuova_rendicontazione_ore(millis_rendicontabili_DOCENTE.get(), idpr, host);
                    long millischeck1 = nuova_rendicontazione_ore(docente.getMillistotaleore(), idpr, host);

                    if (millischeck >= ore) {
                        docente.setTotaleorerendicontabili(calcoladurata(ore));
                        docente.setMillistotaleorerendicontabili(ore);
                    } else if (millischeck >= millischeck1) {
                        docente.setTotaleorerendicontabili(calcoladurata(millischeck1));
                        docente.setMillistotaleorerendicontabili(millischeck1);
                    } else {
                        docente.setTotaleorerendicontabili(calcoladurata(millischeck));
                        docente.setMillistotaleorerendicontabili(millischeck);
                    }
                }
//            } else {
//                
//                
//                
//                
//            }
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

    public static long nuova_rendicontazione_ore(long millis, int idpr, String host) {
        try {
            Db_Bando db0 = new Db_Bando(host);
            List<Integer> listpr = db0.elencoidnuovarendicontazione();
            db0.closeDB();
            if (listpr.contains(idpr)) {
                long real = millis / 1800000;
                return real * 1800000;
            }
        } catch (Exception e1) {
            log.severe(estraiEccezione(e1));
        }
        return millis;
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
                case 678:
                    db0.getConnection().createStatement().executeUpdate("UPDATE fad_track f SET action = REPLACE(action,'CARA&#39;','CARA\\'') "
                            + "WHERE f.room LIKE 'FADMCNDD_" + idpr + "%' AND action LIKE '%CARA&#39;'");
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.severe(estraiEccezione(e));
        }
    }

}
