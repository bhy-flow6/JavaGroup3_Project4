package library;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Patron-facing Swing kiosk for the Library Checkout System MVP.
 * Uses existing catalogs directly; LibraryController is not modified.
 * Printing/email delivery is simulated by on-screen confirmation only.
 */
public class PatronGUIStarter extends JFrame {
    private final JTextField cardField = new JTextField(20);
    private final JTextField bookField = new JTextField(20);
    private final DefaultListModel<String> scannedBooks = new DefaultListModel<>();
    private final JList<String> bookList = new JList<>(scannedBooks);
    private final JLabel statusLabel = new JLabel("Please scan your library card to begin checkout.");
    private final JButton validateButton = new JButton("Validate Card");
    private final JButton addBookButton = new JButton("Scan Book");
    private final JButton finishButton = new JButton("Finish Checkout");
    private final JButton resetButton = new JButton("Start Over");

    private final UserCatalog userCatalog;
    private final BookCatalog bookCatalog;
    private final LoanCatalog loanCatalog;
    private final ReminderCatalog reminderCatalog;

    private Patron currentPatron;
    private Receipt currentReceipt;

    public PatronGUIStarter(UserCatalog users, BookCatalog books,
            LoanCatalog loans, ReminderCatalog reminders) {
        super("Library Checkout System - Patron Kiosk");
        this.userCatalog = users;
        this.bookCatalog = books;
        this.loanCatalog = loans;
        this.reminderCatalog = reminders;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(660, 490));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(18, 22, 18, 22));
        setContentPane(root);

        JLabel heading = new JLabel("Library Self-Checkout");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 24f));
        root.add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        root.add(center, BorderLayout.CENTER);

        JPanel cardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cardPanel.setBorder(BorderFactory.createTitledBorder("1. Scan Library Card"));
        cardPanel.add(new JLabel("Card number:"));
        cardPanel.add(cardField);
        cardPanel.add(validateButton);
        center.add(cardPanel);

        JPanel booksPanel = new JPanel(new BorderLayout(8, 8));
        booksPanel.setBorder(BorderFactory.createTitledBorder("2. Scan Books"));
        JPanel bookEntry = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bookEntry.add(new JLabel("Book barcode:"));
        bookEntry.add(bookField);
        bookEntry.add(addBookButton);
        booksPanel.add(bookEntry, BorderLayout.NORTH);
        bookList.setVisibleRowCount(7);
        booksPanel.add(new JScrollPane(bookList), BorderLayout.CENTER);
        center.add(booksPanel);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        statusLabel.setBorder(new EmptyBorder(6, 0, 8, 0));
        bottom.add(statusLabel, BorderLayout.NORTH);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(resetButton);
        actions.add(finishButton);
        bottom.add(actions, BorderLayout.SOUTH);
        root.add(bottom, BorderLayout.SOUTH);

        addBookButton.setEnabled(false);
        finishButton.setEnabled(false);
        bookField.setEnabled(false);

        validateButton.addActionListener(e -> onValidateCard());
        addBookButton.addActionListener(e -> onScanBook());
        finishButton.addActionListener(e -> onFinishCheckout());
        resetButton.addActionListener(e -> resetSession());
        cardField.addActionListener(e -> onValidateCard());
        bookField.addActionListener(e -> onScanBook());
    }

    private void onValidateCard() {
        String cardText = cardField.getText().trim();
        if (cardText.isEmpty()) {
            setStatus("Enter or scan a library card number first.");
            return;
        }

        CardStatus status = userCatalog.validateCard(cardText);
        Patron patron = null;
        try {
            patron = userCatalog.searchByCardNumber(Integer.parseInt(cardText)).orElse(null);
        } catch (NumberFormatException e) {
            // Invalid input will be rejected by card validation.
        }

        String message = status.getMessage(patron);
        setStatus(message);

        if (!status.canCheckOut() || patron == null) {
            JOptionPane.showMessageDialog(this, message,
                    "Card Not Accepted", JOptionPane.ERROR_MESSAGE);
            cardField.selectAll();
            cardField.requestFocusInWindow();
            return;
        }

        currentPatron = patron;
        currentReceipt = new Receipt();
        cardField.setEnabled(false);
        validateButton.setEnabled(false);
        bookField.setEnabled(true);
        addBookButton.setEnabled(true);

        // Generate reminders from the same LoanCatalog used by checkout.
        reminderCatalog.generateReminders(loanCatalog);
        String reminders = reminderCatalog.printRemindersForPatron(patron);
        if (!reminders.endsWith("has no reminders.") && !reminders.isBlank()) {
            JOptionPane.showMessageDialog(this, reminders,
                    "Library Due-Date Reminders", JOptionPane.INFORMATION_MESSAGE);
        }

        if (status == CardStatus.EXPIRING_SOON) {
            JOptionPane.showMessageDialog(this, message,
                    "Card Expiring Soon", JOptionPane.WARNING_MESSAGE);
        }
        setStatus("Card accepted. Scan a book barcode to continue.");
        bookField.requestFocusInWindow();
    }

    private void onScanBook() {
        if (currentPatron == null || currentReceipt == null) {
            setStatus("Please validate a library card first.");
            return;
        }
        String barcodeText = bookField.getText().trim();
        int barcode;
        try {
            barcode = Integer.parseInt(barcodeText);
        } catch (NumberFormatException e) {
            setStatus("Enter a numeric book barcode.");
            return;
        }

        Book book = bookCatalog.searchByBarcode(barcode);
        if (book == null) {
            setStatus("Book not found. Please see the front desk.");
            return;
        }
        if (!book.isAvailable()) {
            setStatus("This book is already checked out.");
            return;
        }
        for (Book scanned : currentReceipt.getCart()) {
            if (scanned.getBarcodeID() == barcode) {
                setStatus("This book has already been scanned.");
                return;
            }
        }

        currentReceipt.addBook(book);
        // printInfo() is provided by the team's Book class.
        scannedBooks.addElement(book.printInfo().replace('\n', ' ').trim());
        bookField.setText("");
        finishButton.setEnabled(currentReceipt.getReceiptSize() > 0);
        setStatus("Book added. Scan another or press Finish Checkout.");
        bookField.requestFocusInWindow();
    }

    private void onFinishCheckout() {
        if (currentPatron == null || currentReceipt == null ||
                currentReceipt.getReceiptSize() == 0) {
            setStatus("Please validate a card and scan at least one book.");
            return;
        }

        try {
            // Record the loan and update the shared BookCatalog.
            loanCatalog.createLoan(currentReceipt, currentPatron);
            bookCatalog.checkOutBooks(currentReceipt);

            String receiptText = currentReceipt.printReceipt();
            int printChoice = JOptionPane.showConfirmDialog(
                    this, "Would you like to view a printable receipt?",
                    "Receipt", JOptionPane.YES_NO_OPTION);

            if (printChoice == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, receiptText,
                        "Receipt Preview", JOptionPane.INFORMATION_MESSAGE);
            }

            JOptionPane.showMessageDialog(this,
                    "Checkout recorded successfully.\n" +
                            "Email delivery and physical printing are not implemented in this prototype.",
                    "Checkout Complete", JOptionPane.INFORMATION_MESSAGE);
            resetSession();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this,
                    "Checkout could not be completed: " + e.getMessage(),
                    "Checkout Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetSession() {
        currentPatron = null;
        currentReceipt = null;
        cardField.setText("");
        cardField.setEnabled(true);
        validateButton.setEnabled(true);
        bookField.setText("");
        scannedBooks.clear();
        bookField.setEnabled(false);
        addBookButton.setEnabled(false);
        finishButton.setEnabled(false);
        setStatus("Please scan your library card to begin checkout.");
        cardField.requestFocusInWindow();
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserCatalog users = UserCatalog.withSampleData();
            BookCatalog books = new BookCatalog();
            LoanCatalog loans = new LoanCatalog();
            ReminderCatalog reminders = new ReminderCatalog();
            new PatronGUIStarter(users, books, loans, reminders).setVisible(true);
        });
    }
}
