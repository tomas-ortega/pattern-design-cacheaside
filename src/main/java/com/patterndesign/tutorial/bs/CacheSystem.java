package com.patterndesign.tutorial.bs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import domain.UserAccountDTO;

public class CacheSystem {
	int capacity;
	Map<String, Node> cache = new HashMap<>();
	Node head;
	Node end;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CacheSystem.class);
	
	class Node {
		String userId;
	    UserAccountDTO userAccount;
	    Node previous;
	    Node next;
	    
	    public Node(String userId, UserAccountDTO userAccount) {
	    	this.userId = userId;
	    	this.userAccount = userAccount;
		}
	}
	
	public CacheSystem(int capacity) {
		this.capacity = capacity;
	}
	
	/**
	   * Get user account
	   */
	  public UserAccountDTO get(String userId) {
	    if (cache.containsKey(userId)) {
	      Node node = cache.get(userId);
	      remove(node);
	      setHead(node);
	      return node.userAccount;
	    }
	    return null;
	  }
	
	/* Remove node from linked list. */
	public void remove(Node node) {
		if (node.previous != null) {
			node.previous.next = node.next;
		} else {
			this.head = node.next;
		}
		
		if (node.next != null) {
			node.next.previous = node.previous;
		} else {
			this.end = node.previous;
		}
	}
	
	/*Move node to the front of the list*/
	public void setHead(Node node) {
		node.next = head;
		node.previous = null;
		
		if (this.head != null) {
			head.previous = node;
		}
		
		this.head = node;
		
		if (end == null) {
			this.end = head;
		}
	}
	
	/* Set user account */
	public void set(String userId, UserAccountDTO userAccount) {
		if (this.cache.containsKey(userId)) {
			Node oldNode = this.cache.get(userId);
			oldNode.userAccount = userAccount;
			
			this.remove(oldNode);
			this.setHead(oldNode);
		} else {
			Node newNode = new Node(userId, userAccount);
			
			if (this.cache.size() >= this.capacity) {
				LOGGER.info("# Cache is FULL! Removing {} from cache..." + this.end.userId);
				this.cache.remove(this.end.userId);
				this.remove(this.end);
				this.setHead(newNode);
			} else {
				this.setHead(newNode);
			}
			
			this.cache.put(userId, newNode);
		}
	}
	
	public boolean contains(String userId) {
	    return cache.containsKey(userId);
	  }

	  /**
	   * Invalidate cache for user
	   */
	  public void invalidate(String userId) {
	    Node toBeRemoved = cache.remove(userId);
	    if (toBeRemoved != null) {
	      LOGGER.info("# Has been updated! Removing older version from cache..." + userId);
	      
	      remove(toBeRemoved);
	    }
	  }

	  public boolean isFull() {
	    return cache.size() >= capacity;
	  }

	  public UserAccountDTO getLatestRecordUserData() {
	    return end.userAccount;
	  }

	  /**
	   * Clear cache
	   */
	  public void clear() {
	    head = null;
	    end = null;
	    cache.clear();
	  }

	  /**
	   * Returns cache data in list form.
	   */
	  public List<UserAccountDTO> getCacheDataInListForm() {
	    List<UserAccountDTO> listOfCacheData = new ArrayList<>();
	    Node temp = head;
	    while (temp != null) {
	      listOfCacheData.add(temp.userAccount);
	      temp = temp.next;
	    }
	    return listOfCacheData;
	  }

	  /**
	   * Set cache capacity
	   */
	  public void setCapacity(int newCapacity) {
	    if (capacity > newCapacity) {
	      clear(); // Behavior can be modified to accommodate for decrease in cache size. For now, we'll
	               // just clear the cache.
	    } else {
	      this.capacity = newCapacity;
	    }
	  }
}
