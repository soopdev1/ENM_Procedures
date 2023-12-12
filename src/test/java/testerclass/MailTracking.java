package testerclass;




import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import rc.so.exe.Constant;
import rc.so.exe.Db_Bando;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author rcosco
 */
public class MailTracking {

//    public static void main(String[] args) {
//        Db_Bando bando = new Db_Bando(conf.getString("db.host") + ":3306/enm_gestione_neet_prod");
//        String mailsender = bando.getPath("mailsender");
//        boolean es = sendMail(mailsender, new String[]{"raffaele.cosco@faultless.it"}, new String[]{}, new String[]{}, "testing", "testing", bando);
//        bando.closeDB();
//    }

    public static boolean sendMail(String name, String[] to, String[] cc, String[] bcc, String txt, String subject, Db_Bando dbb) {
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;

        String filename = "";
        String content_type = "";
        String b64 = "";

        String mailjet_api = dbb.getPath("mailjet_api");
        String mailjet_secret = dbb.getPath("mailjet_secret");
        String mailjet_name = dbb.getPath("mailjet_name");

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient customHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();

        ClientOptions options = ClientOptions.builder()
                .apiKey(mailjet_api)
                .apiSecretKey(mailjet_secret)
                .okHttpClient(customHttpClient)
                .build();

        client = new MailjetClient(options);

        //client = new MailjetClient(mailjet_api, mailjet_secret, new ClientOptions("v3.1"));
//        client.setDebug(1);
        JSONArray dest = new JSONArray();
        JSONArray ccn = new JSONArray();
        JSONArray ccj = new JSONArray();

        if (to != null) {
            for (String s : to) {
                dest.put(new JSONObject().put("Email", s)
                        .put("Name", ""));
            }
        } else {
            dest.put(new JSONObject().put("Email", "")
                    .put("Name", ""));
        }

        if (cc != null) {
            for (String s : cc) {
                ccj.put(new JSONObject().put("Email", s)
                        .put("Name", ""));
            }
        } else {
            ccj.put(new JSONObject().put("Email", "")
                    .put("Name", ""));
        }

        if (bcc != null) {
            for (String s : bcc) {
                ccn.put(new JSONObject().put("Email", s)
                        .put("Name", ""));
            }
        } else {
            ccn.put(new JSONObject().put("Email", "")
                    .put("Name", ""));
        }
        
        try {
            JSONObject mail = new JSONObject().put(Emailv31.Message.FROM, new JSONObject()
                    .put("Email", mailjet_name)
                    .put("Name", name))
                    .put(Emailv31.Message.TO, dest)
                    .put(Emailv31.Message.CC, ccj)
                    .put(Emailv31.Message.BCC, ccn)
                    .put(Emailv31.Message.SUBJECT, subject)
                    .put(Emailv31.Message.HTMLPART, txt);

            request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, new JSONArray()
                            .put(mail));

            response = client.post(request);
            System.out.println("MAIL TO " + dest.toList() + " : " + response.getStatus() + " -- " + response.getData());
            return response.getStatus() == 200;
        } catch (Exception ex) {
            System.err.println("MAIL ERROR: " + Constant.estraiEccezione(ex));
            return false;
        }
    }
}



