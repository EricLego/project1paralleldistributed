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
