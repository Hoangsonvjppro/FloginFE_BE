# 🐳 Docker Deployment Guide

## Architecture

This application uses a microservices architecture with 4 Docker containers:

```
┌─────────────────────────────────────────────┐
│             Frontend Container               │
│          (React + Nginx on port 80)         │
└──────────────────┬──────────────────────────┘
                   │
                   │ HTTP Requests
                   ▼
┌─────────────────────────────────────────────┐
│            Backend Container                 │
│     (Spring Boot on port 8081)              │
└───────┬──────────────────────┬──────────────┘
        │                      │
        │                      │
        ▼                      ▼
┌──────────────┐      ┌──────────────────┐
│   Oracle DB  │      │   PostgreSQL DB  │
│ (Auth Data)  │      │ (Product Data)   │
│  Port 1521   │      │   Port 5432      │
└──────────────┘      └──────────────────┘
```

## Prerequisites

- Docker Desktop installed and running
- Docker Compose v3.8 or higher
- At least 8GB RAM available for Docker
- At least 10GB free disk space

## Quick Start

### 1. Build and Start All Services

```bash
docker-compose up -d
```

This will:
- Pull/build all required images
- Start 4 containers (Oracle, PostgreSQL, Backend, Frontend)
- Set up networking between containers
- Initialize databases

### 2. Check Container Status

```bash
docker-compose ps
```

You should see all 4 services running:
```
NAME                STATUS              PORTS
flogin-frontend     Up (healthy)        0.0.0.0:80->80/tcp
flogin-backend      Up (healthy)        0.0.0.0:8081->8081/tcp
flogin-oracle       Up (healthy)        0.0.0.0:1521->1521/tcp
flogin-postgres     Up (healthy)        0.0.0.0:5432->5432/tcp
```

### 3. Access the Application

- **Frontend**: http://localhost
- **Backend API**: http://localhost:8081/api
- **Backend Health**: http://localhost:8081/actuator/health

### 4. View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f oracle-db
docker-compose logs -f postgres-db
```

## Container Details

### Frontend Container
- **Image**: Custom (Node 20 + Nginx Alpine)
- **Port**: 80
- **Build**: Multi-stage (build with Node, serve with Nginx)
- **Health Check**: HTTP GET / every 30s

### Backend Container
- **Image**: Custom (Maven + OpenJDK 21)
- **Port**: 8081
- **Build**: Multi-stage (build with Maven, run with JRE)
- **Health Check**: HTTP GET /actuator/health every 30s
- **Startup Time**: ~60 seconds (waits for databases)

### Oracle Database Container
- **Image**: container-registry.oracle.com/database/express:21.3.0-xe
- **Port**: 1521 (database), 5500 (EM Express)
- **Volume**: oracle-data (persistent storage)
- **Init Scripts**: init-scripts/oracle/
- **Startup Time**: ~60-90 seconds

### PostgreSQL Database Container
- **Image**: postgres:16-alpine
- **Port**: 5432
- **Volume**: postgres-data (persistent storage)
- **Init Scripts**: init-scripts/postgres/
- **Startup Time**: ~10 seconds

## Common Commands

### Start Services
```bash
# Start all services
docker-compose up -d

# Start specific service
docker-compose up -d backend

# Start with logs
docker-compose up
```

### Stop Services
```bash
# Stop all services
docker-compose down

# Stop and remove volumes (⚠️ deletes data)
docker-compose down -v

# Stop specific service
docker-compose stop backend
```

### Rebuild Services
```bash
# Rebuild all
docker-compose build

# Rebuild specific service
docker-compose build backend

# Rebuild and restart
docker-compose up -d --build
```

### Database Access

#### Oracle Database
```bash
# Connect to Oracle container
docker exec -it flogin-oracle sqlplus sys/OraclePassword123@//localhost:1521/XE as sysdba

# Connect as auth_user
docker exec -it flogin-oracle sqlplus auth_user/AuthPassword123@//localhost:1521/XE
```

#### PostgreSQL Database
```bash
# Connect to PostgreSQL container
docker exec -it flogin-postgres psql -U product_user -d products

