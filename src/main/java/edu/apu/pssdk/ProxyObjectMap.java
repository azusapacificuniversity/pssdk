package edu.apu.pssdk;

import java.util.ArrayList;
import java.util.Map;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyHashMap;
import org.graalvm.polyglot.proxy.ProxyObject;

/**
 * A class that acts as both a ProxyObject and a ProxyHashMap, backed by a standard Java Map. NOT
 * sure if we want to keep this or just do ProxyObject and ProxyHashMap conversions
 */
public class ProxyObjectMap implements ProxyObject, ProxyHashMap {

  private final Map<String, Object> map;

  /**
   * Constructor that gets a map to base the ProxyObject and ProxyHashMap implemenations
   *
   * @param map the underlying map upon which this ProxyObjectMap is based off
   */
  public ProxyObjectMap(Map<String, Object> map) {
    this.map = map;
  }

  // --- Implementation of ProxyObject methods ---

  /** Get value when having a key */
  @Override
  public Object getMember(String key) {
    return map.get(key);
  }

  /** Sets a key/value pair to the map */
  @Override
  public void putMember(String key, Value value) {
    // You might need a more sophisticated conversion depending on your use case
    map.put(key, value.asHostObject());
  }

  /** Check if key exists */
  @Override
  public boolean hasMember(String key) {
    return map.containsKey(key);
  }

  /** returns list of keys as arrays */
  @Override
  public Object getMemberKeys() {
    return ProxyArray.fromList(new ArrayList<>(map.keySet()));
  }

  /** Deletes entry from map */
  @Override
  public boolean removeMember(String key) {
    return map.remove(key) != null;
  }

  // --- Implementation of ProxyHashMap methods ---
  // These often overlap with ProxyObject functionality or are handled by the map

  /** Gets the size of the map */
  @Override
  public long getHashSize() {
    return map.size();
  }

  /** Checks if entry exists in the map */
  @Override
  public boolean hasHashEntry(Value key) {
    return map.containsKey(key.asString());
  }

  /** Gets the value of the key */
  @Override
  public Object getHashValue(Value key) {
    return map.get(key.asString());
  }

  /** Set a value to the key */
  @Override
  public void putHashEntry(Value key, Value value) {
    putMember(key.asString(), value);
  }

  /** Deletes the entry from the map */
  @Override
  public boolean removeHashEntry(Value key) {
    return removeMember(key.asString());
  }

  /** Returns an iterator to iterate over the entries */
  @Override
  public Object getHashEntriesIterator() {
    // Returns an iterator of map entries
    return map.entrySet().iterator();
  }

  /**
   * Comma-separated list of key-value mappings enclosed in curly braces ({}), following the pattern
   * `{key=value, key=value, ...}`
   *
   * @return a string representation of the map
   */
  @Override
  public String toString() {
    return map.toString();
  }
}
