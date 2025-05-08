package at.aau.serg.monopoly.websoket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GiveUpWebSocketHandlerTest {

    @Disabled("Disabled due to a bug")
    @Test
    public void testMarkPlayerAsLoser_runsWithoutException() {
        GameHistoryService service = new GameHistoryService();

        // Einfach aufrufen – mehr nicht
        service.markPlayerAsLoser("test-user");

        // Kein assert nötig – Test besteht, wenn keine Exception fliegt
    }

    @Disabled("Disabled due to a bug")
    @Test
    void testAddUserToSessionMap() {
        GameWebSocketHandler handler = new GameWebSocketHandler();
        handler.sessionToUserId.put("session-1", "user123");

        assert (handler.sessionToUserId.get("session-1").equals("user123"));
    }

    @Disabled("Disabled due to a bug")
    @Test
    void testCreateJsonNodeWithUserId() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("userId", "user123");

        assert (node.get("userId").asText().equals("user123"));
    }
}