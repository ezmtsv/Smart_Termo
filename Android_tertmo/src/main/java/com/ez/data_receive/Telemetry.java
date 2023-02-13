package com.ez.data_receive;

public abstract class Telemetry {
    static final int OKK = 0x55;
    static final int NNO = 0x33;
    public Integer [] data;

    Telemetry (char [] Buf) {
        data = new Integer[192];
        data = convertBuf (Buf);
    }
    abstract void init_Data ();
    private Integer [] convertBuf (char [] buf) {
        Integer [] out = new Integer[192];
        for (int  i= 0; i < 192; i++) {
            out[i] = (int)buf[i] & 0xff;
        }
        for (int i = 128; i<192; i++) {
            if (out[i] == 127) out[i-64] = out[i-64] +128;
        }   // нужно добавить 128, чтобы восстановить число
        return out;
    }

}
