package com.ez.smarttermo;

import android.util.Log;

import com.ez.data_receive.ObjTelemetry;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SendParam implements Cur_Data_ForSend{
    ObjTelemetry obj;
    private char[] buf;
    private int[] buf_int;
    private long crc_send;
    String tag = "tag";
    void log (String s) { Log.d (tag, s); }

    SendParam (ObjTelemetry obj) {
        this.obj = obj;
        buf = new char[192];
        buf_int = new int[192];
        for (int i = 0; i < 192; i++) buf[i] = ' ';
        String str = "EZAP";                                  // преамбула
        str.getChars(0, 4, buf, 0);
        obj.sys_DATA = find_SECOND (cur_data());
//        log ("SendParam obj.sys_DATA " + obj.sys_DATA);
        readyData();
    }
    @Override
    public void readyData() {
        int shift = 70;
        buf_int[67] = obj.count_cmd & 0xff;
        buf_int[68] = obj.cmd & 0xff;
        buf_int[shift] = obj.timeNightSec  & 0xff;
        shift++;
        buf_int[shift] = (obj.timeNightSec >> 8)  & 0xff;
        shift++;
        buf_int[shift] = (obj.timeNightSec >> 16)  & 0xff;
        shift++;
        buf_int[shift] = obj.timeDaySec  & 0xff;
        shift++;
        buf_int[shift] = (obj.timeDaySec >> 8)  & 0xff;
        shift++;
        buf_int[shift] = (obj.timeDaySec >> 16)  & 0xff;
        shift++;
        buf_int[shift] = obj.flagGASOk;
        shift++;
        buf_int[shift] = obj.flagTarifOk;
        shift++;
        buf_int[shift] = obj.sys_DATA  & 0xff;
        shift++;
        buf_int[shift] = (obj.sys_DATA >> 8)  & 0xff;
        shift++;
        buf_int[shift] = (obj.sys_DATA >> 16)  & 0xff;
        shift++;
        buf_int[shift] = obj.airHomeTmpGist  & 0xff;
        shift++;
        buf_int[shift] = (obj.airHomeTmpGist >> 8)  & 0xff;
        shift++;
        buf_int[shift] = obj.airTariffGist  & 0xff;
        shift++;
        buf_int[shift] = obj.setAirTmp  & 0xff;
        shift++;
        buf_int[shift] = (obj.setBoilerTmp * 100) & 0xff;
        shift++;
        buf_int[shift] = ((obj.setBoilerTmp * 100) >> 8) & 0xff;
        shift ++;
        buf_int[shift] = obj.boilerTmpGist  & 0xff;
        shift ++;
        if (obj.flagBoilerON) buf_int[shift] = 1;
        else buf_int[shift] = 0;
        shift++;
        if (obj.flagBoilerON) buf_int[shift] = 1;
        else buf_int[shift] = 0;
        shift++;
        //       System.out.println("SendParam " + buf[0]+buf[1]+buf[2]+buf[3]);
        crc_send = obj.CalculateCRC (buf_int, 123);                     // вычисляем контрольную сумму по 123 байт массива включительно
        buf_int[124] = (int)(crc_send & 0xff);
        buf_int[125] = (int)((crc_send>>8)&0xff);
        buf_int[126] = (int)((crc_send>>16)&0xff);
        buf_int[127] = (int)((crc_send>>24)&0xff);
//        Log.d(tag, "CRC paket = "+crc_send+" : " + buf_int[124]+ " : "+ buf_int[125]+ " : "+ buf_int[126]+ " : "+ buf_int[127] );

        for (int j = 64; j < 128; j++) {
            if (buf_int[j] > 127) {
                buf_int[j] = buf_int[j] & 0x7f;
                buf_int[j + 64] = 127;
            } else {
                buf_int[j + 64] = 0;
            }
        }
        for (int i = 0; i<64; i++) {
            buf_int[i] = (int)buf[i];
        }
        for (int j=64; j<192; j++) {
            buf[j] = (char)buf_int[j];
        }
    }

    @Override
    public String readyStringData () {
        String s = new String (buf);
        return s;
    }

    private String cur_data () {
        String s = "";
        Date curTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");  // задаем формат даты
        s = sdf.format(curTime);
//        log ("SendParam cur_data () " + s);
        return sdf.format(curTime);
    }

    int find_SECOND (String str_time) {          // получение системного времени в секундах
        int sec = 0;
        String hour, min, SEC_;
        char [] tmr= new char[8];
        try {
            str_time.getChars (11, 19, tmr, 0);
            hour = "" + tmr[0] + tmr[1];
            min = "" + tmr[3] + tmr[4];
            SEC_ = "" + tmr[6] + tmr[7]; //sec = 352;
            sec = (Integer.parseInt(hour)) * 60 * 60+Integer.parseInt(min) * 60 + Integer.parseInt(SEC_);
//            log ("SendParam hour  :: "+Integer.parseInt(hour)+", min :: "+Integer.parseInt(min)+", sec :: "+Integer.parseInt(SEC_));
        }
        catch (Exception e) {
            log ("SendParam Exception find_SECOND!");
        }
        return sec;
    }
}

