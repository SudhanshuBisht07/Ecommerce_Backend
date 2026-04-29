package com.easymart.service.impl;

import com.easymart.domain.OrderStatus;
import com.easymart.domain.PaymentStatus;
import com.easymart.model.*;
import com.easymart.repository.*;
import com.easymart.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    @Override
    public Set<Order> createOrder(User user, Address shippingAddress, Cart cart) throws Exception {

        Address address=addressRepository.save(shippingAddress);
        if(!user.getAddresses().contains(shippingAddress)){
            user.getAddresses().add(shippingAddress);
            userRepository.save(user);
        }
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new Exception("Cart is empty");
        }

        Map<Long, List<CartItem>> itemsBySeller=cart.getCartItems().stream()
                .collect(Collectors.groupingBy(item->item.getProduct().getSeller().getId()));
        Set<Order> orders=new HashSet<>();
        for(Map.Entry<Long, List<CartItem>> entry: itemsBySeller.entrySet()){
            Long sellerId= entry.getKey();
            List<CartItem> items=entry.getValue();
            BigDecimal totalOrderPrice = items.stream()
                    .map(CartItem::getSellingPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalMrpPrice = items.stream()
                    .map(CartItem::getMrpPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int totalItem = items.stream().mapToInt(CartItem::getQuantity).sum();

            Order createdOrder = new Order();
            createdOrder.setUser(user);
            createdOrder.setSellerId(sellerId);
            createdOrder.setTotalMrpPrice(totalMrpPrice);
            createdOrder.setTotalSellingPrice(totalOrderPrice);
            createdOrder.setDiscount(totalMrpPrice.subtract(totalOrderPrice));
            createdOrder.setTotalItem(totalItem);
            createdOrder.setShippingAddress(address);
            createdOrder.setOrderStatus(OrderStatus.PENDING);
            createdOrder.getPaymentDetails().setStatus(PaymentStatus.PENDING);
            Order savedOrder=orderRepository.save(createdOrder);
            orders.add(savedOrder);

            List<OrderItem> orderItems=new ArrayList<>();

            for(CartItem item:items){
                OrderItem orderItem=new OrderItem();
                orderItem.setOrder(savedOrder);
                orderItem.setMrpPrice(item.getMrpPrice());
                orderItem.setProduct(item.getProduct());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setSize(item.getSize());
                orderItem.setUserId(item.getUserId());
                orderItem.setSellingPrice(item.getSellingPrice());

                savedOrder.getOrderItems().add(orderItem);

                OrderItem savedOrderItem=orderItemRepository.save(orderItem);
                orderItems.add(savedOrderItem);
            }
        }
        cartItemRepository.deleteAll(new ArrayList<>(cart.getCartItems()));
        cart.getCartItems().clear();
        cart.setDiscount(BigDecimal.ZERO);
        cart.setCouponCode(null);
        cart.setTotalMrpPrice(BigDecimal.ZERO);
        cart.setTotalSellingPrice(BigDecimal.ZERO);
        cart.setTotalItems(0);
        cartRepository.save(cart);
        return orders;
    }

    @Override
    public Order findOrderById(Long id) throws Exception {
        return orderRepository.findById(id).orElseThrow(()->new Exception("order not found.."));
    }

    @Override
    public List<Order> userOrderHistory(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public List<Order> sellersOrder(Long sellerId) {
        return orderRepository.findBySellerId(sellerId);
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus orderStatus) throws Exception {
        Order order=findOrderById(orderId);
        order.setOrderStatus(orderStatus);
        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(Long orderId, User user) throws Exception {
        Order order=findOrderById(orderId);
        if(!user.getId().equals(order.getUser().getId())){
            throw new Exception("you dont have access to this order");
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    @Override
    public OrderItem getOrderItemById(Long id) throws Exception {
        return orderItemRepository.findById(id).orElseThrow(()->new Exception("order item does not exist"));
    }
}
