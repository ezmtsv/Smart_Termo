package com.ez.smarttermo;

import com.ez.data_receive.ObjTelemetry;

public class SendCoefConfig implements Cur_Data_ForSend {
    ObjTelemetry obj;
    private char[] buf;
    private int[] buf_int;
    long crc_send;
    SendCoefConfig (ObjTelemetry obj) {
        this.obj = obj;
        buf = new char[192];
        buf_int = new int[192];
        for (int i = 0; i < 192; i++) buf[i] = ' ';
        String str = "EZAP";                                  // преамбула
        str.getChars(0, 4, buf, 0);
        readyData();
    }
    private int[] readyDataCoeff (String[] s) {                     // T˚C = A0*adc^4+A1*adc^3\n+A2*adc^2+A3*adc^1+A4"
        int[] coeffArr = new int[10];
        coeffArr[0] = (int)((Double.parseDouble(s[0])*10000.0));    // A1
        coeffArr[1] = (int)((Double.parseDouble(s[1])*10000.0));    // A2
        coeffArr[2] = (int)((Double.parseDouble(s[2])*10000.0));    // A3
        coeffArr[3] = (int)((Double.parseDouble(s[3])*10000.0));    // A4
        coeffArr[4] = Integer.parseInt(s[4]);                       // A1E
        coeffArr[5] = Integer.parseInt(s[5]);                       // A2E
        coeffArr[6] = Integer.parseInt(s[6]);                       // A3E
        coeffArr[7] = Integer.parseInt(s[7]);                       // A4E
        coeffArr[8] = (int)((Double.parseDouble(s[8])*10000.0));    // A0
        coeffArr[9] = Integer.parseInt(s[9]);                       // A0E для нулевого члена степень не может быть больше 15 и она всегда отрицательна, поэтому значение размещаем в байте отвечающем за знаки
        return coeffArr;
    }
    @Override
    public void readyData() {
//        System.out.println("SendCoefConfig");
        int[] coeffArr = new int[10];
        coeffArr = readyDataCoeff (obj.coeffStrArray);

        buf_int[70] = 0;                                        // сбрасываем знаки коэф.
        if (coeffArr[0] < 0) {
            buf_int[70] = buf_int[70] | 0x01;                   // в случае отрицательного значения выставляем бит 1
            coeffArr[0] = coeffArr[0] * -1;                     // переводим в положительное число
        }
        if (coeffArr[1] < 0) {
            buf_int[70] = buf_int[70] | 0x02;                   // в случае отрицательного значения выставляем бит 2
            coeffArr[1] = coeffArr[1] * -1;                     // переводим в положительное число
        }
        if (coeffArr[2] <0 ) {
            buf_int[70]= buf_int[70] | 0x04;                    // в случае отрицательного значения выставляем бит 3
            coeffArr[2] = coeffArr[2] * -1;                     // переводим в положительное число
        }
        if (coeffArr[3] < 0) {
            buf_int[70] = buf_int[70] | 0x08;                   // в случае отрицательного значения выставляем бит 4
            coeffArr[3] = coeffArr[3] * -1;                     // переводим в положительное число
        }
        if (coeffArr[9] <0 ) { coeffArr[9] = coeffArr[9] * -1; }
        buf_int[70] = buf_int[70] | (coeffArr[9] << 4);         // записываем показатель степени нулевого члена в buf_int[29], в первых чеиырех битах хранятся знаки для А1-А4
//////////////////////////////////////////////////////////////////////////////////
        buf_int[71] = coeffArr[0]&0xff;
        buf_int[72] = (coeffArr[0]>>8)&0xff;
        buf_int[73]= (coeffArr[0]>>16)&0xff;

        buf_int[75] = coeffArr[1]&0xff;
        buf_int[76] = (coeffArr[1]>>8)&0xff;
        buf_int[77]= (coeffArr[1]>>16)&0xff;

        buf_int[79] = coeffArr[2] & 0xff;
        buf_int[80] = (coeffArr[2] >> 8) & 0xff;
        buf_int[81] = (coeffArr[2] >> 16) & 0xff;

        buf_int[83] = coeffArr[3] & 0xff;
        buf_int[84] = (coeffArr[3] >> 8) & 0xff;
        buf_int[85] = (coeffArr[3] >> 16) & 0xff;

        if (coeffArr[8] < 0) {
            coeffArr[8] = coeffArr[8] * -1;
            buf_int[89] = 0x80;
        }                                                       // в случае отрицательного значения мантиссы в старшем байте в стпршем разряде рисуем 1
        buf_int[87] = coeffArr[8] & 0xff;
        buf_int[88] = (coeffArr[8] >> 8) & 0xff;
        buf_int[89] = buf_int[89] | ((coeffArr[8] >> 16) & 0xff);
////////////////////////обработка основания и порядка степени //////////////////////////////
        buf_int[74] = 0;
        buf_int[78] = 0;
        buf_int[82] = 0;
        buf_int[86] = 0;
        if (coeffArr[4] < 0) {
            buf_int[74] = 0x80;                                 // в случае отрицательного значения выставляем старший бит в 1
            coeffArr[4] = coeffArr[4] * -1;                     // переводим в положительное число
        }
        if (coeffArr[5] < 0) {
            buf_int[78] = 0x80;                                 // в случае отрицательного значения выставляем старший бит в 1
            coeffArr[5] = coeffArr[5] * -1;                     // переводим в положительное число
        }
        if (coeffArr[6] < 0) {
            buf_int[82] = 0x80;                                 // в случае отрицательного значения выставляем старший бит в 1
            coeffArr[6] = coeffArr[6] * -1;                     // переводим в положительное число
        }
        if (coeffArr[7] < 0) {
            buf_int[86]= 0x80;                                  // в случае отрицательного значения выставляем старший бит в 1
            coeffArr[7] = coeffArr[7] * -1;                     // переводим в положительное число
        }

        buf_int[74] = buf_int[74] | coeffArr[4];
        buf_int[78] = buf_int[78] | coeffArr[5];
        buf_int[82] = buf_int[82] | coeffArr[6];
        buf_int[86] = buf_int[86] | coeffArr[7];

        crc_send = obj.CalculateCRC (buf_int, 123);        // вычисляем контрольную сумму по 123 байт массива включительно
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
    public String readyStringData() {
        String s = new String (buf);
        return s;
    }
}

