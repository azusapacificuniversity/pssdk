package edu.apu.pssdk;

import java.util.HashMap;
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

  public CIPropertyInfoCollection getGetKeyInfoCollection() throws JOAException {
    return (CIPropertyInfoCollection) iCi.getProperty("GetKeyInfoCollection");
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

  public ProxyObject save(Map<String, Object> data) throws JOAException {
    // use to GET before SAVE
    Map<String, String> getProps = new HashMap<>();
    CIPropertyInfoCollection getKeyInfoCol = getGetKeyInfoCollection();
    for (long i = 0; i < getKeyInfoCol.getCount(); i++) {
      IObject getKeyPropInfo = getKeyInfoCol.item(i);
      String keyName = getKeyPropInfo.getProperty("Name").toString();
      getProps.put(keyName, (String) data.get(keyName));
    }
    this.get(getProps);
    // Get propertyInfoCollection
    CIPropertyInfoCollection propInfoCol = getPropertyInfoCollection();
    CiRow root = CiRow.factory(iCi, propInfoCol);
    root.unParse(data);

    if (((Boolean) (iCi.invokeMethod("Save", new Object[0]))).booleanValue()) {
      return this.get(getProps);
    }
    throw new JOAException("Unable to save object");
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
