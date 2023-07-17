# unicode-bom

> **Warning** This rule is deprecated and will be removed in the future. See [the migration guide](https://github.com/stylelint/stylelint/tree/15.10.1/docsmigration-guideto-15.md).

Require or disallow the Unicode Byte Order Mark.

## Options

`string`: `"always"|"never"`

### `"always"`

The following pattern is considered a problem:

<!-- prettier-ignore -->
```css
a {}
```

The following pattern is _not_ considered a problem:

<!-- prettier-ignore -->
```css
U+FEFF
a {}
```

### `"never"`

The following pattern is considered a problem:

<!-- prettier-ignore -->
```css
U+FEFF
a {}
```

The following pattern is _not_ considered a problem:

<!-- prettier-ignore -->
```css
a {}
```
