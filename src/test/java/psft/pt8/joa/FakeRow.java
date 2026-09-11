package psft.pt8.joa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory stand-in for PSJOA's package-private {@code CIRow}. Implements {@code ICIRow} so that
 * {@code edu.apu.pssdk.Is.ciRow(..)} recognises it the same way it recognises a real row.
 */
class FakeRow implements IObject, ICIRow {

  private final List<JoaFixture.PropSpec> spec;
  private final Map<String, Object> values = new LinkedHashMap<>();

  FakeRow(List<JoaFixture.PropSpec> spec) {
    this.spec = spec;
    for (JoaFixture.PropSpec p : spec)
      values.put(p.name, p.collection ? new FakeScroll(p.children) : "");
  }

  /* ---------- IObject ---------- */

  @Override
  public Object getProperty(String name) throws JOAException {
    String key = JoaNames.keyOf(values, name);
    if (key == null) throw new JOAException("Property " + name + " does not exist on this CI row");
    return values.get(key);
  }

  @Override
  public void setProperty(String name, Object value) throws JOAException {
    String key = JoaNames.keyOf(values, name);
    if (key == null) throw new JOAException("Property " + name + " does not exist on this CI row");
    if (values.get(key) instanceof FakeScroll)
      throw new JOAException("Property " + name + " is a collection and cannot be set directly");
    values.put(key, value);
  }

  @Override
  public Object invokeMethod(String name, Object[] args) throws JOAException {
    throw new JOAException("Unknown CIRow method: " + name);
  }

  @Override
  public String getClassName() {
    return "CompIntfcRow";
  }

  @Override
  public String getNamespaceName() {
    return "CompIntfc";
  }

  /* ---------- ICIRow ---------- */

  @Override
  public long getItemNum() {
    return 0L;
  }

  @Override
  public Object getPropertyByName(String name) throws JOAException {
    return getProperty(name);
  }

  @Override
  public long setPropertyByName(String name, Object value) throws JOAException {
    setProperty(name, value);
    return 0L;
  }

  @Override
  public IObject getPropertyInfoByName(String name) throws JOAException {
    for (JoaFixture.PropSpec p : spec)
      if (JoaNames.is(name, p.name)) return new FakePropertyInfo(p);
    throw new JOAException("Property " + name + " does not exist on this CI row");
  }

  /* ---------- test-side helpers (independent of the SDK under test) ---------- */

  /** Seeds this row directly, bypassing the SDK, so fixtures never depend on the code they test. */
  @SuppressWarnings("unchecked")
  void apply(Map<String, Object> data) {
    for (Map.Entry<String, Object> e : data.entrySet()) {
      String key = JoaNames.keyOf(values, e.getKey());
      if (key == null) key = e.getKey();
      if (values.get(key) instanceof FakeScroll scroll)
        scroll.apply((List<Map<String, Object>>) e.getValue());
      else values.put(key, e.getValue());
    }
  }

  /** Plain nested Map/List view of this row, for assertions. */
  Map<String, Object> snapshot() {
    Map<String, Object> out = new LinkedHashMap<>();
    for (Map.Entry<String, Object> e : values.entrySet())
      out.put(
          e.getKey(), e.getValue() instanceof FakeScroll scroll ? scroll.snapshot() : e.getValue());
    return out;
  }

  /** True when every key property is blank, which is how the SDK detects an unused dummy row. */
  boolean isBlankKeyed() {
    for (JoaFixture.PropSpec p : spec)
      if (p.key && "".equals(String.valueOf(values.get(p.name)))) return true;
    return false;
  }

  List<JoaFixture.PropSpec> spec() {
    return spec;
  }

  static List<Map<String, Object>> snapshotAll(List<FakeRow> rows) {
    List<Map<String, Object>> out = new ArrayList<>();
    for (FakeRow r : rows) out.add(r.snapshot());
    return out;
  }
}
