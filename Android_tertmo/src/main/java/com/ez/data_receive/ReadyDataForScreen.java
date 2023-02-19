package com.ez.data_receive;

import android.util.Log;

import com.ez.smarttermo.DataForSend;

import java.io.Serializable;

public class ReadyDataForScreen implements Serializable {
        private final String count_COMAND;
        private final String command;
        private final String timeServer;
        private final String timeWork;
        private final String timeHeat;
        private final String chargeAccum;
        private final String statusBusy;
        private final String airHomeTmp;
        private final String airOutTmp;
        private final String waterTmp;
        private final String timePowOFF;
        private String statusHeat;
        private String statusPow;
        private final String setAirTmp;
        private final String statusWorkGAS;
        private final String boilerTmp;
        private final String setBoilerTmp;
        private final String boilerTmpGist;
        private String statusWorkTariff;
        private String statusSocket;
        private final String airHomeADC;
        private final String airOutADC;
        private final String airWaterADC;
        private final String airBoilerADC;
        private final String debug;
        private String infoFromserver;
        private String infoFromserverADC;
        private String serverIP;
        private String portServ;
        private String macADR;
        private boolean flagBoilerON;
        private boolean flagPowOk;
        private boolean flagInversOUTOk;
        private boolean flagGASOk;
        private boolean flagHeatingOk;
        private boolean flagTarifOk;
        private boolean flagNOTHeat;
        private final int timeDaySec;
        private final int timeNightSec;
        private final int boilerTmpGistINT;

        public ReadyDataForScreen (TelemetrySmartTermo data) {
            int tmp;
            tmp = data.get_timePowOFF();
            timePowOFF = "Откл. электр. за период " + tmp/60 + "ч. " + tmp%60 + "мин.";

            tmp = data.get_timeHeat();
            timeHeat = "За период нагрев составил " + tmp/60 + "ч. " + tmp%60 + "мин.";

            statusBusy = data.get_statusBusy();

            if (data.get_statusHeat().equals("OK")) {
                statusHeat = "Нагрев включен";
                flagHeatingOk = true;
            } else if (data.get_statusHeat().equals("NO")) {
                statusHeat = "Нагрев выключен";
                flagHeatingOk = false;
            }

            if (data.get_statusPow().equals("OK")) {
                statusPow = "Сеть 220В в норме";
                flagPowOk = true;
            } else if (data.get_statusPow().equals("NO")) {
                statusPow = "НЕТ ПИТАНИЯ 220В!";
                flagPowOk = false;
            }

            airHomeTmp = data.get_airHomeTmp();

            waterTmp = data.get_waterTmp();

            setAirTmp = data.get_setAirTmp();

            statusWorkGAS = "\n"+"гистерезис " + data.get_airHomeTmpGist() + "˚C";

            if (data.get_statusWorkGAS().equals("OK")) {
                flagGASOk = true;
            } else if (data.get_statusWorkGAS().equals("NO")) {
                flagGASOk = false;
            }

            if (data.get_statusWorkTariff().equals("OK")) {
                statusWorkTariff = "Учитывать дневной и ночной тариф" + " ±" + data.get_airTariffGist() +
                        "˚C" + "\n" + "день : " + get_time(data.get_timeDaySec()) + "  ночь : " + get_time(data.get_timeNightSec());
                flagTarifOk = true;
            } else if (data.get_statusWorkTariff().equals("NO")) {
                statusWorkTariff = "Учитывать дневной и ночной тариф";
                flagTarifOk = false;
            }

            airHomeADC = data.get_airHomeADC();

            airWaterADC = data.get_airWaterADC();

            timeServer = get_timeSERVER(data.get_timeServer());

            if (data.get_statusInversOut().equals("OK")) {
                flagInversOUTOk = true;
            } else if (data.get_statusInversOut().equals("NO")) {
                flagInversOUTOk = false;
            }

            timeWork = get_time(data.get_timeWork());

            if (data.get_chargeAccum() > 2.5) {
                chargeAccum = String.format("Напряжение аккумулятора %.2f", data.get_chargeAccum()) + "В";
            } else chargeAccum = "Аккумулятор не подключен!";

            boilerTmp = data.get_boilerTmp();

            airOutTmp = data.get_airOutTmp();

            boilerTmpGist = "гистерезис ± " + data.get_boilerTmpGist() + "˚C";

            if (data.get_statusWorkBoiler().equals("OK")) {
                flagBoilerON = true;
            } else if (data.get_statusWorkBoiler().equals("NO")) {
                flagBoilerON = false;
            }

            setBoilerTmp = data.get_setBoilerTmp();

            if (data.get_flagNOHeat().equals("OK")) {
                flagNOTHeat = true;
            } else if (data.get_flagNOHeat().equals("NO")) {
                flagNOTHeat = false;
            }

            airOutADC = data.get_airOutADC();

            airBoilerADC = data.get_airBoilerADC();

            count_COMAND = data.get_count_COMAND().toString();
            command = data.get_command().toString();
            debug = data.get_debug().toString();
            infoFromserver = "\n"+"Дата и время сервера " + timeServer + "\n" +
                    "Отчет составлен за " + timeWork + "\n" + timeHeat + "\n" + timePowOFF +
                    "\n" + statusPow + "\n" + chargeAccum;
            infoFromserverADC = "\n"+
                    "АЦП возд.              " + get_airHomeADC () + "\n"
                    +"АЦП теплоносит. " + get_airWaterADC () + "\n"
                    +"АЦП бойлер           " + get_airBoilerADC () + "\n"
                    +"АЦП улица             " + get_airOutADC ();
            timeDaySec = data.get_timeDaySec();
            timeNightSec = data.get_timeNightSec();

            boilerTmpGistINT = Integer.parseInt (data.get_boilerTmpGist());
            if (data.get_command() == DataForSend.SET_LINK) {
                try {
                    serverIP = data.get_serverIP();
                    if (serverIP.length() == 12) {
                        serverIP = "" + serverIP.charAt(0) + serverIP.charAt(1) + serverIP.charAt(2) + "." +
                                serverIP.charAt(3) + serverIP.charAt(4) + serverIP.charAt(5) + "." +
                                serverIP.charAt(6) + serverIP.charAt(7) + serverIP.charAt(8) + "." +
                                serverIP.charAt(9) + serverIP.charAt(10) + serverIP.charAt(11);
                    }
                    portServ = data.get_portServ();
                } catch (Exception e) {
                    Log.d("tag", "ReadyDataForScreen Exception data.get_serverIP()");
                }
            }
            macADR =  data.get_macADR();
            macADR =  macADR.substring(0,macADR.length() - 1);
        }

