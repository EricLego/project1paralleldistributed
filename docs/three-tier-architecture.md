# Three-tier architecture overview

The countdown protocol implementation follows a three-tier design:

1. **Presentation tier** – Implemented by the client applications (`countdown.iterative.client` and `countdown.concurrent.client`).
   They collect the user input (the starting value), submit it to the server, and display the returned countdown to the console.
2. **Application tier** – Implemented by the server controllers (`countdown.iterative.server` and `countdown.concurrent.server`).
   Each server accepts TCP connections, coordinates request handling, and delegates countdown computation to the service layer.
3. **Data/logic tier** – Encapsulated in `countdown.service.CountdownService`, which owns the business rule for generating the countdown sequence.
   The shared `countdown.common.request_handler.CountdownRequestHandler` acts as the infrastructure gateway that converts socket communication into service calls.

## Server state analysis

The servers are **stateless**: no global or session-level data is retained between requests. Every connection is handled using the
incoming integer value alone, and the `CountdownService` deterministically generates the countdown from that single input. After the
response is sent, the connection is closed and no state persists.

This separation keeps state management and business logic isolated from networking details, enabling each tier to evolve independently.
