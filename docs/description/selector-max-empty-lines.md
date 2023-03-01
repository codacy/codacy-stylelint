# selector-max-empty-lines

Limit the number of adjacent empty lines within selectors.

<!-- prettier-ignore -->
```css
a,
              /* ← */
b {        /* ↑ */
  color: red; /* ↑ */
}             /* ↑ */
/**              ↑
 *        This empty line */
```

The [`fix` option](https://github.com/stylelint/stylelint/tree/14.16.1/docs/user-guide/usage/options.md#fix) can automatically fix all of the problems reported by this rule.

## Options

`int`: Maximum number of adjacent empty lines allowed.

For example, with `0`:

The following patterns are considered problems:

<!-- prettier-ignore -->
```css
a

b {
  color: red;
}
```

<!-- prettier-ignore -->
```css
a,

b {
  color: red;
}
```

<!-- prettier-ignore -->
```css
a

>
b {
  color: red;
}
```

<!-- prettier-ignore -->
```css
a
>

b {
  color: red;
}
```

The following patterns are _not_ considered problems:

<!-- prettier-ignore -->
```css
a b {
  color: red;
}
```

<!-- prettier-ignore -->
```css
a
b {
  color: red;
}
```

<!-- prettier-ignore -->
```css
a,
b {
  color: red;
}
```

<!-- prettier-ignore -->
```css
a > b {
  color: red;
}
```

<!-- prettier-ignore -->
```css
a
>
b {
  color: red;
}
```
