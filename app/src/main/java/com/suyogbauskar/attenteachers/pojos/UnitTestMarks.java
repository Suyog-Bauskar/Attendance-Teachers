package com.suyogbauskar.attenteachers.pojos;

public class UnitTestMarks {
    String rollNo, unitTest1Marks, unitTest2Marks;

    public UnitTestMarks(String rollNo, String unitTest1Marks, String unitTest2Marks) {
        this.rollNo = rollNo;
        this.unitTest1Marks = unitTest1Marks;
        this.unitTest2Marks = unitTest2Marks;
    }

    public String getRollNo() {
        return rollNo;
    }

    public String getUnitTest1Marks() {
        return unitTest1Marks;
    }

    public String getUnitTest2Marks() {
        return unitTest2Marks;
    }
}
