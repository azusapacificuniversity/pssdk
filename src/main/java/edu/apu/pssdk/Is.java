package edu.apu.pssdk;

import java.util.List;
import java.util.Map;

/** Utility class with static methods to check object types. */
public class Is {

  /**
   * Checks if the given object is a PSJOA CI Scroll.
   *
   * @param obj the object to check
   * @return true if the object is a PSJOACI Scroll, false otherwise
   */
  public static boolean ciScroll(Object obj) {
    String className = "class psft.pt8.joa.CIScroll";
    return obj.getClass().toString().equals(className);
  }

  /**
   * Checks if the given object is a PSJOA CI Row.
   *
   * @param obj the object to check
   * @return true if the object is a PSJOA CI Row, false otherwise
   */
  public static boolean ciRow(Object obj) {
    String className = "class psft.pt8.joa.CIRow";
    return obj.getClass().toString().equals(className);
  }

  /**
   * Checks if the given object is a PolyglotList (ex: a JS Array).
   *
   * @param obj the object to check
   * @return true if the object is a PolyglotList, false otherwise
   */
  public static boolean polyglotList(Object obj) {
    String className = "class com.oracle.truffle.polyglot.PolyglotList";
    return obj.getClass().toString().equals(className);
  }

  public static boolean listOfMaps(Object obj) {
    try {
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> list = (List<Map<String, Object>>) obj;
      return list.stream().allMatch(item -> item instanceof Map);
    } catch (ClassCastException e) {
      return false;
    }
  }
}
