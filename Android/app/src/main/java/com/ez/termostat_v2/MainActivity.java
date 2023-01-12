package com.ez.termostat_v2;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

import static android.net.wifi.SupplicantState.COMPLETED;
import static android.os.AsyncTask.Status.FINISHED;
import static android.os.AsyncTask.Status.PENDING;
import static android.util.Base64.DEFAULT;
import static android.util.Base64.NO_WRAP;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{
    private int progress = 0;
    private ImageView prg_img;
    private View item_img = null;
    TextView daybtn;
    TextView nightbtn;
    private TextView tmpset;
    private TextView status;
    private TextView txt_dev;
    private TextView IP_adr;
    private TextView Status_heat;
    private TextView tmp_air;
    private TextView tmp_water;
    private TextView tmp_out;
    private TextView tmp_boiler;
    private TextView mode_heart_color;
    private TextView txtGAS_EL;
    private TextView txt_gist_B;
    private RadioButton rdBut;
    private LinearLayout ml;
    private LinearLayout ml_bar;
    private LinearLayout lay_stat;
    private RelativeLayout lay_stat_soscet;
    private RelativeLayout lay_tmpset;
    private ImageView menu;
    private ImageView fon_air;
    private ImageView fon_watter;
    private ImageView fon_out;
    private ImageView fon_boiler;
    private ImageView fon_progress_img;
    private ImageView but_stat_img;
    private ImageView butback_stat_img;

    ListView lst_ipadr;
    ArrayAdapter adapter_for_nameIP;

    static ProgressBar progressBar;

    int eX, eY, poseY, width, hight, scalegr, step, step_boiler;
    int bufferDataLength;
    int count_wait_send_com = 0;
    int port_int;
    static int count_req = 1;
    int count_start_req = 0;
    int count_lost_connect = 0;
    static int count_bad_answ = 0;          // счетчик неуспешно отправленных запросов
    static int count_change_set_tmp = 0;    // счетчик задающий паузу в отбражении температуры установленной сервером
    static int count_wait_answer = 0;       // счетчик ожидания ответа от сервера
    final int OKK = 0x55; //
    final int NNO = 0x33; //
    static int busy;
    int step_progr = 0;
    int cnt_wifi_status = 0;

    boolean rdbutCheck = false;
    boolean cmd_menu = false;
    boolean connect_server = false;
    boolean touchFlag = false;
    static boolean sync_Flag_set_tmp = true;
    static boolean data_read = false;
    boolean mRun = true;
    static boolean mode_tarif = false;
    static boolean req_OK = false;         // флаг успешного ответа сервера
    static boolean change_tmp = false;     // флаг изменения установленной температуры
    static int wait_answer_server = 0;
    static boolean thr_start = false;
    boolean new_IP_found;
    boolean flag_sel_HEATER1;
    boolean flag_long_IP = false;

    static float scale_X;
    static float scale_Y;

    static String tim_GIST;
    static String stat_info_prog;

    tcp_client clientTCP;
    Timer timer;
    TimerTask mTimerTask;
    Timer timer2;
    TimerTask timereq2;

    String tag = "TAG";
    private String mServerMessage = "";
    static String status_SERV = "status_SERVER";
    String [] str_tmp = new String[2];
    String port, SERVER_IP;
    String SERVER_IP_tmp;
    String cur_data_cl;
    String name_SSID = "";
    String pass_SSID = "";
    String name_SSID_answMC = "";
    String pass_SSID_answMC = "";
    String str_mess = "";
    String heater2 = "эл. котёл";
    String heater1 = "газ. котёл";
    Telemetry tele;
    wifi_con WIFI_obj;

/////+CIFSR:STAIP,"172.18.42.156"
    static char[] bufTCPout = new char[192];
    static char[] bufTCPin = new char[192];
    static char[] debug_char_dim = {'d', 'e', 'b', 'a', 'g'};
    //////////////////////////////////////////////////////
/*    final static char req_data_serv = 'T';  // - запрос телеметрии
    final static char set_temp = 'S';       // - команда установки температуры
    final static char set_gisteresis = 'G';       // - команда установки ночного и дневного режимаб
    final static char OK = 'O';             // - подтверждение принятия данных
    final static char NO = 'N';
    final static char reload_serv = 'R';  // - перезагрузка
    final static char set_tmp_async = 'A';  // - установка температуры без подачи команды термостату
    static char command;*/
    //////////////////////////////////////////////////////
    static int time_NIGHT = 23*60*60;   // для передачи МК
    static int time_DAY = 7*60*60;      // для передачи МК
    static int work_GAS;
    static int status_mode_GAS = 0x33;
    static int work_TARIF_;
    static int work_TARIF = 0x33;
    static int SECOND_;
    static int gisteresis_TMP_;
    static int gisteresis_TMP;
    static int gisteresis_TARIF_;
    static int gisteresis_TARIF;
    static int count_COMAND;
    static int COMAND;
    static int delta_tmp_;
    static int delta_tmp_BOILER;
    static int gist_tmp_BOILER;
    static int set_tmpSRV;
    static int time_Night_int;  // для получения от МК
    static int time_Day_int;    // для получения от МК
    static int tmp_W;
    static int tmp_A;
    static int tmp_O;
    static int tmp_B;
    static int time_powOFF_int = 0;
    static int time_heat_int = 0;
    static int set_tmp_serv = -1;
    static int  air_tmp_int;
    static int  water_tmp_int;
    static int count_cmd_MK = 0;                                    // счетчик команд отправленных МК
    static int count_cmd_repeat_MK = 0;                             // счетчик повторных команд
    static int[] sys_DATA = new int[4];
    static int cmd_send;
    static int  status_busy = 0x33;
    static int sec_MC = 0;
    static int set_tmp_serv_prev = -1;
    static int set_tmp_boiler_prev = -1;
    static int cmd_success;
    int count_dialogND = 0;
    int myHourD = 7;
    int myMinuteD = 0;
    int myHourN = 23;
    int myMinuteN = 0;
    int [] coeff_resist_from_tmp = new int[10];
    int time_toch_menu = 0;
    int num_save_ip;
    int count_load_def = 0;
    int visible_set_TMP = 0;            // 1 - отображается бар настройки температуры воздуха, 2 - настройка температуры воды бойлера
    int cnt_0_25_sec = 0;
    final int net_NOT_found = 0;
    final int net_dimmer_NOT_found = 1;
    final int wait_scan_net = 2;
    boolean flag_toch_menu = false;

    static char status_heat = 'N';
    static char status_pow = 'N';

    static String tmp_debag;
    static String time_Night;
    static String time_Day;
    String netSSID_cur = "";

    final int req_data = 0x34;              //запрос телеметрии                                 ////десятич.с. 52
    final int set_tmp = 0x44;               //установить температуру  воздуха                   ////десятич.с. 68
    final int set_tmpBR = 0x20;             //установить температуру воды в бойлере             ////десятич.с. 32
    final int set_ONOFFboiler = 0x22;       // Включить/Выключить бойлер
    final int set_ONOFF_AlarmHeat = 0x23;   // Аварийное откл. нагрева
    final int synchro = 0x64;               // синхронизировать время                           ////десятич.с. 100
    final int synchro_timeTARIF = 0x67;     // синхронизировать время работы по тарифу          ////десятич.с. 103
    //    final int no_req_data1 = 0x83;    //выдавать показания без запроса каждую секунду     ////десятич.с. 131
//    final int no_req_data15 = 0x84;       //выдавать показания без запроса каждые 15сек.      ////десятич.с.
    final int set_koef = 0x93;              //принять и сохранить коэффициенты калибр.          ////десятич.с. 147
    final int  set_GAS	= 0x75;             // выбор режима работы газ-эл.                      ////десятич.с. 117
    final int  set_work_TARIF = 0x78;       // вкл.-выкл. режима работы день-ночь               ////десятич.с. 120
    static int cool_modeON =			    0x39;     // вкл. режим охлажд.                     ////десятич.с. 57
    static int cool_modeOFF	=		        0x40;     // выкл. режим охлажд.                    ////десятич.с. 64
    static int set_link	=		            0x11;     // применить настройки сети
    static int config_mail	=		        0x12;     // настройки эл. почты
    final int load_def  =                   0x19;
    private static final int MY_REQUEST_CODE = 123;

    static boolean cool_ON = false;
    static boolean flag_boiler = false;
    static boolean flag_boiler_out = false;
    static boolean flag_AlarmOFF_out = false;
    static boolean flag_AlarmOFF = false;
    static boolean flag_calibrovka_run = false;
    static boolean initESP_ON = false;
    int netID;
    //////////////////////////////////////////////////////debug
    char chR = 128;
    //////////////////////////////////////////////////////debug
    SharedPreferences sPref;
    protected PowerManager.WakeLock mWakeLock;
    final Context cntx = this;
    private final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    BroadcastReceiver wifi_BroadcastReceiver;
    ArrayList<String> name_adr;
    //List<String> name_adr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ml = (LinearLayout) findViewById(R.id.ml);
        lay_tmpset = (RelativeLayout) findViewById(R.id.lay_tmpset);
        lay_stat_soscet = (RelativeLayout) findViewById(R.id.lay_stat_soscet);
        lay_stat = (LinearLayout) findViewById(R.id.lay_stat);
        ml_bar = (LinearLayout) findViewById(R.id.ml_bar);
        tmpset = (TextView)findViewById(R.id.tmpset);
        tmp_air = (TextView)findViewById(R.id.tmp1);
        tmp_water = (TextView)findViewById(R.id.tmp2);
        tmp_out = (TextView)findViewById(R.id.tmp3);
        tmp_boiler = (TextView)findViewById(R.id.tmp4);
        status = (TextView)findViewById(R.id.status_txt);
        txt_dev = (TextView)findViewById(R.id.txt_dev);
        mode_heart_color  = (TextView)findViewById(R.id.mode_heart_color);
        Status_heat= (TextView)findViewById(R.id.status_mode);
        IP_adr = (TextView)findViewById(R.id.ip_adr);
        txt_gist_B = (TextView) findViewById(R.id.txt_gist_B);
        prg_img = (ImageView) findViewById(R.id.progress_img);
        but_stat_img = (ImageView) findViewById(R.id.but_stat_img);
        butback_stat_img = (ImageView) findViewById(R.id.butback_stat_img);
        fon_progress_img = (ImageView) findViewById(R.id.fon_progress_img);
        txtGAS_EL = (TextView)findViewById(R.id.txt_gas_el);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        rdBut = (RadioButton)findViewById(R.id.radio_btn);

        fon_air = (ImageView)findViewById(R.id.fon_air);
        fon_watter = (ImageView)findViewById(R.id.fon_watter);
        fon_out = (ImageView)findViewById(R.id.fon_out);
        fon_boiler = (ImageView)findViewById(R.id.fon_boiler);


        menu = (ImageView)findViewById(R.id.menu);
        menu.setOnTouchListener(this);
        fon_air.setOnTouchListener(this);
        fon_boiler.setOnTouchListener(this);
        fon_out.setOnTouchListener(this);
        fon_watter.setOnTouchListener(this);
        but_stat_img.setOnTouchListener(this);
        butback_stat_img.setOnTouchListener(this);
        prg_img.setOnTouchListener(this);
        IP_adr.setOnLongClickListener(clickIP);
        txtGAS_EL.setOnLongClickListener(clickHeater);

               /* This code together with the one in onDestroy()
         * will make the screen be always on until this Activity gets destroyed. */
        Log.d(tag, "mWakeLock = "+ mWakeLock );
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag);
        this.mWakeLock.acquire();
        Log.d(tag, "mWakeLock = "+ mWakeLock );
        ///////////////////////////////////////////
        /*
        netSSID_cur = getCurrentSsid(this);                                     // получаем имя сети
        try {
            char[] tmp_char = new char[netSSID_cur.length() - 2];                   // объявляем массив символов длиной netSSID_cur.length()-2
            netSSID_cur.getChars(1, netSSID_cur.length() - 1, tmp_char, 0);           // копируем имя сети в массив символов исключая 1 и последний символы(кавычки)
            String tmp_str = new String(tmp_char);                                  // инициализируем новую строку элементами массива символов
            netSSID_cur = tmp_str;
            Log.d(tag, "name SSID " + netSSID_cur);
        }catch(Exception tt){  Log.d(tag, "NOt connection WIFI! "); netSSID_cur = null; }
        Log.d(tag, "netSSID_cur "+netSSID_cur);*/
/*        ///////////////////Для получения инфо о подключенной сети WIFI
        wifi_BroadcastReceiver = new WIFI_BroadcastReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION); //.NETWORK_IDS_CHANGED_ACTION);   //.NETWORK_STATE_CHANGED_ACTION
        this.registerReceiver(wifi_BroadcastReceiver, filter);*/
        //////////////////////////////////////////
        //////////////////////////////////////////
// получение ширины текущего разрешения
        WindowManager w = getWindowManager();// объект менеджер окна
        Display d = w.getDefaultDisplay();
        width = d.getWidth();
        hight = d.getHeight();
        Log.d(tag, "Screen width "+width);
        Log.d(tag, "Screen hight "+hight);
        step = width/45;                    // вычисление размера 1 градуса в пикселях, всего 50гр.(от 5 до 50)
        step_boiler = width/65;
        double tmp_X, tmp_Y;
        tmp_X = (double)width;
        tmp_Y = (double)hight;
        scale_X = (float)(tmp_X/1080);
        scale_Y = (float)(tmp_Y/1920);

//        ml.setBackground(createLayerDrawable(R.drawable.termo_fon, 1, 1)); /// установка бэкграунда нужного разрешения termo_fon_light
        ml.setBackground(createLayerDrawable(R.drawable.new_fon, 1, 1)); ///
        ml_bar.setBackground(createLayerDrawable(R.drawable.new_bar_m, 1, (float)0.072)); /// установка бэкграунда нужного разрешения ( по горизонтали вместо 1 было 0,12, размер бэкграунда сохранялся при этом, но картинка сильно искажалась)
 //       ml_bar.setBackgroundResource(R.drawable.bar_m); /// установка бэкграунда нужного разрешения (слабые девайсы при этом могут тормозить)
        menu.setImageDrawable(createLayerDrawable(R.drawable.menu_pic, (float)0.12, (float)0.072));

        fon_air.setImageDrawable(createLayerDrawable(R.drawable.fon_air, (float)0.45, (float)0.25)); /// коэфф. умножается на текущее разрешение по X, Y. т.е. дополнитильный масштабирующий коэф. вводить смысла нет. (0.45 * 1080, 0.25 * 1920)
        fon_watter.setImageDrawable(createLayerDrawable(R.drawable.fon_watter, (float)0.45, (float)0.25));
        fon_out.setImageDrawable(createLayerDrawable(R.drawable.fon_out, (float)0.45, (float)0.25));
        fon_boiler.setImageDrawable(createLayerDrawable(R.drawable.fon_boiler, (float)0.45, (float)0.25));
        but_stat_img.setImageDrawable(createLayerDrawable(R.drawable.but_stat, (float)0.73, (float)0.05));
        butback_stat_img.setImageDrawable(createLayerDrawable(R.drawable.but_stat, (float)0.73, (float)0.05));

        set_pos_but(fon_air, 60*scale_X, 80*scale_Y);
        set_pos_but(fon_out, 553*scale_X, 80*scale_Y);
        set_pos_but(fon_watter, 60*scale_X, 567*scale_Y);
        set_pos_but(fon_boiler, 553*scale_X, 567*scale_Y);

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  // убирание системной строки с экрана
        tmpset.setTextSize(TypedValue.COMPLEX_UNIT_PX, 80 * scale_Y);            // размер текста в пикселях
        IP_adr.setTextSize(TypedValue.COMPLEX_UNIT_PX, 54 * scale_Y);            // размер текста в пикселях
        status.setTextSize(TypedValue.COMPLEX_UNIT_PX, 56 * scale_Y);            // размер текста в пикселях
        txtGAS_EL.setTextSize(TypedValue.COMPLEX_UNIT_PX, 56 * scale_Y);            // размер текста в пикселях
        txt_gist_B.setTextSize(TypedValue.COMPLEX_UNIT_PX, 56 * scale_Y);            // размер текста в пикселях

        tmp_air.setTextSize(TypedValue.COMPLEX_UNIT_PX, 142 * scale_X);            // размер текста в пикселях
        tmp_water.setTextSize(TypedValue.COMPLEX_UNIT_PX, 142 * scale_X);            // размер текста в пикселях
        tmp_out.setTextSize(TypedValue.COMPLEX_UNIT_PX, 142 * scale_X);            // размер текста в пикселях
        tmp_boiler.setTextSize(TypedValue.COMPLEX_UNIT_PX, 142 * scale_X);            // размер текста в пикселях
        Status_heat.setTextSize(TypedValue.COMPLEX_UNIT_PX, 68 * scale_X);            // размер текста в пикселях
        txt_dev.setTextSize(TypedValue.COMPLEX_UNIT_PX, 52 * scale_X);            // размер текста в пикселях
        mode_heart_color.setTextSize(TypedValue.COMPLEX_UNIT_PX, 46 * scale_X);            // размер текста в пикселях

        set_pos_but(tmp_air, 84*scale_X, 364*scale_Y);
        set_pos_but(tmp_water, 84*scale_X, 847*scale_Y);
        set_pos_but(tmp_out, 578*scale_X, 364*scale_Y);
        set_pos_but(tmp_boiler, 578*scale_X, 847*scale_Y);
        set_pos_but(butback_stat_img, 160*scale_X, 470*scale_Y);

        if (width == 1080 && hight > 2100 && hight < 2200) {
            set_pos_but(Status_heat, 280*scale_X, 200*scale_Y);
            set_pos_but(IP_adr, 180*scale_X, 170*scale_Y);
            set_pos_but(status, 180*scale_X, 235*scale_Y);
            set_pos_but(txtGAS_EL, 180*scale_X, (float)30*scale_Y);
            set_pos_but(but_stat_img, 160*scale_X, 350*scale_Y);
        } else {
            set_pos_but(Status_heat, 280*scale_X, 160*scale_Y);
            set_pos_but(IP_adr, 260*scale_X, 140*scale_Y);
            set_pos_but(status, 260*scale_X, 190*scale_Y);
            set_pos_but(txtGAS_EL, 260*scale_X, 1*scale_Y);
            set_pos_but(but_stat_img, 160*scale_X, 320*scale_Y);
        }

        prg_img.setImageDrawable(createLayerDrawable(R.drawable.progress, (float)0.89, (float)0.04));
        fon_progress_img.setImageDrawable(createLayerDrawable(R.drawable.fon_progress, (float)0.89, (float)0.04));

        set_pos_but(tmpset, 70*scale_X, 10*scale_Y);
        set_pos_but(prg_img, 70*scale_X, 150*scale_Y);
        set_pos_but(fon_progress_img, 70*scale_X, 150*scale_Y);


        set_pos_but(rdBut, 50*scale_X, 20*scale_Y);
        set_pos_but(mode_heart_color, 170*scale_X, 30*scale_Y);
        set_pos_but(txt_gist_B, 280*scale_X,60*scale_Y);




        set_pos_but(progressBar, 10*scale_X, 110*scale_Y);
        set_pos_but(txt_dev, 80*scale_X, 1*scale_Y);

