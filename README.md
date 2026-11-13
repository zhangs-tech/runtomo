# Runtomo

Runtomo is a fitness app with gacha and companions.
Daily steps convert to in-game currency for new characters.
Walk or run with your companion to unlock new characters via gacha.

## Project Structure

- `android/`: Jetpack Compose Android app.
- `server/`: Kotlin Spring Boot 4.1 backend with Flyway and PostgreSQL 18.
- `docs/`: Project documentation:
  - [erd.md](docs/erd.md): Database schema.
  - [flow.md](docs/flow.md): App flow notes.
  - [usecases/](docs/usecases): MVP use cases.
  - [tomos/](docs/tomos): Companion species specs.
  - [foods/](docs/foods): Food item specs.
- [RunTomo_Idea.md](RunTomo_Idea.md): Full game design and roadmap.

## Android

```sh
cd android
```

### Dependencies

- Android SDK 37 (`compileSdk 37`, `targetSdk 37`, `minSdk 36`)
- Java 11 compatibility

### 1. Setup Environment Variables

Open the `android` directory in Android Studio to generate `local.properties` with `sdk.dir`, or create it manually:

```properties
# Android SDK path
sdk.dir=/path/to/android/sdk
```

Copy `secrets.properties.example` to `secrets.properties` and set your local values:

```sh
cp secrets.properties.example secrets.properties
```

- `runtomo.googleWebClientId`: Google OAuth web client ID (must match `GOOGLE_WEB_CLIENT_ID` on the server).
- `runtomo.apiBaseUrl`: Backend URL (`http://10.0.2.2:8080/` for the emulator, or `http://localhost:8080/` with `adb reverse tcp:8080 tcp:8080`).

### 2. Build and Test

We recommend using Android Studio, or run Gradle from the command line:

```sh
./gradlew assembleDebug
./gradlew test
```

### 3. Testing with Android Activity

It is impractical to test by walking with a physical device.
Use [Health Connect Toolbox](https://developer.android.com/health-and-fitness/health-connect/test/health-connect-toolbox) to write test step data.

Usually `adb` is not in `PATH`, depending on your system.

## Spring Server

```sh
cd server
```

### Dependencies

- Docker (for PostgreSQL 18)
- Java 25

We used [sdkman](https://sdkman.io/install/) to install Java 25 (Temurin):

```sh
sdk install java 25.0.2-tem
```

### 1. Setup Environment Variables

Copy `.env.example` to `.env` and fill in your values:

```sh
cp .env.example .env
```

Generate an RSA key pair for JWT signing (matching `JWT_PUBLIC_KEY` and `JWT_PRIVATE_KEY` in `.env`), for example in `server/keys/`:

```sh
mkdir -p keys
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out keys/app.key
openssl rsa -pubout -in keys/app.key -out keys/app.pub
```

### 2. Run and Test

Start PostgreSQL (listens on port `5433`):

```sh
docker compose up -d
```

Run the server or the test suite:

```sh
./gradlew bootRun
./gradlew test
```

## Google Authorization

1. Go to Cloud Console https://console.cloud.google.com/auth/clients
2. Create new Web Application client -> get client ID for GOOGLE_WEB_CLIENT_ID and runtomo.googleWebClientId
3. Get SHA-1 fingerprint of your debug keystore:

```sh
keytool -list -v -keystore ~/.android/debug.keystore
```

Default password is `android`. The `debug.keystore` location might be different depending on your OS.

4. Create new Android Application client, insert the obtained SHA-1 fingerprint and package name `com.zhangs_tech.runtomo`

