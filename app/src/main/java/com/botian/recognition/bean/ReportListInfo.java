package com.botian.recognition.bean;

import java.util.List;

public class ReportListInfo {

    /**
     * list : [{"create_time":{"date":4,"hours":13,"seconds":15,"month":0,"nanos":653000000,"timezoneOffset":-480,"year":121,"minutes":39,"time":1609738755653,"day":1},"endtime":"","fnote":"","fbiller":"魏海霞","starttime":"","freason":"","gongxu":"包装","workno":"HB202101040003","ftype":"1","rownumber":1,"kdepartment":"板摆臂包装线Boxed A-arms packaging","jlruserno":"8a80808d6cf012ac016cf11797ba00ba","id":"6cf4f589-3627-48ee-a519-7e9fdac83b25","fnumber":"5.05.PGH-1-SA-001-R-00"},{"create_time":{"date":4,"hours":13,"seconds":25,"month":0,"nanos":300000000,"timezoneOffset":-480,"year":121,"minutes":31,"time":1609738285300,"day":1},"endtime":"","fnote":"","fbiller":"魏海霞","starttime":"","freason":"","gongxu":"包装","workno":"HB202101040002","ftype":"1","rownumber":2,"kdepartment":"板摆臂包装线Boxed A-arms packaging","jlruserno":"8a80808d6cf012ac016cf11797ba00ba","id":"bafa1bec-dc61-4671-97b9-f8a7aa44e3fb","fnumber":"5.07.WM-CA-T.0000"},{"create_time":{"date":4,"hours":12,"seconds":54,"month":0,"nanos":330000000,"timezoneOffset":-480,"year":121,"minutes":31,"time":1609734714330,"day":1},"endtime":"","fnote":"","fbiller":"魏海霞","starttime":"2021-01-04 15:47","freason":"","gongxu":"包装","workno":"HB202101040001","ftype":"1","rownumber":3,"kdepartment":"板摆臂包装线Boxed A-arms packaging","jlruserno":"8a80808d6cf012ac016cf11797ba00ba","id":"cd3564d5-4173-4c96-8b92-46faedfa87de","fnumber":"5.14.DIF-P-RZR1K-HA1"},{"create_time":{"date":31,"hours":12,"seconds":28,"month":11,"nanos":517000000,"timezoneOffset":-480,"year":120,"minutes":59,"time":1609390768517,"day":4},"endtime":"","fnote":"","fbiller":"魏海霞","starttime":"2021-01-04 09:19","freason":"","gongxu":"包装","workno":"HB202012310002","ftype":"1","rownumber":4,"kdepartment":"板摆臂包装线Boxed A-arms packaging","jlruserno":"8a80808d6cf012ac016cf11797ba00ba","id":"25f30099-cd40-46a3-9a3e-df31ac1cfe7f","fnumber":"5.01.BES-H-TAL.0001"},{"create_time":{"date":31,"hours":9,"seconds":0,"month":11,"nanos":713000000,"timezoneOffset":-480,"year":120,"minutes":2,"time":1609376520713,"day":4},"endtime":"2020-12-31 12:52","fnote":"","fbiller":"魏海霞","starttime":"2020-12-29 15:09","freason":"","gongxu":"包装","workno":"HB202012310001","ftype":"1","rownumber":5,"kdepartment":"板摆臂包装线Boxed A-arms packaging","jlruserno":"8a80808d6cf012ac016cf11797ba00ba","id":"b3943d02-67bd-4f35-a4cf-6534a0496d4c","fnumber":"5.01.BES-H-TAL.0001","worktime":"2623"}]
     * count : 14
     * code : 1
     * message : 查询成功
     */

