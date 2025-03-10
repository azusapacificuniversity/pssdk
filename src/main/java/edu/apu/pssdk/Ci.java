package edu.apu.pssdk;

import java.util.Map;
import org.graalvm.polyglot.proxy.ProxyObject;
import psft.pt8.joa.CIPropertyInfoCollection;
import psft.pt8.joa.IObject;
import psft.pt8.joa.JOAException;

public class Ci {
  IObject iCi;

  public Ci(IObject iCi) throws JOAException {
    if (iCi == null) {
      throw new JOAException("Unable to Get Component Interface");
    }
    this.iCi = iCi;
  }

  public static Ci factory(Object obj) throws JOAException {
    return new Ci((IObject) obj);
  }

  public CIPropertyInfoCollection getPropertyInfoCollection() throws JOAException {
    return (CIPropertyInfoCollection) iCi.getProperty("PropertyInfoCollection");
  }

  public ProxyObject get(Map<String, String> props) throws JOAException {
    for (Map.Entry<String, String> entry : props.entrySet())
      iCi.setProperty(entry.getKey(), entry.getValue());
    if (((Boolean) (iCi.invokeMethod("Get", new Object[0]))).booleanValue()) {
      // create a CiRow as the ROOT Row for this CI
      return CiRow.factory(iCi, getPropertyInfoCollection()).parse();
    }
    throw new JOAException("Unable to get object");
  }

  public boolean getInteractiveMode() throws JOAException {
    return (boolean) iCi.getProperty("InteractiveMode");
  }

  public Ci setInteractiveMode(boolean inInteractiveMode) throws JOAException {
    iCi.setProperty("InteractiveMode", inInteractiveMode);
    return this;
  }

  public boolean getGetHistoryItems() throws JOAException {
    return (boolean) iCi.getProperty("GetHistoryItems");
  }

  public Ci setGetHistoryItems(boolean inGetHistoryItems) throws JOAException {
    iCi.setProperty("GetHistoryItems", inGetHistoryItems);
    return this;
  }

  public boolean getEditHistoryItems() throws JOAException {
    return (boolean) iCi.getProperty("EditHistoryItems");
  }

  public Ci setEditHistoryItems(boolean inEditHistoryItems) throws JOAException {
    iCi.setProperty("EditHistoryItems", inEditHistoryItems);
    return this;
  }
}
