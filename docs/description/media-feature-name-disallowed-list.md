# media-feature-name-disallowed-list

Specify a list of disallowed media feature names.

<!-- prettier-ignore -->
```css
@media (min-width: 700px) {}
/**     ↑
 * This media feature name */
```

The [`message` secondary option](https://github.com/stylelint/stylelint/16.17.0/docs/user-guide/configure.md#message) can accept the arguments of this rule.

## Options

`array|string|regex`: `["array", "of", "unprefixed", /media-features/, "regex"]|"media-feature"|"/regex/"|/regex/`

Given:

```json
["max-width", "/^my-/"]
```

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
@media (max-width: 50em) {}
```

<!-- prettier-ignore -->
```css
@media (my-width: 50em) {}
```

<!-- prettier-ignore -->
```css
@media (max-width < 50em) {}
```

<!-- prettier-ignore -->
```css
@media (10em < my-height < 50em) {}
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
@media (min-width: 50em) {}
```

<!-- prettier-ignore -->
```css
@media print and (min-resolution: 300dpi) {}
```

<!-- prettier-ignore -->
```css
@media (min-width >= 50em) {}
```

<!-- prettier-ignore -->
```css
@media (10em < width < 50em) {}
```
