package edu.apu.pssdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import psft.pt8.joa.CIPropertyInfoCollection;
import psft.pt8.joa.JOAException;

/**
 * Wraps PSJOA CIPropertyInfoCollection in an iterable class, to iterate over and access
 * PropertyInfo elements.
 */
public class PropertyInfoCollection implements Iterable<PropertyInfo> {

  Map<String, PropertyInfo> mapPropInfo = new HashMap<String, PropertyInfo>();
  Logger logger;

  /**
   * Constructs a new PropertyInfoCollection from a PSJOA CIPropertyInfoCollection.
   *
   * @param propInfoCol The underlying PSJOA CIPropertyInfoCollection object.
   */
  public PropertyInfoCollection(CIPropertyInfoCollection propInfoCol) throws JOAException {
    this.logger = LoggerFactory.getLogger(PropertyInfoCollection.class);
    this.logger.info("PropertyInfoCollection: " + propInfoCol.getClassName());
    for (long i = 0; i < propInfoCol.getCount(); i++) {
      PropertyInfo pi = PropertyInfo.factory(propInfoCol.item(i));
      mapPropInfo.put(pi.getName(), pi);
    }
  }

  /**
   * Factory method to create an instance of PropertyInfoCollection.
   *
   * @param propInfoCol The underlying PSJOA CIPropertyInfoCollection object.
   * @return A new PropertyInfoCollection instance.
   */
  public static PropertyInfoCollection factory(CIPropertyInfoCollection propInfoCol)
      throws JOAException {
    return new PropertyInfoCollection(propInfoCol);
  }

  /**
   * Retrieves a PropertyInfo object at the specified index.
   *
   * @param i The 0-based index of the PropertyInfo to retrieve.
   * @return The PropertyInfo object at the given index.
   * @throws JOAException if an error occurs during the retrieval.
   */
  public PropertyInfo get(String propName) throws JOAException {
    return mapPropInfo.get(propName);
  }

  /**
   * Returns a list of PropertyInfo objects that are identified as keys in this collection.
   *
   * @return A list of PropertyInfo objects that are keys.
   * @throws JOAException if an error occurs during the PeopleSoft API call while checking property
   *     info.
   */
  public List<PropertyInfo> keys() throws JOAException {
    List<PropertyInfo> keys = new ArrayList<PropertyInfo>();
    for (PropertyInfo pi : this) {
      if (pi.isKey()) keys.add(pi);
    }
    return keys;
  }

  /**
   * Returns the number of PropertyInfo in this collection.
   *
   * @return the number of PropertyInfo in this collection
   */
  public long count() {
    return mapPropInfo.size();
  }

  /**
   * Returns an iterator over the PropertyInfo elements in this collection.
   *
   * @return an iterator over the PropertyInfo elements in this collection
   */
  @Override
  public Iterator<PropertyInfo> iterator() {
    return mapPropInfo.entrySet().stream().map(e -> e.getValue()).iterator();
  }
}
