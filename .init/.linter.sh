#!/bin/bash
cd /home/kavia/workspace/code-generation/fitview-tv-40940-40949/fitness_tv_frontend
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

