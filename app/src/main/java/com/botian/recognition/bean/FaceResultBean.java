package com.botian.recognition.bean;

import java.util.List;

public class FaceResultBean {

    /**
     * respCode : 0
     * data : [{"personId":"10001","personName":"name"},{"personId":"10001","personName":"name"}]
     * ok : true
     * size : 0
     * message : 成功
     */

    private String respCode;
    private boolean  ok;
    private int      size;
    private String   message;
    private List<DataBean> data;

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * personId : 10001
         * personName : name
         */

        private String personId;
        private String personName;

        public String getPersonId() {
            return personId;
        }

        public void setPersonId(String personId) {
            this.personId = personId;
        }

        public String getPersonName() {
            return personName;
        }

        public void setPersonName(String personName) {
            this.personName = personName;
        }
    }
}
