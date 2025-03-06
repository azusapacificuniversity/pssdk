package edu.apu.pssdk;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CITest {
  @Test
  public void someLibraryMethodReturnsTrue() {
    CI classUnderTest = new CI();
    assertTrue(classUnderTest.someLibraryMethod(), "someLibraryMethod should return 'true'");
  }
}
