package org.delcom.ifs25004pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25004pbo2026springbootcashflow.domain.entity.Transaction;

import java.math.BigDecimal;

/** Port spesifik transaksi dengan penomoran ID otomatis. */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {

    /** Menyimpan transaksi bertipe pemasukan. */
    Transaction saveIncome(String description, BigDecimal amount);

    /** Menyimpan transaksi bertipe pengeluaran. */
    Transaction saveExpense(String description, BigDecimal amount);
}
