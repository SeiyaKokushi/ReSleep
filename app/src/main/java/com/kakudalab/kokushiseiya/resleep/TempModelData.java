package com.kakudalab.kokushiseiya.resleep;

/**
 * Created by kokushiseiya on 15/12/23.
 */
public class TempModelData {
    private double peakPowerMax_t;
    private double peakPowerMin_t;
    private double powerRangeMax_t;
    private double powerRangeMin_t;
    private double[] deviation_t;
    private double standardDeviation_t;

    public TempModelData(String act){
        if (act.equals("exercise")) {
            peakPowerMax_t = 46.667128;
            peakPowerMin_t = 24.583128;
            powerRangeMax_t = 18.817756;
            powerRangeMin_t = -4.637834;
            deviation_t = new double[]{-7.089961,
                    28.398225,
                    -0.775372,
                    -3.952516,
                    -5.261462,
                    -5.491619,
                    -5.827295};
            standardDeviation_t = 11.739728;


        } else if (act.equals("eat")) {
            peakPowerMax_t = 10.221194;
            peakPowerMin_t = 4.573257;
            powerRangeMax_t = 4.117736;
            powerRangeMin_t = -0.468405;
            deviation_t = new double[]{-1.824665,
                    5.438109,
                    0.269820,
                    -0.664483,
                    -1.004523,
                    -1.057925,
                    -1.156333};
            standardDeviation_t = 2.295404;

        } else if (act.equals("nap")) {
            peakPowerMax_t = 50.219768;
            peakPowerMin_t = 8.447498;
            powerRangeMax_t = 16.927019;
            powerRangeMin_t = 0.519448;
            deviation_t = new double[]{-8.723234,
                    17.173439,
                    6.090763,
                    -0.778027,
                    -4.761539,
                    -4.213986,
                    -4.787416};
            standardDeviation_t = 8.212132;

        } else if (act.equals("alcohol")) {
            peakPowerMax_t = 20.340514;
            peakPowerMin_t = -11.748009;
            powerRangeMax_t = 2.580678;
            powerRangeMin_t = -0.371597;
            deviation_t = new double[]{-1.104541,
                    3.191712,
                    1.086935,
                    -0.474776,
                    -0.735974,
                    -1.004207,
                    -0.959149};
            standardDeviation_t = 1.477640;

        } else if (act.equals("cafe")) {
                peakPowerMax_t = 35.035694;
                peakPowerMin_t = 9.235286;
                powerRangeMax_t = 14.091200;
                powerRangeMin_t = 1.082965;
                deviation_t = new double[]{-7.587083,
                        14.548407,
                        2.090903,
                        -1.328101,
                        -1.839140,
                        -2.362376,
                        -3.522611};
                standardDeviation_t = 6.510736;
        } else if (act.equals("tabacco")) {
            peakPowerMax_t = 27.008625;
            peakPowerMin_t = 12.807773;
            powerRangeMax_t = 11.315406;
            powerRangeMin_t = -1.031752;
            deviation_t = new double[]{-5.141827,
                    14.632087,
                    0.594748,
                    -1.988724,
                    -2.105200,
                    -2.759676,
                    -3.231408};
            standardDeviation_t = 6.179861;

        }
    }

    public double getPeakPowerMax_t(){
        return peakPowerMax_t;
    }

    public double getPeakPowerMin_t(){
        return peakPowerMin_t;
    }

    public double getPowerRangeMax_t(){
        return powerRangeMax_t;
    }

    public double getPowerRangeMin_t(){
        return powerRangeMin_t;
    }

    public double getStandardDeviation_t(){
        return standardDeviation_t;
    }

    public double[] getDeviation_t(){
        return deviation_t;
    }

}
