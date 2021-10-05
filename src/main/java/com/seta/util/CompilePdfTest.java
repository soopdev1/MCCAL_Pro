/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seta.util;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.seta.db.Entity;
import com.seta.domain.DocumentiPrg;
import com.seta.domain.ProgettiFormativi;
import com.seta.domain.TipoDoc;
import java.text.SimpleDateFormat;
import java.util.Date;

/**i
 *
 * @author agodino
 */
public class CompilePdfTest {

    public static boolean compileValutazione(ProgettiFormativi p) {
        Entity e = new Entity();
        e.begin();
        String template = "C:/percosro file in";

        String out = "C:/percorso file out";
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(template), new PdfWriter(out));
            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            
            form.getField("data").setValue(sdf.format(new Date()));
            form.getField("soggetto").setValue(p.getSoggetto().getRagionesociale());
            form.getField("protocollo").setValue(p.getSoggetto().getProtocollo());
            form.getField("id").setValue(p.getId().toString());
            form.getField("cip").setValue(p.getCip());
            form.getField("data_start").setValue(sdf.format(p.getStart()));
            form.getField("data_end").setValue(sdf.format(p.getEnd_fb()));
            form.getField("importo").setValue(String.format("€ %.2f", p.getImporto()));
            
            form.flattenFields();//chiude il documento
            pdfDoc.close();
            
            e.persist(new DocumentiPrg(out, e.getEm().find(TipoDoc.class, 27L), p));//carico il pdf al progetto
            e.commit();
            return true;
        } catch (Exception ex) {
            e.insertTracking(null, "CompilePdf compileValutazione: " + ex.getMessage());
        } finally {
            e.close();
        }
        return false;
    }
}
