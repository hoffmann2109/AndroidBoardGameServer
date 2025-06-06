package model;

import at.aau.serg.monopoly.websoket.PropertyTransactionService;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Führt alle Bot-Züge in einem separaten Daemon-Thread aus.
 */
public class BotManager {

    /* ────────────────── Callback ────────────────── */

    /** Vom Handler bereitgestellte Funktionen, die der Bot aufrufen darf. */
    public interface BotCallback {
        void broadcast(String msg);          // Chat / Systemmeldung an alle
        void updateGameState();              // kompletten Spielstand pushen
        void advanceToNextPlayer();          // Zug an nächsten Spieler übergeben
    }

    /* ────────────────── Felder ────────────────── */

    private static final Logger log = Logger.getLogger(BotManager.class.getName());

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
        exec.scheduleWithFixedDelay(this::processTurn, 500, 500, TimeUnit.MILLISECONDS);
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
        exec.execute(() -> processSpecificBot(botId));
    }

    /* ────────────────── Kernlogik ────────────────── */

    private void processTurn() {
        // 1) Spiel läuft überhaupt?
        if (!game.isStarted()) return;

        // 2) Lock versuchen (nicht blockierend)
        if (!game.getTurnLock().tryLock()) return;
        try {
            Player cur = game.getCurrentPlayer();
            if (cur == null || !cur.isBot()) return;

            doFullMove(cur);
        } finally {
            game.getTurnLock().unlock();
        }
    }

    /** Führt einen Zug speziell für den Bot mit <code>botId</code> aus. */
    private void processSpecificBot(String botId) {
        if (!game.getTurnLock().tryLock()) return;
        try {
            Player p = game.getPlayerById(botId).orElse(null);
            if (p != null && p.isBot()) {
                doFullMove(p);
            }
        } finally {
            game.getTurnLock().unlock();
        }
    }

    /** Ein *vollständiger* Bot-Zug (würfeln, ziehen, kaufen, evtl. Ende). */
    private void doFullMove(Player bot) {

        log.info(() -> "Bot-Turn für " + bot.getName());

        DiceManagerInterface dm = game.getDiceManager();
        int roll      = dm.rollDices();
        boolean pasch = dm.isPasch();

        cb.broadcast("BOT_ROLL:" + bot.getId() + ":" + dm.getLastRollValues());
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
        cb.updateGameState();      // Position + evtl. Besitz anzeigen

        // Pasch → noch einmal; sonst Zugende
        if (!pasch) {
            game.nextPlayer();
            cb.advanceToNextPlayer();
        } else {
            // Für den zweiten Wurf freigeben
            bot.setHasRolledThisTurn(false);
        }
    }

    /** Prüft, ob kaufbar, kauft und meldet das. */
    private void tryBuyCurrentField(Player bot) {
        int pos = bot.getPosition();

        if (pts.canBuyProperty(bot, pos) && pts.buyProperty(bot, pos)) {
            cb.broadcast("SYSTEM: " + bot.getName() + " bought property " + pos);
            cb.updateGameState();
            log.info(() -> "Bot kauft Feld " + pos);
        }
    }
}
