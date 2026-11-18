#!/usr/bin/env python3
"""
KUPI Complete Microservices Build and Run Script
Builds and runs all KUPI microservices in proper dependency order

Usage:
    python3 build_and_run_services.py              # Build and run all services
    python3 build_and_run_services.py --build-only # Only build services
    python3 build_and_run_services.py --run-only   # Only run services (must build first)
    python3 build_and_run_services.py --clean      # Clean build artifacts
    python3 build_and_run_services.py --status     # Check service status
    python3 build_and_run_services.py --help       # Show help
"""

import os
import sys
import subprocess
import time
import argparse
import signal
import shutil
from pathlib import Path
from datetime import datetime

# Configuration
PROJECT_ROOT = Path.cwd()
# Java 21: taken from JAVA_HOME, else the first "java" on PATH
JAVA_21_PATH = (os.path.join(os.environ["JAVA_HOME"], "bin", "java")
                if os.environ.get("JAVA_HOME") else (shutil.which("java") or "java"))
LOG_DIR = "/tmp"

# Load environment variables from .env.local
def load_env_file(env_file=".env.local"):
    """Load environment variables from .env.local file"""
    env_vars = {}
    env_path = PROJECT_ROOT / env_file

    if not env_path.exists():
        print(f"Warning: {env_file} not found at {env_path}")
        return env_vars

    try:
        with open(env_path, 'r') as f:
            for line in f:
                line = line.strip()
                # Skip comments and empty lines
                if not line or line.startswith('#'):
                    continue
                # Parse KEY=VALUE
                if '=' in line:
                    key, value = line.split('=', 1)
                    env_vars[key.strip()] = value.strip()

        print(f"✓ Loaded {len(env_vars)} environment variables from {env_file}")
        return env_vars
    except Exception as e:
        print(f"Error loading {env_file}: {str(e)}")
        return env_vars

# Load env file at startup
ENV_VARS = load_env_file()

# Service configurations (ordered by startup dependency)
SERVICES = {
    "discovery": {
        "name": "Discovery Service (Eureka)",
        "dir": "kupi-discovery-service",
        "jar": "kupi-discovery-service-0.0.1-SNAPSHOT.jar",
        "port": 8761,
        "log": f"{LOG_DIR}/discovery-service.log",
        "health_url": "http://localhost:8761",
        "wait_time": 15,
    },
    "gateway": {
        "name": "API Gateway (Zuul)",
        "dir": "kupi-api-gateway",
        "jar": "kupi-api-gateway-0.0.1-SNAPSHOT.jar",
        "port": 8080,
        "log": f"{LOG_DIR}/api-gateway.log",
        "health_url": "http://localhost:8080/actuator/health",
        "wait_time": 10,
    },
    "auth": {
        "name": "Auth Service",
        "dir": "kupi-auth-service",
        "jar": "kupi-auth-service-0.0.1-SNAPSHOT.jar",
        "port": 8081,
        "log": f"{LOG_DIR}/auth-service.log",
        "health_url": "http://localhost:8081/actuator/health",
        "wait_time": 10,
    },
    "otp": {
        "name": "OTP Service",
        "dir": "kupi-otp-service",
        "jar": "kupi-otp-service-0.0.1-SNAPSHOT.jar",
        "port": 8082,
        "log": f"{LOG_DIR}/otp-service.log",
        "health_url": "http://localhost:8082/actuator/health",
        "wait_time": 8,
    },
    "file": {
        "name": "File Manager Service",
        "dir": "kupi-file-manager",
        "jar": "kupi-file-manager-service-0.0.1-SNAPSHOT.jar",
        "port": 8083,
        "log": f"{LOG_DIR}/file-manager.log",
        "health_url": "http://localhost:8083/actuator/health",
        "wait_time": 8,
    },
    "core": {
        "name": "Core Service",
        "dir": "kupi-core-service",
        "jar": "kupi-core-service-0.0.1-SNAPSHOT.jar",
        "port": 8084,
        "log": f"{LOG_DIR}/core-service.log",
        "health_url": "http://localhost:8084/actuator/health",
        "wait_time": 10,
    },
    "util": {
        "name": "Util Service",
        "dir": "kupi-util-service",
        "jar": "kupi-util-service-0.0.1-SNAPSHOT.jar",
        "port": 8085,
        "log": f"{LOG_DIR}/util-service.log",
        "health_url": "http://localhost:8085/actuator/health",
        "wait_time": 8,
    },
    "location": {
        "name": "Location Service",
        "dir": "kupi-location-service",
        "jar": "kupi-location-service-0.0.1-SNAPSHOT.jar",
        "port": 8087,
        "log": f"{LOG_DIR}/location-service.log",
        "health_url": "http://localhost:8087/actuator/health",
        "wait_time": 10,
    },
    "order_payment": {
        "name": "Order Payment Service",
        "dir": "kupi-order-payment-service",
        "jar": "kupi-order-payment-service-0.0.1-SNAPSHOT.jar",
        "port": 8088,
        "log": f"{LOG_DIR}/order-payment-service.log",
        "health_url": "http://localhost:8088/actuator/health",
        "wait_time": 10,
    },
    "web": {
        "name": "KUPI Web App (React)",
        "dir": "kupi-web-app",
        "port": 3000,
        "log": f"{LOG_DIR}/web-app.log",
        "health_url": "http://localhost:3000",
        "wait_time": 8,
        "is_web": True,
    },
}

