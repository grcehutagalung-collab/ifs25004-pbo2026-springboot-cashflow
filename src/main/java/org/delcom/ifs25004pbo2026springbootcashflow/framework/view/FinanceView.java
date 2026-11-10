package org.delcom.ifs25004pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25004pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25004pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25004pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25004pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25004pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25004pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25004pbo2026springbootcashflow.usecase.FinanceUseCase;

import java.util.Comparator;
import java.util.List;

/**
 * Tampilan web Vaadin aplikasi Cashflow.
 * Pengurutan: klik header kolom grid atau pilih SortOption pada toolbar.
 */
@Route("")
@PageTitle("Cashflow")
public class FinanceView extends VerticalLayout {

    private static final String EMPTY_ALL = "Data transaksi belum tersedia!";
    private static final String EMPTY_SEARCH = "Transaksi tidak ditemukan!";

    private final FinanceUseCase useCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final Select<SortOption> sortSelect = new Select<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span(EMPTY_ALL);

    private final Span countValue = new Span();
    private final Span incomeValue = new Span();
    private final Span expenseValue = new Span();
    private final Span balanceValue = new Span();

    public FinanceView(FinanceUseCase useCase, FinancePresenter presenter) {
        this.useCase = useCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureGrid();
        configureToolbarFields();
        add(new H1("Cashflow"), buildSummaryCards(), buildToolbar(), statusLabel, grid);
        refresh();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Atribut lang pada <html> (penilaian Accessibility & SEO Lighthouse).
        attachEvent.getUI().getPage().executeJs("document.documentElement.lang = 'id';");
    }

