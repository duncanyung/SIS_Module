
import java.util.ArrayList;
import java.util.Comparator;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author duncan
 */
public class CustomComparator implements Comparator<pair> {

    @Override
    public int compare(pair o1, pair o2) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        if(o1.second>o2.second)
            return 1;
        else
            return 0;
    }
    
}
