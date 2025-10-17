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
