package com.easymart.controller;

import com.easymart.exceptions.ProductException;
import com.easymart.model.Cart;
import com.easymart.model.CartItem;
import com.easymart.model.Product;
import com.easymart.model.User;
import com.easymart.request.AddItemRequest;
import com.easymart.response.ApiResponse;
import com.easymart.service.CartItemService;
import com.easymart.service.CartService;
import com.easymart.service.ProductService;
import com.easymart.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    private final CartItemService cartItemService;
    private final UserService userService;
    private final ProductService productService;


    @GetMapping
    public ResponseEntity<Cart> findUserCartHandler(@RequestHeader("Authorization") String jwt)throws ProductException,Exception{
        User user=userService.findUserByJwtToken(jwt);
        Cart cart=cartService.findUserCart(user);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @PutMapping("/add")
    public ResponseEntity<CartItem> addItemToCart(@RequestBody AddItemRequest req, @RequestHeader("Authorization")String jwt) throws Exception {
        User user=userService.findUserByJwtToken(jwt);
        Product product=productService.findProductById(req.getProductId());

        CartItem item=cartService.addCartItem(user, product, req.getSize(), req.getQuantity());
        ApiResponse res=new ApiResponse();
        res.setMessage("Item added to cart successfully");
        return new ResponseEntity<>(item, HttpStatus.ACCEPTED);
    }
    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<ApiResponse> deleteCartItemHandler(@PathVariable Long cartItemId, @RequestHeader("Authorization")String jwt) throws Exception {
        User user=userService.findUserByJwtToken(jwt);
        cartItemService.removeCartItem(user.getId(), cartItemId);
        ApiResponse res=new ApiResponse();
        res.setMessage("Item remove from cart");
        return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
    }
    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<CartItem> updateCartItemHandler(@PathVariable Long cartItemId, @RequestBody CartItem cartItem, @RequestHeader("Authorization")String jwt) throws Exception {
        User user=userService.findUserByJwtToken(jwt);
        CartItem updateCartItem=null;
        if(cartItem.getQuantity()>0){
            updateCartItem=cartItemService.updateCartItem(user.getId(), cartItemId, cartItem);
        }
        return new ResponseEntity<>(updateCartItem, HttpStatus.ACCEPTED);
    }
}
