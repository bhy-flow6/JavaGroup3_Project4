package library;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Holds every User in the system and answers the LibraryController's
 * questions about them, chiefly UC1 Validate Library Card.
 */
public class UserCatalog {

	private final Map<Integer, User> usersById = new LinkedHashMap<>();
	private final Map<Integer, Patron> patronsByCard = new LinkedHashMap<>();
	private LocalDate lastUpdated = LocalDate.now();

	// ----- adding / removing -----

	public void addUser(User user) {
		if (user == null) {
			throw new IllegalArgumentException("user cannot be null");
		}
		if (usersById.containsKey(user.getUserID())) {
			throw new IllegalArgumentException("Duplicate userID: " + user.getUserID());
		}
		if (user instanceof Patron) {
			Patron p = (Patron) user;
			if (patronsByCard.containsKey(p.getCardNumber())) {
				throw new IllegalArgumentException("Duplicate card number: " + p.getCardNumber());
			}
			patronsByCard.put(p.getCardNumber(), p);
		}
		usersById.put(user.getUserID(), user);
		touch();
	}

	public boolean removeUser(int userID) {
		User removed = usersById.remove(userID);
		if (removed == null) {
			return false;
		}
		if (removed instanceof Patron) {
			patronsByCard.remove(((Patron) removed).getCardNumber());
		}
		touch();
		return true;
	}

	// ----- lookups -----

	public Optional<User> searchByUserID(int userID) {
		return Optional.ofNullable(usersById.get(userID));
	}

	/** Lookup used when a library card is scanned. */
	public Optional<Patron> searchByCardNumber(int cardNumber) {
		return Optional.ofNullable(patronsByCard.get(cardNumber));
	}

	public List<Patron> getAllPatrons() {
		return Collections.unmodifiableList(new ArrayList<>(patronsByCard.values()));
	}

	public List<User> getAllUsers() {
		return Collections.unmodifiableList(new ArrayList<>(usersById.values()));
	}

	public int getTotalCount() {
		return usersById.size();
	}

	public LocalDate getLastUpdated() {
		return lastUpdated;
	}

	// ----- UC1 Validate Library Card -----

	/**
	 * Validates a scanned card number and returns the status the GUI should act on.
	 * Use status.canCheckOut() to decide whether to unlock book scanning, and
	 * status.getMessage(patron) for the text to display.
	 */
	public CardStatus validateCard(int cardNumber, LocalDate today) {
		Optional<Patron> patron = searchByCardNumber(cardNumber);
		if (patron.isEmpty()) {
			return CardStatus.NOT_FOUND;
		}
		return patron.get().checkCardStatus(today);
	}

	public CardStatus validateCard(int cardNumber) {
		return validateCard(cardNumber, LocalDate.now());
	}

	/**
	 * Accepts the raw text from the card-number field, so the GUI doesn't
	 * have to parse it. Non-numeric input counts as not found.
	 */
	public CardStatus validateCard(String cardNumberText) {
		try {
			return validateCard(Integer.parseInt(cardNumberText.trim()));
		} catch (NumberFormatException | NullPointerException e) {
			return CardStatus.NOT_FOUND;
		}
	}

	/** Matches the class diagram: true if the card may be used to check out. */
	public boolean verifyUser(int cardNumber) {
		return validateCard(cardNumber).canCheckOut();
	}

	private void touch() {
		lastUpdated = LocalDate.now();
	}

	// ----- demo data -----

	/**
	 * A catalog pre-loaded with patrons covering every UC1 case, so the GUI
	 * can be tested before real data exists. Dates are relative to today.
	 *
	 *   1001  valid card
	 *   1002  expires in two weeks (warning, still allowed)
	 *   1003  expired yesterday
	 *   1004  valid but has a hold
	 *   anything else  not found
	 */
	public static UserCatalog withSampleData() {
		LocalDate today = LocalDate.now();
		UserCatalog catalog = new UserCatalog();

		catalog.addUser(new Patron(1, "Alice Nguyen", "alice@example.com", 1001, today.plusYears(1)));
		catalog.addUser(new Patron(2, "Ben Ortiz", "ben@example.com", 1002, today.plusWeeks(2)));
		catalog.addUser(new Patron(3, "Chloe Park", "chloe@example.com", 1003, today.minusDays(1)));

		Patron held = new Patron(4, "Dev Shah", "dev@example.com", 1004, today.plusYears(2));
		held.setHold(true);
		catalog.addUser(held);

		return catalog;
	}
}
