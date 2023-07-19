import com.baasflow.events.service.EventSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SanityTest {
    private EventSender eventSender;

    @BeforeEach
    void setUp() {
        eventSender = new EventSender();
    }

    @Test
    void sanityTest() {
        eventSender.send();
    }
}
