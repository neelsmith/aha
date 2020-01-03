// Multi-dimensional plot:  distinct legends containing
// any form of a lexeme, clustered by year.
//
// 1. Add maven repository where we can find our libraries
val myBT = coursierapi.MavenRepository.of("https://dl.bintray.com/neelsmith/maven")
interp.repositories() ++= Seq(myBT)

// 2. Start with OHCO2 Corpus:
import $ivy.`edu.holycross.shot::ohco2:10.18.1`
import $ivy.`edu.holycross.shot.cite::xcite:4.2.0`
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._

val url = "https://raw.githubusercontent.com/neelsmith/hctexts/master/cex/ric-1-3.cex"
val corpus = CorpusSource.fromUrl(url, cexHeader = true)

// use a local file, or retrieve from github:
import scala.io.Source
val fstFile = "aha/ric1-3-parses.txt"
val fstLines = Source.fromFile(fstFile).getLines.toVector

// Combine citable corpus with morphological data to create
// a corpus of parsed tokens:
import $ivy.`edu.holycross.shot::midvalidator:9.2.0`
import $ivy.`edu.holycross.shot::latphone:2.7.2`
import $ivy.`edu.holycross.shot::latincorpus:2.2.1`


import edu.holycross.shot.mid.validator._
import edu.holycross.shot.latin._
import edu.holycross.shot.latincorpus._
val ocreTokens = LatinCorpus.fromFstLines(corpus, Latin24Alphabet, fstLines, strict = false)


// Import everything we'll use for plotting
import $ivy.`org.plotly-scala::plotly-almond:0.7.1`
import $ivy.`edu.holycross.shot::nomisma:3.1.1`
import edu.holycross.shot.nomisma._
import plotly._, plotly.element._, plotly.layout._, plotly.Almond._
repl.pprinter() = repl.pprinter().copy(defaultHeight = 3)




/*
// Collect occurences for a given token's lexemes:
val token = "libertas"
val lexemeUrns = ocreTokens.tokenLexemeIndex(token)
// here, we assume there's only one matching lexeme:
val lexemeUrn = lexemeUrns(0)

// This breaks on a corpus like RIC 1-3 where some
// tokens are not categorized:
//val occurrences =  ocreTokens.lexemeConcordance(lexemeUrn)
//
// This work around does the trick:
val tokenLemmaMap = ocreTokens.tokens.map(t => (t.urn, t.analyses.map(_.lemmaId).distinct))
// Verify that all tokens are lexically unambiguous, before
// reducing data to simple pairing of each parsed token
// with a single lexeme ID:
require(tokenLemmaMap.filter(_._2.size > 1).isEmpty, "Some tokens derive from more than one possible lexeme.")
val tokenLemmaPair = tokenLemmaMap.filter(_._2.nonEmpty).map{ case (t,v) => (t, v(0))}
*/

def occurrencesFromForm(tokenText: String, latinCorpus: LatinCorpus): Vector[String] = {
  val lexemeUrns = latinCorpus.tokenLexemeIndex(tokenText)
  // we've already verified that we have no lexical ambiguity,
  // so can just take the first lexeme ID
  val lexemeUrn = lexemeUrns(0)
  val tokenLemmaMap = latinCorpus.tokens.map(t => (t.urn, t.analyses.map(_.lemmaId).distinct))
  val tokenLemmaPair = tokenLemmaMap.filter(_._2.nonEmpty).map{ case (t,v) => (t, v(0))}
  tokenLemmaPair.filter(_._2 == lexemeUrn).map(_._2)
}


val libertasOccurs = occurrencesFromForm("libertas", ocreTokens)
libertasOccurs.size


// group text passages by issuing authority, by using the
// first two pieces of URN's passage component:
val byAuth = occurrences.groupBy( _.collapsePassageTo(2))
import edu.holycross.shot.histoutils._
val libFreqs = for (auth <- byAuth.keySet) yield {
  val parts = auth.passageComponent.split("\\.")
  println(parts(1) + ": " + byAuth(auth).size + " issues")
  Frequency(parts(1), byAuth(auth).size)
}

val libHist = edu.holycross.shot.histoutils.Histogram(libFreqs.toVector)
val libAuths = libHist.frequencies.map(_.item)
val libCounts = libHist.frequencies.map(_.count)

val libIssuesPlot = Seq(
  Bar(x = libAuths, y = libCounts)
)

plot(libIssuesPlot)



// Assemble normalized/expanded text for each passage:
val psgUrns = occurrences.map(_.collapsePassageBy(1).addVersion("expanded"))
println("Examining " + occurrences.size + " legends.")
val txts = for ((psg,idx) <- psgUrns.zipWithIndex) yield {
  val matchPassage = corpus.nodes.filter(_.urn ~~ psg)
  print(idx + 1 + ". ")
  matchPassage.distinct.size match {
    case 0 => {
      println("NO MATCH for " + psg.passageComponent)
      ""
    }
    case 1 => {
      println(psg.passageComponent + ": " + matchPassage(0).text)
      matchPassage(0).text
    }
    case _ => {
      println("Found " + matchPassage.distinct.size + " distinct matches  for " + psg.passageComponent)
      matchPassage(1).text
    }
  }
}


println(txts.distinct.sorted.mkString("\n"))

