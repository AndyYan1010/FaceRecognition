package com.botian.recognition.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class CheckFaceHistory {

    /**
     * id : 2626L
     * userName : zhangsan
     * userID : 165161
     * checkType : 1
     * checkResult : 0
     * ftime : 2021-01-01 08:00:00
     */

    private String id;
    private String userName;
    private String userID;
    private String checkType;
    private String checkResult;
    private String ftime;
    
    @Generated(hash = 1365222265)
    public CheckFaceHistory(String id, String userName, String userID,
                            String checkType, String checkResult, String ftime) {
        this.id          = id;
        this.userName    = userName;
        this.userID      = userID;
        this.checkType   = checkType;
        this.checkResult = checkResult;
        this.ftime       = ftime;
    }

    @Generated(hash = 1575148195)
    public CheckFaceHistory() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return this.userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getCheckType() {
        return this.checkType;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    public String getCheckResult() {
        return this.checkResult;
    }

    public void setCheckResult(String checkResult) {
        this.checkResult = checkResult;
    }

    public String getFtime() {
        return this.ftime;
    }

    public void setFtime(String ftime) {
        this.ftime = ftime;
    }
}
