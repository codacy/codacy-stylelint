package codacy.stylelint

case class StylelintPatternResult(line: Int, column: Int, rule: String, severity: String, text: String )

case class StylelintResult(source: String, warnings: List[StylelintPatternResult])

