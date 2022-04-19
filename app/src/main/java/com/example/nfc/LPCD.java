package com.example.nfc;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.ArrayList;

public class LPCD {

    private static final String TAG = "wj___ ";
    HandlerThread thread;
    Handler handler;

    final static int TIME_DISTANCE = 5000; // 5 秒
    final static int REPORT_COUNT = 10;
    final static int CHECK_DELAY_TIME = 10000;  // 10 秒
    final ArrayList<LPCDInfo> list = new ArrayList<>();
    private int size = 0;
    private int m5sIn= 0;
    private int m5sOut = 0;

    public LPCD() {
        thread = new HandlerThread("lpcd_report");
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    private Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            checkReport1();
            list.clear();
        }
    };

    LPCDInfo getLastInfo() {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    public void onLPCDEvent(String msg) {
        handler.removeCallbacks(checkRunnable);
        LPCDInfo info = new LPCDInfo(msg);
        LPCDInfo last = getLastInfo();
        size = list.size();
        if (last == null) {
            list.add(info);
        } else {
            if (info.timeInDistance(last, TIME_DISTANCE)) {
                m5sIn = size;
                Log.e(TAG," ------ 相邻事件5秒内 " + " size " + size + " m5sIn " + m5sIn);
                list.add(info);
                checkReport1();

            } else {
                Log.e(TAG," ------ 相邻事件5秒外 " + " size " + size);
                m5sOut = size;
                report2();
                checkReport1();
                list.clear();
                list.add(info);
            }
        }
        handler.postDelayed(checkRunnable, CHECK_DELAY_TIME);
    }

    private void checkReport1() {
        if (list.size() == REPORT_COUNT) { // 条件 1
            report1();
            list.clear();
        }
    }

    private void report1() {
        //
        Log.e(TAG," ------ 上报 report1 " + " m5sIn " + m5sIn);
    }

    private void report2() {
        //
        Log.e(TAG," ------ 上报 report2 " + " m5sOut " + m5sOut);
    }


    public class LPCDInfo {
        final String info;
        final long time; // 单位毫秒

        public LPCDInfo(String info) {
            this.info = info;
            this.time = System.currentTimeMillis();
        }

        public boolean timeInDistance(LPCDInfo info, int distance) {
            if (info == null) {
                return false;
            }
            Log.e(TAG," ------ 时间差 " + (this.time - info.time));
            return Math.abs(this.time - info.time) < distance;
        }
    }
}
