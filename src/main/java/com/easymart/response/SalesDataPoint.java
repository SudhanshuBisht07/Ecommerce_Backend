package com.easymart.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesDataPoint {
    private LocalDate date;
    private BigDecimal revenue;
    private BigDecimal profit;
    private int orders;
}
