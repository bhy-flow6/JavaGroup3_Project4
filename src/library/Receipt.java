package library;

import java.time.LocalDate;

/* Receipt is the "shopping cart" of for checkouts, storing Books until the checkout is finalized
*  Receipts are no longer used after each checkout process, all relevant information is stored
*  in Loan
*/ 

import java.util.ArrayList;

public class Receipt {
// ATTRIBUTES ====================================
	private static int LAST_ID = 0;
	private int receiptID;
	private LocalDate createDate;
	private ArrayList<Book> cart;
	
// GETTERS/SETTERS ===============================
	public int getReceiptID() {
		return receiptID;
	}
	
	public int getReceiptSize() {
		return cart.size();
	}
	
	public Book getReceiptBook(int i) {		
		return cart.get(i);
	}
	
	public ArrayList<Book> getCart(){
		return cart;
	}
	
// CONSTRUCTORS===================================
	public Receipt() {
		LAST_ID++;
		receiptID = LAST_ID;
		cart = new ArrayList<>();
	}
	
	// Testing constructor with preloaded books for testing
	public Receipt(boolean test) {
		LAST_ID++;
		receiptID = LAST_ID;
		cart = new ArrayList<>();
		
		cart.add(new Book("Object Oriented Software Engineering", "David Kung", "978-0-07-3376257", 1001));
		cart.add(new Book("Software Engineering", "David Kung", "978-1-26-4556953", 1002));
	}
	
// METHODS =======================================
	public void addBook (Book b) {
		if (b.isAvailable()) {
			cart.add(b);
		} 
	}
	
	public String printReceipt() {
		createDate = LocalDate.now();
		String msg = "Receipt as of " + createDate + "\n";
		for (Book b : cart) {
			msg += b.printInfo() + "\n";
		}
		
		return msg;
	}
}
