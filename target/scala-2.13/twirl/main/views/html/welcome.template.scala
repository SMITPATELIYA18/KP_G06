
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

object welcome extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template2[List[RepositoryModel],String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(repositorys: List[RepositoryModel], style: String = "java"):play.twirl.api.HtmlFormat.Appendable = {
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
		<form id="form">
			<input type="search" id="searchField"
				placeholder="Enter Search Terms" />
			<button type="submit" name="GO!">Go!</button>
		</form>
	</div>
	<div>
		<h2>Search terms: JAVA Play Framework</h2>
		<div style="margin-top: 3%">
			"""),_display_(/*22.5*/for(repository <- repositorys) yield /*22.35*/ {_display_(Seq[Any](format.raw/*22.37*/("""
			"""),format.raw/*23.4*/("""<div style="margin-bottom: 2%">
				<li>Owner Name: """),_display_(/*24.22*/repository/*24.32*/.getOwnerName()),format.raw/*24.47*/(""" """),format.raw/*24.48*/("""&nbsp; Repository
					Name: """),_display_(/*25.13*/repository/*25.23*/.getRepositoryName()),format.raw/*25.43*/("""
				"""),_display_(/*26.6*/for(topic <- repository.getTopics()) yield /*26.42*/ {_display_(Seq[Any](format.raw/*26.44*/("""
					"""),format.raw/*27.6*/("""("""),_display_(/*27.8*/topic),format.raw/*27.13*/(""")&nbsp;
				""")))}),format.raw/*28.6*/("""	
				"""),format.raw/*29.5*/("""</li>
			</div>
			""")))}),format.raw/*31.5*/("""
		"""),format.raw/*32.3*/("""</div>
		<hr/>
	</div>
</div>
""")))}),format.raw/*36.2*/("""
"""))
      }
    }
  }

  def render(repositorys:List[RepositoryModel],style:String): play.twirl.api.HtmlFormat.Appendable = apply(repositorys,style)

  def f:((List[RepositoryModel],String) => play.twirl.api.HtmlFormat.Appendable) = (repositorys,style) => apply(repositorys,style)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  SOURCE: app/views/welcome.scala.html
                  HASH: 8452b99593b8af02eaaa2ed46dad1a3efafffef5
                  MATRIX: 931->1|1085->62|1112->64|1159->103|1209->116|1237->118|1740->595|1786->625|1826->627|1857->631|1937->684|1956->694|1992->709|2021->710|2078->740|2097->750|2138->770|2170->776|2222->812|2262->814|2295->820|2323->822|2349->827|2392->840|2425->846|2475->866|2505->869|2566->900
                  LINES: 27->1|32->2|33->3|33->3|33->3|35->5|52->22|52->22|52->22|53->23|54->24|54->24|54->24|54->24|55->25|55->25|55->25|56->26|56->26|56->26|57->27|57->27|57->27|58->28|59->29|61->31|62->32|66->36
                  -- GENERATED --
              */
          