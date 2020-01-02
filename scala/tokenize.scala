// clone ocre-texts in adjacent directory:
val mappingsDir = "../ocre-texts/mappings"
val ricTextCexUrl = "https://raw.githubusercontent.com/neelsmith/nomisma/master/cex/ric-1-3-cts.cex"


// 1. Add maven repository where we can find our libraries
val myBT = coursierapi.MavenRepository.of("https://dl.bintray.com/neelsmith/maven")
interp.repositories() ++= Seq(myBT)

// 2. Build text corpus first.
import $ivy.`edu.holycross.shot::ohco2:10.18.1`
import $ivy.`edu.holycross.shot.cite::xcite:4.2.0`

import edu.holycross.shot.ohco2._
import edu.holycross.shot.cite._

/// OHCO2 CORPUS FOR RIC 1-3:
val corpus = CorpusSource.fromUrl(ricTextCexUrl)
corpus.size

// EXPAND CORPUS FROM MAPPINGS FILES IN ocre-text:
import $ivy.`edu.holycross.shot::ocre-texts:0.3.1`

import edu.holycross.shot.ocre._

val lastN = 22
val mappings = TextExpander.loadNMappings(lastN, baseDirectory = mappingsDir)

val expanded = TextExpander.expandText(corpus, mappings)




import $ivy.`edu.holycross.shot::midvalidator:9.2.0`
import $ivy.`edu.holycross.shot::latphone:2.7.2`
import $ivy.`edu.holycross.shot::latincorpus:2.2.1`
