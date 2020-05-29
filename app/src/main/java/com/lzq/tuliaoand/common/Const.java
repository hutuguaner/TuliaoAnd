package com.lzq.tuliaoand.common;

public class Const {

    public static final boolean isRelease = false;

    public static String ip() {
        if (isRelease) {
            //return "192.168.1.13";
            return "192.168.0.194";
        } else {
            //return "192.168.1.13";
            return "192.168.0.194";
        }
    }

    private static String port() {
        if (isRelease) {
            return "1582";
        } else {
            return "1582";
        }
    }

    public static int socketPort(){
        if (isRelease){
            return 1583;
        }else{
            return 1583;
        }
    }


    public static final String GET_VERIFYCODE_EMAIL = "http://" + ip() + ":" + port() + "/getVerifyCodeByEmail";//向邮箱发送验证码
    public static final String REGIST = "http://" + ip() + ":" + port() + "/regist";//注册
    public static final String LOGIN = "http://" + ip() + ":" + port() + "/login";//登录


}
