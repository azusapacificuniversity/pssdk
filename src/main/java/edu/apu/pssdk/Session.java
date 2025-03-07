package edu.apu.pssdk;

import java.util.Map;

import psft.pt8.joa.IObject;
import psft.pt8.joa.ISession;
import psft.pt8.joa.JOAException;

public class Session {

  private ISession iSession;

  public Session(ISession iSession) {
    this.iSession = iSession;
  }

  public Ci getCompIntfc(String ciName, Map<String, Boolean> options) throws JOAException {

    IObject compIntfc = (IObject) this.iSession.getCompIntfc(ciName);

    if (compIntfc == null) {
      throw new JOAException("Unable to Get Component Interface");
    }

    compIntfc.setProperty("InteractiveMode", options.getOrDefault("InteractiveMode", false));
    compIntfc.setProperty("GetHistoryItems", options.getOrDefault("GetHistoryItems", false));
    compIntfc.setProperty("EditHistoryItems", options.getOrDefault("EditHistoryItems", false));


    return new Ci(compIntfc);
  }

}
