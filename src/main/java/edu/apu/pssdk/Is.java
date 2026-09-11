package edu.apu.pssdk;

import java.util.List;
import java.util.Map;

/** Utility class with static methods to check object types. */
public class Is {

  private static final Class<?> I_CI_SCROLL;
  private static final Class<?> I_CI_ROW;

  static {
    try {
      I_CI_SCROLL = Class.forName("psft.pt8.joa.ICIScroll");
      I_CI_ROW = Class.forName("psft.pt8.joa.ICIRow");
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("PSJOA is not on the runtime classpath", e);
    }
  }

  /**
   * Checks if the given object is a PSJOA CI Scroll.
   *
   * @param obj the object to check
   * @return true if the object is a PSJOACI Scroll, false otherwise
   */
  public static boolean ciScroll(Object obj) {
    return I_CI_SCROLL.isInstance(obj);
  }

  /**
   * Checks if the given object is a PSJOA CI Row.
   *
   * @param obj the object to check
   * @return true if the object is a PSJOA CI Row, false otherwise
   */
  public static boolean ciRow(Object obj) {
    return I_CI_ROW.isInstance(obj);
  }

  /**
   * Checks if the given object is a {@code List<Map<String, Object>>}
   *
   * @param obj the object to check
   * @return true if the object is a {@code List<Map<String, Object>>}, false otherwise
   */
  public static boolean listOfStringToObjectMaps(Object obj) {
    try {
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> list = (List<Map<String, Object>>) obj;
      return list.stream().allMatch(item -> item instanceof Map);
    } catch (ClassCastException e) {
      return false;
    }
  }
}
