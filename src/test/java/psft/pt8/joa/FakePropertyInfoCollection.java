package psft.pt8.joa;

import java.util.List;

/**
 * In-memory stand-in for PSJOA's {@code CIPropertyInfoCollection}.
 *
 * <p>Only the generic {@link IObject} surface is implemented. That is enough because {@code
 * JOAObject.getProperty("Count")} and {@code invokeMethod("Item", ...)} are generic reflective
 * dispatchers over {@code getCount()} / {@code item(long)} on the real object, so the SDK can reach
 * every collection through {@code IObject} alone.
 */
class FakePropertyInfoCollection implements IObject {

  private final List<JoaFixture.PropSpec> specs;

  FakePropertyInfoCollection(List<JoaFixture.PropSpec> specs) {
    this.specs = specs;
  }

  @Override
  public Object getProperty(String name) throws JOAException {
    if (JoaNames.is(name, "Count")) return (long) specs.size();
    throw new JOAException("Unknown CIPropertyInfoCollection property: " + name);
  }

  @Override
  public void setProperty(String name, Object value) throws JOAException {
    throw new JOAException("CIPropertyInfoCollection is read-only; cannot set " + name);
  }

  @Override
  public Object invokeMethod(String name, Object[] args) throws JOAException {
    if (JoaNames.is(name, "Item")) {
      int index = (int) ((Number) args[0]).longValue();
      if (index < 0 || index >= specs.size())
        throw new JOAException("CIPropertyInfoCollection index out of range: " + index);
      return new FakePropertyInfo(specs.get(index));
    }
    throw new JOAException("Unknown CIPropertyInfoCollection method: " + name);
  }

  @Override
  public String getClassName() {
    return "CompIntfcPropertyInfoCollection";
  }

  @Override
  public String getNamespaceName() {
    return "CompIntfc";
  }
}
