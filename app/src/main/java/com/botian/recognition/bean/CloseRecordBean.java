package com.botian.recognition.bean;

import java.util.List;

public class CloseRecordBean {

    /**
     * count : 1
     * code : 1
     * message : 查询成功
     * list : [{"workno":"HB202012120002","createtime":"2020-12-24 14:06:35.1200000","rownumber":1,"id":"402f45db-44c3-4d48-b3b6-5f5b02dadf6a","starttime":"2020-12-24 14:06","workid":"76334476-a898-499b-b747-4076ac23e91c","users":"季健,钱培元"}]
     */

    private int count;
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
         * workno : HB202012120002
         * createtime : 2020-12-24 14:06:35.1200000
         * rownumber : 1
         * id : 402f45db-44c3-4d48-b3b6-5f5b02dadf6a
         * starttime : 2020-12-24 14:06
         * workid : 76334476-a898-499b-b747-4076ac23e91c
         * users : 季健,钱培元
         */

        private String workno;
        private String createtime;
        private int    rownumber;
        private String id;
        private String starttime;
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

        public String getStarttime() {
            return starttime;
        }

        public void setStarttime(String starttime) {
            this.starttime = starttime;
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
