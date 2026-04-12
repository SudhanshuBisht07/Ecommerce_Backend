package com.easymart.service.impl;

import com.easymart.model.Order;
import com.easymart.model.Seller;
import com.easymart.model.Transaction;
import com.easymart.repository.SellerRepository;
import com.easymart.repository.TransactionRepository;
import com.easymart.service.TransactionService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final SellerRepository sellerRepository;
    @Override
    public Transaction createTransaction(Order order) throws Exception {
        Seller seller=sellerRepository.findById(order.getSellerId())
                .orElseThrow(() -> new Exception("Seller not found for order"));
        Transaction transaction=new Transaction();
        transaction.setSeller(seller);
        transaction.setCustomer(order.getUser());
        transaction.setOrder(order);

        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> getTransactionBySellerId(Seller seller) {
        return transactionRepository.findBySeller_Id(seller.getId());
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}
