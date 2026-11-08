package library;

public class Book {
// ATTRIBUTES ====================================
	 private String title;
	 private String author;
	 private String isbn;
	 private String status; // Can only be Available, Checked Out, or Unsorted
	 private int barcodeID;
	 
// GETTERS/SETTERS ===============================
	 public String getTitle() {return title;}
	 public String getAuthor() {return author;}
	 public String getISBN() {return isbn;}
	 public int getBarcodeID() {return barcodeID;}
	 
// CONSTRUCTORS===================================
	 public Book(String t, String a, String n, int b) {
		 title = t;
		 author = a;
		 isbn = n;
		 barcodeID = b;
		 status = "Available";
	 }
	 
	// Alternative constructor for testing interactions with unavailable books.
	 public Book(String t, String a, String n, int b, String s) { 
		 title = t;
		 author = a;
		 isbn = n;
		 barcodeID = b;
		 status = s;
	 }
// METHODS =======================================

	 public void checkOut() {
		 status = "Checked Out";
	 }
	 
	 public void checkIn() {
		 status = "Unsorted";
	 }
	 
	 public void restock() {
		 status = "Available";
	 }
	 
	 public boolean isAvailable () {
		 if (status == "Available") { 
			 return true;
		 } else {
			 return false;
		 }
	 }
	 
	 public String printInfo () {
		 return title + " by " + author + " (ISBN: " + isbn + " / Barcode: " + barcodeID + ")";
	 }
}
