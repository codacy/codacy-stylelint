# comment-empty-line-before

Require or disallow an empty line before comments.

<!-- prettier-ignore -->
```css
a {}
              /* ← */
/* comment */ /* ↑ */
/**              ↑
*        This line */
```

This rule ignores:

- comments that are the very first node in the source
- shared-line comments
- single-line comments with `//` (when you're using a custom syntax that supports them)
- comments within selector and value lists

The [`fix` option](https://github.com/stylelint/stylelint/16.17.0/docs/user-guide/options.md#fix) can automatically fix all of the problems reported by this rule.

## Options

`string`: `"always"|"never"`

### `"always"`

There _must always_ be an empty line before comments.

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
a {}
/* comment */
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
a {}

/* comment */
```

<!-- prettier-ignore -->
```css
a {} /* comment */
```

### `"never"`

There _must never_ be an empty line before comments.

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
a {}

/* comment */
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
a {}
/* comment */
```

<!-- prettier-ignore -->
```css
a {} /* comment */
```

## Optional secondary options

### `except: ["first-nested"]`

Reverse the primary option for comments that are nested and the first child of their parent node.

For example, with `"always"`:

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
a {

  /* comment */
  color: pink;
}
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
a {
  /* comment */
  color: pink;
}
```

### `ignore: ["after-comment", "stylelint-commands"]`

#### `"after-comment"`

Ignore comments that follow another comment.

For example, with `"always"`:

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
a {
  background: pink;

  /* comment */
  /* comment */
  color: #eee;
}
```

<!-- prettier-ignore -->
```css
a {
  background: pink;

  /* comment */

  /* comment */
  color: #eee;
}
```

#### `"stylelint-commands"`

Ignore configuration comments, e.g. `/* stylelint-disable color-no-hex */`.

For example, with `"always"`:

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
a {
  background: pink;
  /* not a configuration comment */
  color: #eee;
}
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
a {
  background: pink;
  /* stylelint-disable color-no-hex */
  color: pink;
}
```

### `ignoreComments: ["/regex/", /regex/, "string"]`

Ignore comments matching the given regular expressions or strings.

For example, with `"always"` and given:

```json
["/^ignore/", "string-ignore"]
```

The following patterns are _not_ considered problems:

```css
:root {
  background: pink;
  /* ignore this comment because of the regex */
  color: pink;
}
```

```css
:root {
  background: pink;
  /* string-ignore */
  color: pink;
}
```
