package com.ecosense.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

internal const val PACKAGE_NAME = "com.ecosense"
internal const val TIMEOUT_MS = 5_000L

@RunWith(AndroidJUnit4::class)
class EcoSenseMacrobenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupCold() = measureStartup(StartupMode.COLD)

    @Test
    fun startupWarm() = measureStartup(StartupMode.WARM)

    @Test
    fun startupHot() = measureStartup(StartupMode.HOT)

    @Test
    fun mainNavigationFrameTiming() {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            iterations = 5,
            setupBlock = {
                pressHome()
                startActivityAndWait()
            }
        ) {
            val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            uiDevice.waitForIdle()
            tapText(uiDevice, "Mapa")
            tapText(uiDevice, "Historial")
            flingVertical(uiDevice)
            tapText(uiDevice, "Grupos")
            flingVertical(uiDevice)
            tapDescription(uiDevice, "Ver ranking")
            uiDevice.pressBack()
            tapText(uiDevice, "Perfil")
            flingVertical(uiDevice)
            tapText(uiDevice, "Premios")
            flingVertical(uiDevice)
        }
    }

    private fun measureStartup(startupMode: StartupMode) {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            startupMode = startupMode,
            iterations = 8,
            setupBlock = { pressHome() }
        ) {
            startActivityAndWait()
        }
    }

    private fun tapText(device: UiDevice, text: String) {
        val objectSelector = By.text(text)
        device.wait(Until.hasObject(objectSelector), TIMEOUT_MS)
        device.findObject(objectSelector)?.click()
        device.waitForIdle()
    }

    private fun tapDescription(device: UiDevice, description: String) {
        val objectSelector = By.desc(description)
        device.wait(Until.hasObject(objectSelector), TIMEOUT_MS)
        device.findObject(objectSelector)?.click()
        device.waitForIdle()
    }

    private fun flingVertical(device: UiDevice) {
        device.findObject(By.pkg(PACKAGE_NAME))
            ?.fling(Direction.DOWN)
        device.waitForIdle()
    }
}
