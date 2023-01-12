package com.ez.termostat_v2;

import android.util.Log;

public class Telemetry {
    static String air_tmp;
    static String water_tmp;
    static String time_work;
    static String time_heat;
    static String time_powOFF;
    static String status_heat;
    static String status_pow;
    static String time_data_serv;
    static String charge_accum;
    static int set_tmp_serv = 0xff;
    static String time_N;
    static String time_D;
    static String work_GAS_;
    static String time_server;
    static String out_tmp;
    static String boiler_tmp;
    static int boiler_settmp = 0xff;
    static int boiler_tmp_gist;
    static boolean flag_boiler;
    double chargeAC;

    static char OK = 'O';
    final int OKK = 0x55; //
    final int NNO = 0x33; //
    static int gas;
    static String tim_G;
    static int status_busy;
    static int time_MC;
    static int work_TARIF;
    static int gist_TARIF;
    static int  tmp_A, tmp_W;
    static int  tmp_O, tmp_B;
    static int  tmp_Out, tmp_Boil;
    static int debug1;
    static int count_COMAND;
    static int COMAND;
    static  boolean cool_ON;
    static  boolean crc_flag;
    static int  cmd_succ;
    static int flag_AlarmOFF;
    String tag = "TAG";
    ////////////////////////////
    final int set_tmp =                 0x44;     //установить температуру воздуха                     ////десятич.с. 68
    final int set_tmpBR =               0x20;      //установить температуру бойлера                     ////десятич.с. 32
    final int  set_GAS	=               0x75;     // выбор режима работы газ-эл.                       ////десятич.с. 117
    final int  set_work_TARIF =         0x78;     // вкл.-выкл. режима работы день-ночь                ////десятич.с. 120
    final int cool_modeON =			    0x39;     // вкл. режим охлажд.                                ////десятич.с. 57
    final int cool_modeOFF	=		    0x40;     // выкл. режим охлажд.                               ////десятич.с. 64
    final int set_ONOFFboiler =         0x22;     // Включить/Выключить бойлер                         ////десятич.с. 34
    final int set_ONOFF_AlarmHeat =     0x23;   // Аварийное откл. нагрева                             ////десятич.с. 35
    final int synchro_timeTARIF =       0x67;     // синхронизировать время работы по тарифу          ////десятич.с. 103
    ////////////////////////////

