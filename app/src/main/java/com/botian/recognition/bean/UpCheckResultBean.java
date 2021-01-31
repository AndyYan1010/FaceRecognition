package com.botian.recognition.bean;

import java.util.List;

public class UpCheckResultBean {

    /**
     * code : 1
     * message : 1001上班打卡成功，
     * audio :
     * list : [{"userid":"1001","username":"1001","type":"1","ftime":"2021-01-01 08:00:00","ftype":"1","message":"上班打卡成功"}]
     */

    private String         code;
    private String         message;
    private String         audio;
    private List<ListBean> list;

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

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean {
        /**
         * userid : 1001
         * username : 1001
         * type : 1
         * ftime : 2021-01-01 08:00:00
         * ftype : 1
         * message : 上班打卡成功
         */

        private String userid;
        private String username;
        private String type;
        private String ftime;
        private String ftype;
        private String message;

        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getFtime() {
            return ftime;
        }

        public void setFtime(String ftime) {
            this.ftime = ftime;
        }

        public String getFtype() {
            return ftype;
        }

        public void setFtype(String ftype) {
            this.ftype = ftype;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
