package edu.apu.pssdk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import psft.pt8.joa.CIPropertyInfoCollection;
import psft.pt8.joa.JOAException;

public class PropertyInfoCollection implements Iterable<PropertyInfo> {

  CIPropertyInfoCollection colPropInfo;

  public PropertyInfoCollection(CIPropertyInfoCollection propInfoCol) throws JOAException {
    this.colPropInfo = propInfoCol;
  }

  public static PropertyInfoCollection factory(CIPropertyInfoCollection propInfoCol)
      throws JOAException {
    return new PropertyInfoCollection(propInfoCol);
  }

  public PropertyInfo get(long i) throws JOAException {
    return PropertyInfo.factory(colPropInfo.item(i));
  }

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
    return colPropInfo.getCount();
  }

  /**
   * Returns an iterator over the PropertyInfo elements in this collection.
   *
   * @return an iterator over the PropertyInfo elements in this collection
   */
  @Override
  public Iterator<PropertyInfo> iterator() {
    return new Iterator<PropertyInfo>() {
      private long currentIndex = 0;
      private final long count = count();

      @Override
      public boolean hasNext() {
        return currentIndex < count;
      }

      @Override
      public PropertyInfo next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        try {
          return get(currentIndex++);
        } catch (JOAException e) {
          throw new RuntimeException(
              "Error retrieving PropertyInfo at index " + (currentIndex - 1), e);
        }
      }
    };
  }
}
