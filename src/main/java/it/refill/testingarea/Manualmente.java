package it.refill.testingarea;


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
public class Manualmente {

    public static void main(String[] args) {

        int pf = 172;
        DeD_gestione d = new DeD_gestione(false);
        d.verifica_stanze(pf);
        d.fad_allievi(pf, true);
        d.fad_docenti(pf, true);
        d.fad_ospiti(pf, true);

    }
}
