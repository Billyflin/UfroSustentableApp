package com.example.ufrosustentableapp
import org.junit.runner.RunWith
import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
@RunWith(Cucumber::class)
@CucumberOptions(
    features = ["src/test/resources/features"],
    glue = ["com.example.ufrosustentableapp.steps"],
    plugin = ["pretty", "html:build/reports/cucumber/cucumber-report.html"]
)
class RunCucumberTest
