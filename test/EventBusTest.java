import events.Event;
import events.EventBus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class EventBusTest {

    private static class TestEvent extends Event {
        TestEvent(String type) {
            super(type);
        }
    }

    @Test
    void testPublishDeliversToSubscriber() {
        EventBus bus = new EventBus();
        List<Event> received = new ArrayList<>();

        bus.subscribe("TEST", received::add);
        TestEvent event = new TestEvent("TEST");
        bus.publish(event);

        assertEquals(1, received.size());
        assertSame(event, received.get(0));
    }

    @Test
    void testPublishWithNoSubscribersIsSafeNoOp() {
        EventBus bus = new EventBus();
        assertDoesNotThrow(() -> bus.publish(new TestEvent("NOBODY_LISTENING")));
    }

    @Test
    void testMultipleListenersAllReceiveTheEvent() {
        EventBus bus = new EventBus();
        List<String> calls = new ArrayList<>();

        bus.subscribe("TEST", event -> calls.add("first"));
        bus.subscribe("TEST", event -> calls.add("second"));
        bus.publish(new TestEvent("TEST"));

        assertEquals(List.of("first", "second"), calls);
    }

    @Test
    void testListenerOnlyReceivesItsOwnEventType() {
        EventBus bus = new EventBus();
        List<Event> received = new ArrayList<>();

        bus.subscribe("TYPE_A", received::add);
        bus.publish(new TestEvent("TYPE_B"));

        assertTrue(received.isEmpty());
    }

    @Test
    void testUnsubscribeStopsFurtherDelivery() {
        EventBus bus = new EventBus();
        List<Event> received = new ArrayList<>();
        Consumer<Event> listener = received::add;

        bus.subscribe("TEST", listener);
        bus.publish(new TestEvent("TEST"));
        bus.unsubscribe("TEST", listener);
        bus.publish(new TestEvent("TEST"));

        assertEquals(1, received.size());
    }

    @Test
    void testUnsubscribeUnknownListenerIsSafeNoOp() {
        EventBus bus = new EventBus();
        assertDoesNotThrow(() -> bus.unsubscribe("TEST", event -> { }));
    }
}
