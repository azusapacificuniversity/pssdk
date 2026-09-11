package psft.pt8.joa;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory stand-in for {@code ISession}. Returns a preconfigured {@link FakeCi} from {@code
 * getCompIntfc} and can be primed with PeopleSoft messages so the error paths that build a {@code
 * PssdkException} can be exercised without an app server.
 */
public final class FakeSession implements ISession {

  private final FakeCi ci;
  private final List<String> messages = new ArrayList<>();
  private boolean connected = true;
  private boolean errorPending;
  private boolean disconnectSucceeds = true;

  public FakeSession(FakeCi ci) {
    this.ci = ci;
  }

  /** Primes an error condition so {@code PssdkException} collects these messages. */
  public FakeSession withPendingError(String... texts) {
    errorPending = true;
    for (String t : texts) messages.add(t);
    return this;
  }

  /** Makes disconnect() fail, so CI.close() raises an IOException. */
  public FakeSession failDisconnect() {
    disconnectSucceeds = false;
    return this;
  }

  public boolean isConnected() {
    return connected;
  }

  /* ---------- ISession ---------- */

  @Override
  public boolean connect(long type, String path, String user, String password, byte[] extra) {
    connected = true;
    return true;
  }

  @Override
  public boolean connectS(
      long type, String path, String user, String password, byte[] extra, String domainPassword) {
    connected = true;
    return true;
  }

  @Override
  public boolean connectUsingCountryCd(
      long type, String path, String user, String password, String country, byte[] extra) {
    connected = true;
    return true;
  }

  @Override
  public boolean connectUsingCountryCdS(
      long type,
      String path,
      String user,
      String password,
      String country,
      byte[] extra,
      String domainPassword) {
    connected = true;
    return true;
  }

  @Override
  public boolean disconnect() {
    if (!disconnectSucceeds) return false;
    connected = false;
    return true;
  }

  @Override
  public Object getComponent(String name) {
    return null;
  }

  @Override
  public Object getCompIntfc(String name) {
    return ci;
  }

  @Override
  public boolean getErrorPending() {
    return errorPending;
  }

  @Override
  public boolean getWarningPending() {
    return false;
  }

  @Override
  public IPSMessageCollection getPSMessages() {
    return new FakeMessageCollection(messages);
  }

  @Override
  public IRegionalSettings getRegionalSettings() {
    return null;
  }

  @Override
  public boolean getSuspendFormatting() {
    return false;
  }

  @Override
  public void setSuspendFormatting(boolean suspend) {}

  @Override
  public INamespace getNamespace(String name) {
    return null;
  }

  @Override
  public String sendSynchronizationRequest(String request) {
    return "";
  }

  /* ---------- IObject ---------- */

  @Override
  public Object getProperty(String name) throws JOAException {
    throw new JOAException("Unknown Session property: " + name);
  }

  @Override
  public void setProperty(String name, Object value) throws JOAException {
    throw new JOAException("Unknown Session property: " + name);
  }

  @Override
  public Object invokeMethod(String name, Object[] args) throws JOAException {
    throw new JOAException("Unknown Session method: " + name);
  }

  @Override
  public String getClassName() {
    return "Session";
  }

  @Override
  public String getNamespaceName() {
    return "PeopleSoft";
  }

  private static final class FakeMessageCollection implements IPSMessageCollection {
    private final List<String> texts;

    FakeMessageCollection(List<String> texts) {
      this.texts = texts;
    }

    @Override
    public long getCount() {
      return texts.size();
    }

    @Override
    public IPSMessage item(long index) {
      return new FakeMessage(texts.get((int) index));
    }

    @Override
    public IPSMessage first() {
      return texts.isEmpty() ? null : item(0);
    }

    @Override
    public IPSMessage next() {
      return null;
    }

    @Override
    public void deleteItem(long index) {
      texts.remove((int) index);
    }

    @Override
    public void deleteAll() {
      texts.clear();
    }
  }

  private static final class FakeMessage implements IPSMessage {
    private final String text;

    FakeMessage(String text) {
      this.text = text;
    }

    @Override
    public long getType() {
      return 1L;
    }

    @Override
    public long getCode() {
      return 0L;
    }

    @Override
    public String getText() {
      return text;
    }

    @Override
    public String getExplainText() {
      return text;
    }

    @Override
    public String getSource() {
      return "FakeSession";
    }

    @Override
    public long getMessageSetNumber() {
      return 91L;
    }

    @Override
    public long getMessageNumber() {
      return 37L;
    }

    @Override
    public long getMessageType() {
      return 1L;
    }
  }
}
