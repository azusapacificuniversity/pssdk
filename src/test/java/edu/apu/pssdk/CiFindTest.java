package edu.apu.pssdk;

import static edu.apu.pssdk.CiFixtures.element;
import static edu.apu.pssdk.CiFixtures.member;
import static edu.apu.pssdk.CiFixtures.size;
import static edu.apu.pssdk.CiFixtures.wrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static psft.pt8.joa.JoaFixture.ci;
import static psft.pt8.joa.JoaFixture.prop;

import java.util.List;
import java.util.Map;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.junit.jupiter.api.Test;
import psft.pt8.joa.FakeCi;
import psft.pt8.joa.FakeSession;
import psft.pt8.joa.JOAException;

/**
 * Covers {@code CI.find}. PSJOA answers Find with a CICollection of CI objects, one per match, not
 * with a Boolean; and the root CI does not answer to {@code Count} afterwards, so the rows have to
 * be read from that collection.
 */
class CiFindTest {

  private static FakeCi rooms() {
    return ci("RL_ROOM")
        .properties(prop("INSTITUTION").key(), prop("RL_BLDG_CD").key(), prop("RL_ROOM_CD").key())
        .getKeys(prop("INSTITUTION").key(), prop("RL_BLDG_CD").key(), prop("RL_ROOM_CD").key())
        .findKeys(
            prop("INSTITUTION").key().listKey(),
            prop("RL_BLDG_CD").key().listKey(),
            prop("RL_ROOM_CD").key().listKey(),
            prop("DESCR").findKey())
        .build();
  }

  private static CI ciWith(FakeCi fake) throws Exception {
    return wrap(fake, new FakeSession(fake));
  }

  @Test
  void findReadsTheMatchesFromTheCollectionItReturns() throws Exception {
    FakeCi fake =
        rooms()
            .whenFindReturns(
                List.of(
                    Map.of("INSTITUTION", "APU", "RL_BLDG_CD", "SHIRE", "RL_ROOM_CD", "A1"),
                    Map.of("INSTITUTION", "APU", "RL_BLDG_CD", "SHIRE", "RL_ROOM_CD", "A2")));

    Object rows = ciWith(fake).find(Map.of("RL_BLDG_CD", "SHIRE")).toProxyArrayOfProxyObjects();

    assertEquals(2, size(rows));
    assertEquals("A1", member(element(rows, 0), "RL_ROOM_CD"));
    assertEquals("A2", member(element(rows, 1), "RL_ROOM_CD"));
  }

  @Test
  void theFindKeysAreSetBeforeFindRuns() throws Exception {
    FakeCi fake = rooms();

    ciWith(fake).find(Map.of("RL_BLDG_CD", "SHIRE"));

    assertEquals("SHIRE", fake.data().get("RL_BLDG_CD"));
    assertTrue(fake.calls().contains("Find"));
  }

  @Test
  void toJsonAfterFindIsTheArrayOfMatches() throws Exception {
    FakeCi fake =
        rooms()
            .whenFindReturns(
                List.of(Map.of("INSTITUTION", "APU", "RL_BLDG_CD", "SHIRE", "RL_ROOM_CD", "A1")));

    Object out = ciWith(fake).find(Map.of("RL_BLDG_CD", "SHIRE")).toJSON();

    assertInstanceOf(ProxyArray.class, out);
    assertEquals(1, size(out));
  }

  @Test
  void aFindWithNoMatchesYieldsNoRows() throws Exception {
    FakeCi fake = rooms().whenFindReturns(List.of());
    CI ci = ciWith(fake).find(Map.of("RL_ROOM_CD", "NOSUCH"));

    assertEquals(0, size(ci.toProxyArrayOfProxyObjects()));
    assertEquals(0, size(ci.toProxyArrayOfProxyHashMaps()));
  }

  @Test
  void toJsonAfterAGetIsTheRecordNotTheEarlierMatches() throws Exception {
    Map<String, Object> b1 =
        Map.of("INSTITUTION", "APU", "RL_BLDG_CD", "SHIRE", "RL_ROOM_CD", "B1");
    FakeCi fake =
        rooms()
            .whenFindReturns(
                List.of(Map.of("INSTITUTION", "APU", "RL_BLDG_CD", "SHIRE", "RL_ROOM_CD", "A1")))
            .whenGetReturns(b1);

    Object out = ciWith(fake).find(Map.of("RL_BLDG_CD", "SHIRE")).get(b1).toJSON();

    assertInstanceOf(ProxyObject.class, out);
    assertEquals("B1", member(out, "RL_ROOM_CD"));
  }

  @Test
  void readingFindRowsWithoutAFindFails() throws Exception {
    CI ci = ciWith(rooms());

    assertThrows(PssdkException.class, ci::toProxyArrayOfProxyObjects);
    assertThrows(PssdkException.class, ci::toProxyArrayOfProxyHashMaps);
  }

  @Test
  void anAppServerErrorDuringFindIsWrappedAsAPssdkException() throws Exception {
    CI ci = ciWith(rooms().failFind());

    PssdkException e =
        assertThrows(PssdkException.class, () -> ci.find(Map.of("RL_BLDG_CD", "SHIRE")));

    assertInstanceOf(JOAException.class, e.getCause());
  }
}
