  Je comprends, mocker la méthode statique `AdminClient.create` pose un problème car Mockito, dans sa version de base, ne supporte pas le mock des méthodes statiques.

Pour résoudre ce problème, il y a plusieurs approches possibles :

1. **Refactoriser le code pour faciliter les tests** :
   Vous pouvez créer une factory ou une interface pour encapsuler la création de l'`AdminClient`, ce qui vous permettrait de la mocker facilement dans vos tests. C'est une bonne pratique pour rendre le code plus testable.

2. **Utiliser PowerMock avec Mockito** :
   PowerMock est une extension de Mockito qui permet de mocker les méthodes statiques. Cependant, l'utilisation de PowerMock peut compliquer la configuration de vos tests et n'est généralement pas recommandée sauf si c'est vraiment nécessaire.

3. **Injecter l'AdminClient comme dépendance** :
   Au lieu de créer l'`AdminClient` à l'intérieur de la méthode, passez-le en tant que paramètre. Cela rend la méthode plus facile à tester car vous pouvez injecter directement l'`AdminClient` mocké.

Je vais vous montrer comment mettre en œuvre la 3ème approche (Injection de dépendance) :

**Modification du KafkaStatusChecker** :

```java
public class KafkaStatusChecker {

    public static KafkaStatus checkKafkaStatus(AdminClient adminClient) {
        try {
            DescribeClusterResult clusterDescription = adminClient.describeCluster();
            KafkaFuture<Node> controllerFuture = clusterDescription.controller();

            if (controllerFuture.get() != null) {
                return KafkaStatus.OK;
            } else {
                return KafkaStatus.DOWN;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return KafkaStatus.DOWN;
        }
    }
}
```

**Modification du test** :

```java
@Test
public void testKafkaIsUp() throws Exception {
    // Mocking
    AdminClient adminClient = mock(AdminClient.class);
    DescribeClusterResult clusterDescription = mock(DescribeClusterResult.class);
    KafkaFuture<Node> futureNode = mock(KafkaFuture.class);
    Node mockNode = new Node(1, "localhost", 9092);

    when(adminClient.describeCluster()).thenReturn(clusterDescription);
    when(clusterDescription.controller()).thenReturn(futureNode);
    when(futureNode.get()).thenReturn(mockNode);

    KafkaStatus status = KafkaStatusChecker.checkKafkaStatus(adminClient);
    assertEquals(KafkaStatus.OK, status);
}
```

En passant l'`AdminClient` comme paramètre à `checkKafkaStatus`, nous évitons la nécessité de mocker la méthode statique et rendons notre code plus testable.
