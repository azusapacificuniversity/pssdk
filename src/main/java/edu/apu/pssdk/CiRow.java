package edu.apu.pssdk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graalvm.polyglot.proxy.ProxyHashMap;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
   * Static factory method to create CiRow from IObject and PropertyInfoCollection.
   *
   * @param iRow IObject representing the CI Row
   * @param propInfoCol PropertyInfoCollection for the CI Row
   * @return CiRow instance
   * @throws JOAException if creation fails
   */
  public static CiRow factory(IObject iRow, PropertyInfoCollection propInfoCol)
      throws JOAException {
    return new CiRow(iRow, propInfoCol);
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
    logger.debug("Getting property: " + propertyName);
    return iRow.getProperty(propertyName);
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
   */
  public boolean isMatch(Map<String, Object> incoming) {
    try {
      for (PropertyInfo key : propInfoCol.keys()) {
        Object incomingVal = incoming.get(key.getName());
        if (incomingVal == null) return false;
        if (!get(key).toString().equals(incomingVal.toString())) return false;
      }
      return true;
    } catch (JOAException e) {
      throw new IllegalStateException("Error checking match: " + e.getMessage(), e);
    }
  }

  /**
   * Convert the CI Row to a ProxyObject, so that it can be serialized to JSON in Graal Node.js
   *
   * @return ProxyObject representation of the CI Row
   * @throws JOAException if conversion fails
   */
  public ProxyObject toProxyObject() throws JOAException {
    Map<String, Object> result = new HashMap<>();

    for (PropertyInfo pi : propInfoCol) {
      String propName = pi.getName();
      Object propVal = get(propName);

      if (Is.ciScroll(propVal)) {
        PropertyInfoCollection pic = pi.getPropertyInfoCollection();
        CiScroll scroll = CiScroll.factory((IObject) propVal, pic);
        result.put(propName, scroll.toProxyArrayOfProxyObjects());
      } else if (Is.ciRow(propVal)) {
        // CiRows can not be nested under ROOT CI or under another CiRow
        // there should be always a CiScroll in between.
        // So we should never get here. Remove `else if`?
      } else { // primitive types
        if (pi.isFindKey() && !pi.isListKey()) continue;
        result.put(propName, propVal);
      }
    }
    return ProxyObject.fromMap(result);
  }

  /**
   * Convert the CI Row to a ProxyHashMap, so that it can be serialized to JSON in GraalPython
   *
   * @return ProxyHashMap representation of the CI Row
   * @throws JOAException if conversion fails
   */
  public ProxyHashMap toProxyHashMap() throws JOAException {
    Map<Object, Object> result = new HashMap<>();

    for (PropertyInfo pi : propInfoCol) {
      String propName = pi.getName();
      Object propVal = get(propName);

      if (Is.ciScroll(propVal)) {
        PropertyInfoCollection pic = pi.getPropertyInfoCollection();
        CiScroll scroll = CiScroll.factory((IObject) propVal, pic);
        result.put(propName, scroll.toProxyArrayOfProxyHashMaps());
      } else if (Is.ciRow(propVal)) {
        // CiRows can not be nested under ROOT CI or under another CiRow
        // there should be always a CiScroll in between.
        // So we should never get here. Remove `else if`?
      } else { // primitive types
        if (pi.isFindKey() && !pi.isListKey()) continue;
        result.put(propName, propVal);
      }
    }
    return ProxyHashMap.from(result);
  }

  /**
   * Populate the CI Row with data from the incoming map. Handles both primitive properties and
   * nested CiScrolls.
   *
   * @param dataObject Map of incoming property values
   * @throws JOAException if population fails
   */
  public void populateWith(Map<String, Object> dataObject) throws JOAException {
    for (PropertyInfo pi : propInfoCol) {
      if (!dataObject.containsKey(pi.getName())) continue;

      Object incomingVal = dataObject.get(pi.getName());
      if (incomingVal == null || pi.isReadOnly()) continue;

      String propName = pi.getName();

      if (pi.isCollection()) {
        if (!Is.listOfStringToObjectMaps(incomingVal))
          throw new JOAException(propName + " should be a List/Array of Dicts/Objects.");

        Object exVal = get(propName);
        CiScroll scroll = CiScroll.factory((IObject) exVal, pi.getPropertyInfoCollection());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> subDataList = (List<Map<String, Object>>) incomingVal;
        scroll.populateWith(subDataList);
      } else {
        set(propName, incomingVal);
      }
    }
  }
}
