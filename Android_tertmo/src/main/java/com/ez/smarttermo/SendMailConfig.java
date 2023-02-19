package com.ez.smarttermo;

import android.util.Log;
import com.ez.data_receive.ObjTelemetry;

/**
 * Created by EZ on 01.02.2023.
 */

public class SendMailConfig implements Cur_Data_ForSend {
    ObjTelemetry obj;
    private char[] buf;
    private int[] buf_int;
    SendMailConfig (ObjTelemetry obj) {
        this.obj = obj;
        buf = new char[192];
        buf_int = new int[192];
        for (int i = 0; i < 192; i++) buf[i] = '_';
        String str = "EZAP";                                  // преамбула
        str.getChars(0, 4, buf, 0);
        readyData();
    }
    @Override
    public void readyData() {
        String mailFrom64;
        String pass64;
        int[] dim_for_crcmail = new int[144];
        int crc_for_mail = 0;

        try {
            mailFrom64 = obj.coder_base64str(obj.mail_from);
            pass64 = obj.coder_base64str(obj.pass_mail);
            obj.mail_from.getChars(0, obj.mail_from.length(), buf, 4);
            obj.mail_to.getChars(0, obj.mail_to.length(), buf, 26);
            obj.mail_port_serv.getChars(0, obj.mail_port_serv.length(), buf, 48);
            mailFrom64.getChars(0, mailFrom64.length(), buf, 69);
            obj.name_smtp_serv.getChars(0, obj.name_smtp_serv.length(), buf, 101);
            pass64.getChars(0, pass64.length(), buf, 131);

            buf[51] = (char) obj.mail_from.length();
            buf[52] = (char) mailFrom64.length();
            buf[53] = (char) pass64.length();
            buf[54] = (char) obj.mail_to.length();
            buf[55] = (char) obj.name_smtp_serv.length();

            buf_int[67] = obj.count_cmd & 0xff;
            buf_int[68] = obj.cmd & 0xff;

            for (int a = 69; a < 159; a++) buf_int[a] = (int) buf[a];
            for (int a = 0; a < 52; a++) dim_for_crcmail[a] = (int) buf[a + 4];
            for (int a = 52; a < 142; a++) dim_for_crcmail[a] = (int) buf[a + 17];
            crc_for_mail = (int) (obj.CalculateCRC(dim_for_crcmail, 142));
            crc_for_mail = (0x0000007f & crc_for_mail) | ((0x0000007f & (crc_for_mail >> 8)) << 8);
            //           Log.d(tag, "crc_for_mail " + crc_for_mail+"  "+(0x00ff & (crc_for_mail>>8))+"  "+ (0x00ff & crc_for_mail));
            buf_int[159] = 0x00ff & crc_for_mail;
            buf_int[160] = 0x0000007f & (crc_for_mail >> 8);
        } catch (Exception e ) { Log.d ("tag", "Exception SendMailConfig readyData() "); }
        for (int i = 0; i < 64; i++) buf_int[i] = (int)buf[i];
        for (int j = 64; j < 192; j++) buf[j] = (char)buf_int[j];

    }

    @Override
    public String readyStringData() {
        String s = new String (buf);
        return s;
    }
}

