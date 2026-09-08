package edu.apu.pssdk;

import psft.pt8.joa.IObject;
import psft.pt8.joa.JOAException;

class PropertyInfoCatalog {

  PropertyInfoCollection props;
  PropertyInfoCollection getKeys;
  PropertyInfoCollection createKeys;
  PropertyInfoCollection findKeys;

  public PropertyInfoCatalog(
      PropertyInfoCollection props,
      PropertyInfoCollection getKeys,
      PropertyInfoCollection createKeys,
      PropertyInfoCollection findKeys) {
    this.props = props;
    this.getKeys = getKeys;
    this.createKeys = createKeys;
    this.findKeys = findKeys;
  }

  public static PropertyInfoCatalog buildFor(IObject iCi) {
    try {
      return new PropertyInfoCatalog(
          PropertyInfoCollection.factory((IObject) iCi.getProperty("PropertyInfoCollection")),
          PropertyInfoCollection.factory((IObject) iCi.getProperty("GetKeyInfoCollection")),
          PropertyInfoCollection.factory((IObject) iCi.getProperty("CreateKeyInfoCollection")),
          PropertyInfoCollection.factory((IObject) iCi.getProperty("FindKeyInfoCollection")));
    } catch (JOAException e) {
      throw new RuntimeException("Error building PropertyInfoCatalog: " + e.getMessage(), e);
    }
  }

  /**
   * Gets the "PROPERTIES" PropertyInfoCollection for the root CI.
   *
   * @return The PropertyInfoCollection instance for the root CI properties.
   */
  public PropertyInfoCollection getPropertyInfoCollection() throws JOAException {
    return props;
  }

  /**
   * Gets the "FINDKEYS" PropertyInfoCollection for Find operations.
   *
   * @return The PropertyInfoCollection instance for Find operations.
   */
  public PropertyInfoCollection getFindPropertyInfoCollection() throws JOAException {
    return findKeys;
  }

  /**
   * Gets the "GETKEYS" PropertyInfoCollection for Get operations.
   *
   * @return The PropertyInfoCollection instance for Get operations.
   */
  public PropertyInfoCollection getGetKeyInfoCollection() throws JOAException {
    return getKeys;
  }

  /**
   * Gets the "CREATEKEYS" PropertyInfoCollection for Create operations.
   *
   * @return The PropertyInfoCollection instance for Create operations.
   */
  public PropertyInfoCollection getCreateKeyInfoCollection() throws JOAException {
    return createKeys;
  }
}
