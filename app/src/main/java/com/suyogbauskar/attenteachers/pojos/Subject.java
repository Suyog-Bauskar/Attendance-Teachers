package com.suyogbauskar.attenteachers.pojos;

public class Subject {
    private String shortName, Name, code, teacherUID;
    private int semester;

    public Subject(String shortName, String name, String code, int semester) {
        this.shortName = shortName;
        Name = name;
        this.code = code;
        this.semester = semester;
    }

    public Subject(String shortName, String name, String code, int semester, String teacherUID) {
        this.shortName = shortName;
        Name = name;
        this.code = code;
        this.semester = semester;
        this.teacherUID = teacherUID;
    }

    public String getShortName() {
        return shortName;
    }

    public String getName() {
        return Name;
    }

    public String getCode() {
        return code;
    }

    public int getSemester() {
        return semester;
    }

    public String getTeacherUID() {
        return teacherUID;
    }
}
