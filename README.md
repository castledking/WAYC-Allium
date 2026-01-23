# CreativeTracker

📊 A Minecraft plugin to track creative mode item acquisition with in-game GUI and web interface.

---

## ✨ Features

- **Creative Inventory Tracking** – Logs items taken from creative inventory
- **Command Tracking** – Monitors `/give` and `/item` commands
- **Gamemode Tracking** – Records when players switch to creative mode
- **In-Game GUI** – Beautiful paginated log viewer with filtering options
- **Web Interface** – Modern web dashboard with search and statistics
- **SQLite Storage** – Efficient, file-based database
- **Folia Compatible** – Full support for multithreaded servers
- **Bypass Permission** – Exclude trusted players from tracking

---

## 📦 Installation

1. Download `CreativeTracker.jar`
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure in `plugins/CreativeTracker/config.yml`

---

## 💻 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/ct` or `/creativetracker` | Open the log viewer GUI | `creativetracker.use` |
| `/ct stats` | View quick statistics | `creativetracker.use` |
| `/ct reload` | Reload configuration | `creativetracker.admin` |
| `/ct webpassword <password>` | Set web interface password | `creativetracker.admin` |
| `/ct clear confirm` | Clear all logs | `creativetracker.admin` |
| `/ct help` | Show help message | `creativetracker.use` |

---

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `creativetracker.use` | Access to the tracker GUI and basic commands | `op` |
| `creativetracker.admin` | Admin functions (reload, password, clear) | `op` |
| `creativetracker.bypass` | Exclude player from being tracked | `false` |

---

## ⚙️ Configuration

```yaml
# Creative Tracker Configuration

web:
  # Enable the web interface
  enabled: true
  # Port for the web server (access via http://your-ip:port)
  port: 6745

tracking:
  # Track items taken from creative inventory
  creative-inventory: true
  # Track /give command usage
  give-command: true
  # Track /gamemode changes (for logging purposes)
  gamemode-command: true

storage:
  # Auto-save interval in seconds
  auto-save-interval: 300
  # Maximum number of logs to keep (oldest are removed when exceeded)
  max-logs: 10000
```

---

## 🌐 Web Interface

Access the web interface at `http://your-server-ip:6745` (default port).

On first access, you'll be prompted to set a password. You can change it later with `/ct webpassword <new-password>`.

**Features:**
- View all logs with pagination
- Search by player, item, or acquisition method
- Statistics dashboard with top players and items
- Clear logs directly from the interface

---

## 📋 Supported Versions

| Version | Supported |
|---------|-----------|
| 1.21.x | ✅ |
| Folia | ✅ |

**Requirements:**
- Java 17 or higher
- Paper/Spigot 1.21+

---

## 📄 License

This project is licensed under the BSD 3-Clause License - see the [LICENSE](LICENSE) file for details.

---

<div align="center">

## 🤝 Partner

<a href="https://emeraldhost.de/frxme">
  <img src="https://cdn.emeraldhost.de/branding/icon/icon.png" width="80" alt="Emerald Host Logo">
</a>

### Powered by Emerald Host

*DDoS-Protection, NVMe Performance und 99.9% Uptime.* *Der Host meines Vertrauens für alle Development-Server.*

<a href="https://emeraldhost.de/frxme">
  <img src="https://img.shields.io/badge/Code-Frxme10-10b981?style=for-the-badge&logo=gift&logoColor=white&labelColor=0f172a" alt="Use Code Frxme10 for 10% off">
</a>

</div>

---

## 👤 Author

**Frxme**
