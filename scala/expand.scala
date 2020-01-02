// 1. Add maven repository where we can find our libraries
val myBT = coursierapi.MavenRepository.of("https://dl.bintray.com/neelsmith/maven")
interp.repositories() ++= Seq(myBT)

// 2. Build text corpus first.
import $ivy.`edu.holycross.shot::ohco2:10.18.1`
import $ivy.`edu.holycross.shot.cite::xcite:4.2.0`
import $ivy.`edu.holycross.shot::ocre-texts:0.3.1`

import edu.holycross.shot.ohco2._
import edu.holycross.shot.cite._
import edu.holycross.shot.ocre._


import java.io.File
def collectFilesByExtension(dirName: String, extension: String = "csv"): Vector[String] = {
  val dir = new File(dirName)
  if (dir.exists && dir.isDirectory) {
     dir.listFiles.filter(_.isFile).filter(_.getName.endsWith(extension)).map(_.getName).toVector
  } else {
    Vector.empty[String]
  }
}

// Kludgy sequential retrieval because I'm hitting stack overflow
// with recursion:
val baseDir = "/Users/nsmith/Desktop/AHA-work/aha"

val m1maps = collectFilesByExtension(baseDir + "/mappings/m1").map(f => baseDir + "/mappings/m1/" + f)
val m1 = TextExpander.loadMappings(m1maps)
val exp1 = TextExpander.expandText(corpus, m1)
exp1.size

val m2maps = collectFilesByExtension(baseDir + "/mappings/m2").map(f => baseDir + "/mappings/m2/" + f)
val m2 = TextExpander.loadMappings(m2maps)
val exp2 = TextExpander.expandText(exp1, m2)
exp2.size



val m3maps = collectFilesByExtension(baseDir + "/mappings/m3").map(f => baseDir + "/mappings/m3/" + f)
val m3 = TextExpander.loadMappings(m3maps)
val exp3 = TextExpander.expandText(exp2, m3)
exp3.size

val m4maps = collectFilesByExtension(baseDir + "/mappings/m4").map(f => baseDir + "/mappings/m4/" + f)
val m4 = TextExpander.loadMappings(m4maps)
val exp4 = TextExpander.expandText(exp3, m4)
exp4.size


val m5maps = collectFilesByExtension(baseDir + "/mappings/m5").map(f => baseDir + "/mappings/m5/" + f)
val m5 = TextExpander.loadMappings(m5maps)
val exp5 = TextExpander.expandText(exp4, m5)
exp5.size



val m6maps = collectFilesByExtension(baseDir + "/mappings/m6").map(f => baseDir + "/mappings/m6/" + f)
val m6 = TextExpander.loadMappings(m6maps)
val exp6 = TextExpander.expandText(exp5, m6)
exp6.size


val m7maps = collectFilesByExtension(baseDir + "/mappings/m7").map(f => baseDir + "/mappings/m7/" + f)
val m7 = TextExpander.loadMappings(m7maps)
val expanded = TextExpander.expandText(exp6, m7)
expanded.size


import java.io.PrintWriter
new PrintWriter("ric-1-3-expanded.cex"){write(expanded.cex()); close;}
