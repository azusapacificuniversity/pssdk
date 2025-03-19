package edu.apu.pssdk;

import java.lang.reflect.Field;
import psft.pt8.joa.CIPropertyInfoCollection;
import psft.pt8.joa.IObject;
import psft.pt8.joa.JOAException;

public class PropertyInfo {
  static final int ALTERNATE_SEARCH_KEY = 16;
  static final int LISTBOX_ITEM_NUM = 32;
  IObject iPropInfo;
  int useEdit;

  public PropertyInfo(IObject iProp) throws JOAException {
    this.iPropInfo = iProp;
    try {
      Field[] declaredFields = iPropInfo.getClass().getDeclaredFields();
      for (Field field : declaredFields) {
        if (field.getName().equals("m_fUseEdit")) {
          field.setAccessible(true);
          useEdit = (int) field.get(iPropInfo);
        }
      }
    } catch (Exception e) {
      throw new JOAException("Can not access m_fUseEdit field");
    }
  }

  public static PropertyInfo factory(Object iProp) throws JOAException {
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

  public boolean isCollection() throws JOAException {
    return (boolean) iPropInfo.getProperty("IsCollection");
  }

  public boolean isListKey() throws JOAException {
    return (getUseEdit() & LISTBOX_ITEM_NUM) == LISTBOX_ITEM_NUM;
  }

  public boolean isFindKey() throws JOAException {
    return (getUseEdit() & ALTERNATE_SEARCH_KEY) == ALTERNATE_SEARCH_KEY;
  }

  public boolean hasDefaultValue() throws JOAException {
    return (boolean) iPropInfo.getProperty("Default");
  }

  private int getUseEdit() {
    return useEdit;
  }
}
