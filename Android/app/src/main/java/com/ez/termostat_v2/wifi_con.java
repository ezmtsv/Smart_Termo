package com.ez.termostat_v2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by evan on 11.11.2021.
 */

public class wifi_con {
    private BroadcastReceiver wifiReceiver;
    private WifiManager wifiManager;
    Context cntx;
    String tag = "TAG";
    String SSID_cur = "";
    String net_list[][] = new String[20][2];
    boolean flag_scan = false;
    int cnt_net = 0;
    private static final int MY_REQUEST_CODE = 123;
    wifi_con(Context context){
        cntx = context;
        wifiManager = (WifiManager) cntx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // Instantiate broadcast receiver
        wifiReceiver = new WifiBroadcastReceiver();
        // Register the receiver
        cntx.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }
    // Define class to listen to broadcasts
    class WifiBroadcastReceiver extends BroadcastReceiver  {
        @Override
        public void onReceive(Context context, Intent intent)   {
            try {
                Log.d(tag, "onReceive()");
                boolean ok = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (ok) {
                    Log.d(tag, "Scan OK");
                    List<ScanResult> list = wifiManager.getScanResults();
                    showNetworks(list);
                    //showNetworksDetails(list);
                    flag_scan = true;
                } else {
                    Log.d(tag, "Scan not OK");
                    Log.d(tag, "net WIFI not OK");
                }
                WifiInfo info = wifiManager.getConnectionInfo();
                SSID_cur = info.getSSID();
                Log.d(tag, "WifiInfo info " + SSID_cur);
            } catch (Exception e) { Log.d(tag, "WifiBroadcastReceiver Exception " + e); }
        }
    }
    String[] showNetworks(List<ScanResult> results) {
        String val = "";
        String [] net = new String[results.size()];
        int i = 0;

        for( final ScanResult result: results)  {
            final String networkCapabilities = result.capabilities;
            final String networkSSID = result.SSID; // Network Name.
            val = networkSSID + " ("+networkCapabilities+")";
            net[i] = result.toString();
            net_list[i][0] = networkSSID;
            net_list[i][1] = networkCapabilities;
//            connectToNetwork(networkCapabilities, networkSSID, pass);
//            Log.d(tag, "Scan_showNetworks "+net[i]);
            i++;
        }
        cnt_net = i;
        return net;
    }


    public void askAndStartScanWifi()  {
        flag_scan = false;
        try {
            this.doStartScanWifi();
        } catch (Exception e) { Log.d(tag, "Exception_doStartScanWifi "+e);}
    }

    private void doStartScanWifi()  {
        this.wifiManager.startScan();
    }


    public String showWifiState()  {
        int state = this.wifiManager.getWifiState();
        String statusInfo = "Unknown";

        switch (state)  {
            case WifiManager.WIFI_STATE_DISABLING:
                statusInfo = "Disabling";
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                statusInfo = "Disabled";
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                statusInfo = "Enabling";
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                statusInfo = "Enabled";
                break;
            case WifiManager.WIFI_STATE_UNKNOWN:
                statusInfo = "Unknown";
                break;
            default:
                statusInfo = "Unknown";
                break;
        }
        Log.d(tag, "Wifi Status: " + statusInfo);
        return statusInfo;
    }


    public void connectToNetwork(String networkCapabilities, String networkSSID, String pass)  {
        Log.d(tag, "Connecting to network: "+ networkSSID);

        String networkPass = pass;
        //
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID =  "\"" + networkSSID + "\"";

        if(networkCapabilities.toUpperCase().contains("WEP")) { // WEP Network.
            Log.d(tag, "Connecting to WEP Network");

            wifiConfig.wepKeys[0] = "\"" + networkPass + "\"";
            wifiConfig.wepTxKeyIndex = 0;
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        } else if(networkCapabilities.toUpperCase().contains("WPA")) { // WPA Network
            Log.d(tag, "Connecting to WPA Network");
            wifiConfig.preSharedKey = "\""+ networkPass +"\"";
        } else  { // OPEN Network.
            Log.d(tag, "Connecting to OPEN Network");
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }

        wifiManager.addNetwork(wifiConfig);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

        for( WifiConfiguration config : list ) {
            if(config.SSID != null && config.SSID.equals("\"" + networkSSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(config.networkId, true);
                wifiManager.reconnect();
                break;
            }
        }

    }

    void stop_rec(){

        cntx.unregisterReceiver(this.wifiReceiver);
  //      android.os.Process.killProcess(android.os.Process.myPid());

    }
}
