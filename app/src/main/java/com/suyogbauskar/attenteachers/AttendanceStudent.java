package com.suyogbauskar.attenteachers;

import java.util.List;

public class AttendanceStudent {
    private String firstname, lastname;
    private int rollNo;
    private List<String> days;

    public AttendanceStudent(String firstname, String lastname, int rollNo, List<String> days) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.rollNo = rollNo;
        this.days = days;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public int getRollNo() {
        return rollNo;
    }

    public List<String> getDays() {
        return days;
    }
}