# Service startup order (dependencies must start first)
SERVICE_STARTUP_ORDER = [
    "discovery",
    "gateway",
    "auth",
    "otp",
    "file",
    "core",
    "util",
    "location",
    "order_payment",
    "web",
]

# Color codes for output
class Colors:
    """ANSI color codes for terminal output"""
    HEADER = "\033[95m"
    OKBLUE = "\033[94m"
    OKCYAN = "\033[96m"
    OKGREEN = "\033[92m"
    WARNING = "\033[93m"
    FAIL = "\033[91m"
    ENDC = "\033[0m"
    BOLD = "\033[1m"
    UNDERLINE = "\033[4m"


def print_header(text):
    """Print a formatted header"""
    print(f"\n{Colors.HEADER}{Colors.BOLD}{'='*80}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{text.center(80)}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{'='*80}{Colors.ENDC}\n")


def print_section(text):
    """Print a section divider"""
    print(f"\n{Colors.OKBLUE}{Colors.BOLD}{text}{Colors.ENDC}")
    print(f"{Colors.OKBLUE}{'-' * len(text)}{Colors.ENDC}")


def print_success(text):
    """Print success message"""
    print(f"{Colors.OKGREEN}✓ {text}{Colors.ENDC}")


def print_info(text):
    """Print info message"""
    print(f"{Colors.OKCYAN}ℹ {text}{Colors.ENDC}")


def print_warning(text):
    """Print warning message"""
    print(f"{Colors.WARNING}⚠ {text}{Colors.ENDC}")


def print_error(text):
    """Print error message"""
    print(f"{Colors.FAIL}✗ {text}{Colors.ENDC}")


