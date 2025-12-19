package edu.apu.pssdk;

import java.util.Map;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import psft.pt8.joa.CIPropertyInfoCollection;
import psft.pt8.joa.IObject;
import psft.pt8.joa.ISession;
import psft.pt8.joa.JOAException;

/**
 * Represents a Component Interface (CI) in PeopleSoft, exposing methods which proxy stardard CI
 * operations GET, SET, FIND, SAVE, CREATE, and CANCEL. It wraps the CI object received from PSJOA.
 * Should be instantiated via the `AppServer.ciFactory()` methods.
 */
public class CI {
  IObject iCi;
  ISession iSession;

  /**
   * Constructor for CI wrapper class.
   *
   * @param iCi The underlying JOA IObject representing the CI.
   * @param session The ISession that created the CI.
   * @throws JOAException If the provided IObject is null.
   */
  public CI(IObject iCi, ISession session) throws JOAException {
    if (iCi == null) {
      throw new JOAException("Unable to Get Component Interface");
    }
    this.iCi = iCi;
    this.iSession = session;
  }

  /**
   * Factory method for the CI wrapper class.
   *
   * @param obj The JOA IObject representing the CI.
   * @param session The ISession that created the CI.
   * @return A CI instance wrapping the provided IObject.
   * @throws JOAException If unable to create the CI instance.
   */
  public static CI factory(Object obj, ISession session) throws JOAException {
    return new CI((IObject) obj, session);
  }

  /*********************************/
  /***** PROPERTY COLLECTIONS ******/
  /*********************************/

  /**
   * Gets the "PROPERTIES" PropertyInfoCollection for the root CI.
   *
   * @return The PropertyInfoCollection instance.
   * @throws JOAException If unable to retrieve the PropertyInfoCollection.
   */
  public PropertyInfoCollection getPropertyInfoCollection() throws JOAException {
    return PropertyInfoCollection.factory(
        (CIPropertyInfoCollection) iCi.getProperty("PropertyInfoCollection"));
  }

  /**
   * Gets the "FINDKEYS" PropertyInfoCollection for Find operations.
   *
   * @return The PropertyInfoCollection instance for Find operations.
   * @throws JOAException If unable to retrieve the PropertyInfoCollection.
   */
  public PropertyInfoCollection getFindPropertyInfoCollection() throws JOAException {
    return PropertyInfoCollection.factory(
        (CIPropertyInfoCollection) iCi.getProperty("FindKeyInfoCollection"));
  }

  /**
   * Gets the "GETKEYS" PropertyInfoCollection for Get operations.
   *
   * @return The PropertyInfoCollection instance for Get operations.
   * @throws JOAException If unable to retrieve the PropertyInfoCollection.
   */
  public PropertyInfoCollection getGetKeyInfoCollection() throws JOAException {
    return PropertyInfoCollection.factory(
        (CIPropertyInfoCollection) iCi.getProperty("GetKeyInfoCollection"));
  }

  /**
   * Gets the "CREATEKEYS" PropertyInfoCollection for Create operations.
   *
   * @return The PropertyInfoCollection instance for Create operations.
   * @throws JOAException If unable to retrieve the PropertyInfoCollection.
   */
  public PropertyInfoCollection getCreateKeyInfoCollection() throws JOAException {
    return PropertyInfoCollection.factory(
        (CIPropertyInfoCollection) iCi.getProperty("CreateKeyInfoCollection"));
  }

  /*********************************/
  /****** STANDARD METHODS ********/
  /*********************************/

  /**
   * Performs a Get operation on the CI using the provided properties as keys.
   *
   * @param props A map of property names and values to use as keys for the Get operation.
   * @return The CI instance.
   * @throws PssdkException If unable to perform the Get operation.
   */
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

  /**
   * While this is not a "standard" CI operation, it is treated as such in this library. Sets data
   * on the CI from the provided map.
   *
   * @param data A map of property names and values to set on the CI.
   * @return The CI instance.
   * @throws PssdkException If unable to set data on the CI.
   */
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

  /**
   * Performs a Find operation on the CI using the provided FINDKEYS properties as keys.
   *
   * @param props A map of property names and values to use as FINDKEYS keys for the Find operation.
   * @return The CI instance.
   * @throws PssdkException If unable to perform the Find operation.
   */
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

  /**
   * Performs a standard SAVE operation on the CI. You should call `create` or `get` before calling
   * this method. To provide the data, you can either call `set` or optionally provide data here as
   * an argument to do the `set` on the CI before saving.
   *
   * @param data Optional. A map of property names and values to save on the CI.
   * @return The CI instance.
   * @throws PssdkException If unable to perform the Save operation.
   */
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

  /**
   * Performs a standard CREATE operation on the CI using the provided CREATEKEYS keys.
   *
   * @param data A CREATEKEYS map to use for the Create operation.
   * @return The CI instance.
   * @throws PssdkException If unable to perform the Create operation.
   */
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

