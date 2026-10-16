package org.tshop.order;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.tshop.api.OrdersApi;
import org.tshop.api.model.OrderDto;
import org.tshop.api.model.OrderPageDto;
import org.tshop.api.model.PlaceOrderRequestDto;
import org.tshop.auth.CurrentCustomer;
import org.tshop.order.OrderService.Checkout;
import org.tshop.shared.mapping.PageMapper;

@RestController
class OrderController implements OrdersApi {

    private final OrderService orders;
    private final CurrentCustomer currentCustomer;
    private final OrderMapper mapper;
    private final PageMapper pageMapper;

    OrderController(OrderService orders, CurrentCustomer currentCustomer, OrderMapper mapper, PageMapper pageMapper) {
        this.orders = orders;
        this.currentCustomer = currentCustomer;
        this.mapper = mapper;
        this.pageMapper = pageMapper;
    }

    @Override
    public ResponseEntity<OrderDto> placeOrder(PlaceOrderRequestDto request) {
        Checkout checkout = new Checkout(request.getAddressId(),
                PaymentMethod.valueOf(request.getPaymentMethod().name()),
                DeliveryMethod.valueOf(request.getDeliveryMethod().name()));
        Order order = orders.placeOrder(currentCustomer.id(), checkout);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(order));
    }

    @Override
    public ResponseEntity<OrderPageDto> listMyOrders(Integer page, Integer size) {
        Page<Order> result = orders.listForCustomer(currentCustomer.id(), page, size);
        return ResponseEntity.ok(new OrderPageDto(result.map(mapper::toDto).getContent(), pageMapper.toMetadata(result)));
    }

    @Override
    public ResponseEntity<OrderDto> getMyOrder(Long orderId) {
        return ResponseEntity.ok(mapper.toDto(orders.getForCustomer(currentCustomer.id(), orderId)));
    }

    @Override
    public ResponseEntity<OrderDto> cancelMyOrder(Long orderId) {
        return ResponseEntity.ok(mapper.toDto(orders.cancelForCustomer(currentCustomer.id(), orderId)));
    }
}
