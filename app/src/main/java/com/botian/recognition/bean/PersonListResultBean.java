package com.botian.recognition.bean;

import java.io.Serializable;
import java.util.List;

public class PersonListResultBean implements Serializable {

    /**
     * code : 1
     * message : 查询成功
     * list : [{"fname":"10001","fnote":"","id":"10001","fimage":""},{"fname":"季健","fnote":"","id":"10005","fimage":""},{"fname":"宋金华","fnote":"","id":"10006","fimage":"upload/files/20201223/sjh_1608714809142.jpg"}]
     */

    private String         code;
    private String         message;
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

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean implements Serializable {
        /**
         * fname : 10001
         * fnote :
         * id : 10001
         * fimage :
         */

        private String fname;
        private String fnote;
        private String id;
        private String fimage;

        public String getFname() {
            return fname;
        }

        public void setFname(String fname) {
            this.fname = fname;
        }

        public String getFnote() {
            return fnote;
        }

        public void setFnote(String fnote) {
            this.fnote = fnote;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFimage() {
            return fimage;
        }

        public void setFimage(String fimage) {
            this.fimage = fimage;
        }
    }
}
