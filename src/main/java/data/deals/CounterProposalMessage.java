package data.deals;

import java.util.List;

public class CounterProposalMessage extends DealProposalMessage {
    public CounterProposalMessage() {
        super();
        this.setType("COUNTER_OFFER");
    }

    public CounterProposalMessage(String fromPlayerId, String toPlayerId, List<Integer> requested, List<Integer> offered, int money) {
        super("COUNTER_OFFER", fromPlayerId, toPlayerId, requested, offered, money);
    }
}

