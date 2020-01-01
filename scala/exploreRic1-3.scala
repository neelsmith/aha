// 1. Add maven repository where we can find our libraries
val myBT = coursierapi.MavenRepository.of("https://dl.bintray.com/neelsmith/maven")
interp.repositories() ++= Seq(myBT)

// 2. Make libraries available with `$ivy` imports:
import $ivy.`edu.holycross.shot::nomisma:3.1.1`
import $ivy.`edu.holycross.shot::histoutils:2.2.0`
import $ivy.`org.plotly-scala::plotly-almond:0.7.1`
import $ivy.`edu.holycross.shot::midvalidator:9.2.0`
import $ivy.`edu.holycross.shot::latphone:2.7.2`
import $ivy.`edu.holycross.shot::latincorpus:2.2.1`

import $ivy.`edu.holycross.shot::ohco2:10.18.1`
import $ivy.`edu.holycross.shot.cite::xcite:4.2.0`
import edu.holycross.shot.ohco2._
import edu.holycross.shot.cite._



/// OHCO2 CORPUS FOR RIC 1-3:
val ricTextCexUrl = "https://raw.githubusercontent.com/neelsmith/nomisma/master/cex/ric-1-3-cts.cex"
val corpus = CorpusSource.fromUrl(ricTextCexUrl)
println("Texts in RIC 1-3: " + corpus.size)
val textsByAuth = corpus.nodes.groupBy(_.urn.collapsePassageTo(2).passageComponent)
textsByAuth.size

/// OCRE CORPUS
import edu.holycross.shot.nomisma._
val ocreCexUrl = "https://raw.githubusercontent.com/neelsmith/nomisma/master/cex/ocre-cite-ids.cex"
val ocre = OcreSource.fromUrl(ocreCexUrl)

val byAuths = ocre.datable.byAuthority

val lt200 = byAuths.filter(_._2.dateRange.pointAverage < 199)
val gt200 = byAuths.filter(_._2.dateRange.pointAverage >= 199)

gt200.size

val authDates = byAuths.map{ case(auth,ocr) => (auth, ocr.dateRange)}

println(authDates.sortBy(_._2.pointAverage).filterNot(_._1 == "anonymous").map{ case (a,d) => a + ", " + d.toString("-") }.mkString("\n"))
