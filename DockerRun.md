# NoteVault Demo (Step-by-Step)

```bash
# Start database (Docker)
docker-compose up -d db

# Verify database is running
docker ps

# Build project
mvn clean install

# Run application
java -jar target/notevault.jar
``` 