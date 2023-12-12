package testerclass;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import static rc.so.exe.Constant.estraiEccezione;
import rc.so.report.Complessivo;
import static rc.so.report.Create.log;
import rc.so.report.FaseA;
import rc.so.report.FaseB;
import rc.so.report.Lezione;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author raf
 */
public class ReportFad {

//    public static void main(String[] args) {
//
//        int idpr;
//        boolean complessivo;
//        boolean testing = false;
//        boolean neet;
//
//        try {
//            idpr = Integer.parseInt(args[0]);
//            neet = Boolean.valueOf(args[1]);
//            complessivo = Boolean.valueOf(args[2]);
//        } catch (Exception e) {
//            idpr = 0;
//            neet = true;
//            complessivo = false;
//        }
//
//        if (idpr > 0) {
//
//            FaseA FA = new FaseA(testing, neet);
//            FaseB FB = new FaseB(testing, neet);
//
//            System.out.println(FA.getHost());
//
//            //  FASE A
//            try {
//                log.log(Level.INFO, "REPORT FASE A - IDPR {0}", idpr);
//                List<Lezione> calendar1 = FA.calcolaegeneraregistrofasea(idpr, FA.getHost(), false, true, false);
//                FA.registro_aula_FaseA(idpr, FA.getHost(), false, calendar1);
//                log.log(Level.INFO, "COMPLETATO REPORT FASE A - IDPR {0}", idpr);
//
//                log.log(Level.INFO, "REPORT FASE B - IDPR {0}", idpr);
//                List<Lezione> calendar2 = FB.calcolaegeneraregistrofaseb(idpr, FA.getHost(), false, true, false);
//                FB.registro_aula_FaseB(idpr, FA.getHost(), false, calendar2);
//                log.log(Level.INFO, "COMPLETATO REPORT FASE A - IDPR {0}", idpr);
//
//                if (complessivo) {
//                    Complessivo c1 = new Complessivo(FA.getHost());
//                    log.log(Level.INFO, "REPORT COMPLESSIVO - IDPR {0}", idpr);
//                    c1.registro_complessivo(idpr, c1.getHost(), calendar1, calendar2, false);
//                    log.log(Level.INFO, "COMPLETATO REPORT COMPLESSIVO - IDPR {0}", idpr);
//                }
//
//            } catch (Exception e1) {
//                log.severe(estraiEccezione(e1));
//            }
//
//        } else {
//            System.out.println("ERRORE. PROGETTO ERRATO.");
//        }
//
//    }

}
