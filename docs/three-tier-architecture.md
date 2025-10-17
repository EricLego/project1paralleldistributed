# Three-tier architecture overview

The countdown protocol implementation follows a three-tier design:

1. **Presentation tier** – Implemented by the client applications (`countdown.iterative.client` and `countdown.concurrent.client`).
   They collect the user input (the starting value), submit it to the server, and display the returned countdown to the console.
2. **Application tier** – Implemented by the server controllers (`countdown.iterative.server` and `countdown.concurrent.server`).
   Each server accepts TCP connections, coordinates request handling, and delegates countdown computation to the service layer.
3. **Data/logic tier** – Encapsulated in `countdown.service.CountdownService`, which owns the business rule for generating the countdown sequence.
   The shared `countdown.common.request_handler.CountdownRequestHandler` acts as the infrastructure gateway that converts socket communication into service calls.

Module map:

* `countdown.iterative.server` / `countdown.concurrent.server` – TCP controllers that expose the service over iterative and thread-pooled connection handling.
* `countdown.common.request_handler` – Protocol adapter that parses the inbound integer, invokes the service, and emits the countdown messages.
* `countdown.service` – Pure business logic that creates the integer sequence, isolated from transport concerns.
* `countdown.iterative.client` / `countdown.concurrent.client` – Console front-ends that gather user input and render the countdown returned by the server.

## Server state analysis

The countdown servers are **stateful per session**. Each accepted connection stores the client's requested starting value (`n`) and the current
countdown position while the handler streams `n, n-1, …, 1` back to that specific client. Once the session finishes and the socket closes, that
per-connection state is discarded and no information is shared globally across different clients. There is therefore no global state spanning
sessions, only transient session scope data tracked for the lifetime of each connection.

This separation keeps state management and business logic isolated from networking details, enabling each tier to evolve independently.
