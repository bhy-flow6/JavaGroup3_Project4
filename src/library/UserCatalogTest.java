package library;

import java.time.LocalDate;


public class UserCatalogTest {

	private static int passed = 0;
	private static int failed = 0;

	public static void main(String[] args) {
		UserCatalog catalog = UserCatalog.withSampleData();

		System.out.println("=== Sample patrons ===");
		for (Patron p : catalog.getAllPatrons()) {
			System.out.println("  " + p);
		}
		System.out.println("Total users: " + catalog.getTotalCount());
		System.out.println();

		System.out.println("=== UC1 Validate Library Card ===");
		checkCard(catalog, "1001", CardStatus.APPROVED);
		checkCard(catalog, "1002", CardStatus.EXPIRING_SOON);
		checkCard(catalog, "1003", CardStatus.EXPIRED);
		checkCard(catalog, "1004", CardStatus.ON_HOLD);
		checkCard(catalog, "9999", CardStatus.NOT_FOUND);
		checkCard(catalog, "abc", CardStatus.NOT_FOUND);
		checkCard(catalog, "", CardStatus.NOT_FOUND);
		System.out.println();

		System.out.println("=== verifyUser (class diagram method) ===");
		check("1001 can check out", catalog.verifyUser(1001), true);
		check("1002 can check out (warning only)", catalog.verifyUser(1002), true);
		check("1003 blocked (expired)", catalog.verifyUser(1003), false);
		check("1004 blocked (hold)", catalog.verifyUser(1004), false);
		System.out.println();

		System.out.println("=== Expiration edge cases ===");
		LocalDate today = LocalDate.of(2026, 11, 4);
		Patron edge = new Patron(100, "Edge Case", "edge@example.com", 5000, today.plusMonths(1));
		check("expires exactly 1 month out -> warning",
				edge.checkCardStatus(today), CardStatus.EXPIRING_SOON);
		edge.setCardExpiration(today.plusMonths(1).plusDays(1));
		check("expires 1 month + 1 day out -> approved",
				edge.checkCardStatus(today), CardStatus.APPROVED);
		edge.setCardExpiration(today);
		check("expires today -> still valid, warning",
				edge.checkCardStatus(today), CardStatus.EXPIRING_SOON);
		edge.setCardExpiration(today.minusDays(1));
		check("expired yesterday -> expired",
				edge.checkCardStatus(today), CardStatus.EXPIRED);
		System.out.println();

		System.out.println("=== Open loans ===");
		Patron alice = catalog.searchByCardNumber(1001).get();
		alice.addOpenLoan(1);
		alice.addOpenLoan(2);
		alice.addOpenLoan(2); // duplicate should be ignored
		check("two loans after adding 1, 2, 2", alice.getOpenLoanIDs().size(), 2);
		check("remove loan 1", alice.removeOpenLoan(1), true);
		check("remove loan 1 again", alice.removeOpenLoan(1), false);
		System.out.println("  " + alice.getOpenLoans());
		System.out.println();

		System.out.println("=== Catalog add / remove ===");
		expectError("duplicate userID rejected", () ->
				catalog.addUser(new Patron(1, "Copy", "copy@example.com", 7777, today)));
		expectError("duplicate card number rejected", () ->
				catalog.addUser(new Patron(50, "Copy", "copy@example.com", 1001, today)));
		expectError("bad email rejected", () ->
				new Patron(51, "No Email", "not-an-email", 7778, today));
		catalog.addUser(new Patron(5, "Eve Lin", "eve@example.com", 1005, LocalDate.now().plusYears(1)));
		check("new patron can validate", catalog.validateCard(1005), CardStatus.APPROVED);
		check("remove user 5", catalog.removeUser(5), true);
		check("removed card not found", catalog.validateCard(1005), CardStatus.NOT_FOUND);
		System.out.println();

		System.out.println("==============================");
		System.out.println("Passed: " + passed + "   Failed: " + failed);
		System.out.println(failed == 0 ? "ALL TESTS PASSED" : "SOME TESTS FAILED");
	}

	// ----- helpers -----

	private static void checkCard(UserCatalog catalog, String cardText, CardStatus expected) {
		CardStatus actual = catalog.validateCard(cardText);
		Patron p = null;
		try {
			p = catalog.searchByCardNumber(Integer.parseInt(cardText)).orElse(null);
		} catch (NumberFormatException e) {
			// non-numeric input, no patron
		}
		check("card \"" + cardText + "\" -> " + expected, actual, expected);
		System.out.println("        kiosk shows: " + actual.getMessage(p));
	}

	private static void check(String label, Object actual, Object expected) {
		if (actual.equals(expected)) {
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
