package com.easymart.service.impl;

import com.easymart.domain.PaymentStatus;
import com.easymart.model.Order;
import com.easymart.model.OrderItem;
import com.easymart.model.Seller;
import com.easymart.model.SellerReport;
import com.easymart.repository.OrderRepository;
import com.easymart.repository.SellerReportRepository;
import com.easymart.response.SalesDataPoint;
import com.easymart.service.SellerReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SellerReportServiceImpl implements SellerReportService {

    private final SellerReportRepository sellerReportRepository;
    private final OrderRepository orderRepository;

    @Override
    public SellerReport getSellerReport(Seller seller) {
        SellerReport report=sellerReportRepository.findBySeller_Id(seller.getId());

        if(report==null){
            report=new SellerReport();
            report.setSeller(seller);
            report.setTotalOrders(0);
            report.setTotalSales(BigDecimal.ZERO);
            report.setTotalEarnings(BigDecimal.ZERO);
            report.setTotalRefunds(BigDecimal.ZERO);
            report.setCancelledOrders(0);
            return sellerReportRepository.save(report);
        }
        return report;
    }

    @Override
    public SellerReport updateSellerReport(SellerReport sellerReport) {
        return sellerReportRepository.save(sellerReport);
    }

    @Override
    public List<SalesDataPoint> getSalesTimeline(Seller seller, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);

        Map<LocalDate, SalesDataPoint> byDate = new HashMap<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            byDate.put(date, new SalesDataPoint(date, BigDecimal.ZERO, BigDecimal.ZERO, 0));
        }

        List<Order> orders = orderRepository.findBySellerIdOrderByOrderDateDesc(seller.getId());

        for (Order order : orders) {
            if (order.getPaymentStatus() != PaymentStatus.COMPLETED) continue;
            if (order.getOrderDate() == null) continue;

            LocalDate orderDate = order.getOrderDate().toLocalDate();
            if (orderDate.isBefore(startDate)) continue;

            SalesDataPoint point = byDate.get(orderDate);
            if (point == null) continue;

            BigDecimal orderProfit = BigDecimal.ZERO;
            for (OrderItem item : order.getOrderItems()) {
                BigDecimal wholesalePrice = item.getWholesalePrice() != null
                        ? item.getWholesalePrice()
                        : BigDecimal.ZERO;
                // item.getSellingPrice() is already a line total
                // (unitPrice * quantity, set that way when the cart item
                // was created), while wholesalePrice is per-unit. The old
                // code subtracted a per-unit wholesale price from that line
                // total and then multiplied by quantity *again*, roughly
                // squaring the profit for any item with quantity > 1 —
                // this is why profit could end up higher than revenue.
                BigDecimal itemWholesaleTotal = wholesalePrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                orderProfit = orderProfit.add(item.getSellingPrice().subtract(itemWholesaleTotal));
            }

            point.setRevenue(point.getRevenue().add(order.getTotalSellingPrice()));
            point.setProfit(point.getProfit().add(orderProfit));
            point.setOrders(point.getOrders() + 1);
        }

        List<SalesDataPoint> timeline = new ArrayList<>(byDate.values());
        timeline.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        return timeline;
    }
}
