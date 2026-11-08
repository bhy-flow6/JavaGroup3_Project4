package library;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Holds every Book in the system, provides search functionality, and access to Book.
 */

public class LoanCatalog {
// ATTRIBUTES ====================================
	private LocalDate lastUpdated;
	private ArrayList<Loan> catalog;
	
// GETTERS/SETTERS ===============================
	public int getTotalCount() {return catalog.size();}
	public LocalDate getLastUpdate() {return lastUpdated;}

// CONSTRUCTORS===================================
	public LoanCatalog() {
		catalog = new ArrayList<>();
		lastUpdated = LocalDate.now();
	};
	
// METHODS =======================================
	public Loan searchByLoanID(int lID) {
		for (Loan l: catalog) {
			if(l.getLoanID() == lID) {return l;}
		}
		return null;
	}
	
	public ArrayList<Loan> searchByPatronID(int pID) { // Returns list of loans for a Patron
		ArrayList<Loan> holder = new ArrayList<>();
		for (Loan l: catalog) {
			if(l.getLoaner().getUserID() == pID) {
				holder.add(l);
			}
		}
		
		return holder;
	}
	
	public void createLoan (Receipt r, Patron p) {
		Loan l = new Loan(r, p);
		catalog.add(l);
	}
}
