package com.suyogbauskar.attenteachers.pojos;

public class TableRowOfModifySubjects {
    private String teacherID, teacherName, subjectShortName, subjectName, subjectCode;
    private int semester;
    private Subject subject;

    public TableRowOfModifySubjects(String teacherID, String teacherName, String subjectShortName, String subjectName, String subjectCode, int semester, Subject subject) {
        this.teacherID = teacherID;
        this.teacherName = teacherName;
        this.subjectShortName = subjectShortName;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.semester = semester;
        this.subject = subject;
    }

    public String getTeacherID() {
        return teacherID;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getSubjectShortName() {
        return subjectShortName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public int getSemester() {
        return semester;
    }

    public Subject getSubject() {
        return subject;
    }
}
