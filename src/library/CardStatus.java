package library;

/**
 * Result of validating a library card (UC1 Validate Library Card).
 * Each value carries the message the kiosk should show the patron.
 * APPROVED and EXPIRING_SOON both allow checkout to continue.
 */
public enum CardStatus {

	APPROVED("Thank you! You may now begin scanning your books.", true),
	EXPIRING_SOON("Library card expires on %s. Please renew it soon.", true),   // UC1 4c
	NOT_FOUND("Library Card number not found. Please see the front desk.", false), // UC1 4a
	EXPIRED("Library card expired. Please see the front desk.", false),          // UC1 4b
	ON_HOLD("There is a hold on your library card. Please see the front desk.", false); // UC1 4d

	private final String message;
	private final boolean canCheckOut;

	CardStatus(String message, boolean canCheckOut) {
		this.message = message;
		this.canCheckOut = canCheckOut;
	}

	/** True if the patron may proceed to UC3 Check Out Books. */
	public boolean canCheckOut() {
		return canCheckOut;
	}

	/**
	 * Message for the GUI. For EXPIRING_SOON, pass the patron so the
	 * expiration date can be filled in; other statuses ignore it.
	 */
	public String getMessage(Patron patron) {
		if (this == EXPIRING_SOON && patron != null) {
			return String.format(message, patron.getCardExpiration());
		}
		return message.replace(" on %s", "");
	}

	public String getMessage() {
		return getMessage(null);
	}
}
