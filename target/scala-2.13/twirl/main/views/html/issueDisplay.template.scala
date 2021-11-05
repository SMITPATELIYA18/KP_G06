
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

object issueDisplay extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template2[IssueModel,String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(issues: IssueModel,style: String = "java"):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.45*/("""

"""),_display_(/*3.2*/defining(play.core.PlayVersion.current)/*3.41*/ { version =>_display_(Seq[Any](format.raw/*3.54*/("""
"""),format.raw/*4.1*/("""<section id="top">
	<div class="wrapper">
		<h1>Gitterific</h1>
	</div>
</section>

<div
	style="margin-left: 18%; margin-right: 18%; margin-bottom: 1%; margin-top: 1%;">
	<div style="padding: 10px 0px;">
		<h1>A word-level statistics of the issue titles</h1>
		<span>
			<i>(by frequency of the words in descending order)</i>
		</span>
	</div>
	<div>
		<h2 style="margin-bottom: 3%">Repository Name:
			"""),_display_(/*20.5*/issues/*20.11*/.getRepoFullName()),format.raw/*20.29*/("""</h2>
		"""),_display_(/*21.4*/for(words <- {issues.getWordLevelData()}) yield /*21.45*/ {_display_(Seq[Any](format.raw/*21.47*/("""
		"""),format.raw/*22.3*/("""<div style="margin-bottom: 2%">
			<ul>
				<li><b>"""),_display_(/*24.13*/{words._1}),format.raw/*24.23*/("""</b> : : """),_display_(/*24.33*/{words._2}),format.raw/*24.43*/("""</li>
			</ul>
		</div>
		""")))}),format.raw/*27.4*/("""
	"""),format.raw/*28.2*/("""</div>
</div>
""")))}),format.raw/*30.2*/("""
"""))
      }
    }
  }

  def render(issues:IssueModel,style:String): play.twirl.api.HtmlFormat.Appendable = apply(issues,style)

  def f:((IssueModel,String) => play.twirl.api.HtmlFormat.Appendable) = (issues,style) => apply(issues,style)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  SOURCE: app/views/issueDisplay.scala.html
                  HASH: cc438b7da36ebcfbbeb9a660b03157e0f6177cf6
                  MATRIX: 925->1|1063->44|1093->49|1140->88|1190->101|1218->103|1665->524|1680->530|1719->548|1755->558|1812->599|1852->601|1883->605|1964->659|1995->669|2032->679|2063->689|2123->719|2153->722|2200->739
                  LINES: 27->1|32->1|34->3|34->3|34->3|35->4|51->20|51->20|51->20|52->21|52->21|52->21|53->22|55->24|55->24|55->24|55->24|58->27|59->28|61->30
                  -- GENERATED --
              */
          