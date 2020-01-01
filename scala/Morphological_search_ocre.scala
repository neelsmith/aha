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

val url = "https://raw.githubusercontent.com/neelsmith/hctexts/master/cex/ocre43k.cex"
val corpus = CorpusSource.fromUrl(url, cexHeader = true)

val fstUrl = "https://raw.githubusercontent.com/neelsmith/hctexts/master/workfiles/ocre/ocre-fst.txt"
val fstLines = Source.fromURL(fstUrl).getLines.toVector

val ocrelat = LatinCorpus.fromFstLines(corpus, Latin24Alphabet, fstLines, strict = false)


// Find coin IDs where a specified lexeme occurs.
def findLexemeOccurrences(lexemeId: String) : Vector[String] = {
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

/*
val token2 = "liberalitas"
val liberalitasCoins = findOccurrences(token2, ocrelat)
println("Found " + liberalitasCoins.size + " coins with legends including a form of 'liberalitas'")
*/

val aug = findOccurrences("avgvsto", ocrelat)
println(aug.size)
//println(ocrelat.tokens.map(_.urn.collapsePassageTo(2).passageComponent).distinct.sorted.mkString("\n"))

val commodus = ocrelat.tokens.filter(_.urn.passageComponent.startsWith("3.com"))

commodus.size

commodus.filter(_.urn.passageComponent.startsWith("3.com.171"))
