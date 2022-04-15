package com.example.nfc;

import android.os.Handler;
import android.os.HandlerThread;

import java.util.ArrayList;

public class LPCD {
    HandlerThread thread;
    Handler handler;

    final static int TIME_DISTANCE = 5000; // 5 秒
    final static int REPORT_COUNT = 10;
    final static int CHECK_DELAY_TIME = 10000;  // 10 秒
    final ArrayList<LPCDInfo> list = new ArrayList<>();

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

    /**
     * 1，相邻lpcd事件时间差在5秒内，发生超10次（记录次数）
     * 2，出现相邻lpcd事件时间差5秒外，上报1，并且开始记录本次事件发生次数 （记录本次次数）
     * 3，如果上一次连续次数超过十次后，后面再没有来LPCD事件，这种情况下之前统计的连续次数就不会触发2这种情况，
     * 但是之前统计的次数需要上报，那就在每次收到LPCD事件后，起一个定时任务（10S），每次收到都去刷新，
     * 当定时任务到了，判断一下有没有触发2的上报，如果触发了，那就不管了，如果没有触发2的上报，那就上报之前统计的次数；
     */
    public void onLPCDEvent(String msg) {
        handler.removeCallbacks(checkRunnable);
        LPCDInfo info = new LPCDInfo(msg);
        LPCDInfo last = getLastInfo();
        if (last == null) {
            list.add(info);
        } else {
            if (info.timeInDistance(last, TIME_DISTANCE)) {
                list.add(info);
                checkReport1();
            } else {
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
    }

    private void report2() {
        //
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
            return Math.abs(this.time - info.time) < distance;
        }
    }
}
