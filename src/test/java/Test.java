import com.seta.db.Entity;
import com.seta.domain.Faq;
import java.util.List;

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
        Entity e = new Entity();
        List<Faq> n = e.faqNoAnswer();
        e.close();
        n.forEach(l->{System.out.println(l.getDomanda_mod());});
    }
}
