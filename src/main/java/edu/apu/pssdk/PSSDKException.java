package edu.apu.pssdk;

/** Custom exception class for PeopleSoft SDK operations. */
public class PSSDKException extends Exception {

  /** Constructs a new exception with null as its detail message. */
  public PSSDKException() {
    super();
  }

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message
   */
  public PSSDKException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause of this exception
   */
  public PSSDKException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause.
   *
   * @param cause the cause of this exception
   */
  public PSSDKException(Throwable cause) {
    super(cause);
  }
}
