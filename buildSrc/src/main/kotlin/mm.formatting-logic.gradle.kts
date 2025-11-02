plugins {
  id("com.diffplug.spotless")
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
