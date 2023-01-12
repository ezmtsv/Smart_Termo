package com.ez.termostat_v2;

import android.os.Environment;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import static android.os.ParcelFileDescriptor.MODE_WORLD_READABLE;
/*
Обмен по TCP IP

Для обмена используем массивы символов bufTCPout и bufTCPin, клиент выступает инициатором обмена.
Для Клиента:
bufTCPout
    Первым байтом --,
    вторым подтверждение принятой посылки - 'O',
    третий - команда,
        'T' - запрос телеметрии
        'S' - команда установки температуры
        'L' - команда выслать лог за последние 2 суток
        'D' - команда выслать лог за дату
    46-47 - номер текущего запроса
    байты с 50 по 63 - текущая дата
bufTCPin
    Первый байт --,
    вторым подтверждение принятой посылки - 'O',

    4 - статус "ночной-дневной режим"
    5 - гистерезис между ночной и дневной температурой
    с 15 по 21 - температура воздуха
    с 22 по 28 - температура теплоносителя
    29 - статус работы котла
    30 - статус питания
    31 - заряд аккумулятора сервера
    32 - часы для времени включения режима НОЧЬ
    33 - минуты для времени включения режима НОЧЬ
    34 - часы для времени включения режима ДЕНЬ
    35 - минуты для времени включения режима ДЕНЬ
    36 - статус работы ГАЗ/Электро
    37 - статус занятости термостата
    38 - 64 - данные лога за последние сутки
        38-39 - время за которое составлен отчет (часы, минуты)
        40-41 - время работы котла за отчетный период (часы, минуты)
        42-43 - время отсутствия питания за расчетный период (часы, минуты)
        44-45 - резерв?
        46-47 - номер последнего запроса
        48 - выставленная температура
        49 - статус USB
        с 50 по 63 - текущая дата


Для Сервера
 bufTCPout
    Первым байтом идет номер запроса,
    вторым подтверждение принятой посылки - 'O',
    третий - команда,
        'W' - штатный рабочий режим
        'A' - тревога

    4 - статус "ночной-дневной режим"
    5 - гистерезис между ночной и дневной температурой
        48 - параметр воздух (штатный режим W, тревога - A)
        49 - параметр вода (штатный режим W, тревога - A)
        50 - параметр питание (штатный режим W, тревога - A)
        51 - параметр резерв (штатный режим W, тревога - A)
            с 15 по 21 - температура воздуха
        с 22 по 28 - температура теплоносителя
        29 - статус работы котла
        30 - статус питания
        31 - заряд аккумулятора сервера
        32 - часы для времени включения режима НОЧЬ
        33 - минуты для времени включения режима НОЧЬ
        34 - часы для времени включения режима ДЕНЬ
        35 - минуты для времени включения режима ДЕНЬ
        36 - статус работы ГАЗ/Электро
        37 - статус занятости термостата
bufTCPin
    Первым байтом идет номер запроса,
    вторым подтверждение принятой посылки - 'O',
    третий - команда,
        'T' - запрос телеметрии
        'S' - команда установки температуры
        'L' - команда выслать лог за последние 2 суток
        'D' - команда выслать лог за дату
 */
public class handler_data {
    int [] dataINint = new int[64];
    String FILENAME_SD = "Log_termo.txt";
    String  DIR_SD = "TERMOSTAT_LOG";
    String tag = "TAG";

    Double koefB = 0.0;
    Double koefK = 0.0;
    Double rezT;
    Double rezADC;
//    MainActivity mainobj;

