package com.patterndesign.tutorial;

import java.text.ParseException;

import com.patterndesign.tutorial.bs.CacheStoreBS;
import com.patterndesign.tutorial.bs.CachingPolicy;
import com.patterndesign.tutorial.bs.DBConnection;

import domain.UserAccountDTO;

public class AppManager {
	private static CachingPolicy cachingPolicy;
	
	private AppManager() {
		
	}
	
	/**
	   *
	   * Developer/Tester is able to choose whether the application should use MongoDB as its underlying
	   * data storage or a simple Java data structure to (temporarily) store the data/objects during
	   * runtime.
	   */
	  public static void initDb(boolean useMongoDb) {
	    if (useMongoDb) {
	      try {
	        DBConnection.connect();
	      } catch (ParseException e) {
	        e.printStackTrace();
	      }
	    } else {
	    	DBConnection.createVirtualDb();
	    }
	  }

	  /**
	   * Initialize caching policy
	   */
	  public static void initCachingPolicy(CachingPolicy policy) {
	    cachingPolicy = policy;
	    if (cachingPolicy == CachingPolicy.BEHIND) {
	      Runtime.getRuntime().addShutdownHook(new Thread(CacheStoreBS::flushCache));
	    }
	    CacheStoreBS.clearCache();
	  }

	  public static void initCacheCapacity(int capacity) {
	    CacheStoreBS.initCapacity(capacity);
	  }

	  /**
	   * Find user account
	   */
	  public static UserAccountDTO find(String userId) {
	    if (cachingPolicy == CachingPolicy.THROUGH || cachingPolicy == CachingPolicy.AROUND) {
	      return CacheStoreBS.readThrough(userId);
	    } else if (cachingPolicy == CachingPolicy.BEHIND) {
	      return CacheStoreBS.readThroughWithWriteBackPolicy(userId);
	    } else if (cachingPolicy == CachingPolicy.ASIDE) {
	      return findAside(userId);
	    }
	    return null;
	  }

	  /**
	   * Save user account
	   */
	  public static void save(UserAccountDTO userAccount) {
	    if (cachingPolicy == CachingPolicy.THROUGH) {
	    	CacheStoreBS.writeThrough(userAccount);
	    } else if (cachingPolicy == CachingPolicy.AROUND) {
	    	CacheStoreBS.writeAround(userAccount);
	    } else if (cachingPolicy == CachingPolicy.BEHIND) {
	    	CacheStoreBS.writeBehind(userAccount);
	    } else if (cachingPolicy == CachingPolicy.ASIDE) {
	      saveAside(userAccount);
	    }
	  }

	  public static String printCacheContent() {
	    return CacheStoreBS.print();
	  }

	  /**
	   * Cache-Aside save user account helper
	   */
	  private static void saveAside(UserAccountDTO userAccount) {
	    DBConnection.updateDb(userAccount);
	    CacheStoreBS.invalidate(userAccount.getUserId());
	  }

	  /**
	   * Cache-Aside find user account helper
	   */
	  private static UserAccountDTO findAside(String userId) {
	    UserAccountDTO userAccount = CacheStoreBS.get(userId);
	    if (userAccount != null) {
	      return userAccount;
	    }

	    userAccount = DBConnection.readFromDb(userId);
	    if (userAccount != null) {
	      CacheStoreBS.set(userId, userAccount);
	    }

	    return userAccount;
	  }
}
