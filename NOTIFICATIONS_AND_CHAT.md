# Real-time Notifikácie a Chat - Dokumentácia

## 1. Real-time Notifikácie (WebSocket)

### Ako to funguje

Aplikácia používa **WebSocket** pre okamžité informovanie používateľov o dôležitých udalostiach v reálnom čase.

#### Server-side implementácia

**Súbory:**
- `server/src/main/java/sk/ikts/server/service/NotificationService.java` - služba pre posielanie notifikácií
- `server/src/main/java/sk/ikts/server/config/SimpleWebSocketHandler.java` - WebSocket handler pre broadcast notifikácií
- `server/src/main/java/sk/ikts/server/config/WebSocketConfig.java` - konfigurácia WebSocket endpointov

**Ako to funguje:**
1. Server používa `SimpMessagingTemplate` (STOMP) a `SimpleWebSocketHandler` (plain WebSocket) pre posielanie notifikácií
2. Notifikácie sa posielajú cez WebSocket endpoint: `ws://127.0.0.1:8081/ws/simple`
3. Všetci pripojení klienti dostávajú notifikácie v reálnom čase

**Typy notifikácií:**
- `NEW_TASK` - nová úloha bola vytvorená
- `TASK_STATUS_CHANGED` - zmena statusu úlohy
- `NEW_MEMBER` - nový člen sa pridál do skupiny
- `NEW_GROUP` - nová skupina bola vytvorená
- `DEADLINE_REMINDER` - upozornenie na deadline (3 dni pred, každý deň)
- `DEADLINE_WARNING` - varovanie pred deadlinom (6 hodín)
- `DEADLINE_URGENT` - urgentné upozornenie (1 hodina)
- `DEADLINE_OVERDUE` - úloha je po deadlínu

**Príklady použitia:**
```java
// Notifikácia o novej úlohe
notificationService.notifyNewTask(groupId, taskId, taskTitle);

// Notifikácia o zmene statusu
notificationService.notifyTaskStatusChange(groupId, taskId, taskTitle, "DONE");

// Notifikácia o novom členovi
notificationService.notifyNewMember(groupId, memberName);
```

#### Client-side implementácia

**Súbory:**
- `client/src/main/java/sk/ikts/client/util/NotificationWebSocketClient.java` - WebSocket klient
- `client/src/main/java/sk/ikts/client/util/NotificationManager.java` - správca notifikácií
- `client/src/main/java/sk/ikts/client/model/Notification.java` - model notifikácie

**Ako to funguje:**
1. Klient sa pripojí k WebSocket serveru pri otvorení dashboardu
2. Prijaté notifikácie sa zobrazujú ako toast notifikácie v aplikácii
3. Notifikácie sa tiež zobrazujú v status labeli a spúšťajú automatické obnovenie dát

**Príklad pripojenia:**
```java
NotificationWebSocketClient webSocketClient = new NotificationWebSocketClient();
webSocketClient.connect(notification -> {
    // Spracovanie notifikácie
    NotificationManager.showInfo(notification.getMessage());
    // Obnovenie dát podľa typu notifikácie
});
```

### Deadline Reminders

**Súbor:** `server/src/main/java/sk/ikts/server/service/DeadlineReminderService.java`

**Ako to funguje:**
1. Service beží každú hodinu (`@Scheduled(fixedRate = 3600000)`)
2. Kontroluje úlohy s deadlinom do **3 dní** (72 hodín)
3. Posiela **jednu notifikáciu každý deň** pre každú úlohu s blížiacim sa deadlinom
4. Typy upozornení:
   - **1-3 dni pred deadlinom**: denné upozornenie
   - **6-24 hodín**: varovanie (raz denne)
   - **1-6 hodín**: varovanie (raz denne)
   - **Menej ako 1 hodina**: urgentné upozornenie (len raz)

**Príklad:**
- Úloha má deadline za 3 dni → používateľ dostane upozornenie dnes, zajtra a pozajtra (3x)
- Úloha má deadline za 1 deň → používateľ dostane upozornenie dnes a zajtra (2x)

---

## 2. Diskusia / Správy (Chat)

### Ako to funguje

Aplikácia podporuje **real-time chat** v rámci skupín, kde členovia môžu komunikovať v reálnom čase.

#### Server-side implementácia

**Súbory:**
- `server/src/main/java/sk/ikts/server/controller/ChatController.java` - REST controller pre chat
- `server/src/main/java/sk/ikts/server/service/ChatService.java` - služba pre správu chat správ
- `server/src/main/java/sk/ikts/server/model/ChatMessage.java` - entita chat správy
- `server/src/main/java/sk/ikts/server/repository/ChatMessageRepository.java` - repository pre chat správy