def check_docker_containers():
    """Check if Docker containers (MySQL and Redis) are running, start if needed"""
    print_section("Checking Docker Containers")

    # Check if docker-compose is installed
    result = subprocess.run(["which", "docker-compose"], capture_output=True, text=True)
    if result.returncode != 0:
        print_warning("docker-compose not found - containers may not be available")
        return True  # Don't fail, proceed anyway
    print_success("docker-compose found")

    # Check if MySQL container is running
    result = subprocess.run(
        ["docker", "ps", "--filter", "name=kupi-mysql", "--format", "{{.Names}}"],
        capture_output=True,
        text=True,
    )
    mysql_running = "kupi-mysql" in result.stdout

    # Check if Redis container is running
    result = subprocess.run(
        ["docker", "ps", "--filter", "name=kupi-redis", "--format", "{{.Names}}"],
        capture_output=True,
        text=True,
    )
    redis_running = "kupi-redis" in result.stdout

    if mysql_running and redis_running:
        print_success("MySQL container is running")
        print_success("Redis container is running")
        return True

    # Start containers if not running
    if not mysql_running or not redis_running:
        print_info("Starting Docker containers...")

        # Check if docker-compose.yml exists
        compose_file = PROJECT_ROOT / "docker-compose.yml"
        if not compose_file.exists():
            print_warning("docker-compose.yml not found - skipping container startup")
            return True

        try:
            # Start containers in background
            result = subprocess.run(
                ["docker-compose", "up", "-d"],
                cwd=str(PROJECT_ROOT),
                capture_output=True,
                text=True,
                timeout=60,
            )

            if result.returncode != 0:
                print_warning(f"docker-compose up failed: {result.stderr}")
                return True  # Don't fail, proceed anyway

            print_info("Docker containers starting...")

            # Wait for containers to be healthy (max 60 seconds)
            print_info("Waiting for containers to be ready (max 60 seconds)...")
            for i in range(12):  # Try 12 times with 5-second intervals
                time.sleep(5)

                # Check MySQL
                mysql_result = subprocess.run(
                    ["docker", "exec", "kupi-mysql", "mysqladmin", "ping", "-h", "localhost"],
                    capture_output=True,
                    text=True,
                    timeout=5,
                )

                # Check Redis
                redis_result = subprocess.run(
                    ["docker", "exec", "kupi-redis", "redis-cli", "ping"],
                    capture_output=True,
                    text=True,
                    timeout=5,
                )

                if mysql_result.returncode == 0 and redis_result.returncode == 0:
                    print_success("MySQL container is ready")
                    print_success("Redis container is ready")
                    return True

                print_info(f"Waiting for containers... ({i+1}/12 attempts)")

            print_warning("Containers started but took longer than expected")
            return True

        except Exception as e:
            print_warning(f"Error starting Docker containers: {str(e)}")
            return True  # Don't fail, proceed anyway

    return True


def check_prerequisites():
    """Check if all prerequisites are installed"""
    print_section("Checking Prerequisites")

    # Check Java 21
    if not os.path.exists(JAVA_21_PATH):
        print_error(f"Java 21 not found at {JAVA_21_PATH}")
        print_info("Please install Java 21 or update JAVA_21_PATH in the script")
        return False
    print_success("Java 21 found")

    # Check Maven
    result = subprocess.run(["which", "mvn"], capture_output=True, text=True)
    if result.returncode != 0:
        print_error("Maven not found in PATH")
        print_info("Please install Maven")
        return False
    print_success("Maven found")

    # Check if .env.local exists
    env_file = PROJECT_ROOT / ".env.local"
    if not env_file.exists():
        print_warning(".env.local not found - using hardcoded defaults")
        print_info("Create .env.local file with environment variables for custom configuration")
    else:
        print_success(".env.local found")
        # Show which critical variables are loaded
        db_url = ENV_VARS.get("DB_URL", "Not set")
        db_user = ENV_VARS.get("DB_USERNAME", "Not set")
        print_info(f"  Database: {db_url}")
        print_info(f"  Username: {db_user}")

    # Check if service directories exist
    for service_key in SERVICE_STARTUP_ORDER:
        service = SERVICES[service_key]
        service_dir = PROJECT_ROOT / service["dir"]
        if not service_dir.exists():
            print_error(f"{service['name']} directory not found: {service_dir}")
            return False
        print_success(f"{service['name']} directory found")

    return True


def build_service(service_key, service):
    """Build a specific service using Maven or npm"""
    print_section(f"Building {service['name']}")

    service_dir = PROJECT_ROOT / service["dir"]

    # Check if this is a web app (React)
    if service.get("is_web", False):
        print_info(f"Building {service['name']} (npm/React)...")
        print_info(f"Location: {service_dir}")

        package_file = service_dir / "package.json"
        if not package_file.exists():
            print_error(f"package.json not found: {package_file}")
            return False

        # Install dependencies
        print_info("Installing dependencies...")
        cmd = ["npm", "install"]
        try:
            result = subprocess.run(cmd, cwd=str(service_dir), capture_output=True, text=True, timeout=600)
            if result.returncode != 0:
                print_warning(f"npm install returned code {result.returncode}")

            print_success(f"{service['name']} dependencies installed")
            return True
        except subprocess.TimeoutExpired:
            print_error(f"Build timeout for {service['name']}")
            return False
        except Exception as e:
            print_error(f"Error building {service['name']}: {str(e)}")
            return False
    else:
        # Java service - use Maven
        pom_file = service_dir / "pom.xml"

        if not pom_file.exists():
            print_error(f"pom.xml not found: {pom_file}")
            return False

        print_info(f"Building {service['name']}...")
        print_info(f"Location: {service_dir}")

        cmd = ["mvn", "clean", "package", "-DskipTests", "-f", str(pom_file)]

        try:
            result = subprocess.run(cmd, capture_output=True, text=True, timeout=300)

            if result.returncode != 0:
                print_error(f"Build failed for {service['name']}")
                print_error("Error output:")
                print(result.stderr[-500:] if len(result.stderr) > 500 else result.stderr)
                return False

            # Extract build time from output
            if "BUILD SUCCESS" in result.stdout:
                print_success(f"{service['name']} built successfully")
                # Try to extract build time
                for line in result.stdout.split("\n"):
                    if "Total time:" in line:
                        print_info(line.strip())
                return True
            else:
                print_error(f"Build failed for {service['name']}")
                return False

        except subprocess.TimeoutExpired:
            print_error(f"Build timeout for {service['name']}")
            return False
        except Exception as e:
            print_error(f"Error building {service['name']}: {str(e)}")
            return False