  /**
   * Invokes the standard CANCEL method on the CI, then closes the CI session.
   *
   * @throws PssdkException If unable to cancel the CI.
   */
  public void cancel() throws PssdkException {
    try {
      if (!((Boolean) (iCi.invokeMethod("Cancel", new Object[0]))).booleanValue()) {
        throw new PssdkException("Operation CANCEL failed.", iSession);
      }
      this.close();
      iCi = null;
    } catch (JOAException e) {
      throw new PssdkException("Operation CANCEL failed. Original error enclosed.", e, iSession);
    }
  }

  /**
   * Closes the CI session.
   *
   * @throws PssdkException If unable to close the Session which created the CI.
   */
  public void close() throws PssdkException {
    if (!iSession.disconnect()) {
      throw new PssdkException("Operation CANCEL failed.", iSession);
    }
    iSession = null;
    return;
  }

  /*********************************/
  /*********** TO DATA *************/
  /*********************************/

  /**
   * Gets the data out of the CI as an object that is native to the GraalVM client language. The
   * data returned should be easily serializable to JSON or other formats.
   *
   * @return A ProxyObject representing the data in the CI.
   * @throws PssdkException If unable to get data out of the CI.
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
   * @throws PssdkException If unable to get list data out of the CI.
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
  /********** CI OPTIONS ***********/
  /*********************************/

  /**
   * Gets to see if the CI is in interactive mode.
   *
   * @return true if interactive mode is enabled, false otherwise.
   * @throws JOAException if unable to get the InteractiveMode property.
   */
  public boolean getInteractiveMode() throws JOAException {
    return (boolean) iCi.getProperty("InteractiveMode");
  }

  /**
   * Sets the CI to run in interactive mode.
   *
   * @param inInteractiveMode true to enable interactive mode, false to disable it.
   * @return the CI instance
   * @throws JOAException if unable to set the InteractiveMode property.
   */
  public CI setInteractiveMode(boolean inInteractiveMode) throws JOAException {
    iCi.setProperty("InteractiveMode", inInteractiveMode);
    return this;
  }

  /**
   * Gets to see if the CI is set to GetHistoryItems.
   *
   * @return true if GetHistoryItems is enabled, false otherwise.
   * @throws JOAException if unable to get the GetHistoryItems property.
   */
  public boolean getGetHistoryItems() throws JOAException {
    return (boolean) iCi.getProperty("GetHistoryItems");
  }

  /**
   * Sets the CI to GetHistoryItems.
   *
   * @param inGetHistoryItems true to enable GetHistoryItems, false to disable it.
   * @return the CI instance
   * @throws JOAException if unable to set the GetHistoryItems property.
   */
  public CI setGetHistoryItems(boolean inGetHistoryItems) throws JOAException {
    iCi.setProperty("GetHistoryItems", inGetHistoryItems);
    return this;
  }

  /**
   * Gets to see if the CI is set to EditHistoryItems.
   *
   * @return true if EditHistoryItems is enabled, false otherwise.
   * @throws JOAException if unable to get the EditHistoryItems property.
   */
  public boolean getEditHistoryItems() throws JOAException {
    return (boolean) iCi.getProperty("EditHistoryItems");
  }

  /**
   * Sets the CI to EditHistoryItems.
   *
   * @param inEditHistoryItems true to enable EditHistoryItems, false to disable it.
   * @return the CI instance
   * @throws JOAException if unable to set the EditHistoryItems property.
   */
  public CI setEditHistoryItems(boolean inEditHistoryItems) throws JOAException {
    iCi.setProperty("EditHistoryItems", inEditHistoryItems);
    return this;
  }

  /**
   * Gets to see if the CI is set to GetDummyRows.
   *
   * @return true if GetDummyRows is enabled, false otherwise.
   * @throws JOAException if unable to get the GetDummyRows property.
   */
  public boolean getGetDummyRows() throws JOAException {
    return (boolean) iCi.getProperty("GetDummyRows");
  }

  /**
   * Sets the CI to GetDummyRows.
   *
   * @param inGetDummyRows true to enable GetDummyRows, false to disable it.
   * @return the CI instance
   * @throws JOAException if unable to set the GetDummyRows property.
   */
  public CI setGetDummyRows(boolean inGetDummyRows) throws JOAException {
    iCi.setProperty("GetDummyRows", inGetDummyRows);
    return this;
  }

  /**
   * Gets to see if the CI is set to StopOnFirstError.
   *
   * @return true if StopOnFirstError is enabled, false otherwise.
   * @throws JOAException if unable to get the StopOnFirstError property.
   */
  public boolean getStopOnFirstError() throws JOAException {
    return (boolean) iCi.getProperty("StopOnFirstError");
  }

  /**
   * Sets the CI to StopOnFirstError.
   *
   * @param inStopOnFirstError true to enable StopOnFirstError, false to disable it.
   * @return the CI instance
   * @throws JOAException if unable to set the StopOnFirstError property.
   */
  public CI setStopOnFirstError(boolean inStopOnFirstError) throws JOAException {
    iCi.setProperty("StopOnFirstError", inStopOnFirstError);
    return this;
  }
}
