package com.hmall.trade.listener;

import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayStatusListener {

    private final IOrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "trade.pay.success.queue", durable = "true", arguments = @Argument(name="x-queue-mode", value="lazy")),
            exchange = @Exchange(name = "pay.direct"),
            key = "pay.success"
    ))

    public void handlePaySuccess(String orderId) {
        // 1. 查看订单
        Order order = orderService.getById(Long.valueOf(orderId));
        // 2. 判断订单状态
        if (order == null || order.getStatus() != 1) {
            // 不做处理
            return;
        }
        // 3. 修改订单状态
        orderService.markOrderPaySuccess(Long.valueOf(orderId));
    }
}
