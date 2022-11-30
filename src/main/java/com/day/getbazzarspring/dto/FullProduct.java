package com.day.getbazzarspring.dto;

import cn.hutool.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FullProduct {
    private String productId;
    private List<JSONObject> sell_summary;
    private List<JSONObject> buy_summary;
    private JSONObject quick_status;
}
