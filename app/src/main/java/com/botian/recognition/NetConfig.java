package com.botian.recognition;

/**
 * 网络相关配置
 *
 * @author zww
 */
public class NetConfig {
    public static String MainROOT     = "http://3n348g8245.wicp.vip:42007/haianJK";
    //public static String MainROOT     = "http://10.0.0.11:42007/haianJK/";
    //人脸搜索
    public static String SEARCHFACE   = MainROOT + "searchFace";
    //获取班线列表
    public static String BMLIST       = MainROOT + "bmlist";
    //新开机或增加人员（获取开机类别）
    public static String SERKJTYPE    = MainROOT + "serkjtype";
    //新开机增加人员
    public static String ADDWORKUSER  = MainROOT + "addworkuser";
    //开机记录列表
    public static String KJRECORDLIST = MainROOT + "kjrecordlist";
    //增加开机人员
    public static String ADDUSER      = MainROOT + "adduser";
    //上班
    public static String STARTWORK    = MainROOT + "startwork";
    //下班
    public static String ENDWORK      = MainROOT + "endwork";
    //关机列表
    public static String GJRECORDLIST = MainROOT + "gjrecordlist";
    //关机
    public static String HBGUANJI     = MainROOT + "hbguanji";

    //汇报单号
    public static String SERHUIBAOWORK  = MainROOT + "serhuibaowork";
    //汇报单人员列表
    public static String GDRYLIST       = MainROOT + "gdrylist";
    //批量汇报
    public static String ADDRY2         = MainROOT + "addry2";
    //删除人员
    public static String DELHBRY        = MainROOT + "delhbry";
    //人员确认开机
    public static String ISKAIJI        = MainROOT + "iskaiji";
    //提交人脸特征值
    public static String UPDATEUSERNOTE = MainROOT + "updateusernote";
    //人员列表
    public static String USERSLIST      = MainROOT + "userslist";
    //提交打卡信息
    public static String UPDATEWORK     = MainROOT + "updatework";
    //获取特征值列表
    public static String FNOTELIST      = MainROOT + "fnotelist";

    //获取密码
    public static String SEROK = MainROOT + "serok";

}