        public String get_airHomeTmp () { return airHomeTmp; }
        public String get_airOutTmp () { return airOutTmp; }
        public String get_waterTmp () { return waterTmp; }
        public String get_statusHeat () { return statusHeat; }
        public String get_statusPow () { return statusPow; }
        public String get_statusWorkGAS () { return statusWorkGAS; }
        public String get_boilerTmp () { return boilerTmp; }
        public String get_setBoilerTmp () { return setBoilerTmp; }
        public String get_boilerTmpGist () { return boilerTmpGist; }
        public String get_statusBusy () { return statusBusy; }
        public String get_airHomeADC () { return airHomeADC; }
        public String get_airWaterADC () { return airWaterADC; }
        public String get_statusWorkTariff () { return statusWorkTariff; }
        public String get_airOutADC () { return airOutADC; }
        public String get_airBoilerADC () { return airBoilerADC; }
        public String get_count_COMAND () { return count_COMAND; }
        public String get_command () { return command; }
        public String get_timeServer () { return timeServer; }
        public String get_timeWork () { return timeWork; }
        public String get_timeHeat () { return timeHeat; }
        public String get_timePowOFF () { return timePowOFF; }
        public String get_chargeAccum () { return chargeAccum; }
        public String get_setAirTmp () { return setAirTmp; }
        public String get_debug () { return debug; }
        public String get_serverIP () { return serverIP; }
        public String get_portServ () { return portServ; }
        public String get_macADR () { return macADR; }
        public String get_statusSocket () { return statusSocket; }
        public boolean get_flagBoilerON () { return flagBoilerON; }
        public boolean get_flagPowOk () { return flagPowOk; }
        public boolean get_flagInversOUTOk () { return flagInversOUTOk; }
        public boolean get_flagGASOk () { return flagGASOk; }
        public boolean get_flagHeatingOk () { return flagHeatingOk; }
        public boolean get_flagTarifOk () { return flagTarifOk; }
        public boolean get_flagNOTHeat () { return flagNOTHeat; }
        public String get_infoFromserver () { return infoFromserver; }
        public void set_statusSocket (String s) { this.statusSocket = s; }
        public int get_timeNightSec () { return timeNightSec; }
        public int get_timeDaytSec () { return timeDaySec; }
        public String get_infoFromserverADC () { return infoFromserverADC; }
        public int get_boilerTmpGistINT () { return boilerTmpGistINT; }

        String get_time (Integer val) {
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

        String get_timeSERVER (Integer val) {
            String time;
            String prefix_hour = "";
            String prefix_min = "";
            String prefix_sec = "";
            int hour, min, sec;

            hour = val/(60*60);
            min = (val%(60*60))/60;
            sec = val%60;

            if (hour < 10) prefix_hour = "0";
            if (min < 10) prefix_min = "0";
            if (sec < 10) prefix_sec = "0";
            time = "" + prefix_hour + hour + ":" + prefix_min + min + ":" + prefix_sec + sec;

            return time;
        }


}
