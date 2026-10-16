package org.tshop.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {

    List<CartItem> findByIdCustomerIdOrderByAddedAtAsc(Long customerId);

    /** One DELETE statement, instead of loading every item and deleting them one by one. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CartItem c WHERE c.id.customerId = :customerId")
    int deleteAllByCustomerId(Long customerId);
}
