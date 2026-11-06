package edu.apu.pssdk;

import static edu.apu.pssdk.CiFixtures.wrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static psft.pt8.joa.JoaFixture.ci;
import static psft.pt8.joa.JoaFixture.prop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import psft.pt8.joa.FakeCi;
import psft.pt8.joa.FakeSession;
import psft.pt8.joa.IObject;

/**
 * Covers how {@code set} matches incoming rows against the rows already in a scroll when PSJOA
 * flags a child collection as a key. PSJOA reads a collection's Key, Required and Prompt flags from
 * bits that carry no metadata for collections, so on RL_ROOM a child of RL_ROOM_TBL comes back as a
 * key in some sessions (RL_ROOM_PARTS as {@code Key,Required,Prompt} in one, RL_STDNT_ROOM_V in
 * another). A collection can never equal an incoming value, so if it counts as a key no existing
 * row ever matches, and {@code set} deletes the row PeopleSoft created and inserts a new one in its
 * place.
 */
class CiSetTest {

  private static final String TODAY = "11/06/2026";

  private static final Map<String, Object> ROOM_KEYS =
      Map.of(
          "INSTITUTION", "APU",
          "CAMPUS", "APU",
          "RL_BLDG_CD", "SHIRE",
          "RL_ROOM_CD", "ZZTEST1");

  /** A CI shaped like RL_ROOM, including the child collection PSJOA reports as a key. */
  private static FakeCi rlRoom() {
    return ci("RL_ROOM")
        .properties(
            prop("INSTITUTION").key(),
            prop("CAMPUS").key(),
            prop("RL_BLDG_CD").key(),
            prop("RL_ROOM_CD").key(),
            prop("RL_ROOM_TBL")
                .of(
                    prop("EFFDT").key().required(),
                    prop("EFF_STATUS"),
                    prop("RL_FLOOR_CD"),
                    prop("RL_ROOM_TYPE"),
                    prop("RL_ROOM_PARTS").key().required().of(prop("PART").key(), prop("DESCR"))))
        .createKeys(
            prop("INSTITUTION").key(),
            prop("CAMPUS").key(),
            prop("RL_BLDG_CD").key(),
            prop("RL_ROOM_CD").key())
        .build();
  }

  /** A created RL_ROOM holding the effective-dated row PeopleSoft adds on Create. */
  private static FakeCi createdRoom() throws Exception {
    FakeCi fake =
        rlRoom()
            .whenCreateReturns(
                Map.of("RL_ROOM_TBL", List.of(Map.of("EFFDT", TODAY, "EFF_STATUS", "A"))));
    wrap(fake, new FakeSession(fake)).create(ROOM_KEYS);
    return fake;
  }

  private static PropertyInfoCollection roomTblRow(FakeCi fake) throws Exception {
    return PropertyInfoCatalog.buildFor(fake)
        .getPropertyInfoCollection()
        .get("RL_ROOM_TBL")
        .getPropertyInfoCollection();
  }

  @Test
  void aChildCollectionFlaggedAsKeyIsNotAKey() throws Exception {
    List<String> keys = new ArrayList<>();
    for (PropertyInfo pi : roomTblRow(rlRoom()).keys()) keys.add(pi.getName());

    assertEquals(List.of("EFFDT"), keys);
  }

  @Test
  void aRowMatchesOnItsScalarKeys() throws Exception {
    FakeCi fake = createdRoom();
    CiRow row =
        CiScroll.factory((IObject) fake.getProperty("RL_ROOM_TBL"), roomTblRow(fake)).get(0);

    assertTrue(row.isMatch(Map.of("EFFDT", TODAY)), "the collection kept the row from matching");
    assertFalse(row.isMatch(Map.of("EFFDT", "01/01/1900")));
    assertFalse(row.isMatch(Map.of("RL_FLOOR_CD", "1ST_FLOOR")), "a missing key cannot match");
    assertFalse(row.isEmpty());
  }

  @Test
  void setAfterCreateUpdatesTheRowPeopleSoftCreated() throws Exception {
    FakeCi fake = createdRoom();
    CI ci = wrap(fake, new FakeSession(fake));

    ci.set(
        Map.of(
            "RL_ROOM_TBL",
            List.of(
                Map.of("EFFDT", TODAY, "RL_FLOOR_CD", "1ST_FLOOR", "RL_ROOM_TYPE", "APT_2B_1BT"))));

    List<Map<String, Object>> rows = fake.rows("RL_ROOM_TBL");
    assertEquals(1, rows.size(), rows.toString());
    assertEquals("1ST_FLOOR", rows.get(0).get("RL_FLOOR_CD"));
    assertEquals("APT_2B_1BT", rows.get(0).get("RL_ROOM_TYPE"));
    assertEquals(
        "A", rows.get(0).get("EFF_STATUS"), "the row was deleted and re-inserted, not updated");
  }

  @Test
  void aRowWithDifferentKeysIsStillReplaced() throws Exception {
    FakeCi fake = createdRoom();
    CI ci = wrap(fake, new FakeSession(fake));

    ci.set(Map.of("RL_ROOM_TBL", List.of(Map.of("EFFDT", "01/01/2027", "RL_FLOOR_CD", "2ND"))));

    List<Map<String, Object>> rows = fake.rows("RL_ROOM_TBL");
    assertEquals(1, rows.size(), rows.toString());
    assertEquals("01/01/2027", rows.get(0).get("EFFDT"));
    assertEquals("", rows.get(0).get("EFF_STATUS"));
  }
}
