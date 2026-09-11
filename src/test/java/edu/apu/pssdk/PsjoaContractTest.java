package edu.apu.pssdk;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Pins the assumptions the SDK makes about the psjoa jar itself. These are static facts about the
 * vendor classes, so they hold without an App Server; a PeopleTools upgrade that renames any of
 * them would otherwise fail silently or only in production.
 */
class PsjoaContractTest {

  /** {@code Is} resolves these by name in a static initialiser, so a rename fails at class load. */
  @Test
  void theScrollAndRowInterfacesAreStillNamedTheSame() {
    assertDoesNotThrow(() -> Class.forName("psft.pt8.joa.ICIScroll"));
    assertDoesNotThrow(() -> Class.forName("psft.pt8.joa.ICIRow"));
  }

  /**
   * {@code PropertyInfoCollection} reaches a CIPropertyInfoCollection through the generic {@code
   * IObject} surface: {@code getProperty("Count")} and {@code invokeMethod("Item", ..)} are
   * dispatched by JOAObject onto these two methods.
   */
  @Test
  void thePropertyInfoCollectionStillExposesCountAndItem() throws Exception {
    Class<?> collection = Class.forName("psft.pt8.joa.CIPropertyInfoCollection");

    assertEquals(long.class, collection.getMethod("getCount").getReturnType());
    assertNotNull(collection.getMethod("item", long.class));
  }

  /**
   * A user-defined CI method may only return a PeopleCode primitive, and {@code
   * CISvc.deserializeObject} builds exactly a Boolean, a Long or a String from the wire. {@code
   * CI.execute} therefore returns Object; narrowing it back to Boolean would break every method
   * that returns a string or a number.
   */
  @Test
  void executeReturnsObjectSoAnyPrimitiveCanComeBack() throws Exception {
    Method execute = CI.class.getMethod("execute", String.class, Map.class);

    assertEquals(Object.class, execute.getReturnType());
  }
}
