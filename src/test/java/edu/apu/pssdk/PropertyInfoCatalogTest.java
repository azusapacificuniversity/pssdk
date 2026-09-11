package edu.apu.pssdk;

import static edu.apu.pssdk.CiFixtures.personalData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import psft.pt8.joa.FakeCi;

/** Verifies that CI metadata is read correctly off the generic PSJOA IObject surface. */
class PropertyInfoCatalogTest {

  @Test
  void buildsAllFourKeyCollections() throws Exception {
    FakeCi fake = personalData();

    PropertyInfoCatalog catalog = PropertyInfoCatalog.buildFor(fake);

    assertEquals(5, catalog.getPropertyInfoCollection().count());
    assertEquals(1, catalog.getGetKeyInfoCollection().count());
    assertEquals(1, catalog.getCreateKeyInfoCollection().count());
    assertEquals(2, catalog.getFindPropertyInfoCollection().count());
  }

  @Test
  void readsPerPropertyFlags() throws Exception {
    PropertyInfoCollection props =
        PropertyInfoCatalog.buildFor(personalData()).getPropertyInfoCollection();

    assertTrue(props.get("EMPLID").isKey());
    assertTrue(props.get("EMPLID").isRequired());
    assertFalse(props.get("NAME").isKey());
    assertTrue(props.get("LAST_UPD_DTTM").isReadOnly());
    assertTrue(props.get("PHONES").isCollection());
  }

  @Test
  void readsFindKeyAndListKeyBitsOutOfUseEdit() throws Exception {
    PropertyInfoCollection findKeys =
        PropertyInfoCatalog.buildFor(personalData()).getFindPropertyInfoCollection();

    assertTrue(findKeys.get("EMPLID").isListKey(), "EMPLID is a list box item");
    assertFalse(findKeys.get("EMPLID").isFindKey());
    assertTrue(findKeys.get("NAME").isFindKey(), "NAME is an alternate search key");
    assertFalse(findKeys.get("NAME").isListKey());
  }

  @Test
  void nestedCollectionsCarryTheirOwnPropertyInfo() throws Exception {
    PropertyInfoCollection props =
        PropertyInfoCatalog.buildFor(personalData()).getPropertyInfoCollection();

    PropertyInfoCollection phones = props.get("PHONES").getPropertyInfoCollection();

    assertEquals(2, phones.count());
    assertTrue(phones.get("PHONE_TYPE").isKey());
    assertFalse(phones.get("PHONE").isKey());
  }

  @Test
  void iterationPreservesDeclarationOrder() throws Exception {
    List<String> names = new ArrayList<>();
    for (PropertyInfo pi : PropertyInfoCatalog.buildFor(personalData()).getPropertyInfoCollection())
      names.add(pi.getName());

    assertEquals(List.of("EMPLID", "NAME", "BIRTHDATE", "LAST_UPD_DTTM", "PHONES"), names);
  }
}
