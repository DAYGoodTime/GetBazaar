package com.day.getbazzarspring.pojo;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long amount;
    private BigDecimal pricePerUnit;
    private int orders;

    public static JSONObject EmptyObj() {
        return new JSONObject(new Order(0L, new BigDecimal(0), 0));
    }

    public static Order convJSON(JSONObject jsonObject) {
        return JSONUtil.toBean(jsonObject, Order.class);
    }

}
