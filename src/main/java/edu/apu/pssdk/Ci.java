package edu.apu.pssdk;

import java.util.HashMap;
import java.util.Map;
import org.graalvm.polyglot.proxy.ProxyArray;
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

  public PropertyInfoCollection getPropertyInfoCollection() throws JOAException {
    return PropertyInfoCollection.factory(
        (CIPropertyInfoCollection) iCi.getProperty("PropertyInfoCollection"));
  }

  public PropertyInfoCollection getFindPropertyInfoCollection() throws JOAException {
    return PropertyInfoCollection.factory(
        (CIPropertyInfoCollection) iCi.getProperty("FindKeyInfoCollection"));
  }

  public PropertyInfoCollection getGetKeyInfoCollection() throws JOAException {
    return PropertyInfoCollection.factory(
        (CIPropertyInfoCollection) iCi.getProperty("GetKeyInfoCollection"));
  }

  public PropertyInfoCollection getCreateKeyInfoCollection() throws JOAException {
    return PropertyInfoCollection.factory(
        (CIPropertyInfoCollection) iCi.getProperty("CreateKeyInfoCollection"));
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

  public ProxyArray find(Map<String, String> props) throws JOAException {
    for (Map.Entry<String, String> entry : props.entrySet())
      iCi.setProperty(entry.getKey(), entry.getValue());
    Object[] args = new Object[0];
    return CiScroll.factory(iCi.invokeMethod("Find", args), getFindPropertyInfoCollection())
        .parse();
  }

  public ProxyObject save(Map<String, Object> data) throws JOAException {

    // use to GET before SAVE
    Map<String, String> getProps = new HashMap<>();
    for (PropertyInfo getKey : getGetKeyInfoCollection()) {
      String keyName = getKey.getName();
      getProps.put(keyName, (String) data.get(keyName));
    }
    this.get(getProps);

    // unparse
    CiRow root = CiRow.factory(iCi, getPropertyInfoCollection());
    root.unParse(data);

    // invoke save on the CI
    if (((Boolean) (iCi.invokeMethod("Save", new Object[0]))).booleanValue()) {
      return CiRow.factory(iCi, getPropertyInfoCollection()).parse();
    }
    throw new JOAException("Unable to save object");
  }

  public ProxyObject create(Map<String, Object> data) throws JOAException {
    CiRow createRoot = CiRow.factory(iCi, getCreateKeyInfoCollection());
    createRoot.unParse(data);
    if (!((Boolean) (iCi.invokeMethod("Create", new Object[0]))).booleanValue()) {
      throw new JOAException("Operation CREATE not supported by the CI");
    }

    // unparse
    CiRow root = CiRow.factory(iCi, getPropertyInfoCollection());
    root.unParse(data);

    // invoke save on the CI
    if (((Boolean) (iCi.invokeMethod("Save", new Object[0]))).booleanValue()) {
      return CiRow.factory(iCi, getPropertyInfoCollection()).parse();
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
