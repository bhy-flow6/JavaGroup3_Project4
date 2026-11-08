/* Note: Give that this is limited functionality MVP, no database integration
 * has been included in this program. Instead, Books are stored in an ArrayList
 * which would be replaced with a database module in theoretical future iterations
 * of the program
 */
package library;

import java.time.LocalDate;
import java.util.ArrayList;


public class BookCatalog {
// ATTRIBUTES ====================================
	private LocalDate lastUpdated;
	private int totalCount;
	private ArrayList<Book> catalog;
	private static int lastID = 0;
	
// GETTERS/SETTERS ===============================
	public int getTotalCount() {
		return totalCount;
	}
// CONSTRUCTORS===================================
	public BookCatalog () { 
		catalog = new ArrayList<>();
		lastUpdated = LocalDate.now();
		
		// Manually created books
		catalog.add(new Book("Object Oriented Software Engineering", "David Kung", "978-0-07-3376257", 1001));
		catalog.add(new Book("Software Engineering", "David Kung", "978-1-26-4556953", 1002));
		catalog.add(new Book("The Lion, the Witch, and the Wardrobe", "CS Lewis", "978-0-06-4471046", 1003));
		catalog.add(new Book("The Fellowship of the Ring", "JRR Tolkien", "978-0-26-1102354", 1004, "Checked Out"));
				
	}

// METHODS =======================================
	public Book searchByBarcode(int bID) {
		for(Book b : catalog) {
			if (b.getBarcodeID() == bID) {
				return b;
			}
		}
		return null;
	}
	
	public Book searchByAuthor(String auth) {
		for(Book b : catalog) {
			if (b.getAuthor().toLowerCase() == auth.toLowerCase()) { //To lower case to make searching easier
				return b;
			}
		}
		return null;
	}
	
	public void addBookToCatalog (Book b) {
		catalog.add(b);
		lastUpdated = LocalDate.now();
	}
	
	public boolean checkAvailable(int bID) {
		Book b = searchByBarcode(bID);
		
		if(b != null) {
			return b.isAvailable();
		} else {
			return false;
		}
	}
	
	public void checkOutBooks(Receipt r) {
		for(int i = 0; i < r.getReceiptSize(); i++) {
			int bID = r.getReceiptBook(i).getBarcodeID();
			searchByBarcode(bID).checkOut();
		}
	}
}
