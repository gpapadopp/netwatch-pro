package eu.gpapadop.netwatchpro.utils;

import android.content.Context;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import eu.gpapadop.netwatchpro.R;

public class DateTimeUtils {
    private Context parentContext;
    public DateTimeUtils(Context newParentContext){
        this.parentContext = newParentContext;
    }

    public String formatDateTime(LocalDateTime dateTimeToFormat){
        String dateString = "";
        if (dateTimeToFormat.getDayOfMonth() < 10){
            dateString += "0" + String.valueOf(dateTimeToFormat.getDayOfMonth());
        } else {
            dateString += String.valueOf(dateTimeToFormat.getDayOfMonth());
        }
        if (dateTimeToFormat.getMonthValue() < 10){
            dateString += "/0" + String.valueOf(dateTimeToFormat.getMonthValue());
        } else {
            dateString += "/" + String.valueOf(dateTimeToFormat.getMonthValue());
        }
        dateString += "/" + String.valueOf(dateTimeToFormat.getYear());

        if (dateTimeToFormat.getHour() < 10){
            dateString += " 0" + String.valueOf(dateTimeToFormat.getHour());
        } else {
            dateString += " " + String.valueOf(dateTimeToFormat.getHour());
        }
        if (dateTimeToFormat.getMinute() < 10){
            dateString += ":0" + String.valueOf(dateTimeToFormat.getMinute());
        } else {
            dateString += ":" + String.valueOf(dateTimeToFormat.getMinute());
        }
        if (dateTimeToFormat.getSecond() < 10){
            dateString += ":0" + String.valueOf(dateTimeToFormat.getSecond());
        } else {
            dateString += ":" + String.valueOf(dateTimeToFormat.getSecond());
        }
        return dateString;
    }

    public String calculateTimeAgo(String dateTimeString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);

        LocalDateTime currentDateTime = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, currentDateTime);

        long secondsAgo = duration.getSeconds();
        long minutesAgo = duration.toMinutes();
        long hoursAgo = duration.toHours();
        long daysAgo = duration.toDays();

        if (secondsAgo < 60) {
            return secondsAgo + " " + this.parentContext.getString(R.string.seconds_ago);
        } else if (minutesAgo < 60) {
            return minutesAgo + " " + this.parentContext.getString(R.string.minutes_ago);
        } else if (hoursAgo < 24) {
            return hoursAgo + " " + this.parentContext.getString(R.string.hours_ago);
        } else {
            return daysAgo + " " + this.parentContext.getString(R.string.days_ago);
        }
    }
}
