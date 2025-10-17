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
