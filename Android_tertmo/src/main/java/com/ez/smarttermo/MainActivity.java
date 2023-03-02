package com.ez.smarttermo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ez.data_receive.ObjTelemetry;
import com.ez.data_receive.ReadyDataForScreen;
import com.ez.data_save.DataSave;
import com.ez.data_save.ReadWriteData;
import com.ez.dialog.DialogSelect;
import com.ez.dialog.VisualCommand;
import com.ez.screen.DisplayParam;

import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener,
        LoaderManager.LoaderCallbacks<ReadyDataForScreen>, DialogSelect.OnCompleteListener,
        VisualCommand.OnCompleteListener {

    private ImageView prg_img;
    private View item_img = null;
    private TextView tmpset;
    private TextView status;
    private TextView txt_dev;
    private TextView IP_adr;
    private TextView status_heat;
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
    private ProgressBar progressBar;
    Context cntx = this;
    WindowManager w;
    DisplayParam d;
    ObjTelemetry objTelemetry;
    DataSave dataSaves;
    ReadWriteData rwData;
    private HashMap<String, String []> dev;

    Loader<ReadyDataForScreen> loaderReceive;
//    Loader<ReadyDataForScreen> loaderSend;

    private int touchFlag = 0;
    private boolean flag_toch_menu = false;
    private boolean flag_calibrovka = false;
    private boolean rdbutCheck = false;
    private boolean change_tmp = false;

    private int progress = 0;
    private int visible_set_TMP = 0;            // 1 - отображается бар настройки температуры воздуха, 2 - настройка температуры воды бойлера
    private int time_toch_menu = 0;
    private int eX, eY, poseY;
    private final int IDLOADERGET = 1;
//    private final int IDLOADERSEND = 2;
    private byte cnt = 0;
    private byte prevcnt = 0;
    private int waitAnswercom = 0;
    private int tempScreen = 0;
    private boolean statusSock;
    Timer tmr;
    TimerTask timerTask;

    String tag = "tag";
    void log (String s) { Log.d(tag, s); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d (tag, "MainActivity onCreate");
        System.out.println("MainActivity System.out.println");
        w = getWindowManager();
        d = new DisplayParam(w, this);
        objTelemetry = new ObjTelemetry();
        rwData = new ReadWriteData(cntx);
        dataSaves = (DataSave)rwData.readData ();
        if (dataSaves == null) {
            dataSaves = new DataSave();
            rwData.saveData (dataSaves);
            dataSaves = (DataSave)rwData.readData ();
        }

        initView();
        if (dataSaves.get_dialogStart()) Startdialog.sendEmptyMessageDelayed(0, 5000);

        ////////// обработка события касания экрана ..........
        View root = findViewById(android.R.id.content).getRootView();
        root.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                System.err.println("Display If  Part ::->" + touchFlag);
                if(touchFlag != 0) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.d(tag, "onTouch DOWN oncreate");
                            eY = (int) event.getY();
                            poseY = eY;
                            switch (item_img.getId()) {
                                case R.id.menu:
                                    //time_toch_menu = 0;
                                    cnt = 0;
                                    flag_toch_menu = true;
                                    Log.d(tag, "PUSH menu!!!");
                                    showPopupMenu(item_img);                /// вывод меню
                                    break;
                                case R.id.fon_air:
                                    fon_air.setImageDrawable(d.createLayerDrawable(R.drawable.fon_air_press, (float) 0.45, (float) 0.25));
                                    break;
                                case R.id.fon_boiler:
                                    fon_boiler.setImageDrawable(d.createLayerDrawable(R.drawable.fon_boiler_press, (float) 0.45, (float) 0.25));
                                    break;
                                case R.id.fon_out:
                                    fon_out.setImageDrawable(d.createLayerDrawable(R.drawable.fon_out_press, (float) 0.45, (float) 0.25));
                                    break;
                                case R.id.fon_watter:
                                    fon_watter.setImageDrawable(d.createLayerDrawable(R.drawable.fon_watter_press, (float) 0.45, (float) 0.25));
                                    break;
                                case R.id.but_stat_img:
                                    but_stat_img.setImageDrawable(d.createLayerDrawable(R.drawable.butback_stat, (float) 0.73, (float) 0.05));
                                    break;
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            eX = (int) event.getX();
                            eY = (int) event.getY();
                            touchFlag = 2;
                            if (item_img.getId() == R.id.progress_img) {
                                if (visible_set_TMP == 2) {
                                    change_tmp = true;
                                    showtmp.sendEmptyMessage((eX / d.step_boiler) + 5);
                                }
                                if (visible_set_TMP == 1) {
                                    change_tmp = true;
                                    showtmp.sendEmptyMessage((eX/d.step) + 5);
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            touchFlag = 0;
                            switch (item_img.getId()) {
                                case R.id.fon_air:
                                    fon_air.setImageDrawable(d.createLayerDrawable(R.drawable.fon_air, (float) 0.45, (float) 0.25));
                                    if (visible_set_TMP == 2) visible_set_TMP = 0;
                                    show_bar_set_tmp (visible_set_TMP + 2);
                                    break;
                                case R.id.fon_boiler:
                                    fon_boiler.setImageDrawable(d.createLayerDrawable(R.drawable.fon_boiler, (float) 0.45, (float) 0.25));
                                    if (visible_set_TMP == 1) visible_set_TMP = 0;
                                    show_bar_set_tmp (visible_set_TMP + 4);
                                    break;
                                case R.id.fon_out:
                                    fon_out.setImageDrawable(d.createLayerDrawable(R.drawable.fon_out, (float) 0.45, (float) 0.25));
                                    break;
                                case R.id.fon_watter:
                                    fon_watter.setImageDrawable(d.createLayerDrawable(R.drawable.fon_watter, (float) 0.45, (float) 0.25));
                                    break;
                                case R.id.but_stat_img:
                                    lay_stat_soscet.setVisibility(View.GONE);
                                    lay_stat.setVisibility(View.VISIBLE);
                                    but_stat_img.setImageDrawable(d.createLayerDrawable(R.drawable.but_stat, (float) 0.73, (float) 0.05));
                                    break;
                                case R.id.progress_img:
                                    if (visible_set_TMP == 2) {
                                        change_tmp = true;
                                        showtmp.sendEmptyMessage((eX / d.step_boiler) + 5);
                                    }
                                    if (visible_set_TMP == 1) {
                                        change_tmp = true;
                                        showtmp.sendEmptyMessage((eX/d.step) + 5);
                                    }
                                    log ("sendCommand progress_img, visible_set_TMP = " + visible_set_TMP);
                                    break;
                            }
                            flag_toch_menu = false;
                            break;
                    }
                }
                return true;
            }
        });
        getSupportLoaderManager().initLoader(IDLOADERGET, null,  this);

    }
