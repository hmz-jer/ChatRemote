 /**
 * Version optimisée du BerDecoderHandler pour Netty 4.1.14
 * @author Seb Perriot
 * @version 1.0
 * @since 2015-09-14
 */
public class BerDecoderHandler extends ByteToMessageDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(BerDecoderHandler.class);
    
    // Réutilisation d'instance pour éviter les allocations
    private final ThreadLocal<BerDecoder> decoderThreadLocal = 
        ThreadLocal.withInitial(() -> new BerDecoder());

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Vérification préliminaire de la taille minimale
        if (in.readableBytes() < getMinimumBerMessageSize()) {
            return; // Pas assez de données, attendre
        }
        
        // Marquer la position actuelle pour pouvoir revenir en arrière
        in.markReaderIndex();
        
        try {
            // Utiliser l'instance thread-local au lieu de créer un nouveau decoder
            BerDecoder decoder = decoderThreadLocal.get();
            
            // Option 1: Si BerDecoder peut accepter ByteBuf directement (optimal)
            BerMessage message = decoder.decode(in);
            
            if (message != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Decoded BER message of type: {}", 
                             message.getClass().getSimpleName());
                }
                out.add(message);
            } else {
                // Message incomplet, revenir à la position marquée
                in.resetReaderIndex();
            }
            
        } catch (IndexOutOfBoundsException e) {
            // Pas assez de données disponibles
            in.resetReaderIndex();
        } catch (Exception e) {
            // Erreur de décodage, revenir à la position marquée
            in.resetReaderIndex();
            throw e;
        }
    }
    
    /**
     * Alternative si BerDecoder nécessite un byte array
     * (moins optimal mais compatible)
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < getMinimumBerMessageSize()) {
            return;
        }
        
        in.markReaderIndex();
        
        try {
            // Lire sans copier en utilisant un slice
            ByteBuf slice = in.readSlice(in.readableBytes());
            
            // Si BerDecoder nécessite absolument un byte array
            if (slice.hasArray() && slice.arrayOffset() == 0) {
                // Utiliser directement le backing array si possible
                byte[] array = slice.array();
                BerDecoder decoder = decoderThreadLocal.get();
                BerMessage message = decoder.decode(array, 0, slice.readableBytes());
                
                if (message != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Decoded BER message");
                    }
                    out.add(message);
                    // Avancer le reader index du nombre d'octets consommés
                    in.readerIndex(in.readerIndex() + decoder.getConsumedBytes());
                } else {
                    in.resetReaderIndex();
                }
            } else {
                // Fallback: copie minimale nécessaire
                byte[] bytes = new byte[slice.readableBytes()];
                slice.readBytes(bytes);
                
                BerDecoder decoder = decoderThreadLocal.get();
                BerMessage message = decoder.decode(bytes);
                
                if (message != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Decoded BER message");
                    }
                    out.add(message);
                } else {
                    in.resetReaderIndex();
                }
            }
            
        } catch (Exception e) {
            in.resetReaderIndex();
            throw e;
        }
    }
    
    /**
     * Version avec pool d'objets simple pour Netty 4.1.14
     */
    private static final Queue<BerDecoder> DECODER_POOL = new ConcurrentLinkedQueue<>();
    private static final int MAX_POOL_SIZE = 32;
    
    private BerDecoder borrowDecoder() {
        BerDecoder decoder = DECODER_POOL.poll();
        return decoder != null ? decoder : new BerDecoder();
    }
    
    private void returnDecoder(BerDecoder decoder) {
        if (DECODER_POOL.size() < MAX_POOL_SIZE) {
            decoder.reset(); // Supposant qu'il y a une méthode reset
            DECODER_POOL.offer(decoder);
        }
    }
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < getMinimumBerMessageSize()) {
            return;
        }
        
        in.markReaderIndex();
        BerDecoder decoder = borrowDecoder();
        
        try {
            // Éviter msg.copy() de votre code original
            BerMessage message;
            
            // Méthode optimisée selon l'API de votre BerDecoder
            if (in.hasArray()) {
                // Utiliser directement le backing array
                byte[] array = in.array();
                int offset = in.arrayOffset() + in.readerIndex();
                int length = in.readableBytes();
                message = decoder.decode(array, offset, length);
            } else {
                // Pour les ByteBuf directs, une copie est nécessaire
                byte[] bytes = new byte[in.readableBytes()];
                in.getBytes(in.readerIndex(), bytes); // getBytes ne modifie pas readerIndex
                message = decoder.decode(bytes);
            }
            
            if (message != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Decoded BER message: {}", 
                             ByteBufUtil.hexDump(in, in.readerIndex(), 
                                               Math.min(16, in.readableBytes())));
                }
                out.add(message);
                // Avancer le reader index selon les octets consommés
                in.readerIndex(in.readerIndex() + decoder.getBytesConsumed());
            } else {
                in.resetReaderIndex();
            }
            
        } catch (Exception e) {
            in.resetReaderIndex();
            throw e;
        } finally {
            returnDecoder(decoder);
        }
    }
    
    private int getMinimumBerMessageSize() {
        // Taille minimale d'un message BER (tag + length + value)
        // Ajustez selon votre protocole spécifique
        return 2;
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Nettoyage des resources thread-local si nécessaire
        decoderThreadLocal.remove();
        super.channelInactive(ctx);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (LOG.isErrorEnabled()) {
            LOG.error("Erreur de décodage BER sur canal {}", ctx.channel(), cause);
        }
        ctx.close();
    }
}
