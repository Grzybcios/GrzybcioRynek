# GrzybcioRynek

![Logo](GrzybcioRynek-logo-96x96.png)

Plugin Spigot/Paper dla Minecraft 1.21 — rynek graczy z GUI, ekonomią Vault oraz wsparciem Nexo i Oraxen.

---

## Opis krótki (PL)

**GrzybcioRynek** to plugin rynku graczy dla serwera Minecraft, który pozwala wystawiać przedmioty z ręki, przeglądać oferty w graficznym interfejsie i kupować je za serwerową walutę. Integruje się z Vault (ekonomia) oraz opcjonalnie z Nexo i Oraxen, dzięki czemu poprawnie obsługuje także custom itemy. Oferty mają limity, datę wygaśnięcia i bezpieczne transakcje — sprzedawca dostaje pieniądze automatycznie oraz powiadomienie o sprzedaży.

## Short description (EN)

**GrzybcioRynek** is a player marketplace plugin for Minecraft that lets players list items from hand, browse listings in a GUI, and purchase them using the server economy. It integrates with Vault and optionally with Nexo and Oraxen, so custom items are supported as well. Listings have limits, expiry dates, and safe transactions — sellers are paid automatically and notified when their item is sold.

---

## Opis długi (PL)

**GrzybcioRynek** to plugin Spigot/Paper dla Minecraft **1.21**, który dodaje na serwer **rynek graczy** (aukcje domowe) z graficznym interfejsem, integracją z ekonomią Vault oraz wsparciem dla przedmiotów z **Nexo** i **Oraxen**.

Gracze mogą wystawiać przedmiot trzymany w ręce na sprzedaż, przeglądać oferty innych w wygodnym GUI z kategoriami i stronicowaniem, a także kupować przedmioty za serwerową walutę. Sprzedawca otrzymuje pieniądze automatycznie, a przy sprzedaży dostaje powiadomienie — także po ponownym wejściu na serwer, jeśli był offline.

Plugin zapisuje oferty w pliku `listings.yml`, pilnuje limitów (domyślnie max. 10 ofert na gracza), wygasania ofert (domyślnie 7 dni) oraz bezpiecznych transakcji z rollbackiem w razie błędu. Administratorzy mogą przeładować konfigurację i usuwać cudze oferty.

### Główne funkcje

- **GUI rynku** z kategoriami: Narzędzia, Bloki, Walka, Inne
- **Wystawianie** przedmiotów z ręki (`/market sell`, `/wystaw`)
- **Zakup** z potwierdzeniem w osobnym oknie GUI
- **Ekonomia Vault** — wymagana do działania pluginu
- **Nexo / Oraxen** — poprawne zapisywanie i odtwarzanie custom itemów
- **Wygasanie ofert** — po upływie czasu przedmiot wraca do ekwipunku sprzedawcy
- **Powiadomienia o sprzedaży** — online od razu, offline po wejściu na serwer
- **Konfigurowalne wiadomości**, limity cen i ustawienia rynku

### Komendy

| Komenda | Opis |
|---------|------|
| `/market`, `/rynek`, `/ah` | Otwiera GUI rynku |
| `/market sell <cena>` | Wystawia przedmiot z ręki |
| `/market remove <id>` | Usuwa własną ofertę (admin: dowolną) |
| `/market reload` | Przeładowuje konfigurację |
| `/wystaw <cena>` | Skrót do wystawienia przedmiotu |
| `/aukcje`, `/rynek-gui`, `/ah-gui` | Otwiera GUI rynku |

### Wymagania

- **Spigot/Paper 1.21+**
- **Vault** + plugin ekonomii (np. EssentialsX Economy)
- Opcjonalnie: **Nexo**, **Oraxen**

### Uprawnienia

| Uprawnienie | Opis | Domyślnie |
|-------------|------|-----------|
| `market.use` | Korzystanie z rynku | wszyscy |
| `market.sell` | Wystawianie przedmiotów | wszyscy |
| `market.reload` | Przeładowanie configu | OP |
| `market.admin` | Usuwanie cudzych ofert | OP |

---

## Long description (EN)

**GrzybcioRynek** is a Spigot/Paper plugin for Minecraft **1.21** that adds a **player marketplace** (auction house) with a graphical interface, Vault economy integration, and support for custom items from **Nexo** and **Oraxen**.

Players can list the item in their hand for sale, browse other players' listings in a categorized GUI with pagination, and purchase items using the server's currency. Sellers are paid automatically and receive sale notifications — including after reconnecting if they were offline.

Listings are stored in `listings.yml`. The plugin enforces limits (default: 10 listings per player), listing expiry (default: 7 days), and safe transactions with rollback on failure. Administrators can reload the config and remove other players' listings.

### Key features

- **Market GUI** with categories: Tools, Blocks, Combat, Other
- **Listing items** from hand (`/market sell`, `/wystaw`)
- **Purchases** with a confirmation GUI
- **Vault economy** — required for the plugin to run
- **Nexo / Oraxen** — proper saving and recreation of custom items
- **Listing expiry** — expired items are returned to the seller's inventory
- **Sale notifications** — instant when online, queued when offline
- **Configurable messages**, price limits, and market settings

### Commands

| Command | Description |
|---------|-------------|
| `/market`, `/rynek`, `/ah` | Opens the market GUI |
| `/market sell <price>` | Lists the item in hand |
| `/market remove <id>` | Removes your listing (admins: any listing) |
| `/market reload` | Reloads the configuration |
| `/wystaw <price>` | Shortcut to list an item |
| `/aukcje`, `/rynek-gui`, `/ah-gui` | Opens the market GUI |

### Requirements

- **Spigot/Paper 1.21+**
- **Vault** + an economy plugin (e.g. EssentialsX Economy)
- Optional: **Nexo**, **Oraxen**

### Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `market.use` | Use the marketplace | everyone |
| `market.sell` | List items for sale | everyone |
| `market.reload` | Reload config | OP |
| `market.admin` | Remove other players' listings | OP |

---

**Autor:** Grzybcio · **Wersja:** 1.0.0