///////////END onCREATE//////////
    void startLoader () {
        getSupportLoaderManager().restartLoader(IDLOADERGET, null,  this);
    }

    @Override
    public Loader<ReadyDataForScreen> onCreateLoader(int id, Bundle args) {
//        log ("MainActivity onCreateLoader");
/*        if (id == IDLOADERSEND) {
            loaderSend = new LoaderData(this, args);
            return loaderSend;
        }
        else {
            loaderReceive = new LoaderData(this);
            return loaderReceive;
        }*/
        loaderReceive = new LoaderData(this);
        return loaderReceive;

    }

    @Override
    public void onLoadFinished(Loader<ReadyDataForScreen> loader, ReadyDataForScreen data) {
                log("MainActivity onLoadFinished");
//                log("statusSocket " + data.get_statusSocket());
//                log("airHomeTmp " + data.get_airHomeTmp());
//                log("airOutTmp " + data.get_airOutTmp());
//                log("boilerTmp " + data.get_boilerTmp());
//                log("waterTmp " + data.get_waterTmp());
//                log("airBoilerADC() " + data.get_airBoilerADC());
//                log("airHomeADC() " + data.get_airHomeADC());
//                log("airOutADC " + data.get_airOutADC());
//                log("airWaterADC " + data.get_airWaterADC());
//                log("boilerTmpGist() " + data.get_boilerTmpGist());
//                log("chargeAccum() " + data.get_chargeAccum());
//                log("command() " + data.get_command());
//                log("count_COMAND " + data.get_count_COMAND());
//                log("setAirTmp " + data.get_setAirTmp());
//                log("setBoilerTmp " + data.get_setBoilerTmp());
//                log("timeHeat() " + data.get_timeHeat());
//                log("timeServer " + data.get_timeServer());
//                log("timeWork " + data.get_timeWork());
//               log("statusWorkGAS " + data.get_statusWorkGAS());
//                log("statusSocket " + data.get_statusWorkTariff());

//        if (loader.getId() == IDLOADERGET) {
            //log ("test loader IDLOADERGET " + loaderReceive);
            if (data != null) {
                progressBar.setVisibility(View.GONE);
                updateScreen(data);
            } else {
                progressBar.setVisibility(View.VISIBLE);
                statusSock = false;
                status.setText("Ожидаю соединения...");
            }
            if (loaderReceive != null) getSupportLoaderManager().destroyLoader(IDLOADERGET);
            getSupportLoaderManager().restartLoader(IDLOADERGET, null, this);
//        }
        cnt++;
        if (flag_toch_menu && cnt > 5) {
            if (!flag_calibrovka) show_dialog(5);
            flag_calibrovka = true;
        }
    }

    @Override
    public void onLoaderReset(Loader<ReadyDataForScreen> loader) {

    }

    void handlbuckstat(){
        lay_stat_soscet.setVisibility(View.VISIBLE);
        lay_stat.setVisibility(View.GONE);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchFlag = 1;
                item_img = v;
                if (item_img.getId() == R.id.butback_stat_img) handlbuckstat();
                Log.d(tag, "onTouch _DOWN_ " + touchFlag);
                break;
            case MotionEvent.ACTION_UP:
                touchFlag = 3;
                Log.d(tag, "onTouch _UP_ " + touchFlag);
                break;
            default:
                break;
        }
        return false;
    }

    View.OnLongClickListener clickIP = new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
            show_dialog(12);
            return false;
        }
    };

    View.OnLongClickListener clickHeater = new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
            show_dialog(15);
            return false;
        }
    };


    public void onClick(View v) {
        if(visible_set_TMP != 2) {
            if (rdbutCheck) {
                rdBut.setChecked(false);
                rdbutCheck = false;
                mode_heart_color.setTextColor(getResources().getColor(R.color.default_));
                mode_heart_color.setText("Учитывать дневной и ночной тариф");
                objTelemetry.flagTarifOk = DataForSend.NO;
                objTelemetry.cmd = DataForSend.SET_WORK_TARIFF;
                sendCommand();
                //new VisualCommand ("Thread1", objTelemetry, cntx).start();
            } else {
                show_dialog (6);
            }
//            log ("objTelemetry.flagTarifOkCUR onclick " + objTelemetry.flagTarifOk);
        }
    }

    private void initView () {
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
        status_heat= (TextView)findViewById(R.id.status_mode);
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

        ml.setBackground(d.createLayerDrawable(R.drawable.new_fon, 1, 1)); ///
        ml_bar.setBackground(d.createLayerDrawable(R.drawable.new_bar_m, 1, (float)0.072));                     // установка бэкграунда нужного разрешения ( по горизонтали вместо 1 было 0,12, размер бэкграунда сохранялся при этом, но картинка сильно искажалась)
        //       ml_bar.setBackgroundResource(R.drawable.bar_m);                                                // установка бэкграунда нужного разрешения (слабые девайсы при этом могут тормозить)
        menu.setImageDrawable(d.createLayerDrawable(R.drawable.menu_pic, (float)0.12, (float)0.072));

        fon_air.setImageDrawable(d.createLayerDrawable(R.drawable.fon_air, (float)0.45, (float)0.25));          // коэфф. умножается на текущее разрешение по X, Y. т.е. дополнитильный масштабирующий коэф. вводить смысла нет. (0.45 * 1080, 0.25 * 1920)
        fon_watter.setImageDrawable(d.createLayerDrawable(R.drawable.fon_watter, (float)0.45, (float)0.25));
        fon_out.setImageDrawable(d.createLayerDrawable(R.drawable.fon_out, (float)0.45, (float)0.25));
        fon_boiler.setImageDrawable(d.createLayerDrawable(R.drawable.fon_boiler, (float)0.45, (float)0.25));
        but_stat_img.setImageDrawable(d.createLayerDrawable(R.drawable.but_stat, (float)0.73, (float)0.05));
        butback_stat_img.setImageDrawable(d.createLayerDrawable(R.drawable.but_stat, (float)0.73, (float)0.05));

        d.set_pos_but(fon_air, 60 * d.scale_X, 80 * d.scale_Y);
        d.set_pos_but(fon_out, 553 * d.scale_X, 80 * d.scale_Y);
        d.set_pos_but(fon_watter, 60 * d.scale_X, 567 * d.scale_Y);
        d.set_pos_but(fon_boiler, 553 * d.scale_X, 567 * d.scale_Y);

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  // убирание системной строки с экрана
        tmpset.setTextSize(TypedValue.COMPLEX_UNIT_PX, 80 * d.scale_Y);              // размер текста в пикселях
        IP_adr.setTextSize(TypedValue.COMPLEX_UNIT_PX, 54 * d.scale_Y);              // размер текста в пикселях
        status.setTextSize(TypedValue.COMPLEX_UNIT_PX, 56 * d.scale_Y);              // размер текста в пикселях
        txtGAS_EL.setTextSize(TypedValue.COMPLEX_UNIT_PX, 56 * d.scale_Y);           // размер текста в пикселях
        txt_gist_B.setTextSize(TypedValue.COMPLEX_UNIT_PX, 56 * d.scale_Y);          // размер текста в пикселях

        tmp_air.setTextSize(TypedValue.COMPLEX_UNIT_PX, 142 * d.scale_X);            // размер текста в пикселях
        tmp_water.setTextSize(TypedValue.COMPLEX_UNIT_PX, 142 * d.scale_X);          // размер текста в пикселях
        tmp_out.setTextSize(TypedValue.COMPLEX_UNIT_PX, 142 * d.scale_X);            // размер текста в пикселях
        tmp_boiler.setTextSize(TypedValue.COMPLEX_UNIT_PX, 142 * d.scale_X);         // размер текста в пикселях
        status_heat.setTextSize(TypedValue.COMPLEX_UNIT_PX, 68 * d.scale_X);         // размер текста в пикселях
        txt_dev.setTextSize(TypedValue.COMPLEX_UNIT_PX, 52 * d.scale_X);             // размер текста в пикселях
        mode_heart_color.setTextSize(TypedValue.COMPLEX_UNIT_PX, 46 * d.scale_X);    // размер текста в пикселях

        d.set_pos_but(tmp_air, 84 * d.scale_X, 364 * d.scale_Y);
        d.set_pos_but(tmp_water, 84*d.scale_X, 847 * d.scale_Y);
        d.set_pos_but(tmp_out, 578 * d.scale_X, 364 * d.scale_Y);
        d.set_pos_but(tmp_boiler, 578 * d.scale_X, 847 * d.scale_Y);
        d.set_pos_but(butback_stat_img, 160 * d.scale_X, 470 * d.scale_Y);

        if (d.width == 1080 && d.hight > 2100 && d.hight < 2200) {
            d.set_pos_but(status_heat, 280 * d.scale_X, 200 * d.scale_Y);
            d.set_pos_but(IP_adr, 180 * d.scale_X, 170 * d.scale_Y);
            d.set_pos_but(status, 180 * d.scale_X, 235 * d.scale_Y);
            d.set_pos_but(txtGAS_EL, 180 * d.scale_X, (float)30 * d.scale_Y);
            d.set_pos_but(but_stat_img, 160 * d.scale_X, 350 * d.scale_Y);
        } else {
            d.set_pos_but(status_heat, 280 * d.scale_X, 160 * d.scale_Y);
            d.set_pos_but(IP_adr, 260 * d.scale_X, 140 * d.scale_Y);
            d.set_pos_but(status, 260 * d.scale_X, 190 * d.scale_Y);
            d.set_pos_but(txtGAS_EL, 260 * d.scale_X, 1 * d.scale_Y);
            d.set_pos_but(but_stat_img, 160 * d.scale_X, 320 * d.scale_Y);
        }

        prg_img.setImageDrawable(d.createLayerDrawable(R.drawable.progress, (float)0.89, (float)0.04));
        fon_progress_img.setImageDrawable(d.createLayerDrawable(R.drawable.fon_progress, (float)0.89, (float)0.04));

        d.set_pos_but(tmpset, 70 * d.scale_X, 10 * d.scale_Y);
        d.set_pos_but(prg_img, 70 * d.scale_X, 150 * d.scale_Y);
        d.set_pos_but(fon_progress_img, 70*d.scale_X, 150 * d.scale_Y);
        d.set_pos_but(rdBut, 50 * d.scale_X, 20 * d.scale_Y);
        d.set_pos_but(mode_heart_color, 170 * d.scale_X, 30 * d.scale_Y);
        d.set_pos_but(txt_gist_B, 280 * d.scale_X,60 * d.scale_Y);
        d.set_pos_but(progressBar, 10 * d.scale_X, 110 * d.scale_Y);
        d.set_pos_but(txt_dev, 80 * d.scale_X, 1 * d.scale_Y);

        lay_tmpset.setVisibility(View.GONE);
        lay_stat.setVisibility(View.GONE);
        txt_gist_B.setVisibility(View.GONE);
        txtGAS_EL.setHintTextColor(getResources().getColor(R.color.default_));

        //IP_adr.setText("IP адрес " + dataSaves.get_curDevice());
        objTelemetry.ServerIP = dataSaves.get_curDevice();
        IP_adr.setText("IP адрес " + objTelemetry.ServerIP);
        status_heat.setText("НАГРЕВ ВКЛЮЧЕН");
        txtGAS_EL.setText("Нагреватель ...");
        status.setText("Ожидаю соединения...");
        tmpset.setText("Уст. t˚C воздуха   ... ");

        objTelemetry.setAirTmpCUR = -1;
        objTelemetry.setBoilerTmpCUR = -1;
        statusSock = false;
        objTelemetry.cmd = DataForSend.REQ_DATA;
        objTelemetry.cnt_repeat = 0;
        final Random random = new Random();
        objTelemetry.count_cmd = random.nextInt(126);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (prevcnt == cnt) {
                    if (loaderReceive != null) getSupportLoaderManager().destroyLoader(IDLOADERGET);
                    log ("MainActivity timerTask prevcnt == cnt" + cnt);
                    startLoader ();
                }
                else {
//                    log ("MainActivity timerTask cnt = " + cnt + ", prevcnt = " + prevcnt);
                    prevcnt = cnt;
                }
            }
        };

        tmr = new Timer();
        tmr.schedule (timerTask, 4000 , 4000);
    }

    void show_bar_set_tmp (int set_visible) {
        switch (set_visible) {
            case 2:                 // включение бара настройки темп. воздуха
                lay_tmpset.setVisibility(View.VISIBLE);
                lay_stat_soscet.setVisibility(View.GONE);
                lay_stat.setVisibility(View.GONE);
                txt_gist_B.setVisibility(View.GONE);
                mode_heart_color.setVisibility(View.VISIBLE);
                rdBut.setVisibility(View.VISIBLE);
                visible_set_TMP = 1;
                if (objTelemetry.setAirTmpCUR == -1) tmpset.setText("Уст. t˚C воздуха   ... ");
                else {
                    if (waitAnswercom == VisualCommand.ANSWERCOM_WAIT && objTelemetry.cmd == DataForSend.SET_TMP) showtmp.sendEmptyMessage(objTelemetry.setAirTmp);
                    else showtmp.sendEmptyMessage(objTelemetry.setAirTmpCUR);
                }
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
                mode_heart_color.setVisibility(View.GONE);
                rdBut.setVisibility(View.GONE);
                txt_gist_B.setVisibility(View.VISIBLE);
                visible_set_TMP = 2;
                if (objTelemetry.setBoilerTmpCUR == -1) tmpset.setText("Уст. t˚C гор.воды   ... ");
                else {
                    if (waitAnswercom == VisualCommand.ANSWERCOM_WAIT && objTelemetry.cmd == DataForSend.SET_TMPBR) showtmp.sendEmptyMessage(objTelemetry.setBoilerTmp);
                    else showtmp.sendEmptyMessage(objTelemetry.setBoilerTmpCUR);
                }
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

    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.menu); // Для Android 4.0
        MenuItem itemCOOL = popupMenu.getMenu().getItem(4);
        MenuItem itemBoiler = popupMenu.getMenu().getItem(5);
        MenuItem itemAlarmOFF = popupMenu.getMenu().getItem(13);

        if(objTelemetry.flagInversOUTOkCUR){
            itemCOOL.setChecked(true);
        }
        else{
            itemCOOL.setChecked(false);
        }

        if (objTelemetry.flagBoilerONCUR) {
            itemBoiler.setChecked(true);
        }
        else {
            itemBoiler.setChecked(false);
        }
        if (objTelemetry.flagNOTHeatCUR) {
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
                        show_dialog(1);
                        break;
                    case R.id.menu_2:
                        show_dialog(2);
                        break;
                    case R.id.menu_3:
                        show_dialog(3);
                        break;
                    case R.id.menu_4:
                        show_dialog(16);
                        break;
                    case R.id.menu_5:
                        show_dialog(4);
                        break;
                    case R.id.menu_6:
                        show_dialog(7);
                        break;
                    case R.id.menu_7:

                        finish();
                        break;
                    case R.id.menu_8:
                        show_dialog(9);
                        break;
                    case R.id.menu_9:
                        show_dialog(8);
                        break;
                    case R.id.menu_10:
                        Intent intent;
                        String[] TO = {"evan77@ro.ru"};
                        intent = new Intent(Intent.ACTION_SEND);
                        intent.setData(Uri.parse("mailto:"));
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_EMAIL, TO);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Smart Termo, обратная связь");
                        intent.putExtra(Intent.EXTRA_TEXT, "Вопросы по работе приложения и модуля board_SMART1:\n\n");
                        try {
                            startActivity(Intent.createChooser(intent, "Отправка письма..."));
                            Log.d(tag, "Finished sending email...");
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(MainActivity.this, "There is no email client installed", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.menu_11:
                        show_dialog(11);
                        break;
                    case R.id.menu_12:
                        show_dialog(14);
                        break;
                    case R.id.menu_13:
                        show_dialog(17);
                        break;
                    case R.id.menu_14:
                        show_dialog(10);
                        break;
                    case R.id.menu_15:
                            objTelemetry.cmd = DataForSend.SYNCHRO;
                            sendCommand();
                        break;
                }

                return true;
            }
        });
        MenuPopupHelper menuHelper = new MenuPopupHelper(this, (MenuBuilder) popupMenu.getMenu(), v);
        menuHelper.setForceShowIcon(true);
        menuHelper.setGravity(Gravity.END); menuHelper.show();
    }

    Handler showtmp = new Handler() {

        @Override
        public void handleMessage(Message msg) {
        int temp = msg.what;
        log ("MainActivity showtmp change_tmp " + change_tmp + ", touchFlag " + touchFlag + ", waitAnswercom " + waitAnswercom);
        DisplayParam.BarShowTmp bar = d.showtmp(visible_set_TMP, eY, poseY, temp);
        if (visible_set_TMP == 2) {
            if (change_tmp && touchFlag == 0 && waitAnswercom == 0) {
                //objTelemetry.setBoilerTmpCUR = temp;
                objTelemetry.cmd = DataForSend.SET_TMPBR;
                objTelemetry.setBoilerTmp = temp;
                sendCommand();
                change_tmp = false;
             }
        }
        if (visible_set_TMP == 1) {
            if (change_tmp  && touchFlag == 0 && waitAnswercom == 0) {
                //objTelemetry.setAirTmpCUR = temp;
                objTelemetry.cmd = DataForSend.SET_TMP;
                objTelemetry.setAirTmp = temp;
                sendCommand();
                change_tmp = false;
            }
        }
        tempScreen = temp;
        tmpset.setText (bar.get_txt());
        prg_img.setImageAlpha(bar.get_alfa());
        }
    };

    void updateScreen(ReadyDataForScreen data) {
        Handler h = new Handler(hc);
        Message msg = new Message();
        msg.obj = data;
        h.sendMessage(msg);
    }

    Handler.Callback hc = new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            ReadyDataForScreen data = (ReadyDataForScreen)msg.obj;
            readyData (data);
            return false;
        }
    };

    void readyData (ReadyDataForScreen data) {
        try {
            String[] str;
            tmp_air.setText(data.get_airHomeTmp());
            tmp_water.setText(data.get_waterTmp());
            tmp_out.setText(data.get_airOutTmp());
            tmp_boiler.setText(data.get_boilerTmp());
            if (flag_calibrovka) txt_dev.setText(data.get_infoFromserverADC());
            else txt_dev.setText(data.get_infoFromserver());
            status.setText(data.get_statusSocket());
            mode_heart_color.setText(data.get_statusWorkTariff());
            txt_gist_B.setText(data.get_boilerTmpGist());
            if (data.get_flagTarifOk()) {
                mode_heart_color.setTextColor(getResources().getColor(R.color.GREEN_));
                rdbutCheck = true;
                rdBut.setChecked(true);
            }
            else {
                mode_heart_color.setTextColor(getResources().getColor(R.color.default_));
                rdbutCheck = false;
                rdBut.setChecked(false);
            }
            status_heat.setText(data.get_statusHeat());
            if (data.get_flagHeatingOk()) status_heat.setTextColor(Color.RED);
            else status_heat.setTextColor(getResources().getColor(R.color.default_));
            if (touchFlag == 0 && !change_tmp) {
                objTelemetry.setAirTmpCUR = Integer.parseInt(data.get_setAirTmp());
                objTelemetry.setBoilerTmpCUR = Integer.parseInt(data.get_setBoilerTmp());
                if (waitAnswercom != VisualCommand.ANSWERCOM_WAIT) {
                    if (visible_set_TMP == 1) {
                        if (tempScreen != objTelemetry.setAirTmpCUR) showtmp.sendEmptyMessage(objTelemetry.setAirTmpCUR);
                    }
                    if (visible_set_TMP == 2) {
                        if (tempScreen != objTelemetry.setBoilerTmpCUR) showtmp.sendEmptyMessage(objTelemetry.setBoilerTmpCUR);
                    }
                 }
            }
            //log ("MainActivity touchFlag " + touchFlag + " change_tmp " + change_tmp);
            if (data.get_statusSocket().equals("Соединение установлено")) {
              statusSock = true;
            }
            if (data.get_flagGASOk()) {
                objTelemetry.flagGASOkCUR = DataForSend.OK;
                txtGAS_EL.setText("Нагреватель " + dataSaves.get_curDeviceHeaterName1() + data.get_statusWorkGAS());
                txtGAS_EL.setTextColor(getResources().getColor(R.color.Color_GAS));
            }
            else {
                objTelemetry.flagGASOkCUR = DataForSend.NO;
                txtGAS_EL.setText("Нагреватель " + dataSaves.get_curDeviceHeaterName2() + data.get_statusWorkGAS());
                txtGAS_EL.setTextColor(getResources().getColor(R.color.Color_EL));
            }
//            if (objTelemetry.cmd != DataForSend.REQ_DATA) {
                objTelemetry.boilerTmpGistCUR = data.get_boilerTmpGistINT();
                objTelemetry.flagNOTHeatCUR = data.get_flagNOTHeat();
//            log ("objTelemetry.flagNOTHeatCUR " + objTelemetry.flagNOTHeatCUR);
//            log ("cmdCUR " + objTelemetry.cmdCUR + " cmd " +objTelemetry.cmd);
            log (" count_cmdCUR " + objTelemetry.count_cmdCUR + " count_cmd " + objTelemetry.count_cmd);
            objTelemetry.flagInversOUTOkCUR = data.get_flagInversOUTOk();
            objTelemetry.flagBoilerONCUR = data.get_flagBoilerON();
            objTelemetry.timeNightSecCUR = data.get_timeNightSec();
            objTelemetry.timeDaySecCUR = data.get_timeDaytSec();
            objTelemetry.cmdCUR = Integer.parseInt(data.get_command());
            objTelemetry.count_cmdCUR = Integer.parseInt(data.get_count_COMAND());
            if (data.get_flagTarifOk()) objTelemetry.flagTarifOkCUR = DataForSend.OK;
            else objTelemetry.flagTarifOkCUR = DataForSend.NO;
            if (objTelemetry.cmdCUR == DataForSend.SET_LINK) {
                objTelemetry.ServerIPCUR = data.get_serverIP();
//                log ("MainActivity ServerIPCUR " + objTelemetry.ServerIPCUR);
//                log ("MainActivity portServ " + data.get_portServ());
            }

        } catch (Exception e) { log ("Exception updateScreen!"); }
    }

    void show_dialog (int id_Dialog) {
        Bundle args = new Bundle();
        args.putSerializable("keyOBJ", objTelemetry);
        args.putInt("id_Dialog", id_Dialog);
        args.putSerializable("keyDevices", dataSaves);
        DialogSelect dlg = new DialogSelect();
        dlg.setArguments(args);
        dlg.onAttach(MainActivity.this);
        dlg.show(getFragmentManager(), "dlg");
    }

    @Override
    public void onComplete(ObjTelemetry obj) {
        objTelemetry = obj;
        dataSaves = (DataSave)rwData.readData ();
        IP_adr.setText("IP адрес " + obj.ServerIP);
//        if (obj.cmd == DataForSend.SET_TMPBR) showtmp.sendEmptyMessage(setBoilerTmp);
        if (objTelemetry.cmd == DataForSend.SET_WORK_TARIFF) {
            if (objTelemetry.flagTarifOk == DataForSend.NO) {
//                objTelemetry.cmd = 100;
                rdBut.setChecked(false);
                rdbutCheck = false;
                rdBut.setChecked(false);
                mode_heart_color.setTextColor(getResources().getColor(R.color.default_));
                mode_heart_color.setText("Учитывать дневной и ночной тариф");
            } else if (objTelemetry.flagTarifOk == DataForSend.OK) {

                mode_heart_color.setText("Учитывать дневной и ночной тариф" + " ±" + objTelemetry.airTariffGist + "˚C" +
                        "\n" + "день : " + objTelemetry.get_timeDay() +
                        "  ночь : " + objTelemetry.get_timeNight());
                mode_heart_color.setTextColor(getResources().getColor(R.color.GREEN_));
                rdbutCheck = true;
                rdBut.setChecked(true);
            }
        }
        if (objTelemetry.cmd != DataForSend.REQ_DATA) {
            sendCommand();
        }
    }

    Handler Startdialog = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            show_dialog(13);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        log ("MainActivity onDestroy");
    }

    @Override
    public void onCompleteANSW(int answ) {
        waitAnswercom = answ;
        log ("MainAcnivity onCompleteANSW " + answ);
        waitAnswercom = 0;
        if (objTelemetry.cmd == DataForSend.SET_LINK && objTelemetry.ServerIPCUR.length() == 15) {
            try {
                dev = dataSaves.get_devices();
                dev.put(objTelemetry.ServerIPCUR, new String[]{"Газ", "Электро"});
                dataSaves.put_devices(dev);
                rwData.saveData(dataSaves);

                dataSaves.set_curDevice(objTelemetry.ServerIPCUR);
                rwData.saveData(dataSaves);
                objTelemetry.ServerIP = objTelemetry.ServerIPCUR;
            } catch (Exception e) { log ("MainActivity Exception save New Net WIFI"); }
        }
       if (objTelemetry.cmd == DataForSend.SET_COEF) flag_calibrovka = false;
        objTelemetry.cmd = DataForSend.REQ_DATA;
    }

    void sendCommand () {
        if (statusSock) {
            if (waitAnswercom == 0) {
                waitAnswercom = VisualCommand.ANSWERCOM_WAIT;
                objTelemetry.count_cmd++;
                objTelemetry.cnt_repeat = 0;
                if (objTelemetry.count_cmd > 126) objTelemetry.count_cmd = 0;
                Cur_Data_ForSend objsend;
                objsend = new DataForSend(objTelemetry).getInitClasses();
                String message = objsend.readyStringData();
                VisualCommand viscom = new VisualCommand("Thread1", objTelemetry, message, cntx);
                viscom.onAttachANSW(this);
                viscom.start();
            } else {
                DialogSelect.show_txt_toast("Дождитесь выполнения предыдущей команды!", cntx);
            }
        } else DialogSelect.show_txt_toast("Нет соединения с сервером!", cntx);
    }

/*
    void saveData (Object obj) {
        try {
            FileOutputStream outputStream = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(obj);
            outputStream.close();
        } catch (Exception e) { log ("Exception saveData");}
    }

    Object readData () {
        try {
            File fl = getBaseContext().getFileStreamPath(FILENAME);
            Object obj = null;
            if (fl.exists()) {
                FileInputStream inputStream = openFileInput(FILENAME);
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                obj = objectInputStream.readObject();
                inputStream.close();
                return obj;
            }
            else return null;
        } catch (Exception e) {
            log ("MainActiviy Exception readData");
            return null;
        }
    }*/
}
