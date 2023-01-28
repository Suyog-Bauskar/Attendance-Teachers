package com.suyogbauskar.attenteachers.pojos;

public class StudentData {
    private int rollNo, batch, semester, unitTest1Marks, unitTest2Marks;
    private long enrollNo;
    private String firstname, lastname, division;
    private boolean manual, microProject;

    public StudentData(int rollNo, int batch, int semester, long enrollNo, String firstname, String lastname, String division) {
        this.rollNo = rollNo;
        this.batch = batch;
        this.semester = semester;
        this.enrollNo = enrollNo;
        this.firstname = firstname;
        this.lastname = lastname;
        this.division = division;
    }

    public StudentData(int rollNo, String firstname, String lastname, long enrollNo, boolean manual, boolean microProject) {
        this.rollNo = rollNo;
        this.enrollNo = enrollNo;
        this.firstname = firstname;
        this.lastname = lastname;
        this.manual = manual;
        this.microProject = microProject;
    }

    public StudentData(int rollNo, int unitTest1Marks, int unitTest2Marks, String firstname, String lastname) {
        this.rollNo = rollNo;
        this.unitTest1Marks = unitTest1Marks;
        this.unitTest2Marks = unitTest2Marks;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public StudentData(int rollNo, String firstname, String lastname) {
        this.rollNo = rollNo;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public StudentData(int rollNo, int batch, int semester, long enrollNo, String division) {
        this.rollNo = rollNo;
        this.batch = batch;
        this.semester = semester;
        this.enrollNo = enrollNo;
        this.division = division;
    }

    public int getUnitTest1Marks() {
        return unitTest1Marks;
    }

    public int getUnitTest2Marks() {
        return unitTest2Marks;
    }

    public boolean isManual() {
        return manual;
    }

    public boolean isMicroProject() {
        return microProject;
    }

    public int getBatch() {
        return batch;
    }

    public String getDivision() {
        return division;
    }

    public int getRollNo() {
        return rollNo;
    }

    public long getEnrollNo() {
        return enrollNo;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public int getSemester() {
        return semester;
    }
}
