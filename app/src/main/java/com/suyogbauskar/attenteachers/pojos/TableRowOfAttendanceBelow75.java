package com.suyogbauskar.attenteachers.pojos;

public class TableRowOfAttendanceBelow75 {
    private String division, batch, name;
    private int rollNo;
    private float percentage;

    public TableRowOfAttendanceBelow75(String division, String batch, String name, int rollNo, float percentage) {
        this.division = division;
        this.batch = batch;
        this.name = name;
        this.rollNo = rollNo;
        this.percentage = percentage;
    }

    public String getDivision() {
        return division;
    }

    public String getBatch() {
        return batch;
    }

    public String getName() {
        return name;
    }

    public int getRollNo() {
        return rollNo;
    }

    public float getPercentage() {
        return percentage;
    }
}
