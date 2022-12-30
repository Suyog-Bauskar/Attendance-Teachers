package com.suyogbauskar.attenteachers.pojos;

public class SubjectInformation {
    private String subjectCode, subjectName, subjectShortName;

    public SubjectInformation(String subjectCode, String subjectName, String subjectShortName) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.subjectShortName = subjectShortName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectShortName() {
        return subjectShortName;
    }

    public void setSubjectShortName(String subjectShortName) {
        this.subjectShortName = subjectShortName;
    }
}
