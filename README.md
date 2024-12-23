# Inscript
A simple, easy and very configurable configuration language.
---
## Default Data Types:
| Data Type | Java      | Usage Example                                |
|-----------|-----------|----------------------------------------------|
| String    | String    | `'Hello'`, `"Hello"`, `Hello`                |
| Boolean   | Boolean   | `True`, `False`, `true`, `false`             |
| Byte      | Byte      | `13B`                                        |
| Short     | Short     | `255S`                                       |
| Integer   | Integer   | `100`                                        |
| Double    | Double    | `10.5D`                                      |
| Float     | Float     | `10.00F`                                     |
| Long      | Long      | `100000L`                                    |
| Character | Character | `'A'C`                                       |
| UUID      | UUID      | `uuid(4ad4c78c-d4a4-4d25-91cf-4f001efc46c0)` |
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
    try {
      new BigDecimal(text);
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
## Configuring the language
If you want to change stuff like defining a list, or just want to change the indent, you can refer to the InscriptConstants interface.
```java
InscriptConstants.INDENT.set("  "::repeat);

InscriptConstants.LIST_START.set("List(");
InscriptConstants.LIST_END.set(")");
```
---
## Using Inscript
You can load/save contents from/to files, or just load/save contents from/to strings.
If you want to use files, you have to provide a `Path` or a `File`.
```java
final Path path = Path.of(/* ... */);
final Inscript inscript = Inscript.newInscript(path);
```
If you want to use the latter, you don't need to provide anything:
```java
final Inscript inscript = Inscript.emptyInscript();
```

To obtain the inscript editor, you use `Inscript.getEditor()`.

Loading:
1. Files
   ```java
   inscript.loadFromDisk();
   ```
2. String
   ```java
   inscript.loadFromString("gender = \"male\"")
   ```

Saving:
1. Files
   ```java
   inscript.saveToDisk();
   ```
2. String
   ```java
   final String saved = inscript.saveToString()
   ```
