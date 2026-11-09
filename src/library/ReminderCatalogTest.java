package library;

import java.time.LocalDate;
import java.util.List;

/**
 * Console tests for the reminder system (Reminder + ReminderCatalog).
 * Every loan starts today with the default 2-week due date, so the tests
 * move "today" forward instead of changing the loans.
 */
public class ReminderCatalogTest {

	private static int passed = 0;
	private static int failed = 0;

	public static void main(String[] args) {
		LocalDate start = LocalDate.now();
		UserCatalog users = UserCatalog.withSampleData();
		Patron alice = users.searchByCardNumber(1001).get();
		Patron ben = users.searchByCardNumber(1002).get();

		LoanCatalog loans = new LoanCatalog();
		Loan aliceLoan = loans.createLoan(receiptWith(2001, 2002), alice);
		Loan benLoan = loans.createLoan(receiptWith(2003), ben);
		LocalDate due = aliceLoan.getDueDate(); // start + 14 days

		ReminderCatalog reminders = new ReminderCatalog();

		System.out.println("=== Which reminder does a loan need? ===");
		check("due in 14 days -> none", ReminderCatalog.reminderTypeFor(aliceLoan, start), null);
		check("due in 4 days -> none", ReminderCatalog.reminderTypeFor(aliceLoan, due.minusDays(4)), null);
		check("due in 3 days -> DUE_SOON", ReminderCatalog.reminderTypeFor(aliceLoan, due.minusDays(3)), Reminder.Type.DUE_SOON);
		check("due in 1 day -> DUE_SOON", ReminderCatalog.reminderTypeFor(aliceLoan, due.minusDays(1)), Reminder.Type.DUE_SOON);
		check("due today -> DUE_TODAY", ReminderCatalog.reminderTypeFor(aliceLoan, due), Reminder.Type.DUE_TODAY);
		check("1 day late -> OVERDUE", ReminderCatalog.reminderTypeFor(aliceLoan, due.plusDays(1)), Reminder.Type.OVERDUE);
		System.out.println();

		System.out.println("=== Generating reminders over time ===");
		check("day 0: nothing to remind", reminders.generateReminders(loans, start).size(), 0);
		check("patron with no reminders", reminders.printRemindersForPatron(alice), "Alice Nguyen has no reminders.");

		List<Reminder> soon = reminders.generateReminders(loans, due.minusDays(2));
		check("2 days before due: one reminder per loan", soon.size(), 2);
		check("  ...of type DUE_SOON", soon.get(0).getType(), Reminder.Type.DUE_SOON);
		check("  ...starts Pending", soon.get(0).getStatus(), Reminder.PENDING);
		check("  ...addressed to the loaner", soon.get(0).getPatron(), alice);
		System.out.println("        message: " + soon.get(0).getMessage());

		check("running again same day adds nothing", reminders.generateReminders(loans, due.minusDays(2)).size(), 0);
		check("next day still DUE_SOON, no duplicate", reminders.generateReminders(loans, due.minusDays(1)).size(), 0);
		check("due date: DUE_TODAY reminders added", reminders.generateReminders(loans, due).size(), 2);
		check("total reminders so far", reminders.getTotalCount(), 4);
		System.out.println();

		System.out.println("=== Sending ===");
		check("4 pending reminders sent", reminders.sendPendingReminders(due), 4);
		check("sending again sends nothing", reminders.sendPendingReminders(due), 0);
		Reminder first = reminders.getAllReminders().get(0);
		check("status is Sent", first.getStatus(), Reminder.SENT);
		check("send date recorded", first.getSendDate(), due);
		first.sendReminder(due.plusDays(5));
		check("re-sending keeps original send date", first.getSendDate(), due);
		System.out.println();

		System.out.println("=== Returned loans ===");
		benLoan.closeLoan(); // Ben returns his book on the due date
		List<Reminder> late = reminders.generateReminders(loans, due.plusDays(3));
		check("3 days late: only Alice's open loan gets OVERDUE", late.size(), 1);
		check("  ...and it is Alice's", late.get(0).getPatron(), alice);
		System.out.println("        message: " + late.get(0).getMessage());
		check("Ben's reminders hidden once returned", reminders.printRemindersForPatron(ben), "Ben Ortiz has no reminders.");

		Loan aliceLoan2 = loans.createLoan(receiptWith(2004), alice);
		reminders.generateReminders(loans, aliceLoan2.getDueDate().minusDays(1));
		aliceLoan2.closeLoan(); // returned before the reminder went out
		check("pending reminder for a returned loan is not sent", reminders.sendPendingReminders(due.plusDays(4)), 1);
		System.out.println();

		System.out.println("=== What the kiosk shows Alice ===");
		String shown = reminders.printRemindersForPatron(alice);
		System.out.println("        " + shown.replace("\n", "\n        "));
		check("shows only the latest reminder per loan", shown.split("\n").length, 1);
		check("  ...which is the overdue one", shown.contains("overdue"), true);
		check("Alice has 4 reminders on record", reminders.getRemindersForPatron(alice).size(), 4);
		System.out.println();

		System.out.println("=== Extending a loan ===");
		LoanCatalog loans2 = new LoanCatalog();
		Loan extended = loans2.createLoan(receiptWith(2005), alice);
		extended.extendDueDate(7);
		ReminderCatalog reminders2 = new ReminderCatalog();
		check("old due date passes with no reminder", reminders2.generateReminders(loans2, start.plusDays(14)).size(), 0);
		check("reminder follows the new due date", reminders2.generateReminders(loans2, start.plusDays(21)).get(0).getType(), Reminder.Type.DUE_TODAY);
		System.out.println();

		System.out.println("=== Bad input ===");
		expectError("null LoanCatalog rejected", () -> reminders.generateReminders(null, start));
		expectError("null loan rejected", () -> new Reminder(null, Reminder.Type.DUE_SOON, start));
		check("null patron prints nothing", reminders.printRemindersForPatron(null), "");
		System.out.println();

		System.out.println("==============================");
		System.out.println("Passed: " + passed + "   Failed: " + failed);
		System.out.println(failed == 0 ? "ALL TESTS PASSED" : "SOME TESTS FAILED");
	}

	// ----- helpers -----

	private static Receipt receiptWith(int... barcodes) {
		Receipt r = new Receipt();
		for (int b : barcodes) {
			r.addBook(new Book("Test Book " + b, "Test Author", "000-0-00-" + b, b));
		}
		return r;
	}

	private static void check(String label, Object actual, Object expected) {
		boolean ok = (actual == null) ? expected == null : actual.equals(expected);
		if (ok) {
			passed++;
			System.out.println("  PASS  " + label);
		} else {
			failed++;
			System.out.println("  FAIL  " + label + "  (got " + actual + ", expected " + expected + ")");
		}
	}

	private static void expectError(String label, Runnable action) {
		try {
			action.run();
			failed++;
			System.out.println("  FAIL  " + label + "  (no exception thrown)");
		} catch (IllegalArgumentException e) {
			passed++;
			System.out.println("  PASS  " + label + "  (" + e.getMessage() + ")");
		}
	}
}