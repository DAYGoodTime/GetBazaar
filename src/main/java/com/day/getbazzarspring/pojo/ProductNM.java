package com.day.getbazzarspring.pojo;

import cn.hutool.core.bean.BeanUtil;
import com.day.getbazzarspring.dto.FullProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductNM {

    private String productId;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private BigDecimal LowestBuyOderPrice;
    private BigDecimal HighestSellOderPrice;
    private Long timestamp;

    public static Product_NM_Redis build(QuickState quickState) {
        Product_NM_Redis product_nm_redis = new Product_NM_Redis();
        BeanUtil.copyProperties(quickState, product_nm_redis, true);
        return product_nm_redis;
    }


}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Product_NM_Redis {
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private BigDecimal LowestBuyOderPrice;
    private BigDecimal HighestSellOderPrice;
    private Long timestamp;
}