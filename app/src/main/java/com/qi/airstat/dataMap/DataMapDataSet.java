package com.qi.airstat.dataMap;

/**
 * Created by JUMPSNACK on 8/8/2016.
 */
public class DataMapDataSet {
    private float aqiValue;
    private double temperature = -1f;
    private double co = -1f;
    private double so2 = -1f;
    private double no2 = -1f;
    private double o3 = -1f;
    private double pm = -1f;

    public DataMapDataSet() {
    }

    public DataMapDataSet(double temparature, double co, double so2, double no2, double o3, double pm) {
        this.temperature = temparature;
        this.co = co;
        this.so2 = so2;
        this.no2 = no2;
        this.o3 = o3;
        this.pm = pm;
    }

    public void dataReset(double temparature, double co, double so2, double no2, double o3, double pm) {
        this.temperature = temparature;
        this.co = co;
        this.so2 = so2;
        this.no2 = no2;
        this.o3 = o3;
        this.pm = pm;
    }

    public double getAqiValue() {
        int count = 0;
        double coResult, so2Result, no2Result, o3Result, pmResult;

        if ((coResult = getCoGrade(co)) != -1) count++;
        if ((so2Result = getSo2Grade(so2)) != -1) count++;
        if ((no2Result = getNo2Grade(no2)) != -1) count++;
        if ((o3Result = getO3Grade(o3)) != -1) count++;
        if ((pmResult = getPmGrade(pm)) != -1) count++;

        return (coResult + so2Result + no2Result + o3Result + pmResult) / count;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public double getCo() {
        return co;
    }

    public void setCo(float co) {
        this.co = co;
    }

    public double getSo2() {
        return so2;
    }

    public void setSo2(float so2) {
        this.so2 = so2;
    }

    public double getNo2() {
        return no2;
    }

    public void setNo2(float no2) {
        this.no2 = no2;
    }

    public double getO3() {
        return o3;
    }

    public void setO3(float o3) {
        this.o3 = o3;
    }

    public double getPm() {
        return pm;
    }

    public void setPm(float pm) {
        this.pm = pm;
    }

    private double getO3Grade(double o3) {
        if (0 <= o3 && o3 <= 54) {
            return (50 * o3 / 54);
        } else if (54 < o3 && o3 <= 70) {
            return (100 - 51) * (o3 - 55) / (70 - 55);
        } else if (70 < o3 && o3 <= 85) {
            return (150 - 101) * (o3 - 71) / (85 - 71);
        } else if (85 < o3 && o3 <= 105) {
            return (200 - 151) * (o3 - 86) / (105 - 86);
        } else if (105 < o3 && o3 <= 200) {
            return (300 - 201) * (o3 - 106) / (200 - 106);
        } else if (200 < o3 && o3 <= 504) {
            return (400 - 301) * (o3 - 405) / (504 - 405);
        } else if (504 < o3 && o3 <= 605) {
            return (500 - 401) * (o3 - 505) / (604 - 505);
        } else {
            return -1;
        }
    }

    private double getPmGrade(double pm) {
        if (0 <= pm && pm <= 12) {
            return (50 - 0) * (pm - 0) / (12 - 0);
        } else if (12 < pm && pm <= 35.4) {
            return (100 - 51) * (pm - 51) / (35.4 - 12.1);
        } else if (35.5 < pm && pm <= 55.4) {
            return (150 - 101) * (pm - 101) / (55.4 - 35.5);
        } else if (55.4 < pm && pm <= 150.4) {
            return (200 - 151) * (pm - 151) / (150.4 - 55.5);
        } else if (150.4 < pm && pm <= 250.4) {
            return (300 - 201) * (pm - 201) / (250.4 - 150.5);
        } else if (250.4 < pm && pm <= 350.4) {
            return (400 - 301) * (pm - 301) / (350.4 - 250.5);
        } else if (350.4 < pm && pm <= 500.4) {
            return (500 - 401) * (pm - 401) / (500.4 - 350.5);
        } else {
            return -1;
        }
    }

    private double getCoGrade(double co) {
        if (0 <= co && co <= 4.4) {
            return (50 - 1) * (co - 0) / (4.4 - 0);
        } else if (4.4 < co && co <= 9.4) {
            return (100 - 51) * (co - 4.5) / (9.4 - 4.5);
        } else if (9.4 < co && co <= 12.4) {
            return (150 - 101) * (co - 9.5) / (12.4 - 9.5);
        } else if (12.4 < co && co <= 15.4) {
            return (200 - 151) * (co - 12.5) / (15.4 - 12.5);
        } else if (15.4 < co && co <= 30.4) {
            return (300 - 201) * (co - 15.5) / (30.4 - 15.5);
        } else if (30.4 < co && co <= 40.4) {
            return (400 - 301) * (co - 30.5) / (40.4 - 30.5);
        } else if (40.4 < co && co <= 50.4) {
            return (500 - 401) * (co - 40.5) / (50.4 - 40.5);
        } else {
            return -1;
        }
    }

    private double getSo2Grade(double so2) {
        if (0 <= so2 && so2 <= 35) {
            return (50 - 1) * (so2 - 0) / (35 - 0);
        } else if (35 < so2 && so2 <= 75) {
            return (100 - 51) * (so2 - 36) / (75 - 36);
        } else if (75 < so2 && so2 <= 185) {
            return (150 - 101) * (so2 - 76) / (185 - 76);
        } else if (185 < so2 && so2 <= 304) {
            return (200 - 151) * (so2 - 186) / (304 - 186);
        } else if (304 < so2 && so2 <= 604) {
            return (300 - 201) * (so2 - 305) / (604 - 305);
        } else if (604 < so2 && so2 <= 804) {
            return (400 - 301) * (so2 - 605) / (804 - 605);
        } else if (804 < so2 && so2 <= 1004) {
            return (500 - 401) * (so2 - 805) / (1004 - 805);
        } else {
            return -1;
        }
    }

    private double getNo2Grade(double no2) {
        if (0 <= no2 && no2 <= 53) {
            return (50 - 1) * (no2 - 0) / (53 - 0);
        } else if (53 < no2 && no2 <= 100) {
            return (100 - 51) * (no2 - 54) / (100 - 54);
        } else if (100 < no2 && no2 <= 360) {
            return (150 - 101) * (no2 - 101) / (360 - 101);
        } else if (360 < no2 && no2 <= 649) {
            return (200 - 151) * (no2 - 361) / (649 - 361);
        } else if (649 < no2 && no2 <= 1249) {
            return (300 - 201) * (no2 - 650) / (1249 - 650);
        } else if (1249 < no2 && no2 <= 1649) {
            return (400 - 301) * (no2 - 605) / (1649 - 605);
        } else if (1649 < no2 && no2 <= 2049) {
            return (500 - 401) * (no2 - 1650) / (2049 - 1650);
        } else {
            return -1;
        }
    }
}