package library;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A UI-only Swing prototype for the Library Checkout System.
 * Backend validation, barcode lookup, loan creation and receipt delivery
 * are not implemented here. Connect this view to the team's controller.
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

    public PatronGUIStarter() {
        super("Library Checkout System - Patron Kiosk (UI Prototype)");
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
        String cardNumber = cardField.getText().trim();
        if (cardNumber.isEmpty()) {
            setStatus("Enter or scan a library card number first.");
            return;
        }
        // TODO: Call LibraryController -> UserCatalog for real validation.
        setStatus("Card " + cardNumber + " entered. Validation is not connected yet.");
        JOptionPane.showMessageDialog(this,
                "The interface is working, but card validation must be connected to the team's backend.",
                "Backend not connected", JOptionPane.INFORMATION_MESSAGE);
        // Keep book checkout disabled until a real validation result is available.
    }

    private void onScanBook() {
        // This is intentionally disabled until card validation is connected.
        String barcode = bookField.getText().trim();
        if (barcode.isEmpty())
            return;
        // TODO: Use LibraryController -> BookCatalog and only add available books.
        scannedBooks.addElement(barcode);
        bookField.setText("");
        finishButton.setEnabled(!scannedBooks.isEmpty());
    }

    private void onFinishCheckout() {
        // TODO: Use CheckoutSystem/LoanCatalog, email a receipt,
        // and ask whether a printed receipt is wanted.
        JOptionPane.showMessageDialog(this,
                "Checkout completion is not connected yet.",
                "Backend not connected", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetSession() {
        cardField.setText("");
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
        SwingUtilities.invokeLater(() -> new PatronGUIStarter().setVisible(true));
    }
}
