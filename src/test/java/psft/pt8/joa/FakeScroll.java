package psft.pt8.joa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * In-memory stand-in for PSJOA's package-private {@code CIScroll}. Implements {@code ICIScroll} so
 * that {@code edu.apu.pssdk.Is.ciScroll(..)} recognises it the same way it recognises a real
 * scroll.
 *
 * <p>A freshly built scroll holds a single blank row, which is what a real CI returns when {@code
 * GetDummyRows} is on and what {@code CiScroll.isEmpty()} keys off.
 */
class FakeScroll implements IObject, ICIScroll {

  /**
   * Where {@code InsertItem(n)} puts the new row. PeopleSoft documents InsertItem as inserting
   * <em>after</em> the given index; this is the one behaviour of the fake that is worth pinning
   * down with an integration test against a real app server, because {@code
   * CiScroll.insertEmptyRow()} always passes 0 and therefore depends on it for row ordering.
   */
  private static final boolean INSERT_AFTER_INDEX = true;

  private final List<JoaFixture.PropSpec> rowSpec;
  private final List<FakeRow> rows = new ArrayList<>();

  FakeScroll(List<JoaFixture.PropSpec> rowSpec) {
    this.rowSpec = rowSpec;
    this.rows.add(new FakeRow(rowSpec));
  }

  /* ---------- IObject ---------- */

  @Override
  public Object getProperty(String name) throws JOAException {
    if (JoaNames.is(name, "Count")) return (long) rows.size();
    throw new JOAException("Unknown CIScroll property: " + name);
  }

  @Override
  public void setProperty(String name, Object value) throws JOAException {
    throw new JOAException("Cannot set " + name + " on a CI collection");
  }

  @Override
  public Object invokeMethod(String name, Object[] args) throws JOAException {
    if (JoaNames.is(name, "Item")) return item(index(args[0]));
    if (JoaNames.is(name, "InsertItem")) return insertItem(index(args[0]));
    if (JoaNames.is(name, "DeleteItem")) return deleteItem(index(args[0]));
    if (JoaNames.is(name, "CurrentItem")) return currentItem();
    throw new JOAException("Unknown CIScroll method: " + name);
  }

  @Override
  public String getClassName() {
    return "CompIntfcCollection";
  }

  @Override
  public String getNamespaceName() {
    return "CompIntfc";
  }

  /* ---------- ICIScroll ---------- */

  @Override
  public long getCount() {
    return rows.size();
  }

  @Override
  public IObject item(long index) throws JOAException {
    if (index < 0 || index >= rows.size())
      throw new JOAException("CI collection index out of range: " + index);
    return rows.get((int) index);
  }

  @Override
  public IObject insertItem(long index) throws JOAException {
    int at = INSERT_AFTER_INDEX ? (int) index + 1 : (int) index;
    at = Math.max(0, Math.min(at, rows.size()));
    FakeRow row = new FakeRow(rowSpec);
    rows.add(at, row);
    return row;
  }

  @Override
  public boolean deleteItem(long index) throws JOAException {
    if (index < 0 || index >= rows.size()) return false;
    rows.remove((int) index);
    return true;
  }

  @Override
  public IObject currentItem() throws JOAException {
    return item(0);
  }

  @Override
  public long currentItemNum() {
    return 0L;
  }

  @Override
  public IObject getEffectiveItem(String effdt, long sequence) throws JOAException {
    return item(0);
  }

  @Override
  public long getEffectiveItemNum(String effdt, long sequence) {
    return 0L;
  }

  /* ---------- test-side helpers ---------- */

  void apply(List<Map<String, Object>> dataList) {
    rows.clear();
    if (dataList.isEmpty()) {
      rows.add(new FakeRow(rowSpec));
      return;
    }
    for (Map<String, Object> data : dataList) {
      FakeRow row = new FakeRow(rowSpec);
      row.apply(data);
      rows.add(row);
    }
  }

  List<Map<String, Object>> snapshot() {
    return FakeRow.snapshotAll(rows);
  }

  private static long index(Object arg) {
    return ((Number) arg).longValue();
  }
}
