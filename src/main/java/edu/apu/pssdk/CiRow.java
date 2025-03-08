package edu.apu.pssdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graalvm.polyglot.proxy.ProxyObject;
import psft.pt8.joa.CIPropertyInfoCollection;
import psft.pt8.joa.IObject;
import psft.pt8.joa.JOAException;

public class CiRow {
  IObject iRow;
  CIPropertyInfoCollection propInfoCol;

  public CiRow(IObject iRow) throws JOAException {
    propInfoCol = (CIPropertyInfoCollection) iRow.getProperty("PropertyInfoCollection");
    this.iRow = iRow;
  }

  public static CiRow factory(Object iRow) throws JOAException {
    return new CiRow((IObject) iRow);
  }

  public boolean isEmpty() throws JOAException {
    boolean result = false;
    for (long i = 0; i < propInfoCol.getCount(); i++) {
      PropertyInfo pi = PropertyInfo.factory(propInfoCol.item(i));
      if (!pi.isKey()) continue;

      if (get(pi).toString().equals("")) {
        result = true;
        break;
      }
    }
    return result;
  }

  public Object get(PropertyInfo prop) throws JOAException {
    return iRow.getProperty(prop.getName());
  }

  public Object get(String propertyName) throws JOAException {
    return iRow.getProperty(propertyName);
  }

  public long getCount() throws JOAException {
    return ((Long) iRow.getProperty("Count")).longValue();
  }

  public CIPropertyInfoCollection getPropertyInfoCollection() {
    return propInfoCol;
  }

  public List<PropertyInfo> getKeys() throws JOAException {
    List<PropertyInfo> keys = new ArrayList<PropertyInfo>();
    for (int i = 0; i < propInfoCol.getCount(); i++) {
      PropertyInfo pi = PropertyInfo.factory(propInfoCol.item(i));
      if (pi.isKey()) keys.add(pi);
    }
    return keys;
  }

  public Boolean isMatch(Map<String, Object> incoming) throws JOAException {
    for (PropertyInfo key : getKeys()) {
      String incomingVal = incoming.get(key.getName()).toString();
      if (!get(key).toString().equals(incomingVal)) return false;
    }
    return true;
  }

  public ProxyObject parse() throws JOAException {
    CIPropertyInfoCollection propInfoCol = getPropertyInfoCollection();
    Map<String, Object> result = new HashMap<>();

    for (int i = 0; i < propInfoCol.getCount(); i++) {

      PropertyInfo pi = PropertyInfo.factory(propInfoCol.item(i));
      String propName = pi.getName();
      Object propVal = get(propName);

      if (Is.ciScroll(propVal)) {
        CiScroll scroll = CiScroll.factory(propVal);
        result.put(propName, scroll.parse());
      } else if (Is.ciRow(propVal)) {
        // We did not find a CI that would have
        // a CiRow nested under ROOT or another CiRow
      } else { // primitive types
        result.put(propName, propVal);
      }
    }
    return ProxyObject.fromMap(result);
  }
}
