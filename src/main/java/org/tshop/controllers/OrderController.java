package org.tshop.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tshop.api.OrdersApi;
import org.tshop.dto.OrderDto;
import org.tshop.dto.OrderPropertiesDto;
import org.tshop.dto.OrderSearchResultDto;
import org.tshop.services.OrderService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client-addresses")
public class OrderController implements OrdersApi {
    private final OrderService service;

    @Override
    public ResponseEntity<Void> createOrder(OrderPropertiesDto properties) {
        service.create(properties);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> deleteOrder(Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<OrderDto> getOrder(Integer id) {
        OrderDto dto = service.get(id);
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<OrderSearchResultDto> getOrderSearchResult(Integer pageNumber, Integer pageSize) {
        OrderSearchResultDto dto = service.getSearchResult(pageNumber, pageSize);
        return ResponseEntity.ok().body(dto);
    }

    @Override
    public ResponseEntity<OrderDto> updateOrder(Integer id, OrderPropertiesDto properties) {
        final OrderDto dto = service.update(id, properties);
        return ResponseEntity.ok().body(dto);
    }
}
