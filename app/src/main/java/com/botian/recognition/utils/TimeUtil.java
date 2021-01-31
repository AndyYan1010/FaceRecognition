package com.botian.recognition.utils;

import com.botian.recognition.bean.ReportListInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

    public static String subStrTime2Sec(String oriTime) {
        if (null == oriTime || "".equals(oriTime)) {
            return "--";
        }
        if (oriTime.length() <= 19) {
            return oriTime;
        }
        return oriTime.substring(0, 19);
    }

    public static String changeDateTime2String(ReportListInfo.ListBean.CreateTimeBean createTimeBean) {
        if (null == createTimeBean || createTimeBean.getTime() == 0) {
            return "--";
        }
        return getChangeDate(createTimeBean.getTime());
    }

    public static String getChangeDate(long longTime) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Calendar   calendar  = Calendar.getInstance();
        calendar.setTimeInMillis(longTime);
        return formatter.format(calendar.getTime());
    }

    public static String getNowDateAndTimeStr() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
