package edu.apu.pssdk;

import java.util.Map;
import java.util.stream.Collectors;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import psft.pt8.joa.CIPropertyInfoCollection;
import psft.pt8.joa.IObject;
import psft.pt8.joa.ISession;
import psft.pt8.joa.JOAException;

public class CI {
  IObject iCi;
  ISession iSession;

  public CI(IObject iCi, ISession session) throws JOAException {
    if (iCi == null) {
      throw new JOAException("Unable to Get Component Interface");
    }
    this.iCi = iCi;
    this.iSession = session;
  }

  public static CI factory(Object obj, ISession session) throws JOAException {
    return new CI((IObject) obj, session);
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

  private ProxyObject get(Map<String, String> props) throws JOAException {
    for (Map.Entry<String, String> entry : props.entrySet())
      iCi.setProperty(entry.getKey(), entry.getValue());
    if (((Boolean) (iCi.invokeMethod("Get", new Object[0]))).booleanValue()) {
      // a "CiRow" acting the ROOT for this CI
      ProxyObject result = CiRow.factory(iCi, getPropertyInfoCollection()).toProxy();
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
    return result;
  }

  public CI set(Map<String, Object> data) throws JOAException {
    // unparse
    CiRow root = CiRow.factory(iCi, getPropertyInfoCollection());
    root.populateWith(data);
    return this;
  }

  public ProxyObject save(Map<String, Object> data) throws JOAException, PssdkException {
    // unparse
    if (data != null) this.set(data);
    // invoke save on the CI
    if (((Boolean) (iCi.invokeMethod("Save", new Object[0]))).booleanValue()) {
      ProxyObject result = CiRow.factory(iCi, getPropertyInfoCollection()).toProxy();
      return result;
    }
    throw new PssdkException("Unable to save object", iSession);
  }

  public CI create(Map<String, Object> data) throws JOAException, PssdkException {
    try {
      CiRow createRoot = CiRow.factory(iCi, getCreateKeyInfoCollection());
      createRoot.populateWith(data);
      if (!((Boolean) (iCi.invokeMethod("Create", new Object[0]))).booleanValue()) {
        throw new PssdkException("Attempt to create duplicate entry", iSession);
      }
      return this;
    } catch (JOAException e) {
      if (e.getMessage().contains("Distributed Object Manager: Page=Create"))
        throw new PssdkException("Operation CREATE not supported by the CI", e, iSession);
      throw e;
    }
  }

  /**
   * Helper method to create and then save a new object.
   *
   * @param data The data for the new object.
   * @return A ProxyObject representing the saved object.
   * @throws JOAException If a JOA error occurs.
   * @throws PssdkException If a PSDK specific error occurs.
   */
  public ProxyObject insert(Map<String, Object> data) throws JOAException, PssdkException {
    return this.create(data).save(data);
  }

  /**
   * Helper method to get and then save an object.
   *
   * @param data The data for the object to update.
   * @return A ProxyObject representing the saved object.
   * @throws JOAException If a JOA error occurs.
   * @throws PssdkException If a PSDK specific error occurs.
   */
  public ProxyObject update(Map<String, Object> data) throws JOAException, PssdkException {
    this.get(
        data.entrySet().stream()
            .filter(entry -> entry.getValue() instanceof String)
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> (String) entry.getValue())));
    return this.save(data);
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

  public boolean getGetDummyRows() throws JOAException {
    return (boolean) iCi.getProperty("GetDummyRows");
  }

  public CI setGetDummyRows(boolean inGetDummyRows) throws JOAException {
    iCi.setProperty("GetDummyRows", inGetDummyRows);
    return this;
  }

  public boolean getStopOnFirstError() throws JOAException {
    return (boolean) iCi.getProperty("StopOnFirstError");
  }

  public CI setStopOnFirstError(boolean inStopOnFirstError) throws JOAException {
    iCi.setProperty("StopOnFirstError", inStopOnFirstError);
    return this;
  }
}
