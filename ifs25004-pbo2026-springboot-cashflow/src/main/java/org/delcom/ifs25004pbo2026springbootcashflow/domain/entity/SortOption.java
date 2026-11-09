package org.delcom.ifs25004pbo2026springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan transaksi. Setiap opsi membawa comparator-nya sendiri
 * sehingga logika pengurutan terpusat di domain.
 */
public enum SortOption {
    /** Jumlah terkecil lebih dulu. */
    AMOUNT_ASC("Jumlah terkecil", Comparator.comparing(Transaction::getAmount)),

    /** Jumlah terbesar lebih dulu. */
    AMOUNT_DESC("Jumlah terbesar", Comparator.comparing(Transaction::getAmount).reversed()),

    /** Pemasukan ditampilkan lebih dulu. */
    INCOME_FIRST("Pemasukan dulu", Comparator.comparingInt((Transaction t) -> t.isIncome() ? 0 : 1)),

    /** Pengeluaran ditampilkan lebih dulu. */
    EXPENSE_FIRST("Pengeluaran dulu", Comparator.comparingInt((Transaction t) -> t.isIncome() ? 1 : 0));

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String label() {
        return label;
    }

    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
