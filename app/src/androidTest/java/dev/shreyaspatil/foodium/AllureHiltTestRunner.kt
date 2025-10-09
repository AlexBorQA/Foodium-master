package dev.shreyaspatil.foodium

import android.app.Application
import android.content.Context
import io.qameta.allure.android.runners.AllureAndroidJUnitRunner

class AllureHiltTestRunner : AllureAndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application {
        return super.newApplication(cl, "dagger.hilt.android.testing.HiltTestApplication", context)
    }
}


