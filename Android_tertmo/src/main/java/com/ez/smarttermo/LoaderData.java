package com.ez.smarttermo;

import com.ez.data_receive.ObjTelemetry;
import com.ez.data_receive.ReadyDataForScreen;
import com.ez.data_receive.TelemetrySmartTermo;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by EZ on 29.01.2023.
 */

public class LoaderData extends AsyncTaskLoader<ReadyDataForScreen> {
    private String tag = "tag";
    void log (String s) { Log.d(tag, s); }
    private char [] buf;
    private int size;
//    private String adr;     // = "192.168.2.69";
    private int port = 8558;
    private int comand;
    private final String SOCKET_OK = "Соединение установлено";
    ObjTelemetry obj;
    Cur_Data_ForSend objsend;

    public LoaderData(Context context) {
        super(context);
        comand = DataForSend.REQ_DATA;
//        log ("LoaderData Context context, int cmd " + comand);
    }
    public LoaderData(Context context, Bundle bndl) {
        super(context);
        obj = (ObjTelemetry)bndl.getSerializable("OBJ_SEND");
        objsend = new DataForSend(obj).getInitClasses();
        comand = 0;
//        log ("LoaderData Context context, Bundle bndl "+ comand);
    }
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
        TcpClient client = new TcpClient(obj.ServerIP, port);
        String status = "";
        status = client.connectTCP();
        if (status.equals(SOCKET_OK)) {
            if (comand == DataForSend.REQ_DATA) {
//                log ("LoaderData cmd == DataForSend.REQ_DATA");
                GetData getData = client.readData();
//                log("GetData - " + getData);
                if (getData != null) {
                    buf = getData.get_buf();
                    size = getData.get_size();
                }
                if (size == 192) {
                    try {
                        data = new ReadyDataForScreen(new TelemetrySmartTermo(buf));
                        data.set_statusSocket(status);
                    } catch (Exception e) {
                        log("LoaderData Exception - buf[] not initialized!");
                    }
                }
            } else {
//                log ("LoaderData cmd != DataForSend.REQ_DATA");
                if (objsend != null) client.send_message (objsend.readyStringData());
            }
        }

        return data;
    }

    class GetData {
        private  int size;
        private char [] buf;
        GetData (char [] buf, int size) {
            this.buf = buf;
            this.size = size;
        }
        int get_size () { return size; }
        char [] get_buf () {return buf; }
    }

    private class TcpClient {
        private String status_SERVER;
        private String dstAddress;
        private int dstPort;
        private Socket socket;
        private BufferedReader mBufferIn;
        private boolean waitReadOK;
        private boolean flagSendData;


        TcpClient (String dstAddress, int dstPort) {
            status_SERVER = "подключаюсь..";
            this.dstAddress = dstAddress;
            this.dstPort = dstPort;
            size = 0;
            waitReadOK = false;
            flagSendData = false;
        }
        String connectTCP () {
            try {
                SocketAddress socketAddress = new InetSocketAddress(dstAddress, dstPort);
                socket = new Socket();
                socket.connect(socketAddress, 2000);  // подключение к серверу, 2сек timeout
                Log.d(tag, "LoaderData Connect OK!");
                status_SERVER = "Соединение установлено";
            } catch (SocketException e) {
                status_SERVER = "Невозможно подключиться к серверу";
                Log.d(tag, "LoaderData Exception " + status_SERVER);
                closeSocket();
            } catch (UnknownHostException e) {
                status_SERVER = "Невозможно подключиться к серверу.";
                Log.d(tag, "LoaderData Exception " + status_SERVER);
                closeSocket();
            } catch (IOException e) {
                status_SERVER = "Невозможно подключиться к серверу..";
                Log.d(tag, "LoaderData Exception " + status_SERVER);
                closeSocket();
            } catch (Exception e) {
                status_SERVER = "Невозможно подключиться к серверу...";
                Log.d(tag, "LoaderData Exception " + status_SERVER);
                closeSocket();
            }
            return status_SERVER;
        }

        GetData readData () {
            char[] buf = new char[300];
            int size;
            GetData getdata;

            try {
                WaitRead waitread = new WaitRead();     // при зависании получения данных "mBufferIn.read(buf)" более 5 сек. произойдет закрытие сокета и сброс
                new Thread(waitread).start();
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                size = mBufferIn.read(buf);             // ждем когда в буфере появятся данные и считываем колличество принятых символов и само сообщение в буфер buff
                getdata = new GetData(buf, size);
                waitReadOK = true;
                if (socket != null) {
                    if (!flagSendData) closeSocket();
                }
                return getdata;
            } catch (Exception e) {
                log ("LoaderData Exception - Error get data!");
                if (socket != null) {
                    closeSocket();
                }
                return null;
            }
        }

        class WaitRead implements Runnable {
            private int cnt;
            WaitRead () {
                cnt = 0;
            }
            @Override
            public void run() {
                while (!waitReadOK) {
                    SystemClock.sleep(500);
                    cnt++;
                    if (cnt > 5) {
                        waitReadOK = true;
                        closeSocket();
                        log ("LoaderData WaitRead Exit, cnt =  " + cnt);
                    }
                }
            }
        }

        void send_message (String message) {
/*            try {
                flagSendData = true;
                GetData getdata = readData();
                byte[] buf = new byte[1024];
                buf = message.getBytes();
                OutputStream sout = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(sout);
                dataOutputStream.write(buf);
                closeSocket();
                Log.d (tag, "message_sended: " + message);
            }
            catch (IOException e) {
                Log.d(tag, "message_not sended - IOException");
                closeSocket();
            }
            catch (Exception e) {
                Log.d(tag, "message_not sended Exception " + e);
            }*/
// debug
            try {
//                flagSendData = true;
//                GetData getdata = readData();
                char[] ch = message.toCharArray();
                for (char i : ch) {
                    log ("char: " + i + ", int val = " + (int)i);
                }
                //log ("message_SEND:" + message);
            } catch (Exception e) { log ("LoaderData message_Exception:::  " + e); }


            closeSocket();
        }

        public void closeSocket () {
            try {
                socket.close();
            } catch (Exception e) {
                log ("LoaderData Exception - Error close socket!");
            }
        }
    }

}
