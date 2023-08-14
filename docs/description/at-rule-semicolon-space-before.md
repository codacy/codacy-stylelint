# at-rule-semicolon-space-before

> **Warning** This rule is deprecated and will be removed in the future. See [the migration guide](https://github.com/stylelint/stylelint/tree/15.10.2/docsmigration-guideto-15.md).

Require a single space or disallow whitespace before the semicolons of at-rules.

<!-- prettier-ignore -->
```css
@import "components/buttons";
/**                         ↑
 * The space before this semicolon */
```

## Options

`string`: `"always"|"never"`

### `"always"`

There _must always_ be a single space before the semicolons.

The following pattern is considered a problem:

<!-- prettier-ignore -->
```css
@import "components/buttons";
```

The following pattern is _not_ considered a problem:

<!-- prettier-ignore -->
```css
@import "components/buttons" ;
```

### `"never"`

There _must never_ be a single space before the semicolons.

The following pattern is considered a problem:

<!-- prettier-ignore -->
```css
@import "components/buttons" ;
```

The following pattern is _not_ considered a problem:

<!-- prettier-ignore -->
```css
@import "components/buttons";
```
