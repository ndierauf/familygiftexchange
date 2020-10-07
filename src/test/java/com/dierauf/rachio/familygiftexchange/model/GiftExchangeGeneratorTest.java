package com.dierauf.rachio.familygiftexchange.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GiftExchangeGeneratorTest {

	private static final String[] HAPPY_FAMILY_MEMBER_NAMES = new String[] { "Nick", "Trevor", "Amy", "Sam", "Nancy",
			"Matsuo-san", "Ingo", "Renato", "Judith", "Neal", "Teymour", "Ryan", "Selim", "Robert", "Claudia",
			"Kaj-Erik", "Hesham", "Michael Sr.", "Michael Jr.", "Allison", "Brad", "Hitesh", "Khaled" };

	private Set<Integer> familyMembers;
	private GiftExchangeGenerator instance;

	@BeforeEach
	public void setup() {
		this.instance = GiftExchangeGenerator.instance();
		this.familyMembers = this.generateFamilyMembers();
	}

	@Test
	void testGenerateGiftExchanges_HappyPath() throws Exception {
		Map<Integer, Integer> giftExchangePairs = this.instance.generateGiftExchanges(this.familyMembers);
		this.assertGiftExchangePairs(giftExchangePairs);
	}

	@Test
	void testGenerateGiftExchanges_OnlyOneFamilyMember() {
		this.familyMembers = new HashSet<>(Arrays.asList(0));
		try {
			this.instance.generateGiftExchanges(this.familyMembers);
			fail(GiftExchangeGenerator.ERROR_MESSAGE_MUST_HAVE_AT_LEAST_TWO_FAMILY_MEMBERS);
		} catch (Exception e) {
			assertEquals(GiftExchangeGenerator.ERROR_MESSAGE_MUST_HAVE_AT_LEAST_TWO_FAMILY_MEMBERS, e.getMessage());
		}
	}

	@Test
	void testGenerateGiftExchanges_TwoFamilyMembers() throws Exception {
		this.familyMembers = new HashSet<>(Arrays.asList(0, 1));
		Map<Integer, Integer> giftExchangePairs = this.instance.generateGiftExchanges(this.familyMembers);
		this.assertGiftExchangePairs(giftExchangePairs);
	}

	@Test
	void testGenerateGiftExchanges_StressTest() throws Exception {
		for (int i = 2; i < HAPPY_FAMILY_MEMBER_NAMES.length; i++) {
			this.familyMembers = new HashSet<>();
			for (int j = 0; j < i; j++) {
				this.familyMembers.add(j);
			}
			Map<Integer, Integer> giftExchangePairs = this.instance.generateGiftExchanges(this.familyMembers);
			this.assertGiftExchangePairs(giftExchangePairs);
			assertEquals(i, giftExchangePairs.size());
		}
	}

	private void assertGiftExchangePairs(Map<Integer, Integer> giftExchangePairs) {
		assertNotNull(giftExchangePairs);
		this.printOutGiftExchangeValues(giftExchangePairs);
		// Verify gifter is not gifting themself.
		giftExchangePairs.forEach((k, v) -> assertNotEquals(k, v));
		// Verify receiver is not receiving multiple gifts.
		this.assertReceiveOnlyOneGiftEach(giftExchangePairs);
	}

	// Asserts that each only receives exactly one gift.
	private void assertReceiveOnlyOneGiftEach(Map<Integer, Integer> giftExchangePairs) {
		Set<Integer> giverIds = giftExchangePairs.keySet();
		Set<Integer> receivers = new HashSet<>();
		for (Integer giverId : giverIds) {
			Integer receiverId = giftExchangePairs.get(giverId);
			if (!receivers.add(receiverId)) {
				fail(HAPPY_FAMILY_MEMBER_NAMES[receiverId] + " is already receiving a gift.");
				// Also means someone else is not receiving a gift.
			}
		}
	}

	// Print out values.
	private void printOutGiftExchangeValues(Map<Integer, Integer> giftExchangePairs) {
		giftExchangePairs.forEach((k, v) -> System.out.println(k + " : " + v + " - " + HAPPY_FAMILY_MEMBER_NAMES[k] + " gifts to " + HAPPY_FAMILY_MEMBER_NAMES[v]));
		System.out.println("___");
	}

	private Set<Integer> generateFamilyMembers() {
		Set<Integer> familyMembers = new HashSet<>();
		for (int i = 0; i < HAPPY_FAMILY_MEMBER_NAMES.length; i++) {
			familyMembers.add(i);
		}
		return familyMembers;
	}

}
