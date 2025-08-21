package edu.apu.pssdk;

import java.util.HashMap;
import java.util.Map;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import psft.pt8.joa.CIPropertyInfoCollection;
import psft.pt8.joa.IObject;
import psft.pt8.joa.JOAException;

public class CI {
  IObject iCi;

  public CI(IObject iCi) throws JOAException {
    if (iCi == null) {
      throw new JOAException("Unable to Get Component Interface");
    }
    this.iCi = iCi;
  }

  public static CI factory(Object obj) throws JOAException {
    return new CI((IObject) obj);
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
    return this.get(props, true);
  }

  private ProxyObject get(Map<String, String> props, boolean reset) throws JOAException {
    for (Map.Entry<String, String> entry : props.entrySet())
      iCi.setProperty(entry.getKey(), entry.getValue());
    if (((Boolean) (iCi.invokeMethod("Get", new Object[0]))).booleanValue()) {
      // a "CiRow" acting the ROOT for this CI
      ProxyObject result = CiRow.factory(iCi, getPropertyInfoCollection()).toProxy();
      if (reset) cancel(); // reset the CI
      return result;
    }
    throw new JOAException("Unable to get object");
  }

  public ProxyArray find(Map<String, String> props) throws JOAException {
    for (Map.Entry<String, String> entry : props.entrySet())
      iCi.setProperty(entry.getKey(), entry.getValue());
    Object[] args = new Object[0];
    ProxyArray result =
        CiScroll.factory(iCi.invokeMethod("Find", args), getFindPropertyInfoCollection()).toProxy();
    cancel();
    return result;
  }

  public ProxyObject save(Map<String, Object> data) throws JOAException {

    // use to GET before SAVE
    Map<String, String> getProps = new HashMap<>();
    for (PropertyInfo getKey : getGetKeyInfoCollection()) {
      String keyName = getKey.getName();
      getProps.put(keyName, (String) data.get(keyName));
    }
    this.get(getProps, false);

    // unparse
    CiRow root = CiRow.factory(iCi, getPropertyInfoCollection());
    root.populateWith(data);

    // invoke save on the CI
    if (((Boolean) (iCi.invokeMethod("Save", new Object[0]))).booleanValue()) {
      ProxyObject result = CiRow.factory(iCi, getPropertyInfoCollection()).toProxy();
      cancel();
      return result;
    }
    throw new JOAException("Unable to save object");
  }

  public ProxyObject create(Map<String, Object> data) throws JOAException, PssdkException {
    try {
      CiRow createRoot = CiRow.factory(iCi, getCreateKeyInfoCollection());
      createRoot.populateWith(data);
      if (!((Boolean) (iCi.invokeMethod("Create", new Object[0]))).booleanValue()) {
        throw new PssdkException("Attempt to create duplicate entry", iCi.getSession());
      }

      // unparse
      CiRow root = CiRow.factory(iCi, getPropertyInfoCollection());
      root.populateWith(data);

      // invoke save on the CI
      if (((Boolean) (iCi.invokeMethod("Save", new Object[0]))).booleanValue()) {
        return CiRow.factory(iCi, getPropertyInfoCollection()).toProxy();
      }
      throw new PssdkException("Unable to save object", iCi.getSession());
    } catch (JOAException e) {
      if (e.getMessage().contains("Distributed Object Manager: Page=Create"))
        throw new PssdkException("Operation CREATE not supported by the CI", e, iCi.getSession());
      throw e;
    }
  }

  public void cancel() throws JOAException {
    if (!((Boolean) (iCi.invokeMethod("Cancel", new Object[0]))).booleanValue()) {
      throw new JOAException("Operation CANCEL failed.");
    }
  }

  public boolean getInteractiveMode() throws JOAException {
    return (boolean) iCi.getProperty("InteractiveMode");
  }

  public CI setInteractiveMode(boolean inInteractiveMode) throws JOAException {
    iCi.setProperty("InteractiveMode", inInteractiveMode);
    return this;
  }

  public boolean getGetHistoryItems() throws JOAException {
    return (boolean) iCi.getProperty("GetHistoryItems");
  }

  public CI setGetHistoryItems(boolean inGetHistoryItems) throws JOAException {
    iCi.setProperty("GetHistoryItems", inGetHistoryItems);
    return this;
  }

  public boolean getEditHistoryItems() throws JOAException {
    return (boolean) iCi.getProperty("EditHistoryItems");
  }

  public CI setEditHistoryItems(boolean inEditHistoryItems) throws JOAException {
    iCi.setProperty("EditHistoryItems", inEditHistoryItems);
    return this;
  }
}
