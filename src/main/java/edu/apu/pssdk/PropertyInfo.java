package edu.apu.pssdk;

import java.lang.reflect.Field;
import psft.pt8.joa.CIPropertyInfoCollection;
import psft.pt8.joa.IObject;
import psft.pt8.joa.JOAException;

/**
 * Wrapper class for a PeopleSoft Component Interface Property Info (CI Property Info), which
 * provides metadata about a property in a CI, such as its name, whether it is a key, read-only,
 * required, or part of a collection.
 */
public class PropertyInfo {
  static final int ALTERNATE_SEARCH_KEY = 16;
  static final int LISTBOX_ITEM_NUM = 32;
  IObject iPropInfo;
  int useEdit;

  /**
   * Constructor to initialize PropertyInfo with IObject.
   *
   * @param iProp IObject representing the CI Property Info
   * @throws JOAException if initialization fails
   */
  public PropertyInfo(IObject iProp) throws JOAException {
    this.iPropInfo = iProp;
    try {
      Field[] declaredFields = iPropInfo.getClass().getDeclaredFields();
      for (Field field : declaredFields) {
        if (field.getName().equals("m_fUseEdit")) {
          field.setAccessible(true);
          useEdit = (int) field.get(iPropInfo);
        }
      }
    } catch (Exception e) {
      throw new JOAException("Can not access m_fUseEdit field");
    }
  }

  /**
   * Static factory method to create PropertyInfo from IObject.
   *
   * @param iProp IObject representing the CI Property Info
   * @return PropertyInfo instance
   * @throws JOAException if creation fails
   */
  public static PropertyInfo factory(Object iProp) throws JOAException {
    return new PropertyInfo((IObject) iProp);
  }

  /**
   * A getter for the PropertyInfoCollection of the PropertyInfo.
   *
   * @return PropertyInfoCollection of the PropertyInfo
   * @throws JOAException if retrieval fails
   */
  public PropertyInfoCollection getPropertyInfoCollection() throws JOAException {
    return PropertyInfoCollection.factory(
        (CIPropertyInfoCollection) iPropInfo.getProperty("PropertyInfoCollection"));
  }

  /**
   * Get the name of the property.
   *
   * @return name of the property
   * @throws JOAException if retrieval fails
   */
  public String getName() throws JOAException {
    return iPropInfo.getProperty("Name").toString();
  }

  /**
   * Check if the property is a key.
   *
   * @return true if the property is a key, false otherwise
   * @throws JOAException if retrieval fails
   */
  public boolean isKey() throws JOAException {
    return (boolean) iPropInfo.getProperty("Key");
  }

  /**
   * Check if the property is read-only.
   *
   * @return true if the property is read-only, false otherwise
   * @throws JOAException if retrieval fails
   */
  public boolean isReadOnly() throws JOAException {
    return (boolean) iPropInfo.getProperty("IsReadOnly");
  }

  /**
   * Check if the property is required to have a value.
   *
   * @return true if the property is required, false otherwise
   * @throws JOAException if retrieval fails
   */
  public boolean isRequired() throws JOAException {
    return (boolean) iPropInfo.getProperty("Required");
  }

  /**
   * Check if the property is a collection.
   *
   * @return true if the property is a collection, false otherwise
   * @throws JOAException if retrieval fails
   */
  public boolean isCollection() throws JOAException {
    return (boolean) iPropInfo.getProperty("IsCollection");
  }

  /**
   * Check if the property is a list key. A list key gets populated after a find operation returns.
   *
   * @return true if the property is a list key, false otherwise
   * @throws JOAException if retrieval fails
   */
  public boolean isListKey() throws JOAException {
    return (getUseEdit() & LISTBOX_ITEM_NUM) == LISTBOX_ITEM_NUM;
  }

  /**
   * Check if the property is part of FINDKEYS.
   *
   * @return true if the property is an alternate search key, false otherwise
   * @throws JOAException if retrieval fails
   */
  public boolean isFindKey() throws JOAException {
    return (getUseEdit() & ALTERNATE_SEARCH_KEY) == ALTERNATE_SEARCH_KEY;
  }

  /**
   * Get the useEdit flags of the property for bitwise operations for the other methods.
   *
   * @return useEdit flags
   * @throws JOAException if retrieval fails
   */
  private int getUseEdit() {
    return useEdit;
  }
}
