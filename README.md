# Kung Fu Chess

Real-time chess variant (pieces move asynchronously with cooldowns instead of turns) — Java + Swing.

## Requirements

- Java 17
- Maven

## Build

```
mvn compile
```

Run the tests:

```
mvn test
```

## Running — network mode (client/server)

All game logic runs on the server; each client is a thin display that connects over WebSocket.

1. Start the server first (`server.ServerMain`, listens on port `8887`):

   ```
   mvn compile exec:java -Dexec.mainClass=server.ServerMain
   ```

2. Start one client per player (`client.ClientMain`), each in its own terminal/process:

   ```
   mvn compile exec:java -Dexec.mainClass=client.ClientMain
   ```

   Each client prompts for a username and password on the console (a new username registers automatically). After logging in, use the **Play** button to get matched by rating, or **Room** to create/join a private room with another player.

   By default the client connects to `ws://localhost:8887`. To connect elsewhere, pass the URL as an argument:

   ```
   mvn compile exec:java -Dexec.mainClass=client.ClientMain -Dexec.args="ws://<host>:8887"
   ```

   > Note: `exec:java` requires the `exec-maven-plugin`. If it isn't configured in `pom.xml`, run `ServerMain` / `ClientMain` directly from your IDE instead (both are plain classes with a `main` method).

## Running — local mode (single process)

For a quick offline game on one machine (no server needed), run `LocalMain` directly from your IDE, or:

```
mvn compile
java -cp target/classes LocalMain
```
