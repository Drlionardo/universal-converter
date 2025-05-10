# Description

Solution to the SKB Kontur internship test assignment for the [Java Backend](https://kontur.ru/education/programs/intern/java) summer program in 2021.  
Author of the solution: **Danila Iagupets**.  
Original [repository](https://github.com/gnkoshelev/universal-converter) with the test assignment.

# Universal Converter

It has happened! We are not alone in the [Universe](https://habr.com/ru/company/skbkontur/blog/518490/)!  
Representatives of countless extraterrestrial civilizations have simultaneously made contact!  
We are interested in how far they have advanced in the exact sciences.  
However, after exchanging databases of scientific articles and encyclopedias, we encountered a problem —  
an unimaginably large number of unit systems is used, even within a single civilization!

Now we need to understand how many Betelgeusian mjyuds are in one Earth meter  
or how many Earth liters fit into one RasAlgethian yidom.

We are invited to help our fellow scientists by writing an HTTP service for converting all units of measurement.

## Conversion Rules

Scientists have partially compiled conversion rules between alien units of measurement  
and provided them in [CSV](https://en.wikipedia.org/wiki/Comma-separated_values) format:
```csv
S,T,value
```
Here, `S` and `T` are units of measurement, and `value` corresponds to how many `T` are in one `S`.

Example file:
```csv
m,cm,100
mm,m,0.001
km,m,1000
hour,min,60
min,s,60
```

This example defines 5 conversion rules:
- `1 m = 100 cm`
- `1 mm = 0.001 m`
- `1 km = 1000 m`
- `1 hour = 60 min`
- `1 min = 60 s`

The path to the file must be passed as an argument when starting the HTTP service.

## Expressions for Conversion

Expressions are written using the unit abbreviations from the file,  
using only multiplication `*` and division `/` operators.  
The division symbol can be used no more than once and serves to write a fraction.

**Note:** Whitespace characters are ignored.

Example of a "dimensionless quantity":
```text
(empty string)
```

Example of a "meter":
```text
m
```

Example of a "hertz":
```text
1 / s
```

Example of "meter per second":
```text
m / s
```

Example of a "newton":
```text
kg * m / s * s
```

Here `kg * m` is the numerator, and `s * s` is the denominator.

## API

The service must provide one method `POST /convert` with a JSON body:
```json
{
 "from": "<expression in source units>",
 "to": "<expression in target units>"
}
```

Possible responses:
- Code `400 Bad Request` if the expressions contain unknown units  
  (i.e., not present in the provided conversion rules file).
- Code `404 Not Found` if the conversion is not possible  
  (e.g., converting meters to kilograms).
- Code `200 OK` if the conversion is possible.  
  The response body must contain the conversion factor as a decimal with **15 significant digits**.

Example request body:
```json
{
 "from": "m / s",
 "to":  "km / hour"
}
```

And the corresponding response body (assuming the CSV file from the earlier example):
```text
3.6
```

Thus, the service confirms the conversion according to the formula:
```text
1 m / s = 3.6 km / hour
```

## Conversion Examples

The following examples use this conversion file:
```csv
m,cm,100
mm,m,0.001
km,m,1000
hour,min,60
min,s,60
```

The service must successfully perform conversions such as:
```text
1 m = 3.6 km * s / hour
```
```text
1 km / m = 1000
```

## Requirements for Implementation

- Code must compile and run with Java 11.
- Build the service using Apache Maven with the command `mvn package`.
- The service should be packaged as a fat JAR — all dependencies included in a single JAR file.
- The service is launched with the command `java -jar universal-converter-1.0.0.jar /path/to/file.csv`,  
  where `/path/to/file.csv` is the path to the file with conversion rules.
- The service must accept HTTP requests on the standard port (`80`).
- Source code must follow [Java Code Conventions](https://www.oracle.com/technetwork/java/codeconventions-150003.pdf)  
  and the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).

## Testing Information

The conversion rules file will contain no more than `1,000,000` different conversion rules,  
and no more than `200,000` unique units of measurement.
