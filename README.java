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

    // Création d'un buffer direct non sécurisé avec une capacité de 256 octets
    ByteBuf buf = Unpooled.directBuffer(256);

    // Écriture des données dans le buffer
    channel.writeInbound(Unpooled.wrappedBuffer(openREQ));
    
    ByteBuf message = (ByteBuf) channel.readOutbound();
    message.readBytes(buf);  // Lire dans le buffer direct avec la capacité ajustée

    // Extraction des données du buffer pour comparaison
    byte[] bufArray = new byte[buf.readableBytes()];
    buf.readBytes(bufArray);

    // Comparaison des tableaux d'octets
    Assert.assertArrayEquals(expectedOpenConf, bufArray);
}
