package com.wiblog.cmp.client.common;

/**
 * 状态码
 *
 * @author pwm
 * @date 2020/3/1
 */
public enum StateEnum {

    /**
     * 成功
     */
    SUCCESS(10000);

    StateEnum(int code) {
        this.code = code;
    }

    private int code;

    public int getCode(){
        return code;
    }

}
