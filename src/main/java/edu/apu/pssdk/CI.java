package edu.apu.pssdk;

import java.util.Map;
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

  /*********************************/
  /***** PROPERTY COLLECTIONS ******/
  /*********************************/

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

  /*********************************/
  /****** STANDARD METHODS ********/
  /*********************************/

  public CI get(Map<String, Object> props) throws PssdkException {
    try {
      CiRow createRoot = CiRow.factory(iCi, getGetKeyInfoCollection());
      createRoot.populateWith(props);
      if (!((Boolean) (iCi.invokeMethod("Get", new Object[0]))).booleanValue()) {
        throw new PssdkException("Unable to perform get on CI.", iSession);
      }
      return this;
    } catch (JOAException e) {
      throw new PssdkException(
          "Unable to perform get on CI. Original error enclosed.", e, iSession);
    }
  }

  public CI set(Map<String, Object> data) throws PssdkException {
    try {
      // unparse
      CiRow root = CiRow.factory(iCi, getPropertyInfoCollection());
      root.populateWith(data);
      return this;
    } catch (Exception e) {
      throw new PssdkException("Unable to set data on CI. Original error enclosed.", e, iSession);
    }
  }

  public CI find(Map<String, Object> props) throws PssdkException {
    try {
      for (Map.Entry<String, Object> entry : props.entrySet())
        iCi.setProperty(entry.getKey(), entry.getValue());
      Object[] args = new Object[0];
      if (!((Boolean) (iCi.invokeMethod("Find", args))).booleanValue()) {
        throw new PssdkException("Unable to do a find on the CI.", iSession);
      }
      return this;
    } catch (JOAException e) {
      throw new PssdkException(
          "Unable to do a find on the CI. Original error enclosed.", e, iSession);
    }
  }

  public CI save(Map<String, Object> data) throws PssdkException {
    try {
      // unparse
      if (data != null) this.set(data);
      // invoke save on the CI
      if (!((Boolean) (iCi.invokeMethod("Save", new Object[0]))).booleanValue()) {
        throw new PssdkException("Unable to save object", iSession);
      }
      return this;
    } catch (JOAException e) {
      throw new PssdkException("Unable to save object. Original error enclosed.", e, iSession);
    }
  }

  public CI create(Map<String, Object> data) throws PssdkException {
    try {
      CiRow createRoot = CiRow.factory(iCi, getCreateKeyInfoCollection());
      createRoot.populateWith(data);
      if (!((Boolean) (iCi.invokeMethod("Create", new Object[0]))).booleanValue()) {
        throw new PssdkException(
            "Can not perform Operation CREATE on the CI. Probably, an attempt to create duplicate entry",
            iSession);
      }
      return this;
    } catch (JOAException e) {
      if (e.getMessage().contains("Distributed Object Manager: Page=Create"))
        throw new PssdkException("Operation CREATE not supported by the CI", e, iSession);
      throw new PssdkException(
          "Can not perform Operation CREATE on the CI. Original error enclosed.", e, iSession);
    }
  }

  public CI cancel() throws PssdkException {
    try {
      if (!((Boolean) (iCi.invokeMethod("Cancel", new Object[0]))).booleanValue()) {
        throw new PssdkException("Operation CANCEL failed.", iSession);
      }
      return this;
    } catch (JOAException e) {
      throw new PssdkException("Operation CANCEL failed. Original error enclosed.", e, iSession);
    }
  }

  /*********************************/
  /*********** TO DATA *************/
  /*********************************/

  /**
   * Gets the data out of the CI as an object that is native to the GraalVM client language.
   * The data returned should be easily serializable to JSON or other formats.
   *
   * @return A ProxyObject representing the data in the CI.
   * @throws JOAException If a JOA error occurs.
   */
  public ProxyObject toJSON() throws PssdkException {
    try {
      return CiRow.factory(iCi, getPropertyInfoCollection()).toProxy();
    } catch (JOAException e) {
      throw new PssdkException(
          "Unable to get data out of the CI. Original error enclosed.", e, iSession);
    }
  }

  /**
   * Gets the data out of the CI as a list/array that is native to your language. The data returned
   * should be easily serializable to JSON or other formats. This method is intended for use after a
   * Find operation.
   *
   * @return A ProxyArray representing the data in the CI.
   * @throws JOAException If a JOA error occurs.
   */
  public ProxyArray toList() throws PssdkException {
    try {
      return CiScroll.factory(iCi, getFindPropertyInfoCollection()).toProxy();
    } catch (JOAException e) {
      throw new PssdkException(
          "Unable to get list data out of the CI. Original error enclosed.", e, iSession);
    }
  }

  /*********************************/
  /******** HELPER METHODS *********/
  /*********************************/

  /**
   * Helper method to get a new object and return its data.
   *
   * @param data The data for the new object.
   * @return A ProxyObjectMap representing the object.
   * @throws PssdkException If a PSDK specific error occurs.
   */
  public ProxyObjectMap fetch(Map<String, Object> data) throws PssdkException {
    try {
      return this.get(data).toData();
    } finally {
      this.cancel();
    }
  }

  /**
   * Helper method to create and then save a new object.
   *
   * @param data The data for the new object.
   * @return A ProxyObjectMap representing the saved object.
   * @throws PssdkException If a PSDK specific error occurs.
   */
  public ProxyObjectMap add(Map<String, Object> data) throws PssdkException {
    try {
      return this.create(data).save(data).toData();
    } finally {
      this.cancel();
    }
  }

  /**
   * Helper method to get and then save an object.
   *
   * @param data The data for the object to update.
   * @return A ProxyObject representing the saved object.
   * @throws PssdkException If a PSDK specific error occurs.
   */
  public ProxyObjectMap update(Map<String, Object> data) throws PssdkException {
    try {
      return this.get(data).save(data).toData();
    } finally {
      this.cancel();
    }
  }

  public ProxyArray search(Map<String, Object> data) throws JOAException, PssdkException {
    return this.find(data).toList();
  }

  /*********************************/
  /********** CI OPTIONS ***********/
  /*********************************/

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
