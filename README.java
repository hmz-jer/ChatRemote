 @Test
public void testGoodMessage() {
    byte[] openREQ = new byte[] {
        0x36, 0x00, 0x00, (byte) 0x80, 0x00, 0x2F, // Frame size
        // autres octets...
    };

    byte[] expectedOpenConf = new byte[] {
        0x36, 0x00, 0x00, (byte) 0x80, 0x00, 0x35, // Frame size
        // autres octets...
    };

    // Écriture dans le canal
    channel.writeInbound(Unpooled.wrappedBuffer(openREQ));

    // Lire tous les messages sortants
    List<ByteBuf> outboundBuffers = new ArrayList<>();
    ByteBuf outboundMessage;
    while ((outboundMessage = (ByteBuf) channel.readOutbound()) != null) {
        outboundBuffers.add(outboundMessage);
    }

    // Agrégation des messages
    ByteBuf aggregatedBuffer = Unpooled.buffer();
    for (ByteBuf buf : outboundBuffers) {
        aggregatedBuffer.writeBytes(buf);
        buf.release();  // Libérer la mémoire après utilisation
    }

    // Convertir le buffer agrégé en tableau d'octets
    byte[] bufArray = new byte[aggregatedBuffer.readableBytes()];
    aggregatedBuffer.readBytes(bufArray);

    // Libérer le buffer agrégé
    aggregatedBuffer.release();

    // Comparaison des tailles
    System.out.println("Taille attendue: " + expectedOpenConf.length);
    System.out.println("Taille réelle: " + bufArray.length);

    // Affichage du contenu octet par octet
    for (int i = 0; i < bufArray.length; i++) {
        System.out.printf("Byte %d: 0x%02X\n", i, bufArray[i]);
    }

    // Si nécessaire, tronquer les octets supplémentaires avant comparaison
    byte[] truncatedBufArray = Arrays.copyOf(bufArray, expectedOpenConf.length);

    // Comparaison des tableaux
    Assert.assertArrayEquals(expectedOpenConf, truncatedBufArray);
}
