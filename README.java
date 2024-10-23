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

    // Écrire dans le canal avec un buffer non sécurisé
    channel.writeInbound(Unpooled.wrappedBuffer(openREQ));

    // Lire tous les messages sortants dans outboundMessages
    List<ByteBuf> outboundBuffers = new ArrayList<>();
    ByteBuf outboundMessage;
    while ((outboundMessage = (ByteBuf) channel.readOutbound()) != null) {
        outboundBuffers.add(outboundMessage);
    }

    // Agrégation ou traitement des messages
    ByteBuf aggregatedBuffer = Unpooled.buffer();
    for (ByteBuf buf : outboundBuffers) {
        aggregatedBuffer.writeBytes(buf);
        buf.release();  // Libérer la mémoire après utilisation
    }

    // Convertir le buffer agrégé en tableau d'octets pour comparaison
    byte[] bufArray = new byte[aggregatedBuffer.readableBytes()];
    aggregatedBuffer.readBytes(bufArray);

    // Libérer le buffer agrégé
    aggregatedBuffer.release();

    // Comparer avec le tableau attendu
    Assert.assertArrayEquals(expectedOpenConf, bufArray);
}
