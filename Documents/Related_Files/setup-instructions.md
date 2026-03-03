##  Setup & Run
**1. Database**

Create:

```
notevault_db
```

**2. Environment Variables**

Set:
```
DB_HOST
BD_PORT
DB_NAME
DB_USER
DB_PASSWORD
```

**3. Build**
``` bash
mvn clean install
```

**4. Run**
``` bash
mvn javafx:run
```

---

## Before You Run

Ensure the following are installed on your machine:
- Docker & Docker Compose
- X11 server (Xming on Windows, XQuartz on macOS)

## Running NoteVault with Docker

You can run NoteVault together with a MariaDB database using Docker Compose. This provides a consistent environment for testing and learning.

### Prerequisites

Before running the app:

**1. Docker & Docker Compose installed**
- Docker Desktop for Windows/macOS
- Linux: install `docker` and `docker-compose` via your package manager

**2. X11 server for GUI forwarding**
- Windows: Xming
- macOS: XQuartz
- Linux: typically already included

---

**1. Start the application and database**

You can pass credentials inline while running:

```bash
DB_USER=myuser DB_PASSWORD=mysecret docker-compose up --build
```

- `DB_USER` and `DB_PASSWORD` can be any values you like.

- If you don’t pass them, defaults will be used:

    - **DB_USER=root**

    - **DB_PASSWORD=rootpassword**

Docker Compose will automatically:

- Build the NoteVault app image

- Start the MariaDB database container (`notevault-db`)

- Start the NoteVault application container (`notevault-app`)
> The application will connect to the database automatically using these credentials.

---

**2. Access the application**

- The GUI is forwarded through X11 (`DISPLAY=host.docker.internal:0.0`)

- On Windows, make sure Xming is running

- On macOS, make sure XQuartz is running

---

**3. Stop the application**

```bash
docker-compose down
```

This stops both the application and database containers.
> Database data is persisted in the `db_data` volume, so stopping containers does not delete your data.

---

**4. Notes & Tips**

- The app container waits for the database container via `depends_on`, but it does not wait for DB readiness.

    - If connection fails at first, stop and restart containers.

- To rebuild only the app container:

```bash
docker-compose build app
```

- This approach is beginner-friendly — no .env file is required. Students can experiment with credentials inline.