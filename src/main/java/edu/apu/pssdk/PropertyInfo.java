package edu.apu.pssdk;

import psft.pt8.joa.CIPropertyInfoCollection;
import psft.pt8.joa.IObject;
import psft.pt8.joa.JOAException;

public class PropertyInfo {
  IObject iPropInfo;

  public PropertyInfo(IObject iProp) {
    this.iPropInfo = iProp;
  }

  public static PropertyInfo factory(Object iProp) {
    return new PropertyInfo((IObject) iProp);
  }

  public PropertyInfoCollection getPropertyInfoCollection() throws JOAException {
    return PropertyInfoCollection.factory(
        (CIPropertyInfoCollection) iPropInfo.getProperty("PropertyInfoCollection"));
  }

  public String getName() throws JOAException {
    return iPropInfo.getProperty("Name").toString();
  }

  public boolean isKey() throws JOAException {
    return (boolean) iPropInfo.getProperty("Key");
  }

  public boolean isReadOnly() throws JOAException {
    return (boolean) iPropInfo.getProperty("IsReadOnly");
  }

  public boolean isRequired() throws JOAException {
    return (boolean) iPropInfo.getProperty("Required");
  }
}