//        prg_img.setVisibility(View.GONE);
//        fon_progress_img.setVisibility(View.GONE);
//        tmpset.setVisibility(View.GONE);
        lay_tmpset.setVisibility(View.GONE);
        lay_stat.setVisibility(View.GONE);
        txt_gist_B.setVisibility(View.GONE);
        txtGAS_EL.setHintTextColor(getResources().getColor(R.color.default_));

        //DEBUG
//        lay_stat_soscet.setVisibility(View.GONE);
//        lay_stat.setVisibility(View.VISIBLE);
//        butback_stat_img.setImageDrawable(createLayerDrawable(R.drawable.butback_stat, (float)0.85*scale_Y, (float)0.06*scale_X));

        ///

/////////////////////////////////////////.................
        eX = 11*step;
        showtmp.sendEmptyMessage(0);       // инициализация 16 градуса
        // обработка события касания экрана ..........
        View root = findViewById(android.R.id.content).getRootView();
        root.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                    System.err.println("Display If  Part ::->" + touchFlag);
                if(touchFlag) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.d(tag, "onTouch DOWN oncreate");
                            eY = (int) event.getY();
                            poseY = eY;
                            switch (item_img.getId()) {
                                case R.id.menu:
                                    time_toch_menu = 0;
                                    flag_toch_menu = true;
                                    Log.d(tag, "PUSH menu!!!");
                                    showPopupMenu(item_img);                /// вывод меню
                                    break;
                                case R.id.fon_air:
                                    fon_air.setImageDrawable(createLayerDrawable(R.drawable.fon_air_press, (float) 0.45, (float) 0.25));
                                    break;
                                case R.id.fon_boiler:
                                    fon_boiler.setImageDrawable(createLayerDrawable(R.drawable.fon_boiler_press, (float) 0.45, (float) 0.25));
                                    break;
                                case R.id.fon_out:
                                    fon_out.setImageDrawable(createLayerDrawable(R.drawable.fon_out_press, (float) 0.45, (float) 0.25));
                                    break;
                                case R.id.fon_watter:
                                    fon_watter.setImageDrawable(createLayerDrawable(R.drawable.fon_watter_press, (float) 0.45, (float) 0.25));
                                    break;
                                case R.id.but_stat_img:
                                    but_stat_img.setImageDrawable(createLayerDrawable(R.drawable.butback_stat, (float) 0.73, (float) 0.05));
                                    break;
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            eX = (int) event.getX();
                            eY = (int) event.getY();
//                            Log.d(tag, "MOVE X = "+eX + "MOVE Y = "+ eY);
                            if (item_img.getId() == R.id.progress_img) showtmp.sendEmptyMessage(0);

                            break;
                        case MotionEvent.ACTION_UP:
                            Log.d(tag, "onTouch UP oncreate");

                            if (item_img.getId() == R.id.progress_img) {
                                change_tmp = true;
                                if (busy != OKK) {
                                    //                                       dialog_show(5);             // установка нового значения температуры
                                    /////////////////////////
                                    showtmp.sendEmptyMessage(0);
                                    /////////////////////////
                                } else {
                                    show_txt_toast("Устройство занято отработкой команды!");
                                }
                            }

                            switch (item_img.getId()) {
                                case R.id.fon_air:
                                    fon_air.setImageDrawable(createLayerDrawable(R.drawable.fon_air, (float) 0.45, (float) 0.25));
                                    if (visible_set_TMP == 2) visible_set_TMP = 0;
                                    show_bar_set_tmp(visible_set_TMP + 2);
                                    break;
                                case R.id.fon_boiler:
                                    fon_boiler.setImageDrawable(createLayerDrawable(R.drawable.fon_boiler, (float) 0.45, (float) 0.25));
                                    if (visible_set_TMP == 1) visible_set_TMP = 0;
                                    show_bar_set_tmp(visible_set_TMP + 4);
                                    break;
                                case R.id.fon_out:
                                    fon_out.setImageDrawable(createLayerDrawable(R.drawable.fon_out, (float) 0.45, (float) 0.25));
                                    //wifi_conn_reconn("Smart_Home_EZ", "open", "open");
                                    //getCurrentSsid(cntx);
                                    //wifi_reconnect_net(cntx, "Smart_Home_EZ", "open");
                                    break;
                                case R.id.fon_watter:
                                    fon_watter.setImageDrawable(createLayerDrawable(R.drawable.fon_watter, (float) 0.45, (float) 0.25));
                                    //wifi_conn_reconn("KeeneticWIFI", "TuskaR@7", "open");
                                    break;
                                case R.id.but_stat_img:
                                    lay_stat_soscet.setVisibility(View.GONE);
                                    lay_stat.setVisibility(View.VISIBLE);
                                    but_stat_img.setImageDrawable(createLayerDrawable(R.drawable.but_stat, (float) 0.73, (float) 0.05));
                                    break;
                            }
                            touchFlag = false;
                            flag_toch_menu = false;
/////////////////////////////////////////////////
/////////////////////////////////////////////////
                            break;
                    }
                }
                return true;
            }
        });
////////////////////////////инициализация подключения TCP ip

        SERVER_IP = read_config_str("server_IP");
        port = read_config_str("server_port");
//        if(SERVER_IP.equals("")){SERVER_IP = "192.168.4.1";}
        if(SERVER_IP.equals("")){SERVER_IP = "192.168.4.1";}
        port = "8558";
//        SERVER_IP = "192.168.4.1";  ///DEBUG
        port_int = Integer.parseInt(port);
        clientTCP = new tcp_client(SERVER_IP, port_int);
        IP_adr.setText("IP server "+SERVER_IP);
        count_req = read_config_int("num_req");
        /////////////////////////
        cur_data_cl = cur_data();
        Log.d(tag, cur_data_cl);
        num_save_ip = read_config_int("num_save_ip");
        Log.d (tag, "new num_save_ip "+num_save_ip+" server_IP "+SERVER_IP);
        ////////////////////////
        /*
        String heat_tmp = read_config_str("Name_heater1");
        if(heat_tmp!= ""){ heater1 =  heat_tmp ;}
        heat_tmp = read_config_str("Name_heater2");
        if(heat_tmp!= ""){ heater2 = heat_tmp;}*/
        String heat_tmp = read_config_str(SERVER_IP+"heater1");
        if(heat_tmp!= ""){ heater1 =  heat_tmp ;}
        heat_tmp = read_config_str(SERVER_IP+"heater2");
        if(heat_tmp!= ""){ heater2 =  heat_tmp ;}
        ///////////////////////
        Timer tim_one;
        TimerTask tmr_one_task;
        tim_one = new Timer();
        tmr_one_task = new TMR_one_t();
        try{tim_one.schedule(tmr_one_task, 3500); }catch(Exception tt){;}  // одноразовый запуск таймера через 3.5 сек
////////////////////////
//////////////////////////////////таймер, работает с TCP соединением
        timer = new Timer();
        mTimerTask = new MyTimerTask();
        try{timer.schedule(mTimerTask, 250, 250);}catch(Exception c){;}
////////////////////////////////////////////////////////////
        timer2 = new Timer();
        timereq2 = new TimerTask2();
        if(read_config_int("saved_show_help") ==0 ){try{timer2.schedule(timereq2, 2000);}catch(Exception cx){;} } // одноразовый запуск таймера через 2сек


//        Log.d(tag, "read saved_show_help = "+read_config_int("saved_show_help"));
////////////////////////////////////////////////////////////
        //command = req_data_serv;
        cmd_send = req_data;            // установка команды на запрос телеметрии

        for(int a = 0; a< bufTCPout.length; a++){ bufTCPout[a] = '_'; }
        /////////////////
        try {
            WIFI_obj = new wifi_con(cntx);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // 23
                int permission1 = ContextCompat.checkSelfPermission(cntx, Manifest.permission.ACCESS_COARSE_LOCATION);

                // Check for permissions
                if (permission1 != PackageManager.PERMISSION_GRANTED) {
                    Log.d(tag, "Requesting Permissions");
                    // Request permissions
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_WIFI_STATE,
                                    Manifest.permission.ACCESS_NETWORK_STATE
                            }, MY_REQUEST_CODE);
                    return;
                }
                Log.d(tag, "Permissions Already Granted");
            }
            WIFI_obj.askAndStartScanWifi();                 // сканируем существующие сети
        } catch(Exception e){Log.d(tag, "Exception WIFI_obj.askAndStartScanWifi() in onCreate");}
        /////////////////
    }
    ////////////////////////////onCreate END
    //////////////////////////////////////////////////
/*
    //////////////////////////////////////////
    public class WIFI_BroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            NetworkInfo nwInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            nwInfo.getState();

            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            try {
                List<WifiConfiguration> listOfConfigurations = wifiManager.getConfiguredNetworks();
                for (int index = 0; index < listOfConfigurations.size(); index++) {
                    WifiConfiguration configuration = listOfConfigurations.get(index);
                    if (configuration.networkId == info.getNetworkId()) {
                        netSSID_cur = configuration.SSID;
                        netSSID_cur = netSSID_cur.replace("\"", "");
                    //return configuration.SSID;
                        Log.d(tag, ", ssid " + netSSID_cur);
                    //return ssid;
                    }
                }
            } catch (Exception e) { Log.d(tag,"NetworkInfo ex:"+e); }
        }

    }
    */
    //////////////////////////////////////////

    void show_bar_set_tmp(int set_visible){
        switch (set_visible) {
            case 2:                 // включение бара настройки темп. воздуха
                lay_tmpset.setVisibility(View.VISIBLE);
                lay_stat_soscet.setVisibility(View.GONE);
                lay_stat.setVisibility(View.GONE);
                txt_gist_B.setVisibility(View.GONE);
                mode_heart_color.setVisibility(View.VISIBLE);
                rdBut.setVisibility(View.VISIBLE);
//                tmpset.setText("Уст. t˚C воздуха   +" + Integer.toString(scalegr));
                visible_set_TMP = 1;
                if (tele.set_tmp_serv != 0xff) set_tmp_serv_prev = tele.set_tmp_serv;
                eX = (set_tmp_serv_prev - 5) * step;
                showtmp.sendEmptyMessage(0);
                break;
            case 3:                 // выключение бара настройки темп. воздуха
                lay_tmpset.setVisibility(View.GONE);
                lay_stat_soscet.setVisibility(View.VISIBLE);
                visible_set_TMP = 0;
                break;
            case 4:                 // включение бара настройки темп. бойлера
                lay_tmpset.setVisibility(View.VISIBLE);
                lay_stat_soscet.setVisibility(View.GONE);
                lay_stat.setVisibility(View.GONE);
//                tmpset.setText("Уст. t˚C гор.воды   +" + Integer.toString(scalegr));
                mode_heart_color.setVisibility(View.GONE);
                rdBut.setVisibility(View.GONE);
                txt_gist_B.setVisibility(View.VISIBLE);
                visible_set_TMP = 2;
                if (tele.boiler_settmp != 0xff) set_tmp_boiler_prev = tele.boiler_settmp;
                eX = (set_tmp_boiler_prev - 5) * step_boiler;
                showtmp.sendEmptyMessage(0);
                break;
            case 6:                 // выключение бара настройки темп. бойлера
                lay_tmpset.setVisibility(View.GONE);
                txt_gist_B.setVisibility(View.GONE);
                lay_stat_soscet.setVisibility(View.VISIBLE);
                mode_heart_color.setVisibility(View.VISIBLE);
                rdBut.setVisibility(View.VISIBLE);
                visible_set_TMP = 0;
                break;
        }
    }



    void set_pos_but(View v, float x, float y){                 // если не выполнить эту функцию сначала, то функция v.set.X(float x) работает некорректно
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(

                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins((int)x,(int)y,0,0);
        //                       lp.setMargins(x, y, 0, 0);
        v.setLayoutParams(lp);
    }
   //////////////////////////////////////////////
    class TimerTask2 extends TimerTask {

        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                    "dd:MMMM:yyyy HH:mm:ss a", Locale.getDefault());
            final String strDate = simpleDateFormat.format(calendar.getTime());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                mCounterTextView.setText(strDate);
                    dialog_show(13);
                }
            });
        }
    }
//////////////////////////////////////////////
/*
    private String getCurrentSsid(Context context) {
        String ssid = "";
        String scan_ssid = "";
        try {

            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (networkInfo.isConnected()) {
                final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                final WifiInfo connectionInfo = wifiManager.getConnectionInfo();

                if (connectionInfo != null) {
                    ssid = connectionInfo.getSSID();
                    netID = connectionInfo.getNetworkId();
                    Log.d(tag, "netID = "+ netID);
 //                   Log.d(tag,"Name_ssid_connectionInfo: "+ connectionInfo.getSSID());
                }
                if (ssid.equals("<unknown ssid>")) {
                    List<WifiConfiguration> mScanResults = wifiManager.getConfiguredNetworks();
                    String[] list_ssid = new String[mScanResults.size()];
                    for (int x = 0; x < mScanResults.size(); x++) {
                        scan_ssid += mScanResults.get(x).SSID + "\n";       // сохраняем имена всех сетей в scan_ssid
                        list_ssid[x] = mScanResults.get(x).SSID;
                    }
                    ssid = list_ssid[netID];                            // имя подключенной сети в ssid
                }
            } else { ssid = null; }

        }catch(Exception e){ Log.d(tag, "Exception getCurrentSsid!"); ssid = null; }
        Log.d(tag, "Name_ssid netID = "+ netID);
        Log.d(tag, "Name_ssid_scan = "+ scan_ssid);
        Log.d(tag,"Name_ssid: "+ssid);
//        Log.d(tag,"Name_ssid: "+ssid); netID
//      /// получить список сохраненных сетей
//        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        String textStatus = "";
//        // List stored networks
//        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
//        int i = 1;
//        for (WifiConfiguration config : configs) {
//            textStatus+= "\n\n" + config.toString();
//            Log.d(tag,"\n from marakana "+i+": "+config.toString()); i++;
 //       }

////////////////


////////////////
        return ssid;
    }
*/
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchFlag = true;
                item_img = v;
                if (item_img.getId() == R.id.butback_stat_img) handlbuckstat();
                Log.d(tag, "onTouch DOWN_");
                break;
            case MotionEvent.ACTION_UP:

                Log.d(tag, "onTouch UP_");
                break;
            default:
                break;
        }
        return false;
    }
    //////////////////////////////////////////////////////////
    void handlbuckstat(){
        lay_stat_soscet.setVisibility(View.VISIBLE);
        lay_stat.setVisibility(View.GONE);
    }