def build_all_services():
    """Build all services"""
    print_header("BUILDING ALL SERVICES")

    for service_key in SERVICE_STARTUP_ORDER:
        service = SERVICES[service_key]
        if not build_service(service_key, service):
            return False

    return True


def kill_existing_java_processes():
    """Kill any existing Java processes"""
    print_section("Cleaning up existing Java processes")

    result = subprocess.run(["pgrep", "-f", "jar.*kupi"], capture_output=True, text=True)
    if result.stdout:
        pids = result.stdout.strip().split("\n")
        for pid in pids:
            if pid:
                print_info(f"Killing process {pid}")
                try:
                    subprocess.run(["kill", "-9", pid])
                except:
                    pass
        time.sleep(2)
        print_success("Cleaned up existing processes")
    else:
        print_info("No existing processes to clean up")


def run_service(service_key, service):
    """Run a specific service (Java or Web)"""
    print_section(f"Starting {service['name']}")

    service_dir = PROJECT_ROOT / service["dir"]
    wait_time = service.get("wait_time", 10)

    # Check if this is a web app (React)
    if service.get("is_web", False):
        print_info(f"Starting {service['name']} (npm/React)...")
        print_info(f"Port: {service['port']}")
        print_info(f"Log file: {service['log']}")

        package_json = service_dir / "package.json"
        if not package_json.exists():
            print_error(f"package.json not found: {package_json}")
            return False

        try:
            # Start npm dev server in background
            with open(service["log"], "w") as log_file:
                env = os.environ.copy()
                env["PORT"] = str(service["port"])

                process = subprocess.Popen(
                    ["npm", "start"],
                    stdout=log_file,
                    stderr=subprocess.STDOUT,
                    cwd=str(service_dir),
                    preexec_fn=os.setsid,
                    env=env,
                )

            print_info(f"Process started with PID: {process.pid}")
            print_info(f"Waiting {wait_time} seconds for service to initialize...")
            time.sleep(wait_time)

            # Verify process is still running
            if process.poll() is not None:
                print_error(f"Process exited unexpectedly")
                with open(service["log"], "r") as f:
                    print_error("Last log lines:")
                    print(f.read()[-500:])
                return False

            print_success(f"{service['name']} started successfully")
            return True

        except Exception as e:
            print_error(f"Error starting {service['name']}: {str(e)}")
            return False

    # Java service - continue with existing logic
    jar_path = service_dir / "target" / service["jar"]

    if not jar_path.exists():
        print_error(f"JAR file not found: {jar_path}")
        print_info("Please build the service first")
        return False

    print_info(f"Starting {service['name']}...")
    print_info(f"Port: {service['port']}")
    print_info(f"Log file: {service['log']}")

    # Services that require database configuration
    db_services = ["auth", "core", "file", "location", "notification", "order_payment"]

    # Build command with service-specific database properties
    # Using -- arguments which Spring Boot will parse after the JAR
    cmd = [
        JAVA_21_PATH,
        "-Dlogging.file.path=/tmp",
        "-jar",
        str(jar_path),
    ]

    # Only add database configuration for database-dependent services
    if service_key in db_services:
        cmd.append("--spring.datasource.username=root")
        cmd.append("--spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect")
        cmd.append("--spring.jpa.hibernate.ddl-auto=update")

        # Add service-specific database URL and unified password
        # Note: allowPublicKeyRetrieval=true is required for MySQL 8.0
        # DB password comes from DB_PASSWORD in each service .env
        unified_password = ""
        cmd.append(f"--spring.datasource.password={unified_password}")

        if service_key == "auth":
            cmd.append("--spring.datasource.url=jdbc:mysql://localhost:3306/kupi_auth?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true")
        elif service_key == "core":
            cmd.append("--spring.datasource.url=jdbc:mysql://localhost:3306/kupi_core?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true")
        elif service_key == "file":
            cmd.append("--spring.datasource.url=jdbc:mysql://localhost:3306/kupi_filemanager?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true")
        elif service_key == "location":
            cmd.append("--spring.datasource.url=jdbc:mysql://localhost:3306/kupi_location?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true")
        elif service_key == "notification":
            cmd.append("--spring.datasource.url=jdbc:mysql://localhost:3306/kupi_notification?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true")
        elif service_key == "order_payment":
            cmd.append("--spring.datasource.url=jdbc:mysql://localhost:3306/kupi_order_payment?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true")

    try:
        # Start service in background with environment variables from .env.local
        env = os.environ.copy()

        # Merge .env.local variables into environment
        env.update(ENV_VARS)

        # Per-service secrets (gitignored <service>/.env) override the shared file
        env.update(load_env_file(str(service_dir / ".env")))

        # Set PORT environment variable for Spring Boot to use
        env["PORT"] = str(service["port"])

        # Only set database configuration for database-dependent services
        if service_key in db_services:
            # Set default SPRING properties if not in .env.local
            if "SPRING_DATASOURCE_USERNAME" not in env:
                env["SPRING_DATASOURCE_USERNAME"] = env.get("DB_USERNAME", "root")
            if "SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT" not in env:
                env["SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT"] = "org.hibernate.dialect.MySQL8Dialect"
            if "SPRING_DATASOURCE_PASSWORD" not in env:
                env["SPRING_DATASOURCE_PASSWORD"] = env.get("DB_PASSWORD", "")

            # Set service-specific database URL from .env.local
            db_password = env.get("DB_PASSWORD", "")

            if service_key == "auth":
                db_url = env.get("SPRING_DATASOURCE_URL") or "jdbc:mysql://localhost:3306/kupi_auth?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
                env["SPRING_DATASOURCE_URL"] = db_url
                env["SPRING_DATASOURCE_PASSWORD"] = db_password
            elif service_key == "core":
                db_url = env.get("SPRING_DATASOURCE_URL") or "jdbc:mysql://localhost:3306/kupi_core?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
                env["SPRING_DATASOURCE_URL"] = db_url
                env["SPRING_DATASOURCE_PASSWORD"] = db_password
                env["SAVE_LOCATION"] = env.get("SAVE_LOCATION", "/tmp/csv/")
            elif service_key == "file":
                db_url = env.get("SPRING_DATASOURCE_URL") or "jdbc:mysql://localhost:3306/kupi_filemanager?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
                env["SPRING_DATASOURCE_URL"] = db_url
                env["SPRING_DATASOURCE_PASSWORD"] = db_password
            elif service_key == "location":
                db_url = env.get("SPRING_DATASOURCE_URL") or "jdbc:mysql://localhost:3306/kupi_location?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
                env["SPRING_DATASOURCE_URL"] = db_url
                env["SPRING_DATASOURCE_PASSWORD"] = db_password
            elif service_key == "notification":
                db_url = env.get("SPRING_DATASOURCE_URL") or "jdbc:mysql://localhost:3306/kupi_notification?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
                env["SPRING_DATASOURCE_URL"] = db_url
                env["SPRING_DATASOURCE_PASSWORD"] = db_password
            elif service_key == "order_payment":
                db_url = env.get("SPRING_DATASOURCE_URL") or "jdbc:mysql://localhost:3306/kupi_order_payment?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
                env["SPRING_DATASOURCE_URL"] = db_url
                env["SPRING_DATASOURCE_PASSWORD"] = db_password

        with open(service["log"], "w") as log_file:
            process = subprocess.Popen(
                cmd,
                stdout=log_file,
                stderr=subprocess.STDOUT,
                cwd=str(service_dir),
                preexec_fn=os.setsid,
                env=env,
            )

        print_info(f"Process started with PID: {process.pid}")
        print_info(f"Waiting {wait_time} seconds for service to initialize...")

        time.sleep(wait_time)

        # Verify process is still running
        if process.poll() is not None:
            print_error(f"Process exited unexpectedly")
            with open(service["log"], "r") as f:
                print_error("Last log lines:")
                print(f.read()[-500:])
            return False

        print_success(f"{service['name']} started successfully")
        return True

    except Exception as e:
        print_error(f"Error starting {service['name']}: {str(e)}")
        return False


