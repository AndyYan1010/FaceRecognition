package com.botian.recognition.bean;

import java.util.List;

public class FnoteListBean {

    /**
     * code : 1
     * message : 查询成功
     * list : [{"fnote":"[F@b978cc3","id":"10001"}]
     */

    private String code;
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

    public static class ListBean {
        /**
         * fnote : [F@b978cc3
         * id : 10001
         */

        private String fnote;
        private String id;

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
    }
}
