package edu.apu.pssdk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import psft.pt8.joa.CIPropertyInfoCollection;
import psft.pt8.joa.IObject;
import psft.pt8.joa.JOAException;

/**
 * Wrapper class for a PeopleSoft Component Interface Row (CI Row). While it represents a single
 * record in a CI Scroll, it can also represent the root CI object.
 */
public class CiRow {
  IObject iRow;
  PropertyInfoCollection propInfoCol;
  Logger logger;

  /**
   * Constructor to initialize CiRow with IObject and PropertyInfoCollection.
   *
   * @param iRow IObject representing the CI Row (the root CI object or a row in a CI Scroll)
   * @param propInfoCol PropertyInfoCollection for the CI Row
   * @throws JOAException if initialization fails
   */
  public CiRow(IObject iRow, PropertyInfoCollection propInfoCol) throws JOAException {
    this.propInfoCol = propInfoCol;
    this.iRow = iRow;
    this.logger = LoggerFactory.getLogger(CiRow.class);
  }

  /**
   * Static factory method to create CiRow from IObject and JOA CIPropertyInfoCollection.
   *
   * @param iRow IObject representing the CI Row
   * @param propInfoCol CIPropertyInfoCollection for the CI Row
   * @return CiRow instance
   * @throws JOAException if creation fails
   */
  public static CiRow factory(Object iRow, CIPropertyInfoCollection propInfoCol)
      throws JOAException {
    return new CiRow((IObject) iRow, PropertyInfoCollection.factory(propInfoCol));
  }

  /**
   * Static factory method to create CiRow from IObject and PropertyInfoCollection.
   *
   * @param iRow IObject representing the CI Row
   * @param propInfoCol PropertyInfoCollection for the CI Row
   * @return CiRow instance
   * @throws JOAException if creation fails
   */
  public static CiRow factory(Object iRow, PropertyInfoCollection propInfoCol) throws JOAException {
    return new CiRow((IObject) iRow, propInfoCol);
  }

  /**
   * Check if the CI Row is empty (all properties have empty strings as values).
   *
   * @return true if the CI Row is empty, false otherwise
   * @throws JOAException if check fails
   */
  public boolean isEmpty() throws JOAException {
    boolean result = false;
    for (PropertyInfo pi : propInfoCol) {
      if (!pi.isKey()) continue;

      if (get(pi).toString().equals("")) {
        result = true;
        break;
      }
    }
    return result;
  }

  /**
   * Get property value in the CI Row
   *
   * @param prop PropertyInfo of the property
   * @return Object value of the property
   * @throws JOAException if retrieval fails
   */
  public Object get(PropertyInfo prop) throws JOAException {
    return get(prop.getName());
  }

  /**
   * Get property value in the CI Row
   *
   * @param propertyName Name of the property
   * @return Object value of the property
   * @throws JOAException if retrieval fails
   */
  public Object get(String propertyName) throws JOAException {
    return iRow.getProperty(propertyName);
  }

  /**
   * Set property value in the CI Row
   *
   * @param prop PropertyInfo of the property
   * @param val Object value to set
   * @return CiRow instance for method chaining
   * @throws JOAException if setting fails
   */
  public CiRow set(PropertyInfo prop, Object val) throws JOAException {
    return set(prop.getName(), val);
  }

  /**
   * Set property value in the CI Row
   *
   * @param propertyName Name of the property
   * @param val Object value to set
   * @return CiRow instance for method chaining
   * @throws JOAException if setting fails
   */
  public CiRow set(String propertyName, Object val) throws JOAException {
    this.logger.debug(propertyName + ": " + val.toString());
    iRow.setProperty(propertyName, val);
    return this;
  }

  /**
   * Count properties in the CI Row
   *
   * @return long count of properties
   * @throws JOAException if counting fails
   */
  public long count() throws JOAException {
    return ((Long) iRow.getProperty("Count")).longValue();
  }

