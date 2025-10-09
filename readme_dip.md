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
JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew :app:androidTestApi30 -PdeviceSerial=emulator-5554 --no-daemon
```

Скрипт‑помощник
```bash
# Run one/all tests on a specific device (default emulator-5554)
./scripts/run-android-tests.sh [serial] [posts|click|about|all]
```

Запуск UI приложения на эмуляторе
```bash
/Users/borodovskikhmail.ru/Library/Android/sdk/platform-tools/adb -s emulator-5554 \
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
    ./scripts/run-android-tests.sh emulator-5554 posts
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
    ./scripts/run-android-tests.sh emulator-5554 click
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
    ./scripts/run-android-tests.sh emulator-5554 about
    ```
  - Ожидаемый результат: открывается `AboutActivity`, выбирается пункт меню `R.string.action_about`.

- Все три теста сразу
  - Gradle (весь класс):
    ```bash
    JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew :app:connectedDebugAndroidTest \
      -Pandroid.testInstrumentationRunnerArguments.class=dev.shreyaspatil.foodium.ui.main.MainActivityTest --no-daemon
    ```
  - Скрипт:
    ```bash
    ./scripts/run-android-tests.sh emulator-5554 all
    ```

### Покрытие кода (Jacoco)

- Юнит‑тесты (HTML/XML отчёт):
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew :app:jacocoUnitTestReport --no-daemon
```
Отчёт: `app/build/reports/jacoco/jacocoUnitTestReport/html/index.html`
Копия в корне: `reports/jacoco/unit/index.html`

- Инструментальные тесты (HTML/XML отчёт):
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew :app:jacocoAndroidTestReport --no-daemon
```
Отчёт: `app/build/reports/jacoco/jacocoAndroidTestReport/html/index.html`
Копия в корне: `reports/jacoco/androidTest/index.html`

### Отчёты Allure

- Юнит‑тесты → HTML отчёт Allure и копия в корень:
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew \
  :app:testDebugUnitTest \
  :app:generateAllureUnitReport \
  :app:copyAllureUnitReportsToRoot --no-daemon
```
Отчёт: `app/build/reports/allure/unit/index.html`
Копия в корне: `reports/allure/unit/index.html`

- Инструментальные тесты (вариант через connected):
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew \
  :app:connectedDebugAndroidTest \
  :app:pullAndroidTestAllureResults \
  :app:generateAllureAndroidTestReport \
  :app:copyAllureReportsToRoot --no-daemon
```
Отчёт: `app/build/reports/allure/androidTest/index.html`
Копия в корне: `reports/allure/androidTest/index.html`

- Инструментальные тесты (вариант direct `am instrument`, укажи serial):
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 11) ./gradlew \
  :app:installDebugAndTestsOnDevice \
  :app:disableDeviceAnimations \
  :app:runMainActivityTestOnDevice \
  :app:pullAndroidTestAllureResultsFromAm \
  :app:generateAllureAndroidTestReportFromAm \
  :app:copyAllureAndroidTestReportsToRootFromAm \
  -PdeviceSerial=emulator-5554 --no-daemon
```


