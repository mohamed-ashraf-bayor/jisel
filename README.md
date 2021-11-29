## JISEL: Java Interface Segregation Library
Interface Segregation Library for Java 17

Pitch Video:
...

### How to Install ?

If you are running a Maven project, add the latest release dependency to your pom.xml
```xml
<dependency>
    <groupId>org.jisel</groupId>
    <artifactId>jisel</artifactId>
    <version>1.0</version>
</dependency>
``` 
For other build tools, please check: [Maven Central](https://search.maven.org/artifact/org.jisel/jisel/1.0/jar).

### Use on your declared bloated interfaces methods

```java
import SealForProfile;

public interface InterfaceA {
    @SealForProfile
    void something();
}
```

### Use on your declared child classes implementing generated sealed interfaces

```java
import AddToProfile;

@AddToProfile("PROFILE_NAME")
public final class ClientA implements SealedInterfaceA {
    // ...
}
```

### Sample classes for testing
[https://github.com/mohamed-ashraf-bayor/jisel-annotation-client](https://github.com/mohamed-ashraf-bayor/jisel-annotation-client)

### Invalid Uses of Jisel
The annotation should be used ONLY on interfaces created in your own project.

### Issues, Bugs, Suggestions
Contribute to the project's growth by reporting issues or making improvement suggestions [here](https://github.com/mohamed-ashraf-bayor/jisel/issues/new/choose)
