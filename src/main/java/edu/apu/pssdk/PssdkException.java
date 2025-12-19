package edu.apu.pssdk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;
import psft.pt8.joa.IPSMessage;
import psft.pt8.joa.IPSMessageCollection;
import psft.pt8.joa.ISession;

/** Custom exception class for PeopleSoft SDK operations. */
public class PssdkException extends Exception {

  /** a collection of Peoplesoft messages to be gathered from the Session */
  private Collection<String> psMessages;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param session the PSJOA ISession that created the CI, that holds PS messages
   */
  public PssdkException(ISession session) {
    this(null, null, session);
  }

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message
   * @param session the PSJOA ISession that created the CI, that holds PS messages
   */
  public PssdkException(String message, ISession session) {
    this(message, null, session);
  }

  /**
   * Constructs a new exception with the specified cause.
   *
   * @param cause the cause of this exception
   * @param session the PSJOA ISession that created the CI, that holds PS messages
   */
  public PssdkException(Throwable cause, ISession session) {
    this(null, cause, session);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause of this exception
   * @param session the PSJOA ISession that created the CI, that holds PS messages
   */
  public PssdkException(String message, Throwable cause, ISession session) {
    super(message, cause);
    psMessages = new ArrayList<String>();
    // ***** Display PeopleSoft Error Messages *****
    if (session.getErrorPending() || session.getWarningPending()) {
      IPSMessageCollection oPSMessageCollection;
      IPSMessage oPSMessage;

      oPSMessageCollection = session.getPSMessages();
      for (int i = 0; i < oPSMessageCollection.getCount(); i++) {
        oPSMessage = oPSMessageCollection.item(i);
        if (oPSMessage != null)
          psMessages.add(
              String.format(
                  "(%d,%d) : %s",
                  oPSMessage.getMessageSetNumber(),
                  oPSMessage.getMessageNumber(),
                  oPSMessage.getText()));
      }
      // ***** Done processing messages in the collection; OK to delete *****
      oPSMessageCollection.deleteAll();
    }
  }

  /**
   * Returns the detail message string of this throwable. If PeopleSoft messages are available, they
   * are appended to the original message.
   *
   * @return the detail message string of this {@code Throwable} instance.
   */
  @Override
  public String getMessage() {
    String mainMessage = super.getMessage() == null ? "" : super.getMessage();
    StringJoiner sj = new StringJoiner("\n", mainMessage + "\n", "");
    sj.setEmptyValue(mainMessage);
    if (psMessages != null && !psMessages.isEmpty()) {
      sj.add("Messages gathered from PeopleSoft session:");
      psMessages.forEach(sj::add);
    }
    return sj.toString();
  }

  /**
   * Returns the collection of PeopleSoft messages associated with this exception.
   *
   * @return a collection of PeopleSoft messages.
   */
  public Collection<String> getPsMessages() {
    return new ArrayList<String>(psMessages); // Return a copy to prevent external modification
  }
}
