package codacy.stylelint
case class StylelintPatternResult(line: Int, column: Int, rule: String, severities: String, text: String)



case class StylelintResult(source: String, warnings: List[StylelintPatternResults])