////////////////////////////////////////////////////
    Handler showtmp = new Handler() {

        @Override
        public void handleMessage(Message msg) {
  /*
            int alpha = 100; int cmd_tmp = req_data;
            boolean flg_tcp_send = false;

            if((eY - poseY)<100&&(eY - poseY)>-100) {          // от точки прикосновения вверх и вниз будет работать только на 100 пикселей

                scalegr = 5 + eX / step;
                if (visible_set_TMP == 1) tmpset.setText("Уст. t˚C воздуха   +" + Integer.toString(scalegr));
                if (visible_set_TMP == 2) tmpset.setText("Уст. t˚C гор.воды   +" + Integer.toString(scalegr));
                alpha = 6 * scalegr;
                alpha = 300-alpha; if(alpha>255)alpha = 255;
                prg_img.setImageAlpha(alpha);
//                set_tmpSRV = set_tmp_serv;
                Log.d(tag, "mode show visible_FUNC");
/////////////////////////////////////////////////////////

                if(!touchFlag) {
                    if (visible_set_TMP == 1) {
                        set_tmp_serv = scalegr;
                        if (set_tmp_serv > set_tmp_serv_prev) {
                            delta_tmp_ = set_tmp_serv - set_tmp_serv_prev;
                            if (sync_Flag_set_tmp) {
                                cmd_tmp = inc_t;
                            } else {
                                cmd_tmp = async_set_TMP_IN;
                            }
                            flg_tcp_send = true;
                            //                       show_txt_toast("INCREMENT - " + delta_tmp_ + " : " + set_tmp_serv + " : " + set_tmp_serv_prev);
                        } else {
                            if (set_tmp_serv_prev > set_tmp_serv) {
                                delta_tmp_ = set_tmp_serv_prev - set_tmp_serv;
                                if (sync_Flag_set_tmp) {
                                    cmd_tmp = dec_t;
                                } else {
                                    cmd_tmp = async_set_TMP_DEC;
                                }
                                flg_tcp_send = true;
//                            show_txt_toast("DECREMENT - " + delta_tmp_ + " : " + set_tmp_serv + " : " + set_tmp_serv_prev);
                                //                           logD("DECREMENT - " + val_ + " : " + set_tmp_serv + " : " + set_tmp_serv_prev);
                            }
                        }
                        Log.d(tag, "предыдущая темп. " + set_tmp_serv_prev + ", новая темп." + set_tmp_serv);
                        set_tmp_serv_prev = set_tmp_serv;
//                if(!flg_tcp_send){ cmd_send = req_data; }       // сбрасываем команду на отправку
                        if (flg_tcp_send) {
                            cmd_send = cmd_tmp;
                        }       // сбрасываем команду на отправку
                        Log.d(tag, "change_tmp: " + change_tmp + " cmd_send: " + cmd_send);
                        cmd_success = set_tmp_serv;
                    }
                }

//////////////////////////////////////////////////////////
            }*/
            int alpha = 100;
//            int cmd_tmp = req_data;
//            boolean flg_tcp_send = false;

            if((eY - poseY)<100&&(eY - poseY)>-100) {          // от точки прикосновения вверх и вниз будет работать только на 100 пикселей
                if (visible_set_TMP == 1 || cmd_menu) {
                    scalegr = 5 + eX / step;
                    if (set_tmp_serv_prev == -1) tmpset.setText("Уст. t˚C воздуха   ... ");
                    else tmpset.setText("Уст. t˚C воздуха   +" + Integer.toString(scalegr));
                    alpha = 6 * scalegr;
                    alpha = 300-alpha; if(alpha>255)alpha = 255;
                    if (change_tmp) {
                        delta_tmp_ = scalegr;
                        cmd_send = set_tmp;
                        cmd_success = delta_tmp_;
                    }
                    set_tmp_serv_prev = scalegr;
                    cmd_menu = false;
                }
                if (visible_set_TMP == 2) {
                    scalegr = 5 + eX / step_boiler;
                    if(scalegr > 70) scalegr = 70;
                    if (set_tmp_boiler_prev == -1) tmpset.setText("Уст. t˚C гор.воды   ... ");
                    else tmpset.setText("Уст. t˚C гор.воды   +" + Integer.toString(scalegr));
                    alpha = (int)(4.64 * (double)scalegr);
                    alpha = 325-alpha; if(alpha>255)alpha = 255;
                    if (change_tmp) {
                        delta_tmp_BOILER = scalegr;
                        cmd_send = set_tmpBR;
                        cmd_success = delta_tmp_BOILER;
                    }
                    set_tmp_boiler_prev = scalegr;
                }
                prg_img.setImageAlpha(alpha);
//                Log.d(tag, "mode show visible_FUNC "+(eY - poseY));
//////////////////////////////////////////////////////////
            }
        }
    };
    //////////////////////////////////////////////////////////отключение-подключение к WIFI сети
    void off_wifi(Context context){
        try {
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiConfiguration wc = new WifiConfiguration();
/*
            wc.SSID = "\"SSIDName\"";
            wc.preSharedKey = "\"password\"";
            wc.hiddenSSID = true;
            wc.status = WifiConfiguration.Status.ENABLED;
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            */
            boolean b=wifi.isWifiEnabled();
            if(b){
                wifi.setWifiEnabled(false);
                Toast.makeText(context,"yes", Toast.LENGTH_SHORT).show(); //wifi.enableNetwork(1, true); getCurrentSsid(cntx);
                ////////////////////////////////////////debug
                ///////////////////////////////////////
            }else {

                wifi.setWifiEnabled(true);
                Toast.makeText(context, "no", Toast.LENGTH_SHORT).show();
            } //Log.d("WifiPreference", "enableNetwork returned " + b );
        } catch (Exception e) { e.printStackTrace(); }
    }
 //////////////////
 void wifi_reconnect_net(Context context, String ssid, String key){

     WifiConfiguration conf = new WifiConfiguration();
     conf.SSID = "\"" + ssid + "\"";
     if(key.equals("open")){
         conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
     }
///        else { conf.preSharedKey = String.format("\"%s\"", key);}
     else { conf.preSharedKey = "\"" + key + "\"";}
     Log.d(tag, "conf. "+conf.SSID + "  "+conf.preSharedKey);
     WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        /* вариант подключения к нужной сети, но почему-то нет подкл. к серверу
        wifiManager.addNetwork(conf);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                break;
            }
        }
        */
     int netId = wifiManager.addNetwork(conf);
     wifiManager.disconnect();
     wifiManager.enableNetwork(netId, true);
     wifiManager.reconnect();
 }
 /*
 void wifi_conn_reconn(String ssid, String key, String secur){

     WifiManager wifiMan = (WifiManager)cntx.getSystemService(Context.WIFI_SERVICE);

     wifiMan.setWifiEnabled(true);
     while (!wifiMan.pingSupplicant());

     WifiConfiguration wifiConfig = new WifiConfiguration();
     wifiConfig.SSID = "\"" + ssid + "\"";
     wifiConfig.status = WifiConfiguration.Status.DISABLED;
     wifiConfig.priority = 40;

     // Dependent on the security type of the selected network
     // we set the security settings for the configuration
     if (secur.equals("open")) {
         // No security
         wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
         wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
         wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
         wifiConfig.allowedAuthAlgorithms.clear();
         wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
         wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
         wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
         wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
         wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
         wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
     } else if (secur.equals("WPA_WPA2")) {
         //WPA/WPA2 Security
         wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
         wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
         wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
         wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
         wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
         wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
         wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
         wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
         wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
         wifiConfig.preSharedKey = "\"".concat(key).concat("\"");
     } else if (secur.equals("WEP")) {
         // WEP Security
         wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
         wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
         wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
         wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
         wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
         wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
         wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
         wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
         wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

         if (getHexKey(key)) wifiConfig.wepKeys[0] = key;
         else wifiConfig.wepKeys[0] = "\"".concat(key).concat("\"");
         wifiConfig.wepTxKeyIndex = 0;
     }

     // Finally we add the new configuration to the managed list of networks
     int networkID = wifiMan.addNetwork(wifiConfig);
     if (networkID > -1) {
         wifiMan.disconnect();
         wifiMan.enableNetwork(networkID, true);
         wifiMan.reconnect();
     }
     Log.d(tag, "networkID "+networkID);
 }
    private static boolean getHexKey(String s) {
        if (s == null) {
            return false;
        }

        int len = s.length();
        if (len != 10 && len != 26 && len != 58) {
            return false;
        }

        for (int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                continue;
            }
            return false;
        }
        return true;
    }

    private String getCurrentSsid(Context context) {
        String ssid = "";
        String scan_ssid = "";

        int[] list_smart_home_conf = new int[20];
        int cnt_smart_home_conf = 0;
        try {

            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (networkInfo.isConnected()) {
                final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                if (connectionInfo != null) {
                    //                   ssid = connectionInfo.getSSID();
                    netID = connectionInfo.getNetworkId();
                    Log.d(tag, "netID = "+ netID);
                }
                List<WifiConfiguration> mScanResults = wifiManager.getConfiguredNetworks();
                String[] list_ssid = new String[mScanResults.size()+1];
                //Log.d(tag,"mScanResults.size(): "+mScanResults.size());
                for(int x=0; x<mScanResults.size();x++){
                    scan_ssid+= "ID: "+mScanResults.get(x).networkId+ " : "+ mScanResults.get(x).SSID+"\n";       // сохраняем имена всех сетей в scan_ssid
                    list_ssid[mScanResults.get(x).networkId] = mScanResults.get(x).SSID;
                    if (mScanResults.get(x).SSID.equals("\"Smart_Home_EZ\"")) {
                        list_smart_home_conf[cnt_smart_home_conf] = mScanResults.get(x).networkId;
                        cnt_smart_home_conf++;
                    }
                }
                ssid = list_ssid[netID];                            // имя подключенной сети в ssid

            } else { ssid = null; }

        }catch(Exception e){ Log.d(tag, "Exception getCurrentSsid!"); ssid = null; }
        Log.d(tag, "Name_ssid netID = "+ netID);
        Log.d(tag, "Name_ssid_scan = "+ scan_ssid);
        Log.d(tag,"Name_ssid: "+ssid);
        Log.d(tag,"Name_ssid_debug: "+list_smart_home_conf[0]+" " +list_smart_home_conf[1]+" "+list_smart_home_conf[2]+" "+list_smart_home_conf[3]+" "+list_smart_home_conf[4]+" "+list_smart_home_conf[5]);

       return ssid;
    }
  */
////////////
////////////
////////////////
///////////////////////////////////////////////////////////
View.OnLongClickListener clickIP = new View.OnLongClickListener() {
    public boolean onLongClick(View v) {
        dialog_show(12);
        return false;
    }
};
    /////////////////////////////////////////////////////////////////////////
    View.OnLongClickListener clickHeater = new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
            dialog_show(15);
            return false;
        }
    };
// Изменение высоты ListView в зависимости от количества элементов, чтобы вместить в ScrollView
// в параметрах передаём listView для определения высоты
public void setListViewHeightBasedOnChildren(ListView listView) {
    ArrayAdapter listAdapter = (ArrayAdapter) listView.getAdapter();

    int totalHeight = 0;
    // проходимся по элементам коллекции
    for(int i = 0; i < listAdapter.getCount(); i++) {
        View listItem = listAdapter.getView(i, null, listView);
        listItem.measure(0, 0);
        // получаем высоту
        totalHeight += listItem.getMeasuredHeight();
    }

    // устанавливаем новую высоту для контейнера
    ViewGroup.LayoutParams params = listView.getLayoutParams();
    params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
    listView.setLayoutParams(params);
    listView.requestLayout();
}
    ///////////
    /////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    final Handler handlinfo_DAY_NIGHT = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (daybtn != null && nightbtn != null) {
                daybtn.setText(Integer.toString(myHourD) + " ч. " + Integer.toString(myMinuteD) + " мин.");
                nightbtn.setText(Integer.toString(myHourN) + " ч. " + Integer.toString(myMinuteN) + " мин.");
            }
        }
    };
    void dialog_show(int dialog){

        final AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch(dialog){
            case 1:                                             // настройка подключения к домашней сети

                LinearLayout view;
                view = (LinearLayout) getLayoutInflater().inflate(R.layout.config_client, null);
                final EditText config_port = view.findViewById(R.id.config_port);
                final EditText config_IP = view.findViewById(R.id.config_IP);
                final EditText config_SSID = view.findViewById(R.id.config_SSID);
                final EditText config_pasw = view.findViewById(R.id.config_pasw);
                final ProgressBar progrbar = view.findViewById(R.id.progrBar);
                final TextView info_prog = view.findViewById(R.id.info_prog);
                final Button apply = view.findViewById(R.id.button2);
                config_IP.setHint(SERVER_IP);

                if (!SERVER_IP.equals("192.168.4.1")) {
                    close_TCP();
                    SERVER_IP = "192.168.4.1";
                    config_IP.setHint(SERVER_IP);
                    config_port.setHint(port);
                    IP_adr.setText("IP server " + SERVER_IP);
                }
                apply.setText("Применить");
                builder.setView(view);
                alert = builder.create();
                alert.show();
                //////////////////////////////////
                final Handler info_NET = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        String mes = "";
                        switch(msg.what){
                            case net_NOT_found:
                                mes = "Нет подключения к сети WIFI!";
                                break;
                            case net_dimmer_NOT_found:
                                //    mes = "Сеть \"Dimmer_EZ\" не найдена!";
                                mes = "Сеть \"Smart_Home_EZ\" не найдена!";
                                break;
                            case wait_scan_net:
                                mes = "Секундочку...";
                                break;
                        }
                        show_txt_toast(mes);
                    }
                };
                //////////////////////////////////

                if(netSSID_cur != null){                                                // проверяем, если подключение WIFI
                    if(!netSSID_cur.equals("Smart_Home_EZ")) {                          //// проверяем, если подключение к сети Smart_Home_EZ
                      show_txt_toast("Подключитесь к \"Smart_Home_EZ\" и введите настройки вашей сети");
                    }
                } else {
                    show_txt_toast("Проверьте подключение к WIFI сети!");
                }

                View.OnClickListener apply_but = new View.OnClickListener() {           // отработка пункта меню настройки порта сервера
                    public void onClick(View v) {
                        try {
                            name_SSID = config_SSID.getText().toString();
                            pass_SSID = config_pasw.getText().toString();
                        }catch(Exception e){
                            show_txt_ex();
                        }
                        /////////////////////////////////////
                        final Handler handlinfo_startESP = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                info_prog.setText(stat_info_prog);
                            }
                        };
                        if( name_SSID.length() != 0 && pass_SSID.length() == 8 ) {                      // пароль должен быть 8 символов
                            ////////////////////////////////////
                                initESP_ON = true;
                                progrbar.setVisibility(View.VISIBLE);
                                name_SSID.getChars(0, name_SSID.length(), bufTCPout, 35);
                                pass_SSID.getChars(0, pass_SSID.length(), bufTCPout, 52);
                                bufTCPout[51] = (char)name_SSID.length();
                                Log.d(tag, "lenght SSID = " + (int) bufTCPout[51]+ " len = "+ name_SSID.length());
                                cmd_send = set_link;

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        thr_start = true;
                                        boolean start_reconnect = false;
                                        while (initESP_ON) {                                            // в потоке, ожидаем получение подтверждения принятой МК команды set_link, после подтверждения будет сброшен бит initESP_ON
                                            try {
                                                Thread.sleep(400);
                                                if (step_progr == 1) {
                                                    if(start_reconnect ){ stat_info_prog = "Сохранение.";}
                                                    else { stat_info_prog = "Обновление"+ "\n"+"параметров.";}
                                                }
                                                if (step_progr == 2) {
                                                    if(start_reconnect ){ stat_info_prog = "Сохранение..";}
                                                    else {stat_info_prog = "Обновление"+ "\n"+"параметров..";}
                                                }
                                                if (step_progr == 3) {
                                                    if(start_reconnect ){ stat_info_prog = "Сохранение...";}
                                                    else {stat_info_prog = "Обновление"+ "\n"+"параметров...";}
                                                }
                                                if (step_progr == 4) {
                                                    if(start_reconnect ){ stat_info_prog = "Сохранение....";}
                                                    else {stat_info_prog = "Обновление"+ "\n"+"параметров....";}
                                                }
                                                if (step_progr == 5) {
                                                    if(start_reconnect ){ stat_info_prog = "Сохранение.....";}
                                                    else {stat_info_prog = "Обновление"+ "\n"+"параметров.....";}
                                                }
                                                if (step_progr == 6) {
                                                    if(start_reconnect ){ stat_info_prog = "Сохранение......";}
                                                    else {stat_info_prog = "Обновление"+ "\n"+"параметров......";}
                                                }
                                                step_progr++;
                                                handlinfo_startESP.sendEmptyMessage(0);
                                                if (step_progr > 6) step_progr = 0;
                                                if (!netSSID_cur.equals("Smart_Home_EZ")) {
                                                    if (!start_reconnect) {
                                                        cnt_0_25_sec = 0;
                                                        start_reconnect = true;
                                                        Log.d(tag, "[ESS] ");
                                                        if (step_progr == 0) WIFI_obj.connectToNetwork("[ESS]", "Smart_Home_EZ", "open");
                                                    }
                                                }
                                            } catch (Exception ee) {
                                                Log.d(tag, "Exception sleep");
                                            }

                                        }
                                        //////////////////////////////////////////////////////////
                                        thr_start = false;
                                        alert.dismiss();
                                        //SERVER_IP = "192.168.4.1";
                                        close_TCP();  //??
                                        //////////////////////////////////////////////////////////
                                    }
                                }).start();
                        }
                        else {
                            show_txt_toast("Данные введены не корректно!");
//                            off_wifi(cntx);
                        }
                        //                       while (initESP_ON);
                    }

                };
                apply.setOnClickListener(apply_but);
            break;
            case 2:                                                                         // установка температуры
                /*
                final EditText settmp_v = new EditText(this);                               //создаем новое поле EditText
                settmp_v.setHint("...˚C");
                settmp_v.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setTitle("Smart Termo")
                        .setMessage("Установить температуру ?")
                        .setView(settmp_v)
                        .setNegativeButton("НЕТ", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        })
                        .setPositiveButton("ДА", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(settmp_v.getText().length() != 0){
                                    int send_tmp = Integer.parseInt(settmp_v.getText().toString());
                                    eX = (send_tmp - 5) * step;
                                    Log.d(tag, "set new tempM " + eX + "step " + step);
                                    showtmp.sendEmptyMessage(0);

                                }
                            }
                        });
                alert = builder.create();
                alert.show();
                break;
*/

                LinearLayout view_settmp;
                view_settmp = (LinearLayout) getLayoutInflater().inflate(R.layout.lay_set_tmp, null);
                final TextView settmp = view_settmp.findViewById(R.id.fonlay_txt);
                final EditText settmp_txt = view_settmp.findViewById(R.id.settmp_txt);
                final TextView but_ye = view_settmp.findViewById(R.id.text_ye);
                final TextView but_no = view_settmp.findViewById(R.id.text_no);
                settmp.setText("Установить температуру ?");
                settmp_txt.setHint("...˚C");

                builder.setView(view_settmp);
                alert = builder.create();
                alert.show();
                View.OnClickListener butyes = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        if(settmp_txt.getText().length() != 0){
                            int send_tmp = Integer.parseInt(settmp_txt.getText().toString());
                            eX = (send_tmp - 5) * step;
                            Log.d(tag, "set new tempM " + eX + "step " + step);
                            change_tmp = true;
                            cmd_menu = true;
                            showtmp.sendEmptyMessage(0);
                            alert.dismiss();
                        }
                    }
                };
                but_ye.setOnClickListener(butyes);
                View.OnClickListener butno = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                };
                but_no.setOnClickListener(butno);
                break;
            case 3:                                                 // помощь
