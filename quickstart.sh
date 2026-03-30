#!/bin/bash

###############################################################################
# Swag Labs Test Automation - Jenkins Setup Quickstart
#
# This script sets up Jenkins locally using Docker and configures it to
# automatically run tests when code is pushed to your GitHub repository.
#
# Prerequisites:
#   - Docker and Docker Compose installed
#   - GitHub account with repository access
#   - Git installed locally
#
# Usage:
#   ./quickstart.sh [options]
#
# Options:
#   --with-ngrok      Include ngrok for webhook tunneling
#   --github-token    GitHub personal access token (for private repos)
#   --ngrok-token     Ngrok authtoken (required if using --with-ngrok)
#   --help            Display this help message
#
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
USE_NGROK=false
GITHUB_TOKEN=""
NGROK_TOKEN=""

# Function to print colored output
print_header() {
    echo -e "${BLUE}========== $1 ==========${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Function to display help
show_help() {
    head -20 "$0" | tail -17
}

# Parse command-line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --with-ngrok)
            USE_NGROK=true
            shift
            ;;
        --github-token)
            GITHUB_TOKEN="$2"
            shift 2
            ;;
        --ngrok-token)
            NGROK_TOKEN="$2"
            shift 2
            ;;
        --help)
            show_help
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Check if Docker and Docker Compose are installed
print_header "Checking Prerequisites"

if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker Desktop."
    exit 1
fi
print_success "Docker is installed"

if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose."
    exit 1
fi
print_success "Docker Compose is installed"

# Check Docker daemon is running
if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running. Please start Docker Desktop."
    exit 1
fi
print_success "Docker daemon is running"

# Create .env file for Docker Compose
print_header "Setting Up Environment"

if [ "$USE_NGROK" = true ]; then
    if [ -z "$NGROK_TOKEN" ]; then
        print_warning "Ngrok selected but no token provided."
        print_info "Get a free ngrok account at https://ngrok.com"
        read -p "Enter your ngrok authtoken: " NGROK_TOKEN
    fi
    echo "NGROK_AUTHTOKEN=${NGROK_TOKEN}" > .env
    print_success "Created .env file with ngrok token"
fi

# Start Jenkins
print_header "Starting Jenkins and Docker Compose"

if [ "$USE_NGROK" = true ]; then
    print_info "Starting Jenkins with ngrok tunnel..."
    docker-compose --profile ngrok up -d
else
    print_info "Starting Jenkins..."
    docker-compose up -d
fi

# Wait for Jenkins to start
print_header "Waiting for Jenkins to Start"
JENKINS_URL="http://localhost:8080"
MAX_ATTEMPTS=60
ATTEMPT=1

while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
    if curl -s "$JENKINS_URL" > /dev/null 2>&1; then
        print_success "Jenkins is running!"
        break
    fi
    
    if [ $((ATTEMPT % 10)) -eq 0 ]; then
        print_info "Waiting for Jenkins... (${ATTEMPT}/${MAX_ATTEMPTS})"
    fi
    
    sleep 2
    ATTEMPT=$((ATTEMPT + 1))
done

if [ $ATTEMPT -gt $MAX_ATTEMPTS ]; then
    print_error "Jenkins failed to start within ${MAX_ATTEMPTS} attempts"
    exit 1
fi

# Get initial admin password
print_header "Jenkins Initial Setup"
print_info "Retrieving initial admin password..."

JENKINS_CONTAINER="jenkins-ci"
INITIAL_PASSWORD=$(docker exec "$JENKINS_CONTAINER" cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null || echo "")

if [ -z "$INITIAL_PASSWORD" ]; then
    print_warning "Could not retrieve initial password automatically"
    print_info "Access Jenkins at $JENKINS_URL and check logs for password"
    print_info "Run: docker logs $JENKINS_CONTAINER"
