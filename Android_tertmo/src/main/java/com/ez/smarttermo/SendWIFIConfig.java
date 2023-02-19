package com.ez.smarttermo;

import com.ez.data_receive.ObjTelemetry;

public class SendWIFIConfig implements Cur_Data_ForSend {
    ObjTelemetry obj;
    private char[] buf;
    private int[] buf_int;
    private long crc_send;
    SendWIFIConfig (ObjTelemetry obj) {
        this.obj = obj;
        buf = new char[192];
        buf_int = new int[192];
        for (int i = 0; i < 192; i++) buf[i] = ' ';
        String str = "EZAP";                                  // преамбула
        str.getChars(0, 4, buf, 0);
        readyData();
    }
    @Override
    public void readyData() {
        buf_int[67] = obj.count_cmd & 0xff;
        buf_int[68] = obj.cmd & 0xff;
        obj.name_SSID.getChars(0, obj.name_SSID.length(), buf, 35);
        obj.pass_SSID.getChars(0, obj.pass_SSID.length(), buf, 52);
        buf[51] = (char)obj.name_SSID.length();

        for(int i = 0; i<64; i++) buf_int[i] = (int)buf[i];

        crc_send = obj.CalculateCRC (buf_int, 123);                     // вычисляем контрольную сумму по 123 байт массива включительно
        buf_int[124] = (int)(crc_send & 0xff);
        buf_int[125] = (int)((crc_send>>8)&0xff);
        buf_int[126] = (int)((crc_send>>16)&0xff);
        buf_int[127] = (int)((crc_send>>24)&0xff);


        for (int i = 0; i<64; i++) {
            buf_int[i] = (int)buf[i];
        }

        for (int j = 64; j < 128; j++) {
            if (buf_int[j] > 127) {
                buf_int[j] = buf_int[j] & 0x7f;
                buf_int[j + 64] = 127;
            } else {
                buf_int[j + 64] = 0;
            }
        }

        for (int j=64; j<192; j++) {
            buf[j] = (char)buf_int[j];
        }
    }

    @Override
    public String readyStringData() {
        String s = new String (buf);
        return s;
    }
}
