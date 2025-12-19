package edu.apu.pssdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.graalvm.polyglot.proxy.ProxyArray;
import psft.pt8.joa.IObject;
import psft.pt8.joa.JOAException;

/**
 * Wrapper class for a PeopleSoft Component Interface Scroll (CI Scroll), which can be iterated over
 * to access its rows. Represents a scroll nested within a CI. Contains multiple CiRow objects.
 */
public class CiScroll implements Iterable<CiRow> {
  IObject iScroll;
  PropertyInfoCollection propInfoCol;

  /**
   * Constructor to initialize CiScroll with IObject and PropertyInfoCollection.
   *
   * @param iScroll IObject representing the CI Scroll
   * @param propInfoCol PropertyInfoCollection for the CI Scroll
   * @throws JOAException if initialization fails
   */
  public CiScroll(IObject iScroll, PropertyInfoCollection propInfoCol) throws JOAException {
    this.propInfoCol = propInfoCol;
    this.iScroll = iScroll;
  }

  /**
   * Static factory method to create CiScroll from IObject and PropertyInfoCollection.
   *
   * @param obj IObject representing the CI Scroll
   * @param propInfoCol PropertyInfoCollection for the CI Scroll
   * @return CiScroll instance
   * @throws JOAException if creation fails
   */
  public static CiScroll factory(Object obj, PropertyInfoCollection propInfoCol)
      throws JOAException {
    return new CiScroll((IObject) obj, propInfoCol);
  }

  /**
   * A getter for the PropertyInfoCollection of the CiScroll. (set in constructor)
   *
   * @return PropertyInfoCollection of the CiScroll
   * @throws JOAException if retrieval fails
   */
  public PropertyInfoCollection getPropertyInfoCollection() throws JOAException {
    return propInfoCol;
  }

  /**
   * Checks if the CiScroll is empty (has one CI row which is empty).
   *
   * @return true if the CiScroll is empty, false otherwise
   * @throws JOAException if check fails
   */
  public boolean isEmpty() throws JOAException {
    return (count() == 1 && get(0).isEmpty());
  }

  /**
   * Gets the count of CI rows in the CiScroll.
   *
   * @return count of CI rows
   * @throws JOAException if retrieval fails
   */
  public long count() throws JOAException {
    return ((Long) iScroll.getProperty("Count")).longValue();
  }

  /**
   * Gets the CiRow at the specified index.
   *
   * @param index Index of the CiRow to retrieve
   * @return CiRow at the specified index
   * @throws JOAException if retrieval fails
   */
  public CiRow get(long index) throws JOAException {
    Object[] args = new Object[1];
    args[0] = index;
    return CiRow.factory(iScroll.invokeMethod("Item", args), getPropertyInfoCollection());
  }

  /**
   * Finds the first CiRow matching the provided data.
   *
   * @param data Map of property names and values to match
   * @return First matching CiRow, or null if none found
   * @throws JOAException if search fails
   */
  public CiRow find(Map<String, Object> data) throws JOAException {
    for (CiRow row : this) {
      if (row.isMatch(data)) {
        return row;
      }
    }
    return null;
  }

  /**
   * Inserts an empty CiRow into the CiScroll.
   *
   * @return Newly inserted empty CiRow
   * @throws JOAException if insertion fails
   */
  public CiRow insertEmptyRow() throws JOAException {
    IObject iObj = (IObject) iScroll.invokeMethod("InsertItem", new Object[] {0});
    return CiRow.factory(iObj, getPropertyInfoCollection());
  }

  /**
   * Deletes the CiRow at the specified index.
   *
   * @param index Index of the CiRow to delete
   * @return Result of the delete operation
   * @throws JOAException if deletion fails
   */
  public Object delete(long index) throws JOAException {
    return iScroll.invokeMethod("DeleteItem", new Object[] {index});
  }

  /**
   * Converts the CiScroll to a ProxyArray representation. So that it can be serialized to JSON.
   *
   * @return ProxyArray representing the CiScroll
   * @throws JOAException if conversion fails
   */
  public ProxyArray toProxy() throws JOAException {
    List<Object> result = new ArrayList<Object>();
    for (CiRow row : this) {
      result.add(row.toProxy());
    }
    return ProxyArray.fromList(result);
  }

  /**
   * Populates the CiScroll with data from the incoming list of maps. Updates existing rows, deletes
   * rows not found in incoming data, and adds new rows for non-existing data.
   *
   * @param dataList List of maps containing incoming data
   * @throws JOAException if population fails
   */
  public void populateWith(List<Map<String, Object>> dataList) throws JOAException {
    // Update existing and delete if not found in incoming
    long j = count();
    while (j > 0 && !isEmpty()) {
      CiRow exRow = get(--j);
      if (!exRow.isEmpty()) {
        Map<String, Object> incomingMatch = exRow.findIn(dataList, /* deleteFound */ true);
        if (incomingMatch != null) {
          exRow.populateWith(incomingMatch);
        } else {
          delete(j); // next takes index of the deleted, but we're looking for previous
        }
      }
    }
    // Add non-existing
    for (Map<String, Object> incomingObj : dataList) {
      CiRow newRow = isEmpty() ? get(0) : insertEmptyRow();
      newRow.populateWith(incomingObj);
    }
  }

  /**
   * Implements the Iterable interface to allow iteration over CiRow objects in the CiScroll.
   *
   * @return Iterator over CiRow objects
   */
  @Override
  public java.util.Iterator<CiRow> iterator() {
    return new CiScrollIterator();
  }

  /** Private iterator class for CiScroll to iterate over CiRow objects. */
  private class CiScrollIterator implements java.util.Iterator<CiRow> {
    private long currentIndex = 0;
    private final long count;

    /** Constructor to initialize the iterator and get the count of rows. */
    public CiScrollIterator() {
      try {
        count = count();
      } catch (JOAException e) {
        throw new RuntimeException("Failed to get scroll count", e);
      }
    }

    /**
     * Checks if there are more CiRow objects to iterate over.
     *
     * @return true if there are more CiRow objects, false otherwise
     */
    @Override
    public boolean hasNext() {
      return currentIndex < count;
    }

    /**
     * Gets the next CiRow object in the iteration.
     *
     * @return Next CiRow object
     * @throws java.util.NoSuchElementException if there are no more elements
     */
    @Override
    public CiRow next() {
      if (!hasNext()) {
        throw new java.util.NoSuchElementException();
      }
      try {
        return get(currentIndex++);
      } catch (JOAException e) {
        throw new RuntimeException("Failed to get row at index " + (currentIndex - 1), e);
      }
    }
  }
}
