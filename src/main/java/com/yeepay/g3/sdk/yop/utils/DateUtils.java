package com.yeepay.g3.sdk.yop.utils;

import com.yeepay.g3.sdk.yop.exception.YopClientException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.tz.FixedDateTimeZone;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class DateUtils {

    private DateUtils() {
        // do nothing
    }

    private static final DateTimeZone GMT = new FixedDateTimeZone("GMT", "GMT", 0, 0);
    private static final long MILLI_SECONDS_OF_365_DAYS = 365L * 24 * 60 * 60 * 1000;

    private static final int AWS_DATE_MILLI_SECOND_PRECISION = 3;

    /**
     * ISO 8601 format
     */
    protected static final DateTimeFormatter iso8601DateFormat =
            ISODateTimeFormat.dateTime().withZone(GMT);

    /**
     * Alternate ISO 8601 format without fractional seconds
     */
    protected static final DateTimeFormatter alternateIso8601DateFormat =
            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(GMT);

    /**
     * RFC 822 format
     */
    protected static final DateTimeFormatter rfc822DateFormat =
            DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
                    .withLocale(Locale.US)
                    .withZone(GMT);

    private static final DateTimeFormatter compressedIso8601DateFormat = DateTimeFormat
            .forPattern("yyyyMMdd").withZoneUTC();

    /**
     * This is another ISO 8601 format that's used in clock skew error response
     */
    protected static final DateTimeFormatter compressedIso8601TimeFormat =
            DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss'Z'")
                    .withZone(GMT);

    /**
     * Parses the specified date string as an ISO 8601 date and returns the Date
     * object.
     *
     * @param dateString The date string to parse.
     * @return The parsed Date object.
     */
    public static Date parseISO8601Date(String dateString) {
        try {
            return doParseISO8601Date(dateString);
        } catch (RuntimeException ex) {
            throw handleException(ex);
        }
    }

    static Date doParseISO8601Date(final String dateStringOrig) {
        String dateString = dateStringOrig;

        // For EC2 Spot Fleet.
        if (dateString.endsWith("+0000")) {
            dateString = dateString
                    .substring(0, dateString.length() - 5)
                    .concat("Z");
        }

        // https://github.com/aws/aws-sdk-java/issues/233
        String temp = tempDateStringForJodaTime(dateString);
        try {
            if (temp.equals(dateString)) {
                // Normal case: nothing special here
                return new Date(iso8601DateFormat.parseMillis(dateString));
            }
            // Handling edge case:
            // Joda-time can only handle up to year 292278993 but we are given
            // 292278994;  So we parse the date string by first adjusting
            // the year to 292278993. Then we add 1 year back afterwards.
            final long milliLess365Days = iso8601DateFormat.parseMillis(temp);
            final long milli = milliLess365Days + MILLI_SECONDS_OF_365_DAYS;
            if (milli < 0) { // overflow!
                // re-parse the original date string using JodaTime so as to
                // throw  an exception with a consistent message
                return new Date(iso8601DateFormat.parseMillis(dateString));
            }
            return new Date(milli);
        } catch (IllegalArgumentException e) {
            try {
                return new Date(alternateIso8601DateFormat.parseMillis(dateString));
                // If the first ISO 8601 parser didn't work, try the alternate
                // version which doesn't include fractional seconds
            } catch (Exception oops) {
                // no the alternative route doesn't work; let's bubble up the original exception
                throw e;
            }
        }
    }

    /**
     * Returns a date string with the prefix temporarily substituted, if
     * applicable, so that JodaTime can handle it.  Otherwise, if not applicable,
     * the original date string is returned.
     * <p>
     * See https://github.com/aws/aws-sdk-java/issues/233
     */
    private static String tempDateStringForJodaTime(String dateString) {
        final String fromPrefix = "292278994-";
        final String toPrefix = "292278993-";
        return dateString.startsWith(fromPrefix)
                ? toPrefix + dateString.substring(fromPrefix.length())
                : dateString;
    }

    /**
     * Returns the original runtime exception iff the joda-time being used
     * at runtime behaves as expected.
     *
     * @throws IllegalStateException if the joda-time being used at runtime
     *                               doens't appear to be of the right version.
     */
    private static <E extends RuntimeException> E handleException(E ex) {
        if (JodaTime.hasExpectedBehavior())
            return ex;
        throw new IllegalStateException("Joda-time 2.2 or later version is required, but found version: " + JodaTime.getVersion(), ex);
    }

    /**
     * Formats the specified date as an ISO 8601 string.
     *
     * @param date The date to format.
     * @return The ISO 8601 string representing the specified date.
     */
    public static String formatISO8601Date(Date date) {
        try {
            return iso8601DateFormat.print(date.getTime());
        } catch (RuntimeException ex) {
            throw handleException(ex);
        }
    }

    /**
     * Formats the specified date as an ISO 8601 string.
     *
     * @param date the date to format
     * @return the ISO-8601 string representing the specified date
     */
    public static String formatISO8601Date(DateTime date) {
        try {
            return iso8601DateFormat.print(date);
        } catch (RuntimeException ex) {
            throw handleException(ex);
        }
    }

    /**
     * Parses the specified date string as an RFC 822 date and returns the Date
     * object.
     *
     * @param dateString The date string to parse.
     * @return The parsed Date object.
     */
    public static Date parseRFC822Date(String dateString) {
        if (dateString == null) {
            return null;
        }
        try {
            return new Date(rfc822DateFormat.parseMillis(dateString));
        } catch (RuntimeException ex) {
            throw handleException(ex);
        }
    }

    /**
     * Formats the specified date as an RFC 822 string.
     *
     * @param date The date to format.
     * @return The RFC 822 string representing the specified date.
     */
    public static String formatRFC822Date(Date date) {
        try {
            return rfc822DateFormat.print(date.getTime());
        } catch (RuntimeException ex) {
            throw handleException(ex);
        }
    }

    /**
     * Returns a string representation of the given date time in yyyyMMdd
     * format. The date returned is in the UTC zone.
     * <p>
     * For example, given a time "1416863450581", this method returns "20141124"
     */
    public static String formatCompressedIso8601DateStamp(long timeMilli) {
        return compressedIso8601DateFormat.print(timeMilli);
    }

    /**
     * Returns a string representation of the given date time in
     * yyyyMMdd'T'HHmmss'Z' format. The date returned is in the UTC zone.
     * <p>
     * For example, given a time "1416863450581", this method returns
     * "20141124T211050Z"
     */
    public static String formatCompressedIso8601Timestamp(long timeMilli) {
        return compressedIso8601TimeFormat.print(timeMilli);
    }

    /**
     * Parses the specified date string as a compressedIso8601TimeFormat ("yyyyMMdd'T'HHmmss'Z'") and returns the Date
     * object.
     *
     * @param dateString The date string to parse.
     * @return The parsed Date object.
     */
    public static Date parseCompressedISO8601Date(String dateString) {
        try {
            return new Date(compressedIso8601TimeFormat.parseMillis(dateString));
        } catch (RuntimeException ex) {
            throw handleException(ex);
        }
    }

    /**
     * Parses the given date string returned by the AWS service into a Date
     * object.
     */
    public static Date parseServiceSpecificDate(String dateString) {
        if (dateString == null)
            return null;
        try {
            BigDecimal dateValue = new BigDecimal(dateString);
            return new Date(dateValue.scaleByPowerOfTen(
                    AWS_DATE_MILLI_SECOND_PRECISION).longValue());
        } catch (NumberFormatException nfe) {
            throw new YopClientException("Unable to parse date : "
                    + dateString, nfe);
        }
    }

    /**
     * Formats the give date object into an AWS Service format.
     */
    public static String formatServiceSpecificDate(Date date) {
        if (date == null)
            return null;
        BigDecimal dateValue = BigDecimal.valueOf(date.getTime());
        return dateValue.scaleByPowerOfTen(0 - AWS_DATE_MILLI_SECOND_PRECISION)
                .toPlainString();
    }

    public static Date cloneDate(Date date) {
        return date == null ? null : new Date(date.getTime());
    }

    /**
     * Returns the number of days since epoch with respect to the given number
     * of milliseconds since epoch.
     */
    public static long numberOfDaysSinceEpoch(long milliSinceEpoch) {
        return TimeUnit.MILLISECONDS.toDays(milliSinceEpoch);
    }

}
