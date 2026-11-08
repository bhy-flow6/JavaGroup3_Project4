package library;

public class BookReceiptTester {
	public static void main() {
		BookCatalog cat = new BookCatalog();
		
		Book b1 = cat.searchByBarcode(1001); 
		Book b2 = cat.searchByBarcode(1003); 
		Book b3 = cat.searchByBarcode(1004); 
		
		Receipt r1 = new Receipt(true);
		Receipt r2 = new Receipt();
		r2.addBook(b2);
		r2.addBook(b1);
		r2.addBook(b3);
		
		System.out.println(cat.searchByBarcode(1001).isAvailable());
		System.out.println(cat.searchByBarcode(1003).isAvailable());
		System.out.println(cat.searchByBarcode(1004).isAvailable());
		cat.checkOutBooks(r2);
		System.out.println("======");
		System.out.println(cat.searchByBarcode(1001).isAvailable());
		System.out.println(cat.searchByBarcode(1003).isAvailable());
		
		System.out.println(r2.printReceipt());
		
	}
}
