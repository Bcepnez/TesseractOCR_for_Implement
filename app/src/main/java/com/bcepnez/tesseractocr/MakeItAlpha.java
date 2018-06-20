package com.bcepnez.tesseractocr;

/**
 * Created by BenzRST on 20-Jun-18.
 */

public class MakeItAlpha {
    public String convertToAlpha(String data){
        data = data.replaceAll("2","Z");
        data = data.replaceAll("1","I");
        data = data.replaceAll("0","O");
        data = data.replaceAll("4","A");
        data = data.replaceAll("€","E");
        data = data.replaceAll("£","E");
//        data = data.replaceAll("G","6");
        return data;
    }
}
