package com.ez.data_save;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by EZ on 07.02.2023.
 */

public class DataSave implements Serializable{
    private boolean dialogStart;
    private HashMap<String, String []> devices;
    private String curDevice;
    private String curDeviceHeaterName1;
    private String curDeviceHeaterName2;

    public DataSave () {
        dialogStart = true;
        devices = new HashMap<>();
        curDevice = "192.168.4.1";
        curDeviceHeaterName1 = "Газ";
        curDeviceHeaterName2 = "Электро";
        devices.put (curDevice, new String[]{curDeviceHeaterName1, curDeviceHeaterName2});
    }

    public void set_dialogStart (boolean dialogStart) {
        this.dialogStart = dialogStart;
    }
    public boolean get_dialogStart () {
        return dialogStart;
    }
    public void put_devices (HashMap<String, String []> dev) {
        devices = dev;
    }
    public HashMap<String, String []> get_devices () {
        return devices;
    }
    public void set_curDevice (String key) {
        curDevice = key;
    }
    public String get_curDevice () {
        return curDevice;
    }
    public String get_curDeviceHeaterName1 () {
        String[] s = devices.get(curDevice);
        return s[0];
    }
    public String get_curDeviceHeaterName2 () {
        String[] s = devices.get(curDevice);
        return s[1];
    }

}
