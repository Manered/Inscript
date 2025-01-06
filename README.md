# Inscript
A simple, easy configuration file library that supports multiple formats.
---
## Default Data Types:
| Data Type  | Java                 | Usage Example                                |
|------------|----------------------|----------------------------------------------|
| String     | String               | `'Hello'`, `"Hello"`, `Hello`                |
| Boolean    | Boolean              | `True`, `False`, `true`, `false`             |
| Byte       | Byte                 | `13B`                                        |
| Short      | Short                | `255S`                                       |
| Integer    | Integer              | `100`                                        |
| Double     | Double               | `10.5D`                                      |
| Float      | Float                | `10.00F`                                     |
| Long       | Long                 | `100000L`                                    |
| Character  | Character            | `'A'C`                                       |
| UUID       | UUID                 | `uuid(4ad4c78c-d4a4-4d25-91cf-4f001efc46c0)` |
| Byte Array | byte[] (Uses Base64) | `base64(base64ValueHere...)`                 |
---
## Custom Data Types
You can register your own data types and also a custom section parser
Access the ValueRegistry with:
```java
final ValueRegistry registry = ValueRegistry.REGISTRY;
```
After that, we can register a simple data type:
```java
registry.register(BigDecimal.class, InlineValue.<BigDecimal>builder()
  .matches(text -> {
    if (!text.startsWith("bigDecimal(") && !text.endsWith(")")) return false;

    try {
      new BigDecimal(text.substring(11, text.length() - 1);
      return true;
    } catch (final Exception e) {
      return false;
    }
  })
  .deserialize(text -> {
    if (!text.startsWith("bigDecimal(") && !text.endsWith(")")) return null;
    return new BigDecimal(text.substring(11, text.length() - 1));
  })
  .serialize(bigDecimal -> "bigDecimal(" + bigDecimal.toString() + ")")
  .build()
);
```
You can also make a simple Ticket section or whatever if you like storing stuff without repeating code.
```java
public record Ticket(@NotNull UUID user, long date) {}

registry.register(Ticket.class, InscriptValue.<Ticket>builder()
  .serialize((ticket, section) -> {
    section.set("user", ticket.user());
    section.set("date", ticket.date());
  })
  .deserialize(section -> {
    final Optional<UUID> user = section.get("user", UUID.class);
    final Optional<Long> date = section.get("date", Long.class);

    if (user.isEmpty() || date.isEmpty()) return null;
    return new Ticket(user.get(), date.get());
  })
  .build()
);
```
And we're done!
---
## Inscript Constants
If you want to change the default indent or the root section node key you can do that easily.
```java
InscriptConstants.INDENT.set("    "::repeat);
InscriptConstants.ROOT_SECTION_KEY.set("*");

InscriptConstants.VERSION.get().get().ifPresent((version) -> {
    System.out.println("Running Inscript version `" + version + "`");
});
```
---
## Using Inscript
**By default, we offer 2 file format implementations:**
1. YAML/YML (.yml) `FileFormats.YAML`
2. DataScript (.ds) `FileFormats.DATASCRIPT`

Inscript doesn't actually need a file to work. You can use it perfectly fine with just strings.

### Getting a new Inscript instance:
```java
// Auto-detect the file format from the file's extension
final Inscript autoDetect = Inscript.newInscript(Path.of("file.yml"));

// Manually provide a file format
final Inscript manual = Inscript.newInscript(FileFormats.DATASCRIPT, Path.of("file.ds"));

// Can't use file I/O features, must use saveToString and loadFromString. You must specify the file format.
final Inscript empty = Inscript.newInscript(FileFormats.YAML);
```
### Saving and Loading
Now, the function you should call depends on the source.
If you haven't specified a Path or File in Inscript#newInscript, that means that you can't use `loadFromDisk()` and `saveToDisk()`.
```java
final Inscript inscript = /* ... */;

// Only use if you are sure you provided a path to Inscript.newInscript()
inscript.loadFromDisk();
```
### Node Editor
There are 3 node implementations:
1. `ScalarNode` - a mapping of a key to a value.
2. `SectionNode` - holds a bunch of children nodes.
3. `RootSectionNode` - a default implementation of SectionNode with a specific root key and an empty modifiable list of children nodes.

Every editor is a `SectionNode` wrapped in a **`ConfigSection`**
A ConfigSection is a chainable/fluent builder interface that has the following methods:
```java
@NotNull
@Unmodifiable
Set<ConfigNode> getChildren();
    
@NotNull
@Unmodifiable
Set<String> getKeys();

boolean isRoot();

@NotNull
Optional<ConfigNode> getNode(final @NotNull String key);

boolean isSection(final @NotNull String key);

boolean isScalar(final @NotNull String key);

@NotNull
SectionNode getSection();

@NotNull
Optional<ConfigSection> getSection(final @NotNull String key);

@NotNull
ConfigSection createSection(final @NotNull String key);

@NotNull
@CanIgnoreReturnValue
ConfigSection section(final @NotNull String key, final @NotNull Consumer<ConfigSection> handler);

@NotNull
<T> Optional<T> get(final @NotNull String key, final @NotNull Class<? extends T> ignoredType);

@NotNull
<T> List<T> getList(final @NotNull String key, final @NotNull Class<? extends T> ignoredType);

@NotNull
@CanIgnoreReturnValue
<T> ConfigSection set(final @NotNull String key, final @Nullable T value);

boolean has(final @NotNull String key);

boolean contains(final @NotNull String key);

@NotNull
@CanIgnoreReturnValue
ConfigSection unset(final @NotNull String key);

@NotNull
@CanIgnoreReturnValue
ConfigSection reset();

@NotNull
@CanIgnoreReturnValue
ConfigSection forEachSection(final @NotNull Consumer<ConfigSection> sectionConsumer);

@NotNull 
@CanIgnoreReturnValue
ConfigSection forEachScalar(final @NotNull Consumer<ScalarNode<?>> scalarConsumer);

@NotNull
@CanIgnoreReturnValue 
ConfigSection forEach(final @NotNull Consumer<ScalarNode<?>> scalarConsumer, final @NotNull Consumer<ConfigSection> sectionConsumer);

@NotNull
@CanIgnoreReturnValue
ConfigSection comment(final @NotNull String key, final @NotNull Collection<? extends String> comments);

@NotNull
Collection<String> getComments(final @NotNull String key);

@NotNull
@CanIgnoreReturnValue 
ConfigSection comment(final @NotNull String key, final @NotNull String @NotNull ... comments);
```
To access the root section editor, you can use:
```java
final Inscript inscript = /* ... */;
final ConfigSection root = inscript.getRoot();
```

### Miscellaneous
> ![NOTE]
> By default, Inscript does not run anything asynchronously for you.

> ![CAUTION]
> Any loading and saving operations from Inscript **should be run asynchronously**. 
> 
> Make sure to run the string based methods (not the disk ones) asynchronously as well.
> They are basically the same as the disk based ones without the file writing and reading.
>
> **Example:**
> ```java
> CompletableFuture.runAsync(inscript::loadFromDisk).thenRun(() -> {
>     // ...
> });
> ```