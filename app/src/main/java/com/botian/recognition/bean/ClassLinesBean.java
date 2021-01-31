package com.botian.recognition.bean;

import java.util.List;

public class ClassLinesBean {

    /**
     * code : 1
     * message : 查询成功
     * list : [{"id":"2c90b4586c6554f4016c655da465000b","department":"1号线-管摆臂"},{"id":"2c90b4586c6554f4016c655dd088000d","department":"2号线-管摆臂"},{"id":"2c90b4586c6554f4016c655e0984000f","department":"3号线-管摆臂"},{"id":"2c90b4586c6554f4016c655ea9880011","department":"4号线-管保险杠"},{"id":"2c90b4586c6554f4016c655eda310013","department":"5号线-管保险杠"},{"id":"2c90b4586c6ea7bb016c6ea9a9180001","department":"6号线-门"},{"id":"2c90b4586c6ea7bb016c6ea9d7620003","department":"7号线折弯保险杠"},{"id":"2c90b4586c6ea7bb016c6eaa019a0005","department":"9号线折弯摆臂"},{"id":"2c90b4586c6ea7bb016c6eaa2a110007","department":"8号线RA支架"},{"id":"8a80808d6cf012ac016cf0320ed50006","department":"激光"}]
     * userno : 8a80808d6cf012ac016cf11797ba00ba
     * username : 魏海霞
     * kdepartment : 板摆臂包装线Boxed A-arms packaging
     * gongxu : 包装
     */

    private String         code;
    private String         message;
    private String         userno;
    private String         username;
    private String         kdepartment;
    private String         gongxu;
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

    public String getUserno() {
        return userno;
    }

    public void setUserno(String userno) {
        this.userno = userno;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKdepartment() {
        return kdepartment;
    }

    public void setKdepartment(String kdepartment) {
        this.kdepartment = kdepartment;
    }

    public String getGongxu() {
        return gongxu;
    }

    public void setGongxu(String gongxu) {
        this.gongxu = gongxu;
    }

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean {
        /**
         * id : 2c90b4586c6554f4016c655da465000b
         * department : 1号线-管摆臂
         */

        private String id;
        private String department;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }
    }
}
