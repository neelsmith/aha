// Load previously expanded corpus:
val expandedCex = "aha/texts/ric-1-3-expanded.cex"


// 1. Add maven repository where we can find our libraries
val myBT = coursierapi.MavenRepository.of("https://dl.bintray.com/neelsmith/maven")
interp.repositories() ++= Seq(myBT)

// 2. Build text corpus first.
import $ivy.`edu.holycross.shot::ohco2:10.18.1`
import $ivy.`edu.holycross.shot.cite::xcite:4.2.0`

import edu.holycross.shot.ohco2._
import edu.holycross.shot.cite._

/// OHCO2 EXPANDED CORPUS FOR RIC 1-3:
val corpus = CorpusSource.fromFile(expandedCex)
corpus.size

// Tokenize corpus ocre-text:
import $ivy.`edu.holycross.shot::ocre-texts:0.3.1`
import $ivy.`edu.holycross.shot::midvalidator:9.2.0`
import $ivy.`edu.holycross.shot::latphone:2.7.2`
import $ivy.`edu.holycross.shot::latincorpus:2.2.1`

import edu.holycross.shot.latin._
import edu.holycross.shot.latincorpus._
import edu.holycross.shot.mid.validator._
import edu.holycross.shot.ocre._


//val tcorpus = TokenizableCorpus(corpus, NormalizedLegendOrthography)
val validTexts = corpus.nodes.filter(n => NormalizedLegendOrthography.validString(n.text))
val invalidTexts = corpus.nodes.filterNot(n => NormalizedLegendOrthography.validString(n.text))

validTexts.size
invalidTexts.size

val goodContent = validTexts.map(_.text).distinct
val badContent = invalidTexts.map(_.text).distinct
badContent.size

import java.io.PrintWriter
new PrintWriter("ric1-3-unexpanded.txt"){write(badContent.mkString("\n")); close;}