# Run SQL file
docker exec -i flogin-postgres psql -U product_user -d products < backup.sql
```

## Environment Variables

You can override default values by creating a `.env` file:

```env
# Database Passwords
ORACLE_PASSWORD=YourOraclePassword
AUTH_PASSWORD=YourAuthPassword
POSTGRES_PASSWORD=YourPostgresPassword

# Backend Configuration
SPRING_PROFILES_ACTIVE=docker

# Frontend Configuration
REACT_APP_API_URL=http://backend:8081
```

## Troubleshooting

### Container Won't Start
```bash
# Check logs
docker-compose logs [service-name]

# Check resource usage
docker stats

# Restart specific service
docker-compose restart [service-name]
```

### Database Connection Issues
```bash
# Wait for databases to be fully initialized (can take 60-90s for Oracle)
docker-compose logs oracle-db | grep "DATABASE IS READY TO USE"

# Test database connectivity
docker exec flogin-backend nc -zv oracle-db 1521
docker exec flogin-backend nc -zv postgres-db 5432
```

### Backend Can't Connect to Databases
```bash
# Check if databases are healthy
docker-compose ps

# Restart backend after databases are ready
docker-compose restart backend
```

### Frontend Can't Connect to Backend
```bash
# Check backend health
curl http://localhost:8081/actuator/health

# Check nginx configuration
docker exec flogin-frontend cat /etc/nginx/conf.d/default.conf

# Restart frontend
docker-compose restart frontend
```

### Clean Restart
```bash
# Stop everything
docker-compose down

# Remove volumes (⚠️ deletes all data)
docker-compose down -v

# Remove images
docker-compose down --rmi all

# Rebuild and start fresh
docker-compose up -d --build
```

## Backup and Restore

### Backup Data
```bash
# Backup Oracle
docker exec flogin-oracle sh -c 'exp auth_user/AuthPassword123@XE file=/tmp/auth_backup.dmp'
docker cp flogin-oracle:/tmp/auth_backup.dmp ./backups/

# Backup PostgreSQL
docker exec flogin-postgres pg_dump -U product_user products > ./backups/products_backup.sql
```

### Restore Data
```bash
# Restore Oracle
docker cp ./backups/auth_backup.dmp flogin-oracle:/tmp/
docker exec flogin-oracle sh -c 'imp auth_user/AuthPassword123@XE file=/tmp/auth_backup.dmp'

# Restore PostgreSQL
docker exec -i flogin-postgres psql -U product_user products < ./backups/products_backup.sql
```

## Performance Tuning

### Allocate More Memory to Docker
- Docker Desktop → Settings → Resources → Memory
- Recommended: 8GB minimum, 16GB optimal

### Adjust Container Resources
Edit `docker-compose.yml`:

```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
        reservations:
          memory: 1G
```

## Production Considerations

⚠️ **This configuration is for development only!**

For production:
1. Use managed databases (AWS RDS, Azure Database)
2. Implement proper secrets management
3. Enable HTTPS with SSL certificates
4. Set up load balancing
5. Configure logging aggregation
6. Implement monitoring (Prometheus, Grafana)
7. Use production-ready images
8. Set resource limits
9. Enable auto-scaling
10. Regular backups

## Network Architecture

All containers are on the same bridge network `flogin-network`:

```
flogin-network (bridge)
├── oracle-db (1521)
├── postgres-db (5432)
├── backend (8081)
└── frontend (80)
```

Containers can communicate using service names as hostnames.

## Health Checks

All containers have health checks:
- **Frontend**: HTTP GET / every 30s
- **Backend**: HTTP GET /actuator/health every 30s
- **Oracle**: sqlplus connection test every 30s
- **PostgreSQL**: pg_isready every 10s

## Useful Docker Commands

```bash
# View container resource usage
docker stats

# Inspect a container
docker inspect flogin-backend

# Execute command in container
docker exec -it flogin-backend bash

# Copy files from container
docker cp flogin-backend:/app/logs/application.log ./

# View container logs with timestamps
docker-compose logs -f -t backend

# Remove unused images/volumes
docker system prune -a --volumes
```

## Support

For issues:
1. Check logs: `docker-compose logs`
2. Verify all services are healthy: `docker-compose ps`
3. Review this README
4. Check container resource usage: `docker stats`

---

**Happy Dockerizing! 🐳**
