package com.patterndesign.tutorial.bs;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import domain.UserAccountDTO;

public class DBConnection {
	  private static MongoClient mongoClient;
	  private static MongoDatabase db;
	  private static boolean useMongoDB;

	  private static Map<String, UserAccountDTO> virtualDB;

	  private DBConnection() {
	  }

	  /**
	   * Create DB
	   */
	  public static void createVirtualDb() {
	    useMongoDB = false;
	    virtualDB = new HashMap<>();
	  }

	  /**
	   * Connect to DB
	   */
	  public static void connect() throws ParseException {
	    DBConnection.useMongoDB = true;
	    DBConnection.mongoClient = new MongoClient();
	    DBConnection.db = mongoClient.getDatabase("test");
	  }

	  /**
	   * Read user account from DB
	   */
	  public static UserAccountDTO readFromDb(String userId) {
	    if (!useMongoDB) {
	      if (virtualDB.containsKey(userId)) {
	        return virtualDB.get(userId);
	      }
	      return null;
	    }
	    if (db == null) {
	      try {
	        connect();
	      } catch (ParseException e) {
	        e.printStackTrace();
	      }
	    }
	    FindIterable<Document> iterable =
	        db.getCollection("user_accounts").find(new Document("userID", userId));
	    if (iterable == null) {
	      return null;
	    }
	    
	    Document doc = iterable.first();
	    return new UserAccountDTO(userId, doc.getString("userName"), doc.getString("additionalInfo"));
	  }

	  /**
	   * Write user account to DB
	   */
	  public static void writeToDb(UserAccountDTO userAccount) {
	    if (!useMongoDB) {
	      virtualDB.put(userAccount.getUserId(), userAccount);
	      return;
	    }
	    
	    
	    if (db == null) {
	      try {
	        connect();
	      } catch (ParseException e) {
	        e.printStackTrace();
	      }
	    }
	    
	    
	    db.getCollection("user_accounts").insertOne(
	        new Document("userID", userAccount.getUserId()).append("userName",
	            userAccount.getUserName()).append("additionalInfo", userAccount.getAdditionalInfo()));
	  }

	  /**
	   * Update DB
	   */
	  public static void updateDb(UserAccountDTO userAccount) {
	    if (!useMongoDB) {
	      virtualDB.put(userAccount.getUserId(), userAccount);
	      return;
	    }
	    
	    if (db == null) {
	      try {
	        connect();
	      } catch (ParseException e) {
	        e.printStackTrace();
	      }
	    }
	    
	    db.getCollection("user_accounts").updateOne(
	        new Document("userID", userAccount.getUserId()),
	        new Document("$set", new Document("userName", userAccount.getUserName()).append(
	            "additionalInfo", userAccount.getAdditionalInfo())));
	  }

	  /**
	   *
	   * Insert data into DB if it does not exist. Else, update it.
	   */
	  public static void upsertDb(UserAccountDTO userAccount) {
	    if (!useMongoDB) {
	      virtualDB.put(userAccount.getUserId(), userAccount);
	      return;
	    }
	    
	    if (db == null) {
	      try {
	        connect();
	      } catch (ParseException e) {
	        e.printStackTrace();
	      }
	    }
	    
	    db.getCollection("user_accounts").updateOne(
	        new Document("userID", userAccount.getUserId()),
	        new Document("$set", new Document("userID", userAccount.getUserId()).append("userName",
	            userAccount.getUserName()).append("additionalInfo", userAccount.getAdditionalInfo())),
	        new UpdateOptions().upsert(true));
	  }
}
