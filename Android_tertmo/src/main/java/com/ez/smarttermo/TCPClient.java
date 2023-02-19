package com.ez.smarttermo;

import android.os.SystemClock;
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
 * Created by EZ on 17.02.2023.
 */

public class TCPClient {
    static String ip;
    static int port;
    private boolean waitReadOK;
    private Socket socket;
    private String status;
    private BufferedReader mBufferIn;
    static void log (String s) { Log.d ("tag", s); }

    private static TCPClient instance = null;
    private TCPClient(String ipadr, int port) {
        this.ip = ipadr;
        this.port = port;
    }
    public static TCPClient getInstance(String ipadr, int port) {
        if (instance == null) {
            instance = new TCPClient(ipadr, port);
        } else {
            if (!ip.equals(ipadr)) {
                instance = null;
                instance = new TCPClient(ipadr, port);
            }
        }
//        log ("TCPClient ipadr " + ipadr + " ip " + ip);
        return instance;
    }

    public void closeSocket () {
        try {
//            instance = null;
            socket.close();
            socket = null;
        } catch (Exception e) {
            log ("LoaderData Exception - Error close socket!");
        }
    }

    public String connectTCP () {
        try {
            SocketAddress socketAddress = new InetSocketAddress(ip, port);
            socket = new Socket();
            socket.connect(socketAddress, 2000);  // подключение к серверу, 2сек timeout
            log ("TCPClient Connect OK!");
            status = "Соединение установлено";
        } catch (SocketException e) {
            status = "Невозможно подключиться к серверу";
            log ("TCPClient Exception " + status);
            closeSocket();
        } catch (UnknownHostException e) {
            status = "Невозможно подключиться к серверу.";
            log ("TCPClient Exception " + status);
            closeSocket();
        } catch (IOException e) {
            status = "Невозможно подключиться к серверу..";
            log ("TCPClient Exception " + status);
            closeSocket();
        } catch (Exception e) {
            status = "Невозможно подключиться к серверу...";
            log ("TCPClient Exception " + status + " ip " + ip + " port " + port);
            closeSocket();
        }
        return status;
    }

    public String getStatusSocket () {
        return status;
    }

    public void send_message (String message) {
        log ("TCPClient message: " + message);
        try {
            final String messageOUT = message;
            byte[] buf = messageOUT.getBytes();
            if (socket == null) connectTCP ();
            OutputStream sout = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(sout);
            dataOutputStream.write(buf);
//            char[] ch = messageOUT.toCharArray();
//            for (char i : ch) {
//                log("char: " + i + ", int val = " + (int) i);
//            }
//            log ("TCPClient message_sended: " + messageOUT);
        } catch (IOException e) {
            log ("TCPClient message_not sended - IOException");
            closeSocket();
        } catch (Exception e) {
            log ("TCPClient message_not sended Exception " + e);
            closeSocket();
        }
    }

    public class GetData {
        private  int size;
        private char [] buf;
        GetData (char [] buf, int size) {
            this.buf = buf;
            this.size = size;
        }
        int get_size () { return size; }
        char [] get_buf () {return buf; }
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

    GetData readData () {
        char[] buf = new char[300];
        int size;
        GetData getdata;
        if (socket == null) connectTCP();
        try {
            WaitRead waitread = new WaitRead();     // при зависании получения данных "mBufferIn.read(buf)" более 5 сек. произойдет закрытие сокета и сброс
            new Thread(waitread).start();
            mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            size = mBufferIn.read(buf);             // ждем когда в буфере появятся данные и считываем колличество принятых символов и само сообщение в буфер buff
            getdata = new GetData(buf, size);
            waitReadOK = true;
            return getdata;
        } catch (Exception e) {
            log ("LoaderData Exception - Error get data! " + socket);
            status = "Нет данных от устройства...";
            closeSocket();
            return null;
        }
    }
}