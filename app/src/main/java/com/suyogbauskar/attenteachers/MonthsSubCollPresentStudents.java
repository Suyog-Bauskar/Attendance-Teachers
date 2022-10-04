package com.suyogbauskar.attenteachers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MonthsSubCollPresentStudents {
    private String monthName;
    private List<Map<String, List<StudentDataAttendance>>> dayNameAndPresentStudents = new ArrayList<>();

    public MonthsSubCollPresentStudents() {}

    public MonthsSubCollPresentStudents(String monthName, List<Map<String, List<StudentDataAttendance>>> dayNameAndPresentStudents) {
        this.monthName = monthName;
        this.dayNameAndPresentStudents = dayNameAndPresentStudents;
    }

    public String getMonthName() {
        return monthName;
    }

    public List<Map<String, List<StudentDataAttendance>>> getDayNameAndPresentStudents() {
        return dayNameAndPresentStudents;
    }
}
