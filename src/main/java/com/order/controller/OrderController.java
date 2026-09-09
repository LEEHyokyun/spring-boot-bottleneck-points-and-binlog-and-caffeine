package com.order.controller;

import com.order.model.request.OrderSelectRequest;
import com.order.model.request.OrderUpdateRequest;
import com.order.model.response.OrderSelectResponse;
import com.order.model.response.OrderUpdateResponse;
import com.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;


@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/select")
    OrderSelectResponse select(@RequestParam Long orderId){
        return orderService.select(orderId);
    }

    @PostMapping("/update")
    public OrderUpdateResponse update(@RequestBody OrderUpdateRequest orderUpdateRequest) {
        return orderService.update(orderUpdateRequest);
    }

}
