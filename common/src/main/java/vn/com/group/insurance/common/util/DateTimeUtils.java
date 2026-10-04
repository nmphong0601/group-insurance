package vn.com.group.insurance.common.util;

import vn.com.group.insurance.common.constant.CommonConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility methods for date and time manipulation.
 */
public final class DateTimeUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(CommonConstants.DATE_TIME_FORMAT);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(CommonConstants.DATE_FORMAT);

    private DateTimeUtils() {
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    public static LocalDateTime parseDateTime(String text) {
        return text != null && !text.isBlank() ? LocalDateTime.parse(text, DATE_TIME_FORMATTER) : null;
    }

    public static LocalDate parseDate(String text) {
        return text != null && !text.isBlank() ? LocalDate.parse(text, DATE_FORMATTER) : null;
    }
}