**API Endpointy:**
- `GET /api/chat/group/{groupId}` - získanie správ pre skupinu
- `POST /api/chat/send` - odoslanie správy

**Ako to funguje:**
1. Používateľ pošle správu cez REST API (`POST /api/chat/send`)
2. Server uloží správu do databázy
3. Server broadcastuje správu všetkým pripojeným klientom cez WebSocket
4. Všetci členovia skupiny vidia správu v reálnom čase

**Príklad odoslania správy:**
```java
CreateChatMessageRequest request = new CreateChatMessageRequest();
request.setGroupId(groupId);
request.setUserId(userId);
request.setMessage("Ahoj, ako sa máte?");

ChatMessageDTO message = chatService.createMessage(request);
// Správa sa automaticky broadcastuje cez WebSocket
```

#### Client-side implementácia

**Súbory:**
- `client/src/main/java/sk/ikts/client/util/ChatWebSocketClient.java` - WebSocket klient pre chat
- `client/src/main/java/sk/ikts/client/controller/GroupDetailController.java` - UI pre chat v detaile skupiny
- `client/src/main/java/sk/ikts/client/model/ChatMessage.java` - model chat správy

**Ako to funguje:**
1. Pri otvorení detailu skupiny sa načítajú existujúce správy
2. Klient sa pripojí k WebSocket pre real-time správy
3. Nové správy sa zobrazujú automaticky v chat okne
4. Používateľ môže písať a odosielať správy

**UI Funkcie:**
- Zobrazenie histórie správ
- Real-time aktualizácia nových správ
- Rozlíšenie vlastných a cudzích správ (rôzne farby)
- Zobrazenie mena autora a času odoslania

**Príklad použitia:**
```java
// Načítanie správ
String response = ApiClient.get("/chat/group/" + groupId);
List<ChatMessage> messages = gson.fromJson(response, listType);

// Odoslanie správy
Map<String, Object> request = new HashMap<>();
request.put("groupId", groupId);
request.put("userId", userId);
request.put("message", messageText);
ApiClient.post("/chat/send", request);
```

### WebSocket pre Chat

Chat používa rovnaký WebSocket endpoint ako notifikácie (`ws://127.0.0.1:8081/ws/simple`), ale s iným typom správ:
- Notifikácie: `NotificationDTO` s typom `NEW_TASK`, `DEADLINE_REMINDER`, atď.
- Chat správy: `ChatMessageDTO` s typom `CHAT_MESSAGE`

---

## 3. Technické detaily

### WebSocket Konfigurácia

**Server:**
- Endpoint: `/ws/simple`
- Protokol: Plain WebSocket (nie STOMP)
- Port: 8081

**Klient:**
- URL: `ws://127.0.0.1:8081/ws/simple`
- Knižnica: `java-websocket` (org.java_websocket)

### Databázová štruktúra

**Chat Messages:**
```sql
CREATE TABLE chat_messages (
    message_id INTEGER PRIMARY KEY,
    group_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    user_name TEXT NOT NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL
);
```

### Bezpečnosť

- Chat správy sú viditeľné len pre členov skupiny
- Notifikácie sa posielajú len relevantným používateľom (členom skupiny)
- WebSocket pripojenia sú validované na serveri

---

## 4. Príklady použitia

### Notifikácia o novej úlohe

```java
// V TaskService po vytvorení úlohy
notificationService.notifyNewTask(groupId, task.getTaskId(), task.getTitle());
```

### Notifikácia o zmene statusu

```java
// V TaskService po zmene statusu
notificationService.notifyTaskStatusChange(
    groupId, 
    task.getTaskId(), 
    task.getTitle(), 
    task.getStatus().toString()
);
```

### Odoslanie chat správy

```java
// V GroupDetailController
Map<String, Object> request = new HashMap<>();
request.put("groupId", group.getGroupId());
request.put("userId", userId);
request.put("message", chatMessageField.getText());

ApiClient.post("/chat/send", request);
```

---

## 5. Zhrnutie

✅ **Real-time notifikácie** - fungujú cez WebSocket, používateľ dostáva okamžité upozornenia  
✅ **Chat/diskusia** - plne funkčná, real-time komunikácia v rámci skupín  
✅ **Deadline reminders** - automatické denné upozornenia 3 dni pred deadlinom  
✅ **Automatické obnovenie** - UI sa automaticky aktualizuje pri nových notifikáciách

Všetky tieto funkcie sú plne implementované a funkčné v aplikácii.

