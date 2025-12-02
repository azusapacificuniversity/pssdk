package edu.apu.pssdk;

import java.util.ArrayList;
import java.util.Map;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyHashMap;
import org.graalvm.polyglot.proxy.ProxyObject;

/** A class that acts as both a ProxyObject and a ProxyHashMap, backed by a standard Java Map. */
public class ProxyObjectMap implements ProxyObject, ProxyHashMap {

  private final Map<String, Object> map;

  public ProxyObjectMap(Map<String, Object> map) {
    this.map = map;
  }

  // --- Implementation of ProxyObject methods ---

  @Override
  public Object getMember(String key) {
    return map.get(key);
  }

  @Override
  public void putMember(String key, Value value) {
    // You might need a more sophisticated conversion depending on your use case
    map.put(key, value.asHostObject());
  }

  @Override
  public boolean hasMember(String key) {
    return map.containsKey(key);
  }

  @Override
  public Object getMemberKeys() {
    return ProxyArray.fromList(new ArrayList<>(map.keySet()));
  }

  @Override
  public boolean removeMember(String key) {
    return map.remove(key) != null;
  }

  // --- Implementation of ProxyHashMap methods ---
  // These often overlap with ProxyObject functionality or are handled by the map

  @Override
  public long getHashSize() {
    return map.size();
  }

  @Override
  public boolean hasHashEntry(Value key) {
    return map.containsKey(key.asString());
  }

  @Override
  public Object getHashValue(Value key) {
    return map.get(key.asString());
  }

  @Override
  public void putHashEntry(Value key, Value value) {
    putMember(key.asString(), value);
  }

  @Override
  public boolean removeHashEntry(Value key) {
    return removeMember(key.asString());
  }

  @Override
  public Object getHashEntriesIterator() {
    // Returns an iterator of map entries
    return map.entrySet().iterator();
  }

  // --- Additional utility methods if needed ---
  @Override
  public String toString() {
    return map.toString();
  }
}
