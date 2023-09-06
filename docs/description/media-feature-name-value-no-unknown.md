# media-feature-name-value-no-unknown

Disallow unknown values for media features.

<!-- prettier-ignore -->
```css
@media (color: red) {}
/**     ↑      ↑
 * feature and value pairs like these */
```

This rule considers values for media features defined within the CSS specifications to be known.

This rule is only appropriate for CSS. You should not turn it on for CSS-like languages, such as Sass or Less, as they have their own syntaxes.

This rule is experimental with some false negatives that we'll patch in minor releases.

It sometimes overlaps with:

- [`unit-no-unknown`](https://github.com/stylelint/stylelint/tree/15.10.3/librulesunit-no-unknownREADME.md)

If duplicate problems are flagged, you can turn off the corresponding rule.

The [`message` secondary option](https://github.com/stylelint/stylelint/tree/15.10.3/docsuser-guideconfigure.md#message) can accept the arguments of this rule.

## Options

### `true`

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
@media (color: red) { top: 1px; }
```

<!-- prettier-ignore -->
```css
@media (width: 10) { top: 1px; }
```

<!-- prettier-ignore -->
```css
@media (width: auto) { top: 1px; }
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
@media (color: 8) { top: 1px; }
```

<!-- prettier-ignore -->
```css
@media (width: 10px) { top: 1px; }
```
