# MineAds Monitor - Agent Guidelines

## Build Commands
- **Build project**: `./gradlew build`
- **Run all tests**: `./gradlew test`
- **Run single test**: `./gradlew test --tests "*TestClass*" --tests "*TestMethod*"`
- **Run tests in module**: `./gradlew :shared:test --tests "*BatchProcessorTest*"`
- **Clean build**: `./gradlew clean build`
- **Check formatting**: `./gradlew spotlessCheck`
- **Apply formatting**: `./gradlew spotlessApply`

## Code Style Guidelines

### Java Conventions

- **Language version**: Java 21 with toolchain
- **Annotations**: JetBrains null annotations via Lombok
- **License header**: Auto-applied from `file_header.txt`
- **Error handling**: Use try-with-resources, avoid checked exceptions in APIs
- **Compiler flags**: `-parameters`, `-nowarn`, suppress deprecation/processing warnings

### Formatting (EditorConfig)
- **Indentation**: 2 spaces (no tabs)
- **Line endings**: LF (Unix)
- **Encoding**: UTF-8
- **Max line length**: No limit
- **Trailing whitespace**: Trimmed automatically
- **Final newline**: Required
- **Simple blocks**: Keep in one line where possible

### Naming Conventions
- **Packages**: `gg.mineads.monitor.*` hierarchy
- **Classes**: PascalCase
- **Methods/Fields**: camelCase
- **Constants**: UPPER_SNAKE_CASE

### Dependencies & Frameworks
- **Minecraft platforms**: Paper, Velocity, BungeeCord
- **Command framework**: Cloud Commands (v2.0.0)
- **Text**: Kyori Adventure API (v4.24.0)
- **Config**: ConfigLib YAML (v4.6.1)
- **Serialization**: Gson (v2.13.1), MsgPack (v0.9.10)
- **Permissions**: LuckPerms API (v5.5)

### Testing

- **Framework**: JUnit 5 with Jupiter
- **Mocking**: Mockito (v5.19.0)
- **Test fixtures**: Available for shared components
- **Parallel execution**: CPU-based fork count
- **Reports**: JUnit XML and HTML enabled

### Static Analysis

- **ErrorProne**: Enabled for compile-time checks (v2.41.0)
- **SpotBugs**: Configured but disabled (confidence: MEDIUM)
- **Compiler warnings**: Most suppressed except critical ones
