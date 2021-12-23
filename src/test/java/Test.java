
import com.google.gson.JsonObject;
import com.seta.db.Entity;
import com.seta.domain.Allievi;
import com.seta.domain.Faq;
import com.seta.domain.ProgettiFormativi;
import com.seta.entity.Check2;
import com.seta.util.ExportExcel;
import static com.seta.util.Utility.ctrlCheckbox;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.Charsets;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author agodino
 */
public class Test {

    public static void main(String[] args) {
//        Entity e = new Entity();
//        List<Faq> n = e.faqNoAnswer();
//        e.close();
//        n.forEach(l->{System.out.println(l.getDomanda_mod());});
//        
//        

        Entity e = new Entity();
        ProgettiFormativi prg = e.getEm().find(ProgettiFormativi.class, Long.parseLong("17"));
        Check2 cl2 = new Check2();
        Check2.Gestione g = new Check2.Gestione();
        Check2.Fascicolo f = new Check2.Fascicolo();
        cl2.setAllievi_tot(4);
        cl2.setAllievi_ended(4);
        cl2.setProgetto(prg);
        cl2.setNumero_min(ctrlCheckbox(""));
        g.setSwat(ctrlCheckbox(""));
        g.setM9(ctrlCheckbox(""));
        g.setConseganto("");
        g.setCv(ctrlCheckbox(""));
        g.setM13(ctrlCheckbox(""));
        g.setRegistro(ctrlCheckbox(""));
        g.setStato(ctrlCheckbox(""));

        cl2.setGestione(g);
        f.setNote("");
        f.setNote_esito("");
        f.setAllegati_fa(ctrlCheckbox(""));
        f.setFa(ctrlCheckbox(""));
        f.setAllegati_fb(ctrlCheckbox(""));
        f.setFb(ctrlCheckbox(""));
        f.setM2(ctrlCheckbox(""));
        f.setM9(ctrlCheckbox(""));
        cl2.setFascicolo(f);

        Check2.VerificheAllievo ver;
        List<Check2.VerificheAllievo> list_al = new ArrayList();
//        for (String s : request.getParameterValues("allievi[]")) {
//            ver = new Check2.VerificheAllievo();
//            ver.setAllievo(e.getEm().find(Allievi.class, Long.parseLong(s)));
//            ver.setM1(ctrlCheckbox(request.getParameter("m1_" + s)));
//            ver.setM8(ctrlCheckbox(request.getParameter("m8_" + s)));
//            ver.setPi(ctrlCheckbox(request.getParameter("idim_" + s)));
//            ver.setRegistro(ctrlCheckbox(request.getParameter("reg_" + s)));
//            ver.setSe(ctrlCheckbox(request.getParameter("se_" + s)));
//            list_al.add(ver);
//        }
        cl2.setVerifiche_allievi(list_al);

        File checklist = ExportExcel.compileCL2(cl2);
        System.out.println("Test.main() "+checklist.getPath());
    }
}
