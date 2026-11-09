package org.delcom.ifs25004pbo2026springbootcashflow.domain.entity;

/** Jenis transaksi keuangan. */
public enum TransactionType {
    INCOME("Pemasukan"),
    EXPENSE("Pengeluaran");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    /** Nama tampilan jenis transaksi. */
    public String label() {
        return label;
    }
}
