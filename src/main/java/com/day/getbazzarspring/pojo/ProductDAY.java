package com.day.getbazzarspring.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor

@TableName("product_day")
public class ProductDAY {
    @TableId("uni_id")
    private String uniId;
    private String productId;
    private BigDecimal buyOrderPriceAvg;
    private BigDecimal sellOrderPriceAvg;
    private BigDecimal lowestBuyOrderPrice;
    private BigDecimal highestBuyOrderPrice;
    private BigDecimal buyPriceAvg;
    private BigDecimal sellPriceAvg;
    private Long timestamp;
}
