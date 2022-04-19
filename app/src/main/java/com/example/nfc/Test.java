package com.example.nfc;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.ArrayList;

public class Test {

    private static final String TAG = "wj___ ";
    HandlerThread thread;
    Handler handler;

    final static int TIME_DISTANCE = 5000; // 5 秒
    final static int REPORT_COUNT = 10;
    final static int CHECK_DELAY_TIME = 10000;  // 10 秒
    final ArrayList<LPCDInfo> timeInlist = new ArrayList<>();
    final ArrayList<LPCDInfo> timeOutlist = new ArrayList<>();
    private LPCDInfo lastInfo;
    private int report1_size ;
    private int report2_size;
    private int timeInlist_size;
    private int timeOutlist_size;

    public Test() {
        thread = new HandlerThread("lpcd_report");
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    private Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            // 10秒一次的自动检查
            checkReport1(true);
            checkReport2(true);
        }
    };

    public void onLPCDEvent(String msg) {
        handler.removeCallbacks(checkRunnable);
        LPCDInfo info = new LPCDInfo(msg);
        LPCDInfo last = lastInfo;
        if (last == null) {
            lastInfo = info; // 第一个事件
        } else {
            if (info.timeInDistance(last, TIME_DISTANCE)) {
                if (timeInlist.isEmpty()) {
                    timeInlist.add(last);
                }
                timeInlist.add(info);
                checkReport2(false); // 检查是否需要上报事件2
            } else {
                if (timeOutlist.isEmpty()) {
                    timeOutlist.add(last);
                }
                timeOutlist.add(info);
                checkReport1(false); // 检查是否需要上报事件1
            }
        }
        handler.postDelayed(checkRunnable, CHECK_DELAY_TIME);
        lastInfo = info; // 第一个事件
    }

    private void checkReport1(boolean reportCount) {
        if (timeInlist.size() >= REPORT_COUNT) { // 条件 1
            // 当定时任务到了，判断一下有没有触发2种情况的上报，如果触发了，那就不管了
            timeInlist_size = timeInlist.size() - 1;
            report1();
            timeInlist.clear();
        } else {
            // 当定时任务到了，如果没有触发2种情况的上报，那就上报之前统计的次数
            if (reportCount && timeInlist.size() > 0) {
                report1Count(timeInlist.size());
                timeInlist.clear();
            }
        }
    }


    private void checkReport2(boolean reportCount) {
        if (timeOutlist.size() >= REPORT_COUNT) { // 条件 1
            timeOutlist_size = timeOutlist.size() - 2;
            report2();
            timeOutlist.clear();
        } else {
            if (reportCount && timeOutlist.size() > 0) {
                report2Count(timeInlist.size());
                timeOutlist.clear();
            }
        }
    }

    private void report1() {
        //
        Log.e(TAG," ------ 上报 report1 " + " report1_size " + report1_size +
                " timeInlist_size " + timeInlist_size + " 相减后次数 " + (timeInlist_size - timeOutlist_size));


    }

    private void report1Count(int size) {
        //
        report1_size = size;
        Log.e(TAG," ------ 上报 report1 " +  size);
    }

    private void report2() {
        //
        Log.e(TAG," ------ 上报 report2 " + " report2_size " + report2_size +
                " timeOutlist_size " + timeOutlist_size + " 相减后次数 " + (timeOutlist_size - timeInlist_size));
    }

    private void report2Count(int size) {
        //
        report2_size = size;
        Log.e(TAG," ------ 上报 report2 " + size);
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
