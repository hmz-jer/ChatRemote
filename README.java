 Bien sûr, je vais adapter le test unitaire en conséquence.

Avec le changement, le test se focalisera sur la méthode `checkKafkaStatus` en lui fournissant un mock de `Properties` et vérifiera si elle retourne le statut approprié.

Voici comment vous pourriez adapter le test unitaire:

```java
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.junit.Test;

import java.util.Properties;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class KafkaStatusCheckerTest {

    @Test
    public void testKafkaIsUp() throws Exception {
        // Mocking
        Properties mockProperties = mock(Properties.class);
        AdminClient adminClient = mock(AdminClient.class);
        DescribeClusterResult clusterDescription = mock(DescribeClusterResult.class);
        KafkaFuture<Node> futureNode = mock(KafkaFuture.class);
        Node mockNode = new Node(1, "localhost", 9092); // Mock Node

        when(AdminClient.create(mockProperties)).thenReturn(adminClient);
        when(adminClient.describeCluster()).thenReturn(clusterDescription);
        when(clusterDescription.controller()).thenReturn(futureNode);
        when(futureNode.get()).thenReturn(mockNode);

        KafkaStatus status = KafkaStatusChecker.checkKafkaStatus(mockProperties);

        assertEquals(KafkaStatus.OK, status);
    }

    @Test
    public void testKafkaIsDown() throws Exception {
        // Mocking
        Properties mockProperties = mock(Properties.class);
        AdminClient adminClient = mock(AdminClient.class);
        DescribeClusterResult clusterDescription = mock(DescribeClusterResult.class);
        KafkaFuture<Node> futureNode = mock(KafkaFuture.class);

        when(AdminClient.create(mockProperties)).thenReturn(adminClient);
        when(adminClient.describeCluster()).thenReturn(clusterDescription);
        when(clusterDescription.controller()).thenReturn(futureNode);
        when(futureNode.get()).thenReturn(null);  // Simuler que Kafka est down

        KafkaStatus status = KafkaStatusChecker.checkKafkaStatus(mockProperties);

        assertEquals(KafkaStatus.DOWN, status);
    }
}
```

Dans ces tests, nous mockons la création de `AdminClient`, les appels à `describeCluster()`, et le résultat du futur. Ensuite, nous appelons la méthode `checkKafkaStatus` avec les propriétés mockées et vérifions si elle retourne le bon statut. Il y a deux tests: un pour vérifier lorsque Kafka est "up" et un autre pour vérifier lorsqu'il est "down".
