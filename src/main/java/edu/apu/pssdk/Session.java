package edu.apu.pssdk;

import java.util.Map;
import psft.pt8.joa.ISession;
import psft.pt8.joa.JOAException;

/** A wrapper for PSJOA ISession with a CI factory class */
public class Session {

  /** holds the PSJOA ISession */
  private ISession iSession;

  /**
   * Constructor for the wrapper
   *
   * @param iSession PSJOA ISession
   */
  public Session(ISession iSession) {
    this.iSession = iSession;
  }

  /**
   * A factory method to create CI
   *
   * @param ciName the string name of the Component Interface we want to integrate with
   * @param options Map of options for CI creation, supported options:
   *     <ul>
   *       <li>"InteractiveMode": Boolean to enable/disable interactive mode (default: false)
   *       <li>"GetHistoryItems": Boolean to get history items in GET/SAVE (default: false)
   *       <li>"EditHistoryItems": Boolean to enable editing of history items (default: false)
   *       <li>"StopOnFirstError": Boolean to stop processing on first error (default: false)
   *       <li>"GetDummyRows": Boolean to generate dummy rows in CREATE operations (default: true)
   *     </ul>
   *
   * @return a CI object with standard CI methods to operate upon and integrate with
   * @throws JOAException when JOAEception occurs
   */
  public CI ciFactory(String ciName, Map<String, Boolean> options) throws JOAException {
    Object oCi = iSession.getCompIntfc(ciName);
    return CI.factory(oCi, iSession)
        .setInteractiveMode(options.getOrDefault("InteractiveMode", false))
        .setGetHistoryItems(options.getOrDefault("GetHistoryItems", false))
        .setEditHistoryItems(options.getOrDefault("EditHistoryItems", false))
        .setStopOnFirstError(options.getOrDefault("StopOnFirstError", false))
        .setGetDummyRows(options.getOrDefault("GetDummyRows", true));
  }
}
