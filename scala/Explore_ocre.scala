
// 1. Add maven repository where we can find our libraries
val myBT = coursierapi.MavenRepository.of("https://dl.bintray.com/neelsmith/maven")
interp.repositories() ++= Seq(myBT)

// 2. Make libraries available with `$ivy` imports:
import $ivy.`edu.holycross.shot::nomisma:3.0.0`
import $ivy.`edu.holycross.shot::histoutils:2.2.0`
import $ivy.`org.plotly-scala::plotly-almond:0.7.1`

import edu.holycross.shot.nomisma._
val ocreCex = "https://raw.githubusercontent.com/neelsmith/nomisma/master/cex/ocre-cite-ids.cex"
val ocre = OcreSource.fromUrl(ocreCex)

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



val libRevv =
  Ocre(ocre.hasRevLegend.issues.filter(_.revLegend.contains("LIB")))

val libObvv = ocre.hasObvLegend.issues.filter(_.obvLegend.contains("LIB"))

libRevv.datable.issues.filter(_.dateRange.get.pointAverage > 200).size


val libRevvByYear = libRevv.datable.issues.groupBy(_.dateRange.get.pointAverage)
val libRevvYearFreqs = libRevvByYear.toVector.map{ case (yr, vect) => Frequency(yr, vect.size)}
val libRevvYearHisto = edu.holycross.shot.histoutils.Histogram(libRevvYearFreqs)

val years = libRevvYearHisto.frequencies.map(_.item)
val yearCounts = libRevvYearHisto.frequencies.map(_.count)
val annualPlot = Seq(
  Bar(x = years, y = yearCounts)
)

println(libRevv.issues.map(_.revLegend).distinct.mkString("\n"))


plot(annualPlot)
/*
val groupedByYear = ocre.datable.issues.groupBy(_.dateRange.get.pointAverage)
val yearFreqs = groupedByYear.toVector.map{ case (yr, vect) => Frequency(yr, vect.size)}
val yearHisto = edu.holycross.shot.histoutils.Histogram(yearFreqs)

val years = yearHisto.frequencies.map(_.item)
val yearCounts = yearHisto.frequencies.map(_.count)

val annualPlot = Seq(
  Bar(x = years, y = yearCounts)
)
plot(annualPlot)

val byAuthorityChronological = ocre.datable.byAuthority
val authNames = byAuthorityChronological.map(_._1)
val authOcres =  byAuthorityChronological.map(_._2)


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
plot(issueFreqPlot)

plot(annualPlot)
*/