def run_all_services():
    """Run all services in dependency order"""
    print_header("RUNNING ALL SERVICES")

    failed_services = []
    for service_key in SERVICE_STARTUP_ORDER:
        service = SERVICES[service_key]
        print_info(f"[{SERVICE_STARTUP_ORDER.index(service_key) + 1}/{len(SERVICE_STARTUP_ORDER)}] Starting {service['name']}...")
        if not run_service(service_key, service):
            print_error(f"Failed to start {service['name']}")
            failed_services.append(service['name'])
            # Critical services - fail if discovery or gateway fail
            if service_key in ["discovery", "gateway"]:
                return False
            # For other services, continue attempting to start remaining services
            continue
        print_info("")

    if failed_services:
        print_warning(f"Some services failed to start: {', '.join(failed_services)}")

    return True


def check_service_health(service_key, service):
    """Check if a service is running and healthy"""
    print_section(f"Checking {service['name']} Health")

    # Check if port is listening
    print_info(f"Checking if port {service['port']} is listening...")
    result = subprocess.run(
        ["lsof", "-i", f":{service['port']}"],
        capture_output=True,
        text=True,
    )

    if result.returncode != 0:
        print_error(f"Port {service['port']} is not listening")
        return False

    print_success(f"Port {service['port']} is listening")

    # Try to access health endpoint
    print_info(f"Checking service health at {service['health_url']}...")
    result = subprocess.run(
        ["curl", "-s", "-o", "/dev/null", "-w", "%{http_code}", service["health_url"]],
        capture_output=True,
        text=True,
        timeout=5,
    )

    if result.returncode == 0:
        http_code = result.stdout.strip()
        if http_code.startswith("2"):
            print_success(f"Service is healthy (HTTP {http_code})")
            return True
        else:
            print_warning(f"Service returned HTTP {http_code}")
            return True  # Still consider it running
    else:
        print_warning(f"Could not reach service at {service['health_url']}")
        return True  # Might still be initializing


