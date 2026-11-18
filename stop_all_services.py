#!/usr/bin/env python3
"""
KUPI Complete Microservices Stop Script
Stops all running KUPI microservices and Docker containers

Usage:
    python3 stop_all_services.py              # Stop all services and Docker
    python3 stop_all_services.py --services-only  # Stop only Java services (keep Docker)
    python3 stop_all_services.py --docker-only    # Stop only Docker containers (keep Java)
    python3 stop_all_services.py --help       # Show help
"""

import os
import sys
import subprocess
import time
import argparse

# Color codes for terminal output
class Colors:
    HEADER = '\033[95m'
    BLUE = '\033[94m'
    CYAN = '\033[96m'
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'

def print_header(text):
    """Print a section header"""
    print(f"\n{Colors.HEADER}{Colors.BOLD}{'='*80}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{text.center(80)}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{'='*80}{Colors.ENDC}\n")

def print_section(text):
    """Print a subsection"""
    print(f"\n{Colors.BLUE}{Colors.BOLD}{text}{Colors.ENDC}")
    print(f"{Colors.BLUE}{'-' * len(text)}{Colors.ENDC}")

def print_success(text):
    """Print success message"""
    print(f"{Colors.GREEN}✓ {text}{Colors.ENDC}")

def print_error(text):
    """Print error message"""
    print(f"{Colors.RED}✗ {text}{Colors.ENDC}")

def print_info(text):
    """Print info message"""
    print(f"{Colors.CYAN}ℹ {text}{Colors.ENDC}")

def print_warning(text):
    """Print warning message"""
    print(f"{Colors.YELLOW}⚠ {text}{Colors.ENDC}")

def stop_java_services():
    """Stop all running Java services and web apps"""
    print_section("Stopping Java Services and Web Apps")

    try:
        # Check if any Java processes are running
        result = subprocess.run(
            ["pgrep", "-l", "java"],
            capture_output=True,
            text=True
        )

        if result.returncode == 0:
            processes = result.stdout.strip().split('\n')
            print_info(f"Found {len(processes)} Java process(es)")

            # Kill all Java processes
            subprocess.run(["killall", "java"], check=False)
            print_success("All Java services stopped")
        else:
            print_warning("No Java services are currently running")

        # Check if any npm processes are running
        result = subprocess.run(
            ["pgrep", "-l", "npm"],
            capture_output=True,
            text=True
        )

        if result.returncode == 0:
            processes = result.stdout.strip().split('\n')
            print_info(f"Found {len(processes)} npm process(es)")

            # Kill all npm processes
            subprocess.run(["killall", "npm"], check=False)
            print_success("All npm services stopped")
        else:
            print_info("No npm services are currently running")

        # Also check for node processes (React dev server)
        result = subprocess.run(
            ["pgrep", "-l", "node"],
            capture_output=True,
            text=True
        )

        if result.returncode == 0:
            processes = result.stdout.strip().split('\n')
            print_info(f"Found {len(processes)} node process(es)")

            # Kill all node processes
            subprocess.run(["killall", "node"], check=False)
            print_success("All node processes stopped")
        else:
            print_info("No node processes are currently running")

        # Wait for processes to terminate
        time.sleep(2)

    except Exception as e:
        print_error(f"Error stopping services: {str(e)}")
        return False

    return True

