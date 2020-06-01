package com.lzq.tuliaoand.common;

public class Const {

    public static final boolean isRelease = false;

    public static String ip() {
        if (isRelease) {
            //return "192.168.1.13";
            //return "192.168.1.4";
            return "192.168.1.180";
        } else {
            //return "192.168.1.13";
            //return "192.168.1.4";
            return "192.168.1.180";
        }
    }

    private static String port() {
        if (isRelease) {
            return "1582";
        } else {
            return "1582";
        }
    }

    public static int socketPort() {
        if (isRelease) {
            return 1583;
        } else {
            return 1583;
        }
    }


    public static final String GET_VERIFYCODE_EMAIL = "http://" + ip() + ":" + port() + "/getVerifyCodeByEmail";//向邮箱发送验证码
    public static final String REGIST = "http://" + ip() + ":" + port() + "/regist";//注册
    public static final String LOGIN = "http://" + ip() + ":" + port() + "/login";//登录
    public static final String GET_ALL_USERS = "http://" + ip() + ":" + port() + "/getUsers";//获取所有用户数据
    public static final String UPLOAD_POSITION = "http://" + ip() + ":" + port() + "/uploadPosition";//上传位置信息
    public static final String UPLOAD_MSG = "http://" + ip() + ":" + port() + "/uploadMsg";//上传消息数据
    public static final String GET_MSGS = "http://" + ip() + ":" + port() + "/getMsgs";//获取消息
    public static final String UPLOAD_BROADCAST = "http://" + ip() + ":" + port() + "/uploadBroadcast";//上传广播



}
