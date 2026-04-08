package com.easymart.service.impl;

import com.easymart.model.Cart;
import com.easymart.model.CartItem;
import com.easymart.model.User;
import com.easymart.repository.CartItemRepository;
import com.easymart.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;


    @Override
    public CartItem updateCartItem(Long userId, Long id, CartItem cartItem) throws Exception {
        CartItem item=findCartItemById(id);
        User cartItemUser=item.getCart().getUser();
        if(cartItemUser.getId().equals(userId)){
            item.setQuantity(cartItem.getQuantity());
            item.setMrpPrice(item.getProduct().getMrpPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            item.setSellingPrice(item.getProduct().getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            return cartItemRepository.save(item);
        }
        throw new Exception("you cant update this cart item");
    }

    @Override
    public void removeCartItem(Long userId, Long cartItemId) throws Exception {
        CartItem item=findCartItemById(cartItemId);
        User cartItemUser=item.getCart().getUser();
        if(cartItemUser.getId().equals(userId)){
            Cart cart = item.getCart();
            cart.getCartItems().remove(item);
            item.setCart(null);
            cartItemRepository.delete(item);
        }
        else{
            throw new Exception("you cant delete this item ");
        }
    }

    @Override
    public CartItem findCartItemById(Long id) throws Exception {
        return cartItemRepository.findById(id).orElseThrow(()->new Exception(" cart item not found with id :"+id));
    }
}
