# Program listings

## Common request handler

_Source: `src/main/python/countdown/common/request_handler.py`_

```python
"""Infrastructure helpers shared by server implementations."""
from __future__ import annotations

import socket
from dataclasses import dataclass
from typing import Tuple

from countdown.service import CountdownService

SocketAddress = Tuple[str, int]


@dataclass
class CountdownRequestHandler:
    """Translate socket I/O into countdown service calls."""

    service: CountdownService

    def handle(self, connection: socket.socket, address: SocketAddress) -> None:
        """Process a single countdown request on an open connection."""
        with connection:
            with connection.makefile("r", encoding="utf-8", newline="\n") as reader, connection.makefile(
                "w", encoding="utf-8", newline="\n"
            ) as writer:
                try:
                    raw_line = reader.readline()
                    if not raw_line:
                        return
                    start_value = int(raw_line.strip())
                    countdown = self.service.generate_countdown(start_value)
                except ValueError as exc:
                    writer.write(f"ERROR: {exc}\n")
                    writer.flush()
                    return

                for value in countdown:
                    writer.write(f"{value}\n")
                writer.flush()
```

## Countdown service

_Source: `src/main/python/countdown/service.py`_

```python
"""Business logic for the countdown protocol."""
from __future__ import annotations

from dataclasses import dataclass


@dataclass
class CountdownService:
    """Generate countdown sequences for the protocol."""

    def generate_countdown(self, start: int) -> list[int]:
        """Return the countdown sequence from ``start`` down to 1."""
        if not isinstance(start, int):
            raise ValueError("Countdown value must be an integer")
        if start <= 0:
            raise ValueError("Countdown value must be a positive integer")
        return list(range(start, 0, -1))
```

## Iterative server

_Source: `src/main/python/countdown/iterative/server.py`_

```python
"""Iterative countdown server implementation."""
from __future__ import annotations

import argparse
import socket
from typing import Optional

from countdown.common.request_handler import CountdownRequestHandler
from countdown.service import CountdownService


def run_server(host: str, port: int, max_clients: Optional[int] = None) -> None:
    """Run the iterative countdown server."""
    service = CountdownService()
    handler = CountdownRequestHandler(service)

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
        server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server_socket.bind((host, port))
        server_socket.listen()
        print(f"[Iterative] Listening on {host}:{port}...")

        served = 0
        try:
            while max_clients is None or served < max_clients:
                connection, address = server_socket.accept()
                print(f"[Iterative] Connected: {address[0]}:{address[1]}")
                handler.handle(connection, address)
                served += 1
                print(f"[Iterative] Completed: {address[0]}:{address[1]}")
        except KeyboardInterrupt:
            print("[Iterative] Shutting down (Ctrl+C received)...")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Iterative countdown server")
    parser.add_argument("port", type=int, nargs="?", default=5000, help="Port to listen on")
    parser.add_argument("host", nargs="?", default="0.0.0.0", help="Host/IP to bind")
    parser.add_argument("--max-clients", type=int, default=None, help="Serve at most this many clients before exiting")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    run_server(args.host, args.port, args.max_clients)


if __name__ == "__main__":
    main()
```

## Iterative client

_Source: `src/main/python/countdown/iterative/client.py`_

```python
"""Iterative countdown client implementation."""
from __future__ import annotations

import argparse
import socket
from typing import Iterable


def request_countdown(value: int, host: str, port: int) -> Iterable[str]:
    """Send a countdown request and yield server responses."""
    if value <= 0:
        raise ValueError("Countdown value must be a positive integer")

    with socket.create_connection((host, port)) as sock:
        sock.sendall(f"{value}\n".encode("utf-8"))
        with sock.makefile("r", encoding="utf-8", newline="\n") as reader:
            for line in reader:
                yield line.strip()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Iterative countdown client")
    parser.add_argument("value", type=int, help="Starting integer for the countdown")
    parser.add_argument("host", nargs="?", default="127.0.0.1", help="Server hostname or IP")
    parser.add_argument("port", type=int, nargs="?", default=5000, help="Server port")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    for response in request_countdown(args.value, args.host, args.port):
        print(response)


if __name__ == "__main__":
    main()
```

## Concurrent server

_Source: `src/main/python/countdown/concurrent/server.py`_

```python
"""Concurrent countdown server implementation using threads."""
from __future__ import annotations

import argparse
import socket
from concurrent.futures import ThreadPoolExecutor
from typing import Optional

from countdown.common.request_handler import CountdownRequestHandler
from countdown.service import CountdownService


def _serve_client(handler: CountdownRequestHandler, connection: socket.socket, address: tuple[str, int]) -> None:
    try:
        handler.handle(connection, address)
    finally:
        print(f"[Concurrent] Completed: {address[0]}:{address[1]}")


def run_server(
    host: str,
    port: int,
    *,
    max_clients: Optional[int] = None,
    max_workers: Optional[int] = None,
) -> None:
    """Run the concurrent countdown server."""
    service = CountdownService()
    handler = CountdownRequestHandler(service)

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
        server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server_socket.bind((host, port))
        server_socket.listen()
        print(f"[Concurrent] Listening on {host}:{port}...")

        served = 0
        with ThreadPoolExecutor(max_workers=max_workers) as executor:
            futures = []
            try:
                while max_clients is None or served < max_clients:
                    connection, address = server_socket.accept()
                    print(f"[Concurrent] Connected: {address[0]}:{address[1]}")
                    futures.append(executor.submit(_serve_client, handler, connection, address))
                    served += 1
            except KeyboardInterrupt:
                print("[Concurrent] Shutting down (Ctrl+C received)...")
            finally:
                for future in futures:
                    future.result()

def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Concurrent countdown server")
    parser.add_argument("port", type=int, nargs="?", default=5001, help="Port to listen on")
    parser.add_argument("host", nargs="?", default="0.0.0.0", help="Host/IP to bind")
    parser.add_argument("--max-clients", type=int, default=None, help="Serve at most this many clients before exiting")
    parser.add_argument("--max-workers", type=int, default=None, help="Maximum worker threads")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    run_server(
        args.host,
        args.port,
        max_clients=args.max_clients,
        max_workers=args.max_workers,
    )


if __name__ == "__main__":
    main()
```

## Concurrent client

_Source: `src/main/python/countdown/concurrent/client.py`_

```python
"""Concurrent countdown client implementation."""
from __future__ import annotations

import argparse
import socket
from typing import Iterable


def request_countdown(value: int, host: str, port: int) -> Iterable[str]:
    """Send a countdown request and yield responses."""
    if value <= 0:
        raise ValueError("Countdown value must be a positive integer")

    with socket.create_connection((host, port)) as sock:
        sock.sendall(f"{value}\n".encode("utf-8"))
        with sock.makefile("r", encoding="utf-8", newline="\n") as reader:
            for line in reader:
                yield line.strip()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Concurrent countdown client")
    parser.add_argument("value", type=int, help="Starting integer for the countdown")
    parser.add_argument("host", nargs="?", default="127.0.0.1", help="Server hostname or IP")
    parser.add_argument("port", type=int, nargs="?", default=5001, help="Server port")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    for response in request_countdown(args.value, args.host, args.port):
        print(response)


if __name__ == "__main__":
    main()
```
