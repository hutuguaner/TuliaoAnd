package com.lzq.tuliaoand.common;

//所有的 sharepreference 的key 放到这里  管理 保证唯一性
public enum SPKey implements UniqueKeyListener {

    IS_JUST_INSTALL,// 是否刚刚安装
    IS_LOGIN,//是否登录
    EMAIL_LOGINED,//当前登录的 用户的邮箱
    ;

    @Override
    public String getUniqueName() {
        return Integer.toString(this.ordinal());
    }
}
