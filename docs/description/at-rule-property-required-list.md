# at-rule-property-required-list

Specify a list of required properties (or descriptors) for an at-rule.

<!-- prettier-ignore -->
```css
    @font-face { font-display: swap; font-family: 'foo'; }
/**  ↑           ↑                   ↑
 *  At-rule and required descriptor names */
```

The [`message` secondary option](https://github.com/stylelint/stylelint/16.17.0/docs/user-guide/configure.md#message) can accept the arguments of this rule.

## Options

`object`: `{ "at-rule-name": ["array", "of", "properties or descriptors"]|"property or descriptor" }`

Given:

```json
{
  "font-face": ["font-display", "font-family", "font-style"]
}
```

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
@font-face {
    font-family: 'foo';
    src: url('./fonts/foo.woff2') format('woff2');
}
```

<!-- prettier-ignore -->
```css
@font-face {
    font-family: 'foo';
    font-style: normal;
    src: url('./fonts/foo.woff2') format('woff2');
}
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
@font-face {
    font-display: swap;
    font-family: 'foo';
    font-style: normal;
    src: url('./fonts/foo.woff2') format('woff2');
}
```
