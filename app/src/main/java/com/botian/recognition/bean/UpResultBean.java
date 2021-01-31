package com.botian.recognition.bean;

import java.util.List;

public class UpResultBean {

    /**
     * code : 1
     * message : 上班打卡成功
     */

    private String         code;
    private String         message;
    private String         audio;
    private List<ListBean> list;

    /**
     * data : {"audio":"UklGRpx/AABXQVZFZm10IBAAAAABAAEAgD4AAAB9AAA"}
     */

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
         * userid : 11
         * username : 张三
         * ftype : 1
         * message : 上班打卡成功
         */

        private String userid;
        private String username;
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
