
import rc.soop.gestione.Toscana_gestione;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Administrator
 */
public class ore {

    public static void main(String[] args) {
        Toscana_gestione tg = new Toscana_gestione(false);
        String sql = "SELECT a.idallievi FROM allievi a WHERE a.idprogetti_formativi IS NOT NULL";
        tg.ore_convalidateAllievi(sql);
        tg.ore_ud(sql);
        
    }
}
