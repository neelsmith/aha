
// 1. Add maven repository where we can find our libraries
val myBT = coursierapi.MavenRepository.of("https://dl.bintray.com/neelsmith/maven")
interp.repositories() ++= Seq(myBT)

// 2. Make libraries available with `$ivy` imports:
import $ivy.`edu.holycross.shot::nomisma:3.0.0`
import $ivy.`edu.holycross.shot::histoutils:2.2.0`
import $ivy.`org.plotly-scala::plotly-almond:0.7.1`

import edu.holycross.shot.nomisma._
// Import OCRE data -- from URL, or a local file:
//
//val ocreCex = "https://raw.githubusercontent.com/neelsmith/nomisma/master/cex/ocre-cite-ids.cex"
//val ocre = OcreSource.fromUrl(ocreCex)
//
val ocreCexFile = "ocre-cite-ids.cex"
val ocre = OcreSource.fromFile(ocreCexFile)

// Sanity check:
println(ocre.size + " records loaded.")


// Import plotly libraries, and set display defaults suggested for use in Jupyter NBs:
import plotly._, plotly.element._, plotly.layout._, plotly.Almond._
repl.pprinter() = repl.pprinter().copy(defaultHeight = 3)

import edu.holycross.shot.histoutils._



// Use the date range object:
println("Total number of issues in OCRE: " + ocre.size)
println("Number of datable issues: " + ocre.datable.size)

println("Chronological range of issues in OCRE: " + ocre.dateRange.toString(" - "))
println("Span of years: " + ocre.dateSpan)


val avgFreq = ocre.size * 1.0f / ocre.dateSpan
println("Average annual rate of striking: " + avgFreq + " issues per year")

val groupedByYear = ocre.datable.issues.groupBy(_.dateRange.get.pointAverage)
val yearFreqs = groupedByYear.toVector.map{ case (yr, vect) => Frequency(yr, vect.size)}
val yearHisto = edu.holycross.shot.histoutils.Histogram(yearFreqs)

val years = yearHisto.frequencies.map(_.item)
val yearCounts = yearHisto.frequencies.map(_.count)

val annualPlot = Seq(
  Bar(x = years, y = yearCounts)
)


val legendsLayout = Layout(
  title = "Frequency of issues, 32 BCE - 491 CE",
  showlegend = false,
  yaxis = Axis(title = "Number of issues"),
  xaxis = Axis(title = "Year CE"),
  height = 600,
  width = 900
)

plot(annualPlot, legendsLayout)

val byAuthorityChronological = ocre.datable.byAuthority
val authNames = byAuthorityChronological.map(_._1)
val authOcres =  byAuthorityChronological.map(_._2)

// THis is slow: be patient
val summaries = for (auth <- authNames) yield {
    print(auth + ": ")
    val ocreForAuth =  Ocre(ocre.issuesForAuthority(auth))
    println(ocreForAuth.size + " issues in " + ocreForAuth.dateSpan + " years.")
    println(" == " + ocreForAuth.size * 1.0f / ocreForAuth.dateSpan)
    (auth, ocreForAuth.size, ocreForAuth.dateSpan,  ocreForAuth.size * 1.0f / ocreForAuth.dateSpan)
}

val issueFreqPlot = Seq(
  Bar(x = authNames, y = summaries.map(_._4))
)

val authorityFreqLayout = Layout(
  title = "Annual frequency of issues grouped by authority",
  showlegend = false,
  yaxis = Axis(title = "Average number of issues per year"),
  xaxis = Axis(title = "Issuing authority"),
  height = 600,
  width = 900
)
plot(issueFreqPlot, authorityFreqLayout)

plot(annualPlot)
