package com.day.getbazzarspring.utils;

import cn.hutool.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class R implements Serializable {

    private String code;
    private String msg;
    private Object data;

    public static R OK_json(JSONObject json) {
        return new R("200", "操作成功", json);
    }

    public static R OK() {
        return new R("200", "操作成功", null);
    }

    public static R error(String msg) {
        return new R("100", msg, null);
    }

}
