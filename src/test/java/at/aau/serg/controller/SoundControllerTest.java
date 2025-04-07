
package at.aau.serg.controller;
import at.aau.serg.websocketdemoserver.controller.SoundController;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class SoundControllerTest {

    @Test
    void testGetAvailableSounds() {
        SoundController controller = new SoundController();

        List<String> sounds = controller.getAvailableSounds();

        assertNotNull(sounds);
        assertEquals(4, sounds.size());
        assertTrue(sounds.contains("Classic"));
        assertTrue(sounds.contains("Chime"));
        assertTrue(sounds.contains("Beep"));
        assertTrue(sounds.contains("Nature"));
    }

    @Test
    void testSetUserSound() {
        SoundController controller = new SoundController();

        String result = controller.setUserSound("Chime");

        assertEquals("Sound gespeichert: Chime", result);
    }

    @Test
    void testSetUserSoundInvalid() {
        SoundController controller = new SoundController();

        String result = controller.setUserSound("NonExistingSound");

        assertEquals("Fehler: Sound nicht verfügbar.", result);
    }
}

