package library;

import java.time.LocalDate;

public class BookReceiptTester {
	public static void main() {
		BookCatalog cat = new BookCatalog();
		
		Book b1 = cat.searchByBarcode(1001); 
		Book b2 = cat.searchByBarcode(1003); 
		Book b3 = cat.searchByBarcode(1004); 
		
		Patron u1 = new Patron (123, "TestUser", "TestUser@mail.com",1001, LocalDate.now().plusYears(1));
		Receipt r2 = new Receipt();
		r2.addBook(b2);
		r2.addBook(b1);
		r2.addBook(b3);

		Loan l1 = new Loan(r2, u1);
		
		System.out.println(l1.daysUntilDue());
		System.out.println(l1.printLoanedReceipt());
		
		l1.extendDueDate(3);
		
		System.out.println(l1.printLoanedReceipt());
		
		
	}
}
