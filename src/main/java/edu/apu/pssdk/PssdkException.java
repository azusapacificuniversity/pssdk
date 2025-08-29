package edu.apu.pssdk;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;
import psft.pt8.joa.IPSMessage;
import psft.pt8.joa.IPSMessageCollection;
import psft.pt8.joa.ISession;

/** Custom exception class for PeopleSoft SDK operations. */
public class PssdkException extends Exception {

  private Collection<String> psMessages;

  public PssdkException(ISession session) {
    this(null, null, session);
  }

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message
   */
  public PssdkException(String message, ISession session) {
    this(message, null, session);
  }

  /**
   * Constructs a new exception with the specified cause.
   *
   * @param cause the cause of this exception
   */
  public PssdkException(Throwable cause, ISession session) {
    this(null, cause, session);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause of this exception
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
    StringJoiner sj = new StringJoiner("\n", Strings.nullToEmpty(super.getMessage()) + "\n", "");
    sj.setEmptyValue(Strings.nullToEmpty(super.getMessage()));
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
