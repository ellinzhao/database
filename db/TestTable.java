package db;

import static org.junit.Assert.*;


import org.junit.Test;

import java.lang.reflect.Array;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;


/**
 * Created by Ellin on 2/25/17.
 */
public class TestTable {
    //@Test
    public void createAsSelect() {
        Parser p = new Parser();
        //String[] test = p.eval("create table a as select q, x/2 as h, y/2 as i from b, c where x<=2 and y!=2");
        String[] test = p.eval("create table a as select hi hi from b");
        for (String s : test) {
            System.out.println(s);
        }
    }

    //@Test
    public void loadTest() {
        ArrayList<String> test = new ArrayList<>();
        test.add("test");
        test.add("Lastname string,Firstname string,TeamName string");
        test.add("'Lee','Maurice','Mets'");
        Parser p = new Parser();
        ArrayList<String[]> list = p.loadTableParser(test);
        for (String[] l : list) {
            for (String s : l) {
                System.out.println(s);
            }
        }
    }

    //@Test
    public void select() {
        Parser p = new Parser();
        String[] test = p.eval("select a/2 as p, x/q as h from b, c where x <'hi' and y===2");
        for (String s : test) {
            System.out.println(s);
        }
    }

    //@Test
    public void insertRow() {
        Parser p = new Parser();
        String[] test = p.eval("insert into a values 'Golden Bears', 'hi',  6");
        for (String s : test) {
            System.out.println(s);
        }
    }

    @Test
    public void patternMatch() {
        Parser p = new Parser();
        String[] test = p.selectHelper("x / y as z", "a", "x > 2");
        for (String s : test) {
            System.out.println(s);
        }
    }

}
