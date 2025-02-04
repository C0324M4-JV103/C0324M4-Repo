package com.c0324.casestudym5.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;


public class DateTimeUtil {

    public static String getTimeDifference(Date createdAt) {
        LocalDateTime createdDateTime = createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime currentDateTime = LocalDateTime.now();

        long diffInNano = Duration.between(createdDateTime, currentDateTime).toNanos();
        long diffInSeconds = Duration.between(createdDateTime, currentDateTime).getSeconds();
        long diffInMinutes = Duration.between(createdDateTime, currentDateTime).toMinutes();
        long diffInHours = Duration.between(createdDateTime, currentDateTime).toHours();
        long diffInDays = Duration.between(createdDateTime, currentDateTime).toDays();
        long diffInMonths = diffInDays / 30;
        long diffInYears = diffInDays / 365;

        if (diffInNano > 0 && diffInSeconds <= 0) {
            return "Vừa xong";
        } else if (diffInSeconds < 60) {
            return diffInSeconds + " giây trước";
        } else if (diffInMinutes < 60) {
            return diffInMinutes + " phút trước";
        } else if (diffInHours < 24) {
            return diffInHours + " giờ trước";
        } else if (diffInDays < 30) {
            return diffInDays + " ngày trước";
        } else if (diffInMonths < 12) {
            return diffInMonths + " tháng trước";
        } else {
            return diffInYears + " năm trước";
        }
    }

    public static int calculateAge(Date birthDate) {
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);
        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    public static LocalDate convertDateToLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDate();
    }

}
