
package views.html

import _root_.play.twirl.api.TwirlFeatureImports._
import _root_.play.twirl.api.TwirlHelperImports._
import _root_.play.twirl.api.Html
import _root_.play.twirl.api.JavaScript
import _root_.play.twirl.api.Txt
import _root_.play.twirl.api.Xml
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import java.lang._
import java.util._
import play.core.j.PlayMagicForJava._
import play.mvc._
import play.api.data.Field
import play.data._
import play.core.j.PlayFormsMagicForJava._
import scala.jdk.CollectionConverters._

object welcome extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template3[SearchCacheStore,String,String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(cacheResult: SearchCacheStore,error:String, style: String = "java"):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*2.1*/("""
"""),_display_(/*3.2*/defining(play.core.PlayVersion.current)/*3.41*/ { version =>_display_(Seq[Any](format.raw/*3.54*/("""

"""),format.raw/*5.1*/("""<section id="top">
	<div class="wrapper">
		<h1>Gitterific</h1>
	</div>
</section>
<div
	style="margin-left: 18%; margin-right: 18%; margin-bottom: 1%; margin-top: 1%;">
	<div style="padding: 10px 0px; text-align: center">
		<form id="form" action="/" method="GET">
			<input type="search" id="searchField" name="search"
				placeholder="Enter Search Terms" />
			<button type="submit">Go!</button>
		</form>
	</div>
	<div>
		"""),_display_(/*20.4*/if(error != null)/*20.21*/ {_display_(Seq[Any](format.raw/*20.23*/("""
		"""),format.raw/*21.3*/("""<h2>Error: """),_display_(/*21.15*/error),format.raw/*21.20*/("""</h2>
		""")))}),format.raw/*22.4*/(""" """),_display_(/*22.6*/if(cacheResult != null)/*22.29*/ {_display_(Seq[Any](format.raw/*22.31*/(""" """),_display_(/*22.33*/for(repositorys <-
		cacheResult.getSearches()) yield /*23.29*/{_display_(Seq[Any](format.raw/*23.30*/("""
		"""),format.raw/*24.3*/("""<h2>Search terms: """),_display_(/*24.22*/repositorys/*24.33*/.getQuery()),format.raw/*24.44*/("""</h2>
		<div style="margin-top: 3%">
			"""),_display_(/*26.5*/for(repository <- repositorys.getRepositorys()) yield /*26.52*/ {_display_(Seq[Any](format.raw/*26.54*/("""
			"""),format.raw/*27.4*/("""<div style="margin-bottom: 2%">
				<ul>
					<li><b>Owner Name:</b> """),_display_(/*29.30*/repository/*29.40*/.getOwnerName()),format.raw/*29.55*/(""" """),format.raw/*29.56*/("""&nbsp; |
						&nbsp;<b> Repository Name:</b> """),_display_(/*30.39*/repository/*30.49*/.getRepositoryName()),format.raw/*30.69*/("""
						"""),_display_(/*31.8*/if(!repository.getTopics().isEmpty())/*31.45*/{_display_(Seq[Any](format.raw/*31.46*/(""" """),format.raw/*31.47*/("""&nbsp; | &nbsp; """),_display_(/*31.64*/for(topic
						<- repository.getTopics()) yield /*32.33*/ {_display_(Seq[Any](format.raw/*32.35*/(""" """),format.raw/*32.36*/("""("""),_display_(/*32.38*/topic),format.raw/*32.43*/(""")&nbsp; """)))}),format.raw/*32.52*/(""" """)))}),format.raw/*32.54*/("""</li>
				</ul>
			</div>
			""")))}),format.raw/*35.5*/("""
		"""),format.raw/*36.3*/("""</div>
		<hr />
		""")))}),format.raw/*38.4*/(""" """)))}),format.raw/*38.6*/("""
	"""),format.raw/*39.2*/("""</div>
</div>
""")))}),format.raw/*41.2*/("""
"""))
      }
    }
  }

  def render(cacheResult:SearchCacheStore,error:String,style:String): play.twirl.api.HtmlFormat.Appendable = apply(cacheResult,error,style)

  def f:((SearchCacheStore,String,String) => play.twirl.api.HtmlFormat.Appendable) = (cacheResult,error,style) => apply(cacheResult,error,style)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  SOURCE: app/views/welcome.scala.html
                  HASH: 1f01f9c3eb2b8186fc0d3a3cb302a8a635ecf306
                  MATRIX: 933->1|1095->70|1122->72|1169->111|1219->124|1247->126|1700->553|1726->570|1766->572|1796->575|1835->587|1861->592|1900->601|1928->603|1960->626|2000->628|2029->630|2092->677|2131->678|2161->681|2207->700|2227->711|2259->722|2326->763|2389->810|2429->812|2460->816|2557->886|2576->896|2612->911|2641->912|2715->959|2734->969|2775->989|2809->997|2855->1034|2894->1035|2923->1036|2967->1053|3025->1095|3065->1097|3094->1098|3123->1100|3149->1105|3189->1114|3222->1116|3282->1146|3312->1149|3361->1168|3393->1170|3422->1172|3467->1187
                  LINES: 27->1|32->2|33->3|33->3|33->3|35->5|50->20|50->20|50->20|51->21|51->21|51->21|52->22|52->22|52->22|52->22|52->22|53->23|53->23|54->24|54->24|54->24|54->24|56->26|56->26|56->26|57->27|59->29|59->29|59->29|59->29|60->30|60->30|60->30|61->31|61->31|61->31|61->31|61->31|62->32|62->32|62->32|62->32|62->32|62->32|62->32|65->35|66->36|68->38|68->38|69->39|71->41
                  -- GENERATED --
              */
          