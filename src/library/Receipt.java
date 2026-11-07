package library;

import java.time.LocalDate;
import java.util.ArrayList;

public class Receipt {
// ATTRIBUTES ====================================
	private static int LAST_ID = 0;
	private int receiptID;
	private LocalDate issueDate;
	private ArrayList<Book> cart;
	
// GETTERS/SETTERS ===============================
	public int getReceiptID() {
		return receiptID;
	}
	
// CONSTRUCTORS===================================
	public Receipt() {
		LAST_ID++;
		receiptID = LAST_ID;
		cart = new ArrayList<>();
	}
	
	// Constructor with preloaded books for testing
	public Receipt(boolean test) {
		LAST_ID++;
		receiptID = LAST_ID;
		cart = new ArrayList<>();
		
		cart.add(new Book("Object Oriented Software Engineering", "David Kung", "978-0-07-3376257", 1));
	}
	
// METHODS =======================================
	public void addBook (Book b) {
		cart.add(b);
	}
	
	public String printReceipt() {
		String msg = "Receipt as of " + LocalDate.now() + "\n";
		for (Book b : cart) {
			msg += b.printInfo() + "\n";
		}
		
		return msg;
	}
}