else
    print_success "Initial Admin Password (save this!):"
    echo -e "${YELLOW}${INITIAL_PASSWORD}${NC}"
fi

# Display setup instructions
print_header "Next Steps"

echo ""
echo "1️⃣  Access Jenkins:"
echo "   URL: ${BLUE}$JENKINS_URL${NC}"
echo ""
echo "2️⃣  Complete Jenkins Setup:"
echo "   - Paste the admin password above"
echo "   - Install suggested plugins (including: Git, Pipeline, HTML Publisher, JUnit)"
echo ""
echo "3️⃣  Create a New Pipeline Job:"
echo "   - Click 'New Item'"
echo "   - Name: 'Swag Labs Tests'"
echo "   - Select 'Pipeline' → Click OK"
echo ""
echo "4️⃣  Configure Pipeline:"
echo "   - Go to 'Definition' → Select 'Pipeline script from SCM'"
echo "   - SCM: 'Git'"
echo "   - Repository URL: ${BLUE}<your-github-repo-url>${NC}"
if [ -n "$GITHUB_TOKEN" ]; then
    echo "   - Credentials: Add GitHub credentials with token: ${BLUE}${GITHUB_TOKEN}${NC}"
fi
echo "   - Script Path: 'Jenkinsfile'"
echo "   - Build Triggers: Check 'GitHub hook trigger for GITScm polling'"
echo ""
echo "5️⃣  Set up GitHub Webhook:"

if [ "$USE_NGROK" = true ]; then
    echo "   - Ngrok is running on this machine"
    echo "   - Your public URL will be shown in ngrok web UI (http://localhost:4040)"
    echo "   - GitHub Webhook URL: ${BLUE}https://<ngrok-url>/github-webhook/${NC}"
else
    echo "   - In GitHub repo → Settings → Webhooks → Add webhook"
    echo "   - Payload URL: ${BLUE}http://<your-public-ip>:8080/github-webhook/${NC}"
    echo "   - Content type: application/json"
    echo "   - Events: Push events"
fi
echo ""
echo "6️⃣  Run Your First Build:"
echo "   - Manually trigger a build in Jenkins, or"
echo "   - Push to GitHub to automatically trigger via webhook"
echo ""
echo "📊 View Reports:"
echo "   - After test runs, check 'Last Successful Artifacts' for reports"
echo ""

# Display ngrok info if enabled
if [ "$USE_NGROK" = true ]; then
    echo -e "${YELLOW}========== Ngrok Tunnel Info ==========${NC}"
    echo "Your Jenkins is exposed to the internet via ngrok!"
    echo "Access ngrok dashboard: http://localhost:4040"
    echo ""
    sleep 3
    print_info "Checking ngrok tunnel status..."
    NGROK_PUBLIC_URL=$(curl -s http://localhost:4040/api/tunnels | grep -o '"public_url":"[^"]*' | head -1 | cut -d'"' -f4 || echo "")
    if [ -n "$NGROK_PUBLIC_URL" ]; then
        echo "✅ Your public Jenkins URL: ${BLUE}${NGROK_PUBLIC_URL}${NC}"
        echo "✅ GitHub Webhook URL:      ${BLUE}${NGROK_PUBLIC_URL}/github-webhook/${NC}"
    else
        print_warning "Could not retrieve ngrok URL. Check http://localhost:4040"
    fi
    echo ""
fi

# Display Docker commands
print_header "Useful Docker Commands"

echo ""
echo "View logs:"
echo "  ${BLUE}docker-compose logs -f jenkins${NC}"
echo ""
echo "Stop Jenkins:"
echo "  ${BLUE}docker-compose down${NC}"
echo ""
echo "Remove Jenkins data (clean start):"
echo "  ${BLUE}docker-compose down -v${NC}"
echo ""
echo "Rebuild Docker image:"
echo "  ${BLUE}docker-compose up -d --build${NC}"
echo ""

print_success "Jenkins setup complete! 🚀"
