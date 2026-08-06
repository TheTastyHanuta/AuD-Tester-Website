#!/usr/bin/env bash
# Deploy. Run from anywhere: /path/to/AuD-Tester-Website/deploy.sh
#
# The node version comes from .nvmrc, which must match the `interpreter` pm2
# starts the app with. Native modules (better-sqlite3) compile against the ABI
# of whichever node runs `npm ci` - if that differs from the node pm2 uses,
# the install succeeds but the app crashes at require time.
cd "$(dirname "$0")"

export NVM_DIR="${NVM_DIR:-$HOME/.nvm}"
. "$NVM_DIR/nvm.sh"
nvm use || exit 1

set -euo pipefail

git pull
npm ci

# Fails if a dependency's install script was blocked by npm's allowScripts
# and the native binding never got built. Keeps the running app up instead
# of restarting into a crash loop.
node -e "require('better-sqlite3')"

pm2 restart aud-website aud-worker
