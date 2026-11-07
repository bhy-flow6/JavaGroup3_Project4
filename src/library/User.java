package library;

import java.util.Objects;

/**
 * Base class for anyone with an account in the library system.
 * Concrete user types (Patron, and Librarian if it is ever brought back
 * into scope) extend this class and decide what "verified" means for them.
 */
public abstract class User {

	private final int userID;
	private String userName;
	private String userEmailAddress;

	protected User(int userID, String userName, String userEmailAddress) {
		if (userID <= 0) {
			throw new IllegalArgumentException("userID must be positive");
		}
		this.userID = userID;
		setUserName(userName);
		setUserEmail(userEmailAddress);
	}

	public int getUserID() {
		return userID;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserEmail() {
		return userEmailAddress;
	}

	public final void setUserName(String userName) {
		if (userName == null || userName.isBlank()) {
			throw new IllegalArgumentException("userName cannot be blank");
		}
		this.userName = userName.trim();
	}

	public final void setUserEmail(String userEmailAddress) {
		if (userEmailAddress == null || !userEmailAddress.contains("@")) {
			throw new IllegalArgumentException("Invalid email address: " + userEmailAddress);
		}
		this.userEmailAddress = userEmailAddress.trim();
	}

	/**
	 * Returns true if this user is currently allowed to use the system.
	 */
	public abstract boolean verifyUser();

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof User)) {
			return false;
		}
		return userID == ((User) o).userID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(userID);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[id=" + userID + ", name=" + userName + "]";
	}
}
