package psft.pt8.joa;

/**
 * In-memory stand-in for PSJOA's package-private {@code CIPropertyInfo}.
 *
 * <p>The field {@code m_fUseEdit} is deliberately named after the PSJOA field, because {@code
 * edu.apu.pssdk.PropertyInfo} reads the FINDKEYS / list-key bits out of it reflectively via {@code
 * getDeclaredFields()}. Renaming it silently turns {@code isFindKey()} and {@code isListKey()} into
 * {@code false} instead of failing, so the name is part of the contract.
 */
class FakePropertyInfo implements IObject {

  private final JoaFixture.PropSpec spec;
  private final int m_fUseEdit;

  FakePropertyInfo(JoaFixture.PropSpec spec) {
    this.spec = spec;
    this.m_fUseEdit = spec.useEdit;
  }

  @Override
  public Object getProperty(String name) throws JOAException {
    if (JoaNames.is(name, "Name")) return spec.name;
    if (JoaNames.is(name, "Key")) return spec.key;
    if (JoaNames.is(name, "IsCollection")) return spec.collection;
    if (JoaNames.is(name, "IsReadOnly")) return spec.readOnly;
    if (JoaNames.is(name, "Required")) return spec.required;
    if (JoaNames.is(name, "PropertyInfoCollection"))
      return new FakePropertyInfoCollection(spec.children);
    throw new JOAException("Unknown CIPropertyInfo property: " + name);
  }

  @Override
  public void setProperty(String name, Object value) throws JOAException {
    throw new JOAException("CIPropertyInfo is read-only; cannot set " + name);
  }

  @Override
  public Object invokeMethod(String name, Object[] args) throws JOAException {
    throw new JOAException("Unknown CIPropertyInfo method: " + name);
  }

  @Override
  public String getClassName() {
    return "CompIntfcPropertyInfo";
  }

  @Override
  public String getNamespaceName() {
    return "CompIntfc";
  }
}
