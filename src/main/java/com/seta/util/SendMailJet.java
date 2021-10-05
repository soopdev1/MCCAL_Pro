/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seta.util;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import static com.mailjet.client.resource.Emailv31.MESSAGES;
import static com.mailjet.client.resource.Emailv31.Message.BCC;
import static com.mailjet.client.resource.Emailv31.Message.CC;
import static com.mailjet.client.resource.Emailv31.Message.FROM;
import static com.mailjet.client.resource.Emailv31.Message.HTMLPART;
import static com.mailjet.client.resource.Emailv31.Message.SUBJECT;
import static com.mailjet.client.resource.Emailv31.Message.TO;
import static com.mailjet.client.resource.Emailv31.resource;
import com.seta.db.Entity;
import static com.seta.util.Utility.createDir;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author agodino
 */
public class SendMailJet {

    public static boolean sendMail(String name, String[] to, String txt, String subject) throws MailjetException {
        return sendMail(name, to, null, txt, subject, null);
    }

//    public static boolean sendMailNoDB(String name, String[] to, String txt, String subject, String mailjet_api, String mailjet_secret) throws MailjetException {
//        return sendMailNoDB(name, to, null, txt, subject, null, mailjet_api, mailjet_secret);
//    }
    public static boolean sendMail(String name, String[] to, String[] bcc, String txt, String subject) throws MailjetException {
        return sendMail(name, to, bcc, txt, subject, null);
    }

