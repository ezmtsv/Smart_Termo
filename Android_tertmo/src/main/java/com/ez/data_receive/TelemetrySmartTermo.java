package com.ez.data_receive;

import android.support.annotation.NonNull;
import com.ez.smarttermo.DataForSend;


public class TelemetrySmartTermo extends Telemetry{
    private final int signs;
    private int shift;
    private Integer timeNightSec;
    private Integer timeDaySec;
    private final Integer count_COMAND;
    private final Integer command;
    private Integer debug;
    private Double chargeAccum;
    private Integer timeServer;
    private Integer timeWork;
    private String statusBusy;
    private String statusInversOut;
    private String airHomeTmp;
    private String airHomeTmpGist;
    private String airTariffGist;
    private String airOutTmp;
    private String waterTmp;
    private Integer timeHeat;
    private Integer timePowOFF;
    private String statusHeat;
    private String statusPow;
    private String setAirTmp;
    private String statusWorkGAS;
    private String boilerTmp;
    private String setBoilerTmp;
    private String boilerTmpGist;
    private String statusWorkTariff;
    private String statusWorkBoiler;
    private String airHomeADC;
    private String airOutADC;
    private String airWaterADC;
    private String airBoilerADC;
    private String flagNOHeat;
    private String serverIP;
    private String portServ;
    private String macADR;
    private final Integer [] data;

    public TelemetrySmartTermo(@NonNull char[] Buf) {
        super(Buf);
        data = super.data;
        signs = data[87];
        count_COMAND = data[67];
        command = data[68];
        shift = 70;
        init_Data ();
    }
    public String get_airHomeTmp () { return airHomeTmp; }
    public String get_airOutTmp () {
        return airOutTmp;
    }
    public String get_waterTmp () {
        return waterTmp;
    }
    public String get_statusHeat () {
        return statusHeat;
    }
    public String get_statusPow () {
        return statusPow;
    }
    public String get_setAirTmp () {
        return setAirTmp;
    }
    public String get_statusWorkGAS () { return statusWorkGAS; }
    public String get_boilerTmp () {
        return boilerTmp;
    }
    public String get_setBoilerTmp () {
        return setBoilerTmp;
    }
    public String get_boilerTmpGist () {
        return boilerTmpGist;
    }
    public String get_airHomeTmpGist () {
        return airHomeTmpGist;
    }
    public String get_statusBusy () {
        return statusBusy;
    }
    public String get_airHomeADC () { return airHomeADC; }
    public String get_airWaterADC () { return airWaterADC; }
    public String get_statusInversOut () { return statusInversOut; }
    public String get_airTariffGist () { return airTariffGist; }
    public String get_statusWorkTariff () { return statusWorkTariff; }
    public String get_statusWorkBoiler () { return statusWorkBoiler; }
    public String get_airOutADC () { return airOutADC; }
    public String get_airBoilerADC () { return airBoilerADC; }
    public String get_flagNOHeat () { return flagNOHeat; }
    public String get_serverIP () { return serverIP; }
    public String get_portServ () { return portServ; }
    public String get_macADR () { return macADR; }
    public Integer get_timeNightSec () {
        return timeNightSec;
    }
    public Integer get_timeDaySec () { return timeDaySec; }
    public Integer get_count_COMAND () {
        return count_COMAND;
    }
    public Integer get_command () {
        return command;
    }
    public Integer get_debug () {
        return debug;
    }
    public Integer get_timeServer () {
        return timeServer;
    }
    public Integer get_timeWork () {
        return timeWork;
    }
    public Integer get_timeHeat () {
        return timeHeat;
    }
    public Integer get_timePowOFF () {
        return timePowOFF;      // shift + 1
    }
    public Double get_chargeAccum () {
        return chargeAccum;
    }