    Telemetry(char[] buf, int cmd){
        time_data_serv = "";
        int hourN, minN, hourD, minD;
        String prefix_hour = "", prefix_min = ""; tim_G = "";
///////////////////////////////////////////////////////////
        
        int   i = 64+6; int znaki_TMP;
        int air_tmp_int, water_tmp_int;
        int debug, sec, hour, min;
        int time_Night_int, time_Day_int;
        int int_buf_int[] = new int[192];
        int time_work_serv;
        for(int k=0; k<192; k++){ int_buf_int[k] = (int)buf[k]&0xff;  }
        for(int k=128; k<192; k++){ if(int_buf_int[k] == 127)int_buf_int[k-64] = int_buf_int[k-64] +128;  }   // нужно добавить 128, чтобы восстановить число
        long crc_s = CalculateCRC(int_buf_int,123);
        if ((crc_s & 0xffff) == (int_buf_int[124] | int_buf_int[125]<<8)) crc_flag = true;
        else crc_flag = false;
        crc_flag = true; /// временно, для работы на предыдущей плате!
        Log.d(tag, "crc_ "+(crc_s & 0xff)+ "-"+((crc_s & 0xff00)>>8)+"\n"+int_buf_int[124]+"-"+int_buf_int[125]+" "+crc_flag);
//        Log.d(tag, "crc_ "+((crc_s & 0xff00) | (crc_s & 0xff)) + " get crc "+(crc_s & 0xffff));
//////////////////////////////////////////////////
/*
        for(int k=120; k<128; k++){
//            Log.d(tag, "buf_TCP["+k+"] = "+buf[k]);
            Log.d(tag, "buf_TCP["+k+"] = "+int_buf_int[k]);
        }
*/
 /*
        Log.d(tag, "answer_server buf_TCP[2] "+int_buf_int[2+64]);
        for(int km = 31; km<34; km++){
            Log.d(tag, "answer_server buf_TCP["+km+"]= "+Integer.toString(int_buf_int[km+64]+int_buf_int[km+128]));
        }
*/
        /*
        for(int km = 42; km<45; km++){
            Log.d(tag, "answer_server buf_TCP["+km+"]= "+Integer.toString(int_buf_int[km+64]+int_buf_int[km+128])+ " +128= "+int_buf_int[km+128]);
        }*/
//        Log.d(tag, "cool "+ int_buf_int[40+64]);
//////////////////////////////////////////////////

        String tmp_debag;

        int tmp;
        count_COMAND = int_buf_int[64+3];
        COMAND = int_buf_int[64+4];

        tmp = int_buf_int[i]; i++; tmp = ((int_buf_int[i])<<8) |  tmp; i++;
        time_powOFF = "Откл. электр. за период " +tmp/60+"ч. "+tmp%60+"мин.";    // получаем время отключения электричества

        tmp = int_buf_int[i]; i++; tmp = ((int_buf_int[i])<<8) |  tmp; i++;
        time_heat = "За период нагрев составил " +tmp/60+"ч. "+tmp%60+"мин.";         // получаем время нагрева за период

        if(int_buf_int[i] == OKK) {status_busy = OKK; } else{ status_busy = NNO; } i++;

        if(int_buf_int[i] == OKK){status_heat = "Нагрев включен";} else {status_heat = "Нагрев выключен";} i++;

        if(int_buf_int[i] == OKK){status_pow= "Сеть 220В в норме";} else {status_pow= "НЕТ ПИТАНИЯ 220В!";} i++;

        air_tmp_int = int_buf_int[i]; i++; air_tmp_int = ((int_buf_int[i])<<8) |  air_tmp_int; i++;                        // получаем темп. воздуха

        water_tmp_int = int_buf_int[i]; i++; water_tmp_int = ((int_buf_int[i])<<8) |  water_tmp_int; i++;                        // получаем темп. воды

        set_tmp_serv = int_buf_int[i]; i++;

        gas = int_buf_int[i]; i++;

        work_TARIF = int_buf_int[i]; i++;

        tmp = int_buf_int[i]; i++; tmp = ((int_buf_int[i])<<8) |  tmp; i++;
        double tmp_gist = ((double)tmp)/100;
        tim_G = Double.toString(tmp_gist);

        gist_TARIF = int_buf_int[i]; i++;

        znaki_TMP = int_buf_int[i]; i++;
        tmp_A = int_buf_int[i]; i++;
        tmp_A = ((int_buf_int[i])<<8) | tmp_A; i++;
        tmp_W = int_buf_int[i]; i++;
        tmp_W = ((int_buf_int[i])<<8) | tmp_W; i++;
        if((znaki_TMP & 0x1)==1){ air_tmp_int = air_tmp_int *-1;}
        if((znaki_TMP & 0x2)==2){ water_tmp_int = water_tmp_int *-1;}
        sec = int_buf_int[i]; i++;
        sec = ((int_buf_int[i])<<8) | sec; i++;
        sec = ((int_buf_int[i])<<16) | sec; i++;
        time_MC = sec;
        time_Night_int = int_buf_int[i]; i++;
        time_Night_int = ((int_buf_int[i])<<8) | time_Night_int; i++;
        time_Night_int = ((int_buf_int[i])<<16) | time_Night_int; i++;
        time_Day_int = int_buf_int[i]; i++;
        time_Day_int = ((int_buf_int[i])<<8) | time_Day_int; i++;
        time_Day_int = ((int_buf_int[i])<<16) | time_Day_int; i++;
        debug = int_buf_int[i]; i++;                       // для сохраненного времени записи температуры
        debug = ((int_buf_int[i])<<8) | debug; i++;
        debug = ((int_buf_int[i])<<16) | debug; i++;
        if(int_buf_int[i] == OKK){ cool_ON = true; }
        else{ cool_ON = false;  } ; i++;
        time_work_serv = int_buf_int[i]; i++;
        time_work_serv = ((int_buf_int[i])<<8) | time_work_serv; i++;
        time_work_serv = ((int_buf_int[i])<<16) | time_work_serv; i++;
        //// принимаем напряжение на аккум 3.4В - это 0% заряда, 4.2В - это 100%
        /// тогда  (4.2 - 3.4)/100 -  процентов на вольт, находим заряд в процентах - (напряж. на аккум. - 3,4)*процентов на вольт
/*        chargeAC = (((double)int_buf_int[i])/10)/0.041;
        if(chargeAC>100){ chargeAC = 100; }

        else{ charge_accum = "Заряд аккумулятора "+two_symbol_after_point(""+chargeAC)+"%"; }*/
  //      Log.d(tag, "zaryad 0_byte "+int_buf_int[i]+ " zaryad 1_byte "+int_buf_int[i+1]);

        chargeAC = ((double)(int_buf_int[i]/10))+((double)(int_buf_int[i+1])/100); /// напряж. на аккум.
//        Log.d(tag, "chargeAC "+chargeAC);
//        chargeAC = (chargeAC - 3.4)*(100/(4.2 - 3.4));                      /////берем  4,2В на аккум за 100% заряда, 3.4В за 0%
//        chargeAC = (double)((int)(chargeAC*10))/10;                         /// оставляем одну цифру в после запятой в процентаз заряда аккум
//        Log.d(tag, "chargeAC "+chargeAC+"  int_buf_int["+i+"]= "+int_buf_int[i]+"  int_buf_int["+(i+1)+"]= "+int_buf_int[i+1]);
//        charge_accum = "Заряд аккумулятора "+two_symbol_after_point(""+chargeAC)+"%";
//        if(chargeAC>100){ chargeAC = 100; }
        charge_accum = "Напряжение аккумулятора "+two_symbol_after_point(""+chargeAC)+"В";
        if(chargeAC<2.5){ charge_accum = "Аккумулятор не подключен!"; }

        i+=2;
        tmp_B = int_buf_int[i]; i++;
        tmp_B = ((int_buf_int[i])<<8) | tmp_B; i++;
        if((znaki_TMP & 0x8) == 8){ tmp_B = tmp_B *-1;}
        tmp_O = int_buf_int[i]; i++;
        tmp_O = ((int_buf_int[i])<<8) | tmp_O; i++;
        if((znaki_TMP & 0x4) == 4){ tmp_O = tmp_O *-1;}
        boiler_tmp_gist = int_buf_int[i]; i++;
        if (int_buf_int[i] == 1)flag_boiler = true;
        else flag_boiler = false;
        i++;
        boiler_settmp = int_buf_int[i];
        i++;
        flag_AlarmOFF = int_buf_int[i]; i++;
        tmp_Out = int_buf_int[i]; i++;
        tmp_Out = ((int_buf_int[i])<<8) | tmp_Out; i++;
        tmp_Boil = int_buf_int[i]; i++;
        tmp_Boil = ((int_buf_int[i])<<8) | tmp_Boil; i++;

        out_tmp = get_tmp_(tmp_O);
        boiler_tmp = get_tmp_(tmp_B);
//        Log.d(tag, "out_tmp "+out_tmp+"  boiler_tmp "+boiler_tmp);


//        debug1 = int_buf_int[i]; i++;                  //debug1 = (int)buf[126];
//        tmp_debag = "Время МК "+Integer.toString(sec)+" : "+ Integer.toString(debug) +" : "+ Integer.toString(debug1);
//        Log.d(tag, "time save = "+debug/3600+"ч "+((debug%3600)/60)+"мин "+debug%60+"сек");

        if(gas == OKK){
//            work_GAS_ = "Нагреватель - Газовый котел,"+"\n"+"гистерезис "+tim_G+"˚C";
            work_GAS_ = "\n"+"гистерезис "+tim_G+"˚C";
        }
        else{
//            work_GAS_ = "Нагреватель - Электро котел,"+"\n"+"гистерезис "+tim_G+"˚C";
            work_GAS_ = "\n"+"гистерезис "+tim_G+"˚C";
        }
        air_tmp = get_tmp_(air_tmp_int);
        water_tmp = get_tmp_(water_tmp_int);

        hourN = time_Night_int/(60*60);
        hourD = time_Day_int/(60*60);
        minN = (time_Night_int%(60*60))/60;
        minD = (time_Day_int%(60*60))/60;

        int serv_hour, serv_min, serv_sec;
        serv_hour = time_MC/(60*60); serv_min = (time_MC%(60*60))/60; serv_sec = time_MC%60;
        if(serv_hour<10){ time_data_serv = "0"+ serv_hour; } else { time_data_serv = ""+ serv_hour;}
        if(serv_min<10){ time_data_serv = time_data_serv+ ":0"+ serv_min; } else { time_data_serv = time_data_serv+ ":"+ serv_min;}
        if(serv_sec<10){ time_data_serv = time_data_serv+ ":0"+ serv_sec; } else { time_data_serv = time_data_serv+ ":"+ serv_sec;}
//                time_data_serv = ""+ time_MC/(60*60)+":"+(time_MC%(60*60))/60+":"+time_MC%60;


        prefix_hour = ""; prefix_min = "";
        if(hourN<10) prefix_hour = "0";
        if(minN<10) prefix_min = "0";
        time_N = ""+prefix_hour+hourN+":"+prefix_min+minN;

        prefix_hour = ""; prefix_min = "";
        if(hourD<10) prefix_hour = "0";
        if(minD<10) prefix_min = "0";
        time_D = ""+prefix_hour+hourD+":"+prefix_min+minD;
 //       time_data_serv = "...";
        time_work = "Отчет составлен за "+ time_work_serv/(60*60)+" ч. "+(time_work_serv%(60*60))/60+" мин.";

//        switch (COMAND){
        switch (cmd){  /// COMAND
            case set_tmp:
                cmd_succ = set_tmp_serv;
                break;
            case set_tmpBR:
                cmd_succ = boiler_settmp;
                break;
            case set_GAS:
                cmd_succ = gas+1;
                break;
            case set_work_TARIF:
                cmd_succ = work_TARIF+2;
                break;
            case cool_modeON:
                if(cool_ON)cmd_succ = OKK;
                else cmd_succ = NNO;
                break;
            case cool_modeOFF:
                if(!cool_ON)cmd_succ = OKK;
                else cmd_succ = NNO;
                break;
            case set_ONOFFboiler:
                if(flag_boiler)cmd_succ = OKK;
                else cmd_succ = NNO;
                break;
            case set_ONOFF_AlarmHeat:
                if(flag_AlarmOFF == 1)cmd_succ = OKK;
                else cmd_succ = NNO;
                break;
            case synchro_timeTARIF:
                cmd_succ = OKK;
                break;
            default:
                cmd_succ = 0xffffff;
                break;
        }

///////////////////////////////////////////////////////////
    }
    String get_tmp_(int temperatute){
        String air_tmp = "";
        Double a_tmp;
        a_tmp = ((double)temperatute)/100;

        if(temperatute > 0){
            air_tmp = "+";
        }
        ;
        air_tmp = air_tmp + two_symbol_after_point(Double.toString(a_tmp));
/*
        if(a_tmp < alarm_a_tmp){
            if(!flag_start_alarmtask) {
                flag_start_alarmtask = true;
                tmr_alarm = new Timer();
                alarmtask = new Time_alarm();
                try {
                    tmr_alarm.schedule(alarmtask, 1000, 1500000);
                } catch (Exception c) {
                    ;
                }
            }
        }else{ ;}
*/
        return air_tmp;
    }
///////////////////////////////////////////
String two_symbol_after_point(String symb){
    String res;
    int lenght_str;
    char[] s_buf = new char[100];
    char[] buf = new char[100];
    buf  = symb.toCharArray();      // получение из строки массива символов
    int count_sym_point = 0;
    int count_sym = 0;

    boolean flag_point = false;
    lenght_str = symb.length(); // количество символов в строке
    for(int  tmp = 0; tmp< 100; tmp++){s_buf[tmp] = ' ';}
    /////////////////////округление до 2 знаков после запятой
    for(int i = 0; i< lenght_str; i++){
        s_buf[i]= buf[i];
        count_sym++;
        if(buf[i]== '.'){
            flag_point = true;
        }

        if(flag_point){count_sym_point++;}
        if(count_sym_point >2) break;
    }
    ////////////////////////////
    res= "";
    for(int i = 0; i< count_sym; i++){res = res+s_buf[i];}
    return res;
}

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
    /////////////////////////////////////////////////////////////////////////
}
