package edu.apu.pssdk;

import java.util.ArrayList;
import java.util.List;
import org.graalvm.polyglot.proxy.ProxyArray;
import psft.pt8.joa.CIPropertyInfoCollection;
import psft.pt8.joa.IObject;
import psft.pt8.joa.JOAException;

public class CiScroll {
  IObject iScroll;

  public CiScroll(IObject iScroll) {
    this.iScroll = iScroll;
  }

  public static CiScroll factory(Object obj) {
    return new CiScroll((IObject) obj);
  }

  public CIPropertyInfoCollection getPropertyInfoCollection() throws JOAException {
    return (CIPropertyInfoCollection) iScroll.getProperty("PropertyInfoCollection");
  }

  public boolean isEmpty() throws JOAException {
    return (getCount() == 1 && CiRow.factory(get(0)).isEmpty());
  }

  public long getCount() throws JOAException {
    return ((Long) iScroll.getProperty("Count")).longValue();
  }

  public Object get(long Index) throws JOAException {
    Object[] args = new Object[1];
    args[0] = Index;
    return iScroll.invokeMethod("Item", args);
  }

  public ProxyArray parse() throws JOAException {
    List<Object> result = new ArrayList<Object>();
    for (int j = 0; j < getCount(); j++) {
      CiRow row = CiRow.factory(get(j));
      result.add(row.parse());
    }
    return ProxyArray.fromList(result);
  }
}
