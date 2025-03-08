package edu.apu.pssdk;

public class Is {

  public static boolean ciScroll(Object obj) {
    return obj.getClass().toString().equals("class psft.pt8.joa.CIScroll");
  }

  public static boolean ciRow(Object obj) {
    return obj.getClass().toString().equals("class psft.pt8.joa.CIRow");
  }
}
