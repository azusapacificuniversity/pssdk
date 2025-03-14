package edu.apu.pssdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.graalvm.polyglot.proxy.ProxyArray;
import psft.pt8.joa.CIPropertyInfoCollection;
import psft.pt8.joa.IObject;
import psft.pt8.joa.JOAException;

public class CiScroll implements Iterable<CiRow> {
  IObject iScroll;
  CIPropertyInfoCollection propInfoCol;

  public CiScroll(IObject iScroll, CIPropertyInfoCollection propInfoCol) throws JOAException {
    this.propInfoCol = propInfoCol;
    this.iScroll = iScroll;
  }

  public static CiScroll factory(Object obj, CIPropertyInfoCollection propInfoCol)
      throws JOAException {
    return new CiScroll((IObject) obj, propInfoCol);
  }

  public CIPropertyInfoCollection getPropertyInfoCollection() throws JOAException {
    return propInfoCol;
  }

  public boolean isEmpty() throws JOAException {
    return (count() == 1 && get(0).isEmpty());
  }

  public long count() throws JOAException {
    return ((Long) iScroll.getProperty("Count")).longValue();
  }

  /** TODO: We're assuming that a CiScroll has only CiRows as children */
  public CiRow get(long index) throws JOAException {
    Object[] args = new Object[1];
    args[0] = index;
    return CiRow.factory(iScroll.invokeMethod("Item", args), getPropertyInfoCollection());
  }

  public CiRow find(Map<String, Object> data) throws JOAException {
    for (long i = 0; i < count(); i++) {
      CiRow row = get(i);
      if (row.isMatch(data)) {
        return row;
      }
    }
    return null;
  }

  public CiRow insertEmptyRow() throws JOAException {
    IObject iObj = (IObject) iScroll.invokeMethod("InsertItem", new Object[] {0});
    return CiRow.factory(iObj, getPropertyInfoCollection());
  }

  public Object delete(long index) throws JOAException {
    return iScroll.invokeMethod("DeleteItem", new Object[] {index});
  }

  public ProxyArray parse() throws JOAException {
    List<Object> result = new ArrayList<Object>();
    for (int j = 0; j < count(); j++) {
      result.add(get(j).parse());
    }
    return ProxyArray.fromList(result);
  }

  @Override
  public java.util.Iterator<CiRow> iterator() {
    return new CiScrollIterator();
  }

  private class CiScrollIterator implements java.util.Iterator<CiRow> {
    private long currentIndex = 0;
    private final long count;

    public CiScrollIterator() {
      try {
        count = count();
      } catch (JOAException e) {
        throw new RuntimeException("Failed to get scroll count", e);
      }
    }

    @Override
    public boolean hasNext() {
      return currentIndex < count;
    }

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