def stop_docker_services():
    """Stop Docker containers"""
    print_section("Stopping Docker Containers")

    try:
        # Check if docker-compose is installed
        result = subprocess.run(
            ["which", "docker-compose"],
            capture_output=True,
            text=True
        )
        if result.returncode != 0:
            print_warning("docker-compose not found - skipping Docker container shutdown")
            return True  # Don't fail, proceed anyway

        # Check if docker-compose.yml exists
        if not os.path.exists("docker-compose.yml"):
            print_warning("docker-compose.yml not found - skipping Docker container shutdown")
            return True  # Don't fail, proceed anyway

        # Check if any Docker containers are running
        check_result = subprocess.run(
            ["docker-compose", "ps"],
            capture_output=True,
            text=True
        )

        if "Up" not in check_result.stdout:
            print_info("No Docker containers running")
            return True

        # Stop and remove containers
        print_info("Stopping Docker containers...")
        result = subprocess.run(
            ["docker-compose", "down"],
            capture_output=True,
            text=True,
            timeout=30
        )

        if result.returncode == 0:
            print_success("Docker containers stopped successfully")
            time.sleep(1)
        else:
            print_warning(f"docker-compose down returned non-zero exit code: {result.returncode}")
            if result.stderr:
                print_warning(f"Error output: {result.stderr}")
            # Don't fail on this, containers might still be stopping
            return True

    except subprocess.TimeoutExpired:
        print_warning("Docker container shutdown timed out - containers may still be stopping")
        return True  # Don't fail
    except Exception as e:
        print_warning(f"Error stopping Docker services: {str(e)}")
        return True  # Don't fail, continue cleanup

    return True

def get_service_status():
    """Get status of running services"""
    print_section("Service Status Check")

    try:
        # Check Java processes
        result = subprocess.run(
            ["pgrep", "-l", "java"],
            capture_output=True,
            text=True
        )

        if result.returncode == 0:
            java_count = len(result.stdout.strip().split('\n'))
            print_warning(f"{java_count} Java process(es) still running")
        else:
            print_success("No Java services running")

        # Check npm processes
        result = subprocess.run(
            ["pgrep", "-l", "npm"],
            capture_output=True,
            text=True
        )

        if result.returncode == 0:
            npm_count = len(result.stdout.strip().split('\n'))
            print_warning(f"{npm_count} npm process(es) still running")
        else:
            print_success("No npm processes running")

        # Check node processes
        result = subprocess.run(
            ["pgrep", "-l", "node"],
            capture_output=True,
            text=True
        )

        if result.returncode == 0:
            node_count = len(result.stdout.strip().split('\n'))
            print_warning(f"{node_count} node process(es) still running")
        else:
            print_success("No node processes running")

        # Check Docker containers
        result = subprocess.run(
            ["docker-compose", "ps"],
            capture_output=True,
            text=True,
            cwd="."
        )

        if result.returncode == 0:
            if "Up" in result.stdout:
                print_warning("Docker containers still running:")
                for line in result.stdout.split('\n'):
                    if line.strip() and "Container" not in line:
                        print_info(f"  {line}")
            else:
                print_success("No Docker containers running")
        else:
            print_success("No Docker containers running")
    except Exception as e:
        print_info(f"Could not check service status: {str(e)}")

def main():
    """Main function"""
    parser = argparse.ArgumentParser(
        description="KUPI Microservices Stop Script",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python3 stop_all_services.py              # Stop all services and Docker
  python3 stop_all_services.py --services-only   # Stop only Java services
  python3 stop_all_services.py --docker-only     # Stop only Docker containers
        """
    )

    parser.add_argument(
        "--services-only",
        action="store_true",
        help="Stop only Java services (keep Docker running)"
    )
    parser.add_argument(
        "--docker-only",
        action="store_true",
        help="Stop only Docker containers (keep Java services running)"
    )

    args = parser.parse_args()

    print_header("KUPI MICROSERVICES STOP SCRIPT")

    success = True

    # Determine what to stop
    stop_services = not args.docker_only  # Stop services unless --docker-only
    stop_docker = not args.services_only  # Stop docker unless --services-only

    # Stop services
    if stop_services:
        if not stop_java_services():
            success = False

    # Stop Docker
    if stop_docker:
        if not stop_docker_services():
            success = False

    # Check final status
    get_service_status()

    print_header("STOP COMPLETE")

    if success:
        print_success("All services stopped successfully")
        print_info("To start services again, run:")
        print_info("  python3 build_and_run_services.py")
        print()
        return 0
    else:
        print_error("Some services failed to stop")
        print()
        return 1

if __name__ == "__main__":
    sys.exit(main())
