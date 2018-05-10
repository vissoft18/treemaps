#!/bin/bash

mkdir bin

# Compile https://github.com/EduardoVernier/dynamic-map
javac -d bin $(find ./code/dynamic-map -name "*.java")

# Compile https://github.com/EduardoVernier/insertion-treemap
javac -d bin $(find ./code/insertion-treemap -name "*.java")

# Compile Incremental, OTPBSS, Hilbert and Moore (get link to repo)
javac -classpath "./code/StableTreemap/libraries/opencsv-3.7.jar:./code/StableTreemap/libraries/Jama-1.0.3.jar" $(find ./code/StableTreemap/ -name "*.java")
