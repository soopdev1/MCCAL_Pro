/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seta.util;

import com.seta.domain.Allievi;
import com.seta.domain.ProgettiFormativi;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author agodino
 */
public class MakeTarGz {

    public static void createTarArchive(List<ProgettiFormativi> p_list, String file_path_name) throws IOException, ParseException {
        File f = new File(file_path_name);
        FileOutputStream fos = new FileOutputStream(f);
        GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos));
        final TarArchiveOutputStream tarOs = new TarArchiveOutputStream(gos);

        List<Allievi> allievi = new ArrayList<>();

        p_list.forEach(p -> {
            p.getDocumenti().stream()
                    .filter(d -> d.getTipo().getEstrazione() == 1 && d.getDeleted() == 0)
                    .forEach(d -> {
                        try {
                            addFilesToTarGZ(d.getPath(), p.getCip() + "/" + (d.getDocente() != null ? "Docenti/" + d.getDocente().getCognome() + "/" : ""), tarOs);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
            p.getAllievi().stream()
                    .filter(a -> a.getStatopartecipazione().getId().equals("01")).forEach(a -> {
                allievi.add(a);//aggiungo allievi per estrazione excel
                a.getDocumenti().stream()
                        .filter(d -> d.getTipo().getEstrazione() == 1 && d.getDeleted() == 0)
                        .forEach(d -> {
                            try {
                                addFilesToTarGZ(d.getPath(), p.getCip() + "/Allievi/" + a.getCodicefiscale() + "/", tarOs);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        });
            });
        });

        String excel = ExportExcel.createExcelAllievi(allievi);
        addFilesToTarGZ(excel, "", tarOs);
        new File(excel).deleteOnExit();
        tarOs.close();
        gos.close();
        fos.flush();
        fos.close();
        new File(excel).delete();
//        return f;
    }

    public static ByteArrayOutputStream createTarArchive(List<ProgettiFormativi> p_list) {
        try {
            ByteArrayOutputStream fos = new ByteArrayOutputStream();
            GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos));
            final TarArchiveOutputStream tarOs = new TarArchiveOutputStream(gos);

            List<Allievi> allievi = new ArrayList<>();

            p_list.forEach(p -> {
                p.getDocumenti().stream()
                        .filter(d -> d.getTipo().getEstrazione() == 1 && d.getDeleted() == 0)
                        .forEach(d -> {
                            try {
                                addFilesToTarGZ(d.getPath(), p.getCip() + "/" + (d.getDocente() != null ? "Docenti/" + d.getDocente().getCognome() + "/" : ""), tarOs);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        });
                p.getAllievi().stream()
                        .filter(a -> a.getStatopartecipazione().getId().equals("01")).forEach(a -> {
                    allievi.add(a);//aggiungo allievi per estrazione excel
                    a.getDocumenti().stream()
                            .filter(d -> d.getTipo().getEstrazione() == 1 && d.getDeleted() == 0)
                            .forEach(d -> {
                                try {
                                    addFilesToTarGZ(d.getPath(), p.getCip() + "/Allievi/" + a.getCodicefiscale() + "/", tarOs);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            });
                });
            });

            String excel = ExportExcel.createExcelAllievi(allievi);
            addFilesToTarGZ(excel, "", tarOs);
            new File(excel).deleteOnExit();
            tarOs.close();
            gos.close();
            new File(excel).delete();
            return fos;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static void addFilesToTarGZ(String filePath, String parent, TarArchiveOutputStream tarArchive) throws IOException {
        File file = new File(filePath);
        String entryName = parent + file.getName();
        tarArchive.putArchiveEntry(new TarArchiveEntry(file, entryName));
        if (file.isFile()) {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            IOUtils.copy(bis, tarArchive);
            tarArchive.closeArchiveEntry();
            bis.close();
        } else if (file.isDirectory()) {
            tarArchive.closeArchiveEntry();
            for (File f : file.listFiles()) {
                addFilesToTarGZ(f.getAbsolutePath(), entryName + File.separator, tarArchive);
            }
        }
    }
}