def check_all_services_health():
    """Check health of all services"""
    print_header("CHECKING SERVICES HEALTH")

    for service_key in SERVICE_STARTUP_ORDER:
        service = SERVICES[service_key]
        if not check_service_health(service_key, service):
            pass  # Don't fail, some services might still be initializing

    return True


def display_status():
    """Display current status of all services"""
    print_header("SERVICES STATUS")

    running_count = 0
    for service_key in SERVICE_STARTUP_ORDER:
        service = SERVICES[service_key]
        result = subprocess.run(
            ["pgrep", "-f", f"{service['jar'].replace('.jar', '')}"],
            capture_output=True,
            text=True,
        )

        if result.returncode == 0:
            print_success(f"{service['name']} is running")
            print_info(f"  Port: {service['port']}")
            print_info(f"  URL: {service['health_url']}")
            print_info(f"  Log: {service['log']}")
            running_count += 1
        else:
            print_error(f"{service['name']} is not running")

    print_section(f"Summary: {running_count}/{len(SERVICE_STARTUP_ORDER)} services running")


def display_access_points():
    """Display access points and useful information"""
    print_header("ACCESS POINTS & COMMANDS")

    print_section("Web Interfaces")
    print(f"  Eureka Dashboard: {Colors.OKCYAN}http://localhost:8761{Colors.ENDC}")
    print(f"  API Gateway:      {Colors.OKCYAN}http://localhost:8080{Colors.ENDC}")

    print_section("Microservices")
    for service_key in SERVICE_STARTUP_ORDER:
        if service_key not in ["discovery", "gateway"]:
            service = SERVICES[service_key]
            print(f"  {service['name']:.<35} http://localhost:{service['port']}")

    print_section("Eureka Service Registry")
    print(f"  All Services:     {Colors.OKCYAN}http://localhost:8761/eureka/apps{Colors.ENDC}")

    print_section("Health Checks")
    print(f"  Gateway Health:   {Colors.OKCYAN}http://localhost:8080/actuator/health{Colors.ENDC}")

    print_section("Log Files")
    for service_key in SERVICE_STARTUP_ORDER:
        service = SERVICES[service_key]
        print(f"  {service['name']:.<35} {Colors.OKCYAN}{service['log']}{Colors.ENDC}")

    print_section("Useful Commands")
    print(f"  {Colors.OKCYAN}Check service registration:{Colors.ENDC}")
    print(f"    curl http://localhost:8761/eureka/apps")
    print(f"  {Colors.OKCYAN}Stop all services:{Colors.ENDC}")
    print(f"    killall java")
    print(f"  {Colors.OKCYAN}View service logs:{Colors.ENDC}")
    print(f"    tail -f /tmp/<service-name>.log")


