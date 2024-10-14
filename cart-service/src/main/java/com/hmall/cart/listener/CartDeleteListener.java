package com.hmall.cart.listener;

import com.hmall.cart.service.impl.CartServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class CartDeleteListener {

    private final CartServiceImpl cartService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "cart.clear.queue", durable = "true"),
            exchange = @Exchange(name = "trade.topic"),
            key = "order.create"
    ))
    public void handle(Collection<Long> itemIds) {
        // 删除购物车
        cartService.removeByItemIds(itemIds);
    }
}
