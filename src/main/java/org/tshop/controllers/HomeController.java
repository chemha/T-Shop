package org.tshop.controllers;

import lombok.RequiredArgsConstructor;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tshop.dto.GoodPropertiesDto;
import org.tshop.dto.ProductPropertiesDto;
import org.tshop.entities.ClientEntity;
import org.tshop.entities.GoodEntity;
import org.tshop.entities.ProductEntity;
import org.tshop.services.ClientAddressService;
import org.tshop.services.ClientService;
import org.tshop.services.ProductService;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class HomeController {
    @Autowired
    private ProductService productService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private ClientAddressService clientAddressService;

    @GetMapping
    public String home(Model model, HttpSession session) {
        if (session.getAttribute("cart") == null) session.setAttribute("cart", new LinkedHashMap<ProductEntity, Integer>());
        model.addAttribute("products", productService.findAll());

        return "home";
    }

    @PostMapping("addCart")
    public String addCart(@RequestParam Integer productId, HttpSession session, RedirectAttributes redirectAttributes) {
        ProductEntity product = productService.loadEntity(productId);
        Map<ProductEntity, Integer> cart = (LinkedHashMap<ProductEntity, Integer>) session.getAttribute("cart");

        if (cart.containsKey(product)) {
            Integer currentQuantity = cart.get(product);
            if (currentQuantity + 1 > product.getQuantity()) {
                redirectAttributes.addFlashAttribute("error", "Error while adding the product. There isn't enough stock");
                return "redirect:/";
            }
            cart.put(product, currentQuantity + 1);
        } else {
            if (product.getQuantity() <= 0) {
                redirectAttributes.addFlashAttribute("error", "Error while adding the product. There isn't enough stock");
                return "redirect:/";
            }
            cart.put(product, 1);
        }

        if (cart.get(product) == null) {
            redirectAttributes.addFlashAttribute("error", "Error while adding the product, please try again");
            return "redirect:/";
        }

        redirectAttributes.addFlashAttribute("success", "The product was added successfully");

        session.setAttribute("cart", cart);
        return "redirect:/";
    }

    @PostMapping("removeCart")
    public String removeCart(@RequestParam Integer productId, HttpSession session, RedirectAttributes redirectAttributes) {
        ProductEntity product = productService.loadEntity(productId);
        Map<ProductEntity, Integer> cart = (LinkedHashMap<ProductEntity, Integer>) session.getAttribute("cart");

        cart.remove(product);

        if (cart.get(product) != null) {
            redirectAttributes.addFlashAttribute("error", "Error while removing the product, please try again");
            return "redirect:/cart";
        }

        session.setAttribute("cart", cart);
        redirectAttributes.addFlashAttribute("success", "The product was removed successfully");
        return "redirect:/cart";
    }

    @PostMapping("modifyCart")
    public String modifyCart(@RequestParam Integer productId, @RequestParam Integer cartQuantity, HttpSession session, RedirectAttributes redirectAttributes) {
        final ProductEntity product = productService.loadEntity(productId);

        if (cartQuantity > product.getQuantity()) {
            redirectAttributes.addFlashAttribute("error", "Error while modifying quantity. There isn't enough stock");
            return "redirect:/cart";
        }

        final Map<ProductEntity, Integer> cart = (LinkedHashMap<ProductEntity, Integer>) session.getAttribute("cart");

        cart.put(product, cartQuantity);

        session.setAttribute("cart", cart);
        redirectAttributes.addFlashAttribute("success", "The product was modified successfully");
        return "redirect:/cart";
    }

    @GetMapping("cart")
    public String getCart(HttpSession session, Model model) {
        Integer clientId = (Integer) session.getAttribute("userId");
        model.addAttribute("addresses", clientAddressService.findByClientId(clientId));
        Map<ProductEntity, Integer> cart = (LinkedHashMap<ProductEntity, Integer>) session.getAttribute("cart");
        float price = 0;

        for (Map.Entry<ProductEntity, Integer> entry : cart.entrySet()) {
            price += entry.getKey().getPrice() * entry.getValue();
        }

        model.addAttribute("price", price);

        return "clients/cart";
    }


    /*
    @PostMapping("/cart")
    public String addCart(@RequestParam Integer id, @RequestParam Integer quantity, Model model) {
        GoodEntity goodEntity = new GoodEntity();
        ProductEntity product = new ProductEntity();
        double total = 0;

        product = productService.loadEntity(id);
        goodEntity.setQuantity(quantity);

        Integer currentProductId = product.getId();


        return ("/client/cart");
    }
    */
}
