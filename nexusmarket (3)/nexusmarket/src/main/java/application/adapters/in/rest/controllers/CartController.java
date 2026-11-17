package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.CommercialRestMapper;
import application.adapters.in.rest.mappers.RestReferences;
import application.adapters.in.rest.requests.AddCartItemRequest;
import application.adapters.in.rest.requests.UpdateCartItemRequest;
import application.adapters.in.rest.responses.OrderResponse;
import application.domain.models.Order;
import application.domain.services.order.AddItemToCartService;
import application.domain.services.order.CalculateOrderTotalService;
import application.domain.services.order.ConsultCartService;
import application.domain.services.order.RemoveItemFromCartService;
import application.domain.services.order.UpdateCartItemQuantityService;
import application.infrastructure.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/** The cart of the authenticated BUYER (its order in CART status). */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Transactional
public class CartController {

    private final AuthenticatedUserResolver authenticatedUser;
    private final ConsultCartService consultCartService;
    private final AddItemToCartService addItemToCartService;
    private final UpdateCartItemQuantityService updateCartItemQuantityService;
    private final RemoveItemFromCartService removeItemFromCartService;
    private final CalculateOrderTotalService calculateOrderTotalService;

    @GetMapping
    public OrderResponse consult() {
        return toResponse(consultCartService.execute(authenticatedUser.resolve()));
    }

    @GetMapping("/total")
    public BigDecimal total() {
        return consultCartService.executeTotal(authenticatedUser.resolve());
    }

    @PostMapping("/items")
    public OrderResponse addItem(@Valid @RequestBody AddCartItemRequest request) {
        return toResponse(addItemToCartService.execute(authenticatedUser.resolve(),
                RestReferences.variant(request.variantIdentifier()), request.quantity()));
    }

    @PatchMapping("/items/{variantIdentifier}")
    public OrderResponse updateItem(@PathVariable String variantIdentifier,
                                    @Valid @RequestBody UpdateCartItemRequest request) {
        return toResponse(updateCartItemQuantityService.execute(authenticatedUser.resolve(),
                RestReferences.variant(variantIdentifier), request.quantity()));
    }

    @DeleteMapping("/items/{variantIdentifier}")
    public OrderResponse removeItem(@PathVariable String variantIdentifier) {
        return toResponse(removeItemFromCartService.execute(authenticatedUser.resolve(),
                RestReferences.variant(variantIdentifier)));
    }

    private OrderResponse toResponse(Order cart) {
        return CommercialRestMapper.toResponse(cart, calculateOrderTotalService.execute(cart));
    }
}
