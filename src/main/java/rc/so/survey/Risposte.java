/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rc.so.survey;

/**
 *
 * @author raf
 */
public class Risposte {

    String codice;
    String idgruppo;
    String iddomanda;

    public Risposte(String codice, String idgruppo, String iddomanda) {
        this.codice = codice;
        this.idgruppo = idgruppo;
        this.iddomanda = iddomanda;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public String getIdgruppo() {
        return idgruppo;
    }

    public void setIdgruppo(String idgruppo) {
        this.idgruppo = idgruppo;
    }

    public String getIddomanda() {
        return iddomanda;
    }

    public void setIddomanda(String iddomanda) {
        this.iddomanda = iddomanda;
    }



}
