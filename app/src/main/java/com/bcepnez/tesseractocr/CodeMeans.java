package com.bcepnez.tesseractocr;

/**
 * Created by BenzRST on 18-Jun-18.
 */

public class CodeMeans {
    public String decode(String code){
        switch (code){
            case "D" : return "Germany";
            case "GRC" : return "Greece";
        }
        return code;
    }
}
