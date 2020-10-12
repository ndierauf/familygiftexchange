package com.dierauf.rachio.familygiftexchange.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Nicholas Dierauf
 * A Singleton class for generating family gift exchange pairs.
 * Can be considered a 'Service'.
 */
public class GiftExchangeGenerator {

	static final String INSTRUCTIONS = "First arguement must be a number indicating the number of family members per family unit, followed by a list of family members. "
			+ "Example: \njava -jar FamilyGiftExchange.jar 2 Nick Trevor Amy Sam";
	static final String ERROR_MESSAGE_FAMILY_MEMBER_GIVING_TO_ANOTHER_FAMILY_MEMBER = "Family member giving to another family member. ";
	static final String ERROR_MESSAGE_DUPLICATE_RECEIVER_ID_FOUND = "Duplicate receiverId found: ";
	static final String ERROR_MESSAGE_GIVER_ID_AND_RECEIVER_ID_CANNOT_BE_THE_SAME = "GiverId and ReceiverId cannot be the same: ";
	static final String ERROR_MESSAGE_RECEIVER_ID_CANNOT_BE_NULL = "ReceiverId cannot be null. (GiverId: ";
	static final String ERROR_MESSAGE_GIVER_ID_CANNOT_BE_NULL = "GiverId cannot be null: ";
	static final String ERROR_MESSAGE_FAMILY_UNIT_IS_TOO_LARGE = "Family unit is too large: ";
	static final String ERROR_MESSAGE_FAMILY_MEMBER_ID_FOUND_IN_MULTIPLE_FAMILY_UNITS = "Family member ID found in multiple family units: ";
	static final String ERROR_MESSAGE_INVALID_FAMILY_MEMBER_ID_NULL = "Invalid family member ID (null)";
	static final String ERROR_MESSAGE_INVALID_FAMILY_UNIT_NULL = "Invalid family unit (null).";
	static final String ERROR_MESSAGE_MUST_HAVE_AT_LEAST_TWO_FAMILY_UNITS = "Must have at least two family units to generate gift exchange. ";

	static final int DEFAULT_FAMILYUNIT_SIZE = 5;
	static final String[] HAPPY_FAMILY_MEMBER_NAMES = new String[] { "Nick", "Trevor", "Amy", "Sam", "Nancy",
			"Matsuo-san", "Ingo", "Renato", "Judith", "Neal", "Teymour", "Ryan", "Selim", "Robert", "Claudia",
			"Kaj-Erik", "Hesham", "Michael Sr.", "Michael Jr.", "Allison", "Brad", "Hitesh", "Khaled" };

	static final Logger LOGGER = LogManager.getLogger(GiftExchangeGenerator.class);

	private static final Random RANDOM = new Random(); // Is thread-safe, but maybe not efficient at high volumes.
	private static final GiftExchangeGenerator INSTANCE = new GiftExchangeGenerator(); // Singleton created on class load.


	// Assures Singleton.
	private GiftExchangeGenerator() {};
	public static GiftExchangeGenerator instance() {
		return INSTANCE;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String[] familyMemberNames = INSTANCE.retrieveFamilyNames(args);
		Set<Set<Integer>> familyUnits = INSTANCE.generateFamilyUnits(args, familyMemberNames);
		Map<Integer, Integer> giverReceiverMap = INSTANCE.generateGiftExchanges(familyUnits);
		LOGGER.info("Results: {}", () -> INSTANCE.printOutGiftExchangeValues(giverReceiverMap, familyMemberNames));
	}

	public Map<Integer, Integer> generateGiftExchanges(Set<Set<Integer>> familyUnits) throws Exception {

		this.validateParameters(familyUnits); // Assures clean data. Throws exception for bad data.
		Set<Integer> familyMemberIds = familyUnits.stream().flatMap(Set::stream).collect(Collectors.toSet());

		Map<Integer, Integer> giverReceiverMap = new HashMap<>();
		for (Set<Integer> familyUnit : familyUnits) {
			Set<Integer> receiverIdPool = this.createReceiverIdPool(familyMemberIds, giverReceiverMap, familyUnit);
			// receiverIds is now guaranteed not to have giverId.
			for (Integer giverId : familyUnit) {
				if (receiverIdPool.isEmpty()) {
					// If receiverIds.isEmpty() then we need to 'swap' an eligible receiverId from a
					// previous selection with the current giverId. Returns the swapped receiverId.
					this.swapWithAnExistingNonFamilyMember(giverId, giverReceiverMap, familyUnit, familyMemberIds);
				}
				else {
					Integer receiverId = this.randomSelection(receiverIdPool);
					giverReceiverMap.put(giverId, receiverId);
					receiverIdPool.remove(receiverId);
				}
				if (LOGGER.isDebugEnabled()) {
					this.validateGiverReceiverMap(giverReceiverMap, familyUnits);
				}
			}
		}

		this.validateGiverReceiverMap(giverReceiverMap, familyUnits);
		return giverReceiverMap;
	}

