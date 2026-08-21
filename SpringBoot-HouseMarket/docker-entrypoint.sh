#!/bin/sh
set -e

# Populate the uploads volume on first start. Docker does copy image content
# into an empty named volume, but this guard keeps initialization predictable
# when the volume is created by an older setup or manually mounted empty.
if [ ! -d /app/uploads/houses ] || [ -z "$(ls -A /app/uploads/houses 2>/dev/null)" ]; then
  mkdir -p /app/uploads
  cp -a /app/seed-uploads/. /app/uploads/
fi

exec java -jar /app/app.jar
