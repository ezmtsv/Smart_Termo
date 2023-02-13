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
        obj.name_SSID.getChars(0, obj.name_SSID.length(), buf, 35);
        obj.pass_SSID.getChars(0, obj.pass_SSID.length(), buf, 52);
        buf[51] = (char)obj.name_SSID.length();

        for (int i = 0; i<64; i++) {
            buf_int[i] = (int)buf[i];
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
