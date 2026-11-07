package library;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A library patron: a User who holds a library card and can borrow books.
 *
 * Open loans are tracked by loan ID rather than by Loan object so this class
 * does not depend on Loan / LoanCatalog. Look up the full Loan with
 * LoanCatalog.searchByLoanID(id).
 */
public class Patron extends User {

	/** UC1 4c: warn when the card expires within this many months. */
	public static final int EXPIRY_WARNING_MONTHS = 1;

	private final int cardNumber;
	private LocalDate cardExpiration;
	private boolean hasHold;
	private final List<Integer> openLoans = new ArrayList<>();

	public Patron(int userID, String userName, String userEmailAddress,
			int cardNumber, LocalDate cardExpiration) {
		super(userID, userName, userEmailAddress);
		if (cardNumber <= 0) {
			throw new IllegalArgumentException("cardNumber must be positive");
		}
		if (cardExpiration == null) {
			throw new IllegalArgumentException("cardExpiration cannot be null");
		}
		this.cardNumber = cardNumber;
		this.cardExpiration = cardExpiration;
	}

	// ----- card -----

	public int getCardNumber() {
		return cardNumber;
	}

	public LocalDate getCardExpiration() {
		return cardExpiration;
	}

	/** Used when a librarian renews the card. */
	public void setCardExpiration(LocalDate cardExpiration) {
		if (cardExpiration == null) {
			throw new IllegalArgumentException("cardExpiration cannot be null");
		}
		this.cardExpiration = cardExpiration;
	}

	public boolean isExpired(LocalDate today) {
		return cardExpiration.isBefore(today);
	}

	public boolean isExpiringSoon(LocalDate today) {
		return !isExpired(today)
				&& !cardExpiration.isAfter(today.plusMonths(EXPIRY_WARNING_MONTHS));
	}

	// ----- holds -----

	public boolean hasHold() {
		return hasHold;
	}

	public void setHold(boolean hasHold) {
		this.hasHold = hasHold;
	}

	// ----- verification (UC1) -----

	/**
	 * Full UC1 check against a given date. Not-found is handled by UserCatalog,
	 * since a Patron that exists was by definition found.
	 */
	public CardStatus checkCardStatus(LocalDate today) {
		if (isExpired(today)) {
			return CardStatus.EXPIRED;
		}
		if (hasHold) {
			return CardStatus.ON_HOLD;
		}
		if (isExpiringSoon(today)) {
			return CardStatus.EXPIRING_SOON;
		}
		return CardStatus.APPROVED;
	}

	public CardStatus checkCardStatus() {
		return checkCardStatus(LocalDate.now());
	}

	/** True if the card is not expired and has no holds. */
	@Override
	public boolean verifyUser() {
		return checkCardStatus().canCheckOut();
	}

	// ----- loans -----

	public void addOpenLoan(int loanID) {
		if (!openLoans.contains(loanID)) {
			openLoans.add(loanID);
		}
	}

	/** Call when LoanCatalog closes a loan (UC4). */
	public boolean removeOpenLoan(int loanID) {
		return openLoans.remove(Integer.valueOf(loanID));
	}

	public List<Integer> getOpenLoanIDs() {
		return Collections.unmodifiableList(openLoans);
	}

	public boolean hasOpenLoans() {
		return !openLoans.isEmpty();
	}

	/** Readable summary of open loans, as in the class diagram. */
	public String getOpenLoans() {
		if (openLoans.isEmpty()) {
			return getUserName() + " has no open loans.";
		}
		return getUserName() + " has " + openLoans.size() + " open loan(s): " + openLoans;
	}

	@Override
	public String toString() {
		return "Patron[id=" + getUserID() + ", name=" + getUserName()
				+ ", card=" + cardNumber + ", expires=" + cardExpiration
				+ (hasHold ? ", HOLD" : "") + "]";
	}
}