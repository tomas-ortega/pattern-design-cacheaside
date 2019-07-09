package com.patterndesign.tutorial.bs;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import domain.UserAccountDTO;

public class CacheStoreBS {
	private static final Logger LOGGER = LoggerFactory.getLogger(CacheSystem.class);

	  static CacheSystem cache;

	  private CacheStoreBS() {
	  }

	  /**
	   * Init cache capacity
	   */
	  public static void initCapacity(int capacity) {
	    if (cache == null) {
	      cache = new CacheSystem(capacity);
	    } else {
	      cache.setCapacity(capacity);
	    }
	  }

	  /**
	   * Get user account using read-through cache
	   */
	  public static UserAccountDTO readThrough(String userId) {
	    if (cache.contains(userId)) {
	      LOGGER.info("# Cache Hit!");
	      return cache.get(userId);
	    }
	    LOGGER.info("# Cache Miss!");
	    UserAccountDTO userAccount = DBConnection.readFromDb(userId);
	    cache.set(userId, userAccount);
	    return userAccount;
	  }

	  /**
	   * Get user account using write-through cache
	   */
	  public static void writeThrough(UserAccountDTO userAccount) {
	    if (cache.contains(userAccount.getUserId())) {
	    	DBConnection.updateDb(userAccount);
	    } else {
	    	DBConnection.writeToDb(userAccount);
	    }
	    cache.set(userAccount.getUserId(), userAccount);
	  }

	  /**
	   * Get user account using write-around cache
	   */
	  public static void writeAround(UserAccountDTO userAccount) {
	    if (cache.contains(userAccount.getUserId())) {
	    	DBConnection.updateDb(userAccount);
	      cache.invalidate(userAccount.getUserId()); // Cache data has been updated -- remove older
	                                                 // version from cache.
	    } else {
	    	DBConnection.writeToDb(userAccount);
	    }
	  }

	  /**
	   * Get user account using read-through cache with write-back policy
	   */
	  public static UserAccountDTO readThroughWithWriteBackPolicy(String userId) {
	    if (cache.contains(userId)) {
	      LOGGER.info("# Cache Hit!");
	      return cache.get(userId);
	    }
	    LOGGER.info("# Cache Miss!");
	    UserAccountDTO userAccount = DBConnection.readFromDb(userId);
	    if (cache.isFull()) {
	      LOGGER.info("# Cache is FULL! Writing Cache data to DB...");
	      UserAccountDTO toBeWrittenToDb = cache.getLatestRecordUserData();
	      DBConnection.upsertDb(toBeWrittenToDb);
	    }
	    cache.set(userId, userAccount);
	    return userAccount;
	  }

	  /**
	   * Set user account
	   */
	  public static void writeBehind(UserAccountDTO userAccount) {
	    if (cache.isFull() && !cache.contains(userAccount.getUserId())) {
	      LOGGER.info("# Cache is FULL! Writing LRU data to DB...");
	      UserAccountDTO toBeWrittenToDb = cache.getLatestRecordUserData();
	      DBConnection.upsertDb(toBeWrittenToDb);
	    }
	    cache.set(userAccount.getUserId(), userAccount);
	  }

	  /**
	   * Clears cache
	   */
	  public static void clearCache() {
	    if (cache != null) {
	      cache.clear();
	    }
	  }

	  /**
	   * Writes remaining content in the cache into the DB.
	   */
	  public static void flushCache() {
	    LOGGER.info("# flushCache...");
	    if (null == cache) {
	      return;
	    }
	    List<UserAccountDTO> listOfUserAccounts = cache.getCacheDataInListForm();
	    for (UserAccountDTO userAccount : listOfUserAccounts) {
	    	DBConnection.upsertDb(userAccount);
	    }
	  }

	  /**
	   * Print user accounts
	   */
	  public static String print() {
	    List<UserAccountDTO> listOfUserAccounts = cache.getCacheDataInListForm();
	    StringBuilder sb = new StringBuilder();
	    sb.append("\n--CACHE CONTENT--\n");
	    for (UserAccountDTO userAccount : listOfUserAccounts) {
	      sb.append(userAccount.toString() + "\n");
	    }
	    sb.append("----\n");
	    return sb.toString();
	  }

	  /**
	   * Delegate to backing cache store
	   */
	  public static UserAccountDTO get(String userId) {
	    return cache.get(userId);
	  }

	  /**
	   * Delegate to backing cache store
	   */
	  public static void set(String userId, UserAccountDTO userAccount) {
	    cache.set(userId, userAccount);
	  }

	  /**
	   * Delegate to backing cache store
	   */
	  public static void invalidate(String userId) {
	    cache.invalidate(userId);
	  }
}
