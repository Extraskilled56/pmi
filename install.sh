#!/bin/bash

# Exit on error
set -e
# Exit on undefined variable
set -u
# Exit on pipe failure
set -o pipefail

# check for root
if [ "$EUID" -ne 0 ]; then
    echo "Please run as root"
    exit 1
fi

echo "Installing dependencies"
apt update && apt upgrade -y
apt install git snapd curl zip unzip -y || {
    echo "Failed to install dependencies"
    exit 1
}

# Install SDKMAN and required tools
echo "Installing SDKMAN..."
curl -s "https://get.sdkman.io" | bash || {
    echo "Failed to install SDKMAN"
    exit 1
}

echo "Installing Java and Kotlin..."
bash -c "source \"$HOME/.sdkman/bin/sdkman-init.sh\" && \
    sdk install java 22.3.r11-grl && \
    sdk install kotlin" || {
    echo "Failed to install Java or Kotlin"
    exit 1
}

# Install GraalVM native-image
echo "Installing GraalVM native-image..."
bash -c "source \"$HOME/.sdkman/bin/sdkman-init.sh\" && \
    gu install native-image" || {
    echo "Failed to install native-image"
    exit 1
}

echo "Compiling"
mkdir -p .pmi
kotlinc src/Main.kt src/Utils.kt src/Tasks.kt -include-runtime -d .pmi/build.jar || {
    echo "Compilation failed"
    exit 1
}

if [ -f .pmi/build.jar ]; then
    echo "Creating native image..."
    bash -c "source \"$HOME/.sdkman/bin/sdkman-init.sh\" && \
        native-image -jar .pmi/build.jar" || {
        echo "Native image creation failed"
        exit 1
    }
    
    if [ -f "build" ]; then
        echo "Installing..."
        # remove old files
        rm -rf /usr/bin/pmi
        rm -rf /usr/bin/.pmi
        mv build /usr/bin/pmi
        mv .pmi /usr/bin/
        echo "Installation completed successfully"
    else
        echo "Native image file not found"
        exit 1
    fi
else
    echo "Jar file not found"
    exit 1
fi

echo "Done"