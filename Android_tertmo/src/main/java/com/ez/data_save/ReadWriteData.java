package com.ez.data_save;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static java.security.AccessController.getContext;

/**
 * Created by EZ on 07.02.2023.
 */

public class ReadWriteData {
    private final String FILENAME = "dataSmartTermo";
    Context cntx;
    String tag = "tag";
    void log (String s) { Log.d(tag, s); }

    public ReadWriteData (Context cntx) {
        this.cntx = cntx;
    }
    public void saveData (Object obj) {
        try {
            FileOutputStream outputStream = cntx.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(obj);
            outputStream.close();
        } catch (Exception e) { log ("Exception saveData");}
    }

    public Object readData () {
        try {
            File fl = cntx.getFileStreamPath(FILENAME);
            Object obj = null;
            if (fl.exists()) {
                FileInputStream inputStream = cntx.openFileInput(FILENAME);
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
    }
}