    // ---------- ringkasan ----------

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout cards = new HorizontalLayout(
                card("Total Transaksi", countValue, "#E0E7FF", "#3730A3"),
                card("Total Pemasukan", incomeValue, "#DCFCE7", "#166534"),
                card("Total Pengeluaran", expenseValue, "#FEE2E2", "#991B1B"),
                card("Saldo", balanceValue, "#FEF3C7", "#92400E")
        );
        cards.setWidthFull();
        cards.getStyle().set("align-items", "stretch").set("flex-wrap", "wrap");
        return cards;
    }

    private Div card(String label, Span value, String background, String accent) {
        Span caption = new Span(label);
        caption.getStyle().set("display", "block").set("font-size", "0.85rem")
                .set("font-weight", "500").set("color", accent);
        value.getStyle().set("display", "block").set("margin-top", "0.3rem")
                .set("font-size", "1.5rem").set("font-weight", "700").set("color", accent);
        Div box = new Div(caption, value);
        box.getStyle().set("flex", "1").set("min-width", "180px")
                .set("background", background)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "10px").set("padding", "0.9rem 1rem");
        return box;
    }

    private void updateSummary() {
        countValue.setText(String.valueOf(useCase.countTransactions()));
        incomeValue.setText(presenter.formatMoney(useCase.getTotalIncome()));
        expenseValue.setText(presenter.formatMoney(useCase.getTotalExpense()));
        balanceValue.setText(presenter.formatMoney(useCase.getBalance()));
    }

    // ---------- grid & toolbar ----------

    private void configureGrid() {
        grid.addColumn(Transaction::getId).setHeader("ID").setKey("id")
                .setAutoWidth(true).setFlexGrow(0).setSortable(true)
                .setComparator(Transaction::getId);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setKey("description")
                .setFlexGrow(1).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));
        grid.addColumn(t -> presenter.formatMoney(t.getAmount())).setHeader("Jumlah").setKey("amount")
                .setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getAmount));
        grid.addColumn(presenter::formatType).setHeader("Tipe").setKey("type")
                .setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));
        grid.addColumn(presenter::formatCreatedAt).setHeader("Waktu").setKey("createdAt")
                .setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));
        grid.addComponentColumn(this::createActionButtons).setHeader("Tindakan").setKey("actions")
                .setAutoWidth(true).setFlexGrow(0);
        grid.setEmptyStateComponent(emptyState);
        grid.setSizeFull();
        grid.setMinHeight("320px");
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button delete = new Button("Hapus", VaadinIcon.TRASH.create(), e -> openDeleteDialog(transaction));
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        delete.getStyle().set("--lumo-error-text-color", "#B91C1C");
        delete.setAriaLabel("Hapus transaksi nomor " + transaction.getId());
        return new HorizontalLayout(delete);
    }

    private void configureToolbarFields() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setAriaLabel("Cari keterangan transaksi");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);

        sortSelect.setLabel("Urutkan");
        sortSelect.setPlaceholder("Urutkan...");
        sortSelect.setItems(SortOption.values());
        sortSelect.setItemLabelGenerator(SortOption::label);
        sortSelect.addValueChangeListener(e -> refresh());
    }

    private HorizontalLayout buildToolbar() {
        Button search = new Button("Cari", VaadinIcon.SEARCH.create(), e -> refresh());
        Button reload = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), e -> resetFilters());
        Button income = new Button("Tambah Pemasukan", VaadinIcon.PLUS_CIRCLE.create(),
                e -> openAddDialog(TransactionType.INCOME));
        income.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        income.getStyle().set("--lumo-success-color", "#166534");
        Button expense = new Button("Tambah Pengeluaran", VaadinIcon.MINUS_CIRCLE.create(),
                e -> openAddDialog(TransactionType.EXPENSE));
        expense.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        expense.getStyle().set("--lumo-error-color", "#B91C1C");

        HorizontalLayout toolbar = new HorizontalLayout(searchField, search, sortSelect, reload, income, expense);
        toolbar.setWidthFull();
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        toolbar.setFlexGrow(1, searchField);
        toolbar.getStyle().set("flex-wrap", "wrap");
        return toolbar;
    }

    // ---------- dialog ----------

    private void openAddDialog(TransactionType type) {
        boolean income = type == TransactionType.INCOME;

        TextField description = new TextField("Keterangan");
        description.setWidthFull();
        TextField amount = new TextField("Jumlah (Rp)");
        amount.setPlaceholder("contoh: 1000000");
        amount.setWidthFull();

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(income ? "Tambah Pemasukan" : "Tambah Pengeluaran");
        dialog.setWidth("440px");
        dialog.setMaxWidth("95vw");
        dialog.add(new VerticalLayout(description, amount));

        Button save = new Button("Simpan", VaadinIcon.CHECK.create(), e -> {
            try {
                Transaction created = income
                        ? useCase.addIncome(description.getValue(), amount.getValue())
                        : useCase.addExpense(description.getValue(), amount.getValue());
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refresh();
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.getStyle().set("--lumo-primary-color", "#1D4ED8");
        Button cancel = new Button("Batal", VaadinIcon.CLOSE.create(), e -> dialog.close());
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Hapus Transaksi");
        dialog.add(new Paragraph("Yakin ingin menghapus transaksi \"" + transaction.getDescription()
                + "\" (ID " + transaction.getId() + ")?"));

        Button confirm = new Button("Hapus", VaadinIcon.TRASH.create(), e -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirm.getStyle().set("--lumo-error-color", "#B91C1C");
        Button cancel = new Button("Batal", VaadinIcon.CLOSE.create(), e -> dialog.close());
        dialog.getFooter().add(cancel, confirm);
        dialog.open();
    }

    private void deleteTransaction(Transaction transaction) {
        try {
            useCase.removeTransaction(transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        }
        refresh();
    }

    // ---------- refresh ----------

    private void resetFilters() {
        searchField.clear();
        sortSelect.clear();   // memicu refresh() lewat listener bila nilainya berubah
        refresh();
    }

    private void refresh() {
        String keyword = searchField.getValue();
        boolean searching = keyword != null && !keyword.isBlank();
        List<Transaction> items = useCase.getTransactions(keyword, sortSelect.getValue());

        emptyState.setText(searching ? EMPTY_SEARCH : EMPTY_ALL);
        grid.setItems(items);
        updateSummary();
        statusLabel.setText(presenter.formatBalance(useCase.getBalance())
                + (searching ? "  |  Hasil pencarian: \"" + keyword.trim() + "\" (" + items.size() + ")" : ""));
    }

    private void showSuccess(String message) {
        Notification n = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification n = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