/*                builder.setTitle("Smart Termo").setMessage(R.string.help_termo1);
                alert = builder.create();
                alert.show();
*/
                LinearLayout viewhelp;
                viewhelp = (LinearLayout) getLayoutInflater().inflate(R.layout.help, null);
                Button btOK = viewhelp.findViewById(R.id.butOK);
/*                final TextView help1 = new TextView(this);
                help1.setText(R.string.help_termo1);
                viewhelp.addView(help1);*/

                builder.setView(viewhelp);
                alert = builder.create();
                alert.show();
                View.OnClickListener h_butok = new View.OnClickListener() {        //
                    public void onClick(View v) {
                            alert.dismiss();
                    }
                };
                btOK.setOnClickListener(h_butok);

                break;
            case 4:                                                 // о приложении
                LinearLayout viewabout;
                viewabout = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_lay, null);
                final TextView about_txt = viewabout.findViewById(R.id.fonlay_txt);
                about_txt.setText(R.string.about);
                builder.setView(viewabout);
                alert = builder.create();
                alert.show();
                break;
            case 5:                                                 // меню калибровки
                LinearLayout viewC;
                viewC = (LinearLayout) getLayoutInflater().inflate(R.layout.calibrovka, null);
                final Button butt1 = viewC.findViewById(R.id.button1);
                final EditText A1coef = viewC.findViewById(R.id.A1);
                final EditText A2coef = viewC.findViewById(R.id.A2);
                final EditText A1Ecoef = viewC.findViewById(R.id.A1E);
                final EditText A2Ecoef = viewC.findViewById(R.id.A2E);
                final EditText A3coef = viewC.findViewById(R.id.A3);
                final EditText A4coef = viewC.findViewById(R.id.A4);
                final EditText A3Ecoef = viewC.findViewById(R.id.A3E);
                final EditText A4Ecoef = viewC.findViewById(R.id.A4E);

                final EditText A0coef = viewC.findViewById(R.id.A0);
                final EditText A0Ecoef = viewC.findViewById(R.id.A0E);

                builder.setView(viewC);
                alert = builder.create();
                alert.show();
                flag_calibrovka_run = true;

                View.OnClickListener but_1 = new View.OnClickListener() {        // отработка пункта меню калибровка
                    public void onClick(View v) {
                        try {
                            String A1 = A1coef.getText().toString(), A1E = A1Ecoef.getText().toString();
                            String A2 = A2coef.getText().toString(), A2E = A2Ecoef.getText().toString();
                            String A3 = A3coef.getText().toString(), A3E = A3Ecoef.getText().toString();
                            String A4 = A4coef.getText().toString(), A4E = A4Ecoef.getText().toString();

                            String A0 = A0coef.getText().toString(), A0E = A0Ecoef.getText().toString();
                            save_koef(A1, A2, A3, A4, A1E, A2E, A3E, A4E, A0, A0E);
                            send_koef();
                            alert.dismiss();
                        }catch(Exception e){Log.d(tag, "Exception t1"); show_txt_ex();}
                    }
                };
                butt1.setOnClickListener(but_1);

                break;
            case 6:                                                             // установка режима работы по тарифу
/*
                LinearLayout lay_forIP_gist;
                lay_forIP_gist = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_lay, null);
                final Button butOK_gist = new Button(this);
                final Button butNO_gist = new Button(this);
                butOK_gist.setText("Применить?");
                butNO_gist.setText("Отмена");
                butNO_gist.setBackgroundColor(getResources().getColor(R.color.black_));
                butOK_gist.setBackgroundColor(getResources().getColor(R.color.black_));
                butNO_gist.setTextColor(getResources().getColor(R.color.default_));
                butOK_gist.setTextColor(getResources().getColor(R.color.default_));
                LinearLayout ll_gist=new LinearLayout(this);
                ll_gist.setOrientation(LinearLayout.HORIZONTAL);
                final TextView txtinf = lay_forIP_gist.findViewById(R.id.fonlay_txt);
                txtinf.setText("Установка гистерезиса для температур ночного и дневного режима");

                final EditText gist_v = new EditText(MainActivity.this);
                gist_v.setHint("10");
                gist_v.setInputType(InputType.TYPE_CLASS_NUMBER);

                ll_gist.addView(butOK_gist);
                ll_gist.addView(butNO_gist);
                LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(

                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                lpp.weight = 1;                 // установить вес 1
                butOK_gist.setLayoutParams(lpp);
                butNO_gist.setLayoutParams(lpp);
                lay_forIP_gist.addView(gist_v);
                lay_forIP_gist.addView(ll_gist);

                builder.setView(lay_forIP_gist);
                alert = builder.create();
                alert.show();
                View.OnClickListener OKgist = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        String tmp_gisteresis;
                        if(gist_v.getText().length() != 0) {
                            rdBut.setChecked(true);
                            rdbutCheck = true;
                            tmp_gisteresis =  gist_v.getText().toString();
//                                saved_config("gisteresis",gisteresis_N_D);
                            mode_heart_color.setTextColor(getResources().getColor(R.color.GREEN_));
                            func_get_time_tarif();
                            mode_heart_color.setText("Учитывать дневной и ночной тариф"+" ±"+tmp_gisteresis+"˚C"+"\n"+"день : "+time_Day+"  ночь : "+time_Night);
                            mode_tarif = true;
                            work_TARIF_ = OKK;
//                                    TARIF_change = OKK;

                            gisteresis_TARIF_ = Integer.parseInt(tmp_gisteresis);
                            cmd_success = work_TARIF_ +2;
                            cmd_send = set_work_TARIF;
                        }
                        else{
                            rdBut.setChecked(false); rdbutCheck = false;
                            mode_heart_color.setTextColor(getResources().getColor(R.color.default_));
                            mode_heart_color.setText("Учитывать дневной и ночной тариф");
                            mode_tarif = false;
                        }
                       alert.dismiss();
                    }
                };
                butOK_gist.setOnClickListener(OKgist);
                View.OnClickListener NOgist = new View.OnClickListener() {        //
                    public void onClick(View v) {
                                rdBut.setChecked(false);
                                rdbutCheck = false;
                                mode_tarif = false;
                                mode_heart_color.setTextColor(getResources().getColor(R.color.default_));
                                mode_heart_color.setText("Учитывать дневной и ночной тариф");
                        alert.dismiss();
                    }
                };
                butNO_gist.setOnClickListener(NOgist);
*/
                LinearLayout lay_forIP_gist;
                lay_forIP_gist = (LinearLayout) getLayoutInflater().inflate(R.layout.lay_set_tmp, null);
                final TextView butOK_gist = lay_forIP_gist.findViewById(R.id.text_ye);
                final TextView butNO_gist = lay_forIP_gist.findViewById(R.id.text_no);
                final EditText gist_v = lay_forIP_gist.findViewById(R.id.settmp_txt);
                gist_v.setHint("10");
                gist_v.setInputType(InputType.TYPE_CLASS_NUMBER);
                butOK_gist.setText("Применить?");
                butNO_gist.setText("Отмена");
                final TextView txtinf = lay_forIP_gist.findViewById(R.id.fonlay_txt);
                txtinf.setText("Установка гистерезиса для температур ночного и дневного режима");
                builder.setView(lay_forIP_gist);
                alert = builder.create();
                alert.show();
                View.OnClickListener OKgist = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        String tmp_gisteresis;
                        if(gist_v.getText().length() != 0) {
                            rdBut.setChecked(true);
                            rdbutCheck = true;
                            tmp_gisteresis =  gist_v.getText().toString();
//                                saved_config("gisteresis",gisteresis_N_D);
                            mode_heart_color.setTextColor(getResources().getColor(R.color.GREEN_));
                            func_get_time_tarif();
                            mode_heart_color.setText("Учитывать дневной и ночной тариф"+" ±"+tmp_gisteresis+"˚C"+"\n"+"день : "+time_Day+"  ночь : "+time_Night);
                            mode_tarif = true;
                            work_TARIF_ = OKK;
//                                    TARIF_change = OKK;

                            gisteresis_TARIF_ = Integer.parseInt(tmp_gisteresis);
                            cmd_success = work_TARIF_ +2;
                            cmd_send = set_work_TARIF;
                        }
                        else{
                            rdBut.setChecked(false); rdbutCheck = false;
                            mode_heart_color.setTextColor(getResources().getColor(R.color.default_));
                            mode_heart_color.setText("Учитывать дневной и ночной тариф");
                            mode_tarif = false;
                        }
                        alert.dismiss();
                    }
                };
                butOK_gist.setOnClickListener(OKgist);
                View.OnClickListener NOgist = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        rdBut.setChecked(false);
                        rdbutCheck = false;
                        mode_tarif = false;
                        mode_heart_color.setTextColor(getResources().getColor(R.color.default_));
                        mode_heart_color.setText("Учитывать дневной и ночной тариф");
                        alert.dismiss();
                    }
                };
                butNO_gist.setOnClickListener(NOgist);
                break;
            case 7:                                                                 // установка времени "день", "ночь" для работы по тарифу

                LinearLayout view2;
                view2 = (LinearLayout) getLayoutInflater().inflate(R.layout.night_day_time, null);
                daybtn = view2.findViewById(R.id.text_d);
                nightbtn = view2.findViewById(R.id.text_n);
                final Button use = view2.findViewById(R.id.btn_use);
                builder.setView(view2);

                View.OnClickListener but_day = new View.OnClickListener() {        // отработка пункта меню сервер
                    public void onClick(View v) {
                        showDialog(71);
                    }
                };
                daybtn.setOnClickListener(but_day);
                View.OnClickListener but_night = new View.OnClickListener() {          // отработка пункта меню клиент
                    public void onClick(View v) {
                        showDialog(81);
                    }

                };
                nightbtn.setOnClickListener(but_night);

                alert = builder.create();
                alert.show();

                View.OnClickListener but_use__ = new View.OnClickListener() {        // отработка пункта меню сервер
                    public void onClick(View v) {
                        handlinfo_DAY_NIGHT.sendEmptyMessage(0);
                        if(count_dialogND != 0) {
                            time_NIGHT = myHourN*60*60 + myMinuteN*60;
                            time_DAY = myHourD*60*60 + myMinuteD*60;
                            cmd_send = synchro_timeTARIF;
                            cmd_success = OKK;
                            Log.d(tag, "myMinuteN "+ myMinuteN);
                            Log.d(tag, "myMinuteD "+ myMinuteD);
/////////////////////////////
//       Log.d(tag, "time_NIGHT "+time_NIGHT+" time_DAY "+time_DAY);
////////////////////////////
                            count_dialogND = 0; alert.dismiss();
                        }
                        else count_dialogND = 1;
                        Log.d(tag, "time_NIGHT "+time_NIGHT+" time_DAY "+time_DAY+ " count_dialogND "+count_dialogND);
                        //                   alert.dismiss();
                    }
                };
                use.setOnClickListener(but_use__);
                break;
            case 8:                                                 // выбор нагревателя, газ - электро
                LinearLayout view3;
                view3 = (LinearLayout) getLayoutInflater().inflate(R.layout.mode_work, null);
                final Button gas_mode = view3.findViewById(R.id.button_serv);
                final Button el_mode = view3.findViewById(R.id.button_cl);
                final TextView info_lab = view3.findViewById(R.id.tvT);
                info_lab.setText("Выбор нагревателя");
//                gas_mode.setText("ГАЗ");
//                el_mode.setText("Электро");
                gas_mode.setText(heater1);
                el_mode.setText(heater2);

                builder.setView(view3);
                alert = builder.create();
                alert.show();

                View.OnClickListener handl_gas_mode = new View.OnClickListener() {        // обработка режима ГАЗ
                    public void onClick(View v) {
                        status_mode_GAS = OKK;
                        showDialog(91);
 //                       cmd_send = set_GAS;
                        alert.dismiss();
                    }
                };
                gas_mode.setOnClickListener(handl_gas_mode);
                View.OnClickListener handl_el_mode = new View.OnClickListener() {          // обработка режима Электро
                    public void onClick(View v) {
                        status_mode_GAS = NNO;
                        showDialog(91);
//                        cmd_send = set_GAS;
//                        USB_sendDEV();
                        alert.dismiss();
                    }

                };
                el_mode.setOnClickListener(handl_el_mode);
                break;
            case 9:                                         // режим работы термостата, нагрев - охлаждения (только для работы по газу)
                LinearLayout lay_INVERS;
                lay_INVERS = (LinearLayout) getLayoutInflater().inflate(R.layout.lay_set_tmp, null);
                final TextView txt_INVERS = lay_INVERS.findViewById(R.id.fonlay_txt);
                final TextView edit_INVERS = lay_INVERS.findViewById(R.id.settmp_txt);
                final LinearLayout lay11 = lay_INVERS.findViewById(R.id.lay_for_txtedit);
                final TextView but_yeINVERS = lay_INVERS.findViewById(R.id.text_ye);
                final TextView but_noINVERS = lay_INVERS.findViewById(R.id.text_no);
                lay11.removeView(edit_INVERS);
                String ss;
                if(cool_ON){ss = "Выключить инверсию реле выхода?\n";}
                else{ss = "Включить инверсию реле выхода?\n";}
                txt_INVERS.setText(ss);
                builder.setView(lay_INVERS);
                alert = builder.create();
                alert.show();
                View.OnClickListener butyes11 = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        if(cool_ON){
                            cmd_send =  cool_modeOFF;  cool_ON = false;
                        }
                        else {
                            cmd_send = cool_modeON; cool_ON = true;
                        }
                        cmd_success = OKK;
                        alert.dismiss();
                    }
                };
                but_yeINVERS.setOnClickListener(butyes11);
                View.OnClickListener butno11 = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                };
                but_noINVERS.setOnClickListener(butno11);
                break;
            case 10:
                if(sync_Flag_set_tmp) {
                    builder.setTitle("Smart Termo")
                            .setMessage("Отключить синхронизацию с термостатом установленной температуры?")
                            .setNegativeButton("НЕТ", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    sync_Flag_set_tmp = true;
                                }
                            })
                            .setPositiveButton("ДА", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    sync_Flag_set_tmp = false;
                                }
                            });
                }
                else{
                    builder.setTitle("Smart Termo")
                            .setMessage("Включить синхронизацию с термостатом установленной температуры?")
                            .setNegativeButton("НЕТ", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    sync_Flag_set_tmp = false;
                                }
                            })
                            .setPositiveButton("ДА", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    sync_Flag_set_tmp = true;
                                }
                            });
                }
                alert = builder.create();
                alert.show();
                break;
            case 11:                                             // настройка почты
