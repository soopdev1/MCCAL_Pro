/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seta.util;

import com.seta.db.Entity;
import com.seta.domain.Allievi;
import com.seta.domain.Comuni;
import com.seta.domain.Docenti;
import com.seta.domain.DocumentiPrg;
import com.seta.domain.Documenti_Allievi;
import com.seta.domain.ProgettiFormativi;
import com.seta.domain.TipoDoc;
import com.seta.entity.Check2;
import com.seta.entity.Check2.VerificheAllievo;
import com.seta.entity.Presenti;
import static com.seta.util.Utility.createDir;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author agodino
 */
public class ExportExcel {

    private static int colonna;

//    public static void main(String[] args) {
//        Entity e = new Entity();
//        Docenti d = e.getEm().find(Docenti.class, 1L);
//        e.close();
//
//        lezioniDocente(d);
//    }
    private static String calcoladurata(double doublemhours) {
        if (doublemhours <= 0) {
            return "00:00:00";
        }
        BigDecimal bd = new BigDecimal(doublemhours * 3600000L);
        bd.setScale(2, RoundingMode.HALF_EVEN);
        long millis = bd.longValue();
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        StringBuilder sb = new StringBuilder(64);
        sb.append(StringUtils.leftPad(String.valueOf(hours), 2, "0"));
        sb.append(":");
        sb.append(StringUtils.leftPad(String.valueOf(minutes), 2, "0"));
        sb.append(":");
        sb.append(StringUtils.leftPad(String.valueOf(seconds), 2, "0"));
        return sb.toString();
    }

    public static ByteArrayOutputStream lezioniDocente(Docenti docente) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Lezioni");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            SimpleDateFormat sdfH = new SimpleDateFormat("HH:mm");

