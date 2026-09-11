package psft.pt8.joa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory stand-in for the root object PSJOA hands back from {@code ISession.getCompIntfc(..)}.
 *
 * <p>The entire PSJOA surface the SDK touches is the five-method {@link IObject} interface, so a
 * fake only has to answer {@code getProperty} / {@code setProperty} / {@code invokeMethod}. Nothing
 * here talks to an app server.
 *
 * <p>Note the dual role of the root object, which mirrors real PSJOA: before a Find it behaves like
 * a row (properties by name); after a Find the SDK treats it as a collection and asks it for {@code
 * Count} and {@code Item}.
 */
public final class FakeCi implements IObject {

  private final JoaFixture.CiSpec spec;
  private final FakeRow root;
  private final Map<String, Object> options = new LinkedHashMap<>();
  private final List<String> calls = new ArrayList<>();
  private final List<FakeRow> findRows = new ArrayList<>();
  private final Map<String, Object> operationResults = new LinkedHashMap<>();

  private Map<String, Object> getResult = Map.of();
  private List<Map<String, Object>> findResult = List.of();
  private Map<String, Object> savedData;
  private boolean getSucceeds = true;
  private boolean createSucceeds = true;
  private boolean saveSucceeds = true;
  private boolean findSucceeds = true;
  private boolean cancelSucceeds = true;

  FakeCi(JoaFixture.CiSpec spec) {
    this.spec = spec;
    this.root = new FakeRow(spec.rootRowSpec());
    options.put("InteractiveMode", false);
    options.put("GetHistoryItems", false);
    options.put("EditHistoryItems", false);
    options.put("GetDummyRows", true);
    options.put("StopOnFirstError", false);
  }

  /* ---------- IObject ---------- */

  @Override
  public Object getProperty(String name) throws JOAException {
    if (JoaNames.is(name, "PropertyInfoCollection"))
      return new FakePropertyInfoCollection(spec.properties);
    if (JoaNames.is(name, "GetKeyInfoCollection"))
      return new FakePropertyInfoCollection(spec.getKeys);
    if (JoaNames.is(name, "CreateKeyInfoCollection"))
      return new FakePropertyInfoCollection(spec.createKeys);
    if (JoaNames.is(name, "FindKeyInfoCollection"))
      return new FakePropertyInfoCollection(spec.findKeys);
    if (JoaNames.is(name, "Count")) return (long) findRows.size();

    String option = JoaNames.keyOf(options, name);
    if (option != null) return options.get(option);
    return root.getProperty(name);
  }

  @Override
  public void setProperty(String name, Object value) throws JOAException {
    String option = JoaNames.keyOf(options, name);
    if (option != null) {
      options.put(option, value);
      return;
    }
    root.setProperty(name, value);
  }

  @Override
  public Object invokeMethod(String name, Object[] args) throws JOAException {
    calls.add(name);
    if (JoaNames.is(name, "Get")) {
      if (!getSucceeds) return Boolean.FALSE;
      root.apply(getResult);
      return Boolean.TRUE;
    }
    if (JoaNames.is(name, "Create")) return createSucceeds;
    if (JoaNames.is(name, "Save")) {
      savedData = root.snapshot();
      return saveSucceeds;
    }
    if (JoaNames.is(name, "Find")) {
      if (!findSucceeds) return Boolean.FALSE;
      findRows.clear();
      for (Map<String, Object> row : findResult) {
        FakeRow r = new FakeRow(spec.findKeys);
        r.apply(row);
        findRows.add(r);
      }
      return Boolean.TRUE;
    }
    if (JoaNames.is(name, "Cancel")) return cancelSucceeds;
    if (JoaNames.is(name, "Item")) {
      int index = (int) ((Number) args[0]).longValue();
      if (index < 0 || index >= findRows.size())
        throw new JOAException("Find result index out of range: " + index);
      return findRows.get(index);
    }
    String operation = JoaNames.keyOf(operationResults, name);
    if (operation != null) return operationResults.get(operation);
    throw new JOAException("Unknown CI method: " + name);
  }

  @Override
  public String getClassName() {
    return spec.name;
  }

  @Override
  public String getNamespaceName() {
    return "CompIntfc";
  }

  /* ---------- stubbing ---------- */

  /** Data a successful Get should load into the CI. */
  public FakeCi whenGetReturns(Map<String, Object> data) {
    this.getResult = data;
    return this;
  }

  /** Rows a successful Find should return, shaped by the FINDKEYS collection. */
  public FakeCi whenFindReturns(List<Map<String, Object>> rows) {
    this.findResult = rows;
    return this;
  }

  /**
   * Value a non-standard CI operation should return. PeopleSoft lets these return anything the
   * PeopleCode declares, including nothing, so {@code value} may be null.
   */
  public FakeCi whenOperationReturns(String name, Object value) {
    this.operationResults.put(name, value);
    return this;
  }

  public FakeCi failGet() {
    this.getSucceeds = false;
    return this;
  }

  public FakeCi failCreate() {
    this.createSucceeds = false;
    return this;
  }

  public FakeCi failSave() {
    this.saveSucceeds = false;
    return this;
  }

  public FakeCi failFind() {
    this.findSucceeds = false;
    return this;
  }

  public FakeCi failCancel() {
    this.cancelSucceeds = false;
    return this;
  }

  /* ---------- inspection ---------- */

  /** Current contents of the CI as plain nested maps and lists. */
  public Map<String, Object> data() {
    return root.snapshot();
  }

  /** Contents of the CI at the moment Save was last invoked, or null if it never was. */
  public Map<String, Object> savedData() {
    return savedData;
  }

  /** Standard CI methods invoked so far, in order. */
  public List<String> calls() {
    return List.copyOf(calls);
  }

  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> rows(String collectionName) {
    return (List<Map<String, Object>>) root.snapshot().get(collectionName);
  }
}
