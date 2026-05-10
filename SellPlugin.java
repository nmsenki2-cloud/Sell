name: Build & Release

on:
  push:
    branches: [ main ]
    tags:
      - 'v*'
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout kód
        uses: actions/checkout@v4

      - name: Java 17 beállítása
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Build Maven-nel
        run: mvn clean package -B

      - name: JAR fájl feltöltése artifact-ként
        uses: actions/upload-artifact@v4
        with:
          name: SellPlugin-${{ github.sha }}
          path: target/SellPlugin-*.jar
          retention-days: 30

  release:
    needs: build
    runs-on: ubuntu-latest
    if: startsWith(github.ref, 'refs/tags/v')

    steps:
      - name: Checkout kód
        uses: actions/checkout@v4

      - name: Java 17 beállítása
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Build Maven-nel
        run: mvn clean package -B

      - name: GitHub Release létrehozása
        uses: softprops/action-gh-release@v2
        with:
          files: target/SellPlugin-*.jar
          generate_release_notes: true
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
