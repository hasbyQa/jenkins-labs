# Maven + Java 17 base — handles compile and test in one image
FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

# Install Chrome and all required system dependencies
RUN apt-get update && apt-get install -y \
    wget gnupg curl unzip \
    fonts-liberation libatk-bridge2.0-0 libatk1.0-0 \
    libcups2 libdbus-1-3 libgdk-pixbuf2.0-0 libnspr4 \
    libnss3 libx11-xcb1 libxcomposite1 libxdamage1 \
    libxrandr2 xdg-utils libgbm1 libxkbcommon0 libasound2 \
    && wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" \
       > /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Verify Chrome installed — fails build immediately if something is wrong
RUN google-chrome --version

# Copy pom.xml first so Maven dependency downloads are cached as a separate layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

# CI/Docker environments are slower than local machines.
# SELENIUM_TIMEOUT overrides the 20s default — tests read this via TestConfig.
# surefire timeout gives the full fork 10 minutes before killing it.
ENV SELENIUM_TIMEOUT=30

CMD ["mvn", "test", "-B", "-Dmaven.surefire.timeout=600"]