    @Override
    void init_Data () {
        int tmp;
        macADR = "";
        for (int i = 0; i <6; i++) macADR = macADR + String.format("%02X", data[i]) + ".";
        tmp = data[shift];
        shift++;
        tmp = ((data[shift])<<8) |  tmp;
        shift++;
        timePowOFF = tmp;

        tmp = data[shift];
        shift++;
        tmp = ((data[shift])<<8) |  tmp;
        shift++;
        timeHeat = tmp;

        if (data[shift] == OKK) {                                                   // Статус работы сервера
            statusBusy = "OK";
        } else {
            statusBusy = "NO";
        }
        shift++;

        if (data[shift] == OKK) {                                                   // Статус работы нагревателя
            statusHeat = "OK";
        } else {
            statusHeat = "NO";
        }
        shift++;

        if (data[shift] == OKK) {                                                   // Статус электросети
            statusPow = "OK";
        } else {
            statusPow = "NO";
        }
        shift++;

        tmp = data[shift];                                                          // Температура воздуха в доме
        shift++;
        tmp = ((data[shift])<<8) |  tmp;
        shift++;
        if ((signs & 0x1) == 1)  tmp = tmp *-1;
        airHomeTmp = get_tmp(tmp);

        tmp = data[shift];                                                          // Температура теплоносителя
        shift++;
        tmp = ((data[shift]) << 8) |  tmp;
        shift++;
        if ((signs & 0x2) == 2)  tmp = tmp *-1;
        waterTmp = get_tmp(tmp);

        setAirTmp = data[shift].toString();                                         // Установленная температура
        shift++;

        tmp = data[shift];                                                          // Статус выбора нагревателя
        shift++;
        if (tmp == OKK) {
            statusWorkGAS = "OK";
        }
        else{
            statusWorkGAS = "NO";
        }

        tmp = data[shift];                                                          // Статус работы по тарифу день-ночь
        shift++;
        if (tmp == OKK) {
            statusWorkTariff = "OK";
        }
        else{
            statusWorkTariff = "NO";
        }

        tmp = data[shift];                                                          // Гистерезис установленной температуры
        shift++;
        tmp = ((data[shift])<<8) |  tmp;
        shift++;
        double tmp_gist = ((double)tmp)/100;
//        airHomeTmpGist = "\n"+"гистерезис " + Double.toString(tmp_gist) + "˚C" ;
        airHomeTmpGist = Double.toString(tmp_gist);

        airTariffGist = data[shift].toString();                                     // Гистерезис работы по тарифу день-ночь
        shift+=2;

        tmp = data[shift];                                                          // Значение ADC воздуха в доме
        shift++;
        tmp = ((data[shift])<<8) | tmp;
        shift++;
        airHomeADC = Integer.toString(tmp);

        tmp = data[shift];                                                          // Значение ADC теплоносителя
        shift++;
        tmp = ((data[shift])<<8) | tmp;
        shift++;
        airWaterADC = Integer.toString(tmp);

        tmp = data[shift]; shift++;                                                 // Время сервера в секундах
        tmp = ((data[shift])<<8) | tmp; shift++;
        tmp = ((data[shift])<<16) | tmp; shift++;
        timeServer = tmp;

        timeNightSec = data[shift];                                                 // Время ночного тарифа в секундах
        shift++;
        timeNightSec = ((data[shift])<<8) | timeNightSec;
        shift++;
        timeNightSec = ((data[shift])<<16) | timeNightSec;
        shift++;

        timeDaySec = data[shift];                                                   // Время дневного тарифа в секундах
        shift++;
        timeDaySec = ((data[shift])<<8) | timeDaySec;
        shift++;
        timeDaySec = ((data[shift])<<16) | timeDaySec;
        shift++;

        debug = data[shift]; shift++;                                               // Debug
        debug = ((data[shift])<<8) | debug; shift++;
        debug = ((data[shift])<<16) | debug; shift++;

        tmp = data[shift];                                                          // Статус инверсии выхода
        shift++;
        if (tmp == OKK) {
            statusInversOut = "OK";
        }
        else{
            statusInversOut = "NO";
        }

        timeWork = data[shift];                                                     // Время работы сервера в секундах
        shift++;
        timeWork = ((data[shift])<<8) | timeWork;
        shift++;
        timeWork = ((data[shift])<<16) | timeWork;
        shift++;

        chargeAccum = ((double)(data[shift]/10))+((double)(data[shift + 1])/100);    // Напряж. на аккум.
        shift+=2;

        tmp = data[shift];                                                           // Температура воды в бойлере
        shift++;
        tmp = ((data[shift])<<8) | tmp;
        shift++;
        if ((signs & 0x8) == 8) { tmp = tmp *-1;}
        boilerTmp = get_tmp(tmp);

        tmp = data[shift];                                                           // Температура воздуха на улице
        shift++;
        tmp = ((data[shift])<<8) | tmp;
        shift++;
        if ((signs & 0x4) == 4) { tmp = tmp *-1;}
        airOutTmp = get_tmp(tmp);

        boilerTmpGist = data[shift].toString();                                      // Гистерезис для бойлера
        shift++;

        if (data[shift] == 1) statusWorkBoiler = "OK";                               // Статус работы бойлера
        else statusWorkBoiler = "NO";
        shift++;

        setBoilerTmp = data[shift].toString();                                      // Установленная температура бойлера
        shift++;

        tmp = data[shift];                                                          // Запрет работы нагревателя
        shift++;
        if (tmp == 1) {
            flagNOHeat = "OK";
        }
        else{
            flagNOHeat = "NO";
        }

        tmp = data[shift];                                                          // Значение ADC воздуха на улице
        shift++;
        tmp = ((data[shift])<<8) | tmp;
        shift++;
        airOutADC = Integer.toString(tmp);

        tmp = data[shift]; shift++;                                                 // Значение ADC бойлера
        tmp = ((data[shift])<<8) | tmp; shift++;
        airBoilerADC = Integer.toString(tmp);
        if (command == DataForSend.SET_LINK) {
            serverIP = "";
            portServ = "";
            for (int i = 0; i < 12; i++) serverIP = serverIP + (char)((int)data[i + 19]);
            for (int i = 0; i < 4; i++) portServ = portServ + (char)((int)data[i + 31]);
        }
    }

    private String get_tmp (int val) {
        String air_tmp;
        double a_tmp;
        a_tmp = ((double)val)/100;
        air_tmp = String.format("%.2f" , a_tmp);
        if(val > 0) air_tmp = "+" + air_tmp;
        return air_tmp;
    }
}
