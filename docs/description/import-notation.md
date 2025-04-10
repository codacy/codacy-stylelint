# import-notation

Specify string or URL notation for `@import` rules.

<!-- prettier-ignore -->
```css
@import url("x.jpg");
/**     ↑
 *      This notation */
```

The [`fix` option](https://github.com/stylelint/stylelint/16.17.0/docs/user-guide/options.md#fix) can automatically fix all of the problems reported by this rule.

The [`message` secondary option](https://github.com/stylelint/stylelint/16.17.0/docs/user-guide/configure.md#message) can accept arguments.

## Options

`string`: `"string"|"url"`

### `"string"`

`@import` rules _must always_ use string notation.

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
@import url(foo.css);
```

<!-- prettier-ignore -->
```css
@import url('foo.css');
```

<!-- prettier-ignore -->
```css
@import url("foo.css");
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
@import 'foo.css';
```

<!-- prettier-ignore -->
```css
@import "foo.css";
```

### `"url"`

`@import` rules _must always_ use URL notation.

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
@import 'foo.css';
```

<!-- prettier-ignore -->
```css
@import "foo.css";
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
@import url(foo.css);
```

<!-- prettier-ignore -->
```css
@import url('foo.css');
```

<!-- prettier-ignore -->
```css
@import url("foo.css");
```
