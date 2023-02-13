package com.ez.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.ez.data_receive.ObjTelemetry;
import com.ez.smarttermo.DataForSend;

import static java.security.AccessController.getContext;

/**
 * Created by EZ on 05.02.2023.
 */

public class VisualCommand extends Thread {
    ObjTelemetry obj;
    Context cntx;
    String tag = "tag";
    void log (String s) { Log.d (tag, s); };
    boolean ok;
    static public final int ANSWERCOM_WAIT = 1;
    static public final int ANSWERCOM_OK = 3;
    static public final int ANSWERCOM_NO = 6;
    private int answ;

    AppCompatActivity activity;

    public VisualCommand (String name, ObjTelemetry obj, Context cntx) {
        super (name);
        this.obj = obj;
        this.cntx = cntx;
    }
    @Override
    public void run() {
        String [] sh = {">>>......>>>", "..>>>......>", "....>>>.....", "......>>>..."};
        int j = 0;
        ok = false;

        while (!ok) {
            Message msg = new Message();
            msg.obj = sh[j];
            showCOM.sendMessage (msg);
            SystemClock.sleep(3000);
            j++;
            if (j == 4) j = 0;
            if (obj.cnt_repeat > 4) break;
            ok = checkCommOk(obj);
        }
        if (ok) {
            Message msg = new Message();
            if (obj.cmd == DataForSend.SYNCHRO) msg.obj = "Устройство синхронизировано!";
            else msg.obj = "Выполнено!";
            showCOM.sendMessage (msg);
            log ("VisualCommand Выполнено!");
            answ = ANSWERCOM_OK;
        } else {
            Message msg = new Message();
            msg.obj = "Устройство не подтвердило команду!";
            showCOM.sendMessage (msg);
            log ("VisualCommand команда не прошла!");
            answ = ANSWERCOM_NO;
        }
        ((OnCompleteListener) activity).onCompleteANSW (answ);
    }

    Handler showCOM = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(cntx, (String)msg.obj, Toast.LENGTH_SHORT).show();
        }
    };

    boolean checkCommOk (ObjTelemetry obj) {
        boolean ok = false;

        switch (obj.cmd) {
            case DataForSend.SET_TMP:
                if (obj.setAirTmpCUR == obj.setAirTmp) ok = true;
//                log ("VisualCommand checkCommOk obj.cmd " + obj.cmd + " obj.setAirTmpCUR " + obj.setAirTmpCUR + " obj.setAirTmp " + obj.setAirTmp);
                break;
            case DataForSend.SET_TMPBR:
                if (obj.setBoilerTmpCUR == obj.setBoilerTmp) ok = true;
                break;
            case DataForSend.SET_ON_OFF_BOILER:
                if (obj.flagBoilerONCUR == obj.flagBoilerON) ok = true;
                break;
            case DataForSend.SET_ON_OFF_ALARM_HEAT:
                if (obj.flagNOTHeatCUR == obj.flagNOTHeat) ok = true;
                break;
            case DataForSend.SYNCHRO:
                if (obj.cmdCUR == obj.cmd) ok = true;
                break;
            case DataForSend.SYNCHRO_TIME_TARIFF:       ///////////////////
                if (obj.timeNightSecCUR == obj.timeNightSec && obj.timeDaySecCUR == obj.timeDaySec) ok = true;
                break;
            case DataForSend.SET_COEF:
                if (obj.cmdCUR == obj.cmd) ok = true;
                break;
            case DataForSend.SET_GAS:
                if (obj.flagGASOkCUR == obj.flagGASOk) ok = true;
                break;
            case DataForSend.SET_WORK_TARIFF:
                if (obj.flagTarifOkCUR == obj.flagTarifOk) ok = true;
                break;
            case DataForSend.COOL_MODE_ON:
                if (obj.flagInversOUTOkCUR == obj.flagInversOUTOk) ok = true;
                break;
            case DataForSend.COOL_MODE_OFF:
                if (obj.flagInversOUTOkCUR == obj.flagInversOUTOk) ok = true;
                break;
            case DataForSend.SET_LINK:
                if (obj.cmdCUR == obj.cmd) ok = true;
                break;
            case DataForSend.CONFIG_MAIL:
                if (obj.cmdCUR == obj.cmd) ok = true;
                break;
            case DataForSend.LOAD_DEF:
                if (obj.cmdCUR == obj.cmd) ok = true;
                break;
//            default:
//                if (obj.cmdCUR == obj.cmd) ok = true;
//                break;
        }
//        Log.d ("tag", "objTelemetry.flagTarifOkCUR - obj.cmd " + obj.cmd + ", obj.flagTarifOkCUR "
//                + obj.flagTarifOkCUR + ", obj.flagTarifOk " + obj.flagTarifOk + ", ok " + ok);
        return ok;
    }

    public interface OnCompleteListener {
        void onCompleteANSW(int answ);
    }

    public void onAttachANSW(AppCompatActivity activity) {
        log ("DialogSelect onAttach");
        try {
            this.activity = activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }

}
