package edu.apu.pssdk;

import static edu.apu.pssdk.CiFixtures.personalData;
import static edu.apu.pssdk.CiFixtures.wrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import psft.pt8.joa.FakeCi;
import psft.pt8.joa.FakeSession;
import psft.pt8.joa.JOAException;

/**
 * Covers {@code CI.execute}, which invokes a non-standard CI operation and hands back whatever the
 * PeopleCode returns rather than the CI itself.
 */
class CiExecuteTest {

  private static CI ciWith(FakeCi fake) throws Exception {
    return wrap(fake, new FakeSession(fake));
  }

  @Test
  void returnsWhateverTheOperationReturns() throws Exception {
    FakeCi fake = personalData().whenOperationReturns("SyncToLdap", "SYNCED");

    assertEquals("SYNCED", ciWith(fake).execute("SyncToLdap", null));
    assertTrue(fake.calls().contains("SyncToLdap"));
  }

  @Test
  void aFalseResultIsReturnedRatherThanThrown() throws Exception {
    FakeCi fake = personalData().whenOperationReturns("SyncToLdap", Boolean.FALSE);

    assertEquals(Boolean.FALSE, ciWith(fake).execute("SyncToLdap", null));
  }

  @Test
  void aLongResultComesBackAsALong() throws Exception {
    // CISvc.deserializeObject widens the wire's 32-bit int into a Long, never an Integer.
    FakeCi fake = personalData().whenOperationReturns("CountNotes", 42L);

    Object result = ciWith(fake).execute("CountNotes", null);

    assertInstanceOf(Long.class, result);
    assertEquals(42L, result);
  }

  @Test
  void aVoidOperationReturnsNull() throws Exception {
    FakeCi fake = personalData().whenOperationReturns("Recalculate", null);

    assertNull(ciWith(fake).execute("Recalculate", null));
  }

  @Test
  void dataIsSetOnTheCiBeforeTheOperationRuns() throws Exception {
    FakeCi fake = personalData().whenOperationReturns("SyncToLdap", "SYNCED");

    ciWith(fake).execute("SyncToLdap", Map.of("NAME", "Samuel Demirdjian"));

    assertEquals("Samuel Demirdjian", fake.data().get("NAME"));
    assertTrue(fake.calls().contains("SyncToLdap"));
  }

  @Test
  void nullDataSkipsTheSet() throws Exception {
    FakeCi fake = personalData().whenOperationReturns("SyncToLdap", "SYNCED");

    ciWith(fake).execute("SyncToLdap", null);

    assertEquals("", fake.data().get("NAME"));
  }

  @Test
  void aStandardOperationIsRejectedWhateverItsCase() throws Exception {
    FakeCi fake = personalData();
    CI ci = ciWith(fake);

    for (String operation : new String[] {"save", "Save", "GET", "cancel"}) {
      PssdkException e = assertThrows(PssdkException.class, () -> ci.execute(operation, null));
      assertTrue(e.getMessage().contains("standard operation"), e.getMessage());
    }
    assertFalse(fake.calls().contains("Save"), "the CI should not have been invoked");
  }

  @Test
  void anUnknownOperationIsWrappedAsAPssdkException() throws Exception {
    CI ci = ciWith(personalData());

    PssdkException e = assertThrows(PssdkException.class, () -> ci.execute("NoSuchOp", null));

    assertTrue(e.getMessage().contains("NoSuchOp"), e.getMessage());
    assertInstanceOf(JOAException.class, e.getCause());
  }
}
