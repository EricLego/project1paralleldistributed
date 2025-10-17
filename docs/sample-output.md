# Sample command transcripts

## Iterative suite

Server:
```text
$ java -cp src/main/java com.example.countdown.iterative.IterativeCountdownServer 5050
[Iterative] Listening on port 5050...
[Iterative] Connected: /127.0.0.1:34782
[Iterative] Completed: /127.0.0.1:34782
```

Client:
```text
$ java -cp src/main/java com.example.countdown.iterative.IterativeCountdownClient 5 localhost 5050
5
4
3
2
1
```

## Concurrent suite

Server:
```text
$ java -cp src/main/java com.example.countdown.concurrent.ConcurrentCountdownServer 6060 4
[Concurrent] Listening on port 6060...
[Concurrent] Connected: /127.0.0.1:54996
[Concurrent] Completed: /127.0.0.1:54996
[Concurrent] Connected: /127.0.0.1:55000
[Concurrent] Completed: /127.0.0.1:55000
```

Client 1:
```text
$ java -cp src/main/java com.example.countdown.concurrent.ConcurrentCountdownClient 4 localhost 6060
4
3
2
1
```

Client 2:
```text
$ java -cp src/main/java com.example.countdown.concurrent.ConcurrentCountdownClient 3 localhost 6060
3
2
1
```
