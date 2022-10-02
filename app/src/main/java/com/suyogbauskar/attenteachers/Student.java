package com.suyogbauskar.attenteachers;

public class Student {
    private String firstname, lastname, enrollNo, uid;
    private int rollNo;

    public Student() {}

    public Student(String firstname, String lastname, String enrollNo, String uid, int rollNo) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.enrollNo = enrollNo;
        this.uid = uid;
        this.rollNo = rollNo;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEnrollNo() {
        return enrollNo;
    }

    public String getUid() {
        return uid;
    }

    public int getRollNo() {
        return rollNo;
    }
}
