package com.suyogbauskar.attenteachers;

public class Student {
    private String firstname, lastname;
    private int rollNo;

    public Student(String firstname, String lastname, int rollNo) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.rollNo = rollNo;
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
}
