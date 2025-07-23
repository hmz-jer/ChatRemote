 /**
 * Version optimisée du BerDecoderHandler pour Netty 4.1.14
 * Optimisations principales :
 * 1. Élimination de msg.copy() (gain majeur de performance)
 * 2. Gestion optimisée des ByteBuf
 * 3. Validation rapide des données BER
 * 4. Pool d'objets pour réutilisation (optionnel)
 * 
 * @author Seb Perriot
 * @version 1.0
 * @since 2015-09-14
 */
public class BerDecoderHandler extends ByteToMessageDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(BerDecoderHandler.class);
    
    // Pool optionnel pour réutiliser les instances BerDecoder
    private final Queue<BerDecoder> decoderPool = new ConcurrentLinkedQueue<>();
    private final AtomicInteger poolSize = new AtomicInteger(0);
    private static final int MAX_POOL_SIZE = 16;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Vérification rapide de la taille minimale BER (tag + length minimum)
        if (in.readableBytes() < 2) {
            return; // Pas assez de données, attendre plus
        }
        
        // Validation rapide du premier byte (tag BER)
        byte firstByte = in.getByte(in.readerIndex());
        if (!isValidBerTag(firstByte)) {
            LOG.warn("Tag BER invalide: 0x{}", Integer.toHexString(firstByte & 0xFF));
            ctx.close();
            return;
        }
        
        // Marquer la position pour pouvoir revenir en arrière si nécessaire
        in.markReaderIndex();
        
        try {
            // *** OPTIMISATION CRITIQUE ***
            // ANCIEN CODE (très coûteux) :
            // ByteBuf copy = msg.copy();
            // BerDecoder decoder = new BerDecoder(copy);
            
            // NOUVEAU CODE (optimisé) :
            // Passer directement le ByteBuf sans copie
            BerDecoder decoder = new BerDecoder(in);
            
            // Utiliser la méthode getMessage() existante
            BerMessage message = decoder.getMessage();
            
            if (message != null && !message.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Decoded BER message with {} fields", message.size());
                }
                out.add(message);
            } else {
                // Message incomplet ou vide, revenir à la position marquée
                in.resetReaderIndex();
            }
            
        } catch (IndexOutOfBoundsException e) {
            // Pas assez de données disponibles
            in.resetReaderIndex();
            // Ne pas logger en debug pour éviter le spam
        } catch (IOException e) {
            // Erreur de décodage BER (données corrompues ou incomplètes)
            in.resetReaderIndex();
            if (e.getMessage().contains("Can't read next field")) {
                // Données incomplètes, attendre plus de données
                return;
            } else {
                LOG.warn("Erreur de décodage BER, fermeture connexion: {}", e.getMessage());
                ctx.close();
            }
        } catch (Exception e) {
            // Autres erreurs
            in.resetReaderIndex();
            LOG.error("Erreur inattendue dans le décodage BER", e);
            ctx.close();
        }
    }
    
    /**
     * Version alternative avec pool d'objets pour réutilisation
     * (à utiliser si vous modifiez BerDecoder pour supporter la réinitialisation)
     */
    protected void decodeWithPool(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 2) {
            return;
        }
        
        // Validation rapide
        byte firstByte = in.getByte(in.readerIndex());
        if (!isValidBerTag(firstByte)) {
            ctx.close();
            return;
        }
        
        in.markReaderIndex();
        BerDecoder decoder = borrowDecoder();
        
        try {
            // Si BerDecoder supportait la réinitialisation :
            // decoder.reset();
            // decoder.setBuf(in);
            
            // Pour l'instant, utiliser le constructeur :
            decoder = new BerDecoder(in);
            
            BerMessage message = decoder.getMessage();
            
            if (message != null && !message.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Decoded BER message");
                }
                out.add(message);
            } else {
                in.resetReaderIndex();
            }
            
        } catch (Exception e) {
            in.resetReaderIndex();
            handleDecodingException(ctx, e);
        } finally {
            returnDecoder(decoder);
        }
    }
    
    /**
     * Version avec optimisations avancées de lecture
     */
    protected void decodeOptimized(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Vérification minimale
        if (in.readableBytes() < 2) {
            return;
        }
        
        // Peek rapide pour vérifier la validité sans avancer le reader
        int readerIndex = in.readerIndex();
        byte tag = in.getByte(readerIndex);
        
        if (!isValidBerTag(tag)) {
            LOG.warn("Tag BER invalide, fermeture connexion");
            ctx.close();
            return;
        }
        
        // Estimation rapide de la longueur du message pour éviter les copies partielles
        int estimatedLength = estimateMessageLength(in, readerIndex);
        if (estimatedLength > 0 && in.readableBytes() < estimatedLength) {
            return; // Attendre plus de données
        }
        
        in.markReaderIndex();
        
        try {
            // Créer un slice pour éviter les modifications du ByteBuf principal
            // (optionnel selon votre cas d'usage)
            ByteBuf messageSlice = in.slice();
            BerDecoder decoder = new BerDecoder(messageSlice);
            
            BerMessage message = decoder.getMessage();
            
            if (message != null && !message.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Decoded BER message from {} bytes", 
                             in.readerIndex() - readerIndex);
                }
                out.add(message);
            } else {
                in.resetReaderIndex();
            }
            
        } catch (Exception e) {
            in.resetReaderIndex();
            handleDecodingException(ctx, e);
        }
    }
    
    /**
     * Pool d'objets simple pour réutiliser les BerDecoder
     */
    private BerDecoder borrowDecoder() {
        BerDecoder decoder = decoderPool.poll();
        if (decoder != null) {
            poolSize.decrementAndGet();
            return decoder;
        }
        return new BerDecoder();
    }
    
    private void returnDecoder(BerDecoder decoder) {
        if (decoder != null && poolSize.get() < MAX_POOL_SIZE) {
            // Si BerDecoder avait une méthode reset() :
            // try {
            //     decoder.reset();
            //     decoderPool.offer(decoder);
            //     poolSize.incrementAndGet();
            // } catch (Exception e) {
            //     // Ne pas remettre dans le pool si reset échoue
            // }
        }
    }
    
    /**
     * Validation rapide du tag BER
     */
    private boolean isValidBerTag(byte tag) {
        // Validation basique selon le protocole BER
        // Adaptez selon vos tags spécifiques
        
        // Vérifier que ce n'est pas un tag étendu (pour simplifier)
        if ((tag & 0x1F) == 0x1F) {
            return false; // Tags multi-octets - traitement plus complexe
        }
        
        // Vérifier les classes valides (Universal, Application, Context, Private)
        // Tous sont valides en BER, donc pas de restriction ici
        
        return true; // Tag valide
    }
    
    /**
     * Estimation rapide de la longueur du message pour optimiser les lectures
     */
    private int estimateMessageLength(ByteBuf in, int startIndex) {
        if (in.readableBytes() < 2) {
            return -1;
        }
        
        try {
            int index = startIndex + 1; // Skip tag
            byte lengthByte = in.getByte(index);
            
            // Forme courte
            if ((lengthByte & 0x80) == 0) {
                return 2 + (lengthByte & 0x7F); // tag + length + content
            }
            
            // Forme longue
            int numOctets = lengthByte & 0x7F;
            if (numOctets == 0 || numOctets > 4 || in.readableBytes() < (2 + numOctets)) {
                return -1;
            }
            
            int contentLength = 0;
            for (int i = 0; i < numOctets; i++) {
                contentLength = (contentLength << 8) | (in.getUnsignedByte(index + 1 + i));
            }
            
            return 1 + 1 + numOctets + contentLength; // tag + length_indicator + length_octets + content
            
        } catch (Exception e) {
            return -1; // Erreur d'estimation
        }
    }
    
    /**
     * Gestion centralisée des erreurs de décodage
     */
    private void handleDecodingException(ChannelHandlerContext ctx, Exception e) {
        if (e instanceof IOException) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("Can't read next field") || 
                               msg.contains("Pas assez") ||
                               msg.contains("not enough"))) {
                // Données incomplètes - comportement normal
                return;
            }
        }
        
        if (LOG.isWarnEnabled()) {
            LOG.warn("Erreur de décodage BER sur {}: {}", 
                     ctx.channel().remoteAddress(), e.getMessage());
        }
        
        // Fermer la connexion en cas d'erreur de décodage grave
        ctx.close();
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Nettoyer le pool si utilisé
        decoderPool.clear();
        poolSize.set(0);
        super.channelInactive(ctx);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (LOG.isErrorEnabled()) {
            LOG.error("Exception dans BerDecoderHandler pour {}", 
                     ctx.channel().remoteAddress(), cause);
        }
        ctx.close();
    }
    
    /**
     * Méthode utilitaire pour debugging et monitoring
     */
    public void logStats() {
        if (LOG.isInfoEnabled()) {
            LOG.info("BerDecoderHandler stats - Pool size: {}", poolSize.get());
        }
    }
}
