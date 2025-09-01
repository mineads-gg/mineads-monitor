import com.github.spotbugs.snom.Confidence

plugins {
  id("com.diffplug.spotless")
  id("com.github.spotbugs")
}

spotbugs {
  ignoreFailures.set(true)    // bug free or it doesn't ship!
  reportLevel.set(Confidence.MEDIUM)    // low|medium|high (low = sensitive to even minor mistakes)
  omitVisitors.set(listOf("FindReturnRef")) // https://spotbugs.readthedocs.io/en/latest/detectors.html#findreturnref
}

spotless {
  java {
    target("**/gg/mineads/**")

    trimTrailingWhitespace()
    leadingTabsToSpaces(2)
    endWithNewline()

    licenseHeaderFile(rootProject.file("file_header.txt"))
  }
}