/*
                LinearLayout view_mail;
                view_mail = (LinearLayout) getLayoutInflater().inflate(R.layout.mail_set_form, null);
                final EditText login_mail = view_mail.findViewById(R.id.config_SSID);
                final EditText pass_mail = view_mail.findViewById(R.id.config_pasw);
                final EditText mail_to = view_mail.findViewById(R.id.mail_to);
                final ProgressBar progrbar_mail = view_mail.findViewById(R.id.progrBar);
                final TextView info_prog_mail = view_mail.findViewById(R.id.info_prog);

                final Button apply_mail = view_mail.findViewById(R.id.button2);


                apply_mail.setText("Применить");
                builder.setView(view_mail);
                alert = builder.create();
                alert.show();
                View.OnClickListener apply_mail_handle = new View.OnClickListener() {           // отработка пункта меню настройки порта сервера
                    public void onClick(View v) {
                        String mail_from = "", mail_from64 = "", mailto = "", pass = "", pass64 = "";
                        mail_from = login_mail.getText().toString();
                        mailto = mail_to.getText().toString();
                        pass = pass_mail.getText().toString();
   //   Все поля (mail_from, mail_to, pass) ограниченное количество символов, что бы в итоге не превысило 128 с преамбулой вместе //
   // mail_from - не более 22 (проверка)
   // mail_to -  не более 22 (проверка)
   // pass  - не более 14 (проверка)
   // mail_from64 - не более 32
   // pass64 - не более 20
   // с учетом вышеприведенного получаем карту размещения данных:
                        // mail_from-[4]-[25]
                        // mailto-[26]-[47]
                        // pass-[48]-[61]
                        // mail_from.length:[62] , mailto.length:[63], pass.length:[64]
                        // mail_from64-[69]-[100]
                        // pass64-[101]-[120]
                        // mail_from64.length:[121], pass64.length:[122]
                       boolean flag_ok = false;
                       int account_mail = 3;        // mail.ru -3, yandex.ru - 4, gmail.com - 5
                       char [] tmp_ch = new char [50];
                        if(mail_from.length()<8 || mailto.length()<8 || pass.length()<4){show_txt_toast("Данные не корректны или введены не все!");}
                        else {
                            mail_from64 = coder_base64str(mail_from);
                            pass64 = coder_base64str(pass);
                            if (mailto.length() > 22) {
                                show_txt_toast("Адрес получателя не должен превышать 22 символа!");
                            } else{
                                flag_ok = true;
                                mailto.getChars(0, mailto.length(), tmp_ch, 0);
                                for(int gg=0; gg<mailto.length(); gg++) {
                                    if(tmp_ch[gg] == ' '){
                                        flag_ok = false;
                                        show_txt_toast("Адрес получателя не должен содержать пробелы!");
                                    }
                                }
                                if(flag_ok){
                                    flag_ok = false;
                                    for(int gg=0; gg<mailto.length(); gg++) {
                                        if(tmp_ch[gg] == '.'){
                                            flag_ok = true;
                                         }
                                    }
                                    if(!flag_ok) { show_txt_toast("Некорректный адрес получателя!"); }
                                }
                            }
                            if (mail_from.length() > 22 && flag_ok) {
                                show_txt_toast("Адрес отправителя не должен превышать 22 символа!");
                                flag_ok = false;
                            }else {
                                mail_from.getChars(0, mail_from.length(), tmp_ch, 0);
                                for(int gg=0; gg<mail_from.length(); gg++) {
                                    if(tmp_ch[gg] == ' '){
                                        flag_ok = false;
                                        show_txt_toast("Адрес отправителя не должен содержать пробелы!");
                                    }
                                }
                                if(flag_ok){
                                    flag_ok = false;
                                    for(int gg=0; gg<mail_from.length(); gg++) {
                                        if(tmp_ch[gg] == '.'){
                                            flag_ok = true;
                                        }
                                    }
                                    if(!flag_ok) { show_txt_toast("Некорректный адрес отправителя!"); }
                                }
                            }
                            if (pass.length() > 14 && flag_ok) {
                                show_txt_toast("Пароль не должен превышать 14 символов!");
                                flag_ok = false;
                            }else{
                                pass.getChars(0, pass.length(), tmp_ch, 0);
                                for(int gg=0; gg<pass.length(); gg++) {
                                    if(tmp_ch[gg] == ' '){
                                        flag_ok = false;
                                        show_txt_toast("Пароль не должен содержать пробелы!");
                                    }
                                }
                            }
                            //////////////////////////////
                            mail_from.getChars(0, mail_from.length(), tmp_ch, 0);
                            String acc = ""; boolean start_rec_acc = false;
                            for(int gg=0; gg<mail_from.length(); gg++) {
                                if(start_rec_acc){ acc = acc+tmp_ch[gg]; }
                                if(tmp_ch[gg] == '@'){ start_rec_acc = true; }
                            }
                            if(acc.equals("gmail.com")){
                                account_mail = 5;
                            } else {
                                if(acc.equals("mail.ru")){ account_mail = 3; }
                                else {
                                    flag_ok = false;
                                    show_txt_toast("Нужен аккаунт mail.ru или gmail.com!");
                                    Log.d(tag, "get account = "+acc);
                                }
                            }
                            if(!connect_server){ show_txt_toast("Нет соединения с сервером!"); alert.dismiss();}
                            /////////////////////////////
                        }
                        if(flag_ok){
                           mail_from.getChars(0, mail_from.length(), bufTCPout, 4);
                           mailto.getChars(0, mailto.length(), bufTCPout, 26);
                           pass.getChars(0, pass.length(), bufTCPout, 48);
                           mail_from64.getChars(0, mail_from64.length(), bufTCPout, 69);
                           pass64.getChars(0, pass64.length(), bufTCPout, 101);

                           bufTCPout[62] = (char)mail_from.length();
                           bufTCPout[63] = (char)mailto.length();
                           bufTCPout[64] = (char)pass.length();
                           bufTCPout[121] = (char)mail_from64.length();
                           bufTCPout[122] = (char)pass64.length();
                           bufTCPout[123] = (char)account_mail;
////////////////////////////////////////////////////////////debug
                            Log.d(tag, "config_mail string from64 = "+mail_from64+" pass64 = "+pass64+" pass64.length() "+pass64.length()+" mail_from64.length() "+mail_from64.length()+" account "+account_mail);
//////////////////////////////////////////////////////////////////
                           cmd_send = config_mail;
                           progrbar_mail.setVisibility(View.VISIBLE);
////////////////////////////////////////////
                           final Handler handlinfo_startESP = new Handler() {
                               @Override
                               public void handleMessage(Message msg) {
                                   info_prog_mail.setText(stat_info_prog);
                               }
                           };
//////////////////////////////////////////////
                           if (!thr_start) {                            //чтобы не плодить потоки
                               initESP_ON = true;
                               thr_start = true;
                               new Thread(new Runnable() {
                                   @Override
                                   public void run() {

                                       while (initESP_ON) {                                            // в потоке, ожидаем получение подтверждения принятой МК команды set_link, после подтверждения будет сброшен бит initESP_ON
                                           try {
                                               Thread.sleep(400);
                                               if (step_progr == 1) {
                                                   stat_info_prog = "Сохранение.";
                                               }
                                               if (step_progr == 2) {
                                                   stat_info_prog = "Сохранение..";
                                               }
                                               if (step_progr == 3) {
                                                   stat_info_prog = "Сохранение...";
                                               }
                                               if (step_progr == 4) {
                                                   stat_info_prog = "Сохранение....";
                                               }
                                               if (step_progr == 5) {
                                                   stat_info_prog = "Сохранение.....";
                                               }
                                               if (step_progr == 6) {
                                                   stat_info_prog = "Сохранение......";
                                               }
                                               step_progr++;
                                               handlinfo_startESP.sendEmptyMessage(0);
                                               if (step_progr > 6) step_progr = 0;

                                           } catch (Exception ee) {
                                               Log.d(tag, "Exception config mail while!");
                                           }

                                       }
                                       ;
                                       ///////////////////////////////////////////////////////////
                                       thr_start = false;
 //                                      show_txt_toast("Настройки почты сохранены!");
                                       alert.dismiss();
                                       //////////////////////////////////////////////////////////
                                   }
                               }).start();
                           }
////////////////////////////////////////////
 //                          alert.dismiss();
                       }
                    }
                };
                apply_mail.setOnClickListener(apply_mail_handle);
*/
                LinearLayout view_mail;
                view_mail = (LinearLayout) getLayoutInflater().inflate(R.layout.mail_set_form, null);
                final EditText login_mail = view_mail.findViewById(R.id.config_SSID);
                final EditText pass_mail = view_mail.findViewById(R.id.config_pasw);
                final EditText mail_to = view_mail.findViewById(R.id.mail_to);
                final EditText name_smtp_serv = view_mail.findViewById(R.id.config_SMTP);
                final EditText port_serv = view_mail.findViewById(R.id.config_port_SMTP);

                final ProgressBar progrbar_mail = view_mail.findViewById(R.id.progrBar);
                final TextView info_prog_mail = view_mail.findViewById(R.id.info_prog);

                final Button apply_mail = view_mail.findViewById(R.id.button2);

//////////////////////////////debug/////////////////
                //login_mail.setText("e.zmtsv@mail.ru");
                //pass_mail.setText("E11PV8y2VKAQW6BLGET2");
                //mail_to.setText("evan77@bk.ru");
                //name_smtp_serv.setText("smtp.mail.ru");
                //port_serv.setText("465");
                //////////////////////
                apply_mail.setText("Применить");
                builder.setView(view_mail);
                alert = builder.create();
                alert.show();
                View.OnClickListener apply_mail_handle = new View.OnClickListener() {           // отработка пункта меню настройки порта сервера
                    public void onClick(View v) {
                        String mail_from = "", mail_from64 = "", mailto = "", pass = "", pass64 = "", name_serv = "", name_port = "";
                        mail_from = login_mail.getText().toString();
                        mailto = mail_to.getText().toString();
                        pass = pass_mail.getText().toString();
                        name_serv = name_smtp_serv.getText().toString();
                        name_port = port_serv.getText().toString();
                        //   Все поля (mail_from, mail_to, pass) ограниченное количество символов, что бы в итоге не превысило 128 с преамбулой вместе //
                        // mail_from - не более 22 (проверка)
                        // mail_to -  не более 22 (проверка)
                        // pass  - не более 20 (проверка)
                        // mail_from64 - не более 32
                        // pass64 - не более 28
                        // name_port - 3
                        // name_serv - 30
                        // с учетом вышеприведенного получаем карту размещения данных:
                        // mail_from - [4]-[25]
                        // mailto - [26]-[47]
                        // name_port - [48]-[50]
                        // mail_from.length -[51]
                        // mail_from64.length - [52]
                        // pass64.length - [53]
                        // mailto.length - [54]
                        // name_serv.lenght - [55]
                        // mail_from64 - [69]-[100]
                        // name_serv - [100]-[129]
                        // pass64 - [130]-[157]



                        boolean flag_ok = false;
                        int account_mail = 3;        // mail.ru -3, yandex.ru - 4, gmail.com - 5
                        char [] tmp_ch = new char [50];
                        if(mail_from.length()<8 || mailto.length()<8 || pass.length()<4){show_txt_toast("Данные не корректны или введены не все!");}
                        else {
                            mail_from64 = coder_base64str(mail_from);
                            pass64 = coder_base64str(pass);
                            if (mailto.length() > 22) {
                                show_txt_toast("Адрес получателя не должен превышать 22 символа!");
                            } else{
                                flag_ok = true;
                                mailto.getChars(0, mailto.length(), tmp_ch, 0);
                                for(int gg=0; gg<mailto.length(); gg++) {
                                    if(tmp_ch[gg] == ' '){
                                        flag_ok = false;
                                        show_txt_toast("Адрес получателя не должен содержать пробелы!");
                                    }
                                }
                                if(flag_ok){
                                    flag_ok = false;
                                    for(int gg=0; gg<mailto.length(); gg++) {
                                        if(tmp_ch[gg] == '.'){
                                            flag_ok = true;
                                        }
                                    }
                                    if(!flag_ok) { show_txt_toast("Некорректный адрес получателя!"); }
                                }
                            }
                            if (mail_from.length() > 22 && flag_ok) {
                                show_txt_toast("Адрес отправителя не должен превышать 22 символа!");
                                flag_ok = false;
                            } else {
                                mail_from.getChars(0, mail_from.length(), tmp_ch, 0);
                                for(int gg=0; gg<mail_from.length(); gg++) {
                                    if(tmp_ch[gg] == ' '){
                                        flag_ok = false;
                                        show_txt_toast("Адрес отправителя не должен содержать пробелы!");
                                    }
                                }
                                if(flag_ok){
                                    flag_ok = false;
                                    for(int gg=0; gg<mail_from.length(); gg++) {
                                        if(tmp_ch[gg] == '.'){
                                            flag_ok = true;
                                        }
                                    }
                                    if(!flag_ok) { show_txt_toast("Некорректный адрес отправителя!"); }
                                }
                            }
                            if (pass.length() > 20 && flag_ok) {
                                show_txt_toast("Пароль не должен превышать 20 символов!");
                                flag_ok = false;
                            }else{
                                pass.getChars(0, pass.length(), tmp_ch, 0);
                                for(int gg=0; gg<pass.length(); gg++) {
                                    if(tmp_ch[gg] == ' '){
                                        flag_ok = false;
                                        show_txt_toast("Пароль не должен содержать пробелы!");
                                    }
                                }
                            }
                            //////////////////////////////

                            if(!connect_server){ show_txt_toast("Нет соединения с сервером!"); alert.dismiss();}
                            /////////////////////////////
                        }
                        if(flag_ok){
                            mail_from.getChars(0, mail_from.length(), bufTCPout, 4);
                            mailto.getChars(0, mailto.length(), bufTCPout, 26);
                            name_port.getChars(0, name_port.length(), bufTCPout, 48);
                            mail_from64.getChars(0, mail_from64.length(), bufTCPout, 69);
                            name_serv.getChars(0, name_serv.length(), bufTCPout, 101);
                            pass64.getChars(0, pass64.length(), bufTCPout, 131);

                            bufTCPout[51] = (char)mail_from.length();
                            bufTCPout[52] = (char)mail_from64.length();
                            bufTCPout[53] = (char)pass64.length();
                            bufTCPout[54] = (char)mailto.length();
                            bufTCPout[55] = (char)name_serv.length();



////////////////////////////////////////////////////////////debug
                            Log.d(tag, "config_mail string from64 = "+mail_from64+" pass64 = "+pass64+" pass64.length() "+pass64.length()+" mail_from64.length() "+mail_from64.length()+" account "+account_mail);
//////////////////////////////////////////////////////////////////
                            cmd_send = config_mail;
                            progrbar_mail.setVisibility(View.VISIBLE);
////////////////////////////////////////////
                            final Handler handlinfo_startESP = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    info_prog_mail.setText(stat_info_prog);
                                }
                            };
//////////////////////////////////////////////
                            if (!thr_start) {                            //чтобы не плодить потоки
                                initESP_ON = true;
                                thr_start = true;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        while (initESP_ON) {                                            // в потоке, ожидаем получение подтверждения принятой МК команды set_link, после подтверждения будет сброшен бит initESP_ON
                                            try {
                                                Thread.sleep(400);
                                                if (step_progr == 1) {
                                                    stat_info_prog = "Сохранение.";
                                                }
                                                if (step_progr == 2) {
                                                    stat_info_prog = "Сохранение..";
                                                }
                                                if (step_progr == 3) {
                                                    stat_info_prog = "Сохранение...";
                                                }
                                                if (step_progr == 4) {
                                                    stat_info_prog = "Сохранение....";
                                                }
                                                if (step_progr == 5) {
                                                    stat_info_prog = "Сохранение.....";
                                                }
                                                if (step_progr == 6) {
                                                    stat_info_prog = "Сохранение......";
                                                }
                                                step_progr++;
                                                handlinfo_startESP.sendEmptyMessage(0);
                                                if (step_progr > 6) step_progr = 0;

                                            } catch (Exception ee) {
                                                Log.d(tag, "Exception config mail while!");
                                            }

                                        }
                                        ;
                                        ///////////////////////////////////////////////////////////
                                        thr_start = false;
                                        //                                      show_txt_toast("Настройки почты сохранены!");
                                        alert.dismiss();
                                        //////////////////////////////////////////////////////////
                                    }
                                }).start();
                            }
////////////////////////////////////////////
                            //                          alert.dismiss();
                        }
                    }
                };
                apply_mail.setOnClickListener(apply_mail_handle);
                break;
            case 12: //Добавление новых IP

                final EditText new_ip = new EditText(MainActivity.this);
                final Button newIPbutOK = new Button(this);
                final Button newIPbutNO = new Button(this);
                ScrollView scroll_v = new ScrollView(this);
                LinearLayout l_scrl = new LinearLayout(this);
                l_scrl.setOrientation(LinearLayout.VERTICAL);
                newIPbutOK.setText("Применить");
                newIPbutNO.setText("Отмена");
                new_ip.setInputType(InputType.TYPE_CLASS_TEXT); // вместо энтер на клаве будет крыжик применить
                LinearLayout ll=new LinearLayout(this);
                ll.setOrientation(LinearLayout.HORIZONTAL);
                newIPbutNO.setTextColor(getResources().getColor(R.color.default_));
                newIPbutOK.setTextColor(getResources().getColor(R.color.default_));
                newIPbutOK.setBackgroundColor(getResources().getColor(R.color.black_));
                newIPbutNO.setBackgroundColor(getResources().getColor(R.color.black_));
                ll.addView(newIPbutNO);
                ll.addView(newIPbutOK);

//            newIPbutNO.setX(120);       // смещение по X
//            newIPbutOK.setX(250);       // смещение по X
/////////////////////////
                LinearLayout.LayoutParams lay_param = new LinearLayout.LayoutParams(

                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
//            lp.setMargins(0,0,0,0);
                lay_param.weight = 1;                 // установить вес 1
                lay_param.setMargins(10,20,10,20);
                newIPbutNO.setLayoutParams(lay_param);
//            lp.setMargins(0,0,0,5);
                newIPbutOK.setLayoutParams(lay_param);
//////////////////////////
                final String tmpstr;
                name_adr = new ArrayList<String>();
                num_save_ip = read_config_int("num_save_ip");
                if(num_save_ip != 0){
                    for(int dr = 0; dr<num_save_ip; dr++){
                        name_adr.add(read_config_str("server_IP"+Integer.toString(dr+1)));
                    }
                }

                ////////////////////////////////////////////
                lst_ipadr = new ListView(MainActivity.this);
//                String[] name_adr = { "Иван", "Марья", "Петр" };
//              ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, name_adr);
                adapter_for_nameIP = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, name_adr);  ////final ListAdapter adapter_for_nameIP
                lst_ipadr.setAdapter(adapter_for_nameIP);
                new_IP_found = true;

                LinearLayout lay_forIP;
                lay_forIP = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_lay, null);
                final TextView txtIP = lay_forIP.findViewById(R.id.fonlay_txt);
                txtIP.setText("Установка нового IP адреса");
                lay_forIP.addView(new_ip);
