# JISEL - Java Interface Segregation Library


> ### JISEL 1.2 released:
> - Added new annotations (**@UnSeal**, **@Detach** and **@DetachAll**) allowing to generate pre-java 17 "unsealed" interfaces during the Segregation process
> - Existing Annotations on top of interfaces, methods or parameters are now replicated in the generated interfaces or classes 
> - Bug fixes and improvements


<br>

## Quick Overview

Integrating Jisel with Spring: Segregation of a Spring Data JPA Repository - [PDF](https://github.com/mohamed-ashraf-bayor/jisel-integration-with-spring/blob/master/Jisel_Integration_with_SpringDataJPA.pdf)

v1.2: UnSeal & Detach - [https://youtu.be/HOssFTKPQRM](https://youtu.be/HOssFTKPQRM)

v1.1 Quick Intro - [https://youtu.be/cbYdt8NRUaM](https://youtu.be/cbYdt8NRUaM)

Project's Pitch (v1.0) - [https://youtu.be/nkbu6zxV3R0](https://youtu.be/nkbu6zxV3R0)


<br>

## Installation

If you are running a Maven project, add the latest release dependency to your pom.xml
```xml
<dependency>
    <groupId>org.jisel</groupId>
    <artifactId>jisel</artifactId>
    <version>1.2</version>
</dependency>
``` 
You will also need to include the same dependency as an additional annotation processor in the Maven Compiler plugin of your project
```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${maven-compiler-plugin.version}</version>
                <configuration>
                    <release>17</release>
                    <compilerArgs>-Xlint:unchecked</compilerArgs>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.jisel</groupId>
                            <artifactId>jisel</artifactId>
                            <version>1.2</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

For other build tools, please check: [Maven Central](https://search.maven.org/artifact/org.jisel/jisel/1.2/jar).


<br>

## Provided Annotations

### @TopLevel
- **MANDATORY** annotation, to be applied only on top of abstract methods of the large interface you intend to segregate.<br>
- Allows you to specify methods which should be part of the top-level parent interface generated during segregation.<br>
- As a result, a sealed interface will be generated following the naming convention:
  **Sealed&#60;LargeInterfaceSimpleName&#62;** (**&#60;LargeInterfaceSimpleName&#62;** corresponds to the simplename of the interface being segregated).<br>
- The generated sealed interface will contain all abstract methods annotated with **&#64;TopLevel**.<br>
- Also, any other Jisel annotation combined with **&#64;TopLevel** on the same abstract method, will be ignored in the processing.

### @SealFor
- Annotation to be applied only on top of abstract methods of an interface you intend to segregate.<br>
- Picked up and processed **ONLY** if at least 1 of the abstract methods of the large interface has been annotated with **&#64;TopLevel**.<br>
- Ignored if combined with **&#64;TopLevel** on the same abstract method.<br>
- Expects an array of String values corresponding to the list of profiles you want to seal the method for.<br>
- For each one of the specified profile names, a sealed interface will be generated following the naming convention **Sealed&#60;ProfileName&#62;&#60;LargeInterfaceSimpleName&#62;**(**&#60;LargeInterfaceSimpleName&#62;** corresponds to the simplename of the interface being segregated).
```java
public interface Sociable {

    String STUDENT = "Student";
    String WORKER = "Worker";
    String ACTIVE_WORKER = "ActiveWorker";

    @TopLevel
    String startConversation() throws IllegalStateException;

    @SealFor(STUDENT)
    boolean attendClass(String fieldOfStudy) throws IllegalArgumentException;

    @SealFor(STUDENT)
    void askForHelpWhenNeeded();

    @SealFor({WORKER, ACTIVE_WORKER})
    boolean[] joinOfficeSocialGroups(String[] groups, int maximum);

    @SealFor(ACTIVE_WORKER)
    void leadOfficeSocialGroup(String groupName);

    @SealFor(ACTIVE_WORKER)
    double createNewOfficeSocialGroup(String groupName, List<String> starters) throws ArithmeticException;
}
```

### @AddTo
Annotation to be applied on top of a class, interface or record, which is implementing or extending a sealed interface generated by Jisel.<br>
Expects 2 attributes:
- **profiles**: OPTIONAL - array of String values corresponding to the list of profiles whose generated sealed interfaces are implemented by the annotated class, interface or record.<br>
  If not provided or empty, the annotated class, interface or record will be added to the permits list of the generated top-level parent sealed interface.<br>
- **largeInterface**: **MANDATORY** - _.class_ representation of the large interface. That would be the **&#60;LargeInterfaceSimpleName&#62;** as seen in the sealed interface name convention, followed by "_.class_".<br>
```java
@AddTo(profiles = {STUDENT, WORKER}, largeInterface = Sociable.class)
public final class StudentWorkerHybrid implements SealedStudentSociable, SealedWorkerSociable {
    @Override
    public String startConversation() throws IllegalStateException {
        return null;
    }

    @Override
    public void askForHelpWhenNeeded() {
    }

    @Override
    public boolean attendClass(String param0) throws IllegalArgumentException {
        return false;
    }

    @Override
    public boolean[] joinOfficeSocialGroups(String[] param0, int param1) {
        return new boolean[0];
    }
}
```

<br>

### @UnSeal
Annotation to be applied only on top of large interfaces to segregate. <br>
Generates a classic pre-java 17 interfaces hierarchy, which is basically the Interface Segregation Principle applied without sealing the hierarchy. <br>
The unsealed hierarchy interfaces are generated additionally to the sealed hierarchy generated files, and stored in the created _unsealed_ sub-package. Each one of the generated interfaces follows the naming convention: <ProfileName><LargeInterfaceSimpleName> (<LargeInterfaceSimpleName> is the simplename of the large interface being segregated).<br>
**Note:** This annotation will not work if **&#64;TopLevel** is NOT used anywhere within the large interface.
<br>

```java
@UnSeal
public interface Sociable {

  String STUDENT = "Student";

  @TopLevel
  String startConversation() throws IllegalStateException;

  @SealFor(STUDENT)
  boolean attendClass(String fieldOfStudy) throws IllegalArgumentException;
  ...
}
```

<br>

### @Detach
Repeatable annotation to apply on top of a large interface being segregated. <br>
Expects a mandatory **profile** attribute String value corresponding to one of the profiles provided using the **&#64;SealFor** annotation. <br>
Result will be the generation of an (unsealed) interface for the specified profile. The generated interface contains all abstract methods which have been tagged for the specified profile (through the use of **&#64;SealFor**).<br>
Also, as the generated interface is "detached" from the generated sealed hierarchy, no inheritance declaration clause (_extends_) is generated. <br>
Flexibility is offered, allowing to choose a new name for the generated interface, specify which superInterfaces (along with generics) the generated interface should extend, and list qualified names of annotations (along with their attributes/values) to be added on top of the generated interface. <br>
All generated detached interfaces are stored in the created _detached_ sub-package.<br>
**Note:** This annotation will not work if **&#64;TopLevel** is NOT used anywhere within the large interface, or if the specified profile is none of the ones provided though the **&#64;SealFor** annotation.

```java
@Detach(
        profile = ACTIVE_WORKER,
        rename = ACTIVE_WORKER + "With2SuperInterfaces",
        superInterfaces = {Processor.class, Drivable.class},
        applyAnnotations = """
                @java.lang.Deprecated
                @java.lang.SuppressWarnings({"unchecked", "deprecation", "unused", "testing", "anotherTestValue"})
                @javax.annotation.processing.SupportedOptions("")
                @javax.annotation.processing.SupportedAnnotationTypes("type1")
                """
)
@Detach(profile = STUDENT, rename = "DeprecatedStudent", applyAnnotations = "@java.lang.Deprecated")
public interface Sociable {

  String STUDENT = "Student";
  String WORKER = "Worker";
  String ACTIVE_WORKER = "ActiveWorker";
  
  @TopLevel
  String startConversation() throws IllegalStateException;

  @SealFor(STUDENT)
  boolean attendClass(String fieldOfStudy) throws IllegalArgumentException;
  
  ...
}
```

<br>

### @DetachAll
Annotation to apply on top of a large interface being segregated. <br>
Result will be the generation of (unsealed) interfaces generated, each one corresponding to each profile provided through the use of the **&#64;SealFor** annotation. Also, each generated interface contains all tagged abstract methods for each profile. <br>
Does not provide as much flexibility as the **&#64;Detach** annotation. <br>
All generated detached interfaces are stored in the created _detached.all_ sub-package.
**Note:** This annotation will not work if **&#64;TopLevel** is NOT used anywhere within the large interface.

```java
@DetachAll
public interface Sociable {

  String STUDENT = "Student";
  String WORKER = "Worker";
  String ACTIVE_WORKER = "ActiveWorker";

  @TopLevel
  String startConversation() throws IllegalStateException;

  @SealFor(STUDENT)
  boolean attendClass(String fieldOfStudy) throws IllegalArgumentException;
  
  ...
}
```

<br>

### Sample interfaces and classes for testing
[https://github.com/mohamed-ashraf-bayor/jisel-annotation-client](https://github.com/mohamed-ashraf-bayor/jisel-annotation-client)

### Integration with Spring Framework / Spring Boot
[https://github.com/mohamed-ashraf-bayor/jisel-integration-with-spring](https://github.com/mohamed-ashraf-bayor/jisel-integration-with-spring)

### Issues, Bugs, Suggestions
Contribute to the project's growth by reporting issues or making improvement suggestions [here](https://github.com/mohamed-ashraf-bayor/jisel/issues/new/choose)


<br>
<br>
