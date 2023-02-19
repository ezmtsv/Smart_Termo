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
import com.ez.smarttermo.TCPClient;

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
    private String message;

    AppCompatActivity activity;

    public VisualCommand (String name, ObjTelemetry obj, String message, Context cntx) {
        super (name);
        this.obj = obj;
        this.cntx = cntx;
        this.message = message;
    }
    @Override
    public void run() {
        String [] sh = {">>>......>>>", "..>>>......>", "....>>>.....", "......>>>..."};
        int j = 0;
        int k = 0;
        ok = false;
        TCPClient client = TCPClient.getInstance(obj.ServerIP, 8558);
        client.send_message(message);
        Message msg = new Message();
        msg.obj = sh[j];
        showCOM.sendMessage (msg);
        j++;
        while (!ok) {
//            log ("VisualCommand obj.cnt_repeat " + obj.cnt_repeat);
            msg = new Message();
            msg.obj = sh[j];
            k++;
            SystemClock.sleep(100);
            if (k == 30) {
                j++;
                if (j == 4) j = 0;
                client.send_message(message);
                obj.cnt_repeat++;
                showCOM.sendMessage (msg);
                k = 0;
            }
            //SystemClock.sleep(3000);
            if (obj.cnt_repeat > 20) break;
            ok = checkCommOk(obj);
        }
        if (ok) {
            msg = new Message();
            if (obj.cmd == DataForSend.SYNCHRO) msg.obj = "Устройство синхронизировано!";
            else msg.obj = "Выполнено!";
            showCOM.sendMessage (msg);
            log ("VisualCommand Выполнено!");
            answ = ANSWERCOM_OK;
        } else {
            msg = new Message();
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
//                log ("VisualCommand obj.cmd " + obj.cmd + " obj.cmdCur " + obj.cmdCUR + " obj.setAirTmpCUR " + obj.setAirTmpCUR + " obj.setAirTmp " + obj.setAirTmp);
                break;
            case DataForSend.SET_TMPBR:
                if (obj.setBoilerTmpCUR == obj.setBoilerTmp) ok = true;
//                log ("VisualCommand obj.cmd " + obj.cmd + " obj.cmdCur " + obj.cmdCUR + " obj.setBoilerTmp " + obj.setBoilerTmp + " obj.setBoilerTmpCUR " + obj.setBoilerTmpCUR);
                break;
            case DataForSend.SET_ON_OFF_BOILER:
                if (obj.flagBoilerONCUR == obj.flagBoilerON) ok = true;
//                log ("VisualCommand obj.cmd " + obj.cmd + " obj.cmdCur " + obj.cmdCUR + " flagBoilerON " + obj.flagBoilerON + " flagBoilerONCUR " + obj.flagBoilerONCUR);
                break;
            case DataForSend.SET_ON_OFF_ALARM_HEAT:
                if (obj.flagNOTHeatCUR == obj.flagNOTHeat) ok = true;
//                log ("VisualCommand obj.cmd " + obj.cmd + " obj.cmdCur " + obj.cmdCUR + " obj.flagNOTHeat " + obj.flagNOTHeat + " flagNOTHeatCUR " + obj.flagNOTHeatCUR);
                break;
            case DataForSend.SYNCHRO:
                if (obj.cmdCUR == DataForSend.SYNCHRO && obj.count_cmdCUR == obj.count_cmd) ok = true;
//                log ("VisualCommand obj.cmd " + obj.cmd + " obj.cmdCur " + obj.cmdCUR);
                break;
            case DataForSend.SYNCHRO_TIME_TARIFF:       ///////////////////
                if (obj.timeNightSecCUR == obj.timeNightSec && obj.timeDaySecCUR == obj.timeDaySec) ok = true;
//                log ("VisualCommand obj.cmd " + obj.cmd + " obj.cmdCur " + obj.cmdCUR + " obj.timeNightSecCUR " + obj.timeNightSecCUR + " obj.timeDaySecCUR " + obj.timeDaySecCUR);
//                log ("VisualCommand obj.cmd " + obj.cmd + " obj.cmdCur " + obj.cmdCUR + " obj.timeNightSec " + obj.timeNightSec + " obj.timeDaySec " + obj.timeDaySec);
                break;
            case DataForSend.SET_COEF:
                if (obj.cmdCUR == DataForSend.SYNCHRO && obj.count_cmdCUR == obj.count_cmd) ok = true;
//                log ("VisualCommand obj.cmd " + obj.cmd + " obj.cmdCur " + obj.cmdCUR);
                break;
            case DataForSend.SET_GAS:
                if (obj.flagGASOkCUR == obj.flagGASOk) ok = true;
//                log ("VisualCommand obj.cmd " + obj.cmd + " obj.cmdCur " + obj.cmdCUR + " flagGASOkCUR " + obj.flagGASOkCUR + " flagGASOk " + obj.flagGASOk);
                break;
            case DataForSend.SET_WORK_TARIFF:
                if (obj.flagTarifOkCUR == obj.flagTarifOk) ok = true;
//                log ("VisualCommand obj.cmd " + obj.cmd + " obj.cmdCur " + obj.cmdCUR + " flagTarifOkCUR " + obj.flagTarifOkCUR + " flagTarifOk " + obj.flagTarifOk);
                break;
            case DataForSend.COOL_MODE_ON:
                if (obj.flagInversOUTOkCUR == obj.flagInversOUTOk) ok = true;
//                log ("VisualCommand obj.cmd " + obj.cmd + " obj.cmdCur " + obj.cmdCUR + " flagInversOUTOkCUR " + obj.flagInversOUTOkCUR + " flagInversOUTOk " + obj.flagInversOUTOk);
                break;
            case DataForSend.COOL_MODE_OFF:
                if (obj.flagInversOUTOkCUR == obj.flagInversOUTOk) ok = true;
//                log ("VisualCommand obj.cmd " + obj.cmd + " obj.cmdCur " + obj.cmdCUR + " flagInversOUTOkCUR " + obj.flagInversOUTOkCUR + " flagInversOUTOk " + obj.flagInversOUTOk);
                break;
            case DataForSend.SET_LINK:
                if (obj.cmdCUR == obj.cmd) ok = true;
//                log ("VisualCommand obj.cmd " + obj.cmd + " obj.cmdCur " + obj.cmdCUR);
                break;
            case DataForSend.CONFIG_MAIL:
                if (obj.cmdCUR == DataForSend.SYNCHRO && obj.count_cmdCUR == obj.count_cmd) ok = true;
//                log ("VisualCommand obj.cmd " + obj.cmd + " obj.cmdCur " + obj.cmdCUR);
                break;
            case DataForSend.LOAD_DEF:
                if (obj.cmdCUR == DataForSend.SYNCHRO && obj.count_cmdCUR == obj.count_cmd) ok = true;
//                log ("VisualCommand obj.cmd " + obj.cmd + " obj.cmdCur " + obj.cmdCUR);
                break;
            case DataForSend.SET_TMPGISTBR:
                if (obj.boilerTmpGistCUR == obj.boilerTmpGist) ok = true;
//                log ("VisualCommand obj.cmd " + obj.cmd + " obj.cmdCur " + obj.cmdCUR + " boilerTmpGistCUR " + obj.boilerTmpGistCUR + " boilerTmpGist " + obj.boilerTmpGist);
                break;
            default:
                if (obj.cmdCUR == DataForSend.SYNCHRO && obj.count_cmdCUR == obj.count_cmd) ok = true;
//                break;
        }
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
