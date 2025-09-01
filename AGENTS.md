# MineAds Monitor - Agent Guidelines

## Build Commands

- **Build project**: `./gradlew build`
- **Run tests**: `./gradlew test`
- **Run single test**: `./gradlew test --tests "*TestClass*"`
- **Clean build**: `./gradlew clean build`
- **Check formatting**: `./gradlew spotlessCheck`
- **Apply formatting**: `./gradlew spotlessApply`

## Code Style Guidelines

### Formatting

- **Indentation**: 2 spaces (no tabs)
- **Line endings**: LF (Unix)
- **Encoding**: UTF-8
- **Max line length**: No limit
- **Trailing whitespace**: Trimmed automatically
- **Final newline**: Required

### Java Conventions

- **Language version**: Java 21
- **Imports**: Organize automatically
- **Annotations**: Use JetBrains null annotations via Lombok
- **License header**: Required on all source files (auto-applied)
- **Simple blocks**: Keep in one line where possible
- **Error handling**: Use try-with-resources, avoid checked exceptions in APIs

### Naming

- **Packages**: `gg.mineads.monitor.*` hierarchy
- **Classes**: PascalCase
- **Methods/Fields**: camelCase
- **Constants**: UPPER_SNAKE_CASE

### Dependencies

- **Minecraft platforms**: Paper, Velocity, BungeeCord
- **Command framework**: Cloud Commands
- **Text**: Kyori Adventure API
- **Config**: ConfigLib YAML
- **Serialization**: Gson, MsgPack
- **Permissions**: LuckPerms API

### Testing

- **Framework**: JUnit 5
- **Mocking**: Mockito
- **Test fixtures**: Available for shared components
- **Parallel execution**: Enabled with CPU-based fork count

### Static Analysis

- **ErrorProne**: Enabled for compile-time checks
- **SpotBugs**: Configured but commented out
- **Compiler warnings**: Most suppressed except critical ones