//                lay_forIP.addView(lst_ipadr);
                l_scrl.addView(lst_ipadr);
                setListViewHeightBasedOnChildren(lst_ipadr);        // выставляем отбражение всех элементов списка
                l_scrl.addView(ll);
                scroll_v.addView(l_scrl);
                lay_forIP.addView(scroll_v);

                tmpstr = SERVER_IP;
                new_ip.setHint(SERVER_IP);

                builder.setView(lay_forIP);
                alert = builder.create();
                alert.show();

                View.OnClickListener OKIP = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        close_TCP();
                        SERVER_IP = new_ip.getText().toString();
                        if(SERVER_IP.equals(""))SERVER_IP = tmpstr;
                        else{
                            /////////////////////////////////////////////////////
                            if(SERVER_IP.equals(read_config_str("server_IP"))){ new_IP_found = false;}          // если в памяти у же есть этот IP, то сбрасываем флаг new_IP_found - сохранять не будем
                            for(int ip = 0; ip<num_save_ip ; ip++){
                                if(SERVER_IP.equals(read_config_str("server_IP"+Integer.toString(ip+1)))){ new_IP_found = false;}        //// если в памяти у же есть этот IP, то сбрасываем флаг new_IP_found - сохранять не будем
                            }
                            /////////////////////////////////////////////////////          SERVER_IP != tmpstr
                            if(new_IP_found) {
                                num_save_ip++;
                                saved_config("num_save_ip", num_save_ip);                               // сохраняем новое кол-во подключенных используемых IP адресов
                                saved_config("server_IP"+Integer.toString(num_save_ip), SERVER_IP);     // сохраняем новый IP адрес с индексом "server_IP"+Integer.toString(num_save_ip)
                                ///////////////////////////
                                saved_config(SERVER_IP+"heater1", heater1);     // имя 1 нагревателя для конкретного IP
                                saved_config(SERVER_IP+"heater2", heater2);     // имя 2 нагревателя для конкретного IP
                                //////////////////////////
                            }
                        }
                        saved_config("server_IP", SERVER_IP);                   // сохраняем IP для следующего старта приложения
                        IP_adr.setText("IP server "+SERVER_IP);                 // " port "+ port_int
                        alert.dismiss();
                    }
                };
                newIPbutOK.setOnClickListener(OKIP);

                View.OnClickListener NOIP = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                };
                newIPbutNO.setOnClickListener(NOIP);
                ///////////////////////////////////////
                lst_ipadr.setOnItemClickListener(new AdapterView.OnItemClickListener() {    // еще один вариант обработчика нажатия на пункт списка
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Log.d(tag, "itemClick: position = " + position + ", id = "
                                + id+ " всего адресов в списке "+ " ::: "+num_save_ip);
                        if(!flag_long_IP) {
                            close_TCP();
                            SERVER_IP = name_adr.get(position);
                            saved_config("server_IP", SERVER_IP);                           // сохраняем IP для следующего старта приложения
                            IP_adr.setText("IP server " + SERVER_IP);                         //" port "+ port_int
                            //////////////////Читаем имена нагревателей выбранного IP
                            String heat_tmp = read_config_str(SERVER_IP + "heater1");
                            if (heat_tmp != "") {
                                heater1 = heat_tmp;
                            }
                            heat_tmp = read_config_str(SERVER_IP + "heater2");
                            if (heat_tmp != "") {
                                heater2 = heat_tmp;
                            }
                            txtGAS_EL.setText("Нагреватель " + heater1);
                            //Log.d(tag, "выбор из списка IP, ");

                            ////////////////////////////////
                            alert.dismiss();
                        }else{ flag_long_IP = false; }
                    }
                });
                /////////////////////////
                lst_ipadr.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
                    @Override
                    public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id)
                    {
 //                     show_txt_toast(" id "+id);
                        flag_long_IP = true;
                        SERVER_IP_tmp = name_adr.get(pos);
                        dialog_IN_dial(1);
                        //adapter.notify();
                        //lst_ipadr.removeView();
                        //show_txt_toast(" id "+id+" num_save_ip "+num_save_ip + " SERVER_IP "+SERVER_IP_tmp);
                      return false;
                    }
                });
                ////////////////////////
//////////////////////////////////////////////////////////////
                break;
            case 13:

                LinearLayout viewST;
                viewST = (LinearLayout) getLayoutInflater().inflate(R.layout.lay_set_tmp, null);
                final TextView st_txt = viewST.findViewById(R.id.fonlay_txt);
                final TextView edit_st = viewST.findViewById(R.id.settmp_txt);
                final LinearLayout lay111 = viewST.findViewById(R.id.lay_for_txtedit);
                final TextView newyes = viewST.findViewById(R.id.text_ye);
                final TextView newNO = viewST.findViewById(R.id.text_no);
                st_txt.setText("Для перехода в меню настроек кликните пиктограмму \"домика\" в верхнем правом углу экрана.\n\nПоказывать это напоминание при старте?");
                lay111.removeView(edit_st);

                builder.setView(viewST);
                alert = builder.create();
                alert.show();
                View.OnClickListener handl_newyes = new View.OnClickListener() {        // да
                    public void onClick(View v) {
                        saved_config("saved_show_help", 0);
                        alert.dismiss();
                    }
                };
                newyes.setOnClickListener(handl_newyes);
                View.OnClickListener handl_newNO = new View.OnClickListener() {          // нет
                    public void onClick(View v) {
                        saved_config("saved_show_help", 1);
                        alert.dismiss();
                    }

                };
                newNO.setOnClickListener(handl_newNO);
            break;
            case 14:                                                            /// сбос модуля к установкам по умолчанию
                LinearLayout lay_menu10;
                lay_menu10 = (LinearLayout) getLayoutInflater().inflate(R.layout.lay_set_tmp, null);
                final TextView txt_menu10 = lay_menu10.findViewById(R.id.fonlay_txt);
                final TextView edit_menu10 = lay_menu10.findViewById(R.id.settmp_txt);
                final LinearLayout lay10 = lay_menu10.findViewById(R.id.lay_for_txtedit);
                final TextView but_ye10 = lay_menu10.findViewById(R.id.text_ye);
                final TextView but_no10 = lay_menu10.findViewById(R.id.text_no);
                lay10.removeView(edit_menu10);
                txt_menu10.setText(R.string.sbrosESP);
                txt_menu10.setTextColor(getResources().getColor(R.color.RED));
                builder.setView(lay_menu10);
                alert = builder.create();
                alert.show();
                View.OnClickListener butyes10 = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        cmd_send = load_def;
                        alert.dismiss();
                    }
                };
                but_ye10.setOnClickListener(butyes10);
                View.OnClickListener butno10 = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                };
                but_no10.setOnClickListener(butno10);
            break;
            case 15:                                                        //  новые названия для нагревателя 1 и 2
                LinearLayout lay_selheat;
                String str_n = txtGAS_EL.getText().toString().substring(12, txtGAS_EL.getText().toString().length());

                try{
                   if(str_n.indexOf(heater1) != -1){ flag_sel_HEATER1 = true; }
                   else flag_sel_HEATER1 = false;
                }catch(Exception rr){;}

                // if(str_n.indexOf(heater1)){ flag_sel_HEATER1 = true; }

                lay_selheat = (LinearLayout) getLayoutInflater().inflate(R.layout.lay_set_tmp, null);
                final TextView txt_selheat = lay_selheat.findViewById(R.id.fonlay_txt);
                final EditText edit_selheat = lay_selheat.findViewById(R.id.settmp_txt);
                final LinearLayout lay100 = lay_selheat.findViewById(R.id.lay_for_txtedit);
                final TextView but_selheatY = lay_selheat.findViewById(R.id.text_ye);
                final TextView but_selheatN = lay_selheat.findViewById(R.id.text_no);
                but_selheatY.setText("Применить"); but_selheatN.setText("Отмена");
                lay100.removeView(edit_selheat);
                final EditText edit_selheat1 = new EditText(this);
                final EditText edit_selheat2 = new EditText(this);
                lay100.addView(edit_selheat1); lay100.addView(edit_selheat2);
                edit_selheat1.setHint(heater1); edit_selheat2.setHint(heater2);
                txt_selheat.setText("Введите новые названия для нагревателя 1 и 2\n");

                builder.setView(lay_selheat);
                alert = builder.create();
                alert.show();

                View.OnClickListener h_but_selheatY = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        if(edit_selheat1.getText().length()>1){ heater1 = edit_selheat1.getText().toString();  Log.d(tag, "heater1:"+heater1+":");}
                        if(edit_selheat2.getText().length()>1){ heater2 = edit_selheat2.getText().toString(); Log.d(tag, "heater2:"+heater2+":");}
//                        saved_config("Name_heater1", heater1); saved_config("Name_heater2", heater2);
                        saved_config(SERVER_IP+"heater1", heater1); saved_config(SERVER_IP+"heater2", heater2);
                        if(flag_sel_HEATER1){
                            txtGAS_EL.setText("Нагреватель "+heater1);
                        }else{
                            txtGAS_EL.setText("Нагреватель "+heater2);
                        }
                        alert.dismiss();
                    }
                };
                but_selheatY.setOnClickListener(h_but_selheatY);
                View.OnClickListener h_but_selheatN = new View.OnClickListener() {        //
                    public void onClick(View v) {

                        alert.dismiss();
                    }
                };
                but_selheatN.setOnClickListener(h_but_selheatN);

            break;
            case 16:                                                    /// включить/выключить бойлер
                LinearLayout lay_boiler;
                lay_boiler = (LinearLayout) getLayoutInflater().inflate(R.layout.lay_set_tmp, null);
                final TextView txt_boiler = lay_boiler.findViewById(R.id.fonlay_txt);
                final TextView edit_boiler = lay_boiler.findViewById(R.id.settmp_txt);
                final LinearLayout lay11_b = lay_boiler.findViewById(R.id.lay_for_txtedit);
                final TextView but_ye_boiler = lay_boiler.findViewById(R.id.text_ye);
                final TextView but_no_boiler = lay_boiler.findViewById(R.id.text_no);
                lay11_b.removeView(edit_boiler);
                String sss;
                if(flag_boiler){sss = "Выключить Бойлер?\n";}
                else{sss = "Включить Бойлер?\n";}
                txt_boiler.setText(sss);
                builder.setView(lay_boiler);
                alert = builder.create();
                alert.show();
                View.OnClickListener butyes_B = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        if(flag_boiler){
                            cmd_send =  set_ONOFFboiler;  flag_boiler_out = false;
                            cmd_success = NNO;
                        }
                        else {
                            cmd_send = set_ONOFFboiler; flag_boiler_out = true;
                            cmd_success = OKK;
                        }
                        alert.dismiss();
                    }
                };
                but_ye_boiler.setOnClickListener(butyes_B);
                View.OnClickListener butno_B = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                };
                but_no_boiler.setOnClickListener(butno_B);
                break;
            case 17: //set_ONOFF_AlarmHeat
                LinearLayout lay_alarmONOF;
                lay_alarmONOF = (LinearLayout) getLayoutInflater().inflate(R.layout.lay_set_tmp, null);
                final TextView txt_alarm = lay_alarmONOF.findViewById(R.id.fonlay_txt);
                final TextView edit_alarm = lay_alarmONOF.findViewById(R.id.settmp_txt);
                final LinearLayout lay11_alarm = lay_alarmONOF.findViewById(R.id.lay_for_txtedit);
                final TextView but_ye_alarm = lay_alarmONOF.findViewById(R.id.text_ye);
                final TextView but_no_alarm = lay_alarmONOF.findViewById(R.id.text_no);
                lay11_alarm.removeView(edit_alarm);
                String sstx;
                if(!flag_AlarmOFF){sstx = "Запретить работу нагревателя?\n";}
                else{ sstx = "Разрешить работу нагревателя?\n";}
                txt_alarm.setText(sstx);
                builder.setView(lay_alarmONOF);
                alert = builder.create();
                alert.show();
                View.OnClickListener butyes_alarm = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        if(flag_AlarmOFF){
                            cmd_send =  set_ONOFF_AlarmHeat;  flag_AlarmOFF_out = false;
                            cmd_success = NNO;
                        }
                        else {
                            cmd_send = set_ONOFF_AlarmHeat; flag_AlarmOFF_out = true;
                            cmd_success = OKK;
                        }
                        alert.dismiss();
                    }
                };
                but_ye_alarm.setOnClickListener(butyes_alarm);
                View.OnClickListener butno_alarm = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                };
                but_no_alarm.setOnClickListener(butno_alarm);
                break;

        }

    }
    //////////////////////////////////////////////////////////////////////////
    void wait_save_mode_link(){

    }
    ///////////////////////////////////////////////////////////////////////////
    void dialog_IN_dial(int dialog){
        final AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch(dialog) {
                case 1:
                    LinearLayout lay_txt;
                    lay_txt = (LinearLayout) getLayoutInflater().inflate(R.layout.lay_set_tmp, null);
                    final LinearLayout lay_txtedit = lay_txt.findViewById(R.id.lay_for_txtedit);
                    final TextView txt_info_delIP = lay_txt.findViewById(R.id.fonlay_txt);
                    final EditText edit_gist = lay_txt.findViewById(R.id.settmp_txt);
//            edit_gist.setText(SERVER_IP_tmp);

                    final LinearLayout lay1111 = lay_txt.findViewById(R.id.fon_butt);
                    final TextView but_ye = lay_txt.findViewById(R.id.text_ye);
                    final TextView but_no = lay_txt.findViewById(R.id.text_no);
                    txt_info_delIP.setText("Удалить устройство с IP "+SERVER_IP_tmp+"?");
                    lay_txtedit.removeView(edit_gist);
//            lay1111.removeView(but_no);
                    builder.setView(lay_txt);
                    alert = builder.create();
                    alert.show();

                    View.OnClickListener handl_butyes = new View.OnClickListener() {        //
                        public void onClick(View v) {
                            flag_long_IP = false;
                            String str = "";
                            //String str_heater1 = "";
                            //String str_heater2 = "";
                            int count = 0;
                            if(num_save_ip>1) {
                                for (count = 0; count < num_save_ip; ++count) {             // после цикла получаем номер IP сервера следующий за удаляемым
                                    if (str.equals(SERVER_IP_tmp)) {
                                        break;
                                    } else {
                                        str = name_adr.get(count);
                                    }
                                }
                                name_adr.remove(count-1);
                                for (int i = 0; i < (count - 1); i++) {                      // все IP адреса списка до удаляемого
                                    //Log.d(tag, "name_adr." + i + "  " + name_adr.get(i));
                                }
                                adapter_for_nameIP.notifyDataSetChanged();      // после удаления IP адреса, обновляем список отображемых IP
                                num_save_ip--;
                                ///for (int i = 0; i < num_save_ip; i++)Log.d(tag, "name_adr." + i + "  " + name_adr.get(i));      /// проверка заполнения обновленного списка серверов
                                saved_config("num_save_ip", num_save_ip);                               // сохраняем новое кол-во подключенных используемых IP адресов
                                for (int i = 0; i < num_save_ip; i++){
                                    saved_config("server_IP"+(i+1), name_adr.get(i));     // переписываем  IP адреса с новыми индексами
                                    Log.d(tag, "server_IP"+i+" "+name_adr.get(i));
                                }
                                //saved_config("server_IP"+Integer.toString(num_save_ip), SERVER_IP);     //

                            }
                            alert.dismiss();
                        }
                    };
                    but_ye.setOnClickListener(handl_butyes);
                    View.OnClickListener handl_but_no = new View.OnClickListener() {        //
                        public void onClick(View v) {

                            flag_long_IP = false;
                            alert.dismiss();
                        }
                    };
                    but_no.setOnClickListener(handl_but_no);
                break;
                case 2:

                break;
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    String coder_base64str(String txt){
        String txt_base64 = "";
        try {
            byte[] bytes = txt.getBytes(UTF8_CHARSET);              // инициализация массива байтов символами полученной строки в формате UTF8_CHARSET
//            byte[] bytes = txt.getBytes(Charset.forName("UTF-8"));  // инициализация массива байтов символами полученной строки в формате UTF8_CHARSET
            txt_base64 = Base64.encodeToString(bytes, NO_WRAP);
        }catch(Exception e){ Log.d(tag, "Exception coder_base64str");}
        return txt_base64;
    }
    ///////////////////////////////////////////////////////////
        /////////////////////////timer/////////////////////////////

    class MyTimerTask extends TimerTask {                                       // таймер 250 мс
        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss", Locale.getDefault());
            final String strDate = simpleDateFormat.format(calendar.getTime());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clientTCP.send_staus_socket(str_tmp);
                    status_SERV = str_tmp[0];
                    if(status_SERV.equals("Соединение установлено")){connect_server = true;}
                    else {
                        connect_server = false;
                        txtGAS_EL.setTextColor(getResources().getColor(R.color.default_));
                    }
                    if(status_SERV.equals("Соединение закрыто")){
                        mRun = false;
                    }

                    if(data_read){
//                        mServerMessage = mServerMessage+"\n"+str_tmp[1];
                        mServerMessage = str_tmp[1];
                        pars_data(mServerMessage);
                        data_read = false;
                    }
                    else{

                        if(count_wait_answer > 4){                 // более 8 сек от сервера ничего не приходит - закрываем соединение и перезапускаем подкл. к серверу
                            count_wait_answer = 0;
                            Log.d(tag, "count_wait_answer "+count_wait_answer + " req NUM "+count_req);
                            Log.d(tag, "status client_TCP " + clientTCP.tcp_NET.getStatus());
                            if(status_SERV.equals("Соединение установлено")){
                                close_TCP();
                                Log.d(tag, "Соединение закрыто");
                            }
                        }
                    }
                    if(cmd_send == req_data)change_tmp = false;
                    handlstatus.sendEmptyMessage(0);
                    count_start_req++;
/////////////////////////////////////////////////////////////////
////       Каждую секунду, в случае не получения подтверждения на отпрвленную команду, дублируется посылка.
/////////////////////////////////////////////////////////////////
                    if(count_start_req >= 8) {                       // каждые 2 секунды запрашивается ответ, если нет касания экрана и не получен ответ на предыдущий запрос
                        if (!touchFlag && count_wait_answer == 0){
                            if(cmd_send != req_data) {
//                                if (connect_server) {
                                func_req_data(cmd_send);      // если команда не запрос телеметрии, то отправка команды серверу
//                                } else { cmd_send = req_data; }
                                if(cmd_send == load_def){
                                    count_load_def++;
                                    if(count_load_def>4){ cmd_send = req_data; count_load_def = 0; }
                                }
                            }
///////////////////////////////////////
//                            cmd_send = req_data;   //for debug
////////////////////////////////////////////
                        }
                        count_start_req = 0;                // сброс счетчика задающего интервал отправки запроса
                        count_wait_answer++;                // каждые 2 сек инкремент
                        SECOND_ = find_SECOND_(strDate);
//                        Log.d(tag, "2 second");
                        if(clientTCP.tcp_NET.getStatus().equals(FINISHED)){
                            clientTCP = new tcp_client(SERVER_IP, port_int);
                            Log.d(tag, "Сервер перезапущен!");
                        }
//                        Log.d(tag, "reconnect..." + netSSID_cur);
                    }
///////////////////////////////////////// для запуска диалогового окна калибровки температуры, удерживаем кнопку меню больше 10сек.
                    if(flag_toch_menu) {
                        time_toch_menu++;
                        if (time_toch_menu > 20){
                            if(!flag_calibrovka_run) dialog_show(5);            //Log.d(tag, "time_toch_menu >10");
                        }
                    }
////////////////////////////////////////
//                    Log.d(tag, "250 mS, "+"count_wait_answer = "+count_wait_answer+", count_start_req = "+count_start_req);
//                    Log.d(tag, "wait_connect.. "+netSSID_cur);
                    try {
                        cnt_wifi_status++;
                        if (cnt_wifi_status % 8 == 0) {                 // каждые 2 сек записываем в netSSID_cur имя подключенной сети
                            netSSID_cur = WIFI_obj.SSID_cur;
                            netSSID_cur = netSSID_cur.replace("\"", "");
                            Log.d(tag, "WifiInfo info netSSID_cur "+netSSID_cur);
                        }
                    } catch (Exception e){ Log.d(tag, "Exception WIFI_obj "+e);};
                }
            });
            handlstatus.sendEmptyMessage(0);
            count_start_req++;
            cnt_0_25_sec++;
            if(cnt_0_25_sec>2400)cnt_0_25_sec = 0; /// больше 10 мин - сброс

        }

    }
    //////////////////////////////////////одноразовый запуск таймера/////////////////////////
    class TMR_one_t extends TimerTask {

        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy  HH:mm:ss");
            final String strDate = simpleDateFormat.format(calendar.getTime());
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if(read_config_str("SINHRO").equals(cur_data_cl)){;}
                    else{ cmd_send = synchro; }
                }
            });
        }
    }
    int find_SECOND_(String str_time){          // получение системного времени в секундах
        int sec = 0;
        String hour, min, SEC_;
        char [] tmr= new char[8];
        try{
            str_time.getChars(11, 19, tmr, 0);
            hour = ""+tmr[0]+tmr[1];
            min = ""+tmr[3]+tmr[4];
            SEC_ = ""+ tmr[6]+tmr[7]; //sec = 352;
            sec = (Integer.parseInt(hour))*60*60+Integer.parseInt(min)*60+Integer.parseInt(SEC_);
//            Log.d(tag, "hour  :: "+Integer.parseInt(hour)+", min :: "+Integer.parseInt(min)+", sec :: "+Integer.parseInt(SEC_));
        }
        catch(Exception e){Log.d(tag, "SECOND_ ");}
        return sec;
    }
    ////////////////////////////////////////////////
    ///////////////////////отображение статуса подключения......
    Handler handlstatus = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            status.setText(status_SERV);