    int count = 0;
    handler_data(int [] data){
        int tmp;
        for (int tmpdata:data){                // инициализация массива dataIN значениями массива data, аналог цикла foreach
            dataINint[count] = tmpdata;
            count++;
        };
//        Log.d(tag, "bufIN "+dataINchar[2]+" : "+dataINchar[3]+" : "+dataINchar[4]+" : "+dataINchar[5]+" : "+dataINchar[8]+" : "+dataINchar[9]+" : "+dataINchar[10]+" : "+dataINchar[11]);
    }
    /////////////////////////////////////////////////////
    /* Метод расчитывает температуру по полученным от МК данным АЦП и коэф. К и В
    Функция вида y(данные от АЦП) = K*x(температура) + B
    * */
    protected String [] real_temp(){
        String [] rltemp = new String[2];
        /*
        int kK, kB, tempA, tempW;

        kK = dataINint[8]| (dataINint[9]<<8)|(dataINint[10]<<16)|(dataINint[11]<<24) ;      //получаем из массива dataIN калибровочные коэффициенты
        kB = dataINint[12]| (dataINint[13]<<8)|(dataINint[14]<<16)|(dataINint[15]<<24) ;    //получаем из массива dataIN калибровочные коэффициенты
        tempA = dataINint[22]|(dataINint[23]<<8);                                             //получаем из массива dataIN температуру воздуха
        tempW = dataINint[4]|(dataINint[5]<<8);                                             //получаем из массива dataIN температуру воды
////////////////////////////////////получение знаков коэф.///////
        if((dataINint[16] & 0x01)!=0) { kK = kK*-1;}
        if((dataINint[16] & 0x02)!=0) { kB = kB*-1;}
/////////////////////////////////////////////////////////////////
        koefB = ((double)kB)/100.0; koefK = ((double)kK)/100.0;
        Log.d(tag, "koefK, koefB " + koefK+ " : "+ koefB);
        rezADC = ((double)tempA);                                                                // для воздуха сохраняем данные в rezADC
        rezT = (rezADC - koefB)/koefK;
        rltemp[0] = two_symbol_after_point(Double.toString(rezT));                          //

        rezADC = ((double)tempW);                                                                  // для воды сохраняем данные в rezADC
        rezT = (rezADC - koefB)/koefK;
        rltemp[1] = two_symbol_after_point(Double.toString(rezT));
*/
        int A1, A2, A3, A4, tempA, tempW, A1E, A2E, A3E, A4E;
        double A1doub, A2doub, A3doub, A4doub, tempAdoub, tempWdoub;
        //получаем из массива dataIN калибровочные коэффициенты и данные АЦП
        A1 = dataINint[8]| (dataINint[9]<<8)|(dataINint[10]<<16);
        A2 = dataINint[12]| (dataINint[13]<<8)|(dataINint[14]<<16);
        A3 = dataINint[26]| (dataINint[27]<<8)|(dataINint[28]<<16);
        A4 = dataINint[30]| (dataINint[31]<<8)|(dataINint[32]<<16);
        ////////////////////////////////////получение знаков коэф.///////
        if((dataINint[16] & 0x01)!=0) { A1 = A1*-1;}
        if((dataINint[16] & 0x02)!=0) { A2 = A2*-1;}
        if((dataINint[16] & 0x04)!=0) { A3 = A3*-1;}
        if((dataINint[16] & 0x08)!=0) { A4 = A4*-1;}
////////////////////////////////////получение знаков и порядка степени ///////
        A1E = dataINint[11]; if((A1E & 0x80)!=0){ A1E = (0x7f & A1E)*-1;}
        A2E = dataINint[15]; if((A2E & 0x80)!=0){ A2E = (0x7f & A2E)*-1;}
        A3E = dataINint[29]; if((A3E & 0x80)!=0){ A3E = (0x7f & A3E)*-1;}
        A4E = dataINint[33]; if((A4E & 0x80)!=0){ A4E = (0x7f & A4E)*-1;}

        tempA = dataINint[22]|(dataINint[23]<<8);                                               //получаем из массива dataIN температуру воздуха
        tempW = dataINint[4]|(dataINint[5]<<8);                                                 //получаем из массива dataIN температуру воды


        A1doub = (double)(A1)/100.0; A2doub = (double)(A2)/100.0; A3doub = (double)(A3)/100.0; A4doub = (double)(A4)/100.0;
        tempAdoub = A1doub*Math.pow(10, A1E)*Math.pow(tempA, 3)+A2doub*Math.pow(10, A2E)*Math.pow(tempA, 2)+A3doub*Math.pow(10, A3E)*tempA+A4doub*Math.pow(10, A4E);
        rltemp[0] = two_symbol_after_point(Double.toString(tempAdoub));

        tempAdoub = A1doub*Math.pow(10, A1E)*Math.pow(tempW, 3)+A2doub*Math.pow(10, A2E)*Math.pow(tempW, 2)+A3doub*Math.pow(10, A3E)*tempW+A4doub*Math.pow(10, A4E);
        rltemp[1] = two_symbol_after_point(Double.toString(tempAdoub));
/////////////////////////debug/////////////////
//        Log.d(tag, "A1doub: "+A1doub+" A2doub: "+A2doub+" A3doub: "+A3doub+" A4doub: "+A4doub+" A4: " +A4);
//        Log.d(tag, "tempAdoub: "+tempAdoub);
/////////////////////////////////////////////////////////////////
        return rltemp;                                                                   // возвращаем массив с температурами воды и воздуха
    }
    ////////////////////////получение системной даты и времени///////////////////
    String cur_data(){
        Date curTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy  HH:mm");  // задаем формат даты
        String sdt_= sdf.format(curTime);
//        txt_dev.setText(sdt_);
        return sdt_;
    }
    /////////////////////////////////////запись и чтение файлов на SD карте
    void writeFileSD(String str, String name) {
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.d(tag, "SD-карта не доступна: " + Environment.getExternalStorageState());
            return;
        }
        // получаем путь к SD
        File sdPath = Environment.getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
        // создаем каталог
        sdPath.mkdirs();
        Log.d(tag, "Directory "+sdPath);
        // формируем объект File, который содержит путь к файлу
        FILENAME_SD = name+"_Log_termo.txt";
        File sdFile = new File(sdPath, FILENAME_SD);
        try {
            // открываем поток для записи
            BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile, true));
//////////////////////////////
            //если есть данные в файле, то они будут затерты при
            //BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
            // и далее bw.write(str);
            //если нужно дописать файл, то используем BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile, true));
            // и далее bw.append(str);
//////////////////////////////
            // пишем данные
//            bw.write(str);
            bw.append(str);
            // закрываем поток
            bw.close();
            Log.d(tag, "Файл записан на SD: " + sdFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(tag, "Файл не записан!");
        }
    }

    String readFileSD() {
        String str = "";
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.d(tag, "SD-карта не доступна: " + Environment.getExternalStorageState());
        }
        // получаем путь к SD
        File sdPath = Environment.getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, FILENAME_SD);
        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new FileReader(sdFile));
            // читаем содержимое
            String strr = "";
            while ((strr = br.readLine()) != null) {
                //               Log.d(tag, str);
                if(strr.equals(";")) str = str+"\n";
                str = str  + strr;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
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

}
