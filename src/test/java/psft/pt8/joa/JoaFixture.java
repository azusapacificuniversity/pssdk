package psft.pt8.joa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Test-only builder that describes the shape of a Component Interface and produces an in-memory
 * {@link FakeCi} implementing it.
 *
 * <p>This class lives in {@code psft.pt8.joa} on purpose: the PSJOA interfaces that identify a
 * scroll and a row ({@code ICIScroll}, {@code ICIRow}) are package-private, so a fake can only
 * implement them from inside the vendor package. The jar is not sealed, so adding classes to that
 * package from the test source set is the way to go.
 *
 * <p>Example:
 *
 * <pre>{@code
 * FakeCi ci = JoaFixture.ci("CI_PERSONAL_DATA")
 *     .properties(prop("EMPLID").key(), prop("NAME"), prop("PHONES").of(prop("PHONE_TYPE").key(), prop("PHONE")))
 *     .getKeys(prop("EMPLID").key())
 *     .build();
 * }</pre>
 */
public final class JoaFixture {

  /** Bit in PSJOA's {@code m_fUseEdit} that marks an alternate search key (a FINDKEYS member). */
  public static final int ALTERNATE_SEARCH_KEY = 16;

  /** Bit in PSJOA's {@code m_fUseEdit} that marks a list box item (returned by a Find). */
  public static final int LISTBOX_ITEM_NUM = 32;

  private JoaFixture() {}

  /** Starts describing a CI. */
  public static CiSpec ci(String name) {
    return new CiSpec(name);
  }

  /** Starts describing a single CI property. */
  public static PropSpec prop(String name) {
    return new PropSpec(name);
  }

  /** Metadata for one CI property, mirroring what PSJOA exposes through CIPropertyInfo. */
  public static final class PropSpec {
    final String name;
    final List<PropSpec> children = new ArrayList<>();
    boolean key;
    boolean required;
    boolean readOnly;
    boolean collection;
    int useEdit;

    PropSpec(String name) {
      this.name = name;
    }

    public PropSpec key() {
      this.key = true;
      return this;
    }

    public PropSpec required() {
      this.required = true;
      return this;
    }

    public PropSpec readOnly() {
      this.readOnly = true;
      return this;
    }

    /** Marks the property as a FINDKEYS member (alternate search key). */
    public PropSpec findKey() {
      this.useEdit |= ALTERNATE_SEARCH_KEY;
      return this;
    }

    /** Marks the property as a list box item, i.e. populated by a Find. */
    public PropSpec listKey() {
      this.useEdit |= LISTBOX_ITEM_NUM;
      return this;
    }

    /** Turns the property into a collection (scroll) with the given row properties. */
    public PropSpec of(PropSpec... rowProperties) {
      this.collection = true;
      for (PropSpec p : rowProperties) this.children.add(p);
      return this;
    }
  }

  /** Metadata for a whole CI: its properties plus its GET, CREATE and FIND key collections. */
  public static final class CiSpec {
    final String name;
    final List<PropSpec> properties = new ArrayList<>();
    final List<PropSpec> getKeys = new ArrayList<>();
    final List<PropSpec> createKeys = new ArrayList<>();
    final List<PropSpec> findKeys = new ArrayList<>();

    CiSpec(String name) {
      this.name = name;
    }

    public CiSpec properties(PropSpec... specs) {
      for (PropSpec p : specs) properties.add(p);
      return this;
    }

    public CiSpec getKeys(PropSpec... specs) {
      for (PropSpec p : specs) getKeys.add(p);
      return this;
    }

    public CiSpec createKeys(PropSpec... specs) {
      for (PropSpec p : specs) createKeys.add(p);
      return this;
    }

    public CiSpec findKeys(PropSpec... specs) {
      for (PropSpec p : specs) findKeys.add(p);
      return this;
    }

    public FakeCi build() {
      return new FakeCi(this);
    }

    /** Every property the root CI object must answer to, deduplicated by name. */
    List<PropSpec> rootRowSpec() {
      Map<String, PropSpec> byName = new LinkedHashMap<>();
      for (List<PropSpec> group : List.of(properties, getKeys, createKeys, findKeys))
        for (PropSpec p : group) byName.putIfAbsent(p.name, p);
      return new ArrayList<>(byName.values());
    }
  }
}
