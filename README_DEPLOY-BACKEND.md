
# WebSocket Server Deployment (Gruppe 2)


## Erste Verbindung zum Server 

### Mit dem Server verbinden 
```bash
ssh grp-2@se2-demo.aau.at -p 53200
```
![uni-server-welcomeScreen.png](images%2Funi-server-welcomeScreen.png)


## Erstes Backend-Deployment auf dem Server

### 1. `.jar` und `Dockerfile` vom lokalen Rechner auf den Server kopieren.
```bash
scp -P 53200 WebSocketDemo-Server-0.0.1-SNAPSHOT.jar grp-2@se2-demo.aau.at:~
scp -P 53200 Dockerfile grp-2@se2-demo.aau.at:~
```

### 2. Alten Container stoppen & löschen 
```bash
docker stop websocket-demo
docker rm websocket-demo
```

### 3. Docker-Image bauen
```bash
docker build -t websocket-server-gruppe2 .
```

### 4. Container starten
```bash
docker run -d --name websocket-server-gruppe2 -p 53206:53206 websocket-server-gruppe2
```

### 5. Logs prüfen
```bash
docker ps
docker logs websocket-server-gruppe2
```

---

## Backend-Update 

### 1. Neue `.jar` lokal bauen
```bash
mvn clean package
```
→ Datei wird dann unter `C:\Users\ahmed\SE2\target/WebSocketDemo-Server-0.0.1-SNAPSHOT.jar` liegen. 

### 2. Neue `.jar` auf Server kopieren
```bash
    scp -P 53200 'C:\Users\ahmed\SE2\target\WebSocketDemo-Server-0.0.1-SNAPSHOT.jar grp-2@se2-demo.aau.at:~
```


### 3. Container stoppen & löschen
```bash
docker stop websocket-server-gruppe2
docker rm websocket-server-gruppe2
```

### 4. Neues Image bauen & Container starten
```bash
docker build -t websocket-server-gruppe2 .
docker run -d --name websocket-server-gruppe2 -p 53206:53206 websocket-server-gruppe2
```

![run-docker.png](images%2Frun-docker.png)


