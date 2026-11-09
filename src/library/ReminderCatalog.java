package library;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds every Reminder in the system. Parses the LoanCatalog to decide
 * which open loans need a reminder, and gives the LibraryController the
 * reminders for a given patron.
 *
 * Reminder rules (one reminder of each type per loan, never duplicated):
 *   due in 1..DUE_SOON_DAYS days -> DUE_SOON
 *   due today                    -> DUE_TODAY
 *   past due                     -> OVERDUE
 * Closed loans never get reminders.
 */
public class ReminderCatalog {

	/** A "due soon" reminder is created this many days (or fewer) before the due date. */
	public static final int DUE_SOON_DAYS = 3;

// ATTRIBUTES ====================================
	private LocalDate lastUpdated;
	private final ArrayList<Reminder> catalog;

// GETTERS/SETTERS ===============================
	public int getTotalCount() {return catalog.size();}
	public LocalDate getLastUpdated() {return lastUpdated;}

	public List<Reminder> getAllReminders() {
		return Collections.unmodifiableList(catalog);
	}

// CONSTRUCTORS===================================
	public ReminderCatalog() {
		catalog = new ArrayList<>();
		lastUpdated = LocalDate.now();
	}

// METHODS =======================================

	/**
	 * Decides which reminder (if any) a loan needs on the given date.
	 * Returns null if the loan is closed or not due soon.
	 */
	public static Reminder.Type reminderTypeFor(Loan loan, LocalDate today) {
		if (loan == null || !loan.isOpen()) {
			return null;
		}
		long days = ChronoUnit.DAYS.between(today, loan.getDueDate());
		if (days < 0) {
			return Reminder.Type.OVERDUE;
		}
		if (days == 0) {
			return Reminder.Type.DUE_TODAY;
		}
		if (days <= DUE_SOON_DAYS) {
			return Reminder.Type.DUE_SOON;
		}
		return null;
	}

	/**
	 * Parses the LoanCatalog and creates any reminders that are now needed.
	 * Safe to run repeatedly (e.g. once a day): a loan never gets two
	 * reminders of the same type.
	 *
	 * @return the reminders created by this run
	 */
	public List<Reminder> generateReminders(LoanCatalog loans, LocalDate today) {
		if (loans == null || today == null) {
			throw new IllegalArgumentException("loans and today cannot be null");
		}
		List<Reminder> created = new ArrayList<>();
		for (Loan l : loans.getAllLoans()) {
			Reminder.Type type = reminderTypeFor(l, today);
			if (type != null && !hasReminder(l, type)) {
				Reminder r = new Reminder(l, type, today);
				catalog.add(r);
				created.add(r);
			}
		}
		if (!created.isEmpty()) {
			lastUpdated = today;
		}
		return created;
	}

	public List<Reminder> generateReminders(LoanCatalog loans) {
		return generateReminders(loans, LocalDate.now());
	}

	/**
	 * Sends every pending reminder whose loan is still open.
	 * A loan returned after its reminder was created is skipped, so the
	 * patron is not told to return books they already brought back.
	 *
	 * @return how many reminders were sent
	 */
	public int sendPendingReminders(LocalDate today) {
		int sent = 0;
		for (Reminder r : catalog) {
			if (!r.isSent() && r.getLoan().isOpen()) {
				r.sendReminder(today);
				sent++;
			}
		}
		return sent;
	}

	public int sendPendingReminders() {
		return sendPendingReminders(LocalDate.now());
	}

	/** True if this loan already has a reminder of this type. */
	public boolean hasReminder(Loan loan, Reminder.Type type) {
		for (Reminder r : catalog) {
			if (r.getLoan() == loan && r.getType() == type) {
				return true;
			}
		}
		return false;
	}

	/** All reminders addressed to a patron, oldest first. */
	public List<Reminder> getRemindersForPatron(Patron p) {
		List<Reminder> holder = new ArrayList<>();
		for (Reminder r : catalog) {
			if (r.getPatron().equals(p)) {
				holder.add(r);
			}
		}
		return holder;
	}

	/**
	 * Text for the PatronGUI after the card is validated: the latest
	 * reminder for each of the patron's open loans, one per line.
	 * (Older reminders for the same loan, e.g. "due in 2 days" once the
	 * loan is overdue, are out of date and left off.)
	 */
	public String printRemindersForPatron(Patron p) {
		if (p == null) {
			return "";
		}
		List<Reminder> mine = getRemindersForPatron(p);
		List<Loan> shown = new ArrayList<>();
		List<String> lines = new ArrayList<>();
		for (int i = mine.size() - 1; i >= 0; i--) { // newest first
			Reminder r = mine.get(i);
			if (r.getLoan().isOpen() && !shown.contains(r.getLoan())) {
				shown.add(r.getLoan());
				lines.add(0, r.getMessage());
			}
		}
		StringBuilder msg = new StringBuilder();
		for (String line : lines) {
			msg.append(line).append("\n");
		}
		if (msg.length() == 0) {
			return p.getUserName() + " has no reminders.";
		}
		return msg.toString().trim();
	}
}
