package com.repo_gestion_tr.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateFormat {
    private DateFormat() {
        
    }

    public static String formatDate(LocalDate date) {

        if (date == null) {
            return "Date non définie";
        }
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Date et heure non définies";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
}
