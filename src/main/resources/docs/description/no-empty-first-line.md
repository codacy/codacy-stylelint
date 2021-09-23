# no-empty-first-line

Disallow empty first lines.

<!-- prettier-ignore -->
```css
    \n
    /** ↑
     * This newline */
    a { color: pink; }
```

This rule ignores empty sources. Use the [`no-empty-source`](https://github.com/stylelint/stylelint/tree/13.13.1/lib/rules/no-empty-source/README.md) rule to disallow these.

The [`fix` option](https://github.com/stylelint/stylelint/tree/13.13.1/docs/user-guide/usage/options.md#fix) can automatically fix all of the problems reported by this rule.

## Options

### `true`

The following patterns are considered violations:

<!-- prettier-ignore -->
```css
\n
a { color: pink; }
```

The following patterns are _not_ considered violations:

<!-- prettier-ignore -->
```css
a { color: pink; }
```
