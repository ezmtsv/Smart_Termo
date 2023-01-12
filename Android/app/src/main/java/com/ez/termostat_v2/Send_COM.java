package com.ez.termostat_v2;

import android.util.Log;

/**
 * Created by evan on 20.06.2018.
 */

public class Send_COM {
    MainActivity Mobj;
    int tmp;
    int set_koef =      0x93;
    int set_link =      0x11;
    int config_mail	=	0x12;     // настройки эл. почты
    String tag = "TAG";
    long crc_send;
    int crc_for_mail = 0;
    int [] dim_for_crcmail = new int[144];
    //////////////////////////////////////////////////////////////
    byte server_stat = 1;                       // устройство сервер
    byte client_stat = 0;                       // устройство клиент
    byte termostat_stat = 0b00000010;           // устройство термостат
    byte light_stat = 0b00000110;               // устройство осветительный прибор
    byte power_socet_stat = 0b00000100;         // устройство розетка 220В
    byte operator_stat = 0b00001000;            // оператор
    byte write_stat = 0b00100000;               // запись данных
    byte read_stat = 0b00000000;                // запрос на чтение данных

    byte power_cap0 = 0;                         // мощность подключенного устройства до 100вт
    byte power_cap1 = 1;                         // мощность подключенного устройства от 100 до 200Вт
    byte power_cap2 = 2;                         // мощность подключенного устройства от 200 до 500Вт
    byte power_cap3 = 3;                         // мощность подключенного устройства от 500 до 1000Вт
    byte power_cap4 = 4;                         // мощность подключенного устройства от 4 - от 1000 до 2000Вт
    byte power_cap5 = 5;                         // мощность подключенного устройства более 2кВт
    /////////////////////////////////////////////////////////////
    Send_COM(char [] buf, int [] buf_int, int cmd){
//        char [] buf = new char[192];
        int [] buf_dim_int = new int[192];
 /*
        for(int a=4; a<35;a++){ buf[a]= '_'; }
        if(cmd != set_link) { for(int a=35; a<192;a++){ buf[a]= '_'; } }
        else { for(int a= 60; a<192;a++){ buf[a]= '_'; } }              // с 35 по 59 члены массива заполнены именем сети и ее ключем
*/
        buf[0] = 'E'; buf[1] = 'Z'; buf[2] = 'A'; buf[3] = 'P';         // преамбула
        ////////////////////////////////
//        buf[61] = Mobj.debug_char_dim[0]; buf[62] = Mobj.debug_char_dim[1]; buf[63] = Mobj.debug_char_dim[2]; buf[64] = Mobj.debug_char_dim[3]; buf[65] = Mobj.debug_char_dim[4];
        ////////////////////////////////
//      buf[0] - buf[16] - 16 байт под МАС адрес устройства

        if(cmd != config_mail){ buf[17] = (char)(client_stat | operator_stat | write_stat); }

        buf_dim_int[2+64] = Mobj.count_cmd_repeat_MK & 0xff;
        buf_dim_int[3+64] = Mobj.count_req & 0xff;
        buf_dim_int[4+64] = cmd & 0xff;
////////////////////////////////

////////////////////////////////
        if(cmd != config_mail) {
            if (cmd != set_koef) {
                int i = 6 + 64;
                buf_dim_int[i] = Mobj.time_NIGHT & 0xff;
                i++;
                buf_dim_int[i] = (Mobj.time_NIGHT >> 8) & 0xff;
                i++;
                buf_dim_int[i] = (Mobj.time_NIGHT >> 16) & 0xff;
                i++;
                buf_dim_int[i] = Mobj.time_DAY & 0xff;
                i++;
                buf_dim_int[i] = (Mobj.time_DAY >> 8) & 0xff;
                i++;
                buf_dim_int[i] = (Mobj.time_DAY >> 16) & 0xff;
                i++;
                buf_dim_int[i] = Mobj.status_mode_GAS & 0xff;
                i++;
                buf_dim_int[i] = Mobj.work_TARIF_ & 0xff;
                i++;
                buf_dim_int[i] = 0xff; // Mobj.set_tmp_serv & 0xff; not realised
                i++;
                buf_dim_int[i] = Mobj.SECOND_ & 0xff;              //buf_dim_int[i]  = 247;//
                i++;
                buf_dim_int[i] = (Mobj.SECOND_ >> 8) & 0xff;      //buf_dim_int[i] = 205;//
                i++;
                buf_dim_int[i] = (Mobj.SECOND_ >> 16) & 0xff;      //buf_dim_int[i] = 128; //
                i++;
                buf_dim_int[i] = Mobj.gisteresis_TMP_ & 0xff;
                i++;
                buf_dim_int[i] = (Mobj.gisteresis_TMP_ >> 8) & 0xff;
                i++;
                buf_dim_int[i] = Mobj.gisteresis_TARIF_ & 0xff;
                i++;
                buf_dim_int[i] = Mobj.delta_tmp_ & 0xff;
                i++;
                buf_dim_int[i] = (Mobj.sys_DATA[0]) & 0xff;
                i++;
                buf_dim_int[i] = (Mobj.sys_DATA[1]) & 0xff;
                i++;
                buf_dim_int[i] = (Mobj.sys_DATA[2]) & 0xff;
                i++;
                buf_dim_int[i] = (Mobj.sys_DATA[3]) & 0xff;
                i++;
                buf_dim_int[i] = (Mobj.delta_tmp_BOILER * 100)&0xff;
                i++;
                buf_dim_int[i] = ((Mobj.delta_tmp_BOILER * 100)>>8)&0xff;
                i++;
                buf_dim_int[i] = 5;
                i++;
                if (Mobj.flag_boiler_out) buf_dim_int[i] = 1;
                else buf_dim_int[i] = 0;
                i++;
                if (Mobj.flag_AlarmOFF_out) buf_dim_int[i] = 1;
                else buf_dim_int[i] = 0;
                i++;
//////////////////////////////////
//            Log.d(tag, "SEC " + (int)buf_dim_int[15+64] +" "+ (int)buf_dim_int[16+64]+" "+ (int)buf_dim_int[17+64]);
//////////////////////////////////
            } else {
                for (int i = 6; i < 64; i++) {
                    buf_dim_int[i + 64] = buf_int[i] & 0xff;
                }
            }
        }else {
            for(int a=69; a<159;a++){ buf_dim_int[a] = (int)buf[a]; }
            for(int a=0; a<52;a++){ dim_for_crcmail[a] = (int)buf[a+4]; }
            for(int a=52; a<142;a++){ dim_for_crcmail[a] = (int)buf[a+17]; }
            crc_for_mail = (int)(CalculateCRC(dim_for_crcmail, 142));
            crc_for_mail = (0x0000007f & crc_for_mail) | ((0x0000007f &(crc_for_mail>>8))<<8);
 //           Log.d(tag, "crc_for_mail " + crc_for_mail+"  "+(0x00ff & (crc_for_mail>>8))+"  "+ (0x00ff & crc_for_mail));
            buf_dim_int[159] = 0x00ff & crc_for_mail;
            buf_dim_int[160] = 0x0000007f &(crc_for_mail>>8);
        }
        ////////////////////////////////////////////
        for(int i = 0; i<64; i++){
            buf_dim_int[i] = (int)buf[i];
        }
        ////////////////////////////////////////////
        if (cmd != config_mail) {

            crc_send = CalculateCRC(buf_dim_int, 123); // вычисляем контрольную сумму по 123 байт массива включительно
            buf_dim_int[124] = (int)(crc_send & 0xff); buf_dim_int[125] = (int)((crc_send>>8)&0xff); buf_dim_int[126] = (int)((crc_send>>16)&0xff); buf_dim_int[127] = (int)((crc_send>>24)&0xff);
            Log.d(tag, "CRC paket = "+crc_send+" : " + buf_dim_int[124]+ " : "+ buf_dim_int[125]+ " : "+ buf_dim_int[126]+ " : "+ buf_dim_int[127] );

            for (int j = 64; j < 128; j++) {
                if (buf_dim_int[j] > 127) {
                    buf_dim_int[j] = buf_dim_int[j] & 0x7f;
                    buf_dim_int[j + 64] = 127;
                } else {
                    buf_dim_int[j + 64] = 0;
                }

            }
        }
        for(int j=64; j<192; j++){ buf[j] = (char)buf_dim_int[j]; }
        ////////////////////////////////////////
///////////////////////////////////////Debug
/*        if(cmd == config_mail){
            String ss= "";
            for(int i = 4; i<128; i++){
                ss = ss+(char)buf_dim_int[i];
            }
            Log.d(tag, "config_mail string "+ss);
        }*/
///////////////////////////////////////
//        return buf;
    }
/////////////////////////////////
long  CalculateCRC (int[] dim_crc, int size) {
    long  CRC32 = 0;
    long tmp;

    while ( size != 0 ) {
        CRC32 = CRC32 + ((long)dim_crc[size])*size;
        size--;
    }
    CRC32 = CRC32 & 0xffffffff;
    return CRC32;
}
////////////////////////////////
}