    private int            count;
    private String         code;
    private String         message;
    private List<ListBean> list;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean {
        /**
         * create_time : {"date":4,"hours":13,"seconds":15,"month":0,"nanos":653000000,"timezoneOffset":-480,"year":121,"minutes":39,"time":1609738755653,"day":1}
         * endtime :
         * fnote :
         * fbiller : 魏海霞
         * starttime :
         * freason :
         * gongxu : 包装
         * workno : HB202101040003
         * ftype : 1
         * rownumber : 1
         * kdepartment : 板摆臂包装线Boxed A-arms packaging
         * jlruserno : 8a80808d6cf012ac016cf11797ba00ba
         * id : 6cf4f589-3627-48ee-a519-7e9fdac83b25
         * fnumber : 5.05.PGH-1-SA-001-R-00
         * worktime : 2623
         */

        private CreateTimeBean create_time;
        private String         endtime;
        private String         fnote;
        private String         fbiller;
        private String         starttime;
        private String         freason;
        private String         gongxu;
        private String         workno;
        private String         ftype;
        private int            rownumber;
        private String         kdepartment;
        private String         jlruserno;
        private String         id;
        private String         fnumber;
        private String         worktime;

        public CreateTimeBean getCreate_time() {
            return create_time;
        }

        public void setCreate_time(CreateTimeBean create_time) {
            this.create_time = create_time;
        }

        public String getEndtime() {
            return endtime;
        }

        public void setEndtime(String endtime) {
            this.endtime = endtime;
        }

        public String getFnote() {
            return fnote;
        }

        public void setFnote(String fnote) {
            this.fnote = fnote;
        }

        public String getFbiller() {
            return fbiller;
        }

        public void setFbiller(String fbiller) {
            this.fbiller = fbiller;
        }

        public String getStarttime() {
            return starttime;
        }

        public void setStarttime(String starttime) {
            this.starttime = starttime;
        }

        public String getFreason() {
            return freason;
        }

        public void setFreason(String freason) {
            this.freason = freason;
        }

        public String getGongxu() {
            return gongxu;
        }

        public void setGongxu(String gongxu) {
            this.gongxu = gongxu;
        }

        public String getWorkno() {
            return workno;
        }

        public void setWorkno(String workno) {
            this.workno = workno;
        }

        public String getFtype() {
            return ftype;
        }

        public void setFtype(String ftype) {
            this.ftype = ftype;
        }

        public int getRownumber() {
            return rownumber;
        }

        public void setRownumber(int rownumber) {
            this.rownumber = rownumber;
        }

        public String getKdepartment() {
            return kdepartment;
        }

        public void setKdepartment(String kdepartment) {
            this.kdepartment = kdepartment;
        }

        public String getJlruserno() {
            return jlruserno;
        }

        public void setJlruserno(String jlruserno) {
            this.jlruserno = jlruserno;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFnumber() {
            return fnumber;
        }

        public void setFnumber(String fnumber) {
            this.fnumber = fnumber;
        }

        public String getWorktime() {
            return worktime;
        }

        public void setWorktime(String worktime) {
            this.worktime = worktime;
        }

        public static class CreateTimeBean {
            /**
             * date : 4
             * hours : 13
             * seconds : 15
             * month : 0
             * nanos : 653000000
             * timezoneOffset : -480
             * year : 121
             * minutes : 39
             * time : 1609738755653
             * day : 1
             */

            private int  date;
            private int  hours;
            private int  seconds;
            private int  month;
            private int  nanos;
            private int  timezoneOffset;
            private int  year;
            private int  minutes;
            private long time;
            private int  day;

            public int getDate() {
                return date;
            }

            public void setDate(int date) {
                this.date = date;
            }

            public int getHours() {
                return hours;
            }

            public void setHours(int hours) {
                this.hours = hours;
            }

            public int getSeconds() {
                return seconds;
            }

            public void setSeconds(int seconds) {
                this.seconds = seconds;
            }

            public int getMonth() {
                return month;
            }

            public void setMonth(int month) {
                this.month = month;
            }

            public int getNanos() {
                return nanos;
            }

            public void setNanos(int nanos) {
                this.nanos = nanos;
            }

            public int getTimezoneOffset() {
                return timezoneOffset;
            }

            public void setTimezoneOffset(int timezoneOffset) {
                this.timezoneOffset = timezoneOffset;
            }

            public int getYear() {
                return year;
            }

            public void setYear(int year) {
                this.year = year;
            }

            public int getMinutes() {
                return minutes;
            }

            public void setMinutes(int minutes) {
                this.minutes = minutes;
            }

            public long getTime() {
                return time;
            }

            public void setTime(long time) {
                this.time = time;
            }

            public int getDay() {
                return day;
            }

            public void setDay(int day) {
                this.day = day;
            }
        }
    }
}
