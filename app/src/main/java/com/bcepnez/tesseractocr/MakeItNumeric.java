package com.bcepnez.tesseractocr;

/**
 * Created by BenzRST on 19-Jun-18.
 */

public class MakeItNumeric {
    public String convertToNumeric(String data){
        data = data.replaceAll("O","0");
        data = data.replaceAll("U","0");
        data = data.replaceAll("I","1");
        data = data.replaceAll("Z","2");
        data = data.replaceAll("A","4");
        data = data.replaceAll("S","5");
        data = data.replaceAll("G","6");
        return data;
    }
}
