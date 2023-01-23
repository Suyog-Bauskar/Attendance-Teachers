package com.suyogbauskar.attenteachers.pojos;

public class Subject {
    private String shortName, Name, code;
    private int semester;

    public Subject(String shortName, String name, String code, int semester) {
        this.shortName = shortName;
        Name = name;
        this.code = code;
        this.semester = semester;
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
}