	private void swapWithAnExistingNonFamilyMember(Integer giverId, Map<Integer, Integer> giverReceiverMap,
			Set<Integer> familyUnit, Set<Integer> familyMemberIds) {
		LOGGER.debug("giverId: {}. familyUnit: {}.", giverId, familyUnit);
		LOGGER.debug(() -> this.printGiverReceiverMap("Before swap", giverReceiverMap));
		Integer existingGiverId = this.retrieveEligibleExistingGiverId(giverReceiverMap, familyUnit);
		Integer existingReceiverId = giverReceiverMap.get(existingGiverId);
		Integer remainingReceiverId = this.retrieveRemainingReceiverId(giverReceiverMap, familyMemberIds, giverId);
		LOGGER.debug(
				"Selected random existingGiverId: {}; existingReceiverId: {}; remainingReceiverId: {}. Adding: [{}:{}]; Updating: [{}:{}]. ",
				existingGiverId, existingReceiverId, remainingReceiverId, giverId, existingReceiverId, existingGiverId,
				remainingReceiverId);
		// Make the swap.
		giverReceiverMap.put(existingGiverId, remainingReceiverId);
		giverReceiverMap.put(giverId, existingReceiverId);
		LOGGER.debug(() -> this.printGiverReceiverMap("After swap", giverReceiverMap));
	}

	private Integer retrieveEligibleExistingGiverId(Map<Integer, Integer> giverReceiverMap, Set<Integer> familyUnit) {
		// Filter out any current entries that have giverIds or receiverIds that belong to familyUnit.
		Set<Integer> eligibleGiverIds = giverReceiverMap.keySet().stream()
				.filter(x -> !familyUnit.contains(x) && !familyUnit.contains(giverReceiverMap.get(x)))
				.collect(Collectors.toSet());
		Integer selectedEligibleExistingGiverId = this.randomSelection(eligibleGiverIds);
		LOGGER.debug("eligibleGiverIds: {}", () -> this.pringEligibleGiverIds(eligibleGiverIds, selectedEligibleExistingGiverId));
		return selectedEligibleExistingGiverId;
	}

	private Integer retrieveRemainingReceiverId(Map<Integer, Integer> giverReceiverMap, Set<Integer> familyMemberIds, Integer giverId) {
		Set<Integer> remainingReceiverIds = new HashSet<>(familyMemberIds);
		remainingReceiverIds.removeAll(giverReceiverMap.values());
		Integer remainingReceiverId = this.randomSelection(remainingReceiverIds);
		LOGGER.debug("remainingReceiverIds: {}", () -> this.printRemainingReceiverIds(remainingReceiverIds, remainingReceiverId));
		return remainingReceiverId;
	}

	private Set<Integer> createReceiverIdPool(Set<Integer> familyMemberIds, Map<Integer, Integer> giverReceiverMap,
			Set<Integer> familyUnit) {
		Set<Integer> receiverIdPool = new HashSet<>(familyMemberIds);
		receiverIdPool.removeAll(giverReceiverMap.values());
		receiverIdPool.removeAll(familyUnit);
		return receiverIdPool;
	}

	private Integer randomSelection(Collection<Integer> collection) {
		Integer[] array = collection.toArray(new Integer[0]);
		int index = RANDOM.nextInt(array.length);
		Integer existingGiverId = array[index];
		return existingGiverId;
	}

	// Basic validation. Thorough validation performed in unit tests.
	private void validateGiverReceiverMap(Map<Integer, Integer> giverReceiverMap, Set<Set<Integer>> familyUnits)
			throws Exception {
		this.validateNoNullGiverOrReceiverValues(giverReceiverMap);
		// Verify gifter is not gifting themself.
		this.validateGiverNotSameAsReceiver(giverReceiverMap);
		// Verify receiver is not receiving multiple gifts.
		this.validateReceiveOnlyOneGiftEach(giverReceiverMap);
		this.validateNotGiftingFamilyUnitMembers(giverReceiverMap, familyUnits);
	}

	void validateNoNullGiverOrReceiverValues(Map<Integer, Integer> giverReceiverMap) throws Exception {
		Set<Integer> giverIds = giverReceiverMap.keySet();
		for (Integer giverId : giverIds) {
			if (giverId == null) {
				throw new Exception(ERROR_MESSAGE_GIVER_ID_CANNOT_BE_NULL + giverId + ".");
			}
			if (giverReceiverMap.get(giverId) == null) {
				throw new Exception(ERROR_MESSAGE_RECEIVER_ID_CANNOT_BE_NULL + giverId + ").");
			}
		}
	}

