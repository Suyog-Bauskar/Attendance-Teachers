package com.suyogbauskar.attenteachers;

public class StudentDataAttendance {
    private int rollNo;
    private String firstname, lastname;

    public StudentDataAttendance() {}

    public StudentDataAttendance(int rollNo, String firstname, String lastname) {
        this.rollNo = rollNo;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public int getRollNo() {
        return rollNo;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }
}

