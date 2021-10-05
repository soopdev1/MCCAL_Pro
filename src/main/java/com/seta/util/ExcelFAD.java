/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seta.util;

import com.google.common.base.Splitter;
import com.seta.db.Database;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author rcosco
 */
public class ExcelFAD {

    private static final XSSFColor col1 = new XSSFColor(new java.awt.Color(146, 208, 80), null);
    private static final XSSFColor col2 = new XSSFColor(new java.awt.Color(204, 236, 255), null);
    private static final XSSFColor col3 = new XSSFColor(new java.awt.Color(230, 230, 230), null);
    private static final String pattern0 = "yyyy-MM-dd HH:mm:ss";
    private static final String pattern1 = "yyyy-MM-dd HH:mm";
    private static final String pattern2 = "yyyy-MM-dd";
    private static final String pattern3 = "HH:mm:ss";
    private static final String pattern4 = "HH:mm";
    private static final String pattern5 = "dd/MM/yyyy";
    private static final SimpleDateFormat sd0 = new SimpleDateFormat(pattern0);
    private static final DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern2);

    private static void setBordersToMergedCells(XSSFSheet sheet) {
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        mergedRegions.stream().map((rangeAddress) -> {
            RegionUtil.setBorderTop(BorderStyle.THIN, rangeAddress, sheet);
            return rangeAddress;
        }).map((rangeAddress) -> {
            RegionUtil.setBorderLeft(BorderStyle.THIN, rangeAddress, sheet);
            return rangeAddress;
        }).map((rangeAddress) -> {
            RegionUtil.setBorderRight(BorderStyle.THIN, rangeAddress, sheet);
            return rangeAddress;
        }).forEachOrdered((rangeAddress) -> {
            RegionUtil.setBorderBottom(BorderStyle.THIN, rangeAddress, sheet);
        });
    }

    private static XSSFRow get(XSSFSheet sh, int index) {
        try {
            XSSFRow row = sh.getRow(index);
            if (row == null) {
                row = sh.createRow(index);
            }
            return row;
        } catch (Exception ex) {
            return sh.createRow(index);
        }
    }

    private static XSSFCell get(XSSFRow row, int index) {
        try {
            XSSFCell cell = row.getCell(index);
            if (cell == null) {
                cell = row.createCell(index);

            }
            return cell;
        } catch (Exception ex) {
            return row.createCell(index);
        }
    }

    private static List<Items> list_Allievi(String idpr) {
        List<Items> out = new ArrayList<>();
        try {
            Database db2 = new Database();
            Statement st2 = db2.getC().createStatement();
            try {
                ResultSet rs2 = st2.executeQuery("SELECT idallievi,nome,cognome,codicefiscale FROM allievi f WHERE f.idprogetti_formativi='" + idpr + "'");
                while (rs2.next()) {
                    Items al = new Items(rs2.getString(1), (rs2.getString(2) + " " + rs2.getString(3)).toUpperCase());
                    al.setNome(rs2.getString(2).toUpperCase());
                    al.setCognome(rs2.getString(3).toUpperCase());
                    al.setCf(rs2.getString(4).toUpperCase());
                    out.add(al);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            st2.close();
            db2.closeDB();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    private static List<Items> list_Docenti(String idpr) {
        List<Items> out = new ArrayList<>();
        try {
            Database db2 = new Database();
            Statement st2 = db2.getC().createStatement();
            try {
                ResultSet rs2 = st2.executeQuery("SELECT iddocenti,nome,cognome FROM docenti f WHERE f.iddocenti IN (SELECT p.iddocenti FROM progetti_docenti p WHERE p.idprogetti_formativi='" + idpr + "')");
                while (rs2.next()) {
                    out.add(new Items(rs2.getString(1), (rs2.getString(2) + " " + rs2.getString(3)).toUpperCase()));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            st2.close();
            db2.closeDB();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    private static List<Items> formatAction() {
        List<Items> out = new ArrayList<>();
        out.add(new Items("L1", "Login"));
        out.add(new Items("L2", "Logout"));
        out.add(new Items("L3", "Logout"));
        out.add(new Items("L3", "Logout"));
        out.add(new Items("L4", "Logout"));
        out.add(new Items("L5", "Chiusura stanza"));
        out.add(new Items("IN", "Info"));
        return out;
    }

    private static String calcoladurata(long millis) {
        if (millis < 0) {
            return "Dati non congrui per calcolare il tempo di permanenza.";
        }
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        StringBuilder sb = new StringBuilder(64);
        sb.append(hours);
        sb.append("h ");
        sb.append(minutes);
        sb.append("min ");
        sb.append(seconds);
        sb.append("sec");
        return sb.toString();
    }

    private static String convertTS_Italy(Timestamp ts1) {
        LocalDateTime ldt = ts1.toLocalDateTime();
        Date d1 = Date.from(ldt.atZone(ZoneOffset.UTC).toInstant());
        return sd0.format(d1);
    }

    private static DateTime format(String ing, String pattern) {
        try {
            if (ing.contains(".")) {
                ing = ing.split("\\.")[0];
            }
            return DateTimeFormat.forPattern(pattern).parseDateTime(ing);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static long calcolaDurataLezione(String date, List<Items> calendar) {
        try {
            Items it = calendar.stream().filter(day -> day.getData().equals(date)).findAny().get();

            DateTime start = DateTimeFormat.forPattern(pattern1).parseDateTime(it.getData() + " " + it.getOrainizio());
            DateTime end = DateTimeFormat.forPattern(pattern1).parseDateTime(it.getData() + " " + it.getOrafine());

            return end.getMillis() - start.getMillis();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private static String checkCalendar(String date, List<Items> calendar) {
        StringBuilder newdate = new StringBuilder();
        try {
            String solodata = date.split(" ")[0];
            String soloora = date.split(" ")[1];
            DateTime orario = DateTimeFormat.forPattern(pattern3).parseDateTime(soloora);

            Iterator<Items> cal = calendar.iterator();
            while (cal.hasNext()) {
                Items lezione = cal.next();
                if (lezione.getData().equals(solodata)) {
                    DateTime start = DateTimeFormat.forPattern(pattern4).parseDateTime(lezione.getOrainizio());
                    DateTime end = DateTimeFormat.forPattern(pattern4).parseDateTime(lezione.getOrafine());
                    boolean compreso = !orario.isBefore(start) && !orario.isAfter(end);
                    if (!compreso) {
                        boolean prima = orario.isBefore(start);
                        boolean dopo = orario.isAfter(end);
                        if (prima) {
                            newdate.append(solodata).append(" ").append(lezione.getOrainizio()).append(":00");
                            break;
                        }
                        if (dopo) {
                            newdate.append(solodata).append(" ").append(lezione.getOrafine()).append(":00");
                            break;
                        }
                    }

                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (newdate.toString().trim().equals("")) {
            return date;
        } else {
            return newdate.toString();
        }
    }

    private static long arrotonda(long durata) {
        try {
            long r = durata % (15 * 60 * 1000);
            durata -= r;
            durata += 15 * 60 * 1000;
        } catch (Exception ex) {
        }
        return durata;
    }

    
    public static String generatereportFAD_multistanza(String idpr) {
        try {

            List<Items> elencostanze = new ArrayList<>();
            Database db0 = new Database();
            String nome = db0.getNomePR_F(idpr);
            String pathtemp = db0.getPathtemp("pathTemp");
            //ELENCO STANZE
            String sql0 = "SELECT nomestanza,numerocorso FROM fad_multi WHERE idprogetti_formativi=" + idpr;
            Statement st0 = db0.getC().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs0 = st0.executeQuery(sql0);
            while (rs0.next()) {
                elencostanze.add(new Items(rs0.getString(1), rs0.getString(2)));
            }
            db0.closeDB();
            List<Items> docenti = list_Docenti(idpr);
            List<Items> allievi = list_Allievi(idpr);
            AtomicInteger ok = new AtomicInteger(0);
            if (!elencostanze.isEmpty()) {
                XSSFWorkbook workbook = new XSSFWorkbook();
                XSSFFont headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setFontHeightInPoints((short) 15);
                XSSFCellStyle headerCellStyle = workbook.createCellStyle();
                headerCellStyle.setFont(headerFont);
                headerCellStyle.setBorderBottom(BorderStyle.THIN);
                headerCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                headerCellStyle.setBorderLeft(BorderStyle.THIN);
                headerCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                headerCellStyle.setBorderRight(BorderStyle.THIN);
                headerCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
                headerCellStyle.setBorderTop(BorderStyle.THIN);
                headerCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
                headerCellStyle.setFillForegroundColor(col3);
                headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                XSSFFont normFont = workbook.createFont();
                normFont.setFontHeightInPoints((short) 12);

                elencostanze.forEach(room -> {
                    try {

                        String roomname = room.getId();
                        String numerocorso = room.getDescr();

                        List<Items> calendar = new ArrayList<>();
                        List<Hours> times_A = new ArrayList<>();
                        List<Hours> times_B = new ArrayList<>();
                        List<Track> tracking = new ArrayList<>();
                        List<Items> idutenti = new ArrayList<>();
                        List<Items> azioni = formatAction();

                        Database db1 = new Database();
                        Statement stc = db1.getC().createStatement();
                        ResultSet rsc = stc.executeQuery("SELECT data,orainizio,orafine FROM fad_calendar f WHERE f.idprogetti_formativi = " + idpr + " AND f.numerocorso='" + numerocorso + "' ORDER BY data");
                        while (rsc.next()) {
                            String inizio = rsc.getString(2);
                            String fine = rsc.getString(3);
                            if (inizio.contains(";") || fine.contains(";")) {
                                List<String> orario_inizio = Splitter.on(";").splitToList(inizio);
                                List<String> orario_fine = Splitter.on(";").splitToList(fine);
                                if (orario_inizio.size() == orario_fine.size()) {
                                    for (int i = 0; i < orario_inizio.size(); i++) {
                                        Items lezione = new Items();
                                        lezione.setData(rsc.getString(1));
                                        lezione.setOrainizio(orario_inizio.get(i));
                                        lezione.setOrafine(orario_fine.get(i));
                                        calendar.add(lezione);
                                    }
                                }
                            } else {
                                Items lezione = new Items();
                                lezione.setData(rsc.getString(1));
                                lezione.setOrainizio(inizio);
                                lezione.setOrafine(fine);
                                calendar.add(lezione);
                            }
                        }
                        rsc.close();
                        stc.close();

                        List<String> giornidilezione = calendar.stream().map(le -> le.getData()).collect(Collectors.toList());

                        Statement st1 = db1.getC().createStatement();
                        ResultSet rs1 = st1.executeQuery("SELECT DISTINCT(LEFT(DATE,10)) FROM fad_track f WHERE f.room = '" + roomname + "'");
                        Statement st02 = db1.getC().createStatement();
                        ResultSet rs02 = st02.executeQuery("SELECT p.start,p.end_fa,p.start_fb,p.end_fb FROM progetti_formativi p WHERE p.idprogetti_formativi = " + idpr);
                        if (rs02.next()) {
                            List<String> date_A = new ArrayList<>();
                            List<String> date_B = new ArrayList<>();
                            String inizioA = rs02.getString(1);
                            String fineA = rs02.getString(2);
                            String inizioB = rs02.getString(3);
                            String fineB = rs02.getString(4);
                            while (rs1.next()) {
                                if (fineA == null || inizioB == null) {
                                    date_A.add(rs1.getString(1));
                                } else {
                                    DateTime check = dtf.parseDateTime(rs1.getString(1));
                                    DateTime in_A = dtf.parseDateTime(inizioA);
                                    DateTime fi_A = dtf.parseDateTime(fineA);
                                    if (check.isEqual(in_A) || check.isEqual(fi_A) || (check.isAfter(in_A) && check.isBefore(fi_A))) {
                                        date_A.add(rs1.getString(1));
                                        continue;
                                    }
                                    DateTime in_B = dtf.parseDateTime(inizioB);
                                    if (check.isEqual(in_B) || check.isAfter(in_B)) {
                                        date_B.add(rs1.getString(1));
                                        continue;
                                    }
                                    if (fineB != null) {
                                        DateTime fi_B = dtf.parseDateTime(fineB);
                                        if (check.isEqual(fi_B) || check.isBefore(fi_B)) {
                                            date_B.add(rs1.getString(1));
                                        }
                                    }
                                }
                            }
                            rs1.close();
                            rs02.close();
                            st1.close();
                            st02.close();
                            db1.closeDB();
                            Iterator<String> it_a = date_A.iterator();
                            while (it_a.hasNext()) {
                                String day = it_a.next(); // must be called before you can call i.remove()
                                if (!giornidilezione.contains(day)) {
                                    it_a.remove();
                                }
                            }
                            Iterator<String> it_b = date_B.iterator();
                            while (it_b.hasNext()) {
                                String day = it_b.next(); // must be called before you can call i.remove()
                                if (!giornidilezione.contains(day)) {
                                    it_b.remove();
                                }
                            }

                            if (!date_A.isEmpty()) {
                                Database db2 = new Database();
                                Statement st2 = db2.getC().createStatement();
                                StringBuilder add1 = new StringBuilder("");
                                date_A.forEach(day -> {
                                    try {
                                        ResultSet rs2 = st2.executeQuery("SELECT * FROM fad_track f WHERE f.room = '" + roomname + "' AND f.date LIKE '" + day + "%' ORDER BY f.date");
                                        while (rs2.next()) {
                                            String tipoazione = rs2.getString(2);
                                            String azione = rs2.getString(4);
                                            String date = convertTS_Italy(rs2.getTimestamp(5));
                                            if (tipoazione.equals("L1") || tipoazione.equals("L2") || tipoazione.equals("L3")) {
                                                if (azione.startsWith("ALLIEVO")) {
                                                    try {
                                                        String id = azione.split(";")[1];
                                                        azione = azioni.stream().filter(az -> az.getId().equalsIgnoreCase(tipoazione)).findFirst().get().getDescr() + " -> "
                                                                + allievi.stream().filter(al -> al.getId().equals(id)).findFirst().get().getDescr();
                                                        tracking.add(new Track("", tipoazione, azione, date, day));
                                                    } catch (NoSuchElementException ex) {
                                                    }
                                                } else if (azione.startsWith("DOCENTE")) {
                                                    try {
                                                        String id = azione.split(";")[1];
                                                        azione = azioni.stream().filter(az -> az.getId().equalsIgnoreCase(tipoazione)).findFirst().get().getDescr() + " -> "
                                                                + docenti.stream().filter(al -> al.getId().equals(id)).findFirst().get().getDescr();
                                                        tracking.add(new Track("DOCENTE", tipoazione, azione, date, day));
                                                    } catch (NoSuchElementException ex) {
                                                    }
                                                } else {
                                                    continue;
                                                }
                                            } else if (tipoazione.equals("L4")) {
                                                try {
                                                    String idfad = StringUtils.remove(azione, "USCITA PARTECIPANTE -> ").trim();
                                                    String nomecogn = idutenti.stream().filter(ut -> ut.getId().equals(idfad)).findFirst().get().getDescr();
                                                    azione = "Logout -> " + nomecogn;
                                                    tracking.add(new Track("", tipoazione, azione, date, day));
                                                } catch (NoSuchElementException ex) {
                                                }
                                            }
                                            if (tipoazione.equals("IN")) {
                                                if (azione.startsWith("UTENTE LOGGATO CON ID")) {
                                                    String nomecogn = azione.split("--")[1].trim();
                                                    String idfad = StringUtils.remove(azione.split("--")[0], "UTENTE LOGGATO CON ID").trim();
                                                    idutenti.add(new Items(idfad, nomecogn));
                                                    continue;
                                                } else if (azione.startsWith("PARTECIPANTI") || azione.startsWith("NOME CAMBIATO")) {
                                                    continue;
                                                } else if (azione.startsWith("NUOVO PARTECIPANTE")) {
                                                    String nomecogn = azione.split("--")[1].trim();
                                                    String idfad = StringUtils.remove(azione.split("--")[0], "NUOVO PARTECIPANTE ->").trim();
                                                    idutenti.add(new Items(idfad, nomecogn));
                                                    continue;
                                                }
                                            }
                                            tracking.add(new Track("", tipoazione, azione, date, day));
                                        }
                                    } catch (SQLException ex) {
                                        System.out.println("ERROR: " + ex.getMessage());
                                    }
                                });
                                st2.close();
                                db2.closeDB();
                                List<Track> finaltr = tracking.stream().distinct().collect(Collectors.toList());
                                AtomicInteger index = new AtomicInteger(1);
                                date_A.forEach(day -> {
                                    String giorno = dtf.parseDateTime(day).toString("dd/MM/yyyy");
                                    List<Presenti> presenti = new ArrayList<>();
                                    List<Track> daytr = finaltr.stream().filter(d -> d.getDay().equals(day)).collect(Collectors.toList());
                                    daytr.forEach(tr1 -> {
                                        boolean content = allievi.stream().anyMatch(al -> tr1.getDescr().contains(al.getDescr()));
                                        if (content) {
                                            Items a = allievi.stream().filter(al -> tr1.getDescr().contains(al.getDescr())).findAny().get();
                                            Presenti pr1 = new Presenti(a.getNome(), a.getCognome(), a.getCf());
                                            pr1.setDate(checkCalendar(tr1.getDate(), calendar));
                                            if (tr1.getDescr().contains("Login")) {
                                                pr1.setLogin(true);
                                            } else if (tr1.getDescr().contains("Logout")) {
                                                pr1.setLogout(true);
                                            }
                                            presenti.add(pr1);
                                        }
                                        
                                        if (tr1.getUsertype().equals("DOCENTE")) {
                                            if (tr1.getDescr().contains("Login")) {
                                                if (!room.getNome().trim().contains(StringUtils.remove(tr1.getDescr(), "Login -> ").trim())) {
                                                    if (room.getNome().trim().equals("")) {
                                                        room.setNome(StringUtils.remove(tr1.getDescr(), "Login -> ").trim());
                                                    } else {
                                                        String nuovonome = room.getNome().trim() + ";" + StringUtils.remove(tr1.getDescr(), "Login -> ").trim();
                                                        room.setNome(nuovonome);
                                                    }
                                                }
                                            }
                                        }
                                    });

                                    List<String> dist_cf = presenti.stream().map(cf -> cf.getCf()).distinct().collect(Collectors.toList());
                                    dist_cf.forEach(tr1 -> {
                                        Presenti selected = presenti.stream().filter(cf -> cf.getCf().equals(tr1)).findFirst().get();

                                        List<Presenti> userp = presenti.stream().filter(d -> d.getCf().equals(tr1)).distinct().collect(Collectors.toList());
                                        List<Presenti> login = userp.stream().filter(d -> d.isLogin()).distinct().collect(Collectors.toList());
                                        List<Presenti> logout = userp.stream().filter(d -> d.isLogout()).distinct().collect(Collectors.toList());
                                        if (!login.isEmpty() && !logout.isEmpty()) {

                                            StringBuilder loginvalue = new StringBuilder();
                                            StringBuilder logoutvalue = new StringBuilder();

                                            LinkedList<Presenti> userp_final = new LinkedList<>();
                                            AtomicInteger ind = new AtomicInteger(0);
                                            userp.forEach(ba1 -> {
                                                if (ind.get() == 0) {
                                                    if (ba1.isLogin()) {
                                                        userp_final.add(ba1);
                                                    }
                                                } else {
                                                    if (userp_final.isEmpty()) {
                                                        userp_final.add(ba1);
                                                    } else {
                                                        if (userp.get(ind.get() - 1).isLogin() && !ba1.isLogin()) {
                                                            userp_final.add(ba1);
                                                        } else if (userp.get(ind.get() - 1).isLogout() && !ba1.isLogout()) {
                                                            userp_final.add(ba1);
                                                        } else if (userp.get(ind.get() - 1).isLogout() && ba1.isLogout()) {
                                                            userp_final.removeLast();
                                                            userp_final.add(ba1);
                                                        }
                                                    }
                                                }
                                                ind.addAndGet(1);
                                            });

                                            if ((userp_final.size() % 2) != 0) {
                                                userp_final.removeLast();
                                            }

                                            AtomicLong millis = new AtomicLong(0);
                                            userp_final.forEach(ba1 -> {
                                                if (ba1.isLogin()) {
                                                    millis.addAndGet(-format(ba1.getDate().split("\\.")[0], pattern0).getMillis());
                                                    loginvalue.append(ba1.getDate().split(" ")[1].split("\\.")[0]).append("\n");
                                                } else if (ba1.isLogout()) {
                                                    millis.addAndGet(format(ba1.getDate().split("\\.")[0], pattern0).getMillis());
                                                    logoutvalue.append(ba1.getDate().split(" ")[1].split("\\.")[0]).append("\n");
                                                }
                                            });

                                            XSSFColor c1;
                                            if ((index.get() % 2) == 0) {
                                                c1 = col1;
                                            } else {
                                                c1 = col2;
                                            }

                                            long duratalogin = arrotonda(millis.get());
                                            long maxlezione = calcolaDurataLezione(day, calendar);
                                            if (duratalogin > maxlezione) {
                                                duratalogin = maxlezione;
                                            }
                                            String duratacollegamento = calcoladurata(duratalogin);
                                            Hours time = new Hours(giorno, selected.getNome().toUpperCase(), selected.getCognome().toUpperCase(), tr1, loginvalue.toString().trim(),
                                                    logoutvalue.toString().trim(), duratacollegamento, c1);
                                            time.setDocente(room.getNome());
                                            times_A.add(time);
                                            add1.append("OK");
                                        }
                                    });
                                    if (!add1.toString().equals("")) {
                                        index.addAndGet(1);
                                    }
                                    add1.setLength(0);
                                });
                            }

                            if (!date_B.isEmpty()) {
                                Database db2 = new Database();
                                Statement st2 = db2.getC().createStatement();
                                StringBuilder add1 = new StringBuilder("");
                                date_B.forEach(day -> {
                                    try {
                                        ResultSet rs2 = st2.executeQuery("SELECT * FROM fad_track f WHERE f.room = '" + roomname + "' AND f.date LIKE '" + day + "%' ORDER BY f.date");
                                        while (rs2.next()) {
                                            String tipoazione = rs2.getString(2);
                                            String azione = rs2.getString(4);
                                            String date = convertTS_Italy(rs2.getTimestamp(5));
                                            if (tipoazione.equals("L1") || tipoazione.equals("L2") || tipoazione.equals("L3")) {
                                                if (azione.startsWith("ALLIEVO")) {
                                                    try {
                                                        String id = azione.split(";")[1];
                                                        azione = azioni.stream().filter(az -> az.getId().equalsIgnoreCase(tipoazione)).findFirst().get().getDescr() + " -> "
                                                                + allievi.stream().filter(al -> al.getId().equals(id)).findFirst().get().getDescr();

                                                        tracking.add(new Track("", tipoazione, azione, date, day));
                                                    } catch (NoSuchElementException ex) {

                                                    }
                                                } else if (azione.startsWith("DOCENTE")) {
                                                    try {
                                                        String id = azione.split(";")[1];
                                                        azione = azioni.stream().filter(az -> az.getId().equalsIgnoreCase(tipoazione)).findFirst().get().getDescr() + " -> "
                                                                + docenti.stream().filter(al -> al.getId().equals(id)).findFirst().get().getDescr();
                                                        tracking.add(new Track("DOCENTE", tipoazione, azione, date, day));
                                                    } catch (NoSuchElementException ex) {

                                                    }
                                                } else {
                                                    continue;
                                                }
                                            } else if (tipoazione.equals("L4")) {
                                                try {
                                                    String idfad = StringUtils.remove(azione, "USCITA PARTECIPANTE -> ").trim();
                                                    String nomecogn = idutenti.stream().filter(ut -> ut.getId().equals(idfad)).findFirst().get().getDescr();
                                                    azione = "Logout -> " + nomecogn;
                                                    tracking.add(new Track("", tipoazione, azione, date, day));
                                                } catch (NoSuchElementException ex) {

                                                }
                                            }
                                            if (tipoazione.equals("IN")) {
                                                if (azione.startsWith("UTENTE LOGGATO CON ID")) {
                                                    String nomecogn = azione.split("--")[1].trim();
                                                    String idfad = StringUtils.remove(azione.split("--")[0], "UTENTE LOGGATO CON ID").trim();
                                                    idutenti.add(new Items(idfad, nomecogn));
                                                    continue;
                                                } else if (azione.startsWith("PARTECIPANTI") || azione.startsWith("NOME CAMBIATO")) {
                                                    continue;
                                                } else if (azione.startsWith("NUOVO PARTECIPANTE")) {
                                                    String nomecogn = azione.split("--")[1].trim();
                                                    String idfad = StringUtils.remove(azione.split("--")[0], "NUOVO PARTECIPANTE ->").trim();
                                                    idutenti.add(new Items(idfad, nomecogn));
                                                    continue;
                                                }
                                            }
                                            tracking.add(new Track("", tipoazione, azione, date, day));
                                        }
                                    } catch (SQLException ex) {
                                        System.out.println("ERROR: " + ex.getMessage());
                                    }
                                });
                                st2.close();
                                db2.closeDB();
                                List<Track> finaltr = tracking.stream().distinct().collect(Collectors.toList());
                                AtomicInteger index = new AtomicInteger(1);
                                date_B.forEach(day -> {
                                    String giorno = dtf.parseDateTime(day).toString("dd/MM/yyyy");
                                    List<Presenti> presenti = new ArrayList<>();
                                    List<Track> daytr = finaltr.stream().filter(d -> d.getDay().equals(day)).collect(Collectors.toList());
                                    daytr.forEach(tr1 -> {
                                        boolean content = allievi.stream().anyMatch(al -> tr1.getDescr().contains(al.getDescr()));
                                        if (content) {
                                            Items a = allievi.stream().filter(al -> tr1.getDescr().contains(al.getDescr())).findAny().get();
                                            Presenti pr1 = new Presenti(a.getNome(), a.getCognome(), a.getCf());
                                            pr1.setDate(checkCalendar(tr1.getDate(), calendar));
                                            if (tr1.getDescr().contains("Login")) {
                                                pr1.setLogin(true);
                                            } else if (tr1.getDescr().contains("Logout")) {
                                                pr1.setLogout(true);
                                            }
                                            presenti.add(pr1);
                                        }
                                        if (tr1.getUsertype().equals("DOCENTE")) {
                                            if (tr1.getDescr().contains("Login")) {
                                                if (!room.getNome().trim().contains(StringUtils.remove(tr1.getDescr(), "Login -> ").trim())) {
                                                    if (room.getNome().trim().equals("")) {
                                                        room.setNome(StringUtils.remove(tr1.getDescr(), "Login -> ").trim());
                                                    } else {
                                                        String nuovonome = room.getNome().trim() + ";" + StringUtils.remove(tr1.getDescr(), "Login -> ").trim();
                                                        room.setNome(nuovonome);
                                                    }
                                                }
                                            }
                                        }
                                    });
                                    List<String> dist_cf = presenti.stream().map(cf -> cf.getCf()).distinct().collect(Collectors.toList());
                                    dist_cf.forEach(tr1 -> {
                                        Presenti selected = presenti.stream().filter(cf -> cf.getCf().equals(tr1)).findFirst().get();
                                        List<Presenti> userp = presenti.stream().filter(d -> d.getCf().equals(tr1)).distinct().collect(Collectors.toList());
                                        List<Presenti> login = userp.stream().filter(d -> d.isLogin()).distinct().collect(Collectors.toList());
                                        List<Presenti> logout = userp.stream().filter(d -> d.isLogout()).distinct().collect(Collectors.toList());
                                        if (!login.isEmpty() && !logout.isEmpty()) {
                                            StringBuilder loginvalue = new StringBuilder();
                                            StringBuilder logoutvalue = new StringBuilder();
                                            if (login.size() != logout.size()) {

                                                LinkedList<Presenti> userp_final = new LinkedList<>();
                                                AtomicInteger ind = new AtomicInteger(0);
                                                userp.forEach(ba1 -> {
                                                    if (ind.get() == 0) {
                                                        if (ba1.isLogin()) {
                                                            userp_final.add(ba1);
                                                        }
                                                    } else {
                                                        if (userp_final.isEmpty()) {
                                                            userp_final.add(ba1);
                                                        } else {
                                                            if (userp.get(ind.get() - 1).isLogin() && !ba1.isLogin()) {
                                                                userp_final.add(ba1);
                                                            } else if (userp.get(ind.get() - 1).isLogout() && !ba1.isLogout()) {
                                                                userp_final.add(ba1);
                                                            } else if (userp.get(ind.get() - 1).isLogout() && ba1.isLogout()) {
                                                                userp_final.removeLast();
                                                                userp_final.add(ba1);
                                                            }
                                                        }
                                                    }
                                                    ind.addAndGet(1);
                                                });
                                                if ((userp_final.size() % 2) != 0) {
                                                    userp_final.removeLast();
                                                }
                                                AtomicLong millis = new AtomicLong(0);
                                                userp_final.forEach(ba1 -> {
                                                    if (ba1.isLogin()) {
                                                        millis.addAndGet(-format(ba1.getDate().split("\\.")[0], pattern0).getMillis());
                                                        loginvalue.append(ba1.getDate().split(" ")[1].split("\\.")[0]).append("\n");
                                                    } else if (ba1.isLogout()) {
                                                        millis.addAndGet(format(ba1.getDate().split("\\.")[0], pattern0).getMillis());
                                                        logoutvalue.append(ba1.getDate().split(" ")[1].split("\\.")[0]).append("\n");
                                                    }
                                                });
                                                XSSFColor c1;
                                                if ((index.get() % 2) == 0) {
                                                    c1 = col1;
                                                } else {
                                                    c1 = col2;
                                                }
                                                long duratalogin = arrotonda(millis.get());
                                                long maxlezione = calcolaDurataLezione(day, calendar);
                                                if (duratalogin > maxlezione) {
                                                    duratalogin = maxlezione;
                                                }
                                                String duratacollegamento = calcoladurata(duratalogin);

                                                Hours time = new Hours(giorno, selected.getNome().toUpperCase(), selected.getCognome().toUpperCase(), tr1,
                                                        loginvalue.toString().trim(),
                                                        logoutvalue.toString().trim(), duratacollegamento, c1);
                                                time.setDocente(room.getNome());
                                                times_B.add(time);
                                                add1.append("OK");
                                            }
                                        }
                                    });
                                    if (!add1.toString().equals("")) {
                                        index.addAndGet(1);
                                    }
                                    add1.setLength(0);
                                });
                            }
                            XSSFSheet sheet = workbook.createSheet("FAD Report - Stanza " + roomname);
                            XSSFRow row = get(sheet, 0);
                            XSSFCell cell1 = get(row, 1);
                            cell1.setCellValue("FAD Progetto Formativo: ");
                            cell1.setCellStyle(headerCellStyle);
                            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 3));
                            XSSFCell cell3 = get(row, 4);
                            cell3.setCellValue(nome );
                            cell3.setCellStyle(headerCellStyle);
                            sheet.addMergedRegion(new CellRangeAddress(0, 0, 4, 7));
                            
                            row = get(sheet, 1);
                            XSSFCell cell2a = get(row, 1);
                            cell2a.setCellValue("Docenti: ");
                            cell2a.setCellStyle(headerCellStyle);
                            sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 3));
                            XSSFCell cell3a = get(row, 4);
                            cell3a.setCellValue(room.getNome());
                            cell3a.setCellStyle(headerCellStyle);
                            sheet.addMergedRegion(new CellRangeAddress(1, 1, 4, 7));
                            
                            AtomicInteger in = new AtomicInteger(2);
                            if (!times_A.isEmpty()) {
                                ok.addAndGet(1);
                                XSSFRow row_f = get(sheet, in.get());
                                XSSFCell cell_f = get(row_f, 1);
                                cell_f.setCellValue("FASE A");
                                headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
                                cell_f.setCellStyle(headerCellStyle);
                                sheet.addMergedRegion(new CellRangeAddress(in.get(), in.get(), 1, 7));
                                in.addAndGet(1);
                                row = get(sheet, in.get());
                                cell1 = get(row, 1);
                                cell1.setCellValue("Data");
                                cell1.setCellStyle(headerCellStyle);
                                XSSFCell cell2 = get(row, 2);
                                cell2.setCellValue("Nome");
                                cell2.setCellStyle(headerCellStyle);
                                cell3 = get(row, 3);
                                cell3.setCellValue("Cognome");
                                cell3.setCellStyle(headerCellStyle);
                                XSSFCell cell4 = get(row, 4);
                                cell4.setCellValue("Codice Fiscale");
                                cell4.setCellStyle(headerCellStyle);
                                XSSFCell cell5 = get(row, 5);
                                cell5.setCellValue("Orari Login");
                                cell5.setCellStyle(headerCellStyle);
                                XSSFCell cell6 = get(row, 6);
                                cell6.setCellValue("Orari Logout");
                                cell6.setCellStyle(headerCellStyle);
                                XSSFCell cell7 = get(row, 7);
                                cell7.setCellValue("Totale Ore");
                                cell7.setCellStyle(headerCellStyle);
                                in.addAndGet(1);
                                times_A.forEach(exc -> {
                                    XSSFCellStyle normCellStyle = workbook.createCellStyle();
                                    normCellStyle.setBorderBottom(BorderStyle.THIN);
                                    normCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                                    normCellStyle.setBorderLeft(BorderStyle.THIN);
                                    normCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                                    normCellStyle.setBorderRight(BorderStyle.THIN);
                                    normCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
                                    normCellStyle.setBorderTop(BorderStyle.THIN);
                                    normCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
                                    normCellStyle.setWrapText(true);
                                    normCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                                    normCellStyle.setFont(normFont);
                                    normCellStyle.setFillForegroundColor(exc.getColor());
                                    normCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                                    XSSFRow row1 = get(sheet, in.get());
                                    XSSFCell cell_c1 = get(row1, 1);
                                    cell_c1.setCellValue(exc.getGiorno());
                                    cell_c1.setCellStyle(normCellStyle);
                                    XSSFCell cell_c2 = get(row1, 2);
                                    cell_c2.setCellValue(exc.getNome());
                                    cell_c2.setCellStyle(normCellStyle);
                                    XSSFCell cell_c3 = get(row1, 3);
                                    cell_c3.setCellValue(exc.getCognome());
                                    cell_c3.setCellStyle(normCellStyle);
                                    XSSFCell cell_c4 = get(row1, 4);
                                    cell_c4.setCellValue(exc.getCf());
                                    cell_c4.setCellStyle(normCellStyle);
                                    XSSFCell cell_c5 = get(row1, 5);
                                    cell_c5.setCellValue(exc.getLogindate());
                                    cell_c5.setCellStyle(normCellStyle);
                                    XSSFCell cell_c6 = get(row1, 6);
                                    cell_c6.setCellValue(exc.getLogoutdate());
                                    cell_c6.setCellStyle(normCellStyle);
                                    XSSFCell cell_c7 = get(row1, 7);
                                    cell_c7.setCellValue(exc.getMillisecondtime());
                                    cell_c7.setCellStyle(normCellStyle);
                                    in.addAndGet(1);
                                });
                                in.addAndGet(1);
                                in.addAndGet(1);
                                in.addAndGet(1);
                            }
                            if (!times_B.isEmpty()) {
                                ok.addAndGet(1);
                                XSSFRow row_f = get(sheet, in.get());
                                XSSFCell cell_f = get(row_f, 1);
                                cell_f.setCellValue("FASE B");
                                headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
                                cell_f.setCellStyle(headerCellStyle);
                                sheet.addMergedRegion(new CellRangeAddress(in.get(), in.get(), 1, 7));
                                in.addAndGet(1);
                                row = get(sheet, in.get());
                                cell1 = get(row, 1);
                                cell1.setCellValue("Data");
                                cell1.setCellStyle(headerCellStyle);
                                XSSFCell cell2 = get(row, 2);
                                cell2.setCellValue("Nome");
                                cell2.setCellStyle(headerCellStyle);
                                cell3 = get(row, 3);
                                cell3.setCellValue("Cognome");
                                cell3.setCellStyle(headerCellStyle);
                                XSSFCell cell4 = get(row, 4);
                                cell4.setCellValue("Codice Fiscale");
                                cell4.setCellStyle(headerCellStyle);
                                XSSFCell cell5 = get(row, 5);
                                cell5.setCellValue("Orari Login");
                                cell5.setCellStyle(headerCellStyle);
                                XSSFCell cell6 = get(row, 6);
                                cell6.setCellValue("Orari Logout");
                                cell6.setCellStyle(headerCellStyle);
                                XSSFCell cell7 = get(row, 7);
                                cell7.setCellValue("Totale Ore");
                                cell7.setCellStyle(headerCellStyle);
                                in.addAndGet(1);
                                times_B.forEach(exc -> {
                                    XSSFCellStyle normCellStyle = workbook.createCellStyle();
                                    normCellStyle.setBorderBottom(BorderStyle.THIN);
                                    normCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                                    normCellStyle.setBorderLeft(BorderStyle.THIN);
                                    normCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                                    normCellStyle.setBorderRight(BorderStyle.THIN);
                                    normCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
                                    normCellStyle.setBorderTop(BorderStyle.THIN);
                                    normCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
                                    normCellStyle.setWrapText(true);
                                    normCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                                    normCellStyle.setFont(normFont);
                                    normCellStyle.setFillForegroundColor(exc.getColor());
                                    normCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                                    XSSFRow row1 = get(sheet, in.get());
                                    XSSFCell cell_c1 = get(row1, 1);
                                    cell_c1.setCellValue(exc.getGiorno());
                                    cell_c1.setCellStyle(normCellStyle);
                                    XSSFCell cell_c2 = get(row1, 2);
                                    cell_c2.setCellValue(exc.getNome());
                                    cell_c2.setCellStyle(normCellStyle);
                                    XSSFCell cell_c3 = get(row1, 3);
                                    cell_c3.setCellValue(exc.getCognome());
                                    cell_c3.setCellStyle(normCellStyle);
                                    XSSFCell cell_c4 = get(row1, 4);
                                    cell_c4.setCellValue(exc.getCf());
                                    cell_c4.setCellStyle(normCellStyle);
                                    XSSFCell cell_c5 = get(row1, 5);
                                    cell_c5.setCellValue(exc.getLogindate());
                                    cell_c5.setCellStyle(normCellStyle);
                                    XSSFCell cell_c6 = get(row1, 6);
                                    cell_c6.setCellValue(exc.getLogoutdate());
                                    cell_c6.setCellStyle(normCellStyle);
                                    XSSFCell cell_c7 = get(row1, 7);
                                    cell_c7.setCellValue(exc.getMillisecondtime());
                                    cell_c7.setCellStyle(normCellStyle);
                                    in.addAndGet(1);
                                });
                                in.addAndGet(1);
                                in.addAndGet(1);
                                in.addAndGet(1);
                            }
                            setBordersToMergedCells(sheet);
                            sheet.autoSizeColumn(1, true);
                            sheet.autoSizeColumn(2, true);
                            sheet.autoSizeColumn(3, true);
                            sheet.autoSizeColumn(4, true);
                            sheet.autoSizeColumn(5, true);
                            sheet.autoSizeColumn(6, true);
                            sheet.autoSizeColumn(7, true);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
                if (ok.get() > 0) {
                    if (!new File(pathtemp).exists()) {
                        new File(pathtemp).mkdirs();
                    }
                    File out = new File(pathtemp + "Progetto_" + idpr + "_report_CFAD.xlsx");
                    FileOutputStream fileOut = new FileOutputStream(out);
                    workbook.write(fileOut);
                    fileOut.close();
                    return out.getPath();
                }
            }
        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
}
