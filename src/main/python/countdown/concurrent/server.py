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
