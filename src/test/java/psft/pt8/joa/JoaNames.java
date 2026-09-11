package psft.pt8.joa;

import java.util.Map;

/**
 * Name matching that mirrors PSJOA's generic dispatcher.
 *
 * <p>{@code JOAObject.invokeMethodHelper} upper-cases the incoming name and compares with {@code
 * compareToIgnoreCase}, so a real app server accepts {@code item} and {@code Item} alike. The fakes
 * have to be equally lenient, otherwise they reject calls that work in production.
 */
final class JoaNames {

  private JoaNames() {}

  /** True when {@code name} refers to {@code expected}, ignoring case, as PSJOA does. */
  static boolean is(String name, String expected) {
    return expected.equalsIgnoreCase(name);
  }

  /** The key in {@code map} that matches {@code name} ignoring case, or null when there is none. */
  static String keyOf(Map<String, ?> map, String name) {
    if (map.containsKey(name)) return name;
    for (String key : map.keySet()) if (key.equalsIgnoreCase(name)) return key;
    return null;
  }
}