	void validateGiverNotSameAsReceiver(Map<Integer, Integer> giverReceiverMap) throws Exception {
		for (Entry<Integer, Integer> entry : giverReceiverMap.entrySet()) {
			if (entry.getKey().equals(entry.getValue())) {
				throw new Exception(
						ERROR_MESSAGE_GIVER_ID_AND_RECEIVER_ID_CANNOT_BE_THE_SAME + "[" + entry.getKey() + ":" + entry.getValue() + "]");
			}
		}
	}

	void validateReceiveOnlyOneGiftEach(Map<Integer, Integer> giftExchangePairs) throws Exception {
		Set<Integer> giverIds = giftExchangePairs.keySet();
		Set<Integer> receivers = new HashSet<>();
		for (Integer giverId : giverIds) {
			Integer receiverId = giftExchangePairs.get(giverId);
			if (!receivers.add(receiverId)) {
				throw new Exception(ERROR_MESSAGE_DUPLICATE_RECEIVER_ID_FOUND + receiverId + ". ");
			}
		}
	}

	void validateNotGiftingFamilyUnitMembers(Map<Integer, Integer> giftExchangePairs,
			Set<Set<Integer>> familyUnits) throws Exception {
		for (Set<Integer> familyUnit : familyUnits) {
			for (Integer giverId : familyUnit) {
				Integer receiverId = giftExchangePairs.get(giverId);
				boolean failure = familyUnit.contains(receiverId);
				if (failure) {
					throw new Exception(ERROR_MESSAGE_FAMILY_MEMBER_GIVING_TO_ANOTHER_FAMILY_MEMBER + "giverId: " + giverId
							+ "; receiverId: " + receiverId + "; familyUnit: " + familyUnit);
				}
			}
		}
	}

	// Parameter validation.
	private void validateParameters(Set<Set<Integer>> familyUnits) throws Exception {
		if (familyUnits == null || familyUnits.size() < 2) {
			LOGGER.error(ERROR_MESSAGE_MUST_HAVE_AT_LEAST_TWO_FAMILY_UNITS);
			throw new Exception(ERROR_MESSAGE_MUST_HAVE_AT_LEAST_TWO_FAMILY_UNITS);
		}
		Set<Integer> familyIds = new HashSet<>();
		for (Set<Integer> familyUnit : familyUnits) {
			if (familyUnit == null) {
				LOGGER.error(ERROR_MESSAGE_INVALID_FAMILY_UNIT_NULL);
				throw new Exception(ERROR_MESSAGE_INVALID_FAMILY_UNIT_NULL);
			}
			for (Integer id : familyUnit) {
				if (id == null) {
					LOGGER.error(ERROR_MESSAGE_INVALID_FAMILY_MEMBER_ID_NULL);
					throw new Exception(ERROR_MESSAGE_INVALID_FAMILY_MEMBER_ID_NULL);
				}
				// Use boolean from set.add() to determine if ID already exists in different family unit.
				if (!familyIds.add(id)) {
					String message = ERROR_MESSAGE_FAMILY_MEMBER_ID_FOUND_IN_MULTIPLE_FAMILY_UNITS + id
							+ ". familyUnit: " + familyUnit + ". familyUnits: " + familyUnits;
					LOGGER.error(message);
					throw new Exception(message);
				}
			}
		}
		// Need to make sure that there are enough receivers for a large family of givers, and vice-versa.
		for (Set<Integer> familyUnit : familyUnits) {
			if (familyUnit.size() * 2 > familyIds.size()) {
				String message = ERROR_MESSAGE_FAMILY_UNIT_IS_TOO_LARGE + familyUnit.size()
						+ ". Current family pool size is: " + familyIds.size() + ". ";
				LOGGER.error(message);
				throw new Exception(message);
			}
		}
	}

	private Set<Set<Integer>> generateFamilyUnits(String[] args, String[] familyMemberNames) throws Exception {
		int familyUnitSize = this.retrieveFamilyUnitSize(args);
		Set<Set<Integer>> familyUnits = this.generateFamilyUnits(familyUnitSize, familyMemberNames);
		LOGGER.info(() -> this.printFamiyUnits(familyUnits, familyMemberNames));
		return familyUnits;
	}

	Set<Set<Integer>> generateFamilyUnits(int numberOfMembersPerFamilyUnit, String[] familyMemberNames) {
		int totalNumberOfFamilyMembers = familyMemberNames.length;
		return this.generateFamilyUnits(numberOfMembersPerFamilyUnit, totalNumberOfFamilyMembers);
	}

