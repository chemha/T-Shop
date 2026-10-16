package org.tshop.cart;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.tshop.api.CartApi;
import org.tshop.api.model.CartDto;
import org.tshop.api.model.CartItemRequestDto;
import org.tshop.api.model.CartMergeRequestDto;
import org.tshop.auth.CurrentCustomer;
import org.tshop.cart.CartService.CartView;
import org.tshop.cart.CartService.GuestItem;

import java.util.List;

@RestController
class CartController implements CartApi {

    private final CartService cart;
    private final CurrentCustomer currentCustomer;
    private final CartMapper mapper;

    CartController(CartService cart, CurrentCustomer currentCustomer, CartMapper mapper) {
        this.cart = cart;
        this.currentCustomer = currentCustomer;
        this.mapper = mapper;
    }

    @Mapper
    interface CartMapper {

        @Mapping(target = "items", source = "lines")
        CartDto toDto(CartView cart);
    }

    @Override
    public ResponseEntity<CartDto> getCart() {
        return ResponseEntity.ok(mapper.toDto(cart.get(currentCustomer.id())));
    }

    @Override
    public ResponseEntity<CartDto> setCartItem(Long productId, CartItemRequestDto request) {
        return ResponseEntity.ok(mapper.toDto(cart.setItem(currentCustomer.id(), productId, request.getQuantity())));
    }

    @Override
    public ResponseEntity<CartDto> mergeCart(CartMergeRequestDto request) {
        List<GuestItem> guestItems = request.getItems().stream()
                .map(item -> new GuestItem(item.getProductId(), item.getQuantity()))
                .toList();
        return ResponseEntity.ok(mapper.toDto(cart.merge(currentCustomer.id(), guestItems)));
    }

    @Override
    public ResponseEntity<CartDto> removeCartItem(Long productId) {
        return ResponseEntity.ok(mapper.toDto(cart.removeItem(currentCustomer.id(), productId)));
    }

    @Override
    public ResponseEntity<Void> clearCart() {
        cart.clear(currentCustomer.id());
        return ResponseEntity.noContent().build();
    }
}
