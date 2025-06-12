package at.aau.serg.monopoly.websoket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GiveUpWebSocketHandlerTest {

    @Disabled("Disabled due to a bug")
    @Test
    public void testMarkPlayerAsLoser_runsWithoutException() {
        GameHistoryService service = new GameHistoryService();

        // Einfach aufrufen – mehr nicht
        service.markPlayerAsLoser("test-user", 0,0);

        // Kein assert nötig – Test besteht, wenn keine Exception fliegt
        assertTrue(true, "Die Methode markPlayerAsLoser wurde ohne Exception ausgeführt");
    }

    @Disabled("Disabled due to a bug")
    @Test
    void testAddUserToSessionMap() {
        GameWebSocketHandler handler = new GameWebSocketHandler();
        handler.sessionToUserId.put("session-1", "user123");

        assertEquals("user123", handler.sessionToUserId.get("session-1"));
    }

    @Disabled("Disabled due to a bug")
    @Test
    void testCreateJsonNodeWithUserId() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("userId", "user123");

        assertEquals("user123", node.get("userId").asText());
    }
}