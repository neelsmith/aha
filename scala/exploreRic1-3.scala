// 1. Add maven repository where we can find our libraries
val myBT = coursierapi.MavenRepository.of("https://dl.bintray.com/neelsmith/maven")
interp.repositories() ++= Seq(myBT)

// 2. Build text corpus first.
import $ivy.`edu.holycross.shot::ohco2:10.18.1`
import $ivy.`edu.holycross.shot.cite::xcite:4.2.0`

import edu.holycross.shot.ohco2._
import edu.holycross.shot.cite._

/// OHCO2 CORPUS FOR RIC 1-3:
val ricTextCexUrl = "https://raw.githubusercontent.com/neelsmith/nomisma/master/cex/ric-1-3-cts.cex"
val fullCorpus = CorpusSource.fromUrl(ricTextCexUrl)
// drop anonymous issues for this notebook:
val corpus = Corpus(fullCorpus.nodes.filterNot(_.urn.passageComponent.contains(".anys.")))


val textsByAuth = corpus.nodes.groupBy(_.urn.collapsePassageTo(2).passageComponent)
val obvnodes = corpus.nodes.filter(_.urn.passageComponent.contains("obv"))
val revnodes = corpus.nodes.filter(_.urn.passageComponent.contains("rev"))


println("Texts in RIC 1-3: " + fullCorpus.size)
println("Texts without anon. authority: " + corpus.size)
println("Texts on coins of " + textsByAuth.size + " authorities.")

/// OCRE CORPUS
import $ivy.`edu.holycross.shot::nomisma:3.1.1`
/*
import $ivy.`edu.holycross.shot::histoutils:2.2.0`
import $ivy.`org.plotly-scala::plotly-almond:0.7.1`
import $ivy.`edu.holycross.shot::midvalidator:9.2.0`
import $ivy.`edu.holycross.shot::latphone:2.7.2`
import $ivy.`edu.holycross.shot::latincorpus:2.2.1`
*/
import edu.holycross.shot.nomisma._
val ocreCexUrl = "https://raw.githubusercontent.com/neelsmith/nomisma/master/cex/ocre-cite-ids.cex"
val ocre = OcreSource.fromUrl(ocreCexUrl)

val byAuths = ocre.datable.byAuthority

// Grouping by authority makes it easy to drop out RIC 4
// issues that are contemporary with last of Commodus
// by using the point average of his issues' dates, since
// other authorities issuing in 193 will have an average
// date point equal to or greater than 193.  This also
// makes it easy to omit anonymous issues.
val lt193ByAuth = byAuths.filter(_._2.dateRange.pointAverage < 193).filterNot(_._1 == "anonymous")

val relevantIssues = lt193ByAuth.map(_._2.issues).flatten
val lt193 = Ocre(relevantIssues)
// Check that we got RIC 3 issues as late as 193 CE:
println("Total date range: " + lt193.dateRange.toString(" - "))


// Now let's compare counts of coin records and text nodes:
println(lt193.size + " coin records.")
println(s"\t${lt193.hasObvLegend.size} with obv. legend ")
println(s"\t${lt193.hasRevLegend.size} with rev. legend ")

println(corpus.size + " texts total:")
println(s"\t${obvnodes.size} obv. legends")
println(s"\t${revnodes.size} rev. legends")

val textSet = corpus.nodes.map(_.urn.collapsePassageBy(1).passageComponent).distinct.toSet
val coinSet = lt193.issues.map(_.id).toSet

println("Unique coin ids: " + coinSet.size)
println("Unique coins implied by texts: " + textSet.size)

val idDiffSet = textSet diff coinSet
val idDiff = idDiffSet.toVector
println(idDiff.size)

import java.io.PrintWriter
new PrintWriter("id-diffs.txt"){write(idDiff.mkString("\n")); close;}


val missingMarcusAurelius = idDiff.filterNot(_.startsWith("1_2.cw"))
val civilwars = idDiff.filter(_.startsWith("1_2.cw"))

new PrintWriter("missing-marc-aur.txt"){write(missingMarcusAurelius.mkString("\n"));close;}
new PrintWriter("missing-cw.txt"){write(civilwars.mkString("\n"));close;}


// Verify that all missing M. Aurelius coins are undated.
val dateRanges = for (id <- missingMarcusAurelius) yield {
    ocre.issue(id).get.dateRange
}
require(dateRanges.flatten.isEmpty)


for (id <- civilwars) {
  val coin = ocre.issue(id).get
  val legends =  id + ", obv. " + coin.obvLegend + " rev. " + coin.revLegend
  println(legends)
}


val cwLib = "1_2.cw.133"
corpus.nodes.filter(_.urn.passageComponent.startsWith(cwLib))
///

val legends = for (id <- missingMarcusAurelius) yield {
  val legends =  id + ", obv. " + coin.obvLegend + " rev. " + coin.revLegend
  legends
}

println(legends.mkString("\n"))
