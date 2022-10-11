package com.suyogbauskar.attenteachers.pojos;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Attendance {
    public Map<String, Month> months = new HashMap<>();

    @JsonAnySetter
    public void setMonths(String name, Month value) {
        this.months.put(name, value);
    }

    public Map<String,List<Map<String, Object>>> getAll(){
        Map<String,List<Map<String, Object>>> monthlyAttendance = new HashMap<>();
        for(Map.Entry<String, Month> entry: months.entrySet()){
            List<Map<String, Object>> s = entry.getValue().entries.values().stream().map(e-> e.entries).collect(Collectors.toList());
            monthlyAttendance.put(entry.getKey(),s);
        }
        return monthlyAttendance;
    }

    public Map<String,Set<String>> getColumns(){
        Map<String,Set<String>> monthlyColumns = new HashMap<>();
        for(Map.Entry<String, Month> entry: months.entrySet()){
            monthlyColumns.put(entry.getKey(),entry.getValue().columns);
        }
        return monthlyColumns;
    }

    public static class Month {
        public Map<String, AttendanceEntry> entries = new HashMap<>();
        public Set<String> columns = new HashSet<>();

        @JsonAnySetter
        public void setAttendanceEntry(String name, AttendanceEntry value) {
            this.entries.put(name, value);
            this.columns.addAll(value.columns);
        }
    }

    public static class AttendanceEntry {
        public Map<String, Object> entries = new HashMap<>();
        public Set<String> columns = new HashSet<>();

        @JsonAnySetter
        public void setEntries(String name, Object value) {
            columns.add(name);
            this.entries.put(name, value);
        }
    }
}
