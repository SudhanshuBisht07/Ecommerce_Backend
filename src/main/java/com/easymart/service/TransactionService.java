package com.easymart.service;

import com.easymart.model.Order;
import com.easymart.model.Seller;
import com.easymart.model.Transaction;

import java.util.List;

public interface TransactionService {
    Transaction createTransaction(Order order) throws Exception;
    List<Transaction> getTransactionBySellerId(Seller seller);
    List<Transaction> getAllTransactions();
}
