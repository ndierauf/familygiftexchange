package com.dierauf.rachio.familygiftexchange.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GiftExchangeGeneratorTest {

	private Set<Set<Integer>> familyUnits;
	private GiftExchangeGenerator instance;

	@BeforeEach
	public void setup() {
		this.instance = GiftExchangeGenerator.instance();
		this.familyUnits = this.instance.generateFamilyUnits(GiftExchangeGenerator.DEFAULT_FAMILYUNIT_SIZE,
				GiftExchangeGenerator.HAPPY_FAMILY_MEMBER_NAMES);
		this.setLogLevel(Level.DEBUG);
	}

	@Test
	void test_generateGiftExchanges_HappyPath() throws Exception {
		Map<Integer, Integer> giftExchangePairs = this.instance.generateGiftExchanges(this.familyUnits);
		this.assertGiftExchangePairs(giftExchangePairs);
	}

	@Test
	void test_generateGiftExchanges_OnlyOneFamilyUnit() {
		Set<Integer> familyUnit1 = this.familyUnits.iterator().next();
		this.familyUnits.clear();
		this.familyUnits.add(familyUnit1);
		try {
			this.instance.generateGiftExchanges(this.familyUnits);
			fail(GiftExchangeGenerator.ERROR_MESSAGE_MUST_HAVE_AT_LEAST_TWO_FAMILY_UNITS);
		} catch (Exception e) {
			assertEquals(GiftExchangeGenerator.ERROR_MESSAGE_MUST_HAVE_AT_LEAST_TWO_FAMILY_UNITS, e.getMessage());
		}
	}

	@Test
	void test_generateGiftExchanges_NullFamilyUnits() {
		this.familyUnits = null;
		try {
			this.instance.generateGiftExchanges(this.familyUnits);
			fail(GiftExchangeGenerator.ERROR_MESSAGE_MUST_HAVE_AT_LEAST_TWO_FAMILY_UNITS);
		} catch (Exception e) {
			assertEquals(GiftExchangeGenerator.ERROR_MESSAGE_MUST_HAVE_AT_LEAST_TWO_FAMILY_UNITS, e.getMessage());
		}
	}

	@Test
	void test_generateGiftExchanges_FamilyUnitIsNull() {
		this.familyUnits.add(null);
		try {
			this.instance.generateGiftExchanges(this.familyUnits);
			fail(GiftExchangeGenerator.ERROR_MESSAGE_INVALID_FAMILY_UNIT_NULL);
		} catch (Exception e) {
			assertEquals(GiftExchangeGenerator.ERROR_MESSAGE_INVALID_FAMILY_UNIT_NULL, e.getMessage());
		}
	}

	@Test
	void test_generateGiftExchanges_FamilyUnitContainsNullId() throws Exception {
		Set<Integer> familyUnit = this.familyUnits.iterator().next();
		Integer idToBeRemoved = familyUnit.iterator().next();
		familyUnit.remove(idToBeRemoved);
		familyUnit.add(null);
		try {
			this.instance.generateGiftExchanges(this.familyUnits);
			fail(GiftExchangeGenerator.ERROR_MESSAGE_INVALID_FAMILY_MEMBER_ID_NULL);
		} catch (Exception e) {
			assertEquals(GiftExchangeGenerator.ERROR_MESSAGE_INVALID_FAMILY_MEMBER_ID_NULL, e.getMessage());
		}
	}

	@Test
	void test_generateGiftExchanges_FamilyMemberNotUniqueToFamilyUnits() throws Exception {
		this.onlyTwoFamilyUnits();
		Iterator<Set<Integer>> iterator = this.familyUnits.iterator();
		Set<Integer> familyUnit1 = iterator.next();
		Set<Integer> familyUnit2 = iterator.next();
		familyUnit1.clear();
		familyUnit2.clear();
		familyUnit1.add(-1);
		familyUnit2.add(-1);
		try {
			this.instance.generateGiftExchanges(this.familyUnits);
			fail(GiftExchangeGenerator.ERROR_MESSAGE_FAMILY_MEMBER_ID_FOUND_IN_MULTIPLE_FAMILY_UNITS);
		} catch (Exception e) {
			assertTrue(e.getMessage().contains(GiftExchangeGenerator.ERROR_MESSAGE_FAMILY_MEMBER_ID_FOUND_IN_MULTIPLE_FAMILY_UNITS));
		}
	}

	@Test
	void test_generateGiftExchanges_FamilyUnitTooLarge() throws Exception {
		this.onlyTwoFamilyUnits();
		Set<Integer> familyUnit1 = this.familyUnits.iterator().next();
		familyUnit1.add(-1);
		try {
			this.instance.generateGiftExchanges(this.familyUnits);
			fail("Expecting exception to be thrown: " + GiftExchangeGenerator.ERROR_MESSAGE_FAMILY_UNIT_IS_TOO_LARGE);
		} catch (Exception e) {
			assertTrue(e.getMessage().contains(GiftExchangeGenerator.ERROR_MESSAGE_FAMILY_UNIT_IS_TOO_LARGE));
		}
	}

	@Test
	void test_generateGiftExchanges_TwoFamilyUnitsOneIsEmpty() throws Exception {
		this.onlyTwoFamilyUnits();
		Set<Integer> familyUnit1 = this.familyUnits.iterator().next();
		familyUnit1.clear();
		try {
			this.instance.generateGiftExchanges(this.familyUnits);
			fail("Expecting exception to be thrown: " + GiftExchangeGenerator.ERROR_MESSAGE_FAMILY_UNIT_IS_TOO_LARGE);
		} catch (Exception e) {
			assertTrue(e.getMessage().contains(GiftExchangeGenerator.ERROR_MESSAGE_FAMILY_UNIT_IS_TOO_LARGE));
		}
	}

	private void onlyTwoFamilyUnits() {
		Iterator<Set<Integer>> iterator = this.familyUnits.iterator();
		Set<Integer> familyUnit1 = iterator.next();
		Set<Integer> familyUnit2 = iterator.next();
		this.familyUnits.clear();
		this.familyUnits.add(familyUnit1);
		this.familyUnits.add(familyUnit2);
	}

	@Test
	void test_generateGiftExchanges_TwoFamilyUnitsBothAreEmpty() throws Exception {
		Iterator<Set<Integer>> iterator = this.familyUnits.iterator();
		Set<Integer> familyUnit1 = iterator.next();
		familyUnit1.clear();
		Set<Integer> familyUnit2 = iterator.next();
		familyUnit2.clear();
		Map<Integer, Integer> giftExchangePairs = this.instance.generateGiftExchanges(this.familyUnits);
		this.assertGiftExchangePairs(giftExchangePairs);
	}

	@Test
	void test_main_NoArguements() {
		try {
			GiftExchangeGenerator.main(new String[0]);
			assertTrue(true, "Successful run of main method with no arguements. ");
		} catch (Exception e) {
			fail("Not expecting exception to be thrown. " + e.getMessage());
		}
	}

	@Test
	void test_main_WithArguements() {
		try {
			GiftExchangeGenerator.main(new String[] {"2", "Nick", "Trevor", "Amy", "Sam", "Nancy"});
			assertTrue(true, "Successful run of main method with no arguements. ");
		} catch (Exception e) {
			fail("Not expecting exception to be thrown. " + e.getMessage());
		}
	}

	@Test
	void test_main_WithArguements_OnlyOneArguement() {
		try {
			GiftExchangeGenerator.main(new String[] {"2"});
			fail("Expecting exception to be thrown. ");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains(GiftExchangeGenerator.INSTRUCTIONS));
		}
	}

	@Test
	void test_main_WithArguements_InvalidFamilyUnitSize() {
		try {
			GiftExchangeGenerator.main(new String[] {"-1", "Nick", "Trevor", "Amy", "Sam", "Nancy"});
			fail("Expecting exception to be thrown. ");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains(GiftExchangeGenerator.INSTRUCTIONS));
		}
	}

	@Test
	void test_main_WithArguements_FirstArguementNotANumber() {
		try {
			GiftExchangeGenerator.main(new String[] {"Billy", "Nick", "Trevor", "Amy", "Sam", "Nancy"});
			fail("Expecting exception to be thrown. ");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains(GiftExchangeGenerator.INSTRUCTIONS));
		}
	}

	@Test // Attains 100% code-coverage.
	void test_main_NoArguements_Level_INFO() {
		this.setLogLevel(Level.INFO);
		try {
			GiftExchangeGenerator.main(new String[0]);
			assertTrue(true, "Successful run of main method with no arguements. ");
		} catch (Exception e) {
			fail("Not expecting exception to be thrown. " + e.getMessage());
		}
	}

	@Test
	void test_validateNoNullGiverOrReceiverValues_NullGiverId() throws Exception {
		Map<Integer, Integer> giftExchangePairs = this.instance.generateGiftExchanges(this.familyUnits);
		giftExchangePairs.put(null, -1);
		try {
			this.instance.validateNoNullGiverOrReceiverValues(giftExchangePairs);
			fail("Expecting exception to be thrown. ");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains(GiftExchangeGenerator.ERROR_MESSAGE_GIVER_ID_CANNOT_BE_NULL));
		}
	}

	@Test
	void test_validateNoNullGiverOrReceiverValues_NullReceiverId() throws Exception {
		Map<Integer, Integer> giftExchangePairs = this.instance.generateGiftExchanges(this.familyUnits);
		giftExchangePairs.put(-1, null);
		try {
			this.instance.validateNoNullGiverOrReceiverValues(giftExchangePairs);
			fail("Expecting exception to be thrown. ");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains(GiftExchangeGenerator.ERROR_MESSAGE_RECEIVER_ID_CANNOT_BE_NULL));
		}
	}

	@Test
	void test_validateGiverNotSameAsReceiver() throws Exception {
		Map<Integer, Integer> giftExchangePairs = this.instance.generateGiftExchanges(this.familyUnits);
		giftExchangePairs.put(-1, -1);
		try {
			this.instance.validateGiverNotSameAsReceiver(giftExchangePairs);
			fail("Expecting exception to be thrown. ");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains(GiftExchangeGenerator.ERROR_MESSAGE_GIVER_ID_AND_RECEIVER_ID_CANNOT_BE_THE_SAME));
		}
	}

	@Test
	void test_validateReceiveOnlyOneGiftEach() throws Exception {
		Map<Integer, Integer> giftExchangePairs = this.instance.generateGiftExchanges(this.familyUnits);
		giftExchangePairs.put(-1, -1);
		giftExchangePairs.put(-2, -1);
		try {
			this.instance.validateReceiveOnlyOneGiftEach(giftExchangePairs);
			fail("Expecting exception to be thrown. ");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains(GiftExchangeGenerator.ERROR_MESSAGE_DUPLICATE_RECEIVER_ID_FOUND));
		}
	}

	@Test
	void test_validateNotGiftingFamilyUnitMembers() throws Exception {
		Map<Integer, Integer> giftExchangePairs = this.instance.generateGiftExchanges(this.familyUnits);
		Set<Integer> familyUnit = this.familyUnits.iterator().next();
		Iterator<Integer> iterator = familyUnit.iterator();
		Integer familyMember1 = iterator.next();
		Integer familyMember2 = iterator.next();
		List<Entry<Integer, Integer>> familyMembers = giftExchangePairs.entrySet().stream()
				.filter(x -> x.getKey().equals(familyMember1) || x.getKey().equals(familyMember2))
				.collect(Collectors.toList());
		Integer giverId = familyMembers.get(0).getKey();
		familyMembers.get(1).setValue(giverId);
		try {
			this.instance.validateNotGiftingFamilyUnitMembers(giftExchangePairs, this.familyUnits);
			fail("Expecting exception to be thrown. ");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains(GiftExchangeGenerator.ERROR_MESSAGE_FAMILY_MEMBER_GIVING_TO_ANOTHER_FAMILY_MEMBER));
		}
	}

	@Test
	void test_generateGiftExchanges_StressTest() throws Exception {
		int maxNumberOfFamilyUnits = GiftExchangeGenerator.HAPPY_FAMILY_MEMBER_NAMES.length / 2;
		for (int i = 1; i <= maxNumberOfFamilyUnits; i++) {
			int numberOfMembersPerFamilyUnit = i;
			this.testProgressivelyLargerFamilyUnitSizes(numberOfMembersPerFamilyUnit);
		}
	}

	private void testProgressivelyLargerFamilyUnitSizes(int numberOfMembersPerFamilyUnit) throws Exception {
		int totalNumberOfFamilyMembers = GiftExchangeGenerator.HAPPY_FAMILY_MEMBER_NAMES.length;
		this.familyUnits = this.instance.generateFamilyUnits(numberOfMembersPerFamilyUnit, totalNumberOfFamilyMembers);
		Map<Integer, Integer> giftExchangePairs = this.instance.generateGiftExchanges(this.familyUnits);
		this.assertGiftExchangePairs(giftExchangePairs);
	}

	private void assertGiftExchangePairs(Map<Integer, Integer> giftExchangePairs) {
		assertNotNull(giftExchangePairs);
		System.out.println(this.instance.printOutGiftExchangeValues(giftExchangePairs,
				GiftExchangeGenerator.HAPPY_FAMILY_MEMBER_NAMES));
		// Verify gifter is not gifting themself.
		giftExchangePairs.forEach((k, v) -> assertNotEquals(k, v));
		// Verify receiver is not receiving multiple gifts.
		this.assertReceiveOnlyOneGiftEach(giftExchangePairs);
		this.assertNotGiftingFamilyUnitMembers(giftExchangePairs);
		System.out.println("----");
	}

	// Asserts that each only receives exactly one gift.
	private void assertReceiveOnlyOneGiftEach(Map<Integer, Integer> giftExchangePairs) {
		Set<Integer> giverIds = giftExchangePairs.keySet();
		Set<Integer> receivers = new HashSet<>();
		for (Integer giverId : giverIds) {
			Integer receiverId = giftExchangePairs.get(giverId);
			if (!receivers.add(receiverId)) {
				String message = GiftExchangeGenerator.HAPPY_FAMILY_MEMBER_NAMES[receiverId] + " is already receiving a gift.";
				System.out.println(message);
				fail(message);
				// Also means someone else is not receiving a gift.
			}
		}
	}

	private void assertNotGiftingFamilyUnitMembers(Map<Integer, Integer> giftExchangePairs) {
		for (Set<Integer> familyUnit : this.familyUnits) {
			for (Integer giverId : familyUnit) {
				Integer receiverId = giftExchangePairs.get(giverId);
				String message = "Family member giving to another family member. giverId: " + giverId + "; receiverId: "
						+ receiverId + "; familyUnit: " + familyUnit;
				boolean failure = familyUnit.contains(receiverId);
				if (failure) { System.out.println(message); }
				assertFalse(failure, message);
			}
		}
	}

	private void setLogLevel(Level level) {
		Configurator.setLevel(LogManager.getLogger(this.instance.getClass()).getName(), level);
	}

}
