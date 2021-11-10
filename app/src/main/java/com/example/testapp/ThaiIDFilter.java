package com.example.testapp;
import android.util.Log;

public class ThaiIDFilter {
    public ThaiIDFilter() {
        Log.d("ThaiIDFilter", "Class Init");
    }

    // Handle Gender Field
    public String ProvideGender(String _gender) {
        if (_gender == null || _gender.length() == 0){
            return "";
        }

        if (_gender.equals("1")) {
            return "ชาย";
        }
        else if(_gender.equals("2")) {
            return "หญิง";
        }
        return "";
    } // end fn

    // Handle Date Field
    public String FormatDate(String _date) {
        if (_date.length() == 0 || _date.length() < 7) {
        return "-";
        }
        if (_date.equals("99999999")) {
          return  "ตลอดชีพ";
        }
         String date;
         String _day , _month , _year;
         _day = _date.substring(6,8);
         _month = _date.substring(4,6);
         _year = _date.substring(0,4);
         date = _day + "/" + _month + "/" + _year;
         return date;
    }

}
