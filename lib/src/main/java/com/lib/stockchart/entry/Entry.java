package com.lib.stockchart.entry;

/**
 * description: 所有指标数据
 * created by kalu on 2017/11/9 19:57
 */
public class Entry {

    private float openReal; // 开盘价_坐标值
    private float highReal; // 最高价_坐标值
    private float lowReal; // 最低价_坐标值
    private float closeReal; // 收盘价_坐标值
    private float ma5Real;
    private float ma10Real;
    private float ma20Real;
    private float volumeMa5Real;
    private float volumeMa10Real;
    private float volumeReal; // 量_坐标值
    private float xLabelReal; // X 轴标签

    public float getVolumeMa5Real() {
        return volumeMa5Real;
    }

    public void setVolumeMa5Real(float volumeMa5Real) {
        this.volumeMa5Real = volumeMa5Real;
    }

    public float getVolumeMa10Real() {
        return volumeMa10Real;
    }

    public void setVolumeMa10Real(float volumeMa10Real) {
        this.volumeMa10Real = volumeMa10Real;
    }

    public float getMa5Real() {
        return ma5Real;
    }

    public void setMa5Real(float ma5Real) {
        this.ma5Real = ma5Real;
    }

    public float getMa10Real() {
        return ma10Real;
    }

    public void setMa10Real(float ma10Real) {
        this.ma10Real = ma10Real;
    }

    public float getMa20Real() {
        return ma20Real;
    }

    public void setMa20Real(float ma20Real) {
        this.ma20Real = ma20Real;
    }

    public float getOpenReal() {
        return openReal;
    }

    public void setOpenReal(float openReal) {
        this.openReal = openReal;
    }

    public float getHighReal() {
        return highReal;
    }

    public void setHighReal(float highReal) {
        this.highReal = highReal;
    }

    public float getLowReal() {
        return lowReal;
    }

    public void setLowReal(float lowReal) {
        this.lowReal = lowReal;
    }

    public float getCloseReal() {
        return closeReal;
    }

    public void setCloseReal(float closeReal) {
        this.closeReal = closeReal;
    }

    public float getVolumeReal() {
        return volumeReal;
    }

    public void setVolumeReal(float volumeReal) {
        this.volumeReal = volumeReal;
    }

    public float getxLabelReal() {
        return xLabelReal;
    }

    public void setxLabelReal(float xLabelReal) {
        this.xLabelReal = xLabelReal;
    }

    /***************************************************************************************/

    // 初始需全部赋值的属性
    private final float open; // 开盘价
    private final float high; // 最高价
    private final float low; // 最低价
    private final float close; // 收盘价
    private final int volume; // 量
    private String xLabel; // X 轴标签

    // 量的5日平均和10日平均
    private float volumeMa5;
    private float volumeMa10;

    // MA 指标的三个属性
    private float ma5;
    private float ma10;
    private float ma20;

    // MACD 指标的三个属性
    private float dea;
    private float diff;
    private float macd;

    // KDJ 指标的三个属性
    private float k;
    private float d;
    private float j;

    // RSI 指标的三个属性
    private float rsi1;
    private float rsi2;
    private float rsi3;

    // BOLL 指标的三个属性
    private float up; // 上轨线
    private float mb; // 中轨线
    private float dn; // 下轨线

    /**
     * 自定义分时图用的数据
     *
     * @param close  收盘价
     * @param volume 量
     * @param xLabel X 轴标签
     */
    public Entry(float close, int volume, String xLabel) {
        this.open = 0;
        this.high = 0;
        this.low = 0;
        this.close = close;
        this.volume = volume;
        this.xLabel = xLabel;
    }

    /**
     * 自定义 K 线图用的数据
     *
     * @param open   开盘价
     * @param high   最高价
     * @param low    最低价
     * @param close  收盘价
     * @param volume 量
     * @param xLabel X 轴标签
     */
    public Entry(float open, float high, float low, float close, int volume, String xLabel) {
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.xLabel = xLabel;
    }

    public float getOpen() {
        return open;
    }

    public float getHigh() {
        return high;
    }

    public float getLow() {
        return low;
    }

    public float getClose() {
        return close;
    }

    public int getVolume() {
        return volume;
    }

    public String getXLabel() {
        return xLabel;
    }

    public void setXLabel(String xLabel) {
        this.xLabel = xLabel;
    }

    public float getMa5() {
        return ma5;
    }

    public void setMa5(float ma5) {
        this.ma5 = ma5;
    }

    public float getMa10() {
        return ma10;
    }

    public void setMa10(float ma10) {
        this.ma10 = ma10;
    }

    public float getMa20() {
        return ma20;
    }

    public void setMa20(float ma20) {
        this.ma20 = ma20;
    }

    public float getVolumeMa5() {
        return volumeMa5;
    }

    public void setVolumeMa5(float volumeMa5) {
        this.volumeMa5 = volumeMa5;
    }

    public float getVolumeMa10() {
        return volumeMa10;
    }

    public void setVolumeMa10(float volumeMa10) {
        this.volumeMa10 = volumeMa10;
    }

    public float getDea() {
        return dea;
    }

    public void setDea(float dea) {
        this.dea = dea;
    }

    public float getDiff() {
        return diff;
    }

    public void setDiff(float diff) {
        this.diff = diff;
    }

    public float getMacd() {
        return macd;
    }

    public void setMacd(float macd) {
        this.macd = macd;
    }

    public float getK() {
        return k;
    }

    public void setK(float k) {
        this.k = k;
    }

    public float getD() {
        return d;
    }

    public void setD(float d) {
        this.d = d;
    }

    public float getJ() {
        return j;
    }

    public void setJ(float j) {
        this.j = j;
    }

    public float getRsi1() {
        return rsi1;
    }

    public void setRsi1(float rsi1) {
        this.rsi1 = rsi1;
    }

    public float getRsi2() {
        return rsi2;
    }

    public void setRsi2(float rsi2) {
        this.rsi2 = rsi2;
    }

    public float getRsi3() {
        return rsi3;
    }

    public void setRsi3(float rsi3) {
        this.rsi3 = rsi3;
    }

    public float getUp() {
        return up;
    }

    public void setUp(float up) {
        this.up = up;
    }

    public float getMb() {
        return mb;
    }

    public void setMb(float mb) {
        this.mb = mb;
    }

    public float getDn() {
        return dn;
    }

    public void setDn(float dn) {
        this.dn = dn;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                '}';
    }
}
