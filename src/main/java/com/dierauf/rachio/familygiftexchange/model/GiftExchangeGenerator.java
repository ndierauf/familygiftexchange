package com.dierauf.rachio.familygiftexchange.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author nicho
 * A Singleton class for generating family gift exchange pairs.
 * Can be considered a 'Service'.
 */
public class GiftExchangeGenerator {

	static final String ERROR_MESSAGE_MUST_HAVE_AT_LEAST_TWO_FAMILY_MEMBERS = "Must have at least two family members to generate gift exchange. ";
	private static final GiftExchangeGenerator INSTANCE = new GiftExchangeGenerator(); // Singleton created on class load.

	// Assures Singleton.
	private GiftExchangeGenerator() {};
	public static GiftExchangeGenerator instance() {
		return INSTANCE;
	}

	public Map<Integer, Integer> generateGiftExchanges(Set<Integer> familyMemberIds) throws Exception {

		// Parameter validation.
		if (familyMemberIds == null || familyMemberIds.size() < 2) {
			throw new Exception(ERROR_MESSAGE_MUST_HAVE_AT_LEAST_TWO_FAMILY_MEMBERS);
		}

		Set<Integer> giverIds = familyMemberIds;

		Random random = new Random();
		List<Integer> receiverIds = new ArrayList<>(giverIds);
		Map<Integer, Integer> giverReceiverMap = new HashMap<>();
		for (Integer giverId : giverIds) {
			// Temporarily remove giverId from receiverIds so that giver cannot be matched to itself.
			boolean isTemporarilyRemoved = receiverIds.remove(giverId);
			// receiverIds is now guaranteed not to have giverId.
 			Integer receiverId = (receiverIds.isEmpty()) ?
 				// If receiverIds.isEmpty() then we are on our last giverId and there are no receiverIds to choose from.
 				// Need to 'swap' a random receiverId from a previous selection with the current giverId.
				this.swapReceiverIdWithSomeoneElse(giverId, giverReceiverMap, random) :
				this.retrieveReceiverId(receiverIds, random);
			giverReceiverMap.put(giverId, receiverId);
			// Replace giverId from receiverIds if it had been temporarily removed.
			if (isTemporarilyRemoved) {
				receiverIds.add(giverId);
			}
		}

		return giverReceiverMap;
	}

	private Integer retrieveReceiverId(List<Integer> receiverIds, Random random) {
		int index = random.nextInt(receiverIds.size());
		Integer receiverId = receiverIds.remove(index);
		return receiverId;
	}

	private Integer swapReceiverIdWithSomeoneElse(Integer giverId, Map<Integer, Integer> giverReceiverMap, Random random) {
		// Select random index from keySet:
		Integer[] currentGiverIds = giverReceiverMap.keySet().toArray(new Integer[giverReceiverMap.size()]);
		int randomGiverId = random.nextInt(currentGiverIds.length);
		int newReceiverId = giverReceiverMap.get(randomGiverId);
		giverReceiverMap.put(randomGiverId, giverId);
		return newReceiverId;
	}

}
