package library;

import java.time.LocalDate;
import java.util.ArrayList;

public class LibraryController {
// ATTRIBUTES ====================================
	private BookCatalog allBooks;
	private LoanCatalog allLoans;
	private UserCatalog allUsers;
	private ReminderCatalog allReminders;
	private Receipt currentReceipt;
	private Patron currentPatron;
	
// GETTERS/SETTERS ===============================


// CONSTRUCTORS===================================
	public LibraryController(BookCatalog b, LoanCatalog l, UserCatalog u, ReminderCatalog r) {
		allBooks = b;
		allLoans = l;
		allUsers = u;
		allReminders = r;
	};
	
// METHODS =======================================
	public void startCheckout() {
		boolean addBooks = true;
		
		String msg1 = "Please scan your library card to being checkout."; // GUI DISPLAY. Text is placeholder
		
		int cardNum = 1001; // GUI INPUT. Need to get library card number from GUI. 1001 is a placeholder ID
		
		
		if(!allUsers.verifyUser(cardNum)) {
			String msg2 = "Library Card number not found. Please see the front desk."; // GUI DISPLAY. Text is placeholder
		} else {
			currentPatron = allUsers.searchByCardNumber(cardNum).get();
			currentReceipt = new Receipt();

			String reminderMsg = checkReminders();
			if(hasReminders(reminderMsg)) {
				// GUI DISPLAY. Show reminderMsg (e.g. a JOptionPane) before scanning starts.
			}

			String msg3 = "Please begin scanning your books."; // GUI DISPLAY. Text is placeholder
			
			while(addBooks) {
				int bookID = 10001;// GUI INPUT. Either scan a new book or Finish Checkout. Finish Checkout will set addBooks to false. 
				
				Book b = allBooks.searchByBarcode(bookID);
				
				if(b == null) {
					// GUI OUTPUT. Book not found message.
				} else if (b != null && !b.isAvailable()) {
					// GUI OUTPUT. Book not available message.
				} else {
					currentReceipt.addBook(b);
					// GUI OUTPUT. Update book list. Use Receipt printReceipt should give String of all books with title, author, ISBN, and Barcode
				}
				addBooks = false; // Placeholder to prevent infinite loop. Remove once Finish Checkout and Scan Book is connected
			}
		}
	}
	
	public void finishCheckout() {
		allLoans.createLoan(currentReceipt, currentPatron);
		allBooks.checkOutBooks(currentReceipt);
		
		// GUI OUTPUT+INPUT. Ask for printed receipt. If yes, digital confirmation of receipt being printed. If no, confirmation that receipt was emailed
		
		// GUI OUTPUT. Check out complete
	}
	
	/**
	 * Due-date reminders for the patron whose card was just scanned.
	 * Brings reminders up to date from the LoanCatalog, "emails" any new ones,
	 * and returns the text to show the patron. Calling this on every card scan
	 * stands in for a nightly job (out of scope); it never creates duplicate
	 * reminders or sends one twice.
	 *
	 * Returns "" if no patron is signed in, or "[name] has no reminders."
	 * if the patron has nothing due soon, due today, or overdue.
	 */
	public String checkReminders() {
		return checkReminders(LocalDate.now());
	}

	// Same as above, but lets testing/demo pretend it's a different day
	// (loans are due 14 days out, so e.g. LocalDate.now().plusDays(13) = due tomorrow).
	public String checkReminders(LocalDate today) {
		if(currentPatron == null) {return "";}

		allReminders.generateReminders(allLoans, today);
		allReminders.sendPendingReminders(today);
		return allReminders.printRemindersForPatron(currentPatron);
	}

	// True if checkReminders() returned something worth showing the patron.
	public static boolean hasReminders(String reminderMsg) {
		return !reminderMsg.isEmpty() && !reminderMsg.endsWith("has no reminders.");
	}
}