            CreationHelper createHelper = workbook.getCreationHelper();
            CellStyle celldata = workbook.createCellStyle();
            celldata.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));

            colonna = 0;
            int riga = 0;
            Row row = sheet.createRow(riga);
            writeCell(row, "Data");
            writeCell(row, "Inizio");
            writeCell(row, "Fine");
            writeCell(row, "Cip");
            writeCell(row, "Alunno");

            for (DocumentiPrg d : docente.getRegistri_aula()) {
                if (d.getGiorno() != null) {
                    colonna = 0;
                    riga++;
                    row = sheet.createRow(riga);
                    writeCell(row, d.getGiorno(), celldata);
                    writeCell(row, sdfH.format(d.getOrariostart()));
                    writeCell(row, sdfH.format(d.getOrarioend()));
                    writeCell(row, d.getProgetto().getCip());
                }
            }
            for (Documenti_Allievi d : docente.getRegistri_allievi()) {
                colonna = 0;
                riga++;
                row = sheet.createRow(riga);
                writeCell(row, d.getGiorno(), celldata);
                writeCell(row, sdfH.format(d.getOrariostart_mattina()));
                writeCell(row, sdfH.format(d.getOrarioend_mattina()));
                writeCell(row, d.getAllievo().getProgetto().getCip());
                writeCell(row, d.getAllievo().getCognome() + " " + d.getAllievo().getNome());
                if (d.getOrariostart_pom() != null) {
                    riga++;
                    row = sheet.createRow(riga);
                    colonna = 0;
                    writeCell(row, d.getGiorno(), celldata);
                    writeCell(row, sdfH.format(d.getOrariostart_pom()));
                    writeCell(row, sdfH.format(d.getOrarioend_pom()));
                    writeCell(row, d.getAllievo().getProgetto().getCip());
                    writeCell(row, d.getAllievo().getCognome() + " " + d.getAllievo().getNome());
                }
            }
            Iterator<Cell> cells = row.cellIterator();//fa il resize di tutte le celle
            while (cells.hasNext()) {
                sheet.autoSizeColumn(cells.next().getColumnIndex());
            }
            workbook.write(out);
            workbook.close();
            return out;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static File compileCL2(Check2 check) {
        ProgettiFormativi p = check.getProgetto();

        Entity e = new Entity();
        e.begin();

        File template = new File(e.getPath("templace_c2"));
        String output_name = e.getPath("pathDocSA_Prg").replace("@rssa", p.getSoggetto().getId().toString()).replace("@folder", p.getId().toString()) + "cl2_" + p.getCip() + ".xlsx";
//        createDir(e.getPath("pathDocSA_Prg").replace("@rssa", p.getSoggetto().getId().toString()).replace("@folder", p.getId().toString()));
        try {
            File out_file = new File(output_name);
            FileInputStream inputStream = new FileInputStream(template);
            try (FileOutputStream out = new FileOutputStream(out_file)) {
                Workbook workbook = WorkbookFactory.create(inputStream);

                CellStyle csdate = workbook.createCellStyle();
                csdate.setDataFormat((short) 14);

                Sheet sheet = workbook.getSheetAt(0);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Row row = sheet.getRow(4);
                writeCell(row, 2, p.getSoggetto().getRagionesociale());
                writeCell(row, 4, p.getSoggetto().getProtocollo());
                writeCell(row, 6, p.getId().toString());
                row = sheet.getRow(5);

                writeCell(row, 2, p.getCip());
                writeCellDate(row, 4, p.getStart());
                writeCellDate(row, 6, p.getEnd());
                row = sheet.getRow(6);
                writeCellInt(row, 2, check.getAllievi_tot());
                writeCellInt(row, 4, check.getAllievi_ended());
                row = sheet.getRow(7);
                writeCell(row, 6, check.getNumero_min());
                int riga = 11, column;
                for (VerificheAllievo a : check.getVerifiche_allievi()) {
                    column = 1;
                    row = sheet.getRow(riga);
                    writeCell(row, column, a.getAllievo().getCognome() + " " + a.getAllievo().getNome());
                    column++;
                    writeCell(row, column, a.getM1());
                    column++;
                    writeCell(row, column, a.getM8());
                    column++;
                    writeCell(row, column, a.getSe());
                    column++;
                    writeCell(row, column, a.getPi());
                    column++;
                    writeCell(row, column, a.getRegistro());
                    column++;
                    riga++;
                }
                row = sheet.getRow(25);
                column = 1;
                writeCell(row, column, check.getGestione().getSwat());
                column++;
                writeCell(row, column, check.getGestione().getM13());
                column++;
                writeCell(row, column, check.getGestione().getConseganto());
                column++;
                writeCell(row, column, check.getGestione().getM9());
                column++;
                writeCell(row, column, check.getGestione().getCv());
                column++;
                writeCell(row, column, check.getGestione().getRegistro());
                column++;
                writeCell(row, column, check.getGestione().getStato());
                row = sheet.getRow(31);
                column = 1;
                writeCell(row, column, check.getFascicolo().getM2());
                column++;
                writeCell(row, column, check.getFascicolo().getFa());
                column++;
                writeCell(row, column, check.getFascicolo().getAllegati_fa());
                column++;
                writeCell(row, column, check.getFascicolo().getFb());
                column++;
                writeCell(row, column, check.getFascicolo().getAllegati_fb());
                column++;
                writeCell(row, column, check.getFascicolo().getM9());
                column = 1;
                row = sheet.getRow(32);
                writeCell(row, column, check.getFascicolo().getNote());
                row = sheet.getRow(26);
                writeCell(row, column, check.getFascicolo().getNote_esito());
                row = sheet.getRow(38);
                writeCell(row, 6, sdf.format(new Date()));
                XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
                workbook.write(out);
            }

            //aggiungo l'excel della check 2 ai documenti
            if (!p.getDocumenti().stream().filter(d -> d.getTipo().getId() == 28).findFirst().isPresent()) {
                e.persist(new DocumentiPrg(output_name, e.getEm().find(TipoDoc.class, 28L), p));
            }
            e.commit();

            return out_file;
        } catch (Exception ex) {
            ex.printStackTrace();
            e.insertTracking(null, "ExportExcel compileCL2: " + ex.getMessage());
        } finally {
            e.close();
        }
        return null;
    }

    public static String createExcelAllievi(List<Allievi> allievi) {
        Entity e = new Entity();
        double euro_ore = Double.parseDouble(e.getPath("euro_ore"));
        File template = new File(e.getPath("template_excel"));
//        File template = new File("C:/mnt/Microcredito/Cloud/template_estrazione_allievi.xlsx");
        String output_name = e.getPath("output_excel_archive") + "export_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
//        String output_name = "C:/mnt/Microcredito/estrazioni/" + "export_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
        e.close();
        try {

            File out_file = new File(output_name);
            FileInputStream inputStream = new FileInputStream(template);
            FileOutputStream out = new FileOutputStream(out_file);

            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            int cntriga = 1;

            Row row = null;

            ProgettiFormativi p;
            double ore_a = 0.0, ore_b = 0.0, ore_tot = 0.0;

            for (Allievi a : allievi) {
                row = sheet.createRow((short) cntriga);//riga successiva
                colonna = 0;

                p = a.getProgetto();
                ore_a = oreFa(p.getDocumenti(), a.getId());
                ore_b = a.getEsito().equals("Fase B") ? oreFb(a.getDocumenti()) : 0;
                ore_tot = ore_a + ore_b;
                int ore_tot_int = new Double(ore_tot).intValue();
                writeCell(row, a.getCognome());
                writeCell(row, a.getNome());
                writeCell(row, sdf.format(a.getDatanascita()));
                writeCell(row, a.getCodicefiscale());
                writeCell(row, a.getSesso());
                writeCell(row, a.getComune_nascita().getCittadinanza() == 0 ? "ITALIA" : a.getComune_nascita().getNome());
                writeCell(row, a.getComune_nascita().getCittadinanza() == 0 ? "000" : a.getComune_nascita().getIstat());
                writeCell(row, a.getComune_nascita().getNome());
                writeCell(row, a.getComune_nascita().getProvincia());
                writeCell(row, a.getTelefono());
                writeCell(row, a.getEmail());
                writeCell(row, a.getComune_residenza().getNome());
                writeCell(row, a.getIndirizzoresidenza());
                writeCell(row, a.getComune_residenza().getRegione());
                writeCell(row, a.getComune_residenza().getProvincia());
                writeCell(row, getCodIstat(a.getComune_residenza()));
                writeCell(row, a.getComune_domicilio().getNome());
//                writeCell(row, a.getComune_domicilio().getRegione());
                writeCell(row, a.getComune_domicilio().getProvincia());
                writeCell(row, getCodIstat(a.getComune_domicilio()));
                writeCell(row, a.getTitoloStudio().getDescrizione());
                writeCell(row, a.getTitoloStudio().getCodice());
                //29-04-2020 MODIFICA - CONDIZIONE LAVORATIVA PRECEDENTE
                //writeCell(row, a.getNeet());
                writeCell(row, a.getCondizione_lavorativa().getDescrizione());
                writeCell(row, a.getCondizione_mercato().getId());
                writeCell(row, sdf.format(a.getData_up()));
                writeCell(row, sdf.format(a.getIscrizionegg()));
                writeCell(row, a.getCpi().getDescrizione());
                writeCell(row, String.valueOf(calcolaEta(a.getDatanascita())));
                writeCell(row, p.getSoggetto().getRagionesociale());
                writeCell(row, p.getCip());
                writeCell(row, a.getIdea_impresa());
                writeCell(row, sdf.format(p.getStart()));
                writeCell(row, sdf.format(p.getEnd_fa()));
                writeCell(row, calcoladurata(ore_a));
                writeCell(row, a.getEsito().equals("Fase B") ? sdf.format(p.getStart_fb()) : "-");
                writeCell(row, a.getEsito().equals("Fase B") ? sdf.format(p.getEnd_fb()) : "-");
                writeCell(row, calcoladurata(ore_b));
                writeCell(row, String.valueOf(ore_tot_int));
                writeCell(row, a.getSelfiemployement().getDescrizione());
                writeCell(row, a.getStatopartecipazione().getId());
                writeCell(row, a.getId().toString());
                writeCell(row, a.getEsito().equals("Fase B") ? "A+B" : "A");
                writeCell(row, "SI");
                writeCell(row, "SI");
                BigDecimal bd = new BigDecimal(Double.valueOf(String.valueOf(ore_tot_int)) * euro_ore);
                bd.setScale(2, RoundingMode.HALF_EVEN);
                writeCell(row, String.format("€ %.2f", bd.doubleValue()));

                cntriga++;
            }

            Iterator<Cell> cells = row.cellIterator();//fa il resize di tutte le celle
            while (cells.hasNext()) {
                sheet.autoSizeColumn(cells.next().getColumnIndex());
            }
            workbook.write(out);
            out.close();
            return output_name;
        } catch (Exception ex) {
            System.err.println(Utility.estraiEccezione(ex));
        }
        return "";
    }

    public static void compileTabella1(Long idprogetto) {
        Entity e = new Entity();
        e.begin();

        ProgettiFormativi p = e.getEm().find(ProgettiFormativi.class, idprogetto);

//        String path = "C:\\mnt\\Microcredito\\Cloud\\tabella1_template.xlsx";
//        File template = new File(path);
//        String output_name = "C:\\Users\\agodino\\Desktop\\tabella1_out.xlsx";
        File template = new File(e.getPath("template_tabella_1"));
        String output_name = e.getPath("pathDocSA_Prg").replace("@rssa", p.getSoggetto().getId().toString()).replace("@folder", p.getId().toString()) + "tabella1_" + p.getCip() + ".xlsx";

        try {
            File out_file = new File(output_name);
            FileInputStream inputStream = new FileInputStream(template);
            FileOutputStream out = new FileOutputStream(out_file);

            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdf_short = new SimpleDateFormat("dd/MM");
            SimpleDateFormat sdf_h = new SimpleDateFormat("HH:mm");

            Row row = sheet.getRow(8);
            writeCell(row, 5, "Fase A\nData inizio " + sdf.format(p.getStart()) + " data fine " + sdf.format(p.getEnd_fa()));
            writeCell(row, 18, "Fase B\nData inizio " + sdf.format(p.getStart_fb()) + " data fine " + sdf.format(p.getEnd_fb()));

            //date sono dalla colonna 5 alla 16 riga 9a
            //stesse colonne riga 10, 11 orari lezionea
            row = sheet.getRow(9);
            Row h_inizio = sheet.getRow(10);
            Row h_fine = sheet.getRow(11);
            int riga, column = 5;
            //raggrupapre prima i registri per giorno, settare le ore giuste e sommare le ore ai ragazzi
            List<DocumentiPrg> registri = groupRegisterByDay(p.getDocumenti().stream()
                    .filter((d) -> d.getGiorno() != null && d.getDeleted() == 0)
                    .collect(Collectors.toList()));
            //
            for (DocumentiPrg d : registri) {
                writeCell(row, column, sdf_short.format(d.getGiorno()));
                writeCell(h_inizio, column, sdf_h.format(d.getOrariostart()));
                writeCell(h_fine, column, sdf_h.format(d.getOrarioend()));
                column++;
            }

            List<Allievi> allievi = p.getAllievi().stream().sorted((x, y) -> x.getCognome().compareTo(y.getCognome())).collect(Collectors.toList());

            riga = 12;//da dove iniziare a scrivere i nomi.
            double somma;
            for (Allievi a : allievi) {
                column = 1;
                somma = 0;
                row = sheet.getRow(riga);
                writeCell(row, column, a.getCognome());
                column++;
                writeCell(row, column, a.getNome());
                column++;
                writeCell(row, column, "x");
                column++;
                writeCell(row, column, "x");
                column++;
                for (DocumentiPrg d : registri) {
                    Presenti presente = d.getPresenti_list().stream().filter(p1 -> p1.getId() == a.getId()).findFirst().orElse(null);
                    somma += presente != null ? presente.getOre_riconosciute() : 0;
                    writeCell(row, column, presente != null ? String.valueOf(presente.getOre_riconosciute()) : "0");
                    column++;
                }
                writeCell(row, (column >= 17 ? column : 17), String.valueOf(somma));
                riga++;
            }
            riga = 12;
            for (Allievi a : allievi) {
                somma = 0;
                row = sheet.getRow(riga);
                column = 18;
                for (Documenti_Allievi d : a.getDocumenti().stream().filter(d -> d.getGiorno() != null && d.getDeleted() == 0).collect(Collectors.toList())) {
                    writeCell(row, column, sdf_short.format(d.getGiorno()));
                    column++;
                    if (d.getOrariostart_pom() == null) {
                        writeCell(row, column, sdf_h.format(d.getOrariostart_mattina()) + "-" + sdf_h.format(d.getOrarioend_mattina()));
                    } else {
                        writeCell(row, column, sdf_h.format(d.getOrariostart_pom()) + "-" + sdf_h.format(d.getOrarioend_pom()));
                    }
                    column++;
                    writeCell(row, column, String.valueOf(d.getOrericonosciute()));
                    column++;
                    somma += d.getOrericonosciute();
                }
                writeCell(row, (column >= 30 ? column : 30), String.valueOf(somma));
                riga++;
            }
//            Iterator<Cell> cells = row.cellIterator();//fa il resize di tutte le celle
//            while (cells.hasNext()) {
//                sheet.autoSizeColumn(cells.next().getColumnIndex());
//            }
            workbook.write(out);
            out.close();

            //aggiungo l'excel della tabella 1 ai documenti
            e.persist(new DocumentiPrg(output_name, e.getEm().find(TipoDoc.class, 18L), p));
            e.commit();

        } catch (Exception ex) {
            ex.printStackTrace();
            e.insertTracking(null, "ExportExcel compileTabella1: " + ex.getMessage());
        } finally {
            e.close();
        }
    }

    private static void writeCell(Row row, String dato) {
        Cell cell = row.createCell(colonna);
        cell.setCellValue(dato == null ? "-" : dato);
        colonna++;
    }

    private static void writeCell(Row row, Date dato, CellStyle style) {
        Cell cell = row.createCell(colonna);
        cell.setCellValue(dato);
        cell.setCellStyle(style);
        colonna++;
    }

    private static void writeCellDate(Row row, int colonna, Date dato) {
        Cell cell = row.getCell(colonna);
        cell.setCellValue(dato);
    }

    private static void writeCellInt(Row row, int colonna, int dato) {
        Cell cell = row.getCell(colonna);
        cell.setCellValue(dato);
    }

    private static void writeCell(Row row, int colonna, String dato) {
//        System.out.println("com.seta.util.ExportExcel.writeCell() " + row.getCell(colonna).getCellStyle());
        Cell cell = row.getCell(colonna);
        cell.setCellValue(dato == null ? "-" : dato);
    }

    private static int calcolaEta(Date nascaita) {
        Long millis = new Date().getTime() - nascaita.getTime();
        return (int) (millis / 31556952000L);

    }

    public static double oreFa(List<DocumentiPrg> docs, long id) {
        double ore = 0;
        for (DocumentiPrg d : docs) {
            if (d.getGiorno() != null) {

                Presenti presente = d.getPresenti_list().stream().filter(p -> p.getId() == id).findFirst().orElse(null);

                ore += presente != null ? presente.getOre_riconosciute() : 0;
            }
        }
        return ore;
    }

    public static double oreFb(List<Documenti_Allievi> docs) {
        return docs.stream().filter(d -> d.getGiorno() != null).collect(Collectors.summingDouble(d -> d.getOrericonosciute()));
    }

    private static String getCodIstat(Comuni c) {
        String out = "";

        out += String.format("%03d", Integer.parseInt(c.getCod_regione()));
        out += String.format("%03d", Integer.parseInt(c.getCod_provincia()));
        out += String.format("%03d", Integer.parseInt(c.getCod_comune()));
        return out;
    }

    private static int countAllieviEnd(List<Allievi> allievi) {
        return allievi.stream().filter(a -> a.getStatopartecipazione().getId().equals("01")).collect(Collectors.toList()).size();
    }

    private static List<DocumentiPrg> groupRegisterByDay(List<DocumentiPrg> registri) {
        List<DocumentiPrg> out = new ArrayList<>();
        List<DocumentiPrg> ordered = registri.stream().sorted((x, y) -> x.getGiorno().compareTo(y.getGiorno())).collect(Collectors.toList());//lista ordinata per giorno
        DocumentiPrg tmp;

        for (int i = 0; i < ordered.size(); i++) {
            if (i + 1 < ordered.size()) {
                if (registri.get(i).getGiorno().equals(registri.get(i + 1).getGiorno())) {
                    tmp = new DocumentiPrg();
                    tmp = registri.get(i);
                    //somme ore giorno
                    tmp.setOre(registri.get(i).getOre() + registri.get(i + 1).getOre());
                    //setto gli orari del registro
                    tmp.setOrariostart(registri.get(i).getOrariostart().before(registri.get(i + 1).getOrariostart()) ? registri.get(i).getOrariostart() : registri.get(i + 1).getOrariostart());
                    tmp.setOrarioend(registri.get(i).getOrarioend().before(registri.get(i + 1).getOrarioend()) ? registri.get(i + 1).getOrarioend() : registri.get(i).getOrarioend());
                    //somma le ore di presenza ai ragazzi
                    for (Presenti p : tmp.getPresenti_list()) {
                        for (Presenti p1 : registri.get(i + 1).getPresenti_list()) {
                            if (p.getId().equals(p1.getId())) {
                                p.setOre_riconosciute(p.getOre_riconosciute() + p1.getOre_riconosciute());
                            }
                        }
                    }

                    out.add(tmp);
                    i++;//salto elemento successivo
                } else {
                    out.add(registri.get(i));
                }
            } else {
                out.add(registri.get(i));
            }
        }

        return out.stream().sorted((x, y) -> x.getGiorno().compareTo(y.getGiorno())).collect(Collectors.toList());
    }
}
