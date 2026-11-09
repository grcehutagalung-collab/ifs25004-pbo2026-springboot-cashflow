package org.delcom.ifs25004pbo2026springbootcashflow.usecase;

import org.delcom.ifs25004pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25004pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25004pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25004pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25004pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25004pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FinanceUseCaseTest {

    private FinanceUseCase useCase;
    private final FinancePresenter presenter = new FinancePresenter();

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void balanceFollowsIncomeAndExpense() throws Exception {
        useCase.addIncome("Gaji", "5000000");
        useCase.addExpense("Makan", "25000");
        assertEquals(0, new BigDecimal("4975000").compareTo(useCase.getBalance()));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(useCase.getBalance()));
    }

    @Test
    void balanceStaysConsistentAfterDelete() throws Exception {
        Transaction income = useCase.addIncome("Gaji", "1000");
        useCase.addExpense("Kopi", "200");
        useCase.removeTransaction(income.getId());
        assertEquals(0, new BigDecimal("-200").compareTo(useCase.getBalance()));
    }

    @Test
    void validationRejectsBadInput() {
        assertThrows(ValidationException.class, () -> useCase.addIncome("  ", "100"));
        assertThrows(ValidationException.class, () -> useCase.addIncome(null, "100"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("Makan", "-5"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("Makan", "0"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("Makan", "abc"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("Makan", ""));
        assertThrows(ValidationException.class, () -> useCase.addExpense("Makan", null));
    }

    @Test
    void removeUnknownIdThrows() {
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(99));
    }

    @Test
    void searchIsCaseInsensitiveAndDoesNotMutate() throws Exception {
        useCase.addIncome("Gaji Bulanan", "100");
        useCase.addExpense("Makan siang", "10");
        assertEquals(1, useCase.searchTransactions("GAJI").size());
        assertEquals(2, useCase.getAllTransactions().size());
        assertEquals(2, useCase.searchTransactions("").size());
    }

    @Test
    void sortOptionsWork() throws Exception {
        useCase.addExpense("A", "50");
        useCase.addIncome("B", "10");
        useCase.addIncome("C", "300");

        assertEquals("B", useCase.sortTransactions(SortOption.AMOUNT_ASC).get(0).getDescription());
        assertEquals("C", useCase.sortTransactions(SortOption.AMOUNT_DESC).get(0).getDescription());
        assertTrue(useCase.sortTransactions(SortOption.INCOME_FIRST).get(0).isIncome());
        assertFalse(useCase.sortTransactions(SortOption.EXPENSE_FIRST).get(0).isIncome());
        List<Transaction> combined = useCase.getTransactions("c", SortOption.AMOUNT_ASC);
        assertEquals(1, combined.size());
    }

    @Test
    void presenterFormat() throws Exception {
        Transaction t = useCase.addIncome("Gaji", "1000000");
        assertEquals("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.format(t));
    }

    @Test
    void findAllReturnsDefensiveCopy() throws Exception {
        useCase.addIncome("Gaji", "100");
        useCase.getAllTransactions().clear();
        assertEquals(1, useCase.getAllTransactions().size());
    }
}
