package library;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

/**
 * Stores information about a loan, to include the loaner, books loaned, and when loan is due.
 * All books within a loan share the same due date for simplicity.
 */

public class Loan {
// ATTRIBUTES ====================================
	private int loanID;
	private boolean loanOpen;
	private Patron loaner;
	private LocalDate dueDate;
	private LocalDate issueDate;
	private ArrayList<Book> loanedBooks; // As duplicate of the ArrayList made in the Receipt for construction
	 
// GETTERS/SETTERS ===============================
	public int getLoanID() {return loanID;}
	public Patron getLoaner() {return loaner;}
	public LocalDate getDueDate() {return dueDate;}
	public LocalDate getIssueDate() {return issueDate;}
	public boolean isOpen() {return loanOpen;}
	
// CONSTRUCTORS===================================
	public Loan(Receipt r, Patron u) {
		loanOpen = true;
		loaner = u;
		loanedBooks = new ArrayList<>(r.getCart());
		issueDate = LocalDate.now();
		dueDate = issueDate.plusDays(14); // Default 2 week loan
	}
	
// METHODS =======================================
	public int daysUntilDue() {
		if(loanOpen) {
			return Period.between(issueDate, dueDate).getDays();
		}
		else {
			return 0;
		}
	}
	 
	public void extendDueDate(int days) {
		if(loanOpen) {dueDate = dueDate.plusDays(days);}
	}
	
	public int booksStillLoaned() {
		int counter = 0;
		for(Book b: loanedBooks) {
			if(b.isCheckedOut()) {counter++;}
		}
		
		if(counter == 0) {closeLoan();}
		
		return counter;
	}
	
	public void closeLoan() {
		loanOpen = false;
	}
	 
	public String printLoanedReceipt() {
		String msg = "====================================\n";
		msg += "Loan Issued: " + issueDate + "\n";
		msg += "Loan Due: " + dueDate + "(" + daysUntilDue() + " Days) \n \n";
		msg += "Books Loaned: \n";
		msg += "------------------------------------\n";
		for (Book b : loanedBooks) {
			msg += b.printInfo() + "\n";
		}
		msg += "------------------------------------\n";
		
		return msg;
	 }
}