//            txt_dev.setText(mServerMessage);
//            Log.d(tag,mServerMessage);
            if(status_SERV.equals("подключаюсь..")){
                count_lost_connect++;
                if(count_lost_connect>8) {
                    progressBar.setVisibility(View.VISIBLE); // прогрессбар крутится
                }
                if(count_lost_connect>1000)count_lost_connect = 0;
            }
            else {
                count_lost_connect = 0;
                progressBar.setVisibility(View.GONE);                                       // прогрессбар  невидим
            }
        }
    };
    void close_TCP(){
        clientTCP.tcp_close();
        mRun = false;

    }
    void connect_TCP(){
//        if(!connect_server && !mRun) clientTCP = new tcp_client(SERVER_IP, port_int);
//        else{ close_TCP(); }
        clientTCP = new tcp_client(SERVER_IP, port_int);
    }
    void show_txt_ex(){
        Toast.makeText(this,"Введены не все данные!", Toast.LENGTH_SHORT).show();
    }
    /////////////////////сохранение и чтение сетевых настроек.......
    void saved_config(String str, int par){
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(str, par);
        ed.commit();
    }
    void saved_config(String str, String par){
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(str, par);
        ed.commit();
    }
    String read_config_str(String str) {
        String tmp = "";
        sPref = getPreferences(MODE_PRIVATE);
        tmp = sPref.getString(str, "");
        return tmp;
    }
    int read_config_int(String str){
        int tmp = 0;
        sPref = getPreferences(MODE_PRIVATE);
        tmp = sPref.getInt(str, 0);
        return tmp;
    }

    ////////////////////////////////////////////////////////
    public void onClick(View v) {
        if(visible_set_TMP != 2) {
            if (rdbutCheck) {
                rdBut.setChecked(false);
                rdbutCheck = false;
                mode_heart_color.setTextColor(getResources().getColor(R.color.default_));
                mode_tarif = false;
                work_TARIF_ = NNO;
                cmd_success = work_TARIF_ + 2;
                cmd_send = set_work_TARIF;
                //           saved_config("bool_mode_tarif", mode_tarif);
            } else {
                dialog_show(6);
            }
        }
        Log.d(tag, "rdBut.isChecked2: "+rdBut.isChecked());
    }
    void pars_data(String str){

        int num_r;
        try {
            str.getChars(0, bufTCPin.length, bufTCPin, 0); // копирование символов строки в массив bufTCPin
            num_r = ((int) bufTCPin[46]) | (((int) bufTCPin[47]) << 8);  // получаем номер последнего запроса
            tele = new Telemetry(bufTCPin, cmd_send);
            if (tele.crc_flag) {
                if (!change_tmp && !touchFlag) {
                    if (visible_set_TMP == 1) {

                        eX = (tele.set_tmp_serv - 5) * step;
                        showtmp.sendEmptyMessage(0);
                        Log.d(tag, "mode show visible_AIR " + tele.set_tmp_serv);
                    }
                    if (visible_set_TMP == 2) {
                        eX = (tele.boiler_settmp - 5) * step_boiler;
                        showtmp.sendEmptyMessage(0);
                        Log.d(tag, "mode show visible_BOILER " + tele.boiler_settmp + " gist " + tele.boiler_tmp_gist + " flag " + tele.flag_boiler);
                    }
                } else {
                    if (!touchFlag) count_change_set_tmp++;
                    if (count_change_set_tmp > 2) {
                        count_change_set_tmp = 0;
                        change_tmp = false;
                    } // через 2 ответа сервера, после отпускания пальца с экрана, снова включаем отображении установленной сервером температуры
                }
///////////////////
                Log.d(tag, "func pars_data " + "change_tmp: " + change_tmp + "  count_change_set_tmp: " + count_change_set_tmp);
                if (tele.work_TARIF == OKK) {
                    int rr;
                    rdBut.setChecked(true);
                    rdbutCheck = true;
                    mode_heart_color.setTextColor(getResources().getColor(R.color.GREEN_));
                    rr = tele.gist_TARIF;
                    mode_heart_color.setText("Учитывать дневной и ночной тариф" + " ±" + rr + "˚C" + "\n" + "день : " + tele.time_D + "  ночь : " + tele.time_N);
                    mode_tarif = true;
                } else {
                    rdBut.setChecked(false);
                    rdbutCheck = false;
                    mode_heart_color.setTextColor(getResources().getColor(R.color.default_));
                    mode_heart_color.setText("Учитывать дневной и ночной тариф");
                    mode_tarif = false;
                }
                if (tele.gas == OKK) {
                    txtGAS_EL.setTextColor(getResources().getColor(R.color.Color_GAS));
                    txtGAS_EL.setText("Нагреватель " + heater1 + "," + tele.work_GAS_);
                } else {
                    txtGAS_EL.setTextColor(getResources().getColor(R.color.Color_EL));
                    txtGAS_EL.setText("Нагреватель " + heater2 + "," + tele.work_GAS_);
                }

                if (tele.status_busy == OKK) {
                    busy = OKK;
                } else {
                    busy = NNO;
                }
/////////////////////////////
                if (tele.cool_ON) {
                    cool_ON = true;
                } else {
                    cool_ON = false;
                }
                if (tele.flag_boiler) {
                    flag_boiler = true;
                } else {
                    flag_boiler = false;
                }
                if (tele.flag_AlarmOFF == 1) flag_AlarmOFF = true;
                else flag_AlarmOFF = false;
                tmp_A = tele.tmp_A;
                tmp_W = tele.tmp_W;
                tmp_O = tele.tmp_Out;
                tmp_B = tele.tmp_Boil;
                txt_gist_B.setText("гистерезис " + tele.boiler_tmp_gist + "˚C");
////////////////////////////////////
//            Log.d(tag, "sys_time :: "+ tele.time_data_serv);
//                Log.d(tag, "sys_time_char:: "+ stt);
                Log.d(tag, "boiler_tmp_gist " + tele.boiler_tmp_gist + " flag_boiler " + tele.flag_boiler);
//////////////////////////////////////////////////////////
                handlinfo.sendEmptyMessage(0);
                if (cmd_send != req_data) {                  // если была отправлена какая-то команда, отличная от запроса телеметрии, то ждем подтверждения получения ее от сервера и дублируем запросы этой же командой
                    boolean flag_cmdOK = false;
                    Log.d(tag, " cmd_success " + cmd_success + " tele.cmd_succ " + tele.cmd_succ);
                    if (cmd_success == tele.cmd_succ && count_req == tele.count_COMAND)
                        flag_cmdOK = true;
                    //        if(cmd_success == tele.cmd_succ )flag_cmdOK = true;
                    if (cmd_send == tele.COMAND && count_req == tele.count_COMAND || flag_cmdOK) {
                        Log.d(tag, "cmd_send " + cmd_send + " tele.COMAND " + tele.COMAND + ", count_req " + count_req + " tele count_req " + tele.count_COMAND + " flag_cmdOK " + flag_cmdOK);
                        //                        wait_answer_server = 0;
                        if (cmd_send == synchro) {
                            show_txt_toast("Устройство синхронизировано!");
                            saved_config("SINHRO", cur_data_cl);
                        } else {
                            show_txt_toast("Выполнено!");
                        }
                        if (cmd_send == set_link) {
                            String sstt = "", sst_p = "";
                            for (int y = 0; y < 12; y++) {
                                sstt = sstt + bufTCPin[y + 19];
                            }
                            for (int y = 0; y < 4; y++) {
                                sst_p = sst_p + bufTCPin[y + 31];
                            }
                            Log.d(tag, "IP_adr " + sstt + "  " + "port_N  " + sst_p);
                            SERVER_IP = "" + bufTCPin[19] + bufTCPin[20] + bufTCPin[21] + "." + bufTCPin[22] + bufTCPin[23] + bufTCPin[24] + "." + bufTCPin[25] + bufTCPin[26] + bufTCPin[27] + "." + bufTCPin[28] + bufTCPin[29] + bufTCPin[30];
                            port = "" + bufTCPin[31] + bufTCPin[32] + bufTCPin[33] + bufTCPin[34];
                            initESP_ON = false;
                            saved_config("server_IP", SERVER_IP);
                            saved_config("server_port", port);
                           ////////////////////////////////
                            num_save_ip++;
                            saved_config("num_save_ip", num_save_ip);                               // сохраняем количество известных IP адресов num_save_ip
                            saved_config("server_IP" + Integer.toString(num_save_ip), SERVER_IP);     // сохраняем новое устройство в списке IP адресов
                       //     saved_config(SERVER_IP+"heater1", heater1);     // имя 1 нагревателя для конкретного IP
                       //     saved_config(SERVER_IP+"heater2", heater2);     // имя 2 нагревателя для конкретного IP
                            Log.d (tag, "new num_save_ip "+num_save_ip+" server_IP "+SERVER_IP);
                            ////////////////////////////////
                            port_int = Integer.parseInt(port);
                            SERVER_IP = "192.168.4.1";
                            Log.d(tag, "SERVER_IP " + SERVER_IP + " PORT_SERV " + port);
                        }
                        if (cmd_send == config_mail) {
                            initESP_ON = false;
                        }
                        cmd_send = req_data;                // команда управления прошла успешно, в переменную command помещаем команду запроса телеметрии
                        count_req++;
                        saved_config("num_req", count_req);
                        cmd_success = 0;
                    } else {
                        String sh = "";
                        if (cmd_send != set_link && cmd_send != config_mail) {
                            switch (count_wait_send_com) {
                                case 0:
                                    sh = ">>>......>>>";
                                    break;
                                case 1:
                                    sh = "...>>>......";
                                    break;
                                case 2:
                                    sh = "......>>>...";
                                    break;
                                case 3:
                                    sh = "..>>>......>";
                                    break;
                            }
                            show_txt_toast(sh);
                        }
                        count_wait_send_com++;
                        if (count_wait_send_com > 3) count_wait_send_com = 0;
                        Log.d(tag, "command " + cmd_send + ", answer comand " + tele.COMAND + " answer_req " + tele.count_COMAND);
                    }

                }
///////////////////////////////////////
                String sstt = "", sst_p = "";
                for (int y = 0; y < 12; y++) {
                    sstt = sstt + bufTCPin[y + 19];
                }
                for (int y = 0; y < 4; y++) {
                    sst_p = sst_p + bufTCPin[y + 31];
                }
                Log.d(tag, "IP_adr " + sstt + "  " + "port_N  " + sst_p);
///////////////////////////////////////
                if (count_req > 255) count_req = 1;
                count_wait_answer = 0;                  // в случае успешного приема сбрасываем счетчик ожидания ответа
                //           }
            }
        } catch (Exception e) {
            Log.d(tag, "Exception pars_data client func");
        }

    }
    ////////////////////////////////////////////////////////запрос телеметрии у сервера
    void func_req_data(int command){
        String cur_data;
        int [] dim = new int[64];
        cur_data = cur_data();
 //       cur_data.getChars(0, cur_data.length(), bufTCPout, 50);  // копирование символов строки cur_data с 0 по 10 (дата в формате 10 символов) в массив символов bufTCPout начиная с 4-го
        Log.d(tag, "Send_comand, count REQ; " + count_req+", command: "+cmd_send+", tarif: "+work_TARIF_+", gist: "+gisteresis_TARIF_);
//////////////////////////////////////////////////////////////
        if(command != set_koef){
            Send_COM send = new Send_COM(bufTCPout, dim, command);
        }
/////////////////////////////////////////////////////////////
        str_mess = new String(bufTCPout);    // копирование массива символов в строку
//        str_mess = "privet server!";
        /////////////////////////////////////////////
        //for(int i = 64; i<128; i++){Log.d(tag, "TCPout data ["+i+"]"+Integer.toString((int)bufTCPout[i]+(int)bufTCPout[i+64]));}
        /////////////////////////////////////////////
        new Thread(new Runnable() {
            @Override
            public void run() {
                clientTCP.send_m(str_mess);
            }}).start();
        /////////////////////////////////////////////
    }
    String cur_data(){
        Date curTime = new Date();
//        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy  HH:mm");  // задаем формат даты
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");  // задаем формат даты
        String sdt_= sdf.format(curTime);
//        txt_dev.setText(sdt_);
        return sdt_;
    }
    ///////////////////////отображение статуса подключения......
    Handler handlinfo = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String log_serv = "";
            try {
                log_serv = "\n"+"Дата и время сервера "+tele.time_data_serv + "\n" +
                        tele.time_work + "\n" + tele.time_heat + "\n" + tele.time_powOFF +
                        "\n" +tele.status_pow+"\n"+tele.charge_accum;

                        //"время МК(сек) "+tele.time_MC+
                        //":"+(int) bufTCPin[92]+":"+(int)bufTCPin[93]+":"+(int) bufTCPin[94];
 ///////////////////////////////////////////////////////////////
                if(flag_calibrovka_run){
                   // log_serv ="АЦП1,  АЦП2= "+ Integer.toString(tmp_A)+" : "+Integer.toString(tmp_W);
                   log_serv =   "\n"+
                                "АЦП возд.              "+Integer.toString(tmp_A)+"\n"
                                +"АЦП теплоносит. "+Integer.toString(tmp_W)+"\n"
                                +"АЦП бойлер           "+Integer.toString(tmp_B)+"\n"
                                +"АЦП улица             "+Integer.toString(tmp_O);
                }
////////////////////////////////////////////////////////////////
                txt_dev.setText(log_serv);
                if (tele.status_heat.equals("Нагрев включен")) {
                    Status_heat.setTextColor(Color.RED);
                } else {
                    Status_heat.setTextColor(getResources().getColor(R.color.default_));
                }
                Status_heat.setText(tele.status_heat);
//                tmp_air.setText("Воздух                  "+tele.air_tmp+"˚C");
//                tmp_water.setText("Теплоноситель   "+tele.water_tmp+"˚C");
                tmp_air.setText(tele.air_tmp);
                tmp_water.setText(tele.water_tmp);
                tmp_out.setText(tele.out_tmp);
                tmp_boiler.setText(tele.boiler_tmp);
////////////////////////////////////
 /*               String sst = mServerMessage.substring(0, 4);            // копирует в строку "sst" первые 4 символа (sst[0], sst[1], sst[2], sst[3]) строки "mServerMessage"
                if(sst.equals("EZAP")) { log_serv = mServerMessage;}
                else { log_serv = "ERR!"; }
                txt_dev.setText(log_serv);
*/
/*                char[] str_char= new char[10]; int tt;
                mServerMessage.getChars(125, 128, str_char, 0);
                tt = ((int)str_char[1])&0xff;       //253 - 132;
//                str_char[1] = 174;
                txt_dev.setText("::"+str_char[0]+" "+tt+" "+str_char[2]);
//                txt_dev.setText(mServerMessage);*/
/////////////////////////////////////

            }catch(Exception e){Log.d(tag, "Exception out info!");}
        }
    };
    void show_txt_toast(String str){
        Toast.makeText(this,str, Toast.LENGTH_SHORT).show();
    }
    ////////////////////////////////////////////////////////
    @Override
    public void onBackPressed() {
        close_TCP();
        finish();
    }
    @Override
    public void onDestroy() {

        super.onDestroy();
    }
    /////////////////////////////////////////////////
    private Drawable createLayerDrawable(int ID_drw, float x, float y) {     //получаем объект Drawable из ресурсов (id = "ID_drw") нужной ширины "x"  и высоты "y"
        float xx = (float)width*x;
        float yy = (float)hight*y;
        Bitmap bitm = BitmapFactory.decodeResource(getResources(), ID_drw);
        Bitmap btm = bitm.createScaledBitmap(bitm, (int)xx, (int)yy, true);
        BitmapDrawable drawable0 = new BitmapDrawable(getResources(), btm);
//    BitmapDrawable drawable0 = (BitmapDrawable) getResources().getDrawable(
//            R.drawable.bg_main1920);
        Log.d(tag, "widht "+btm.getWidth()+" hight "+btm.getHeight());

        return drawable0;
    }
    /////////////////////////////////////////////////////////////////////

    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.menu); // Для Android 4.0
        MenuItem itemCOOL = popupMenu.getMenu().getItem(4);
        MenuItem itemBoiler = popupMenu.getMenu().getItem(5);
        MenuItem itemAlarmOFF = popupMenu.getMenu().getItem(11);
