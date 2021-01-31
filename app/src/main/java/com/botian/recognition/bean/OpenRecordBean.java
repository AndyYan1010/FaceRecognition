package com.botian.recognition.bean;

import java.util.List;

public class OpenRecordBean {

    /**
     * code : 1
     * message : 查询成功
     * list : [{"workno":"HB202012220001","createtime":"2020-12-21 16:33:36.8430000","rownumber":1,"id":"f09938dd-6437-402f-868b-6e8663250886","workid":"e6f10db4-39a1-4610-876a-24f8b1af55cd","users":"姓名1,姓名2"},{"workno":"HB202012100001","createtime":"2020-12-22 15:28:10.0000000","rownumber":2,"id":"1"},{"workno":"HB202012100001","createtime":"2020-12-22 15:24:39.0000000","rownumber":3,"id":"12"},{"createtime":"2020-12-22 14:07:31.3300000","rownumber":4,"id":"88d46bf3-c938-4e3c-a43d-cc13d551f76d","users":"张三"}]
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

    public static class ListBean {
        /**
         * workno : HB202012220001
         * createtime : 2020-12-21 16:33:36.8430000
         * rownumber : 1
         * id : f09938dd-6437-402f-868b-6e8663250886
         * workid : e6f10db4-39a1-4610-876a-24f8b1af55cd
         * users : 姓名1,姓名2
         */

        private String workno;
        private String createtime;
        private int    rownumber;
        private String id;
        private String workid;
        private String users;

        public String getWorkno() {
            return workno;
        }

        public void setWorkno(String workno) {
            this.workno = workno;
        }

        public String getCreatetime() {
            return createtime;
        }

        public void setCreatetime(String createtime) {
            this.createtime = createtime;
        }

        public int getRownumber() {
            return rownumber;
        }

        public void setRownumber(int rownumber) {
            this.rownumber = rownumber;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getWorkid() {
            return workid;
        }

        public void setWorkid(String workid) {
            this.workid = workid;
        }

        public String getUsers() {
            return users;
        }

        public void setUsers(String users) {
            this.users = users;
        }
    }
}
