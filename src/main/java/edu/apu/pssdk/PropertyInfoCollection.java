package edu.apu.pssdk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import psft.pt8.joa.IObject;
import psft.pt8.joa.JOAException;

/**
 * Wraps PSJOA CIPropertyInfoCollection in an iterable class, to iterate over and access
 * PropertyInfo elements.
 */
public class PropertyInfoCollection implements Iterable<PropertyInfo> {

  static final String CLASS_NAME = "CompIntfcPropertyInfoCollection";

  Map<String, PropertyInfo> mapPropInfo = new LinkedHashMap<String, PropertyInfo>();
  Logger logger;

  /**
   * Constructs a new PropertyInfoCollection from a PSJOA CIPropertyInfoCollection.
   *
   * @param propInfoCol The underlying PSJOA CIPropertyInfoCollection object.
   * @throws JOAException if propInfoCol is not a PSJOA CIPropertyInfoCollection.
   */
  public PropertyInfoCollection(IObject propInfoCol) throws JOAException {
    if (!CLASS_NAME.equals(propInfoCol.getClassName()))
      throw new JOAException(
          "Expected a " + CLASS_NAME + " but got a " + propInfoCol.getClassName());

    // CIPropertyInfoCollection.getCount()
    long count = ((Number) propInfoCol.getProperty("Count")).longValue();
    for (long i = 0; i < count; i++) {
      // CIPropertyInfoCollection.item(i)
      IObject iPropInfo = (IObject) propInfoCol.invokeMethod("item", new Object[] {i});
      PropertyInfo pi = PropertyInfo.factory(iPropInfo);
      mapPropInfo.put(pi.getName(), pi);
    }
  }

  /**
   * Factory method to create an instance of PropertyInfoCollection.
   *
   * @param propInfoCol The underlying PSJOA CIPropertyInfoCollection object.
   * @return A new PropertyInfoCollection instance.
   * @throws JOAException if an error occurs
   */
  public static PropertyInfoCollection factory(IObject propInfoCol) throws JOAException {
    return new PropertyInfoCollection(propInfoCol);
  }

  /**
   * Retrieves a PropertyInfo object by property name.
   *
   * @param propName The name of the property to retrieve.
   * @return The PropertyInfo object for the given name, or null if there is none.
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
