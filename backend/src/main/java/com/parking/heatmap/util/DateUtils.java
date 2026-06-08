package com.parking.heatmap.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DateUtils {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    public static boolean isPeakHour(int hour) {
        return (hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19);
    }

    public static List<LocalDate> getLastNDays(int n) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = n - 1; i >= 0; i--) {
            dates.add(today.minusDays(i));
        }
        return dates;
    }

    public static List<LocalDate> getNextNDays(int n) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < n; i++) {
            dates.add(today.plusDays(i));
        }
        return dates;
    }

    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }
}
