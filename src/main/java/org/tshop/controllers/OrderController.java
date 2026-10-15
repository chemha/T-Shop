package org.tshop.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tshop.api.OrdersApi;
import org.tshop.dto.*;
import org.tshop.entities.OrderEntity;
import org.tshop.entities.ProductEntity;
import org.tshop.repositories.OrderRepository;
import org.tshop.services.GoodService;
import org.tshop.services.OrderService;
import org.tshop.services.ProductService;

import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController implements OrdersApi {
    private final OrderService orderService;
    private final GoodService goodService;
    private final ProductService productService;
    private final OrderRepository orderRepository;

    @GetMapping("/index")
    public String index(Model model, HttpSession session) {
        final Integer clientId = (Integer) session.getAttribute("userId");
        model.addAttribute("orders", orderService.findByClientId(clientId));

        return "orders/index";
    }

    @GetMapping("/all")
    public String all(Model model) {
        model.addAttribute("orders", orderService.findAll());

        return "orders/all";
    }

    @PostMapping("setStatus")
    public String setStatus(@RequestParam Integer id, @RequestParam String status) {
        OrderEntity order = orderService.loadEntity(id);
        order.setOrderStatus(status);
        orderRepository.save(order);
        return "redirect:/orders/all";
    }

    @PostMapping("/create")
    public String placeOrder(@RequestParam Integer addressId, @RequestParam String payment, @RequestParam String delivery, HttpSession session, RedirectAttributes redirectAttributes) {
        Map<ProductEntity, Integer> cart = (LinkedHashMap<ProductEntity, Integer>) session.getAttribute("cart");
        final Integer clientId = (Integer) session.getAttribute("userId");

        if (cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Your cart is empty");
            return "redirect:/cart";
        }

        // The products stored in the session may be outdated, so check the current stock before ordering
        for (Map.Entry<ProductEntity, Integer> entry : cart.entrySet()) {
            ProductEntity product = productService.loadEntity(entry.getKey().getId());
            if (entry.getValue() > product.getQuantity()) {
                redirectAttributes.addFlashAttribute("error", "There isn't enough stock of " + product.getTitle());
                return "redirect:/cart";
            }
        }

        GoodPropertiesDto goodPropertiesDto;
        ProductPropertiesDto productPropertiesDto;
        final OrderPropertiesDto orderPropertiesDto = new OrderPropertiesDto();

        orderPropertiesDto.setAddressId(addressId);
        orderPropertiesDto.setClientId(clientId);
        orderPropertiesDto.setOrderStatus("Pending");
        orderPropertiesDto.setDeliveryMethod(delivery);
        orderPropertiesDto.setPaymentMethod(payment);
        orderPropertiesDto.setPaymentStatus("Pending");

        OrderDto createdOrder = orderService.create(orderPropertiesDto);

        for (Map.Entry<ProductEntity, Integer> entry : cart.entrySet()) {
            final ProductEntity product = productService.loadEntity(entry.getKey().getId());
            goodPropertiesDto = new GoodPropertiesDto();

            goodPropertiesDto.setOrderId(createdOrder.getId());
            goodPropertiesDto.setProductId(entry.getKey().getId());
            goodPropertiesDto.setQuantity(entry.getValue());

            goodService.create(goodPropertiesDto);

            productPropertiesDto = new ProductPropertiesDto();
            productPropertiesDto.setTitle(product.getTitle());
            productPropertiesDto.setCategoryId(product.getCategory().getId());
            productPropertiesDto.setQuantity(product.getQuantity() - entry.getValue());
            productPropertiesDto.setBrand(product.getBrand());
            productPropertiesDto.setPrice(product.getPrice());
            productPropertiesDto.setVolume(product.getVolume());
            productPropertiesDto.setWeight(product.getWeight());
            productPropertiesDto.setColor(product.getColor());

            productService.update(product.getId(), productPropertiesDto);
        }

        session.setAttribute("cart", new LinkedHashMap<ProductEntity, Integer>());

        return "redirect:/orders/index";
    }

    @Override
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(OrderPropertiesDto properties) {
        OrderDto dto = orderService.create(properties);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(Integer id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(Integer id) {
        OrderDto dto = orderService.get(id);
        return ResponseEntity.ok(dto);
    }

    @Override
    @GetMapping
    public ResponseEntity<OrderSearchResultDto> getOrderSearchResult(Integer pageNumber, Integer pageSize) {
        OrderSearchResultDto dto = orderService.getSearchResult(pageNumber, pageSize);
        return ResponseEntity.ok().body(dto);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrder(Integer id, OrderPropertiesDto properties) {
        final OrderDto dto = orderService.update(id, properties);
        return ResponseEntity.ok().body(dto);
    }
}
