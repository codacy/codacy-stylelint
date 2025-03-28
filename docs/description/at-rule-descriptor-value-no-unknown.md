# at-rule-descriptor-value-no-unknown

Disallow unknown values for descriptors within at-rules.

<!-- prettier-ignore -->
```css
@counter-style foo {
  system: unknown;
/**       ↑
 * Values like this */
}
```

This rule considers descriptors and values defined in the CSS Specifications, up to and including Editor's Drafts, to be known.

You can filter the [CSSTree Syntax Reference](https://csstree.github.io/docs/syntax/) to find out what values are valid for a descriptor of an at-rule.

This rule is only appropriate for CSS. You should not turn it on for CSS-like languages, such as SCSS or Less.

The [`message` secondary option](https://github.com/stylelint/stylelint/16.17.0/docs/user-guide/configure.md#message) can accept the arguments of this rule.

This rule checks descriptor values within at-rules. You can use [`declaration-property-value-no-unknown`](https://github.com/stylelint/stylelint/16.17.0/lib/rules/declaration-property-value-no-unknown/README.md) to disallow unknown values for properties within declarations, and [`at-rule-descriptor-no-unknown`](https://github.com/stylelint/stylelint/16.17.0/lib/rules/at-rule-descriptor-no-unknown/README.md) to disallow unknown descriptors for at-rules.

This rule overlaps with:

- [`color-no-invalid-hex`](https://github.com/stylelint/stylelint/16.17.0/lib/rules/color-no-invalid-hex/README.md)
- [`function-linear-gradient-no-nonstandard-direction`](https://github.com/stylelint/stylelint/16.17.0/lib/rules/function-linear-gradient-no-nonstandard-direction/README.md)
- [`function-no-unknown`](https://github.com/stylelint/stylelint/16.17.0/lib/rules/function-no-unknown/README.md)
- [`string-no-newline`](https://github.com/stylelint/stylelint/16.17.0/lib/rules/string-no-newline/README.md)
- [`unit-no-unknown`](https://github.com/stylelint/stylelint/16.17.0/lib/rules/unit-no-unknown/README.md)

You can either turn off the rules or configure them to ignore the overlaps.

For customizing syntax, see the [`languageOptions`](https://github.com/stylelint/stylelint/16.17.0/docs/user-guide/configure.md#languageoptions) section.

Prior art:

- [stylelint-csstree-validator](https://www.npmjs.com/package/stylelint-csstree-validator)

## Options

### `true`

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
@counter-style foo {
  system: unknown;
}
```

<!-- prettier-ignore -->
```css
@property --foo {
  syntax: unknown;
}
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
@counter-style foo {
  system: numeric;
}
```

<!-- prettier-ignore -->
```css
@property --foo {
  syntax: "<color>";
}
```
