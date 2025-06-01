package at.aau.serg.monopoly.websoket;

import data.deals.DealProposalMessage;
import data.deals.DealResponseMessage;
import data.deals.CounterProposalMessage;
import lombok.Setter;
import model.Game;
import model.Player;
import model.properties.BaseProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Service
public class DealService {

    private static final Logger logger = Logger.getLogger(DealService.class.getName());

    private final PropertyTransactionService propertyTransactionService;
    @Setter
    private Game game;

    private final Map<String, DealProposalMessage> pendingDeals = new ConcurrentHashMap<>();

    public DealService(PropertyTransactionService propertyTransactionService) {
        this.propertyTransactionService = propertyTransactionService;
    }

    public void saveProposal(DealProposalMessage deal) {
        pendingDeals.put(deal.getToPlayerId(), deal);
        logger.info("DealProposal saved for receiver " + deal.getToPlayerId());
    }

    public void saveCounterProposal(CounterProposalMessage counter) {
        pendingDeals.put(counter.getToPlayerId(), counter);
        logger.info("CounterProposal saved for receiver " + counter.getToPlayerId());
    }

    public void removeProposal(String playerId) {
        pendingDeals.remove(playerId);
        logger.info("DealProposal removed for " + playerId);
    }

    private DealProposalMessage getPendingDeal(DealResponseMessage response) {
        DealProposalMessage proposal = pendingDeals.get(response.getToPlayerId());
        if (proposal == null) {
            proposal = pendingDeals.get(response.getFromPlayerId());
        }
        return proposal;
    }

    public void executeTrade(DealResponseMessage response) {
        if (game == null) {
            logger.warning("Game instance is not set in DealService.");
            return;
        }

        DealProposalMessage proposal = getPendingDeal(response);
        if (proposal == null) {
            logger.warning("No saved deal for response from " + response.getFromPlayerId() + " to " + response.getToPlayerId());
            return;
        }

        Player sender = game.getPlayerById(proposal.getFromPlayerId()).orElse(null);
        Player receiver = game.getPlayerById(proposal.getToPlayerId()).orElse(null);
        if (sender == null || receiver == null) {
            logger.warning("Sender or receiver not found");
            return;
        }

        // Eigentum vom Sender → Empfänger
        for (int propId : proposal.getOfferedPropertyIds()) {
            BaseProperty prop = propertyTransactionService.findPropertyById(propId);
            if (prop != null && sender.getId().equals(prop.getOwnerId())) {
                prop.setOwnerId(receiver.getId());
                logger.info(prop.getName() + " from " + sender.getName() + " → " + receiver.getName());
            }
        }

        // Eigentum vom Empfänger → Sender
        for (int propId : proposal.getRequestedPropertyIds()) {
            BaseProperty prop = propertyTransactionService.findPropertyById(propId);
            if (prop != null && receiver.getId().equals(prop.getOwnerId())) {
                prop.setOwnerId(sender.getId());
                logger.info(prop.getName() + " from " + receiver.getName() + " → " + sender.getName());
            }
        }

        // Geld vom Sender → Empfänger
        int money = proposal.getOfferedMoney();
        if (money > 0 && sender.getMoney() >= money) {
            sender.subtractMoney(money);
            receiver.addMoney(money);
            logger.info(money + " € from " + sender.getName() + " → " + receiver.getName());
        }

        // Vorschlag löschen
        removeProposal(response.getToPlayerId());
        removeProposal(response.getFromPlayerId());

        logger.info("Trade executed between " + sender.getName() + " and " + receiver.getName());
    }
}
