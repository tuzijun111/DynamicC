package algorithm;

import java.io.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;


public class Test1 {
    public static void main(String[] args) throws IOException, InterruptedException {
//        Collection<String> col1 = new ArrayList<String>(); // {a, b, c}
//// Collection<String> col1 = new TreeSet<String>();
//        col1.add("a");
//        col1.add("b");
//        col1.add("c");
//
//        Collection<String> col2 = new ArrayList<String>(); // {b, c, d, e}
//// Collection<String> col2 = new TreeSet<String>();
//        col2.add("b");
//        col2.add("c");
//        col2.add("d");
//        col2.add("e");
//
//        col1.retainAll(col2);
//        System.out.println(col1);
        ArrayList<ArrayList<Integer>> Gold = new ArrayList<ArrayList<Integer>>();
        Gold.add(new ArrayList<>());
        Gold.add(new ArrayList<>());
        Gold.get(0).add(1);
        Gold.get(0).add(2);
        Gold.get(0).add(3);
        Gold.get(1).add(4);
        ArrayList<ArrayList<Integer>> Prediction = new ArrayList<ArrayList<Integer>>();
        Prediction.add(new ArrayList<>());
        Prediction.add(new ArrayList<>());
        Prediction.get(0).add(1);
        Prediction.get(0).add(2);
        Prediction.get(1).add(3);
        Prediction.get(1).add(4);

        System.out.println(Evaluate.F1_measure(Gold, Prediction));

    }


}



