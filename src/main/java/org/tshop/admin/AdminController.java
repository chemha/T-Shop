package org.tshop.admin;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.tshop.api.AdminApi;
import org.tshop.api.model.ChangeOrderStatusRequestDto;
import org.tshop.api.model.ChangeRoleRequestDto;
import org.tshop.api.model.CustomerDto;
import org.tshop.api.model.CustomerPageDto;
import org.tshop.api.model.OrderDto;
import org.tshop.api.model.OrderPageDto;
import org.tshop.api.model.OrderStatusDto;
import org.tshop.auth.CurrentCustomer;
import org.tshop.customer.Customer;
import org.tshop.customer.CustomerMapper;
import org.tshop.customer.Role;
import org.tshop.order.Order;
import org.tshop.order.OrderMapper;
import org.tshop.order.OrderService;
import org.tshop.order.OrderStatus;
import org.tshop.shared.mapping.PageMapper;

/**
 * SecurityConfig already restricts /admin/** to admins. {@code @PreAuthorize} repeats the rule
 * on the class itself (defence in depth): it still holds if the URL rules are changed by mistake.
 */
@RestController
@PreAuthorize("hasRole('ADMIN')")
class AdminController implements AdminApi {

    private final CustomerAdminService customers;
    private final OrderService orders;
    private final CurrentCustomer currentCustomer;
    private final CustomerMapper customerMapper;
    private final OrderMapper orderMapper;
    private final PageMapper pageMapper;

    AdminController(CustomerAdminService customers, OrderService orders, CurrentCustomer currentCustomer,
                    CustomerMapper customerMapper, OrderMapper orderMapper, PageMapper pageMapper) {
        this.customers = customers;
        this.orders = orders;
        this.currentCustomer = currentCustomer;
        this.customerMapper = customerMapper;
        this.orderMapper = orderMapper;
        this.pageMapper = pageMapper;
    }

    // ---------------------------------------------------------------- Customers

    @Override
    public ResponseEntity<CustomerPageDto> listCustomers(Integer page, Integer size) {
        Page<Customer> result = customers.list(page, size);
        return ResponseEntity.ok(new CustomerPageDto(result.map(customerMapper::toDto).getContent(), pageMapper.toMetadata(result)));
    }

    @Override
    public ResponseEntity<CustomerDto> getCustomer(Long customerId) {
        return ResponseEntity.ok(customerMapper.toDto(customers.get(customerId)));
    }

    @Override
    public ResponseEntity<CustomerDto> changeCustomerRole(Long customerId, ChangeRoleRequestDto request) {
        Role role = Role.valueOf(request.getRole().name());
        return ResponseEntity.ok(customerMapper.toDto(customers.changeRole(currentCustomer.id(), customerId, role)));
    }

    @Override
    public ResponseEntity<Void> deleteCustomer(Long customerId) {
        customers.delete(currentCustomer.id(), customerId);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- Orders

    @Override
    public ResponseEntity<OrderPageDto> listOrders(@Nullable OrderStatusDto status, Integer page, Integer size) {
        OrderStatus filter = status == null ? null : OrderStatus.valueOf(status.name());
        Page<Order> result = orders.listAll(filter, page, size);
        return ResponseEntity.ok(new OrderPageDto(result.map(orderMapper::toDto).getContent(), pageMapper.toMetadata(result)));
    }

    @Override
    public ResponseEntity<OrderDto> getOrder(Long orderId) {
        return ResponseEntity.ok(orderMapper.toDto(orders.get(orderId)));
    }

    @Override
    public ResponseEntity<OrderDto> changeOrderStatus(Long orderId, ChangeOrderStatusRequestDto request) {
        OrderStatus next = OrderStatus.valueOf(request.getStatus().name());
        return ResponseEntity.ok(orderMapper.toDto(orders.changeStatus(orderId, next)));
    }
}
