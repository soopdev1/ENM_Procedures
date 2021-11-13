package it.refill.testingarea;

import static it.refill.exe.Constant.estraiEccezione;
import it.refill.exe.Db_Bando;
import it.refill.report.Complessivo;
import it.refill.report.Create;
import it.refill.report.FaseA;
import it.refill.report.FaseB;
import it.refill.report.Lezione;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author rcosco
 */
public class GeneraReport {

    public static void main(String[] args) {
        try {

            boolean neet = true;
            boolean testing = false;
            int idpr = 114;
//            
//            
            FaseA FA = new FaseA(testing, neet);
            FaseB FB = new FaseB(testing, neet);
//
//            //  FASE A
            List<Lezione> ca = FA.calcolaegeneraregistrofasea(idpr, FA.getHost(), false, false, false);
            FA.registro_aula_FaseA(idpr, FA.getHost(), false, false);
//
//////            FASE B
            List<Lezione> cb = FB.calcolaegeneraregistrofaseb(idpr, FA.getHost(), false, false, false);
            FB.registro_aula_FaseB(idpr, FA.getHost(), false, cb);
//            
//
            Complessivo c1 = new Complessivo(FA.getHost());
            c1.registro_complessivo(idpr, c1.getHost(), ca, cb, false);
//            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
