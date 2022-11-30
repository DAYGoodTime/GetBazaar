package com.day.getbazzarspring.servicesImpl;

import com.day.getbazzarspring.dao.ProductDAYMapper;
import com.day.getbazzarspring.pojo.ProductDAY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SQLServices {
    @Autowired
    ProductDAYMapper productDAYMapper;

    @Async("asyncPoolTaskExecutor")
    public void processQuick() {
        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("QUICK");
    }

    public boolean insertDate(ProductDAY productDAY) {
        return productDAYMapper.insert(productDAY) == 1;
    }
}
