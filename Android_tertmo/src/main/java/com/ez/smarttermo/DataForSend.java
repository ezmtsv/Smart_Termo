package com.ez.smarttermo;

import com.ez.data_receive.ObjTelemetry;

public class DataForSend {
    public static final int REQ_DATA =                    0x34;       //запрос телеметрии                                 //десятич.с. 52
    public static final int SET_TMP =                     0x44;       //установить температуру  воздуха                   //десятич.с. 68
    public static final int SET_TMPBR =                   0x20;       //установить температуру воды в бойлере             //десятич.с. 32
    public static final int SET_TMPGISTBR =               0x21;       // установить гистерезис темп. воды в бойлере
    public static final int SET_ON_OFF_BOILER =           0x22;       // Включить/Выключить бойлер
    public static final int SET_ON_OFF_ALARM_HEAT =       0x23;       // Аварийное откл. нагрева
    public static final int SYNCHRO =                     0x64;       // синхронизировать время                           //десятич.с. 100
    public static final int SYNCHRO_TIME_TARIFF =         0x67;       // синхронизировать время работы по тарифу          //десятич.с. 103
    public static final int SET_COEF =                    0x93;       // принять и сохранить коэффициенты калибр.          //десятич.с. 147
    public static final int SET_GAS	=                     0x75;       // выбор режима работы газ-эл.                      //десятич.с. 117
    public static final int SET_WORK_TARIFF =             0x78;       // вкл.-выкл. режима работы день-ночь               //десятич.с. 120
    public static final int COOL_MODE_ON =			      0x39;       // вкл. режим охлажд.                               //десятич.с. 57
    public static final int COOL_MODE_OFF	=		      0x40;       // выкл. режим охлажд.                              //десятич.с. 64
    public static final int SET_LINK	=		          0x11;       // применить настройки сети
    public static final int CONFIG_MAIL	=		          0x12;       // настройки эл. почты
    public static final int LOAD_DEF  =                   0x19;
    public static final int OK  =                         0x55;
    public static final int NO  =                         0x33;

    int cmd;
    ObjTelemetry obj;
    DataForSend (ObjTelemetry obj) {
        this.obj = obj;
        this.cmd = obj.cmd;
        //       beginInitData();
    }
    Cur_Data_ForSend getInitClasses () {
        Cur_Data_ForSend cur_data_forSend = null;
        switch (cmd) {
            case CONFIG_MAIL:
                cur_data_forSend = new SendMailConfig(obj);
                break;
            case SET_LINK:
                cur_data_forSend = new SendWIFIConfig(obj);
                break;
            case SET_COEF:
                cur_data_forSend = new SendCoefConfig(obj);
                break;
            default:
                cur_data_forSend = new SendParam(obj);
                break;
        }
        return cur_data_forSend;
    }

}
