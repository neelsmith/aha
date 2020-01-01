
// 1. Add maven repository where we can find our libraries
val myBT = coursierapi.MavenRepository.of("https://dl.bintray.com/neelsmith/maven")
interp.repositories() ++= Seq(myBT)

// 2. Make libraries available with `$ivy` imports:
import $ivy.`edu.holycross.shot::nomisma:3.1.1`
import $ivy.`edu.holycross.shot::histoutils:2.2.0`
import $ivy.`org.plotly-scala::plotly-almond:0.7.1`

import edu.holycross.shot.nomisma._
val ocreCex = "https://raw.githubusercontent.com/neelsmith/nomisma/master/cex/ocre-cite-ids.cex"
val ocre = OcreSource.fromUrl(ocreCex)

// Sanity check:
println(ocre.size + " records loaded.") 


import scala.io.Source
val mintsCsv = "https://raw.githubusercontent.com/neelsmith/nomisma/master/tables/mintpoints.csv"
val mintsData = Source.fromURL(mintsCsv).getLines.toVector


val mints = MintPointCollection(mintsData.mkString("\n"), ",")
println("Number mints: " + mints.size)

val ocreGeo = Ocre(ocre.issues, mints)

val countsForMints = ocreGeo.hasMint.issues.groupBy(_.mint).map{ case (mint, iss) => (mint, iss.size)}
                                                                                                                           
//

val geoCsv = for (mnt <- countsForMints.keySet.toVector) yield {
    val geoData = mints.forMint(mnt)
    geoData match {
        case None => {
            println("No geo data for " + mnt)
            ""
        }
        case _ =>  {
            val csv = mnt + "," + geoData.get.pt + "," + countsForMints(mnt)
            //println(csv)
            csv
        }
    }  
}
val validGeo = geoCsv.filter(_.nonEmpty)
println(validGeo.size + " locatable mints")
println("mint,lon,lat,issues")
println(validGeo.mkString("\n"))

//mints.forMint("rome").get.map(pt => pt.mint + "," + pt.pt)
//println(mints.mintPoints.filter(_.mint == "ravenna"))

val byAuthorityChronological = ocre.hasMint.byAuthority
val authNames = byAuthorityChronological.map(_._1)
val authOcres =  byAuthorityChronological.map(_._2)


val summaries = for (auth <- authNames) yield {

    val ocreForAuth =  Ocre(ocre.issuesForAuthority(auth))
    val countsForMints = ocreForAuth.hasMint.issues.groupBy(_.mint).map{ case (mint, iss) => (mint, iss.size)}
    val csvLines = for (mintCount <- countsForMints) yield {
        val mnt =  mintCount._1
        val issueCount =  mintCount._2
        val geoData = mints.forMint(mnt)
        geoData match {
            case None => {
                println("No geo data for " + mnt)
                ""
            }
            case _ =>  {
                val csv = auth + "," + mnt + "," + geoData.get.pt + "," + issueCount
                //println(csv)
                csv
            }
        }   
    }
    csvLines.toVector.filter(_.nonEmpty)
}



println("authority,mint,lon,lat,issues")
println(summaries.flatten.mkString("\n"))



// If you're running this locally and want to save the CSV data to a file,
// uncomment these two lines:
//import java.io.PrintWriter
//new PrintWriter("mint-issues-by-auth.csv"){write("authority,mint,lon,lat,issues\n" + summaries.flatten.mkString("\n")); close;}


