package com.ez.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ez.data_receive.ObjTelemetry;
import com.ez.data_save.DataSave;
import com.ez.data_save.ReadWriteData;
import com.ez.smarttermo.DataForSend;
import com.ez.smarttermo.MainActivity;
import com.ez.smarttermo.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.zip.Inflater;


/**
 * Created by EZ on 02.02.2023.
 */


public class DialogSelect extends DialogFragment {
    private ObjTelemetry obj;
    private DataSave dataSave;
    private ReadWriteData rwData;
    private HashMap<String, String []> dev;
    private String tag = "tag";
    private int id_Dialog;
    private final String  HEATER1 = "Газ";
    private final String  HEATER2 = "Электро";
    private boolean flagLongClick = false;
    private boolean flagDelIP = false;
    private ArrayList name_adr;
    private ListView lst_ipadr;
    TextView daybtn;
    TextView nightbtn;
    ArrayAdapter adapter_for_nameIP;

    AppCompatActivity activity;
    Context cntx;
    void log (String s) { Log.d(tag, s); }
    LayoutInflater infl;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Bundle args = this.getArguments();
        id_Dialog = args.getInt("id_Dialog");
        obj = (ObjTelemetry)args.getSerializable("keyOBJ");
        dataSave = (DataSave) args.getSerializable("keyDevices");
        dev = dataSave.get_devices();
        cntx = getContext();
        rwData = new ReadWriteData(cntx);
    }
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        this.infl = inflater;
        switch (id_Dialog) {
            case 1:                                                                         // настройка подключения к домашней сети
                view = inflater.inflate(R.layout.config_client, null);
                final EditText config_port = view.findViewById(R.id.config_port);
                final EditText config_IP = view.findViewById(R.id.config_IP);
                final EditText config_SSID = view.findViewById(R.id.config_SSID);
                final EditText config_pasw = view.findViewById(R.id.config_pasw);
                final ProgressBar progrbar = view.findViewById(R.id.progrBar);
                final TextView info_prog = view.findViewById(R.id.info_prog);
                final Button apply = view.findViewById(R.id.button2);

                //config_IP.setHint("SERVER_IP");
                apply.setText("Применить");
                OnClickListener apply_but = new OnClickListener() {               // отработка пункта меню настройки порта сервера
                    public void onClick(View v) {
                    if (config_SSID.getText().toString() != null && config_pasw.getText().toString() != null
                            && config_pasw.getText().toString().length() == 8) {
                        obj.name_SSID = config_SSID.getText().toString();
                        obj.pass_SSID = config_pasw.getText().toString();
                        obj.cmd = DataForSend.SET_LINK;
                        dismiss();
                    } else {
                        show_txt_toast("Заполните корректно поля SSID и Password!", cntx);
                    }
                     }
                };
                apply.setOnClickListener(apply_but);
                break;
            case 2:                                                                         // установка температуры
                view = inflater.inflate(R.layout.lay_set_tmp, null);
                final TextView settmp = view.findViewById(R.id.fon_lay_txt);
                final EditText settmp_txt = view.findViewById(R.id.settmp_txt);
                final TextView but_ye = view.findViewById(R.id.text_ye);
                final TextView but_no = view.findViewById(R.id.text_no);
                settmp.setText("Установить температуру ?");
                settmp_txt.setHint("24˚C");

                OnClickListener butyes = new OnClickListener() {
                    public void onClick(View v) {
                        try {
                            if (settmp_txt.getHint().equals("24˚C")) {
                                obj.cmd = DataForSend.SET_TMP;
                                obj.setAirTmp = 24;
                            } else {
                                final int send_tmp = Integer.parseInt(settmp_txt.getText().toString());
                                if (settmp_txt.getText().length() != 0 && send_tmp != obj.setAirTmp) {
                                    if (send_tmp > 0 && send_tmp < 50) {
                                        obj.cmd = DataForSend.SET_TMP;
                                        obj.setAirTmp = send_tmp;
                                    } else {
                                        show_txt_toast("Введите температуру в диапазоне от 0 до +50˚C!", cntx);
                                    }
                                }
                            }
                        } catch (Exception e) { log ("Exception setTMP " + e); }
                        dismiss();
                    }
                };
                but_ye.setOnClickListener(butyes);
                OnClickListener butno = new OnClickListener() {
                    public void onClick(View v) {
                        dismiss();
                    }
                };
                but_no.setOnClickListener(butno);
                break;
            case 3:                                                                         // помощь
                view = inflater.inflate(R.layout.help, null);
                Button btOK = view.findViewById(R.id.butOK);
                OnClickListener h_butok = new OnClickListener() {
                    public void onClick(View v) {
                        dismiss();
                    }
                };
                btOK.setOnClickListener(h_butok);
                break;
            case 4:                                                                         // о приложении
                view = inflater.inflate(R.layout.fon_lay, null);
                TextView about_txt = view.findViewById(R.id.fonlay_txt);
                about_txt.setText(R.string.about);
                break;
            case 5:                                                                         // меню калибровки
                view = inflater.inflate(R.layout.calibrovka, null);
                final Button butt1 = view.findViewById(R.id.button1);
                final EditText A1coef = view.findViewById(R.id.A1);
                final EditText A2coef = view.findViewById(R.id.A2);
                final EditText A1Ecoef = view.findViewById(R.id.A1E);
                final EditText A2Ecoef = view.findViewById(R.id.A2E);
                final EditText A3coef = view.findViewById(R.id.A3);
                final EditText A4coef = view.findViewById(R.id.A4);
                final EditText A3Ecoef = view.findViewById(R.id.A3E);
                final EditText A4Ecoef = view.findViewById(R.id.A4E);

                final EditText A0coef = view.findViewById(R.id.A0);
                final EditText A0Ecoef = view.findViewById(R.id.A0E);

                OnClickListener but_1 = new OnClickListener() {                   // отработка пункта меню калибровка
                    public void onClick(View v) {
                        try {
                            String A1 = A1coef.getText().toString(), A1E = A1Ecoef.getText().toString();
                            String A2 = A2coef.getText().toString(), A2E = A2Ecoef.getText().toString();
                            String A3 = A3coef.getText().toString(), A3E = A3Ecoef.getText().toString();
                            String A4 = A4coef.getText().toString(), A4E = A4Ecoef.getText().toString();
                            boolean flagOK = true;

                            String A0 = A0coef.getText().toString(), A0E = A0Ecoef.getText().toString();
                            obj.coeffStrArray[0] = A1;
                            obj.coeffStrArray[1] = A2;
                            obj.coeffStrArray[2] = A3;
                            obj.coeffStrArray[3] = A4;
                            obj.coeffStrArray[4] = A1E;
                            obj.coeffStrArray[5] = A2E;
                            obj.coeffStrArray[6] = A3E;
                            obj.coeffStrArray[7] = A4E;
                            obj.coeffStrArray[8] = A0;
                            obj.coeffStrArray[9] = A0E;

                            for (int i = 0; i < 10; i++) if (obj.coeffStrArray[i].equals("")) flagOK = false;
                            if (flagOK) obj.cmd = DataForSend.SET_COEF;
                            else show_txt_toast("Некорректно заполнены данные!", cntx);

                            dismiss();
                        } catch (Exception e) {
                            Log.d(tag, "Exception SET_COEF");
                        }
                    }
                };
                butt1.setOnClickListener(but_1);
                break;
            case 6:                                                                         // установка режима работы по тарифу
                view = inflater.inflate(R.layout.lay_set_tmp, null);
                final TextView butOK_gist = view.findViewById(R.id.text_ye);
                final TextView butNO_gist = view.findViewById(R.id.text_no);
                final EditText gist_v = view.findViewById(R.id.settmp_txt);
                gist_v.setText("4");
                gist_v.setInputType(InputType.TYPE_CLASS_NUMBER);
                butOK_gist.setText("Применить?");
                butNO_gist.setText("Отмена");
                final TextView txtinf = view.findViewById(R.id.fon_lay_txt);
                txtinf.setText("Установка гистерезиса для температур ночного и дневного режима");

                OnClickListener OKgist = new OnClickListener() {
                    public void onClick(View v) {
                        String tmp_gisteresis;
                        if(gist_v.getText().length() != 0) {
                            obj.airTariffGist = Integer.parseInt(gist_v.getText().toString());
                            obj.flagTarifOk = DataForSend.OK;
                            obj.timeNightSec = obj.timeNightSecCUR;
                            obj.timeDaySec = obj.timeDaySecCUR;
                        }
                        else{
                            obj.flagTarifOk = DataForSend.NO;
                        }
                        obj.cmd = DataForSend.SET_WORK_TARIFF;
                        dismiss();
                    }
                };
                butOK_gist.setOnClickListener(OKgist);
                OnClickListener NOgist = new OnClickListener() {
                    public void onClick(View v) {

                        dismiss();
                    }
                };
                butNO_gist.setOnClickListener(NOgist);
                break;
            case 7:                                                                         // установка времени "день", "ночь" для работы по тарифу
                view = inflater.inflate(R.layout.night_day_time, null);
                daybtn = view.findViewById(R.id.text_d);
                nightbtn = view.findViewById(R.id.text_n);
                final TextView use = view.findViewById(R.id.btn_use);

                OnClickListener but_day = new OnClickListener() {
                    public void onClick(View v) {
                        int hour = obj.timeDaySecCUR/3600;
                        int minute = obj.timeDaySecCUR%60;
                        TimePickerDialog tpd = new TimePickerDialog(cntx, myCallBackDay, hour, minute, true);
                        tpd.show();
                        //obj.timeDaySec = 28800; // ?
                    }
                };
                daybtn.setOnClickListener(but_day);

                OnClickListener but_night = new OnClickListener() {
                    public void onClick(View v) {
                        int hour = obj.timeNightSecCUR/3600;
                        int minute = obj.timeNightSecCUR%60;
                        TimePickerDialog tpd = new TimePickerDialog(cntx, myCallBackNight, hour, minute, true);
                        tpd.show();
                        //obj.timeNightSec = 79200;   // ?
                    }
                };
                nightbtn.setOnClickListener(but_night);

                OnClickListener but_use__ = new OnClickListener() {
                    public void onClick(View v) {
                        if (obj.timeNightSec != obj.timeDaySec) {
                            if (obj.timeDaySec > obj.timeNightSec) {
                                if ((obj.timeDaySec - obj.timeNightSec) > 3600) {
                                    obj.cmd = DataForSend.SYNCHRO_TIME_TARIFF;
                                    dismiss();
                                }
                                else show_txt_toast("Введены некорректные значения!", cntx);
                            } else {
                                if ((obj.timeNightSec - obj.timeDaySec) > 3600) {
                                    obj.cmd = DataForSend.SYNCHRO_TIME_TARIFF;
                                    dismiss();
                                }
                                else show_txt_toast("Введены некорректные значения!", cntx);
                            }
                        } else show_txt_toast("Введены некорректные значения!", cntx);
                    }
                };
                use.setOnClickListener(but_use__);
                break;
            case 8:                                                                         // выбор нагревателя, газ - электро
                view = inflater.inflate(R.layout.mode_work, null);
                final Button gas_mode = view.findViewById(R.id.button_serv);
                final Button el_mode = view.findViewById(R.id.button_cl);
                final TextView info_lab = view.findViewById(R.id.tvT);
                info_lab.setText("Выбор нагревателя");
                gas_mode.setTextSize(16);
                el_mode.setTextSize(16);

                String heat1 = dataSave.get_curDeviceHeaterName1(); //"Газовый котелasdf";
                String heat2 = dataSave.get_curDeviceHeaterName2(); //"Электро котелasdf";

                if (heat1.length() > 14) heat1 = heat1.substring(0, 13) + ".";
                if (heat2.length() > 14) heat2 = heat2.substring(0, 13) + ".";

                gas_mode.setText(heat1);
                el_mode.setText(heat2);

                OnClickListener handl_gas_mode = new OnClickListener() {          // обработка режима ГАЗ
                    public void onClick(View v) {
                        dialogGistHeater (DataForSend.OK);
                    }
                };
                gas_mode.setOnClickListener(handl_gas_mode);
                OnClickListener handl_el_mode = new OnClickListener() {           // обработка режима Электро
                    public void onClick(View v) {
                        dialogGistHeater (DataForSend.NO);
                    }

                };
                el_mode.setOnClickListener(handl_el_mode);
                break;
            case 9:                                                                         // инверсия выхода
                view = inflater.inflate(R.layout.lay_set_tmp, null);
                final TextView txt_INVERS = view.findViewById(R.id.fon_lay_txt);
                final TextView edit_INVERS = view.findViewById(R.id.settmp_txt);
                final LinearLayout lay11 = view.findViewById(R.id.lay_for_txtedit);
                final TextView but_yeINVERS = view.findViewById(R.id.text_ye);
                final TextView but_noINVERS = view.findViewById(R.id.text_no);
                lay11.removeView(edit_INVERS);
//            if(cool_ON){ss = "Выключить инверсию реле выхода?\n";}
//            else{ss = "Включить инверсию реле выхода?\n";}
//            txt_INVERS.setText(ss);
                if (obj.flagInversOUTOkCUR) {
                    txt_INVERS.setText ("Выключить инверсию реле выхода?\n");
                } else {
                    txt_INVERS.setText ("Включить инверсию реле выхода?\n");
                }

                OnClickListener butyes11 = new OnClickListener() {
                    public void onClick(View v) {
                        if (obj.flagInversOUTOkCUR) {
                            obj.flagInversOUTOk = false;
                            obj.cmd = DataForSend.COOL_MODE_OFF;
                        } else {
                            obj.flagInversOUTOk = true;
                            obj.cmd = DataForSend.COOL_MODE_ON;
                        }

                        dismiss();
                    }
                };
                but_yeINVERS.setOnClickListener(butyes11);
                OnClickListener butno11 = new OnClickListener() {
                    public void onClick(View v) {
                        dismiss();
                    }
                };
                but_noINVERS.setOnClickListener(butno11);
                break;
            case 10:                                                                                    // гистерезис воды для бойлера
                view = inflater.inflate(R.layout.lay_set_tmp, null);
                final TextView txt_gist = view.findViewById(R.id.fon_lay_txt);
                final TextView edit_gist = view.findViewById(R.id.settmp_txt);
                int type = InputType.TYPE_CLASS_NUMBER |  InputType.TYPE_NUMBER_FLAG_DECIMAL;           // задаем тип ввода данных в формате double
                edit_gist.setInputType(type);
                edit_gist.setHint("5.00");
                final TextView but_yes = view.findViewById(R.id.text_ye);
                final TextView but_noo = view.findViewById(R.id.text_no);
                txt_gist.setText("Введите значение температуры гистерезиса для воды бойлера");
                but_yes.setText("Применить");

                View.OnClickListener handl_butyes = new View.OnClickListener() {        //
                    public void onClick(View v) {
                            if (edit_gist.getText().length() == 0) {
                                obj.boilerTmpGist = 5;  // если не было введено никаких данных
                            } else {
                                String s;
                                s = edit_gist.getText().toString();
                                obj.boilerTmpGist = Integer.parseInt(s);
                            }
                            if (obj.boilerTmpGist < 35) {
                                obj.cmd = DataForSend.SET_TMPGISTBR;
                            } else {
                                show_txt_toast("Введите корретное значение!", cntx);
                            }
                        dismiss();
                    }
                };
                but_yes.setOnClickListener(handl_butyes);

                View.OnClickListener handl_butno = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        dismiss();
                    }
                };
                but_noo.setOnClickListener(handl_butno);

                break;
            case 11:                                                                        // настройка почты
                view = inflater.inflate(R.layout.mail_set_form, null);
                final EditText login_mail = view.findViewById(R.id.config_SSID);
                final EditText pass_mail = view.findViewById(R.id.config_pasw);
                final EditText mail_to = view.findViewById(R.id.mail_to);
                final EditText name_smtp_serv = view.findViewById(R.id.config_SMTP);
                final EditText port_serv = view.findViewById(R.id.config_port_SMTP);

                final ProgressBar progrbar_mail = view.findViewById(R.id.progrBar);
                final TextView info_prog_mail = view.findViewById(R.id.info_prog);
                final Button apply_mail = view.findViewById(R.id.button2);
                apply_mail.setText("Применить");
                OnClickListener apply_mail_handle = new OnClickListener() {
                    public void onClick(View v) {
                        String mail_from = "", mail_from64 = "", mailto = "", pass = "", pass64 = "", name_serv = "", name_port = "";
                        boolean flag_ok = true;
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

                        char[] tmp_ch = new char[50];
                        if (mail_from.length() < 8 || mailto.length() < 8 || pass.length() < 4) {
                            show_txt_toast("Данные не корректны или введены не все!", cntx);
                            flag_ok = false;
                        }
                        else {
                            if (mailto.length() > 22) {
                                show_txt_toast("Адрес получателя не должен превышать 22 символа!", cntx);
                                flag_ok = false;
                            } else {

                                mailto.getChars(0, mailto.length(), tmp_ch, 0);
                                for (int i = 0; i < mailto.length(); i++) {
                                    if (tmp_ch[i] == ' ') {
                                        show_txt_toast("Адрес получателя не должен содержать пробелы!", cntx);
                                        flag_ok = false;
                                    }
                                }
                                if (flag_ok) {
                                    flag_ok = false;
                                    for (int i = 0; i < mailto.length(); i++) {
                                        if (tmp_ch[i] == '.') {
                                            flag_ok = true;
                                        }
                                    }
                                    if (!flag_ok) {
                                        show_txt_toast("Некорректный адрес получателя!", cntx);
                                    }
                                }
                            }
                            if (mail_from.length() > 22 && flag_ok) {
                                show_txt_toast("Адрес отправителя не должен превышать 22 символа!", cntx);
                                flag_ok = false;
                            } else {
                                mail_from.getChars(0, mail_from.length(), tmp_ch, 0);
                                for (int i = 0; i < mail_from.length(); i++) {
                                    if (tmp_ch[i] == ' ') {
                                        show_txt_toast("Адрес отправителя не должен содержать пробелы!", cntx);
                                        flag_ok = false;
                                    }
                                }
                                if (flag_ok) {
                                    flag_ok = false;
                                    for (int i = 0; i < mail_from.length(); i++) {
                                        if (tmp_ch[i] == '.') {
                                            flag_ok = true;
                                        }
                                    }
                                    if (!flag_ok) {
                                        show_txt_toast("Некорректный адрес отправителя!", cntx);
                                    }
                                }
                            }
                            if (pass.length() > 20 && flag_ok) {
                                show_txt_toast("Пароль не должен превышать 20 символов!", cntx);
                                flag_ok = false;
                            } else {
                                pass.getChars(0, pass.length(), tmp_ch, 0);
                                for (int i = 0; i < pass.length(); i++) {
                                    if (tmp_ch[i] == ' ') {
                                        flag_ok = false;
                                        show_txt_toast("Пароль не должен содержать пробелы!", cntx);
                                    }
                                }
                            }
                        }
                        if (flag_ok) {
                            try {
                                obj.pass_mail = pass;
                                obj.mail_from = mail_from;
                                obj.name_smtp_serv = name_serv;
                                obj.mail_port_serv = name_port;
                                obj.mail_to = mailto;
                                obj.cmd = DataForSend.CONFIG_MAIL;
                                dismiss();
                            } catch (Exception e) { log ("DialogSelect Exception EnterMail"); }
                        }
                    }
                };
                apply_mail.setOnClickListener(apply_mail_handle);
                break;
            case 12:                                                                    //Добавление новых IP
                view = inflater.inflate (R.layout.fon_lay, null);
                final LinearLayout lay_forIP = view.findViewById(R.id.mainlayID);
                final TextView txtIP = view.findViewById(R.id.fonlay_txt);
                final EditText new_ip = new EditText(cntx);
                final Button newIPbutOK = new Button(cntx);
                final Button newIPbutNO = new Button(cntx);
                final ScrollView scroll_v = new ScrollView(cntx);
                final LinearLayout l_scrl = new LinearLayout(cntx);
                final LinearLayout ll=new LinearLayout(cntx);
                //final ListView lst_ipadr;
                //final ArrayAdapter adapter_for_nameIP;
                final LinearLayout.LayoutParams lay_param = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                lay_param.weight = 1;                 // установить вес 1
                lay_param.setMargins(10,20,10,20);
                newIPbutNO.setLayoutParams(lay_param);
                newIPbutOK.setLayoutParams(lay_param);

                name_adr = new ArrayList<String>();
                for(String key : dev.keySet()) name_adr.add(key);
                lst_ipadr = new ListView(cntx);
                adapter_for_nameIP = new ArrayAdapter<String>(cntx, android.R.layout.simple_list_item_1, name_adr);  ////final ListAdapter adapter_for_nameIP
                lst_ipadr.setAdapter(adapter_for_nameIP);

                l_scrl.setOrientation(LinearLayout.VERTICAL);
                txtIP.setText("Установка нового IP адреса");
                newIPbutOK.setText("Применить");
                newIPbutNO.setText("Отмена");
                new_ip.setInputType(InputType.TYPE_CLASS_TEXT); // вместо энтер на клаве будет крыжик применить

                ll.setOrientation(LinearLayout.HORIZONTAL);
                newIPbutNO.setTextColor(getResources().getColor(R.color.default_));
                newIPbutOK.setTextColor(getResources().getColor(R.color.default_));
                newIPbutOK.setBackgroundColor(getResources().getColor(R.color.black_));
                newIPbutNO.setBackgroundColor(getResources().getColor(R.color.black_));
                ll.addView(newIPbutNO);
                ll.addView(newIPbutOK);
                lay_forIP.addView(new_ip);
                l_scrl.addView(lst_ipadr);
                l_scrl.addView(ll);
                scroll_v.addView(l_scrl);
                lay_forIP.addView(scroll_v);

                setListViewHeightBasedOnChildren(lst_ipadr);        // выставляем отбражение всех элементов списка

                OnClickListener OKIP = new OnClickListener() {        //
                    public void onClick(View v) {
                        String SERVER_IP = new_ip.getText().toString();
                        if (!dev.containsKey(SERVER_IP)) {
                            addNEWIP (SERVER_IP, HEATER1, HEATER2);
                            setCURIP (SERVER_IP);
                        }
                        dismiss();
                    }
                };
                newIPbutOK.setOnClickListener(OKIP);

                OnClickListener NOIP = new OnClickListener() {        //
                    public void onClick(View v) {
                        dismiss();
                    }
                };
                newIPbutNO.setOnClickListener(NOIP);
                lst_ipadr.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        if (!flagLongClick) {
                            String SERVER_IP = (String) name_adr.get(position);
                            setCURIP(SERVER_IP);
                            dismiss();
                        }
                    }
                });

                lst_ipadr.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id)
                    {
                        flagLongClick = true;
                        DialogDelIP ((String)name_adr.get(position));

                        return false;
                    }
                });
                break;
            case 13:                                                                    // меню при первом старте приложения
                view = inflater.inflate (R.layout.lay_set_tmp, null);
                final TextView st_txt = view.findViewById(R.id.fon_lay_txt);
                final TextView edit_st = view.findViewById(R.id.settmp_txt);
                final LinearLayout lay111 = view.findViewById(R.id.lay_for_txtedit);
                final TextView newyes = view.findViewById(R.id.text_ye);
                final TextView newNO = view.findViewById(R.id.text_no);
                st_txt.setText("Для перехода в меню настроек кликните пиктограмму \"домика\" в верхнем правом углу экрана.\n\nПоказывать это напоминание при старте?");
                lay111.removeView(edit_st);

                OnClickListener handl_newyes = new OnClickListener() {        // да
                    public void onClick(View v) {
                        dataSave.set_dialogStart(true);
                        rwData.saveData (dataSave);
                        dismiss();
                    }
                };
                newyes.setOnClickListener(handl_newyes);
                OnClickListener handl_newNO = new OnClickListener() {         // нет
                    public void onClick(View v) {
                        dataSave.set_dialogStart(false);
                        rwData.saveData (dataSave);
                        dismiss();
                    }

                };
                newNO.setOnClickListener(handl_newNO);
                break;
            case 14:                                                                    // сбос модуля к установкам по умолчанию
                view = inflater.inflate(R.layout.lay_set_tmp, null);
                final TextView txt_menu10 = view.findViewById(R.id.fon_lay_txt);
                final EditText edit_menu10 = view.findViewById(R.id.settmp_txt);
                final LinearLayout lay10 = view.findViewById(R.id.lay_for_txtedit);
                final TextView but_ye10 = view.findViewById(R.id.text_ye);
                final TextView but_no10 = view.findViewById(R.id.text_no);
                lay10.removeView(edit_menu10);
                txt_menu10.setText(R.string.sbrosESP);
                txt_menu10.setTextColor(getResources().getColor(R.color.RED));

                OnClickListener butyes10 = new OnClickListener() {        //
                    public void onClick(View v) {
                        obj.cmd = DataForSend.LOAD_DEF;
                        dismiss();
                    }
                };
                but_ye10.setOnClickListener(butyes10);
                OnClickListener butno10 = new OnClickListener() {
                    public void onClick(View v) {
                        dismiss();
                    }
                };
                but_no10.setOnClickListener(butno10);

                break;
            case 15:                                                                    //  новые названия для нагревателя 1 и 2
                view = inflater.inflate (R.layout.lay_set_tmp, null);
                final TextView txt_selheat = view.findViewById(R.id.fon_lay_txt);
                final EditText edit_selheat = view.findViewById(R.id.settmp_txt);
                final LinearLayout lay100 = view.findViewById(R.id.lay_for_txtedit);
                final TextView but_selheatY = view.findViewById(R.id.text_ye);
                final TextView but_selheatN = view.findViewById(R.id.text_no);
                but_selheatY.setText("Применить");
                but_selheatN.setText("Отмена");
                lay100.removeView(edit_selheat);
                final EditText edit_selheat1 = new EditText(cntx);
                final EditText edit_selheat2 = new EditText(cntx);
                lay100.addView(edit_selheat1);
                lay100.addView(edit_selheat2);
                try {
                    edit_selheat1.setHint(dataSave.get_curDeviceHeaterName1());
                    edit_selheat2.setHint(dataSave.get_curDeviceHeaterName2());
                } catch (Exception e) { log ("DialogSelect Except NEW HEATER! "); }
                txt_selheat.setText("Введите новые названия для нагревателя 1 и 2\n");

                OnClickListener h_but_selheatY = new OnClickListener() {
                    public void onClick(View v) {
                        String s1 = edit_selheat1.getText().toString();
                        String s2 = edit_selheat2.getText().toString();
                        if (s1 != null && s2 != null) {
                            saveNEWHeater (s1, s2);
                        } else show_txt_toast ("Необходимо ввести новые имена нагревателей", cntx);
                        dismiss();
                    }
                };
                but_selheatY.setOnClickListener(h_but_selheatY);
                OnClickListener h_but_selheatN = new OnClickListener() {
                    public void onClick(View v) {

                        dismiss();
                    }
                };
                but_selheatN.setOnClickListener(h_but_selheatN);

                break;
            case 16:                                                                    // включить/выключить бойлер
                view = inflater.inflate (R.layout.lay_set_tmp, null);
                final TextView txt_boiler = view.findViewById(R.id.fon_lay_txt);
                final TextView edit_boiler = view.findViewById(R.id.settmp_txt);
                final LinearLayout lay11_b = view.findViewById(R.id.lay_for_txtedit);
                final TextView but_ye_boiler = view.findViewById(R.id.text_ye);
                final TextView but_no_boiler = view.findViewById(R.id.text_no);
                lay11_b.removeView(edit_boiler);
                if (obj.flagBoilerONCUR) txt_boiler.setText("Выключить Бойлер?\n");
                else txt_boiler.setText("Включить Бойлер?\n");
                OnClickListener butyes_B = new OnClickListener() {        //
                    public void onClick(View v) {
                        if (obj.flagBoilerONCUR) {
                            obj.flagBoilerON = false;
                            obj.cmd = DataForSend.SET_ON_OFF_BOILER;
                            dismiss();
                        }
                        else {
                            obj.flagBoilerON = true;
                            obj.cmd = DataForSend.SET_ON_OFF_BOILER;
                        }

                    }
                };
                but_ye_boiler.setOnClickListener(butyes_B);
                OnClickListener butno_B = new OnClickListener() {
                    public void onClick(View v) {
                        dismiss();
                    }
                };
                but_no_boiler.setOnClickListener(butno_B);

                break;
            case 17:                                                                    // аварийное принудительное отключение нагрева
                view = inflater.inflate (R.layout.lay_set_tmp, null);
                final TextView txt_alarm = view.findViewById(R.id.fon_lay_txt);
                final TextView edit_alarm = view.findViewById(R.id.settmp_txt);
                final LinearLayout lay11_alarm = view.findViewById(R.id.lay_for_txtedit);
                final TextView but_ye_alarm = view.findViewById(R.id.text_ye);
                final TextView but_no_alarm = view.findViewById(R.id.text_no);
                lay11_alarm.removeView(edit_alarm);
                if(!obj.flagNOTHeatCUR) txt_alarm.setText("Запретить работу нагревателя?\n");
                else txt_alarm.setText("Разрешить работу нагревателя?\n");
                OnClickListener butyes_alarm = new OnClickListener() {        //
                    public void onClick(View v) {
                        if(obj.flagNOTHeatCUR){
                            obj.flagNOTHeat = false;
                        }
                        else {
                            obj.flagNOTHeat = true;
                        }
                        obj.cmd = DataForSend.SET_ON_OFF_ALARM_HEAT;
                        dismiss();
                    }
                };
                but_ye_alarm.setOnClickListener(butyes_alarm);
                OnClickListener butno_alarm = new OnClickListener() {        //
                    public void onClick(View v) {
                        dismiss();
                    }
                };
                but_no_alarm.setOnClickListener(butno_alarm);
                break;
        }
        return view;
    }

    public interface OnCompleteListener {
        void onComplete(ObjTelemetry obj);
    }

    public void onAttach(AppCompatActivity activity) {
        log ("DialogSelect onAttach");
        try {
            this.activity = activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }


    public void onDismiss (DialogInterface dialog) {
        super.onDismiss(dialog);
        ((OnCompleteListener) activity).onComplete(obj);
        log ("DialogSelect onDismiss");

    }

    public static void show_txt_toast (String str , Context cntx){
        Toast.makeText(cntx, str, Toast.LENGTH_SHORT).show();
    }

    private void addNEWIP (String key, String heater1, String heater2) {
        dev.put(key, new String[]{heater1, heater2});
        dataSave.put_devices(dev);
        rwData.saveData (dataSave);
    }

    private void delIP (String key) {
        dev.remove(key);
        dataSave.put_devices(dev);
        rwData.saveData (dataSave);
    }

    private void setCURIP (String ip) {
        dataSave.set_curDevice(ip);
        rwData.saveData (dataSave);
        obj.ServerIP = ip;
    }

    private void saveNEWHeater (String s1, String s2) {
        dev.put(dataSave.get_curDevice(), new String[]{s1, s2});
        dataSave.put_devices(dev);
        rwData.saveData (dataSave);
    }

    // Изменение высоты ListView в зависимости от количества элементов, чтобы вместить в ScrollView
// в параметрах передаём listView для определения высоты
    private void setListViewHeightBasedOnChildren(ListView listView) {
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

    void DialogDelIP (String s) {
        final String key = s;
        AlertDialog.Builder builder = new AlertDialog.Builder(cntx);
        final AlertDialog alert;
        View lay_txt;
        lay_txt = infl.inflate(R.layout.lay_set_tmp, null);
        final LinearLayout lay_txtedit = lay_txt.findViewById(R.id.lay_for_txtedit);
        final TextView txt_info_delIP = lay_txt.findViewById(R.id.fon_lay_txt);
        final EditText edit_gist = lay_txt.findViewById(R.id.settmp_txt);
        final TextView but_ye = lay_txt.findViewById(R.id.text_ye);
        final TextView but_no = lay_txt.findViewById(R.id.text_no);
        txt_info_delIP.setText("Удалить устройство с IP " + s +"?");
        lay_txtedit.removeView(edit_gist);

        builder.setView(lay_txt);
        alert = builder.create();
        alert.show();

        View.OnClickListener handl_butyes = new View.OnClickListener() {        //
            public void onClick(View v) {
                if (dataSave.get_curDevice().equals(key)) {
                    show_txt_toast("Устройство используется!\n " +
                            "Переключитесь на другой IP\n и повторите попытку", cntx);
                } else {
                    delIP(key);
                    dataSave = (DataSave) rwData.readData();
                    dev = dataSave.get_devices();
                    name_adr.remove(key);
                    //for(String key : dev.keySet()) name_adr.add(key);
                    adapter_for_nameIP.notifyDataSetChanged();
                }
                flagLongClick = false;
                alert.dismiss();
            }
        };
        but_ye.setOnClickListener(handl_butyes);
        View.OnClickListener handl_but_no = new View.OnClickListener() {        //
            public void onClick(View v) {
                flagLongClick = false;
                alert.dismiss();
            }
        };
        but_no.setOnClickListener(handl_but_no);
    }

    void dialogGistHeater (int gas_electro) {
        final int val = gas_electro;
        AlertDialog.Builder builder = new AlertDialog.Builder(cntx);
        final AlertDialog alert;
        View laygist = infl.inflate(R.layout.lay_set_tmp, null);
        final TextView txt_gist = laygist.findViewById(R.id.fon_lay_txt);
        final TextView edit_gist = laygist.findViewById(R.id.settmp_txt);
        int type = InputType.TYPE_CLASS_NUMBER |  InputType.TYPE_NUMBER_FLAG_DECIMAL;           // задаем тип ввода данных в формате double
        edit_gist.setInputType(type);
        edit_gist.setHint("1.00");
        final TextView but_ye = laygist.findViewById(R.id.text_ye);
        final TextView but_no = laygist.findViewById(R.id.text_no);
        txt_gist.setText("Введите значение температуры гистерезиса");
        but_ye.setText("Применить");

        builder.setView(laygist);
        alert = builder.create();
        alert.show();

        View.OnClickListener handl_butyes = new View.OnClickListener() {        //
            public void onClick(View v) {
                    if (edit_gist.getText().length() == 0) {
                        obj.airHomeTmpGist = 100;  // 1.00*100, если не было введено никаких данных
                    } else {
                        String s;
                        s = edit_gist.getText().toString();
                        obj.airHomeTmpGist = (int) (Double.parseDouble(s) * 100);
                    }
                    if (obj.airHomeTmpGist < 3500) {
                        obj.flagGASOk = val;
                        obj.cmd = DataForSend.SET_GAS;
                    } else {
                        show_txt_toast("Введите корретное значение!", cntx);
                    }
                alert.dismiss();
                dismiss();
            }
        };
        but_ye.setOnClickListener(handl_butyes);

        View.OnClickListener handl_butno = new View.OnClickListener() {        //
            public void onClick(View v) {
                alert.dismiss();
                if (val == 0) dismiss();
            }
        };
        but_no.setOnClickListener(handl_butno);
    }

    TimePickerDialog.OnTimeSetListener myCallBackDay = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hour, int minute) {
            int myHourD = hour;
            int myMinuteD = minute;
            obj.timeDaySec = hour * 3600 + minute * 60;
            daybtn.setText("" + hour + ":" + minute);
        }
    };

    TimePickerDialog.OnTimeSetListener myCallBackNight = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hour, int minute) {
            int myHourN = hour;
            int myMinuteN = minute;
            obj.timeNightSec = hour * 3600 + minute * 60;
            nightbtn.setText("" + hour + ":" + minute);
        }
    };
/*
    void DialogDel_IP () {

        new AlertDialog.Builder(cntx)
                .setMessage("Please, confirm the action")
                .setNegativeButton("НЕТ", NOIP)
                .setPositiveButton("ДА", null)
                .create()
                .show();
    }
    DialogInterface.OnClickListener NOIP = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            show_txt_toast ("oiupoiupoiu", cntx);
        }        //

    };
    */

}
