package pl.metastack.metadocs.input.markdown

import scala.util.Success

import minitest.SimpleTestSuite

import pl.metastack.metadocs.input
import pl.metastack.metadocs.document.tree._

object PegdownSpec extends SimpleTestSuite {
  test("Parse block extension") {
    assertEquals(BlockParser.parse("""[tag key="value"]"""),
      Success(input.tree.Tag("tag", Seq(input.tree.Argument.Named("key", "value")))))

    assertEquals(BlockParser.parse("""[tag   key="value"]"""),
      Success(input.tree.Tag("tag", Seq(input.tree.Argument.Named("key", "value")))))

    assertEquals(BlockParser.parse("""[tag   key="val ue"]"""),
      Success(input.tree.Tag("tag", Seq(input.tree.Argument.Named("key", "val ue")))))

    assertEquals(BlockParser.parse("""[tag key="value" key2="value2"]"""),
      Success(input.tree.Tag("tag", Seq(
        input.tree.Argument.Named("key", "value"),
        input.tree.Argument.Named("key2", "value2")
      ))))

    assertEquals(BlockParser.parse("""[tag key="value"   key2="value2"]"""),
      Success(input.tree.Tag("tag", Seq(
        input.tree.Argument.Named("key", "value"),
        input.tree.Argument.Named("key2", "value2")
      ))))
  }

  test("Don't parse Markdown links") {
    assertEquals(BlockParser.parse("""[autocommit](https://en.wikipedia.org/wiki/Autocommit)""").isFailure, true)
  }

  test("Replace blocks") {
    assertEquals(
      BlockParser.replace("""a [tag key="value"] b"""),
      ("a %1 b",
        Seq(input.tree.Tag("tag",
          Seq(input.tree.Argument.Named("key", "value"))))))
  }

  test("Don't replace in code blocks") {
    val input = """```scala
column[Ref[Supplier], Int]()
```"""
    assertEquals(BlockParser.replace(input)._1, input)
  }

  test("Bold") {
    assertEquals(Pegdown.parse("**Hello**"),
      Root(Paragraph(Bold(Text("Hello")))))
  }

  test("Code") {
    assertEquals(Pegdown.parse("`code`"),
      Root(Paragraph(Code(Text("code")))))
    assertEquals(Pegdown.parse("``code``"),
      Root(Paragraph(Code(Text("code")))))
  }

  test("Link") {
    assertEquals(Pegdown.parse("[Google](http://google.com/)"),
      Root(Paragraph(Url("http://google.com/", Text("Google")))))
  }

  test("Jump") {
    assertEquals(Pegdown.parse("[Section](#section)"),
      Root(Paragraph(Jump("section", Some("Section")))))
  }

  test("Jump without title") {
    assertEquals(Pegdown.parse("[#section]"),
      Root(Paragraph(Jump("section", None))))
  }

  test("Auto link") {
    assertEquals(Pegdown.parse("[autocommit](https://en.wikipedia.org/wiki/Autocommit)"),
      Root(Paragraph(Url("https://en.wikipedia.org/wiki/Autocommit", Text("autocommit")))))
  }

  test("Source code") {
    assertEquals(Pegdown.parse(
      """
        |```scala
        |test()
        |```
      """.stripMargin),

      Root(
        Scala(code = Some("test()"))
      )
    )
  }

  test("List") {
    assertEquals(Pegdown.parse(
      """
        |* Item 1
        |* Item 2
      """.stripMargin),

      Root(
        List(
          ListItem(Text("Item 1")),
          ListItem(Text("Item 2"))
        )
      )
    )
  }

  test("Tables") {
    assertEquals(Pegdown.parse(
"""
|a|b|
|:-|:-|
|l|r|"""),

      Root(
        Table(
          Row(Column(Text("a")), Column(Text("b"))),
          Row(Column(Text("l")), Column(Text("r")))
        )
      )
    )
  }

  test("Chapters") {
    assertEquals(Pegdown.parse(
      """
        |# Chapter 1
        |Content 1
        |# Chapter 2
        |Content 2
      """.stripMargin),

      Root(
        Chapter(None, "Chapter 1", Paragraph(Text("Content 1"))),
        Chapter(None, "Chapter 2", Paragraph(Text("Content 2")))
      )
    )
  }

  test("Chapters with subsections") {
    assertEquals(Pegdown.parse(
      """
        |# Chapter 1
        |Content 1
        |## Section 1
        |Subcontent 1
        |## Section 2
        |Subcontent 2
        |# Chapter 2
        |Content 2
      """.stripMargin),

      Root(
        Chapter(None, "Chapter 1",
          Paragraph(Text("Content 1")),
          Section(None, "Section 1", Paragraph(Text("Subcontent 1"))),
          Section(None, "Section 2", Paragraph(Text("Subcontent 2")))
        ),
        Chapter(None, "Chapter 2", Paragraph(Text("Content 2")))
      )
    )
  }

  test("Quoted") {
    assertEquals(
      Pegdown.parse("\"test\""),
      Root(Paragraph(Text("\"test\"")))
    )
  }
}