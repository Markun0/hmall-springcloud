package com.hmall.trade.listener;

import com.hmall.api.dto.PayOrderDTO;
import com.hmall.trade.constants.MQConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import com.hmall.trade.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.hmall.api.client.PayClient;

@Component
@RequiredArgsConstructor
public class OrderDelayMessageListener {
    private final IOrderService orderService;

    private final PayClient payClient;



    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = MQConstants.DELAY_ORDER_QUEUE_NAME),
                    exchange = @Exchange(name = MQConstants.DELAY_EXCHANGE_NAME, delayed = "true"),
                    key = MQConstants.DELAY_ORDER_KEY
            )
    )
    public void listenOrderDelayMessage(Long orderId) {
        // 1. 查看订单
        Order order = orderService.getById(Long.valueOf(orderId));
        // 2. 判断订单状态
        if (order == null || order.getStatus() != 1) {
            // 不做处理
            return;
        }
        // 3. 未支付，需要查询支付流水状态
        PayOrderDTO payOrderDTO = payClient.queryPayOrderByBizOrderNo(Long.valueOf(orderId));
        // 4. 判断是否支付
        if (payOrderDTO != null || payOrderDTO.getStatus() == 3) {
            // 4.1 已支付，标记订单状态为已支付
            orderService.markOrderPaySuccess(orderId);
        }
        else{
            // 4.2.未支付，取消订单，回复库存
            orderService.cancelOrder(orderId);
        }

    }
}
