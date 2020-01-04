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

// Given the text of a lexical token, find all occurrences
// in the corpus of any form of its lexeme.
def occurrencesFromForm(tokenText: String, latinCorpus: LatinCorpus) : Vector[CtsUrn] = {
  val lexemeUrns = latinCorpus.tokenLexemeIndex(tokenText)
  // we've already verified that we have no lexical ambiguity,
  // so can just take the first lexeme ID
  val lexemeUrn = lexemeUrns(0)
  println("Lexeme for " + tokenText + " is " + lexemeUrn)
  val tokenLemmaMap = latinCorpus.tokens.map(t => (t.urn, t.analyses.map(_.lemmaId).distinct))
  require(tokenLemmaMap.filter(_._2.size > 1).isEmpty, "Some tokens derive from more than one possible lexeme.")
  val tokenLemmaPair = tokenLemmaMap.filter(_._2.nonEmpty).map{ case (t,v) => (t, v(0))}


  tokenLemmaPair.filter(_._2 == lexemeUrn).map(_._1)
}

// "ls.n26481"
val libertasOccurs = occurrencesFromForm("libertas", ocreTokens)
libertasOccurs.size


// FIND HOW MANY OF THESE OCCUR PER TOKEN?
ocreTokens.tokens(0).analyses(0).toString


// group text passages by issuing authority, by using the
// first two pieces of URN's passage component:
val byAuth = libertasOccurs.groupBy( _.collapsePassageTo(2))

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



// Assemble normalized/expanded text for each passage.
val psgUrns = libertasOccurs.map(_.collapsePassageBy(1).addVersion("expanded"))
// THIS IS HORRIFICALLY SLOW.  Be patient: it does work.
println("Examining " + libertasOccurs.size + " legends.")
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


txts.size
println(txts.distinct.sorted.mkString("\n"))


import java.io.PrintWriter
new PrintWriter("libertas-legends.txt") { write(txts.distinct.sorted.mkString("\n"));close;}



// Change this to work by index I guess.
// Map each legend to a ramp?
val colorMap : Map[String, Color.RGB] = Map(

"libertas restitvta" -> Color.RGB(250,0, 0),
"libertas popvli romani" -> Color.RGB(0,250,0),
"libertas" -> Color.RGB(0,0, 250),
"adsertor/salvs/pax" -> Color.RGB(10,10,10),
"libertas avgvsti" -> Color.RGB(0,180,180)
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


// Classify libertas issues

val libertasClasses = Map(
"adsertor libertatis" -> "adsertor/salvs/pax",
"imperator caesar divi filivs consvl Ⅵ libertatis popvli romani vindex" -> "libertas popvli romani",
"imperator Ⅱ consvl Ⅱ pater patriae senatvs consvlto libertas avgvsti" -> "libertas avgvsti",
"imperator Ⅱ senatvs consvlto libertas senatvs consvlto" -> "libertas",
"libertas" -> "libertas",
"libertas avgvsta" -> "libertas avgvsti",
"libertas avgvsta senatvs consvlto" ->  "libertas avgvsti",
"libertas avgvsti" ->  "libertas avgvsti",
"libertas avgvsti imperatoris Ⅱ consvlis patris patriae senatvs consvlto" ->  "libertas avgvsti",
"libertas avgvsti pontif" ->  "libertas avgvsti",
"libertas avgvsti pontifex maximvs tribvnicia potestate ⅩⅣ imperator Ⅷ consvl Ⅴ pater patriae senatvs consvlto" ->  "libertas avgvsti",
"libertas avgvsti pontificis maximi tribvnicia potestate ⅩⅠ imperatoris Ⅶ consvlis Ⅴ patris patriae senatvs consvlto" ->  "libertas avgvsti",
"libertas avgvsti popvli romani" ->  "libertas popvli romani",
"libertas avgvsti senatvs consvlto" -> "libertas avgvsti",
"libertas avgvsti senatvs consvlto remissa quadragensima" -> "libertas avgvsti",
"libertas avgvsti tribvnicia potestate Ⅵ imperatoris Ⅳ consvlis Ⅲ patris patriae senatvs consvlto" -> "libertas avgvsti",
"libertas consvl Ⅳ senatvs consvlto" -> "libertas",
"libertas pontifex maximvs tribvnicia potestate ⅩⅢ imperator ⅤⅢ consvl Ⅴ pater patriae" -> "libertas avgvsti",
"libertas pvblica" ->  "libertas popvli romani",
"libertas pvblica senatvs consvlto" -> "libertas popvli romani",
"libertas restitvta" -> "libertas restitvta",
"libertas restitvta senatvs consvlto" -> "libertas restitvta",
"libertati" -> "libertas",
"pax et libertas" ->  "adsertor/salvs/pax",
"pontifex maximvs tribvnicia potestate consvl Ⅲ libertas pvblica" -> "libertas popvli romani",
"pontifex maximvs tribvnicia potestate consvl Ⅲ senatvs consvlto libertas restitvta" -> "libertas restitvta",
"qvintvs cassivs libertas" -> "libertas",
"salvs et libertas" ->  "adsertor/salvs/pax",
"senatvs popvlvs+qve romanvs adsertori libertatis pvblicae" -> "libertas popvli romani"
)
libertasClasses.size
val traces = for (legend <- mapped.keySet) yield {
  //println(legend + ", " + colorMap(legend))
  val colorClass = libertasClasses(legend)
  Scatter(
    //datedTextHisto.frequencies.map(_.item),
    mapped(legend).toVector.map(_._1),
    mapped(legend).toVector.map(_._2),
    text = legend, //+ mapped(legend).toString,
    name = legend,
    //datedTextHisto.frequencies.map(_.count),
    //text = datedTextHisto.frequencies.map(_.toString),
    mode = ScatterMode(ScatterMode.Markers),
    marker = Marker(
      size = 8,
      color =  colorMap(colorClass)
    )
  )
}
val tracesData = traces.toSeq

val legendsLayout = Layout(
  title = "Legends with 'libertas'",
  showlegend = false,
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




println(libertasClasses.values.toVector.distinct.mkString("\n"))

///
