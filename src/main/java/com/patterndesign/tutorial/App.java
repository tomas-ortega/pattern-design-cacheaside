package com.patterndesign.tutorial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patterndesign.tutorial.bs.CachingPolicy;

import domain.UserAccountDTO;

/**
 * Hello world!
 *
 */
public class App 
{
	 private static final Logger LOGGER = LoggerFactory.getLogger(App.class);


	  /**
	   * Program entry point
	   *
	   * @param args command line args
	   */
	  public static void main(String[] args) {
	    AppManager.initDb(false); // VirtualDB (instead of MongoDB) was used in running the JUnit tests
	                              // and the App class to avoid Maven compilation errors. Set flag to
	                              // true to run the tests with MongoDB (provided that MongoDB is
	                              // installed and socket connection is open).
	    AppManager.initCacheCapacity(3);
	    App app = new App();
	   /* app.useReadAndWriteThroughStrategy();
	    app.useReadThroughAndWriteAroundStrategy();
	    app.useReadThroughAndWriteBehindStrategy();*/
	    app.useCacheAsideStategy();
	  }

	  /**
	   * Read-through and write-through
	   */
	  public void useReadAndWriteThroughStrategy() {
	    LOGGER.info("# CachingPolicy.THROUGH");
	    AppManager.initCachingPolicy(CachingPolicy.THROUGH);

	    UserAccountDTO userAccount1 = new UserAccountDTO("001", "John", "He is a boy.");

	    AppManager.save(userAccount1);
	    LOGGER.info(AppManager.printCacheContent());
	    AppManager.find("001");
	    AppManager.find("001");
	  }

	  /**
	   * Read-through and write-around
	   */
	  public void useReadThroughAndWriteAroundStrategy() {
	    LOGGER.info("# CachingPolicy.AROUND");
	    AppManager.initCachingPolicy(CachingPolicy.AROUND);

	    UserAccountDTO userAccount2 = new UserAccountDTO("002", "Jane", "She is a girl.");

	    AppManager.save(userAccount2);
	    LOGGER.info(AppManager.printCacheContent());
	    AppManager.find("002");
	    
	    LOGGER.info(AppManager.printCacheContent());
	    userAccount2 = AppManager.find("002");
	    userAccount2.setUserName("Jane G.");
	    
	    AppManager.save(userAccount2);
	    LOGGER.info(AppManager.printCacheContent());
	    AppManager.find("002");
	    
	    LOGGER.info(AppManager.printCacheContent());
	    AppManager.find("002");
	  }

	  /**
	   * Read-through and write-behind
	   */
	  public void useReadThroughAndWriteBehindStrategy() {
	    LOGGER.info("# CachingPolicy.BEHIND");
	    AppManager.initCachingPolicy(CachingPolicy.BEHIND);

	    UserAccountDTO userAccount3 = new UserAccountDTO("003", "Adam", "He likes food.");
	    UserAccountDTO userAccount4 = new UserAccountDTO("004", "Rita", "She hates cats.");
	    UserAccountDTO userAccount5 = new UserAccountDTO("005", "Isaac", "He is allergic to mustard.");

	    AppManager.save(userAccount3);
	    AppManager.save(userAccount4);
	    AppManager.save(userAccount5);
	    LOGGER.info(AppManager.printCacheContent());
	    AppManager.find("003");
	    
	    LOGGER.info(AppManager.printCacheContent());
	    UserAccountDTO userAccount6 = new UserAccountDTO("006", "Yasha", "She is an only child.");
	    
	    AppManager.save(userAccount6);
	    LOGGER.info(AppManager.printCacheContent());
	    
	    AppManager.find("004");
	    LOGGER.info(AppManager.printCacheContent());
	  }

	  /**
	   * Cache-Aside
	   */
	  public void useCacheAsideStategy() {
	    LOGGER.info("# CachingPolicy.ASIDE");
	    AppManager.initCachingPolicy(CachingPolicy.ASIDE);
	    LOGGER.info(AppManager.printCacheContent());

	    UserAccountDTO userAccount3 = new UserAccountDTO("003", "Adam", "He likes food.");
	    UserAccountDTO userAccount4 = new UserAccountDTO("004", "Rita", "She hates cats.");
	    UserAccountDTO userAccount5 = new UserAccountDTO("005", "Isaac", "He is allergic to mustard.");
	    
	    AppManager.save(userAccount3);
	    AppManager.save(userAccount4);
	    AppManager.save(userAccount5);

	    LOGGER.info(AppManager.printCacheContent());
	    AppManager.find("003");
	    
	    LOGGER.info(AppManager.printCacheContent());
	    AppManager.find("004");
	    
	    LOGGER.info(AppManager.printCacheContent());
	  }
}
