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

  public CiRow(IObject iRow, CIPropertyInfoCollection propInfoCol) throws JOAException {
    this.propInfoCol = propInfoCol;
    this.iRow = iRow;
  }

  public static CiRow factory(Object iRow, CIPropertyInfoCollection propInfoCol)
      throws JOAException {
    return new CiRow((IObject) iRow, propInfoCol);
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
    return get(prop.getName());
  }

  public Object get(String propertyName) throws JOAException {
    return iRow.getProperty(propertyName);
  }

  public CiRow set(PropertyInfo prop, Object val) throws JOAException {
    return set(prop.getName(), val);
  }

  public CiRow set(String propertyName, Object val) throws JOAException {
    iRow.setProperty(propertyName, val);
    return this;
  }

  public long count() throws JOAException {
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
      System.out.println(key.getName());
      System.out.println(incoming);
      String incomingVal = incoming.get(key.getName()).toString();
      if (!get(key).toString().equals(incomingVal)) return false;
    }
    return true;
  }

  public Map<String, Object> findIn(List<Map<String, Object>> incomingList) throws JOAException {
    return findIn(incomingList, false);
  }

  public Map<String, Object> findIn(List<Map<String, Object>> incomingList, boolean deleteFound)
      throws JOAException {
    for (Map<String, Object> incoming : incomingList) {
      if (isMatch(incoming)) {
        if (deleteFound) incomingList.remove(incoming);
        return incoming;
      }
    }
    return null;
  }

  public ProxyObject parse() throws JOAException {
    CIPropertyInfoCollection propInfoCol = getPropertyInfoCollection();
    Map<String, Object> result = new HashMap<>();

    for (int i = 0; i < propInfoCol.getCount(); i++) {

      PropertyInfo pi = PropertyInfo.factory(propInfoCol.item(i));
      String propName = pi.getName();
      Object propVal = get(propName);

      if (Is.ciScroll(propVal)) {
        CIPropertyInfoCollection pic = pi.getPropertyInfoCollection();
        CiScroll scroll = CiScroll.factory(propVal, pic);
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

  public void unParse(Map<String, Object> incoming) throws JOAException {
    for (long i = 0; i < propInfoCol.getCount(); i++) {
      PropertyInfo pi = PropertyInfo.factory(propInfoCol.item(i));

      if (pi.isReadOnly()) continue;

      String propName = pi.getName();
      Object incomingVal = incoming.get(propName);

      if (incomingVal == null) {
        if (pi.isRequired()) {
          throw new JOAException(propName + " was not supplied with the data but it is required.");
        } else {
          // TODO Do we want to set something to null?
          continue;
        }
      }

      Object exVal = get(propName);
      // check if the property is a Scroll
      if (Is.ciScroll(exVal)) {

        if (!Is.polyglotList(incomingVal))
          throw new JOAException(propName + " should be an Array of CIRows.");

        CiScroll scroll = CiScroll.factory(exVal, pi.getPropertyInfoCollection());
        List<Map<String, Object>> newArr = (List<Map<String, Object>>) incomingVal;

        // Update existing and delete if not found in incoming
        long j = 0;
        while (j < scroll.count() && !scroll.isEmpty()) {
          CiRow exRow = scroll.get(j);
          if (!exRow.isEmpty()) {
            Map<String, Object> incomingMatch = exRow.findIn(newArr, /* deleteFound */ true);
            if (incomingMatch != null) {
              exRow.unParse(incomingMatch);
              j++;
            } else {
              scroll.delete(j);
              // next item will take the index of the deleted row no need to change j
            }
          }
        }
        // Add non-existing
        for (int k = 0; k < newArr.size(); k++) {
          CiRow newRow;
          if (scroll.isEmpty()) {
            newRow = scroll.get(0);
          } else {
            newRow = scroll.insertEmptyRow();
          }
          newRow.unParse(newArr.get(k));
        }
      } else {
        // if the property is not Read-Only and is not a CIScroll
        set(propName, incomingVal);
      }
    }
  }
}
