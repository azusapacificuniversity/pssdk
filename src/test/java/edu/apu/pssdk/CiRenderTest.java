package edu.apu.pssdk;

import static edu.apu.pssdk.CiFixtures.element;
import static edu.apu.pssdk.CiFixtures.size;
import static edu.apu.pssdk.CiFixtures.wrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static psft.pt8.joa.JoaFixture.ci;
import static psft.pt8.joa.JoaFixture.prop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.graalvm.polyglot.proxy.ProxyHashMap;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.junit.jupiter.api.Test;
import psft.pt8.joa.FakeCi;
import psft.pt8.joa.FakeSession;

/**
 * Covers which properties a GET renders. The alternate-search-key bit is only meaningful for
 * scalars, but PeopleSoft reports it on collections too, so filtering on it before knowing whether
 * the property is a scroll silently drops the whole collection from the output.
 */
class CiRenderTest {

  /** A CI whose child collection carries the alternate-search-key bit, as PeopleSoft reports it. */
  private static FakeCi ciWithAltSearchKeyCollection() {
    return ci("CI_PERSONAL_DATA")
        .properties(
            prop("EMPLID").key().listKey(),
            prop("NAME").findKey(),
            prop("BIRTHDATE"),
            prop("PHONES").findKey().of(prop("PHONE_TYPE").key(), prop("PHONE")))
        .getKeys(prop("EMPLID").key())
        .build();
  }

  private static CI loaded(FakeCi fake) throws Exception {
    fake.whenGetReturns(
        Map.of(
            "EMPLID", "001610517",
            "NAME", "Samuel Demirdjian",
            "BIRTHDATE", "1990-01-01",
            "PHONES", List.of(Map.of("PHONE_TYPE", "CELL", "PHONE", "555"))));
    CI ci = wrap(fake, new FakeSession(fake));
    return ci.get(Map.of("EMPLID", "001610517"));
  }

  private static List<String> keysOf(ProxyObject proxyObject) {
    Object memberKeys = proxyObject.getMemberKeys();
    List<String> names = new ArrayList<>();
    for (long i = 0; i < size(memberKeys); i++) names.add(element(memberKeys, i).toString());
    return names;
  }

  @Test
  void aCollectionIsRenderedEvenWhenItCarriesTheAltSearchKeyBit() throws Exception {
    List<String> keys = keysOf(loaded(ciWithAltSearchKeyCollection()).toProxyObject());

    assertTrue(keys.contains("PHONES"), "the collection was dropped from the GET output: " + keys);
  }

  @Test
  void aScalarCarryingTheAltSearchKeyBitIsStillFiltered() throws Exception {
    List<String> keys = keysOf(loaded(ciWithAltSearchKeyCollection()).toProxyObject());

    assertFalse(keys.contains("NAME"), "an alternate search key should not be listed");
    assertTrue(keys.contains("EMPLID"), "a list box item should be listed");
    assertTrue(keys.contains("BIRTHDATE"), "a plain property should be listed");
  }

  @Test
  void theCollectionRendersItsRows() throws Exception {
    ProxyObject out = loaded(ciWithAltSearchKeyCollection()).toProxyObject();

    Object phones = out.getMember("PHONES");
    assertEquals(1, size(phones));
    assertEquals("CELL", CiFixtures.member(element(phones, 0), "PHONE_TYPE"));
  }

  @Test
  void theFindArrayHoldsHashMapsForPython() throws Exception {
    FakeCi fake =
        ci("CI_PERSONAL_DATA")
            .properties(prop("EMPLID").key(), prop("NAME"))
            .findKeys(prop("EMPLID").key().listKey(), prop("NAME").listKey())
            .build();
    fake.whenFindReturns(List.of(Map.of("EMPLID", "001", "NAME", "Sam")));
    CI ci = wrap(fake, new FakeSession(fake)).find(Map.of("NAME", "Sam"));

    Object rows = ci.toProxyArrayOfProxyHashMaps();

    assertEquals(1, size(rows));
    assertInstanceOf(ProxyHashMap.class, element(rows, 0));
  }

  @Test
  void toProxyHashMapIsReachableFromTheCi() throws Exception {
    FakeCi fake = ciWithAltSearchKeyCollection();
    CI ci = loaded(fake);

    // EMPLID, BIRTHDATE and PHONES render; NAME is filtered as an alternate search key.
    assertEquals(3, ci.toProxyHashMap().getHashSize());
  }

  @Test
  void toProxyHashMapAppliesTheSameRules() throws Exception {
    FakeCi fake = ciWithAltSearchKeyCollection();
    CiRow root =
        CiRow.factory(fake, PropertyInfoCatalog.buildFor(fake).getPropertyInfoCollection());
    loaded(fake);

    // EMPLID, BIRTHDATE and PHONES render; NAME is filtered as an alternate search key.
    assertEquals(3, root.toProxyHashMap().getHashSize());
  }
}
