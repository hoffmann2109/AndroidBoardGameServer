package model;

import at.aau.serg.monopoly.websoket.PropertyTransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.properties.BaseProperty;

import java.util.concurrent.*;
import java.util.logging.Logger;




public class BotManager {



    /** Vom Handler bereitgestellte Funktionen, die der Bot aufrufen darf. */
    public interface BotCallback {
        void broadcast(String msg);          // Chat / Systemmeldung an alle
        void updateGameState();              // kompletten Spielstand pushen
        void advanceToNextPlayer();          // Zug an nächsten Spieler übergeben
        void checkBankruptcy();
    }

    /* ────────────────── Felder ────────────────── */

    private static final Logger log = Logger.getLogger(BotManager.class.getName());
    private static final long BOT_DELAY_SEC = 3;
    private final ObjectMapper mapper = new ObjectMapper();

    private final Game game;
    private final PropertyTransactionService pts;
    private final BotCallback cb;

    private final ScheduledExecutorService exec =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "BotThread");
                t.setDaemon(true);
                return t;
            });

    /* ────────────────── Konstruktor ────────────────── */

    public BotManager(Game game,
                      PropertyTransactionService pts,
                      BotCallback cb) {
        this.game = game;
        this.pts  = pts;
        this.cb   = cb;
    }

    /* ────────────────── Lebenszyklus ────────────────── */

    /** startet die Dauerschleife (einmal nach Game-Start aufrufen) */
    public void start() {
        exec.scheduleWithFixedDelay(this::processTurn, BOT_DELAY_SEC, BOT_DELAY_SEC, TimeUnit.SECONDS);
    }

    /** sofort beenden (z.B. wenn das Spiel endet) */
    public void shutdown() {
        exec.shutdownNow();
    }

    /* ────────────────── Externe Trigger ────────────────── */

    /**
     * Wird vom Handler aufgerufen, wenn *nach* einem Bot-Zug
     * direkt der nächste Bot dran ist → sofort verarbeiten.
     */
    public void queueBotTurn(String botId) {
        exec.schedule(
                () -> processSpecificBot(botId),
                BOT_DELAY_SEC,
                TimeUnit.SECONDS);
    }



    private void processTurn() {
        // 1) Spiel läuft überhaupt?
        if (!game.isStarted()) return;

        // 2) Lock versuchen (nicht blockierend)
        if (!game.getTurnLock().tryLock()) return;
        try {
            Player cur = game.getCurrentPlayer();
            if (cur == null || !cur.isBot()) return;

            doFullMove(cur);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            game.getTurnLock().unlock();
        }
    }


    private void processSpecificBot(String botId) {
        if (!game.getTurnLock().tryLock()) return;
        try {
            Player p = game.getPlayerById(botId).orElse(null);
            if (p != null && p.isBot()) {
                doFullMove(p);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            game.getTurnLock().unlock();
        }
    }

    /** Ein *vollständiger* Bot-Zug (würfeln, ziehen, kaufen, evtl. Ende). */
    private void doFullMove(Player bot) throws JsonProcessingException {

        log.info(() -> "Bot-Turn für " + bot.getName());


        DiceManagerInterface dm = game.getDiceManager();
        int roll      = dm.rollDices();
        boolean pasch = dm.isPasch();

        // BotManager.doFullMove (alt)
        cb.broadcast("BOT_ROLL:" + bot.getId() + ":" + dm.getLastRollValues());


        // BotManager.doFullMove (neu)
        ObjectNode rollMsg = mapper.createObjectNode();
        rollMsg.put("type",       "DICE_ROLL");
        rollMsg.put("playerId",   bot.getId());
        rollMsg.put("value",      roll);
        rollMsg.put("manual",     false);
        rollMsg.put("isPasch",    pasch);
        cb.broadcast(mapper.writeValueAsString(rollMsg));

        log.info(() -> " → Würfel: " + dm.getLastRollValues() + (pasch ? " (Pasch)" : ""));

        // Position + 200 € bei Los
        boolean passedGo = game.updatePlayerPosition(roll, bot.getId());
        if (passedGo) {
            cb.broadcast("SYSTEM: " + bot.getName() + " passed GO and collected €200");
        }

        // Kaufen, falls möglich
        tryBuyCurrentField(bot);

        // Würfeln fertig
        bot.setHasRolledThisTurn(true);
        cb.updateGameState();// Position + evtl. Besitz anzeigen
        cb.checkBankruptcy();


        // Pasch → noch einmal; sonst Zugende
        if (!pasch) {
            game.nextPlayer();
            cb.updateGameState();

        } else {
            // Für den zweiten Wurf freigeben
            bot.setHasRolledThisTurn(false);
            cb.updateGameState();
        }
    }

    /** Prüft, ob kaufbar, kauft und meldet das. */
    private void tryBuyCurrentField(Player bot) {

        BaseProperty field =
                pts.findPropertyByPosition(bot.getPosition());
        if (field == null || field.getOwnerId() != null) {
            return;                     // nichts zu kaufen
        }

        // 1) Kann/Möchte der Bot kaufen?
        if (!pts.canBuyProperty(bot, field.getId())) {
            return;
        }

        // 2) Kaufen
        boolean bought = pts.buyProperty(bot, field.getId());
        if (!bought) {
            return;                     // Sicherheits-Exit
        }

  // 3. Nachricht an alle schicken
        try {
            ObjectNode msg = mapper.createObjectNode();
            msg.put("type", "PROPERTY_BOUGHT");
            msg.put(
                    "message",
                    "Player " + bot.getName() + " 🤖 bought property " + field.getName()
            );
            cb.broadcast(mapper.writeValueAsString(msg));
        } catch (Exception e) {
            log.severe("Could not broadcast bot purchase");
        }

        // 4) Spielstand sofort aktualisieren
        cb.updateGameState();
    }
}
