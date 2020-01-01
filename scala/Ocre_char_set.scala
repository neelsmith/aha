
// 1. Add maven repository where we can find our libraries
val myBT = coursierapi.MavenRepository.of("https://dl.bintray.com/neelsmith/maven")
interp.repositories() ++=   
Seq(myBT)

// 2. Make libraries available with `$ivy` imports:
import $ivy.`edu.holycross.shot::nomisma:2.0.1`
//import $ivy.`edu.holycross.shot::ohco2:10.16.0`
//import $ivy.`edu.holycross.shot.cite::xcite:4.1.1`
import $ivy.`edu.holycross.shot::histoutils:2.2.0`
import $ivy.`org.plotly-scala::plotly-almond:0.7.1`

// 3. All scala imports, and configure plotly
import edu.holycross.shot.nomisma._
import edu.holycross.shot.histoutils._

import plotly._, plotly.element._, plotly.layout._, plotly.Almond._
// Set display defaults suggested for use in Jupyter NBs:
repl.pprinter() = repl.pprinter().copy(defaultHeight = 3)

val ocreCex = "https://raw.githubusercontent.com/neelsmith/nomisma/master/cex/ocre-cite-ids.cex"
val ocre = OcreSource.fromUrl(ocreCex)

// Sanity check:
val expectedIssues = 50644
require(ocre.size == expectedIssues) 

val obvLegends : Vector[String] = ocre.hasObvLegend.issues.map(_.obvLegend)
val revLegends : Vector[String]  = ocre.hasRevLegend.issues.map(_.revLegend)
val allLegends : Vector[String] = obvLegends ++ revLegends

val allChars = allLegends.map(_.toVector).flatten


println("Obv legends: " + obvLegends.size)
println("Rev legends: " + revLegends.size)
println("All: " + allLegends.size)
println("Total characters: " + allChars.size)

println("Average number of characters per legend: " + allChars.size / allLegends.size)

val charFreqsSeq = allChars.groupBy( c => c).map{ case (c,vect) => Frequency(c, vect.size)}
val charHistogram = edu.holycross.shot.histoutils.Histogram(charFreqsSeq.toVector).sorted
println("Total distinct characters: " + charHistogram.size)

val charValues = charHistogram.frequencies.map(_.item.toString)
val charCounts = charHistogram.frequencies.map(_.count)

val charHistPlot = Seq(
  Bar(x = charValues, y = charCounts)
)
plot(charHistPlot)
     

val threshhold = 600

// Find percent of two Ints:
def pct(i1: Int, i2: Int): Float = {
    i1 * 100.0f / i2
}

val rareChars = charHistogram.frequencies.filter(_.count < threshhold)
val rareTotal = edu.holycross.shot.histoutils.Histogram(rareChars).total
val threshholdPct = pct(rareTotal, allChars.size)

val lessRareChars = charHistogram.frequencies.filter(_.count >= threshhold)
val lessRareTotal = edu.holycross.shot.histoutils.Histogram(lessRareChars).total
val aboveThreshholdPct = 100 - threshholdPct

println("USING THRESHHOLD VALUE OF " + threshhold + ":")
println( "Percent of character occurrences of " + lessRareChars.size + " characters above threshhold: " + aboveThreshholdPct)
println("Percent of character occurrences of " + rareChars.size + " characters below threshhold: " + threshholdPct)



for (ch <- lessRareChars) {
    println(ch)
}


for (ch <- rareChars) {
    println(ch)
}

val lowerOs = allLegends.filter(_.contains("o")) 
val lowerRs =allLegends.filter(_.contains("r"))

println("Sample of ten legends with 'o's out of " + lowerOs.size + " (" + pct(lowerOs.size,  allLegends.size) + "% of legends)")
println(lowerOs.take(10).mkString("\n"))
println("\nSample of ten legends with 'r's out of " + lowerRs.size + " (" + pct(lowerRs.size,  allLegends.size) + "% of legends)")
println(lowerRs.take(10).mkString("\n"))




val distinctChars = rareChars.size + lessRareChars.size
println( distinctChars + " distinct chars")
println(rareChars.size + " rare ones:")
for (ch <- rareChars) {
    println(ch)
}

val allowedChars = "ABCDEFGHIKLMNOPQRSTVXYZ -•←|"

// True if String s composed only of allowable characters
def validOrtho(s: String, allowedCharacters: String = allowedChars) : Boolean = {
 
    val charChecks = for (c <- s.toVector) yield {
        allowedCharacters.contains(c)
    }
    val flatVals = charChecks.distinct
    (flatVals.size == 1) && (flatVals(0)== true)
}
println("Total valid characters: " + allowedChars.size)

val total = allLegends.size
val sheep = allLegends.filter(leg => validOrtho(leg))
val goats = allLegends.filterNot(leg => validOrtho(leg))

val sheepPct = sheep.size * 100.0f / allLegends.size
val goatsPct = goats.size * 100.0f / allLegends.size
println("Sheep: " + sheep.size + " (" + sheepPct + "% of " + allLegends.size + ")")
println("Goats: " + goats.size + " (" + goatsPct + "% of " + allLegends.size + ")")

val total = 2144309

100.0f / total


