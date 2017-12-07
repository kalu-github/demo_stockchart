package com.lib.stockchart.entry;

import android.util.SparseArray;

import java.util.List;

/**
 * description: 信息集合
 * created by kalu on 2017/11/11 19:42
 */
public class EntryManager {

    //    // 图标边框和信息的内边距
//    private float boardPadding = 10f;
    // 上部权重
    private int weightTop = 5;
    // 下部权重
    private int weightDown = 2;
    // 每个点之间的空隙
    private float pointSpace = 10f;
    // 每个点的宽度
    private int pointWidth = 10;

    public void setPointWidth(int pointWidth) {
        this.pointWidth = pointWidth;
    }

    public int getPointWidth() {
        return pointWidth;
    }

    private volatile int pointHighlight = -1;
    // 高亮点, X坐标索引(第一个上一次, 第二个最新的)
    private volatile float[] pointHighlightX = new float[]{-1, -1};
    // 高亮点, Y坐标索引(第一个上一次, 第二个最新的)
    private volatile float[] pointHighlightY = new float[]{-1, -1};

    public float getPointSpace() {
        return pointSpace;
    }

    public void setPointSpace(float pointSpace) {
        this.pointSpace = pointSpace;
    }

//    public float getBoardPadding() {
//        return boardPadding;
//    }
//
//    public void setBoardPadding(float boardPadding) {
//        this.boardPadding = boardPadding;
//    }

    public int getPointHighlight() {
        return pointHighlight;
    }

    public void setPointHighlight(int pointHighlight) {
        this.pointHighlight = pointHighlight;
    }

    public float[] getPointHighlightX() {
        return pointHighlightX;
    }

    public void setPointHighlightX(boolean clear, float x) {

        if (clear) {
            pointHighlightX[0] = x;
            pointHighlightX[1] = x;
        } else {
            float temp = pointHighlightX[1];
            pointHighlightX[0] = temp;
            pointHighlightX[1] = x;
        }
    }

    public float[] getPointHighlightY() {
        return pointHighlightY;
    }

    public void setPointHighlightY(float oldX, float newX) {
        pointHighlightY[0] = oldX != -2f ? oldX : pointHighlightY[1];
        pointHighlightY[1] = newX;
    }

    public boolean isHighlightMove() {

        float x1 = pointHighlightX[0];
        float x2 = pointHighlightX[1];
        // Log.e("ooooooo1", "x1 = " + x1 + ", x2 = " + x2);
        return x1 != x2;
    }

    public int getWeightTop() {
        return weightTop;
    }

    public void setWeightTop(int weightTop) {
        this.weightTop = weightTop;
    }

    public int getWeightDown() {
        return weightDown;
    }

    public void setWeightDown(int weightDown) {
        this.weightDown = weightDown;
    }

    private EntryManager() {
    }

    public static EntryManager getInstance() {
        return EntryManager.SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static EntryManager instance = new EntryManager();
    }

    // 数据集合
    private List<Entry> entries;

    public List<Entry> getEntryList() {
        return entries;
    }

    public int getIndexMax() {
        if (null == entries) return 0;
        else return entries.size() - 1;
    }

    // 重置数据
    public void resetData() {
        pointHighlightX[0] = -1;
        pointHighlightX[1] = -1;
        pointHighlightY[0] = -1;
        pointHighlightY[1] = -1;
        pointHighlight = -1;
        entries = null;
    }

    // 添加数据
    public void addData(List<Entry> entries) {

        if (null != this.entries) {
            this.entries.clear();
        }

        this.entries = entries;

        // 计算 MA MACD BOLL RSI KDJ 指标
        computeMA();
        computeMACD();
        computeBOLL();
        computeRSI();
        computeKDJ();
    }

