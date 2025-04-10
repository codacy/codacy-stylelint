# number-max-precision

Limit the number of decimal places allowed in numbers.

<!-- prettier-ignore -->
```css
a { top: 3.245634px; }
/**           ↑
 * This decimal place */
```

The [`message` secondary option](https://github.com/stylelint/stylelint/16.17.0/docs/user-guide/configure.md#message) can accept the arguments of this rule.

## Options

`int`: Maximum number of decimal places allowed.

For example, with `2`:

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
a { top: 3.245px; }
```

<!-- prettier-ignore -->
```css
a { top: 3.245634px; }
```

<!-- prettier-ignore -->
```css
@media (min-width: 3.234em) {}
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
a { top: 3.24px; }
```

<!-- prettier-ignore -->
```css
@media (min-width: 3.23em) {}
```

## Optional secondary options

### `ignoreProperties: ["/regex/", /regex/, "string"]`

Ignore the precision of numbers for the specified properties.

For example, with `0`.

Given:

```json
["transition"]
```

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
a { top: 10.5px; }
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
a { transition: all 4.5s ease; }
```

### `ignoreUnits: ["/regex/", /regex/, "string"]`

Ignore the precision of numbers for values with the specified units.

For example, with `2`.

Given:

```json
["/^my-/", "%"]
```

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
a { top: 3.245px; }
```

<!-- prettier-ignore -->
```css
a { top: 3.245634px; }
```

<!-- prettier-ignore -->
```css
@media (min-width: 3.234em) {}
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
a { top: 3.245%; }
```

<!-- prettier-ignore -->
```css
@media (min-width: 3.23em) {}
```

<!-- prettier-ignore -->
```css
a {
  width: 10.5432%;
}
```

<!-- prettier-ignore -->
```css
a { top: 3.245my-unit; }
```

<!-- prettier-ignore -->
```css
a {
  width: 10.989my-other-unit;
}
```

### `insideFunctions: {"/regex/": int, /regex/: int, "string": int}`

The `insideFunctions` option can change a primary option value for specified functions.

For example, with `2`.

Given:

```json
{ "/^(oklch|oklab|lch|lab)$/": 4 }
```

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
a { color: rgb(127.333 0 0); }
```

<!-- prettier-ignore -->
```css
a { color: rgb(calc(127.333 / 3) 0 0); }
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
a { color: oklch(0.333 0 0); }
```

<!-- prettier-ignore -->
```css
a { color: lab(0.3333 0 0); }
```

<!-- prettier-ignore -->
```css
a { color: oklab(calc(127.333 / 3) 0 0); }
```
