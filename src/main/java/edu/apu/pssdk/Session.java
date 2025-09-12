package edu.apu.pssdk;

import java.util.Map;
import psft.pt8.joa.ISession;
import psft.pt8.joa.JOAException;

public class Session {

  private ISession iSession;

  public Session(ISession iSession) {
    this.iSession = iSession;
  }

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