    public float calculatePriceMax(int start, int end) {

        float temp = 0;

        for (int i = start; i <= end; i++) {
            Entry entry = entries.get(i);
            final float ma5 = entry.getMa5();
            final float ma10 = entry.getMa10();
            final float ma20 = entry.getMa20();
            final float high = entry.getHigh();
            final float max1 = Math.max(ma5, ma10);
            final float max2 = Math.max(max1, ma20);
            final float max3 = Math.max(max2, high);
            if (i == start) {
                temp = max3;
            } else {
                temp = Math.max(temp, max3);
            }
        }
        return temp;
    }

    public float calculatePriceMin(int start, int end) {

        float temp = 0;

        for (int i = start; i <= end; i++) {

            Entry entry = entries.get(i);
            if (null == entry) continue;

            final float ma5 = entry.getMa5();
            final float ma10 = entry.getMa10();
            final float ma20 = entry.getMa20();
            final float low = entry.getLow();
            final float min1 = Math.min(ma5, ma10);
            final float min2 = Math.min(min1, ma20);
            final float min3 = Math.min(min2, low);
            if (i == start) {
                temp = min3;
            } else {
                temp = Math.min(temp, min3);
            }
        }
        return temp;
    }

    public float calculateTurnoverMax(int start, int end) {

        float temp = 0;

        for (int i = start; i <= end; i++) {
            Entry entry = entries.get(i);
            final float ma5 = entry.getVolumeMa5();
            final float ma10 = entry.getVolumeMa10();
            final float high = entry.getVolume();
            final float max1 = Math.max(ma5, ma10);
            final float max2 = Math.max(max1, high);
            if (i == start) {
                temp = max2;
            } else {
                temp = Math.max(temp, max2);
            }
        }
        return temp;
    }

    /**
     * 计算 MA
     */
    private void computeMA() {
        float ma5 = 0;
        float ma10 = 0;
        float ma20 = 0;
        float volumeMa5 = 0;
        float volumeMa10 = 0;

        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);

            ma5 += entry.getClose();
            ma10 += entry.getClose();
            ma20 += entry.getClose();

            volumeMa5 += entry.getVolume();
            volumeMa10 += entry.getVolume();

            if (i >= 5) {
                ma5 -= entries.get(i - 5).getClose();
                entry.setMa5(ma5 / 5f);

                volumeMa5 -= entries.get(i - 5).getVolume();
                entry.setVolumeMa5(volumeMa5 / 5f);
            } else {
                entry.setMa5(ma5 / (i + 1f));

                entry.setVolumeMa5(volumeMa5 / (i + 1f));
            }

            if (i >= 10) {
                ma10 -= entries.get(i - 10).getClose();
                entry.setMa10(ma10 / 10f);

                volumeMa10 -= entries.get(i - 10).getVolume();
                entry.setVolumeMa10(volumeMa10 / 5f);
            } else {
                entry.setMa10(ma10 / (i + 1f));

                entry.setVolumeMa10(volumeMa10 / (i + 1f));
            }