val colorMap : Map[String, Color.RGB] = Map(
"libertas avgvsta" -> Color.RGB(0,250,0),
"libertas avgvsti" -> Color.RGB(0,0, 250),
"libertas avgvsti senatvs consvlto" -> Color.RGB(100,100, 250),
"libertas pvblica" -> Color.RGB(200,100,0),
"libertas pvblica senatvs consvlto" -> Color.RGB(255,100,100),
"libertas restitvta senatvs consvlto" -> Color.RGB(255,0,0)
)

val expandedCorpus = Corpus(psgUrns.zip(txts).map{ case (u,t) => CitableNode(u,t) })
expandedCorpus.size

// Load ocre:
val ocreCex = "https://raw.githubusercontent.com/neelsmith/nomisma/master/cex/ocre-cite-ids.cex"
val ocre = OcreSource.fromUrl(ocreCex)

def coinForText(ocre: Ocre, legend: CtsUrn): Option[NomismaIssue] = {
  ocre.issue(legend.collapsePassageBy(1).passageComponent)
}

// Join passages and coins
val expandedWithCoins = for ((psg,idx) <- psgUrns.zipWithIndex) yield {
    val coin = coinForText(ocre, psg).get
    //(coin, psg.passageComponent, txts(idx))
    (coin,txts(idx))
}


println(expandedWithCoins(0))
val groupedByText = expandedWithCoins.groupBy(_._2)

def toOcre(srcIssues: Vector[NomismaIssue], outputIssues: Vector[OcreIssue] = Vector.empty[OcreIssue])  :  Vector[OcreIssue]= {

  if (srcIssues.isEmpty) {
    outputIssues
  } else {
    srcIssues.head match {
      case o: OcreIssue => {

        val newOutput = outputIssues ++ Vector(o)
        //println("Add " + o + " to output")
        //pritnln("New output size " + )
        toOcre(srcIssues.tail, newOutput)
      }
      case _ => {
        println("BAD DATA: not an OcreIssue: " + srcIssues.head)
        toOcre(srcIssues.tail, outputIssues)
      }
    }
  }
}


def traceForGroup(issues: ) = {

  val smallOcre = Ocre(issues)
  /*
  Scatter(
    datedTextHisto.frequencies.map(_.item),
    datedTextHisto.frequencies.map(_.count),
    text = datedTextHisto.frequencies.map(_.toString),
    mode = ScatterMode(ScatterMode.Markers),
    marker = Marker(
      size = 8,
      color = Color.RGB(250,100,100)
    )
  )*/
}


val textKeys = groupedByText.keySet.toVector
val miniOcres = for (keyVal <- textKeys) yield {
  val dataSet = groupedByText(keyVal).map(_._1)
  val ocre = toOcre(dataSet)
  //println(keyVal + ", " + dataSet.size)
  (keyVal -> Ocre(ocre))
}



val datedTextGroups = miniOcres.map { case(legend, miniOcre) => {
  (legend, miniOcre.issues.map(issue => (issue.dateRange.get.pointAverage, legend)).groupBy(_._1).map{ case (k,v) => (k, v.size)})
}}


val mapped = datedTextGroups.toMap //("libertas avgvsti"))

mapped("libertas avgvsti").toVector.map(_._1)
val traces = for (legend <- mapped.keySet) yield {
  //println(legend + ", " + colorMap(legend))
  Scatter(
    //datedTextHisto.frequencies.map(_.item),
    mapped(legend).toVector.map(_._1),
    mapped(legend).toVector.map(_._2),
    text = mapped(legend).toString,
    name = legend,
    //datedTextHisto.frequencies.map(_.count),
    //text = datedTextHisto.frequencies.map(_.toString),
    mode = ScatterMode(ScatterMode.Markers),
    marker = Marker(
      size = 8,
      color = colorMap(legend)
    )
  )
}
val tracesData = traces.toSeq

val legendsLayout = Layout(
  title = "Legends with 'libertas'",
  showlegend = true,
  yaxis = Axis(title = "Number of issues"),
  xaxis = Axis(title = "Year CE"),
  height = 600,
  width = 900
)


plot(tracesData, legendsLayout)

//////////////////////
val datedTexts = expandedWithCoins.map{ case (coin, legend) => (coin.dateRange.get.pointAverage, legend)}
// Plot bubble texts by year:
val datedGroups = datedTexts.groupBy(_._1) //.toVector.sortBy(_._1)

println(datedGroups(68).mkString("\n"))



val yrFreqs = for (yr <- datedGroups.keySet) yield {
  Frequency(yr, datedGroups(yr).size)
}
val datedTextHisto = edu.holycross.shot.histoutils.Histogram(yrFreqs.toVector).sorted

//datedTextHisto.total
//println(datedTextHisto.sorted.frequencies.mkString("\n"))
//datedTextHisto.frequencies
val txtTrace = Scatter(
  datedTextHisto.frequencies.map(_.item),
  datedTextHisto.frequencies.map(_.count),
  text = datedTextHisto.frequencies.map(_.toString),
  mode = ScatterMode(ScatterMode.Markers),
  marker = Marker(
    size = 8,
    color = Color.RGB(250,100,100)
  )
)


val txtData = Seq(txtTrace)
txtData
val txtLayout = Layout(
  title = "Libertas",
  showlegend = false,
  height = 400,
  width = 600
)
plot(txtData,txtLayout)





///
