# OnlineAuctionSystem

A Java Swing desktop application for managing online auctions.

## Project Structure

```
OnlineAuctionSystem/
├── README.md
└── src/
    └── auction/
        ├── Main.java              # Application entry point
        ├── model/                 # Domain model classes (User, Item, Bid, Auction, …)
        │   └── package-info.java
        ├── enums/                 # Enum types (AuctionStatus, BidStatus, UserRole, …)
        │   └── package-info.java
        ├── exceptions/            # Custom exceptions (InvalidBidException, …)
        │   └── package-info.java
        ├── service/               # Business-logic / service layer
        │   └── package-info.java
        └── gui/                   # Swing UI components (frames, panels, dialogs)
            └── package-info.java
```

## Package Overview

| Package              | Purpose |
|----------------------|---------|
| `auction`            | Contains `Main.java` — the application entry point. |
| `auction.model`      | Domain entities such as users, items, bids, and auctions. |
| `auction.enums`      | Enumerations for statuses, roles, and categories. |
| `auction.exceptions` | Custom exception types for validation and error handling. |
| `auction.service`    | Business logic — auction lifecycle, bidding rules, user management. |
| `auction.gui`        | Swing-based graphical user interface. |

## Prerequisites

- **Java 17+** (or any modern JDK with Swing support)
- No external build tools required — compile directly with `javac`.

## Build & Run

```bash
# Compile
javac -d out src/auction/*.java src/auction/**/*.java

# Run
java -cp out auction.Main
```
# java-ad-auction-sim