            if (i >= 20) {
                ma20 -= entries.get(i - 20).getClose();
                entry.setMa20(ma20 / 20f);
            } else {
                entry.setMa20(ma20 / (i + 1f));
            }
        }
    }

    /**
     * 计算 MACD
     */
    private void computeMACD() {
        float ema12 = 0;
        float ema26 = 0;
        float diff = 0;
        float dea = 0;
        float macd = 0;

        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);

            if (i == 0) {
                ema12 = entry.getClose();
                ema26 = entry.getClose();
            } else {
                // EMA（12） = 前一日EMA（12） X 11/13 + 今日收盘价 X 2/13
                // EMA（26） = 前一日EMA（26） X 25/27 + 今日收盘价 X 2/27
                ema12 = ema12 * 11f / 13f + entry.getClose() * 2f / 13f;
                ema26 = ema26 * 25f / 27f + entry.getClose() * 2f / 27f;
            }

            // DIF = EMA（12） - EMA（26） 。
            // 今日DEA = （前一日DEA X 8/10 + 今日DIF X 2/10）
            // 用（DIF-DEA）*2 即为 MACD 柱状图。
            diff = ema12 - ema26;
            dea = dea * 8f / 10f + diff * 2f / 10f;
            macd = (diff - dea) * 2f;

            entry.setDiff(diff);
            entry.setDea(dea);
            entry.setMacd(macd);
        }
    }

    /**
     * 计算 BOLL 需要在计算 MA 之后进行
     */
    private void computeBOLL() {
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);

            if (i == 0) {
                entry.setMb(entry.getClose());
                entry.setUp(Float.NaN);
                entry.setDn(Float.NaN);
            } else {
                int n = 20;
                if (i < 20) {
                    n = i + 1;
                }

                float md = 0;
                for (int j = i - n + 1; j <= i; j++) {
                    float c = entries.get(j).getClose();
                    float m = entry.getMa20();
                    float value = c - m;
                    md += value * value;
                }

                md = md / (n - 1);
                md = (float) Math.sqrt(md);

                entry.setMb(entry.getMa20());
                entry.setUp(entry.getMb() + 2f * md);
                entry.setDn(entry.getMb() - 2f * md);
            }
        }
    }

    /**
     * 计算 RSI
     */
    private void computeRSI() {
        float rsi1 = 0;
        float rsi2 = 0;
        float rsi3 = 0;
        float rsi1ABSEma = 0;
        float rsi2ABSEma = 0;
        float rsi3ABSEma = 0;
        float rsi1MaxEma = 0;
        float rsi2MaxEma = 0;
        float rsi3MaxEma = 0;

        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);

            if (i == 0) {
                rsi1 = 0;
                rsi2 = 0;
                rsi3 = 0;
                rsi1ABSEma = 0;
                rsi2ABSEma = 0;
                rsi3ABSEma = 0;
                rsi1MaxEma = 0;
                rsi2MaxEma = 0;
                rsi3MaxEma = 0;
            } else {
                float Rmax = Math.max(0, entry.getClose() - entries.get(i - 1).getClose());
                float RAbs = Math.abs(entry.getClose() - entries.get(i - 1).getClose());

                rsi1MaxEma = (Rmax + (6f - 1) * rsi1MaxEma) / 6f;
                rsi1ABSEma = (RAbs + (6f - 1) * rsi1ABSEma) / 6f;

                rsi2MaxEma = (Rmax + (12f - 1) * rsi2MaxEma) / 12f;
                rsi2ABSEma = (RAbs + (12f - 1) * rsi2ABSEma) / 12f;

                rsi3MaxEma = (Rmax + (24f - 1) * rsi3MaxEma) / 24f;
                rsi3ABSEma = (RAbs + (24f - 1) * rsi3ABSEma) / 24f;

                rsi1 = (rsi1MaxEma / rsi1ABSEma) * 100;
                rsi2 = (rsi2MaxEma / rsi2ABSEma) * 100;
                rsi3 = (rsi3MaxEma / rsi3ABSEma) * 100;
            }

            entry.setRsi1(rsi1);
            entry.setRsi2(rsi2);
            entry.setRsi3(rsi3);
        }
    }

    /**
     * 计算 KDJ
     */
    private void computeKDJ() {
        float k = 0;
        float d = 0;

        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);

            int startIndex = i - 8;
            if (startIndex < 0) {
                startIndex = 0;
            }

            float max9 = Float.MIN_VALUE;
            float min9 = Float.MAX_VALUE;
            for (int index = startIndex; index <= i; index++) {
                max9 = Math.max(max9, entries.get(index).getHigh());
                min9 = Math.min(min9, entries.get(index).getLow());
            }

            float rsv = 100f * (entry.getClose() - min9) / (max9 - min9);
            if (i == 0) {
                k = rsv;
                d = rsv;
            } else {
                k = (rsv + 2f * k) / 3f;
                d = (k + 2f * d) / 3f;
            }

            entry.setK(k);
            entry.setD(d);
            entry.setJ(3f * k - 2 * d);
        }
    }
}