  /**
   * A getter for the PropertyInfoCollection of the CI Row (set in the constructor).
   *
   * @return PropertyInfoCollection instance
   */
  public PropertyInfoCollection getPropertyInfoCollection() {
    return propInfoCol;
  }

  /**
   * Check if the incoming map matches the key properties of the CI Row.
   *
   * @param incoming Map of incoming property values
   * @return Boolean indicating if the incoming map matches the CI Row keys
   * @throws JOAException if matching fails
   */
  public Boolean isMatch(Map<String, Object> incoming) throws JOAException {
    for (PropertyInfo key : propInfoCol.keys()) {
      Object incomingVal = incoming.get(key.getName());
      if (incomingVal == null) return false;
      if (!get(key).toString().equals(incomingVal.toString())) return false;
    }
    return true;
  }

  /**
   * Find a matching map in the incoming list based on CI Row keys.
   *
   * @param incomingList List of maps to search
   * @return Matching map if found, otherwise null
   * @throws JOAException if searching fails
   */
  public Map<String, Object> findIn(List<Map<String, Object>> incomingList) throws JOAException {
    return findIn(incomingList, false);
  }

  /**
   * Find a matching map in the incoming list based on CI Row keys, with option to delete found.
   *
   * @param incomingList List of maps to search
   * @param deleteFound Boolean indicating if found map should be deleted from the list
   * @return Matching map if found, otherwise null
   * @throws JOAException if searching fails
   */
  public Map<String, Object> findIn(List<Map<String, Object>> incomingList, boolean deleteFound)
      throws JOAException {
    for (Map<String, Object> incoming : incomingList) {
      // TODO: this should be performed in CiScroll, not here
      if (isMatch(incoming)) {
        if (deleteFound) incomingList.remove(incoming);
        return incoming;
      }
    }
    return null;
  }

  /**
   * Convert the CI Row to a ProxyObjectMap representation. So that it can be serialized to JSON.
   *
   * @return ProxyObjectMap representation of the CI Row
   * @throws JOAException if conversion fails
   */
  public ProxyObjectMap toProxy() throws JOAException {
    Map<String, Object> result = new HashMap<>();

    for (PropertyInfo pi : propInfoCol) {
      if (pi.isFindKey() && !pi.isListKey()) continue;
      String propName = pi.getName();
      Object propVal = get(propName);

      if (Is.ciScroll(propVal)) {
        PropertyInfoCollection pic = pi.getPropertyInfoCollection();
        CiScroll scroll = CiScroll.factory(propVal, pic);
        result.put(propName, scroll.toProxy());
      } else if (Is.ciRow(propVal)) {
        // We did not find a CI that would have
        // a CiRow nested under ROOT or another CiRow
      } else { // primitive types
        result.put(propName, propVal);
      }
    }
    return new ProxyObjectMap(result);
  }

  /**
   * Populate the CI Row with data from the incoming map. Handles both primitive properties and
   * nested CiScrolls.
   *
   * @param dataObject Map of incoming property values
   * @throws JOAException if population fails
   */
  @SuppressWarnings("unchecked")
  public void populateWith(Map<String, Object> dataObject) throws JOAException {
    for (PropertyInfo pi : propInfoCol) {
      // if it's read only, we can not do anything about it and PS is going to complain if we try
      if (pi.isReadOnly()) continue;

      String propName = pi.getName();
      Object incomingVal = dataObject.get(propName);

      // if there is no incoming val, just ignore, PS is going to complain if required, anyways
      if (incomingVal == null) continue;

      // check if the property is a Scroll
      if (pi.isCollection()) {
        Object exVal = get(propName);

        if (!Is.polyglotList(incomingVal))
          throw new JOAException(propName + " should be an Array of CIRows.");

        CiScroll scroll = CiScroll.factory(exVal, pi.getPropertyInfoCollection());
        List<Map<String, Object>> subDataList = (List<Map<String, Object>>) incomingVal;

        scroll.populateWith(subDataList);
      } else {
        // if the property is not Read-Only and is not a CIScroll
        set(propName, incomingVal);
      }
    }
  }
}
