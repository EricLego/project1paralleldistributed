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
