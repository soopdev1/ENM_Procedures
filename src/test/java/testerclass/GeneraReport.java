package testerclass;



import it.refill.report.Complessivo;
import it.refill.report.FaseA;
import it.refill.report.FaseB;
import it.refill.report.Lezione;
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

            boolean neet = false;
            boolean testing = false;
            int idpr = 318;
            
//            
            FaseA FA = new FaseA(testing, neet);
//            FaseB FB = new FaseB(testing, neet);
//
            //  FASE A
            List<Lezione> ca = FA.calcolaegeneraregistrofasea(idpr, FA.getHost(), false, true, false);
            FA.registro_aula_FaseA(idpr, FA.getHost(), false, false, neet);

            //  FASE B
//            List<Lezione> cb = FB.calcolaegeneraregistrofaseb(idpr, FA.getHost(), false, true, false);
//            FB.registro_aula_FaseB(idpr, FA.getHost(), false, cb, neet);
//            
//            //  COMPLESSIVO
//            Complessivo c1 = new Complessivo(FA.getHost());
//            c1.registro_complessivo(idpr, c1.getHost(), ca, cb, false, neet);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
