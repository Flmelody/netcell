package org.flmelody.netcell.core.provider.delivery;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author esotericman
 */
public class LocalMessageDeliveryProvider extends AbstractMessageDeliveryProvider {
  private static final Map<String, Set<String>> subscribers = new ConcurrentHashMap<>();

  @Override
  public synchronized void subscribe(String topic, String clientId) {
    subscribers.computeIfAbsent(topic, k -> new HashSet<>()).add(clientId);
  }

  @Override
  public void unsubscribe(String topic, String clientId) {
    subscribers.get(topic).remove(clientId);
  }
}
