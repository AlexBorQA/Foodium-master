### Сборка и тесты (macOS)

Предварительные требования
- Требуется JDK 11 (wrapper выставляет JAVA_HOME на JDK 11).
- Установлен Android SDK; рекомендуется эмулятор AVD `Pixel_2_API_30`.

Сборка
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew :app:assembleDebug --no-daemon
JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew :app:assembleRelease --no-daemon
```

Артефакты
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release (unsigned): `app/build/outputs/apk/release/app-release-unsigned.apk`
- Prod: `app/prod/` содержит debug и подписанный release (debug keystore)

Юнит‑тесты
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew :app:testDebugUnitTest --no-daemon
```

Инструментальные тесты (Espresso)

- Все тесты на подключённом устройстве/эмуляторе:
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew :app:connectedDebugAndroidTest --no-daemon
```

- Только класс `MainActivityTest`:
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=dev.shreyaspatil.foodium.ui.main.MainActivityTest --no-daemon
```

- Один тест‑метод (без кавычек в zsh):
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=dev.shreyaspatil.foodium.ui.main.MainActivityTest \
  -Pandroid.testInstrumentationRunnerArguments.method=aboutMenu_opens_AboutActivity --no-daemon
```

Запуск на API 30 (c отключением анимаций)
```bash
# Uses Gradle helper tasks added in app/build.gradle.kts
JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew :app:androidTestApi30 -PdeviceSerial=emulator-5556 --no-daemon
```

Скрипт‑помощник
```bash
# Run one/all tests on a specific device (default emulator-5556)
./scripts/run-android-tests.sh [serial] [posts|click|about|all]
```

Запуск UI приложения на эмуляторе
```bash
/Users/borodovskikhmail.ru/Library/Android/sdk/platform-tools/adb -s emulator-5556 \
  shell am start -n dev.shreyaspatil.foodium/.ui.main.MainActivity
```

### Инструментальные тесты: список и запуск

- Имя: postsRecyclerView_isDisplayed_onLaunch
  - Класс: dev.shreyaspatil.foodium.ui.main.MainActivityTest
  - Запуск (Gradle):
    ```bash
    JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew :app:connectedDebugAndroidTest \
      -Pandroid.testInstrumentationRunnerArguments.class=dev.shreyaspatil.foodium.ui.main.MainActivityTest \
      -Pandroid.testInstrumentationRunnerArguments.method=postsRecyclerView_isDisplayed_onLaunch --no-daemon
    ```
  - Запуск (скрипт):
    ```bash
    ./scripts/run-android-tests.sh emulator-5556 posts
    ```
  - Ожидаемый результат: элемент `R.id.postsRecyclerView` отображается на экране.

- Имя: clickFirstItem_opens_PostDetailsActivity
  - Класс: dev.shreyaspatil.foodium.ui.main.MainActivityTest
  - Запуск (Gradle):
    ```bash
    JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew :app:connectedDebugAndroidTest \
      -Pandroid.testInstrumentationRunnerArguments.class=dev.shreyaspatil.foodium.ui.main.MainActivityTest \
      -Pandroid.testInstrumentationRunnerArguments.method=clickFirstItem_opens_PostDetailsActivity --no-daemon
    ```
  - Запуск (скрипт):
    ```bash
    ./scripts/run-android-tests.sh emulator-5556 click
    ```
  - Ожидаемый результат: открывается `PostDetailsActivity`, виден `R.id.post_title`.

- Имя: aboutMenu_opens_AboutActivity
  - Класс: dev.shreyaspatil.foodium.ui.main.MainActivityTest
  - Запуск (Gradle):
    ```bash
    JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew :app:connectedDebugAndroidTest \
      -Pandroid.testInstrumentationRunnerArguments.class=dev.shreyaspatil.foodium.ui.main.MainActivityTest \
      -Pandroid.testInstrumentationRunnerArguments.method=aboutMenu_opens_AboutActivity --no-daemon
    ```
  - Запуск (скрипт):
    ```bash
    ./scripts/run-android-tests.sh emulator-5556 about
    ```
  - Ожидаемый результат: открывается `AboutActivity`, отображается строка `R.string.about_us_title`.

- Все три теста сразу
  - Gradle (весь класс):
    ```bash
    JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew :app:connectedDebugAndroidTest \
      -Pandroid.testInstrumentationRunnerArguments.class=dev.shreyaspatil.foodium.ui.main.MainActivityTest --no-daemon
    ```
  - Скрипт:
    ```bash
    ./scripts/run-android-tests.sh emulator-5556 all
    ```


