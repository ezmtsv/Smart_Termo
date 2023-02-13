package com.ez.data_receive;

import android.util.Base64;
import android.util.Log;

import java.io.Serializable;
import java.nio.charset.Charset;

import static android.util.Base64.NO_WRAP;

/**
 * Created by EZ on 31.01.2023.
 */

public class ObjTelemetry implements Serializable{
    static public int timeNightSec;
    static public int timeDaySec;
    static public int sys_DATA;
    static public int airTariffGist;
    static public int airHomeTmpGist;
    static public int setAirTmp;
    static public int setBoilerTmp;
    static public int boilerTmpGist;
    static public int cmd;
    static public int count_cmd;
    static public String name_SSID;
    static public String pass_SSID;
    static public String login_mail;
    static public String pass_mail;
    static public String name_smtp_serv;
    static public String mail_port_serv;
    static public String mail_from;
    static public String mail_to;
    static public String ServerIP;
    static public String timeNight;
    static public String timeDay;
    static public int flagGASOk;
    static public boolean flagHeatingOk;
    static public int flagTarifOk;
    static public boolean flagPowOk;
    static public boolean flagInversOUTOk;
    static public boolean flagBoilerON;
    static public boolean flagNOTHeat;
    static public int[] coeffArray;
    static public String[] coeffStrArray;
    static public int cnt_repeat;

    static public boolean flagInversOUTOkCUR;
    static public boolean flagBoilerONCUR;
    static public boolean flagNOTHeatCUR;
    static public int timeNightSecCUR;
    static public int timeDaySecCUR;
    static public int setAirTmpCUR;
    static public int setBoilerTmpCUR;
    static public int boilerTmpGistCUR;
    static public int cmdCUR;
    static public int count_cmdCUR;
    static public int flagGASOkCUR;
    static public int flagTarifOkCUR;

    private String tag;

    public ObjTelemetry () {
        coeffArray = new int[10];
        coeffStrArray = new String[10];
        tag = "tag";
    }

    public long  CalculateCRC(int[] dim_crc, int size) {
        long  CRC32 = 0;
        long tmp;
        while ( size != 0 ) {
            CRC32 = CRC32 + ((long)dim_crc[size]) * size;
            size--;
        }
        CRC32 = CRC32 & 0xffffffff;
        return CRC32;
    }

    public String coder_base64str(String txt){
        String txt_base64 = "?";
        final Charset UTF8_CHARSET = Charset.forName("UTF-8");
        try {
            byte[] bytes = txt.getBytes(UTF8_CHARSET);              // инициализация массива байтов символами полученной строки в формате UTF8_CHARSET
//            byte[] bytes = txt.getBytes(Charset.forName("UTF-8"));  // инициализация массива байтов символами полученной строки в формате UTF8_CHARSET
            txt_base64 = Base64.encodeToString(bytes, NO_WRAP);
        } catch (Exception e) {
            Log.d(tag, "Exception coder_base64str");
        }
        return txt_base64;
    }

    public String get_timeNight () {
        timeNight = get_time (timeNightSec);
        return timeNight;
    }

    public String get_timeDay () {
        timeDay = get_time (timeDaySec);
        return timeDay;
    }

    private String get_time (Integer val) {
        String time;
        String prefix_hour = "";
        String prefix_min = "";
        int hour, min;

        hour = val/(60*60);
        min = (val%(60*60))/60;

        if (hour < 10) prefix_hour = "0";
        if (min < 10) prefix_min = "0";
        time = "" + prefix_hour + hour + ":" + prefix_min +min;

        return time;
    }
}