    public static boolean sendMail(String name, String[] to, String[] bcc, String txt, String subject, File file) throws MailjetException {
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;

        String filename = "";
        String content_type = "";
        String b64 = "";

        Entity e = new Entity();
        ClientOptions options = ClientOptions.builder()
                .apiKey(e.getPath("mailjet_api"))
                .apiSecretKey(e.getPath("mailjet_secret"))
                .build();

        e.close();
        client = new MailjetClient(options);
        JSONArray dest = new JSONArray();
        JSONArray ccn = new JSONArray();

        for (String s : to) {
            dest.put(new JSONObject().put("Email", s)
                    .put("Name", ""));
        }

        if (bcc != null) {
            for (String s : bcc) {
                ccn.put(new JSONObject().put("Email", s)
                        .put("Name", ""));
            }
        }

        JSONObject mail = new JSONObject().put(Emailv31.Message.FROM, new JSONObject()
                .put("Email", "yisucal.supporto@microcredito.gov.it")
                .put("Name", name))
                .put(Emailv31.Message.TO, dest)
                .put(Emailv31.Message.BCC, ccn)
                .put(Emailv31.Message.SUBJECT, subject)
                .put(Emailv31.Message.HTMLPART, txt);

        if (file != null) {
            try {
                filename = file.getName();
                content_type = Files.probeContentType(file.toPath());
                try (InputStream i = new FileInputStream(file)) {
                    b64 = new String(Base64.encodeBase64(IOUtils.toByteArray(i)));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            mail.put(Emailv31.Message.ATTACHMENTS, new JSONArray()
                    .put(new JSONObject()
                            .put("ContentType", content_type)
                            .put("Filename", filename)
                            .put("Base64Content", b64)));
        }

        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(mail));

        response = client.post(request);
        boolean ok = response.getStatus() == 200;
        if (!ok) {
            System.err.println("ERRORE: sendMail - " + response.getStatus() + " -- " + response.getRawResponseContent() + " --- " + response.getData().toList());
        }
        return ok;
    }

//    public static boolean sendMailNoDB(String name, String[] to, String[] bcc, String txt, String subject, File file, String mailjet_api, String mailjet_secret) 
//            throws MailjetException {
//        MailjetClient client;
//        MailjetRequest request;
//        MailjetResponse response;
//        String filename = "";
//        String content_type = "";
//        String b64 = "";
//
//        ClientOptions options = ClientOptions.builder()
//                .apiKey(mailjet_api)
//                .apiSecretKey(mailjet_secret)
//                .build();
//
//        client = new MailjetClient(options);
//
//        JSONArray dest = new JSONArray();
//        JSONArray ccn = new JSONArray();
//
//        for (String s : to) {
//            dest.put(new JSONObject().put("Email", s)
//                    .put("Name", ""));
//        }
//
//        if (bcc != null) {
//            for (String s : bcc) {
//                ccn.put(new JSONObject().put("Email", s)
//                        .put("Name", ""));
//            }
//        }
//
//        JSONObject mail = new JSONObject().put(Emailv31.Message.FROM, new JSONObject()
//                .put("Email", "yisucal.supporto@microcredito.gov.it")
//                .put("Name", name))
//                .put(Emailv31.Message.TO, dest)
//                .put(Emailv31.Message.BCC, ccn)
//                .put(Emailv31.Message.SUBJECT, subject)
//                .put(Emailv31.Message.HTMLPART, txt);
//
//        if (file != null) {
//            try {
//                filename = file.getName();
//                content_type = Files.probeContentType(file.toPath());
//                try (InputStream i = new FileInputStream(file)) {
//                    b64 = new String(Base64.encodeBase64(IOUtils.toByteArray(i)));
//                }
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//            mail.put(Emailv31.Message.ATTACHMENTS, new JSONArray()
//                    .put(new JSONObject()
//                            .put("ContentType", content_type)
//                            .put("Filename", filename)
//                            .put("Base64Content", b64)));
//        }
//
//        request = new MailjetRequest(Emailv31.resource)
//                .property(Emailv31.MESSAGES, new JSONArray()
//                        .put(mail));
//
//        response = client.post(request);
//
//        boolean ok = response.getStatus() == 200;
//
//        if (!ok) {
//            System.err.println("ERRORE: sendMail - " + response.getStatus() + " -- " + response.getRawResponseContent() + " --- " + response.getData().toList());
//        }
//
//        return ok;
//    }
    public static boolean sendMailEvento(String name, String[] to, String[] bcc, String txt, String subject, AttachMJ evento, String mailjet_api, String mailjet_secret) throws MailjetException {

        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;

        String mailjet_name = "yisucal.supporto@microcredito.gov.it";

        ClientOptions options = ClientOptions.builder()
                .apiKey(mailjet_api)
                .apiSecretKey(mailjet_secret)
                .build();

        client = new MailjetClient(options);
        JSONArray dest = new JSONArray();
        JSONArray ccn = new JSONArray();
        JSONArray ccj = new JSONArray();

        if (to != null) {
            for (String s : to) {
                dest.put(new JSONObject().put("Email", s)
                        .put("Name", ""));
            }
        }

        if (bcc != null) {
            for (String s : bcc) {
                ccn.put(new JSONObject().put("Email", s)
                        .put("Name", ""));
            }
        }

        JSONObject mail = new JSONObject().put(FROM, new JSONObject()
                .put("Email", mailjet_name)
                .put("Name", name))
                .put(TO, dest)
                .put(CC, ccj)
                .put(BCC, ccn)
                .put(SUBJECT, subject)
                .put(HTMLPART, txt);

        if (evento != null) {
            try {
                mail.put(Emailv31.Message.ATTACHMENTS, new JSONArray()
                        .put(new JSONObject()
                                .put("ContentType", evento.getContentType())
                                .put("Filename", evento.getFilename())
                                .put("Base64Content", evento.getBase64Content())));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        request = new MailjetRequest(resource)
                .property(MESSAGES, new JSONArray()
                        .put(mail));

        response = client.post(request);

        boolean ok = response.getStatus() == 200;

        if (!ok) {
            System.err.println("ERRORE: sendMail - " + response.getStatus() + " -- " + response.getRawResponseContent() + " --- " + response.getData().toList());
        }

        return ok;
    }

    private static String conversion2(String date) {
        try {
            String data = date.split(" ")[0];
            String anno = data.split("-")[0];
            String mese = data.split("-")[1];
            String giorno = data.split("-")[2];
            String ora = date.split(" ")[1];
            String ore = ora.split(":")[0];
            String min = ora.split(":")[1];
            String sec = ora.split(":")[2];
            return anno + mese + giorno + "T" + ore + min + sec;
        } catch (Exception e) {
        }
        return date;
    }

    public static AttachMJ createEVENT(String datainizioMYSQL, String datafineMYSQL, String eventName, String pathtemp) {
        try {
            org.joda.time.DateTime now = new org.joda.time.DateTime();
            String r1 = "BEGIN:VCALENDAR";
            String r2 = "PRODID:-//FL_EventsCalendar//iCal4j 2.0//IT";
            String r3 = "VERSION:2.0";
            String r4 = "CALSCALE:GREGORIAN";
            String r5 = "BEGIN:VEVENT";
            String r6 = "DTSTAMP:" + now.toString("yyyyMMdd") + "T" + now.toString("HHmmss") + "Z";
            String r7 = "DTSTART:" + conversion2(datainizioMYSQL);
            String r8 = "DTEND:" + conversion2(datafineMYSQL);
            String uuid = UUID.randomUUID().toString();
            String r9 = "SUMMARY:" + eventName;
            String r10 = "TZID:Europe/Rome";
            String r11 = "UID:" + uuid;
            String r12 = "END:VEVENT";
            String r13 = "END:VCALENDAR";
            createDir(pathtemp);
            File ics = new File(pathtemp + uuid + ".ics");

            try (FileWriter fw = new FileWriter(ics);
                    BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(r1);
                bw.newLine();
                bw.write(r2);
                bw.newLine();
                bw.write(r3);
                bw.newLine();
                bw.write(r4);
                bw.newLine();
                bw.write(r5);
                bw.newLine();
                bw.write(r6);
                bw.newLine();
                bw.write(r7);
                bw.newLine();
                bw.write(r8);
                bw.newLine();
                bw.write(r9);
                bw.newLine();
                bw.write(r10);
                bw.newLine();
                bw.write(r11);
                bw.newLine();
                bw.write(r12);
                bw.newLine();
                bw.write(r13);
                bw.newLine();
            }

            String Filename = "Event_MC.ics";
            String ContentType = "text/calendar";
            String Base64Content;
            Base64Content = new String(Base64.encodeBase64(FileUtils.readFileToByteArray(ics)));
            ics.delete();
            return new AttachMJ(ContentType, Filename, Base64Content);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
