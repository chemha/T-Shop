package org.tshop.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tshop.customer.Customer;
import org.tshop.customer.CustomerRepository;
import org.tshop.customer.Role;
import org.tshop.order.OrderRepository;
import org.tshop.shared.error.ConflictException;
import org.tshop.shared.error.NotFoundException;

@Service
class CustomerAdminService {

    private static final Logger log = LoggerFactory.getLogger(CustomerAdminService.class);

    private final CustomerRepository customers;
    private final OrderRepository orders;

    CustomerAdminService(CustomerRepository customers, OrderRepository orders) {
        this.customers = customers;
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    Page<Customer> list(int page, int size) {
        return customers.findAll(PageRequest.of(page, size, Sort.by("id")));
    }

    @Transactional(readOnly = true)
    Customer get(Long customerId) {
        return customers.findById(customerId).orElseThrow(() -> new NotFoundException("Customer", customerId));
    }

    /** An admin can't demote themselves: that could leave the shop without any admin. */
    @Transactional
    Customer changeRole(Long adminId, Long customerId, Role role) {
        if (adminId.equals(customerId) && role != Role.ADMIN) {
            throw new ConflictException("You can't remove your own admin role");
        }
        Customer customer = get(customerId);
        customer.changeRole(role);
        log.info("Admin {} set the role of customer {} to {}", adminId, customerId, role);
        return customer;
    }

    /**
     * Orders are business records and must be kept, so customers with orders can't be deleted.
     * Their addresses and cart go with them (ON DELETE CASCADE).
     */
    @Transactional
    void delete(Long adminId, Long customerId) {
        if (adminId.equals(customerId)) {
            throw new ConflictException("You can't delete your own account");
        }
        Customer customer = get(customerId);
        if (orders.existsByCustomerId(customerId)) {
            throw new ConflictException("Customer %d has orders and can't be deleted".formatted(customerId));
        }
        customers.delete(customer);
        log.info("Admin {} deleted customer {}", adminId, customerId);
    }
}
