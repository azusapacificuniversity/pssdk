package edu.apu.pssdk;

import java.lang.reflect.Field;
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
  String name;
  PropertyInfoCollection propInfoCol;
  boolean isKey;
  boolean isFindKey;
  boolean isListKey;
  boolean isReadOnly;
  boolean isRequired;
  boolean isCollection;

  /**
   * Constructor to initialize PropertyInfo with IObject.
   *
   * @param iPropInfo IObject representing the CI Property Info
   * @throws JOAException if initialization fails
   */
  public PropertyInfo(IObject iPropInfo) throws JOAException {
    try {
      this.name = iPropInfo.getProperty("Name").toString();
      // set the boolean fields isKey, isCollection, isReadOnly, and isRequired
      this.isKey = (boolean) iPropInfo.getProperty("Key");
      this.isCollection = (boolean) iPropInfo.getProperty("IsCollection");
      this.isReadOnly = (boolean) iPropInfo.getProperty("IsReadOnly");
      this.isRequired = (boolean) iPropInfo.getProperty("Required");

      // set the boolean fields isFindKey and isListKey properties
      for (Field field : iPropInfo.getClass().getDeclaredFields()) {
        if (field.getName().equals("m_fUseEdit")) {
          field.setAccessible(true);
          int useEdit = (int) field.get(iPropInfo);
          this.isFindKey = (useEdit & ALTERNATE_SEARCH_KEY) == ALTERNATE_SEARCH_KEY;
          this.isListKey = (useEdit & LISTBOX_ITEM_NUM) == LISTBOX_ITEM_NUM;
          break;
        }
      }

      if (this.isCollection) {
        this.propInfoCol =
            PropertyInfoCollection.factory(
                (IObject) iPropInfo.getProperty("PropertyInfoCollection"));
      }
    } catch (Exception e) {
      throw new JOAException(e);
    }
  }

  /**
   * Static factory method to create PropertyInfo from IObject.
   *
   * @param iProp IObject representing the CI Property Info
   * @return PropertyInfo instance
   * @throws JOAException if creation fails
   */
  public static PropertyInfo factory(IObject iProp) throws JOAException {
    return new PropertyInfo(iProp);
  }

  /**
   * A getter for the PropertyInfoCollection of the PropertyInfo.
   *
   * @return PropertyInfoCollection of the PropertyInfo
   * @throws JOAException if retrieval fails
   */
  public PropertyInfoCollection getPropertyInfoCollection() throws JOAException {
    return propInfoCol;
  }

  /**
   * Get the name of the property.
   *
   * @return name of the property
   * @throws JOAException if retrieval fails
   */
  public String getName() throws JOAException {
    return name;
  }

  /**
   * Check if the property is a key.
   *
   * @return true if the property is a key, false otherwise
   * @throws JOAException if retrieval fails
   */
  public boolean isKey() throws JOAException {
    return isKey;
  }

  /**
   * Check if the property is read-only.
   *
   * @return true if the property is read-only, false otherwise
   * @throws JOAException if retrieval fails
   */
  public boolean isReadOnly() throws JOAException {
    return isReadOnly;
  }

  /**
   * Check if the property is required to have a value.
   *
   * @return true if the property is required, false otherwise
   * @throws JOAException if retrieval fails
   */
  public boolean isRequired() throws JOAException {
    return isRequired;
  }

  /**
   * Check if the property is a collection.
   *
   * @return true if the property is a collection, false otherwise
   * @throws JOAException if retrieval fails
   */
  public boolean isCollection() throws JOAException {
    return isCollection;
  }

  /**
   * Check if the property is a list key. A list key gets populated after a find operation returns.
   *
   * @return true if the property is a list key, false otherwise
   * @throws JOAException if retrieval fails
   */
  public boolean isListKey() throws JOAException {
    return isListKey;
  }

  /**
   * Check if the property is part of FINDKEYS.
   *
   * @return true if the property is an alternate search key, false otherwise
   * @throws JOAException if retrieval fails
   */
  public boolean isFindKey() throws JOAException {
    return isFindKey;
  }
}
