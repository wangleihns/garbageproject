package com.jin.env.garbage.utils;

import org.springframework.util.Assert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormatUtil {

    public static  String formatDate(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String dateString = null;
        try {
            dateString = sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dateString;
    }

    public static Date parse(String strDate, String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = null;
        try {
            date =  sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     *
     * @param date 2019-08-02
     * @return
     */
    public static Date getFirstDateOfMonth(String date){
        int year = getYear(date);
        int month = getMonth(date);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startTime = calendar.getTime();
        System.out.println(startTime);
        return startTime;
    }

    public static Date getLastDateOfMonth(String date){
        Calendar calendar = Calendar.getInstance();
        int year = getYear(date);
        int month = getMonth(date);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date startTime = calendar.getTime();
        System.out.println(startTime);
        return startTime;
    }

    /**
     * 当月第一天
     * @return
     */
    public static Date getFirstDayOfCurrentMonth(){
        String dateString =  DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd");
        return getFirstDateOfMonth(dateString);
    }

    public  static Date getFirstDayOfCurrentYear(){
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        calendar.clear();
        calendar.set(Calendar.YEAR, currentYear);
        Date currYearFirst = calendar.getTime();
        return currYearFirst;
    }

    public static int getYear(String dateString){
        Assert.hasText(dateString, "日期不能为空");
        Date date = parse(dateString, "yyyy-MM");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    public static int getMonth(String dateString){
        Assert.hasText(dateString, "日期不能为空");
        Date date = parse(dateString, "yyyy-MM");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH);
    }



    public static void main(String[] args) {
        getFirstDateOfMonth("2018-08");
        getLastDateOfMonth("2018-08");
        getFirstDayOfCurrentMonth();
        System.out.println(getFirstDayOfCurrentYear());
    }
}
