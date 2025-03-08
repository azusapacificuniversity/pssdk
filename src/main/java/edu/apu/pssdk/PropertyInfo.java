package edu.apu.pssdk;

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

  public String getName() throws JOAException {
    return iPropInfo.getProperty("Name").toString();
  }

  public boolean isKey() throws JOAException {
    return (boolean) iPropInfo.getProperty("Key");
  }

  public String toString() {
    try {
      return getName();
    } catch (JOAException joae) {
      // TODO: Figure out what to do
      return joae.toString();
    }
  }
}
