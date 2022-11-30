package com.day.getbazzarspring.pojo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.day.getbazzarspring.dto.FullProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuickState {
    private String productId;
    private BigDecimal sellPrice;
    private BigDecimal buyPrice;
    private Long sellVolume;
    private Long buyVolume;
    private Long buyMovingWeek;
    private Long sellMovingWeek;
    private Long sellOrders;
    private Long buyOrders;
    private Long timestamp;
    private BigDecimal LowestBuyOderPrice;
    private BigDecimal HighestSellOderPrice;
    private List<JSONObject> sell_summary;
    private List<JSONObject> buy_summary;

    public static QuickState build(FullProduct fullProduct, Long timestamp) {
        QuickState quickState = JSONUtil.toBean(fullProduct.getQuick_status(), QuickState.class);
        BeanUtil.copyProperties(fullProduct, quickState, true);
        quickState.setTimestamp(timestamp);
        if (fullProduct.getBuy_summary() == null || fullProduct.getBuy_summary().isEmpty()) {
            List<JSONObject> buySummary = new ArrayList<>();
            buySummary.add(Order.EmptyObj());
            quickState.buy_summary = buySummary;
        }
        if (fullProduct.getSell_summary() == null || fullProduct.getSell_summary().isEmpty()) {
            List<JSONObject> sellSummary = new ArrayList<>();
            sellSummary.add(Order.EmptyObj());
            quickState.sell_summary = sellSummary;
        }
        quickState.LowestBuyOderPrice = JSONUtil.toBean(quickState.buy_summary.get(0), Order.class).getPricePerUnit();
        quickState.HighestSellOderPrice = JSONUtil.toBean(quickState.sell_summary.get(0), Order.class).getPricePerUnit();
        return quickState;
    }
}