//        itemCOOL.setCheckable(true);
//        cool_ON = false;

        if(cool_ON){
            itemCOOL.setChecked(true);
        }
        else{
            itemCOOL.setChecked(false);
        }
//        Log.d(tag, "cool_ON "+cool_ON);
        if (flag_boiler) {
            itemBoiler.setChecked(true);
        }
        else {
            itemBoiler.setChecked(false);
        }
        if (flag_AlarmOFF) {
            itemAlarmOFF.setChecked(true);
        }
        else {
            itemAlarmOFF.setChecked(false);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.menu_1:
                        dialog_show(1);
                        break;
                    case R.id.menu_2:
                        dialog_show(2);
                        break;
                    case R.id.menu_3:
                        dialog_show(3);
                        break;
/*                    case R.id.menu_4:
                        dialog_show(5);
                        break; */
                    case R.id.menu_4:
                        dialog_show(16);
                        break;
                    case R.id.menu_5:
                        dialog_show(4);
                        break;
                    case R.id.menu_6:
//                showDialog(7);
                        dialog_show(7);
                        break;
                    case R.id.menu_7:
//                dialog_show(5);
                        close_TCP();
                        finish();
                        break;
                    case R.id.menu_8:
                        dialog_show(9);
//////////////////////////////////////////////
                        break;
                    case R.id.menu_9:
                        dialog_show(8);
                        break;
 /*                   case R.id.menu_10:
//                        dialog_show(10);          // использовалось для работы с китайским термостатом
                        break;*/
                     case R.id.menu_10:
                         Intent intent;
                         String[] TO = {"evan77@ro.ru"};
                         //              String[] CC = {"evan77@bk.ru"};

                         intent = new Intent(Intent.ACTION_SEND);
                         intent.setData(Uri.parse("mailto:"));
                         intent.setType("text/plain");
                         intent.putExtra(Intent.EXTRA_EMAIL, TO);
                         //              intent.putExtra(Intent.EXTRA_CC, CC);
                         intent.putExtra(Intent.EXTRA_SUBJECT, "Smart Termo, обратная связь");
                         intent.putExtra(Intent.EXTRA_TEXT, "Вопросы по работе приложения и модуля board_SMART1:\n\n");
                         try {
                             startActivity(Intent.createChooser(intent, "Отправка письма..."));
//                            finish();
                             Log.d(tag, "Finished sending email...");
                         } catch (android.content.ActivityNotFoundException ex) {
                             Toast.makeText(MainActivity.this, "There is no email client installed", Toast.LENGTH_SHORT).show();
                         }
                        break;
                    case R.id.menu_11:
                        dialog_show(11);
                        break;
                    case R.id.menu_12:
                        dialog_show(14);
                        break;
                    case R.id.menu_13:
                        dialog_show(17);
                        break;
                }

                return true;
            }
        });
        MenuPopupHelper menuHelper = new MenuPopupHelper(this, (MenuBuilder) popupMenu.getMenu(), v);
        menuHelper.setForceShowIcon(true);
        menuHelper.setGravity(Gravity.END); menuHelper.show();

    }
/////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////
//void save_koef(String A1, String A2, String A3, String A4, String A1E, String A2E, String A3E, String A4E ){ // расчет и сохранение коэфф. для функции y = A1*X^3+A2*X^2+A3*X+A4
void save_koef(String A1, String A2, String A3, String A4, String A1E, String A2E, String A3E, String A4E, String A0, String A0E ){ // расчет и сохранение коэфф. для функции y = A0*X^4+A1*X^3+A2*X^2+A3*X+A4

    coeff_resist_from_tmp[0] = (int)((Double.parseDouble(A1)*10000.0));
    coeff_resist_from_tmp[1] = (int)((Double.parseDouble(A2)*10000.0));
    coeff_resist_from_tmp[2] = (int)((Double.parseDouble(A3)*10000.0));
    coeff_resist_from_tmp[3] = (int)((Double.parseDouble(A4)*10000.0));
    coeff_resist_from_tmp[4] = Integer.parseInt(A1E);
    coeff_resist_from_tmp[5] = Integer.parseInt(A2E);
    coeff_resist_from_tmp[6] = Integer.parseInt(A3E);
    coeff_resist_from_tmp[7] = Integer.parseInt(A4E);
    coeff_resist_from_tmp[8] = (int)((Double.parseDouble(A0)*10000.0));   //
    coeff_resist_from_tmp[9] = Integer.parseInt(A0E);                     // для нулевого члена степень не может быть больше 15 и она всегда отрицательна, так как значение размещаем в байте отвечающем за знаки
//    Log.d(tag, " : A1= "+A1+" :A2 = "+A2+" :A3 = "+A3+" :A4 = "+A4+":::"+coeff_resist_from_tmp[0]);
//    Log.d(tag, " : A1E= "+A1E+" :A2E = "+A2E+" :A3E = "+A3E+" :A4E = "+A4E);
    Log.d(tag, " :A0 = "+A0+" : A1= "+A1+" :A2 = "+A2+" :A3 = "+A3+" :A4 = "+A4+":::"+coeff_resist_from_tmp[0]);
    Log.d(tag, " : A0E= "+A0E+" : A1E= "+A1E+" :A2E = "+A2E+" :A3E = "+A3E+" :A4E = "+A4E);

}
    void send_koef(){               // сохранение коэфф. в массиве bufOUT для отправки контроллеру
        ////////////////////////при отрицательных значениях //////////////////////////////
        int [] dim = new int[64];
        
        dim[29] = 0;                                      // сбрасываем знаки коэф.
        if(coeff_resist_from_tmp[0]<0){
            dim[29]= dim[29] | 0x01;                                // в случае отрицательного значения выставляем бит 1
            coeff_resist_from_tmp[0] = coeff_resist_from_tmp[0]*-1;             // переводим в положительное число
        }
        if(coeff_resist_from_tmp[1]<0){
            dim[29]= dim[29] | 0x02;                                // в случае отрицательного значения выставляем бит 2
            coeff_resist_from_tmp[1] = coeff_resist_from_tmp[1]*-1;             // переводим в положительное число
        }
        if(coeff_resist_from_tmp[2]<0){
            dim[29]= dim[29] | 0x04;                                // в случае отрицательного значения выставляем бит 3
            coeff_resist_from_tmp[2] = coeff_resist_from_tmp[2]*-1;             // переводим в положительное число
        }
        if(coeff_resist_from_tmp[3]<0){
            dim[29]= dim[29] | 0x08;                                // в случае отрицательного значения выставляем бит 4
            coeff_resist_from_tmp[3] = coeff_resist_from_tmp[3]*-1;             // переводим в положительное число
        }
        if(coeff_resist_from_tmp[9]<0){ coeff_resist_from_tmp[9] = coeff_resist_from_tmp[9]*-1; }
        dim[29]= dim[29] | (coeff_resist_from_tmp[9]<<4);           // записываем показатель степени нулевого члена в dim[29], в первых чеиырех битах хранятся знаки для А1-А4
//////////////////////////////////////////////////////////////////////////////////
        dim[30] = coeff_resist_from_tmp[0]&0xff;
        dim[31] = (coeff_resist_from_tmp[0]>>8)&0xff;
        dim[32]= (coeff_resist_from_tmp[0]>>16)&0xff;

        dim[34] = coeff_resist_from_tmp[1]&0xff;
        dim[35] = (coeff_resist_from_tmp[1]>>8)&0xff;
        dim[36]= (coeff_resist_from_tmp[1]>>16)&0xff;
//        dim[15]= (a>>24)&0xff;
        dim[38] = coeff_resist_from_tmp[2]&0xff;
        dim[39] = (coeff_resist_from_tmp[2]>>8)&0xff;
        dim[40]= (coeff_resist_from_tmp[2]>>16)&0xff;

        dim[42] = coeff_resist_from_tmp[3]&0xff;
        dim[43] = (coeff_resist_from_tmp[3]>>8)&0xff;
        dim[44]= (coeff_resist_from_tmp[3]>>16)&0xff;

        if(coeff_resist_from_tmp[8]<0){ coeff_resist_from_tmp[8] = coeff_resist_from_tmp[8]*-1;  dim[48] = 0x80; }  // в случае отрицательного значения мантиссы в старшем байте в стпршем разряде рисуем 1
        dim[46] = coeff_resist_from_tmp[8]&0xff;
        dim[47] = (coeff_resist_from_tmp[8]>>8)&0xff;
        dim[48]= dim[48] | ((coeff_resist_from_tmp[8]>>16)&0xff);
////////////////////////обработка основания и порядка степени //////////////////////////////
        dim[33]= 0; dim[37]= 0; dim[41]= 0; dim[45]= 0;
        if(coeff_resist_from_tmp[4]<0){
            dim[33]= 0x80;                                                // в случае отрицательного значения выставляем старший бит в 1
            coeff_resist_from_tmp[4] = coeff_resist_from_tmp[4]*-1;             // переводим в положительное число
        }
        if(coeff_resist_from_tmp[5]<0){
            dim[37]= 0x80;                                                // в случае отрицательного значения выставляем старший бит в 1
            coeff_resist_from_tmp[5] = coeff_resist_from_tmp[5]*-1;             // переводим в положительное число
        }
        if(coeff_resist_from_tmp[6]<0){
            dim[41]= 0x80;                                                // в случае отрицательного значения выставляем старший бит в 1
            coeff_resist_from_tmp[6] = coeff_resist_from_tmp[6]*-1;             // переводим в положительное число
        }
        if(coeff_resist_from_tmp[7]<0){
            dim[45]= 0x80;                                                // в случае отрицательного значения выставляем старший бит в 1
            coeff_resist_from_tmp[7] = coeff_resist_from_tmp[7]*-1;             // переводим в положительное число
        }
        ;
        dim[33]= dim[33] | coeff_resist_from_tmp[4];
        dim[37]= dim[37] | coeff_resist_from_tmp[5];
        dim[41]= dim[41] | coeff_resist_from_tmp[6];
        dim[45]= dim[45] | coeff_resist_from_tmp[7];
        //////////////////////////////////////////////
        flag_calibrovka_run = false;

        Send_COM send = new Send_COM(bufTCPout, dim, set_koef);
        cmd_send = set_koef;
        //////////////////////////////////////////
        String deb = "";
//        for(int i = 29; i< 46; i++){deb = deb + ":"+ i+" =" +dim[i]+"\n"; }
        for(int i = 29; i< 49; i++){deb = deb + ":"+ i+" =" +dim[i]+"\n"; }
        Log.d(tag, "COEFF = \n"+deb);
        //////////////////////////////////////////

    }
    void func_get_time_tarif(){
        /////////////////////////////////
        int hour, min;
        String prefix_hour = "", prefix_min = "";
        try {
            hour = time_Night_int / 3600;                              // находим часы
            min = (time_Night_int % 3600) / 60;                          // находим минуты
            if(hour<10) prefix_hour = "0";
            if(min<10) prefix_min = "0";
            time_Night = prefix_hour+Integer.toString(hour) + ":" + prefix_min+Integer.toString(min);

            prefix_hour = ""; prefix_min = "";
            hour = time_Day_int / 3600;                              // находим часы
            min = (time_Day_int % 3600) / 60;                          // находим минуты
            if(hour<10) prefix_hour = "0";
            if(min<10) prefix_min = "0";
            time_Day = prefix_hour+Integer.toString(hour) + ":" + prefix_min+Integer.toString(min);
        }catch(Exception e){Log.d(tag, "func_get_time_tarif");}
        ///////////////////////////////////
    }
    ///////////////////////////////////////////////////////////////////////////
    protected Dialog onCreateDialog(int id) {

        if (id == 71) {
            TimePickerDialog tpd = new TimePickerDialog(this, myCallBack, myHourD, myMinuteD, true);
            return tpd;
        }
        if (id == 81) {
            TimePickerDialog tpd = new TimePickerDialog(this, myCallBackN, myHourN, myMinuteN, true);
            return tpd;
        }
        if(id == 91){
            show_txt_toast(" SERVER_IP no "+SERVER_IP_tmp);
            /*
            final EditText gist_v = new EditText(MainActivity.this);
            gist_v.setHint("0.75");
            int type = InputType.TYPE_CLASS_NUMBER |  InputType.TYPE_NUMBER_FLAG_DECIMAL;           // задаем тип ввода данных в формате double
            gist_v.setInputType(type);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("Smart Termo")
                    .setMessage("Введите значение температуры гистерезиса:")
                    .setView(gist_v)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if(gist_v.getText().length() == 0){
                                gisteresis_TMP_ = 75;  // 0.75*100, если не было введено никаких данных
                            }
                            else{
                                String sstt;
                                sstt = gist_v.getText().toString();
                                gisteresis_TMP_ = (int)(Double.parseDouble(sstt)*100);
                            }
//                            status_mode_GAS = OKK;
                            cmd_success = status_mode_GAS+1;
                            cmd_send = set_GAS;
//                            USB_sendDEV();
                        }
                    });
            return builder.create();
            */
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LinearLayout laygist = (LinearLayout) getLayoutInflater().inflate(R.layout.lay_set_tmp, null);
            laygist = (LinearLayout) getLayoutInflater().inflate(R.layout.lay_set_tmp, null);
            final TextView txt_gist = laygist.findViewById(R.id.fonlay_txt);
            final TextView edit_gist = laygist.findViewById(R.id.settmp_txt);
            int type = InputType.TYPE_CLASS_NUMBER |  InputType.TYPE_NUMBER_FLAG_DECIMAL;           // задаем тип ввода данных в формате double
            edit_gist.setInputType(type);
            edit_gist.setHint("0.75");
            final LinearLayout lay1111 = laygist.findViewById(R.id.fon_butt);
            final TextView but_ye = laygist.findViewById(R.id.text_ye);
            final TextView but_no = laygist.findViewById(R.id.text_no);
            txt_gist.setText("Введите значение температуры гистерезиса");
            but_ye.setText("Применить");
//            but_ye.setTextColor(Color.GRAY);
            lay1111.removeView(but_no);
            builder.setView(laygist);

            View.OnClickListener handl_butyes = new View.OnClickListener() {        //
                public void onClick(View v) {
                    if(edit_gist.getText().length() == 0){
                        gisteresis_TMP_ = 75;  // 0.75*100, если не было введено никаких данных
                    }
                    else{
                        String sstt;
                        sstt = edit_gist.getText().toString();
                        gisteresis_TMP_ = (int)(Double.parseDouble(sstt)*100);
                    }
                    if(gisteresis_TMP_<3500) {
                        cmd_success = status_mode_GAS + 1;
                        cmd_send = set_GAS;
                    }else{ show_txt_toast("Введите корретное значение!");}
                    dismissDialog(91);
                }
            };
            but_ye.setOnClickListener(handl_butyes);
            return builder.create();
        }

        return super.onCreateDialog(id);
    }
    ////////////////////////////////////////////////////////////////////////////
    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            myHourD = hourOfDay;
            myMinuteD = minute;
            handlinfo_DAY_NIGHT.sendEmptyMessage(0);
            count_dialogND = 1;
            Log.d(tag, "Time is " + myHourD + " hours " + myMinuteD + " minutes");
        }
    };
    TimePickerDialog.OnTimeSetListener myCallBackN = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            myHourN = hourOfDay;
            myMinuteN = minute;
            handlinfo_DAY_NIGHT.sendEmptyMessage(0);
            count_dialogND = 1;
            Log.d(tag, "Time is " + myHourN + " hours " + myMinuteN + " minutes");
        }
    };
    ///////////////////////////////////////////////////////////////////////
    void delay_ms(int time){
        try{
            Thread.sleep(time);
        }catch(Exception ee){ Log.d(tag, "Exception delay_ms"); }
    }
    @Override
    protected void onStart(){
        super.onStart();

    }
}
/*
подключение к заданной сети WIFI
String networkSSID = "test";
String networkPass = "pass";

WifiConfiguration conf = new WifiConfiguration();
conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
Then, for WEP network you need to do this:

conf.wepKeys[0] = "\"" + networkPass + "\"";
conf.wepTxKeyIndex = 0;
conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
For WPA network you need to add passphrase like this:

conf.preSharedKey = "\""+ networkPass +"\"";
For Open network you need to do this:

conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
Then, you need to add it to Android wifi manager settings:

WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
wifiManager.addNetwork(conf);
And finally, you might need to enable it, so Android connects to it:

List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
for( WifiConfiguration i : list ) {
    if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
         wifiManager.disconnect();
         wifiManager.enableNetwork(i.networkId, true);
         wifiManager.reconnect();

         break;
    }
 }
UPD: In case of WEP, if your password is in hex, you do not need to surround it with quotes.

////////////////////////////////////////////////////////////
WifiConfiguration wifiConfig = new WifiConfiguration();
wifiConfig.SSID = String.format("\"%s\"", ssid);
wifiConfig.preSharedKey = String.format("\"%s\"", key);

WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
//remember id
int netId = wifiManager.addNetwork(wifiConfig);
wifiManager.disconnect();
wifiManager.enableNetwork(netId, true);
wifiManager.reconnect();
////////////////////////////////////////////////////////////

 */