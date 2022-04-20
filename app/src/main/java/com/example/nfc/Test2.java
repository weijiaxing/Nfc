package com.example.nfc;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.ArrayList;

public class Test2 {

    private static final String TAG = "wj___ ";
    HandlerThread thread;
    Handler handler;

    final static int TIME_DISTANCE = 5000; // 5 秒
    final static int REPORT_COUNT = 10;
    final static int CHECK_DELAY_TIME = 10000;  // 10 秒
    final ArrayList<LPCDInfo> timeInlist = new ArrayList<>();
    final ArrayList<LPCDInfo> timeOutlist = new ArrayList<>();
    private LPCDInfo lastInfo;

    public Test2() {
        thread = new HandlerThread("lpcd_report");
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    private Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            // 10秒一次的自动检查
            checkReport1(true, false);
            checkReport2(true, false);
        }
    };

    public void onLPCDEvent(String msg) {
        handler.removeCallbacks(checkRunnable);
        LPCDInfo info = new LPCDInfo(msg);
        LPCDInfo last = lastInfo;
        lastInfo = info;
        if (last == null) {
            lastInfo = info; // 第一个事件
        } else {
            if (info.timeInDistance(last, TIME_DISTANCE)) {
                if (timeInlist.isEmpty()) {
                    timeInlist.add(last);
                }
                timeInlist.add(info); // 记录事件1
                checkReport2(false, true); // 检查是否需要上报事件2
            } else {
                if (timeOutlist.isEmpty()) {
                    timeOutlist.add(last);
                }
                timeOutlist.add(info); // // 记录事件2
                checkReport1(false, true); // 检查是否需要上报事件1
            }
        }
        // 启动自动检查定时器
        handler.postDelayed(checkRunnable, CHECK_DELAY_TIME);
    }

    private void checkReport1(boolean reportCount, boolean clearList) {
        if (timeInlist.size() >= REPORT_COUNT) { // 条件 1
            // 当定时任务到了，判断一下有没有触发2种情况的上报，如果触发了，那就不管了
            report1();
            timeInlist.clear();
        } else {
            // 当定时任务到了，如果没有触发2种情况的上报，那就上报之前统计的次数
            if (reportCount && timeInlist.size() > 0) {
                report1Count(timeInlist.size());
                timeInlist.clear();
            }
        }
        if (clearList) {
            // 不管有没有上报，都需要清楚记录
            timeInlist.clear();
        }
    }


    private void checkReport2(boolean reportCount, boolean clearList) {
        if (timeOutlist.size() >= REPORT_COUNT) { // 条件 1
            report2();
            timeOutlist.clear();
        } else {
            if (reportCount && timeOutlist.size() > 0) {
                report2Count(timeInlist.size());
                timeOutlist.clear();
            }
        }
        if (clearList) {
            timeOutlist.clear();
        }
    }

    private void report1() {
        //
        Log.e(TAG," ------ 上报 report1 ");
    }

    private void report1Count(int size) {
        //
        Log.e(TAG," ------ 上报 report1 " +  size);
    }

    private void report2() {
        //
        Log.e(TAG," ------ 上报 report2 ");
    }

    private void report2Count(int size) {
        //
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