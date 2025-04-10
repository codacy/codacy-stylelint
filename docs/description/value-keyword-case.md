# value-keyword-case

Specify lowercase or uppercase for keywords values.

<!-- prettier-ignore -->
```css
    a { display: block; }
/**              ↑
 *    These values */
```

This rule ignores [`<custom-idents>`](https://developer.mozilla.org/en/docs/Web/CSS/custom-ident) of known properties. Keyword values which are paired with non-properties (e.g. `$vars` and custom properties), and do not conform to the primary option, can be ignored using the `ignoreKeywords: []` secondary option.

The [`fix` option](https://github.com/stylelint/stylelint/16.17.0/docs/user-guide/options.md#fix) can automatically fix all of the problems reported by this rule.

The [`message` secondary option](https://github.com/stylelint/stylelint/16.17.0/docs/user-guide/configure.md#message) can accept the arguments of this rule.

## Options

`string`: `"lower"|"upper"`

### `"lower"`

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
a {
  display: Block;
}
```

<!-- prettier-ignore -->
```css
a {
  display: bLoCk;
}
```

<!-- prettier-ignore -->
```css
a {
  display: BLOCK;
}
```

<!-- prettier-ignore -->
```css
a {
  transition: -WEBKIT-TRANSFORM 2s;
}
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
a {
  display: block;
}
```

<!-- prettier-ignore -->
```css
a {
  transition: -webkit-transform 2s;
}
```

### `"upper"`

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
a {
  display: Block;
}
```

<!-- prettier-ignore -->
```css
a {
  display: bLoCk;
}
```

<!-- prettier-ignore -->
```css
a {
  display: block;
}
```

<!-- prettier-ignore -->
```css
a {
  transition: -webkit-transform 2s;
}
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
a {
  display: BLOCK;
}
```

<!-- prettier-ignore -->
```css
a {
  transition: -WEBKIT-TRANSFORM 2s;
}
```

## Optional secondary options

### `ignoreKeywords: ["/regex/", /regex/, "non-regex"]`

Ignore case of keywords values.

For example, with `"lower"`.

Given:

```json
["Block", "/^(f|F)lex$/"]
```

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
a {
  display: bLoCk;
}
```

<!-- prettier-ignore -->
```css
a {
  display: BLOCK;
}
```

<!-- prettier-ignore -->
```css
a {
  display: fLeX;
}
```

<!-- prettier-ignore -->
```css
a {
  display: FLEX;
}
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
a {
  display: block;
}
```

<!-- prettier-ignore -->
```css
a {
  display: Block;
}
```

<!-- prettier-ignore -->
```css
a {
  display: flex;
}
```

<!-- prettier-ignore -->
```css
a {
  display: Flex;
}
```

### `ignoreProperties: ["/regex/", /regex/, "non-regex"]`

Ignore case of the values of the listed properties.

For example, with `"lower"`.

```js
["/^(b|B)ackground$/", "display"];
```

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
a {
  text-align: LEFT;
}
```

<!-- prettier-ignore -->
```css
a {
  text-align: Left;
}
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
a {
  display: bloCk;
}
```

<!-- prettier-ignore -->
```css
a {
  display: BloCk;
}
```

<!-- prettier-ignore -->
```css
a {
  display: BLOCK;
}
```

<!-- prettier-ignore -->
```css
a {
  display: block;
}
```

<!-- prettier-ignore -->
```css
a {
  background: Red;
}
```

<!-- prettier-ignore -->
```css
a {
  Background: deepPink;
}
```

### `ignoreFunctions: ["/regex/", /regex/, "non-regex"]`

Ignore case of the values inside the listed functions.

For example, with `"upper"`.

```js
["/^(f|F)oo$/", "t"];
```

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
a {
  display: b(inline);
}
```

```css
a {
  color: bar(--camelCase);
}
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
a {
  display: t(flex);
}
```

<!-- prettier-ignore -->
```css
a {
  display: t(fLeX);
}
```

<!-- prettier-ignore -->
```css
a {
  color: t(--camelCase);
}
```

<!-- prettier-ignore -->
```css
a {
  color: foo(--camelCase);
}
```

<!-- prettier-ignore -->
```css
a {
  color: Foo(--camelCase);
}
```

### `camelCaseSvgKeywords: true | false` (default: `false`)

If `true`, this rule expects SVG keywords to be camel case when the primary option is `"lower"`.

For example with `true`:

The following pattern is _not_ considered a problem:

<!-- prettier-ignore -->
```css
a {
  color: currentColor;
}
```

The following pattern is considered a problem:

<!-- prettier-ignore -->
```css
a {
  color: currentcolor;
}
```
