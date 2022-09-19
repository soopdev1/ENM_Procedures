/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.otp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.sms.SmsSend;
import it.refill.exe.Db_Bando;
import it.refill.exe.Neet_gestione;
import static it.refill.otp.Sms.MESSAGE_MEDIUM_QUALITY;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.joda.time.DateTime;

/**
 *
 * @author rcosco
 */
public class Sms {

    private static final String BASEURL = "https://api.skebby.it/API/v1.0/REST/";
    //private static final String MESSAGE_HIGH_QUALITY = "GP";
    public static final String MESSAGE_MEDIUM_QUALITY = "TI";
    //private static final String MESSAGE_LOW_QUALITY = "SI";

    public static boolean sendSMS2021(String cell, String msg, Db_Bando db1) {
        try {
            String skebbyuser = db1.getPath("skebbyuser");
            String skebbyPwd = db1.getPath("skebbyPwd");
            String[] authKeys = login(skebbyuser, skebbyPwd);
            SendSMSRequest sendSMS = new SendSMSRequest();
            sendSMS.setMessage(msg);
            sendSMS.setMessageType(MESSAGE_MEDIUM_QUALITY);
            sendSMS.addRecipient("+39" + cell);
            sendSMS.setSender("YISU");
            boolean es = sendSMS(authKeys, sendSMS);
            return es;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String[] login(String username, String password) throws IOException {
        URL url = new URL(BASEURL + "/login?username=" + username + "&password=" + password);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = "";
        String output;
        while ((output = br.readLine()) != null) {
            response += output;
        }
        conn.disconnect();
        String[] parts = response.split(";");
        return parts;
    }

    private static boolean sendSMS(String[] authKeys, SendSMSRequest sendSMS) throws IOException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        URL url = new URL(BASEURL + "/sms");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("user_key", authKeys[0]);
        conn.setRequestProperty("Session_key", authKeys[1]);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-type", "application/json");
        conn.setDoOutput(true);
        String payload = gson.toJson(sendSMS);
        OutputStream os = conn.getOutputStream();
        os.write(payload.getBytes());
        os.flush();
        if (conn.getResponseCode() != 201) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = "";
        String output;
        while ((output = br.readLine()) != null) {
            response += output;
        }
        conn.disconnect();
        SendSMSResponse responseObj = gson.fromJson(response, SendSMSResponse.class);
        return responseObj.isValid();
    }

    public static void countSMS_yesterday(Neet_gestione ne) {
        try {
            Db_Bando db1 = new Db_Bando(ne.host);
            String skebbyuser = db1.getPath("skebbyuser");
            String skebbyPwd = db1.getPath("skebbyPwd");
            String[] authKeys = login(skebbyuser, skebbyPwd);
            String datetd = new DateTime().minusDays(1).toString("yyyyMMdd");
            URL url = new URL(BASEURL + "/smshistory?from=" + datetd + "000000&to=" + datetd + "235959&pageNumber=1&pageSize=10000");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("user_key", authKeys[0]);
            conn.setRequestProperty("Session_key", authKeys[1]);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() != 200) {
                // Print the possible error contained in body response
                String error = "";
                String output;
                BufferedReader errorbuffer = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                while ((output = errorbuffer.readLine()) != null) {
                    error += output;
                }
                System.out.println("Error! HTTP error code : " + conn.getResponseCode()
                        + ", Body message : " + error);
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }
            BufferedReader br
                    = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String response = "";
            String output;
            while ((output = br.readLine()) != null) {
                response += output;
            }
            conn.disconnect();

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            SmsReport sm = gson.fromJson(response, SmsReport.class);
            String ins = "INSERT INTO enm_report VALUES ('" + new DateTime().minusDays(1).toString("yyyy-MM-dd") + "','" + sm.getTotal() + "')";
            Db_OTP db2 = new Db_OTP(false);
            db2.getConnectionDB().createStatement().execute(ins);
            db2.closeDB();
            if (sm.getTotal() > 500) {
                SendMailJet.sendMail(db1.getPath("mailsender"), new String[]{""}, new String[]{""},
                        "VERIFICARE SMS", "IN DATA " + new DateTime().minusDays(1).toString("dd/mm/yyyy")
                        + " RISULTANO INVIATI " + sm.getTotal() + ". CONTROLLARE.", db1, Logger.global);
            }
            db1.closeDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void recuperoconteggio(String[] args) {
        try {
            Neet_gestione ne = new Neet_gestione(false);
            Db_Bando db1 = new Db_Bando(ne.host);
            String skebbyuser = db1.getPath("skebbyuser");
            String skebbyPwd = db1.getPath("skebbyPwd");
            db1.closeDB();
            String[] authKeys = login(skebbyuser, skebbyPwd);

            DateTime dtstart = new DateTime(2022, 1, 1, 0, 0);
            Db_OTP db2 = new Db_OTP(false);

            while (dtstart.isBefore(new DateTime().withMillisOfDay(0))) {
                String datetd = dtstart.toString("yyyyMMdd");
                URL url = new URL(BASEURL + "/smshistory?from=" + datetd + "000000&to=" + datetd + "235959&pageNumber=1&pageSize=10000");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("user_key", authKeys[0]);
                conn.setRequestProperty("Session_key", authKeys[1]);
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() != 200) {
                    // Print the possible error contained in body response
                    String error = "";
                    String output;
                    BufferedReader errorbuffer = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                    while ((output = errorbuffer.readLine()) != null) {
                        error += output;
                    }
                    System.out.println("Error! HTTP error code : " + conn.getResponseCode()
                            + ", Body message : " + error);
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                }
                BufferedReader br
                        = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String response = "";
                String output;
                while ((output = br.readLine()) != null) {
                    response += output;
                }
//            System.out.println("it.refill.otp.Sms.main() " + response);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                SmsReport sm = gson.fromJson(response, SmsReport.class);
                String ins = "INSERT INTO enm_report VALUES ('" + dtstart.toString("yyyy-MM-dd") + "','" + sm.getTotal() + "')";
                db2.getConnectionDB().createStatement().execute(ins);
//                System.out.println(datetd + " = " + sm.getTotal());
                conn.disconnect();
                dtstart = dtstart.plusDays(1);
            }

            db2.closeDB();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean sendSMS2022_MJ(String cell, String msg, Db_Bando db1) {
        try {
            String mailjet_secret = db1.getPath("mailjet_tk");
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            OkHttpClient customHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .build();
            ClientOptions options = ClientOptions.builder().bearerAccessToken(mailjet_secret)
                    .okHttpClient(customHttpClient)
                    .build();
            MailjetClient client = new MailjetClient(options);
            MailjetRequest request = new MailjetRequest(SmsSend.resource)
                    .property(SmsSend.FROM, db1.getPath("mailsender"))
                    .property(SmsSend.TO, cell)
                    .property(SmsSend.TEXT, msg);
            MailjetResponse response = client.post(request);

            System.out.println(response.getStatus());
            System.out.println(response.getData());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

}

class CreditSMS {

    String money, email;
    List<Smsvalue> sms;

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Smsvalue> getSms() {
        return sms;
    }

    public void setSms(List<Smsvalue> sms) {
        this.sms = sms;
    }

}

class SmsReport {

    String result;
    int pageNumber, pageSize, total;
    List<SmsHistory> smshistory = new ArrayList<>();

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<SmsHistory> getSmshistory() {
        return smshistory;
    }

    public void setSmshistory(List<SmsHistory> smshistory) {
        this.smshistory = smshistory;
    }

}

class SmsHistory {

    String order_id, create_time, schedule_time, message_type, sender, message;
    int num_recipients;

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getSchedule_time() {
        return schedule_time;
    }

    public void setSchedule_time(String schedule_time) {
        this.schedule_time = schedule_time;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getNum_recipients() {
        return num_recipients;
    }

    public void setNum_recipients(int num_recipients) {
        this.num_recipients = num_recipients;
    }

}

class Smsvalue {

    String type;
    int quantity;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}

class SendSMSRequest {

    /**
     * The message body
     */
    private String message;

    /**
     * The message type
     */
    private String message_type = MESSAGE_MEDIUM_QUALITY;

    /**
     * Should the API return the remaining credits?
     */
    private boolean returnCredits = false;

    /**
     * The list of recipients
     */
    private List<String> recipient = new ArrayList<>();

    /**
     * The sender Alias (TPOA)
     */
    private String sender = null;

    /**
     * Postpone the SMS message sending to the specified date
     */
    private Date scheduled_delivery_time = null;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return message_type;
    }

    public void setMessageType(String messageType) {
        this.message_type = messageType;
    }

    public boolean isReturnCredits() {
        return returnCredits;
    }

    public void setReturnCredits(boolean returnCredits) {
        this.returnCredits = returnCredits;
    }

    public List<String> getRecipient() {
        return recipient;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Date getScheduledDeliveryTime() {
        return scheduled_delivery_time;
    }

    public void setScheduledDeliveryTime(Date scheduled_delivery_time) {
        this.scheduled_delivery_time = scheduled_delivery_time;
    }

    public void addRecipient(String recipient) {
        this.recipient.add(recipient);
    }
}

/**
 * This class represents the API Response. It is automatically created starting
 * from the JSON object returned by the server, using GSon
 */
class SendSMSResponse {

    private String result;
    private String order_id;
    private int total_sent;
    private int remaining_credits;
    private String internal_order_id;

    public String getResult() {
        return result;
    }

    public String getOrderId() {
        return order_id;
    }

    public int getTotalSent() {
        return total_sent;
    }

    public int getRemainingCredits() {
        return remaining_credits;
    }

    public String getInternalOrderId() {
        return internal_order_id;
    }

    public boolean isValid() {
        return "OK".equals(result);
    }
}
