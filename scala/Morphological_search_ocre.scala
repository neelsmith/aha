// 1. Add maven repository where we can find our libraries
val myBT = coursierapi.MavenRepository.of("https://dl.bintray.com/neelsmith/maven")
interp.repositories() ++= Seq(myBT)

// 2. Make libraries available with `ivy` imports:
import $ivy.`edu.holycross.shot::ohco2:10.18.1`
import $ivy.`edu.holycross.shot::midvalidator:9.2.0`
import $ivy.`edu.holycross.shot::latphone:2.7.2`
import $ivy.`edu.holycross.shot::latincorpus:2.2.1`

//Import all libraries:
import edu.holycross.shot.ohco2._
import scala.io.Source
import edu.holycross.shot.mid.validator._
import edu.holycross.shot.latin._
import edu.holycross.shot.latincorpus._

val url = "https://raw.githubusercontent.com/neelsmith/hctexts/master/cex/ric-1-3.cex"
val corpus = CorpusSource.fromUrl(url, cexHeader = true)

// use a local file, or retrieve from github:
val fstFile = "aha/ric1-3-parses.txt"
val fstLines = Source.fromFile(fstFile).getLines.toVector

/*
val fstUrl = "https://raw.githubusercontent.com/neelsmith/hctexts/master/workfiles/ric1-3/ric1-3-parses.txt"
val fstLines = Source.fromURL(fstUrl).getLines.toVector
*/
val ocrelat = LatinCorpus.fromFstLines(corpus, Latin24Alphabet, fstLines, strict = false)

ocrelat.size



ocrelat.tokenLexemeIndex("libertas")
ocrelat.lexemeConcordance("ls.n26481")

ocrelat.tokens(0).analyses(0).lemmaId

ocrelat.tokens(0).urn

val tokenLemmaMap = ocrelat.tokens.map(t => (t.urn, t.analyses.map(_.lemmaId).distinct))


val lexicallyAmbiguous =  tokenLemmaMap.filter(_._2.size > 1)
lexicallyAmbiguous.isEmpty

val tokenLemmaPair = tokenLemmaMap.filter(_._2.nonEmpty).map{ case (t,v) => (t, v(0))}

val libertas = tokenLemmaPair.filter(_._2 == "ls.n26481")
libertas.size


// Find coin IDs where a specified lexeme occurs.
def findLexemeOccurrences(lexemeId: String) : Vector[String] = {
  // THIS IS BROKEN!
    val occurrences =  ocrelat.lexemeConcordance(lexemeId)
    // Convert text references to coin IDs:
    occurrences.map(_.collapsePassageBy(1).passageComponent)
}


// Find coin IDs where any form of a given token appears
def findOccurrences(tkn : String, latCorpus: LatinCorpus) : Vector[String] = {
    val lexemeIds = latCorpus.tokenLexemeIndex(tkn)
    if (lexemeIds.size == 1){
        val lexemeId = lexemeIds(0)
        findLexemeOccurrences(lexemeId)

    } else {
      println("Found " + lexemeIds.size + " lexemes for " + tkn)
      Vector.empty[String]
    }
}


val token = "libertas"
val libertasCoins = findOccurrences(token, ocrelat)

println("Found " + libertasCoins.size + " coins with legends including a form of 'libertas'")


val aug = findOccurrences("avgvsto", ocrelat)
println(aug.size)
//println(ocrelat.tokens.map(_.urn.collapsePassageTo(2).passageComponent).distinct.sorted.mkString("\n"))
