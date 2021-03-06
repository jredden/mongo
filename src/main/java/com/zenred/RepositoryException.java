package com.zenred;
/**
 * 
 * 
 * @author jredden
 *
 * This exception is not exposed past the data_access package
 */
public class RepositoryException extends Exception {

	private static final long serialVersionUID = 1L;

	public RepositoryException() {
		super();
	}
	public RepositoryException(String message) {
		super(message);
	}

	public RepositoryException(String message, Exception e) {
		super(message, e);
	}

}