	Set<Set<Integer>> generateFamilyUnits(int numberOfMembersPerFamilyUnit, int totalNumberOfFamilyMembers) {
		Set<Set<Integer>> familyUnits = new HashSet<>();
		int remainder = totalNumberOfFamilyMembers % numberOfMembersPerFamilyUnit;
		int wholeFamilyUnitsTotal = totalNumberOfFamilyMembers / numberOfMembersPerFamilyUnit;
 		for (int i = 0; i < wholeFamilyUnitsTotal; i++) {
			int startingIndex = i * numberOfMembersPerFamilyUnit;
			familyUnits.add(this.createFamilyUnit(startingIndex, numberOfMembersPerFamilyUnit));
		}
		if (remainder != 0) {
			int startingIndex = wholeFamilyUnitsTotal * numberOfMembersPerFamilyUnit;
			familyUnits.add(this.createFamilyUnit(startingIndex, remainder));
		}
		LOGGER.info("FamilyUnit size: {} ; FamilyUnits: {} {}", numberOfMembersPerFamilyUnit,
				familyUnits.size(), (remainder == 0 ? ". " : "; One FamilyUnit size is: " + remainder + ". "));
		return familyUnits;
	}

	Set<Integer> createFamilyUnit(int startingIndex, int familyUnitSize) {
		Set<Integer> familyUnit = new HashSet<>();
		for (int i = startingIndex; i < startingIndex + familyUnitSize; i++) {
			familyUnit.add(i);
		}
		return familyUnit;
	}

	private int retrieveFamilyUnitSize(String[] args) throws Exception {
		if (args.length > 0) {
			int familyUnitSize = Integer.parseInt(args[0]);
			if (familyUnitSize < 1) {
				throw new Exception("familyUnitSize must be greater than 0. " + INSTRUCTIONS);
			}
			return Integer.parseInt(args[0]);
		}
		return DEFAULT_FAMILYUNIT_SIZE;
	}

	private String[] retrieveFamilyNames(String[] args) throws Exception {
		if (args.length > 0) {
			if (args.length < 2) {
				throw new Exception(INSTRUCTIONS);
			}
			try {
				Integer.parseInt(args[0]);
			}
			catch(Exception e) {
				throw new Exception(INSTRUCTIONS, e);
			}
			String[] familyNames = new String[args.length - 1];
			for (int i = 1; i < args.length; i++) {
				familyNames[i - 1] = args[i];
			}
			return familyNames;
		}
		return HAPPY_FAMILY_MEMBER_NAMES;
	}

	private String pringEligibleGiverIds(Set<Integer> eligibleGiverIds, Integer selectedEligibleExistingGiverId) {
		StringBuilder sb = new StringBuilder("[");
		eligibleGiverIds.stream().sorted().collect(Collectors.toList()).forEach(x -> sb.append(x + " "));
		sb.append("]. Selected eligibleGiverId: " + selectedEligibleExistingGiverId + ". ");
		return sb.toString();
	}

	private String printRemainingReceiverIds(Set<Integer> remainingReceiverIds, Integer remainingReceiverId) {
		StringBuilder sb = new StringBuilder("[");
		remainingReceiverIds.forEach(x -> sb.append(x + " "));
		sb.append("]. Selected remainingReceiverId: " + remainingReceiverId + ". ");
		return sb.toString();
	}

	private String printFamiyUnits(Set<Set<Integer>> familyUnits, String[] familyMemberNames) {
		StringBuilder sb = new StringBuilder("FamilyUnits:\n");
		sb.append("[");
		for (Set<Integer> familyUnit : familyUnits) {
			sb.append("\n\t[");
			for (Integer familyMemberId : familyUnit) {
				sb.append(familyMemberNames[familyMemberId] + " ");
			}
			sb.append("]");
		}
		sb.append("\n]");
		return sb.toString();
	}

	private String printGiverReceiverMap(String message, Map<Integer, Integer> giverReceiverMap) {
		StringBuilder sb = new StringBuilder(message + ": ");
		giverReceiverMap.entrySet().forEach(x -> sb.append("[" + x.getKey() + ":" + x.getValue() + "] "));
		return sb.toString();
	}

	// Print out values.
	String printOutGiftExchangeValues(Map<Integer, Integer> giftExchangePairs, String[] familyMemberNames) {
		StringBuilder sb = new StringBuilder("\n");
		giftExchangePairs.forEach((k, v) -> sb
				.append(k + " : " + v + " - " + familyMemberNames[k] + " gifts to " + familyMemberNames[v] + "\n"));
		return sb.toString();
	}

}

