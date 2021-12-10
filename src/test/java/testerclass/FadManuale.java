package testerclass;




import it.refill.exe.DeD_gestione;
import it.refill.exe.Neet_gestione;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author rcosco
 */
public class FadManuale {

    public static void main(String[] args) {

        int pf = 259;
        
        DeD_gestione d = new DeD_gestione(false);
        
        
        d.verifica_stanze(pf);
        d.fad_allievi(pf, true);
        d.fad_docenti(pf, true);
        d.fad_ospiti(pf, true);
        
        
//        Neet_gestione n = new Neet_gestione(false);
//        n.verifica_stanze(pf);
//        n.fad_allievi(pf, true);
//        n.fad_docenti(pf, true);
//        n.fad_ospiti(pf, true);
        
    }
}
