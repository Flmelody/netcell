package org.flmelody.netcell;

import org.flmelody.netcell.core.provider.session.LocalSessionProvider;

/**
 * @author esotericman
 */
public class MqttBrokerTest {
  public static void main(String[] args) throws Exception {
    Netcell.setup().use(new LocalSessionProvider()).run();
  }
}
