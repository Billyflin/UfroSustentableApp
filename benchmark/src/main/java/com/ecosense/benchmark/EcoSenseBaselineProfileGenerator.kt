package com.ecosense.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EcoSenseBaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun criticalUserJourneys() {
        baselineProfileRule.collect(
            packageName = PACKAGE_NAME,
            profileBlock = {
                startActivityAndWait()
                val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                uiDevice.waitForIdle()
                tapText(uiDevice, "Mapa")
                tapText(uiDevice, "Historial")
                flingVertical(uiDevice)
                tapText(uiDevice, "Grupos")
                flingVertical(uiDevice)
                tapText(uiDevice, "Perfil")
                flingVertical(uiDevice)
            }
        )
    }

    private fun tapText(device: UiDevice, text: String) {
        val objectSelector = By.text(text)
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
