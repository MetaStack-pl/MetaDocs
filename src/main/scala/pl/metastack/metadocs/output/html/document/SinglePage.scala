package pl.metastack.metadocs.output.html.document

import java.io.File

import pl.metastack.metadocs.FileUtils
import pl.metastack.metadocs.document.tree
import pl.metastack.metadocs.document.{Meta, Extractors}
import pl.metastack.metadocs.output.HTML
import pl.metastack.metadocs.output.html.Components

import pl.metastack.metaweb._
import pl.metastack.{metaweb => web}

object SinglePage {
  def write(root: tree.Root,
            skeleton: Components.Skeleton,
            outputPath: String,
            meta: Option[Meta],
            toc: Boolean,
            tocDepth: Int = 3) {
    def referenceUrl(id: String) = s"#$id"
    val writer = new HTML(referenceUrl)

    val footnotes = Extractors.footnotes(root)

    val body = web.tree.Container(Seq(
      Components.header(meta),
      Components.toc(root, tocDepth, referenceUrl),
      Components.`abstract`(meta),
      writer.root.write(root),
      Components.footnotes(writer, footnotes)
    ))

    val result = skeleton(meta, None, Components.bodyWrapper(body))

    FileUtils.printToFile(new File(outputPath)) { fw =>
      fw.write(result.state.toHtml)
    }
  }
}