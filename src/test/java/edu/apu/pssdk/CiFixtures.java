package edu.apu.pssdk;

import static psft.pt8.joa.JoaFixture.ci;
import static psft.pt8.joa.JoaFixture.prop;

import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import psft.pt8.joa.FakeCi;
import psft.pt8.joa.JOAException;

/** Shared fixtures and small assertion helpers for the fake-JOA tests. */
final class CiFixtures {

  private CiFixtures() {}

  /**
   * A CI shaped like a typical PeopleSoft personal-data interface: a keyed root with a read-only
   * property and one child collection, plus distinct GET, CREATE and FIND key collections.
   */
  static FakeCi personalData() {
    return ci("CI_PERSONAL_DATA")
        .properties(
            prop("EMPLID").key().required(),
            prop("NAME"),
            prop("BIRTHDATE"),
            prop("LAST_UPD_DTTM").readOnly(),
            prop("PHONES").of(prop("PHONE_TYPE").key(), prop("PHONE")))
        .getKeys(prop("EMPLID").key())
        .createKeys(prop("EMPLID").key())
        .findKeys(prop("EMPLID").key().listKey(), prop("NAME").findKey())
        .build();
  }

  /** Wires a CI wrapper around a fake, exactly as AppServer.ciFactory would around a real one. */
  static CI wrap(FakeCi fake, psft.pt8.joa.ISession session) throws JOAException {
    return CI.factory(fake, session, PropertyInfoCatalog.buildFor(fake));
  }

  static Object member(Object proxyObject, String name) {
    return ((ProxyObject) proxyObject).getMember(name);
  }

  static long size(Object proxyArray) {
    return ((ProxyArray) proxyArray).getSize();
  }

  static Object element(Object proxyArray, long index) {
    return ((ProxyArray) proxyArray).get(index);
  }
}
