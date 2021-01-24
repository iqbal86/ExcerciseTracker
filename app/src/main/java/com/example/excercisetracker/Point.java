package com.example.excercisetracker;

public class Point {
    private long xAxis;

    private long yAxis;

    public Point() {
    }

    public Point(long xAxis, long yAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }

    public long getxAxis() {
        return xAxis;
    }

    public void setxAxis(long xAxis) {
        this.xAxis = xAxis;
    }

    public long getyAxis() {
        return yAxis;
    }

    public void setyAxis(long yAxis) {
        this.yAxis = yAxis;
    }
}