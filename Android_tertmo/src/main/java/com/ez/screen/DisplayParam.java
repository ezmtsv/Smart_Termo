package com.ez.screen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.ez.smarttermo.R;

/**
 * Created by EZ on 28.01.2023.
 */

public class DisplayParam {
    public float scale_X;
    public float scale_Y;
    public int width, hight;
    public int step, step_boiler;
    Context cntx;
    String tag = "tag";

    public DisplayParam(WindowManager w, Context cntx) {
        this.cntx = cntx;
        Display d = w.getDefaultDisplay();
        width = d.getWidth();
        hight = d.getHeight();
        step = width/45;                    // вычисление размера 1 градуса в пикселях, всего 50гр.(от 5 до 50)
        step_boiler = width/65;
        double tmp_X, tmp_Y;
        tmp_X = (double)width;
        tmp_Y = (double)hight;
        scale_X = (float)(tmp_X/1080);
        scale_Y = (float)(tmp_Y/1920);
//        Log.d (tag, "DisplayParam");
    }

    public Drawable createLayerDrawable(int ID_drw, float x, float y) {
        float w = (float)width*x;
        float h = (float)hight*y;
        Bitmap bitm = BitmapFactory.decodeResource(cntx.getResources(), ID_drw);
        Bitmap btm = bitm.createScaledBitmap(bitm, (int)w, (int)h, true);
        BitmapDrawable drawable0 = new BitmapDrawable(cntx.getResources(), btm);
        return drawable0;
    }

    public void set_pos_but(View v, float x, float y){
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(

                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins((int)x,(int)y,0,0);
        v.setLayoutParams(lp);
    }

    public BarShowTmp showtmp (int visible_set_TMP, int eY, int poseY, int temp) {
        int alpha = 100;
        BarShowTmp bar = new BarShowTmp();
        if((eY - poseY)<100&&(eY - poseY)>-100) {                       // от точки прикосновения вверх и вниз будет работать только на 100 пикселей
            if (visible_set_TMP == 1) {
                alpha = 6 * temp;
                alpha = 300-alpha; if(alpha>255)alpha = 255;
                bar.alfa = alpha;
                bar.txt = "Уст. t˚C воздуха   +" + temp;
            }
            if (visible_set_TMP == 2) {
                alpha = (int)(4.64 * (double)temp);
                alpha = 325-alpha;
                if (alpha>255)alpha = 255;
                if (alpha < 0) alpha = 1;
                bar.alfa = alpha;
                bar.txt = "Уст. t˚C гор.воды   +" + temp;
            }

        }
        return bar;
    }

    public class BarShowTmp {
        private int alfa;
        private String txt;
        BarShowTmp () { }

        public void set_alfa (int alfa) { this.alfa = alfa; }
        public void set_temp (String txt) { this.txt = txt; }

        public int get_alfa () { return alfa; }
        public String get_txt () { return txt; }
    }

}
