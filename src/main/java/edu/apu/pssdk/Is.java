package edu.apu.pssdk;

public class Is {

  public static boolean ciScroll(Object obj) {
    String className = "class psft.pt8.joa.CIScroll";
    return obj.getClass().toString().equals(className);
  }

  public static boolean ciRow(Object obj) {
    String className = "class psft.pt8.joa.CIRow";
    return obj.getClass().toString().equals(className);
  }

  public static boolean polyglotList(Object obj) {
    String className = "class com.oracle.truffle.polyglot.PolyglotList";
    return obj.getClass().toString().equals(className);
  }
}
