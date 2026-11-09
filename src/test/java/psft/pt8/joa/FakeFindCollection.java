package psft.pt8.joa;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory stand-in for PSJOA's package-private {@code CICollection}, which is what {@code
 * invokeMethod("Find", ..)} returns on a real CI: one CI object per match, each carrying the
 * FINDKEYS values, and an empty collection when nothing matched.
 *
 * <p>It is deliberately not an {@code ICIScroll}. The SDK reaches it through {@code
 * getProperty("Count")} and {@code invokeMethod("Item", ..)} only, which JOAObject dispatches onto
 * {@code getCount()} / {@code item(long)} on the real class.
 */
class FakeFindCollection implements IObject, ICICollection {

  private final List<FakeRow> items;
  private int cursor;

  FakeFindCollection(List<FakeRow> items) {
    this.items = new ArrayList<>(items);
  }

  /* ---------- IObject ---------- */

  @Override
  public Object getProperty(String name) throws JOAException {
    if (JoaNames.is(name, "Count")) return getCount();
    throw new JOAException("Unknown CICollection property: " + name);
  }

  @Override
  public void setProperty(String name, Object value) throws JOAException {
    throw new JOAException("Cannot set " + name + " on a Find result");
  }

  @Override
  public Object invokeMethod(String name, Object[] args) throws JOAException {
    if (JoaNames.is(name, "Item")) {
      long index = ((Number) args[0]).longValue();
      if (index < 0 || index >= items.size())
        throw new JOAException("Find result index out of range: " + index);
      return item(index);
    }
    if (JoaNames.is(name, "First")) return first();
    if (JoaNames.is(name, "Next")) return next();
    throw new JOAException("Unknown CICollection method: " + name);
  }

  @Override
  public String getClassName() {
    return "CompIntfcCollection";
  }

  @Override
  public String getNamespaceName() {
    return "CompIntfc";
  }

  /* ---------- ICICollection ---------- */

  @Override
  public long getCount() {
    return items.size();
  }

  @Override
  public IObject item(long index) {
    return items.get((int) index);
  }

  @Override
  public IObject first() {
    cursor = 0;
    return next();
  }

  @Override
  public IObject next() {
    return cursor < items.size() ? items.get(cursor++) : null;
  }
}
