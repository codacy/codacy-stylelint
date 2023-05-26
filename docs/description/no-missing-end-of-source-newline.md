# no-missing-end-of-source-newline

> **Warning** This rule is deprecated and will be removed in the future. See [the migration guide](https://github.com/stylelint/stylelint/tree/15.6.2/docs/migration-guide/to-15.md).

Disallow missing end-of-source newlines.

<!-- prettier-ignore -->
```css
    a { color: pink; }
    \n
/** ↑
 * This newline */
```

Completely empty files are not considered problems.

The [`fix` option](https://github.com/stylelint/stylelint/tree/15.6.2/docs/user-guide/options.md#fix) can automatically fix all of the problems reported by this rule.

## Options

### `true`

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
a { color: pink; }
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
a { color: pink; }
\n
```
