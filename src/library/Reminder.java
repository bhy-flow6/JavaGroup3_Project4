package library;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * A single due-date reminder for one Loan, addressed to the Loan's Patron.
 * Created by ReminderCatalog when it parses the LoanCatalog.
 *
 * Status is "Pending" until sendReminder() is called, then "Sent".
 * Actually emailing the patron is out of scope; sendReminder() records
 * the send and returns, the same way receipts are "emailed" in UC3.
 */
public class Reminder {

	/** Which kind of reminder this is. Decides the message wording. */
	public enum Type {
		DUE_SOON,  // due within ReminderCatalog.DUE_SOON_DAYS days
		DUE_TODAY,
		OVERDUE
	}

	public static final String PENDING = "Pending";
	public static final String SENT = "Sent";

// ATTRIBUTES ====================================
	private static int LAST_ID = 0;
	private final int reminderID;
	private final Patron patron;
	private final Loan loan;
	private final Type type;
	private final String message;
	private LocalDate sendDate; // null until sent
	private String status;

// GETTERS/SETTERS ===============================
	public int getReminderID() {return reminderID;}
	public Patron getPatron() {return patron;}
	public Loan getLoan() {return loan;}
	public Type getType() {return type;}
	public String getMessage() {return message;}
	public LocalDate getSendDate() {return sendDate;}
	public String getStatus() {return status;}
	public boolean isSent() {return SENT.equals(status);}

// CONSTRUCTORS===================================
	/**
	 * @param loan  the loan this reminder is about (its loaner is the recipient)
	 * @param type  due soon, due today, or overdue
	 * @param today the date the reminder is generated; used for the day count in the message
	 */
	public Reminder(Loan loan, Type type, LocalDate today) {
		if (loan == null || type == null || today == null) {
			throw new IllegalArgumentException("loan, type and today cannot be null");
		}
		LAST_ID++;
		reminderID = LAST_ID;
		this.loan = loan;
		this.patron = loan.getLoaner();
		this.type = type;
		this.status = PENDING;
		this.message = buildMessage(today);
	}

// METHODS =======================================
	private String buildMessage(LocalDate today) {
		LocalDate due = loan.getDueDate();
		long days = ChronoUnit.DAYS.between(today, due);
		String name = patron.getUserName();

		switch (type) {
			case DUE_SOON:
				return "Hi " + name + ", your library books are due on " + due
						+ " (in " + days + (days == 1 ? " day" : " days") + ").";
			case DUE_TODAY:
				return "Hi " + name + ", your library books are due today (" + due + ").";
			case OVERDUE:
			default:
				long late = -days;
				return "Hi " + name + ", your library books were due on " + due
						+ " and are " + late + (late == 1 ? " day" : " days")
						+ " overdue. Please return them as soon as possible.";
		}
	}

	/**
	 * Sends the reminder to the patron's email of record. Sending a reminder
	 * that was already sent does nothing, so a patron is never emailed twice.
	 */
	public void sendReminder(LocalDate today) {
		if (isSent()) {
			return;
		}
		// Real email delivery is out of scope for this MVP.
		sendDate = today;
		status = SENT;
	}

	public void sendReminder() {
		sendReminder(LocalDate.now());
	}

	@Override
	public String toString() {
		return "[" + status + "] #" + reminderID + " " + type + " to "
				+ patron.getUserEmail() + ": " + message;
	}
}
