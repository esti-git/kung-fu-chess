import org.java_websocket.WebSocket;
import protocol.StateCodec;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class FakeWebSocket {

    private final List<String> sentMessages = new ArrayList<>();
    private boolean closed = false;
    private final WebSocket socket;

    FakeWebSocket() {
        this.socket = (WebSocket) Proxy.newProxyInstance(
                WebSocket.class.getClassLoader(),
                new Class<?>[] { WebSocket.class },
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        case "equals":
                            return proxy == args[0];
                        case "toString":
                            return "FakeWebSocket@" + System.identityHashCode(proxy);
                        case "send":
                            if (args != null && args.length == 1 && args[0] instanceof String) {
                                sentMessages.add((String) args[0]);
                            }
                            return null;
                        case "close":
                        case "closeConnection":
                            closed = true;
                            return null;
                        case "isOpen":
                            return !closed;
                        case "isClosed":
                            return closed;
                    }
                    Class<?> returnType = method.getReturnType();
                    if (returnType == boolean.class) return false;
                    if (returnType == int.class) return 0;
                    if (returnType == long.class) return 0L;
                    return null;
                });
    }

    WebSocket socket() {
        return socket;
    }

    List<String> sentMessages() {
        return Collections.unmodifiableList(sentMessages);
    }

    List<String> messagesOfType(String type) {
        List<String> result = new ArrayList<>();
        for (String message : sentMessages) {
            if (type.equals(StateCodec.peekType(message))) {
                result.add(message);
            }
        }
        return result;
    }

    boolean hasMessageOfType(String type) {
        return !messagesOfType(type).isEmpty();
    }

    boolean isClosed() {
        return closed;
    }
}
