package com.ez.smarttermo;

import com.ez.data_receive.ObjTelemetry;
import com.ez.data_receive.ReadyDataForScreen;
import com.ez.data_receive.TelemetrySmartTermo;
import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;


/**
 * Created by EZ on 29.01.2023.
 */

public class LoaderData extends AsyncTaskLoader<ReadyDataForScreen> {
    private String tag = "tag";
    void log (String s) { Log.d(tag, s); }
    private char [] buf;
    private int size;
    private int port = 8558;
    private int comand;
    private final String SOCKET_OK = "Соединение установлено";
    ObjTelemetry obj;
    Cur_Data_ForSend objsend;

    public LoaderData(Context context) {
        super(context);
 //       comand = DataForSend.REQ_DATA;
    }
//    public LoaderData(Context context, Bundle bndl) {
//        super(context);
//        obj = (ObjTelemetry)bndl.getSerializable("OBJ_SEND");
//        objsend = new DataForSend(obj).getInitClasses();
//        comand = 0;
//    }
    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        buf = new char[300];
        forceLoad();
    }
    @Override
    public ReadyDataForScreen loadInBackground() {
//        log ("LoaderData loadInBackground()");
        ReadyDataForScreen data = null;
        final TCPClient client = TCPClient.getInstance(obj.ServerIP, port);
        TCPClient.GetData getData = client.readData();
        if (getData != null) {
            buf = getData.get_buf();
            size = getData.get_size();

//            for (int i = 0; i < 192; i++) {
//                log("get_char: " + buf[i] + ", int val = " + (int)buf[i]);
//            }

        }
        if (size == 192) {
            try {
                data = new ReadyDataForScreen(new TelemetrySmartTermo(buf));
                data.set_statusSocket(client.getStatusSocket());
            } catch (Exception e) {
                log("LoaderData Exception - buf[] not initialized!");
            }
        }
        if (comand != DataForSend.REQ_DATA) {
            if (objsend != null) {
                log("LoaderData objsend != null  send_message");
                client.send_message(objsend.readyStringData());
            }

        }
        return data;
    }

}
