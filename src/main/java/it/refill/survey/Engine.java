/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.refill.survey;

import emoji4j.EmojiUtils;
import static it.refill.exe.Constant.conf;
import it.refill.exe.Db_Bando;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author raf
 */
public class Engine {

    private static final String HOSTSUR = conf.getString("db.host.survey") + ":3308/limesurvey";

    public static void main(String[] args) {
        String insertanswer = "INSERT INTO survey_answer VALUES (?,?,?,?,?,?)";
        String idi = conf.getString("quest.ingresso");
        String idu = conf.getString("quest.uscita");
        try {
            Db_Bando quest = new Db_Bando(HOSTSUR, true);
            List<Risposte> elenco = new ArrayList<>();
            String sel0 = "SHOW COLUMNS FROM lime_survey_" + idi;
            try ( Statement st0 = quest.getConnection().createStatement();  ResultSet rs0 = st0.executeQuery(sel0)) {
                while (rs0.next()) {
                    if (rs0.getString(1).startsWith(idi)) {
                        elenco.add(new Risposte(rs0.getString(1), rs0.getString(1).split("X")[1], rs0.getString(1).split("X")[2]));
                    }
                }
            }

            String sel1 = "SELECT * FROM lime_survey_" + idi + " WHERE submitdate IS NOT NULL";
            try ( Statement st1 = quest.getConnection().createStatement();  ResultSet rs1 = st1.executeQuery(sel1)) {
                while (rs1.next()) {
                    if (rs1.getString(idi + "X2X31") != null) {

                        int idutente = rs1.getInt(idi + "X2X31");
                        String piattaforma = rs1.getString(idi + "X2X30");
                        String data = rs1.getString("submitdate");
                        String ip = rs1.getString("ipaddr");
                        System.out.println(piattaforma + " " + idutente + " --QI-- " + data + " - " + ip);
                        StringBuilder sb1 = new StringBuilder("");
                        elenco.forEach(r1 -> {
                            try {

                                String sel1A = "SELECT a.title,b.question FROM lime_questions a, lime_question_l10ns b WHERE a.sid="
                                        + idi + " AND a.gid=" + r1.getIdgruppo() + " AND a.qid=" + r1.getIddomanda() + " AND a.qid=b.qid";
                                try ( Statement st1A = quest.getConnection().createStatement();  ResultSet rs1A = st1A.executeQuery(sel1A)) {
                                    if (rs1A.next()) {
                                        String sel2A = "SELECT b.answer FROM lime_answers a, lime_answer_l10ns b "
                                                + "WHERE a.qid=" + r1.getIddomanda() + " AND a.code ='"
                                                + rs1.getString(r1.getCodice()) + "' AND b.aid=a.aid";
                                        try ( Statement st2A = quest.getConnection().createStatement();  ResultSet rs2A = st2A.executeQuery(sel2A)) {
                                            if (rs2A.next()) {
//                                                String domanda = Jsoup.parse(rs1A.getString(2)).text();
                                                String risposta = EmojiUtils.htmlify(rs2A.getString(1)).contains("&#9733")
                                                        ? String.valueOf(StringUtils.countMatches(EmojiUtils.htmlify(rs2A.getString(1)), "&#9733")
                                                        ) : rs2A.getString(1);
                                                sb1.append(r1.getCodice()).append("=").append(risposta).append(";;;");
                                            }

                                        }

                                    }
                                }

//                                
                            } catch (Exception ex2) {
                                ex2.printStackTrace();
                            }
                        });

                        if (!sb1.toString().trim().isEmpty()) {
                            Db_Bando db = new Db_Bando(conf.getString("db.host") + ":3306/enm_gestione_neet_prod");
                            try {
                                try ( PreparedStatement ps1 = db.getConnection().prepareStatement(insertanswer)) {
                                    ps1.setInt(1, idutente);
                                    ps1.setString(2, piattaforma + "I");
                                    ps1.setString(3, sb1.toString().trim());
                                    ps1.setString(4, ip);
                                    ps1.setString(5, ip);
                                    ps1.setString(6, data);
                                    ps1.execute();
                                }
                            } catch (Exception ex3) {
                                ex3.printStackTrace();
                            }
                            db.closeDB();
                        }
                    }
                }
            }
            System.out.println("-------------------------");
            elenco.clear();
            String sel0B = "SHOW COLUMNS FROM lime_survey_" + idu;
            try ( Statement st0 = quest.getConnection().createStatement();  ResultSet rs0 = st0.executeQuery(sel0B)) {
                while (rs0.next()) {
                    if (rs0.getString(1).startsWith(idu)) {
                        elenco.add(new Risposte(rs0.getString(1), rs0.getString(1).split("X")[1], rs0.getString(1).split("X")[2]));
                    }
                }
            }

            String sel2 = "SELECT * FROM lime_survey_" + idu + " WHERE submitdate IS NOT NULL";
            try ( Statement st1 = quest.getConnection().createStatement();  ResultSet rs1 = st1.executeQuery(sel2)) {
                while (rs1.next()) {
                    if (rs1.getString(idu + "X3X28") != null) {
                        int idutente = rs1.getInt(idu + "X3X28");
                        String piattaforma = rs1.getString(idu + "X3X29");
                        String data = rs1.getString("submitdate");
                        String ip = rs1.getString("ipaddr");
                        System.out.println(piattaforma + " " + idutente + " --QU-- " + data + " - " + ip);
                        StringBuilder sb2 = new StringBuilder("");
                        elenco.forEach(r1 -> {
                            try {
                                String sel1A = "SELECT a.title,b.question FROM lime_questions a, lime_question_l10ns b WHERE a.sid="
                                        + idu + " AND a.gid=" + r1.getIdgruppo() + " AND a.qid=" + r1.getIddomanda() + " AND a.qid=b.qid";
                                try ( Statement st1A = quest.getConnection().createStatement();  ResultSet rs1A = st1A.executeQuery(sel1A)) {
                                    if (rs1A.next()) {
                                        String sel2A = "SELECT b.answer FROM lime_answers a, lime_answer_l10ns b "
                                                + "WHERE a.qid=" + r1.getIddomanda() + " AND a.code ='"
                                                + rs1.getString(r1.getCodice()) + "' AND b.aid=a.aid";
                                        try ( Statement st2A = quest.getConnection().createStatement();  ResultSet rs2A = st2A.executeQuery(sel2A)) {
                                            if (rs2A.next()) {
//                                                String domanda = Jsoup.parse(rs1A.getString(2)).text();
                                                String risposta = EmojiUtils.htmlify(rs2A.getString(1)).contains("&#9733")
                                                        ? String.valueOf(StringUtils.countMatches(EmojiUtils.htmlify(rs2A.getString(1)), "&#9733")
                                                        ) : rs2A.getString(1);
                                                sb2.append(r1.getCodice()).append("=").append(risposta).append(";;;");
                                            }

                                        }

                                    }
                                }
                            } catch (Exception ex2) {
                                ex2.printStackTrace();
                            }
                        });
                        
                        
                        if (!sb2.toString().trim().isEmpty()) {
                            Db_Bando db = new Db_Bando(conf.getString("db.host") + ":3306/enm_gestione_neet_prod");
                            try {
                                try ( PreparedStatement ps1 = db.getConnection().prepareStatement(insertanswer)) {
                                    ps1.setInt(1, idutente);
                                    ps1.setString(2, piattaforma + "U");
                                    ps1.setString(3, sb2.toString().trim());
                                    ps1.setString(4, ip);
                                    ps1.setString(5, ip);
                                    ps1.setString(6, data);
                                    ps1.execute();
                                }
                            } catch (Exception ex3) {
                                ex3.printStackTrace();
                            }
                            db.closeDB();
                        }
                        
                        
                    }
                }
            }
            quest.closeDB();
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }

    }
}
