package com.example.app.customers.exception;

/**
*Nov 4, 20237:25:44 PM
* 
*/
public class CustomerNotFoundException extends RuntimeException{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public CustomerNotFoundException(String message) {
		super(message);
	}
	public CustomerNotFoundException(Throwable cause) {
		super(cause);
	}
	public CustomerNotFoundException(String message, Throwable cause) {
		super(message,cause);
	}
}
