package com.sporty.settlement;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.List;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;

/**
 * Full flow with the default profile against a real RocketMQ broker. The name server, the broker
 * and the gRPC proxy (local mode, in the broker process) run in one container.
 */
class RocketMqBetSettlementFlowTest extends AbstractBetSettlementFlowTest {

  /**
   * Topic routes carry the broker IP and the proxy's own gRPC port. So the broker advertises {@code
   * 127.0.0.1} (the container IP is not reachable from a macOS host), and the host port must equal
   * the container port.
   */
  private static final int PROXY_PORT = freePort();

  private static final String START_SCRIPT =
      """
      sh mqnamesrv > /tmp/namesrv.log 2>&1 &
      until grep -q 'boot success' /tmp/namesrv.log; do sleep 1; done
      sh mqbroker -n 127.0.0.1:9876 --enable-proxy -pc /tmp/rmq-proxy.json &
      until sh mqadmin clusterList -n 127.0.0.1:9876 2>&1 | grep -q DefaultCluster; do sleep 1; done
      sh mqadmin updatetopic -n 127.0.0.1:9876 -c DefaultCluster -t bet-settlements -a +message.type=NORMAL
      sh mqadmin updateSubGroup -n 127.0.0.1:9876 -c DefaultCluster -g bet-settlement-consumer
      echo 'RocketMQ ready'
      wait
      """;

  @Container static final GenericContainer<?> ROCKETMQ = rocketMq();

  @DynamicPropertySource
  static void rocketMqProperties(DynamicPropertyRegistry registry) {
    registry.add("app.rocketmq.endpoint", () -> "localhost:" + PROXY_PORT);
  }

  private static GenericContainer<?> rocketMq() {
    GenericContainer<?> container =
        new GenericContainer<>("apache/rocketmq:5.5.0")
            .withEnv("JAVA_OPT_EXT", "-Xms256m -Xmx512m -Xmn128m")
            .withCopyToContainer(Transferable.of("brokerIP1=127.0.0.1\n"), "/tmp/broker.conf")
            .withCopyToContainer(
                Transferable.of(
                    """
                    {"grpcServerPort":%d,"brokerConfigPath":"/tmp/broker.conf"}\
                    """
                        .formatted(PROXY_PORT)),
                "/tmp/rmq-proxy.json")
            .withCommand("sh", "-c", START_SCRIPT)
            .waitingFor(
                Wait.forLogMessage(".*RocketMQ ready.*\\n", 1)
                    .withStartupTimeout(Duration.ofMinutes(3)));
    container.setPortBindings(List.of(PROXY_PORT + ":" + PROXY_PORT));
    return container;
  }

  private static int freePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
