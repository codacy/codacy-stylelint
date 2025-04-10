# selector-class-pattern

Specify a pattern for class selectors.

<!-- prettier-ignore -->
```css
    .foo, #bar.baz span, #hoo[disabled] { color: pink; }
/** ↑         ↑
 * These class selectors */
```

This rule ignores non-outputting Less mixin definitions and called Less mixins.

Escaped selectors (e.g. `.u-size-11\/12\@sm`) are parsed as escaped twice (e.g. `.u-size-11\\/12\\@sm`). Your RegExp should account for that.

The [`message` secondary option](https://github.com/stylelint/stylelint/16.17.0/docs/user-guide/configure.md#message) can accept the arguments of this rule.

## Options

`regex|string`

A string will be translated into a RegExp like so `new RegExp(yourString)` — so be sure to escape properly.

The selector value _after `.`_ will be checked. No need to include `.` in your pattern.

Given the string:

```json
"foo-[a-z]+"
```

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
.foop {}
```

<!-- prettier-ignore -->
```css
.foo-BAR {}
```

<!-- prettier-ignore -->
```css
div > #zing + .foo-BAR {}
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
.foo-bar {}
```

<!-- prettier-ignore -->
```css
div > #zing + .foo-bar {}
```

<!-- prettier-ignore -->
```css
#foop {}
```

<!-- prettier-ignore -->
```css
[foo='bar'] {}
```

## Optional secondary options

### `resolveNestedSelectors: true | false` (default: `false`)

This option will resolve nested selectors with `&` interpolation.

For example, with `true`.

Given the string:

```json
"^[A-Z]+$"
```

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
.A {
  &__B {} /* resolved to ".A__B" */
}
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
.A {
  &B {} /* resolved to ".AB" */
}
```
