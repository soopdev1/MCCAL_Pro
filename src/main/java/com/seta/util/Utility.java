/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seta.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.seta.domain.Allievi;
import com.seta.domain.DocumentiPrg;
import com.seta.domain.Documenti_Allievi;
import com.seta.domain.Documenti_Allievi_Pregresso;
import com.seta.domain.ProgettiFormativi;
import com.seta.domain.SoggettiAttuatori;
import com.seta.domain.StatiPrg;
import java.io.IOException;
import java.io.StringWriter;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Paths.get;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import static java.util.Locale.ITALY;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import static org.joda.time.format.DateTimeFormat.forPattern;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author RCOSCO
 */
public class Utility {
    
    
    
    public static boolean test = false;
    
    //ADD RAF
    public static boolean pregresso = true;
    
    
    
    
    public static final String patternSql = "yyyy-MM-dd";
    public static final String patternITA = "dd/MM/yyyy";    //END RAF
    public static final SimpleDateFormat sdmysql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void redirect(HttpServletRequest request, HttpServletResponse response, String destination) throws ServletException, IOException {
        if (response.isCommitted()) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(destination);
            dispatcher.forward(request, response);
        } else {
            response.sendRedirect(destination);
        }
    }

    public static void printRequest(HttpServletRequest request) {
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            for (String paramValue : paramValues) {
                System.out.println(paramName + " : " + new String(paramValue.getBytes(Charsets.ISO_8859_1), Charsets.UTF_8));
            }
        }
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            try {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                List items = upload.parseRequest((RequestContext) request);
                Iterator iterator = items.iterator();
                while (iterator.hasNext()) {
                    FileItem item = (FileItem) iterator.next();
                    if (item.isFormField()) {
                        String fieldName = item.getFieldName();
                        String value = new String(item.getString().getBytes(Charsets.ISO_8859_1), Charsets.UTF_8);
                        System.out.println("MULTIPART FIELD - " + fieldName + " : " + value);
                    } else {
                        String fieldName = item.getFieldName();
                        String fieldValue = item.getName();
                        System.out.println("MULTIPART FILE - " + fieldName + " : " + fieldValue);
                    }
                }
            } catch (FileUploadException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static String formatStringtoStringDate(String dat, String pattern1, String pattern2) {
        try {
            return new SimpleDateFormat(pattern2).format(new SimpleDateFormat(pattern1).parse(dat));
        } catch (ParseException e) {
        }
        return "No correct date";
    }

    public static List<Date> getListDates(Date startDate, Date endDate) {
        List<Date> datesInRange = new ArrayList<>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startDate);

        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(endDate);

        while (calendar.before(endCalendar) || calendar.equals(endCalendar)) {
            Date result = calendar.getTime();
            datesInRange.add(result);
            calendar.add(Calendar.DATE, 1);
        }
        return datesInRange;
    }

    public static String convMd5(String psw) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(psw.getBytes());
            byte byteData[] = md.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString().trim();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "-";
        }
    }

    public static String getRandomString(int length) {
        boolean useLetters = true;
        boolean useNumbers = false;
        return RandomStringUtils.random(length, useLetters, useNumbers);
    }

    public static String correctName(String ing) {
        ing = correggiusername(ing);
        return ing.replaceAll("-", "");
    }

    public static String[] stylePF(Map<String, String[]> map, ProgettiFormativi pf) {
        String[] values = map.get(pf.getStato().getTipo()) == null ? (new String[]{"<b style='color: #363a90;'>Stato progetto</b> : @stato", "color: #363a90;", "background-color: rgba(93, 120, 255, 0.07) !important;"})
                : map.get(pf.getStato().getTipo());
        return values;
    }

    public static Map<String, String[]> mapStyles() {
        Map<String, String[]> ms = new HashMap();
        ms.put("chiuso", new String[]{"<b style='color: #363a90;'>Stato progetto</b> : @stato<br>Procedere con l'invio della documentazione di chiusura<br><b>@giorni</b>", "color: #0abb87;", "background-color: rgba(10, 187, 135, 0.07) !important;"});
        ms.put("errore", new String[]{"<b style='color: #363a90;'>Stato progetto</b> : @stato<br>Progetto formativo in stato di errore", "color: #c30041;", "background-color: rgba(253, 57, 122, 0.07) !important;"});
        ms.put("controllare", new String[]{"<b style='color: #363a90;'>Stato progetto</b> : @stato<br>In attesa di verifica da parte del Microcredito", "color: #eaa21c;", "background-color: rgba(255, 184, 34, 0.07) !important;"});
        return ms;
    }

    public static String[] styleMicro(StatiPrg sp) {
        Map<String, String> ms = new HashMap();
        /*fa-times-circle fa-exclamation-circle fa-check-circle fa-clock*/
        ms.put("chiuso", "color: #0abb87;");
        ms.put("errore", "color: #c30041;");
        ms.put("controllare", "color: #eaa21c;");
        return new String[]{ms.get(sp.getTipo()) == null ? "color: #363a90;" : ms.get(sp.getTipo()), sp.getDescrizione().equalsIgnoreCase(sp.getDe_tipo()) ? sp.getDescrizione() : sp.getDescrizione() + " - " + sp.getDe_tipo()};
    }

    public static String correggiusername(String ing) {
        if (ing != null) {
            ing = ing.replaceAll("\\\\", "");
            ing = ing.replaceAll("\n", "");
            ing = ing.replaceAll("\r", "");
            ing = ing.replaceAll("\t", "");
            ing = ing.replaceAll("'", "");
            ing = ing.replaceAll("“", "");
            ing = ing.replaceAll("‘", "");
            ing = ing.replaceAll("”", "");
            ing = ing.replaceAll("\"", "");
            ing = ing.replaceAll(" ", "_");
            return ing.replaceAll("\"", "");
        } else {
            return "-";
        }
    }

    public static String correggi(String ing) {
        if (ing != null) {
            ing = ing.replaceAll("\\\\", "/");
            ing = ing.replaceAll("\n", "");
            ing = ing.replaceAll("\r", "");
            ing = ing.replaceAll("\t", "");
            ing = ing.replaceAll("'", "\'");
            ing = ing.replaceAll("“", "\'");
            ing = ing.replaceAll("‘", "\'");
            ing = ing.replaceAll("”", "\'");
            ing = ing.replaceAll("\"", "/");
            return ing.replaceAll("\"", "\'");
        } else {
            return "-";
        }
    }

    public static String CaratteriSpeciali(String ing) {
        if (ing != null) {
            ing = StringUtils.replace(ing, "Ã©", "è");
            ing = StringUtils.replace(ing, "Ã¨", "é");
            ing = StringUtils.replace(ing, "Ã¬", "ì");
            ing = StringUtils.replace(ing, "Ã²", "ò");
            ing = StringUtils.replace(ing, "Ã¹", "ù");
            ing = StringUtils.replace(ing, "Ã", "à");
            return ing;
        } else {
            return "-";
        }
    }

    public static String ctrlCheckbox(String check) {
        return check == null ? "NO" : "SI";
    }

    public static String UniqueUser(Map<String, String> usernames, String username) {
        Random rand = new Random();
        boolean ok = usernames.get(username) == null;
        while (!ok) {
            username += String.valueOf(rand.nextInt(10));
            ok = usernames.get(username) == null;
        }
        return username;
    }

    public static Map<StatiPrg, Long> GroupByAndCount(SoggettiAttuatori s) {
        return s.getProgettiformativi().stream().collect(Collectors.groupingBy(ProgettiFormativi::getStato, Collectors.counting()));
    }

    public static void sortDoc(List<DocumentiPrg> documeti) {
        documeti.sort((p1, p2) -> p1.getTipo().getId().compareTo(p2.getTipo().getId()));
    }

    public static void sortDoc_Pregresso(List<Documenti_Allievi_Pregresso> documeti) {
        documeti.sort((p1, p2) -> p1.getTipo().getId().compareTo(p2.getTipo().getId()));
    }

    /**
     * CONVERTE LA DATA IN FORMATO UTILIZZABILE IN JAVA UTIL A PARTIRE DALLA
     * DATA IN INGRESSO
     *
     * @param dat - DATA IN INGRESSO
     * @param pattern1 - PATTERN DATA
     * @return Date
     */
    public static Date getUtilDate(String dat, String pattern1) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(pattern1);
            return formatter.parse(dat);
        } catch (ParseException ex) {
            System.err.println("METHOD: " + new Object() {
            }
                    .getClass()
                    .getEnclosingMethod()
                    .getName());
            System.err.println("ERROR: " + ExceptionUtils.getStackTrace(ex));
        }
        return null;
    }

    public static void writeJsonResponseR(HttpServletResponse response, Object list) {
        try {
            JsonObject jMembers = new JsonObject();
            StringWriter sw = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(sw, list);
            String json_s = sw.toString();
            JsonParser parser = new JsonParser();
            JsonElement tradeElement = parser.parse(json_s);
            jMembers.add("aaData", tradeElement.getAsJsonArray());
            response.setContentType("application/json");
            response.setHeader("Content-Type", "application/json");
            response.getWriter().print(jMembers.toString());
            response.getWriter().close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void setOreLezioni(Allievi a) {
        if (a.getProgetto() != null) {
            //ore Fase A
            for (DocumentiPrg d : a.getProgetto().getDocumenti().stream().filter(doc -> doc.getGiorno() != null && doc.getDeleted() == 0).collect(Collectors.toList())) {
                d.getPresenti_list().stream().filter((p) -> (a.getId() == p.getId())).forEachOrdered((p) -> {
                    a.setOre_fa(a.getOre_fa() + p.getOre_riconosciute());
                });
            }
            //ore Fase B
            for (Documenti_Allievi d : a.getDocumenti().stream().filter(doc -> doc.getGiorno() != null && doc.getDeleted() == 0).collect(Collectors.toList())) {
                a.setOre_fb(a.getOre_fb() + (d.getOrericonosciute() == null ? 0 : d.getOrericonosciute()));
            }
        }
    }

    public static void createDir(String path) {
        try {
            createDirectories(get(path));
        } catch (Exception e) {
        }
    }
    public static String formatStringtoStringDate(String dat, String pattern1, String pattern2, boolean timestamp) {
        try {
            if (timestamp) {
                dat = dat.substring(0, dat.length() - 2);
            }
            if (dat.length() == pattern1.length()) {
                DateTimeFormatter fmt = forPattern(pattern1);
                DateTime dtout = fmt.parseDateTime(dat);
                return dtout.toString(pattern2, ITALY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "DATA ERRATA";
    }
}
