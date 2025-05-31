package at.aau.serg.monopoly.websoket;

import data.deals.DealProposalMessage;
import data.deals.DealResponseMessage;
import data.deals.DealResponseType;
import data.deals.CounterProposalMessage;
import model.Game;
import model.Player;
import model.properties.BaseProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class DealServiceTest {

    private DealService dealService;
    private PropertyTransactionService propertyTransactionService;
    private Game game;
    private Player fromPlayer;
    private Player toPlayer;
    private BaseProperty property;

    @BeforeEach
    void setUp() {
        propertyTransactionService = mock(PropertyTransactionService.class);
        dealService = new DealService(propertyTransactionService);

        game = mock(Game.class);
        fromPlayer = mock(Player.class);
        toPlayer = mock(Player.class);
        property = mock(BaseProperty.class);

        dealService.setGame(game);
    }

    @Test
    void testExecuteTrade_gameNotSet_doesNothing() {
        DealService localService = new DealService(propertyTransactionService);
        DealResponseMessage msg = new DealResponseMessage("DEAL_RESPONSE", "from", "to", DealResponseType.ACCEPT, List.of(1), 100);
        assertDoesNotThrow(() -> localService.executeTrade(msg));
        verifyNoInteractions(propertyTransactionService);
    }

    @Test
    void testExecuteTrade_playersNotFound_doesNothing() {
        when(game.getPlayerById("from")).thenReturn(Optional.empty());
        when(game.getPlayerById("to")).thenReturn(Optional.of(toPlayer));

        DealResponseMessage msg = new DealResponseMessage("DEAL_RESPONSE", "from", "to", DealResponseType.ACCEPT, List.of(1), 100);
        assertDoesNotThrow(() -> dealService.executeTrade(msg));
        verify(propertyTransactionService, never()).findPropertyById(anyInt());
    }

    @Test
    void testExecuteTrade_propertyTransferred_andMoneyExchanged() {
        // Arrange
        when(game.getPlayerById("from")).thenReturn(Optional.of(fromPlayer));
        when(game.getPlayerById("to")).thenReturn(Optional.of(toPlayer));
        when(propertyTransactionService.findPropertyById(1)).thenReturn(property);
        when(fromPlayer.getId()).thenReturn("from");
        when(fromPlayer.getMoney()).thenReturn(200);
        when(toPlayer.getId()).thenReturn("to");
        when(property.getOwnerId()).thenReturn("from");
        when(property.getName()).thenReturn("Teststraße");

        DealProposalMessage proposal = new DealProposalMessage(
                "DEAL_PROPOSAL",
                "from",
                "to",
                List.of(),      // requested
                List.of(1),     // offered
                100             // money
        );
        dealService.saveProposal(proposal);

        DealResponseMessage msg = new DealResponseMessage();
        msg.setType("DEAL_RESPONSE");
        msg.setFromPlayerId("from");
        msg.setToPlayerId("to");
        msg.setResponseType(DealResponseType.ACCEPT);
        msg.setCounterPropertyIds(List.of(1));
        msg.setCounterMoney(100);

        // Act
        dealService.executeTrade(msg);

        // Assert
        verify(property).setOwnerId("to");
        verify(fromPlayer).subtractMoney(100);
        verify(toPlayer).addMoney(100);
    }


    @Test
    void testExecuteTrade_noMoneyTransferred_ifZero() {
        // Arrange
        when(game.getPlayerById("from")).thenReturn(Optional.of(fromPlayer));
        when(game.getPlayerById("to")).thenReturn(Optional.of(toPlayer));
        when(propertyTransactionService.findPropertyById(1)).thenReturn(property);
        when(fromPlayer.getId()).thenReturn("from");
        when(toPlayer.getId()).thenReturn("to");
        when(property.getOwnerId()).thenReturn("from");
        when(property.getName()).thenReturn("Teststraße");

        DealProposalMessage proposal = new DealProposalMessage(
                "DEAL_PROPOSAL",
                "from",
                "to",
                List.of(),      // requested
                List.of(1),     // offered
                0               // money
        );
        dealService.saveProposal(proposal);

        DealResponseMessage msg = new DealResponseMessage();
        msg.setType("DEAL_RESPONSE");
        msg.setFromPlayerId("from");
        msg.setToPlayerId("to");
        msg.setResponseType(DealResponseType.ACCEPT);
        msg.setCounterPropertyIds(List.of(1));
        msg.setCounterMoney(0);

        // Act
        dealService.executeTrade(msg);

        // Assert
        verify(property).setOwnerId("to");
        verify(fromPlayer, never()).subtractMoney(anyInt());
        verify(toPlayer, never()).addMoney(anyInt());
    }


    @Test
    void testExecuteTrade_ignoresPropertyIfNotFound() {
        when(game.getPlayerById("from")).thenReturn(Optional.of(fromPlayer));
        when(game.getPlayerById("to")).thenReturn(Optional.of(toPlayer));
        when(propertyTransactionService.findPropertyById(1)).thenReturn(null);

        DealResponseMessage msg = new DealResponseMessage("DEAL_RESPONSE", "from", "to", DealResponseType.ACCEPT, List.of(1), 0);

        dealService.executeTrade(msg);
        // Should skip property, but still work
    }

    @Test
    void testExecuteTrade_responseTypeNotAccept_doesNothing() {
        when(game.getPlayerById("from")).thenReturn(Optional.of(fromPlayer));
        when(game.getPlayerById("to")).thenReturn(Optional.of(toPlayer));

        DealResponseMessage msg = new DealResponseMessage(
                "DEAL_RESPONSE", "from", "to", DealResponseType.DECLINE, List.of(1), 100
        );

        dealService.executeTrade(msg);

        // keine Transfers, keine Eigentumsänderung
        verifyNoInteractions(propertyTransactionService);
        verify(fromPlayer, never()).subtractMoney(anyInt());
        verify(toPlayer, never()).addMoney(anyInt());
    }
    @Test
    void testExecuteTrade_withCounterOffer_succeeds() {
        // Arrange
        when(game.getPlayerById("from")).thenReturn(Optional.of(fromPlayer));
        when(game.getPlayerById("to")).thenReturn(Optional.of(toPlayer));
        when(propertyTransactionService.findPropertyById(1)).thenReturn(property);
        when(fromPlayer.getId()).thenReturn("from");
        when(toPlayer.getId()).thenReturn("to");
        when(fromPlayer.getMoney()).thenReturn(1000);
        when(property.getOwnerId()).thenReturn("from");
        when(property.getName()).thenReturn("Teststraße");

        // Counter-Angebot: "from" bietet "to" die Straße + 100€
        CounterProposalMessage counter = new CounterProposalMessage(
                "from",       // von
                "to",         // an
                List.of(),    // requested (will nichts vom anderen)
                List.of(1),   // offered property
                100           // bietet 100€ Geld
        );
        dealService.saveCounterProposal(counter);

        DealResponseMessage response = new DealResponseMessage();
        response.setType("DEAL_RESPONSE");
        response.setFromPlayerId("to");  // der ursprüngliche Deal-Anbieter antwortet
        response.setToPlayerId("from");  // an den neuen Anbieter
        response.setResponseType(DealResponseType.ACCEPT);
        response.setCounterPropertyIds(List.of(1));
        response.setCounterMoney(100);

        // Act
        dealService.executeTrade(response);

        // Assert
        verify(property).setOwnerId("to");
        verify(fromPlayer).subtractMoney(100);
        verify(toPlayer).addMoney(100);
    }

}
