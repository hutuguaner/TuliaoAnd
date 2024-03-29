package com.lzq.tuliaoand.common;

public class Const {

    public static final boolean isRelease = false;

    public static String ip() {
        if (isRelease) {
            return "129.28.189.31";
        } else {
            //return "192.168.1.13";
            //return "192.168.1.4";
            //return "192.168.1.180";
            return "192.168.0.185";
        }
    }

    private static String port() {
        if (isRelease) {
            return "1583";
        } else {
            return "1583";
        }
    }


    public static final String GET_VERIFYCODE_EMAIL = "http://" + ip() + ":" + port() + "/getVerifyCodeByEmail";//向邮箱发送验证码
    public static final String REGIST = "http://" + ip() + ":" + port() + "/regist";//注册
    public static final String LOGIN = "http://" + ip() + ":" + port() + "/login";//登录
    public static final String GET_ALL_USERS = "http://" + ip() + ":" + port() + "/getUsers";//获取所有用户数据
    public static final String GET_MSGS = "http://" + ip() + ":" + port() + "/getMsgs";//获取消息
    public static final String GET_VERSION = "http://" + ip() + ":" + port() + "/getVersion";//上传广播

    public static final String APK_URL = "https://apk-lzq-1255312373.cos.ap-hongkong.myqcloud.com/tuliao.apk";


}
