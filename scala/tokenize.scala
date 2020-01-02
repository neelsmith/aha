// clone ocre-texts in adjacent directory:
val mappingsDir = "../ocre-texts/mappings"
val ricTextCexUrl = "https://raw.githubusercontent.com/neelsmith/nomisma/master/cex/ric-1-3-cts.cex"

import java.io.File
def collectFilesByExtension(dirName: String, extension: String = "csv"): Vector[String] = {
  val dir = new File(dirName)
  if (dir.exists && dir.isDirectory) {
     dir.listFiles.filter(_.isFile).filter(_.getName.endsWith(extension)).map(_.getName).toVector
  } else {
    Vector.empty[String]
  }
}


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


// Kludgy sequential retrieval because I'm hitting stack overflow
// with recursion:
val baseDir = "/Users/nsmith/Desktop/AHA-work/aha"

val m1maps = collectFilesByExtension(baseDir + "/mappings/m1").map(f => baseDir + "/mappings/m1/" + f)
val m1 = TextExpander.loadMappings(m1maps)
val exp1 = TextExpander.expandText(corpus, m1)

val m2maps = collectFilesByExtension(baseDir + "/mappings/m2").map(f => baseDir + "/mappings/m2/" + f)
val m2 = TextExpander.loadMappings(m2maps)
val exp2 = TextExpander.expandText(exp1, m2)



val m3maps = collectFilesByExtension(baseDir + "/mappings/m3").map(f => baseDir + "/mappings/m3/" + f)
val m3 = TextExpander.loadMappings(m3maps)
val exp3 = TextExpander.expandText(exp2, m3)



val m4maps = collectFilesByExtension(baseDir + "/mappings/m4").map(f => baseDir + "/mappings/m4/" + f)
val m4 = TextExpander.loadMappings(m4maps)
val exp4 = TextExpander.expandText(exp3, m4)


val m5maps = collectFilesByExtension(baseDir + "/mappings/m5").map(f => baseDir + "/mappings/m5/" + f)
val m5 = TextExpander.loadMappings(m5maps)
val expanded = TextExpander.expandText(exp4, m5)


//val lastN = 22
//val mappings = TextExpander.loadNMappings(lastN, baseDirectory = mappingsDir)





import $ivy.`edu.holycross.shot::midvalidator:9.2.0`
import $ivy.`edu.holycross.shot::latphone:2.7.2`
import $ivy.`edu.holycross.shot::latincorpus:2.2.1`