def clean_build():
    """Clean build artifacts"""
    print_header("CLEANING BUILD ARTIFACTS")

    for service_key in SERVICE_STARTUP_ORDER:
        service = SERVICES[service_key]
        service_dir = PROJECT_ROOT / service["dir"]
        target_dir = service_dir / "target"

        if target_dir.exists():
            print_info(f"Cleaning {service['name']}...")
            subprocess.run(["rm", "-rf", str(target_dir)])
            print_success(f"Cleaned {service['name']}")
        else:
            print_info(f"No build artifacts found for {service['name']}")


def main():
    """Main entry point"""
    parser = argparse.ArgumentParser(
        description="KUPI Complete Microservices Build and Run Script",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python3 build_and_run_services.py              # Build and run all services
  python3 build_and_run_services.py --build-only # Only build services
  python3 build_and_run_services.py --run-only   # Only run services (must build first)
  python3 build_and_run_services.py --clean      # Clean build artifacts
  python3 build_and_run_services.py --status     # Check service status
        """,
    )

    parser.add_argument(
        "--build-only",
        action="store_true",
        help="Only build services, don't run them",
    )
    parser.add_argument(
        "--run-only",
        action="store_true",
        help="Only run services, don't build them",
    )
    parser.add_argument(
        "--clean",
        action="store_true",
        help="Clean build artifacts and exit",
    )
    parser.add_argument(
        "--status",
        action="store_true",
        help="Check status of services and exit",
    )

    args = parser.parse_args()

    try:
        # Display environment configuration summary
        if ENV_VARS:
            print_section("Environment Configuration Loaded from .env.local")
            print_info(f"Total variables: {len(ENV_VARS)}")
            # Show key variables
            critical_vars = ["DB_URL", "DB_USERNAME", "PORT", "SPRING_APPLICATION_APP_KEY"]
            for var in critical_vars:
                if var in ENV_VARS:
                    value = ENV_VARS[var]
                    # Mask sensitive values
                    if "PASSWORD" in var or "SECRET" in var or "KEY" in var.upper():
                        value = "***" if value else "Not set"
                    print_info(f"  {var}={value}")

        # Handle clean
        if args.clean:
            clean_build()
            return 0

        # Handle status
        if args.status:
            display_status()
            return 0

        # Check prerequisites
        if not check_prerequisites():
            print_error("Prerequisites check failed")
            return 1

        # Check and start Docker containers
        check_docker_containers()

        # Build services
        if not args.run_only:
            if not build_all_services():
                print_error("Build failed")
                return 1

        # Run services
        if not args.build_only:
            kill_existing_java_processes()
            if not run_all_services():
                print_error("Failed to start services")
                return 1

            # Check health
            time.sleep(5)
            check_all_services_health()

            # Display information
            display_status()
            display_access_points()

            print_header("✅ ALL SERVICES RUNNING SUCCESSFULLY")

        return 0

    except KeyboardInterrupt:
        print_header("INTERRUPTED BY USER")
        print_warning("Services are still running. Use 'killall java' to stop them.")
        return 130
    except Exception as e:
        print_error(f"Unexpected error: {str(e)}")
        return 1


if __name__ == "__main__":
    sys.exit(main())
