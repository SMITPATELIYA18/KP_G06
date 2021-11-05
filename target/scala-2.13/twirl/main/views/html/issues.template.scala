
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

object issues extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template2[IssueModel,AssetsFinder,play.twirl.api.HtmlFormat.Appendable] {

  /*
* This template takes a two arguments, a String containing a
* message to display and an AssetsFinder to locate static assets.
*/
  def apply/*5.2*/(issues: IssueModel)(implicit assetsFinder: AssetsFinder):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*5.59*/("""

"""),format.raw/*11.4*/("""
"""),_display_(/*12.2*/main("Issues")/*12.16*/ {_display_(Seq[Any](format.raw/*12.18*/("""

    """),format.raw/*17.8*/("""
    """),_display_(/*18.6*/issueDisplay(issues, style = "java")),format.raw/*18.42*/("""

""")))}),format.raw/*20.2*/("""
"""))
      }
    }
  }

  def render(issues:IssueModel,assetsFinder:AssetsFinder): play.twirl.api.HtmlFormat.Appendable = apply(issues)(assetsFinder)

  def f:((IssueModel) => (AssetsFinder) => play.twirl.api.HtmlFormat.Appendable) = (issues) => (assetsFinder) => apply(issues)(assetsFinder)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  SOURCE: app/views/issues.scala.html
                  HASH: 46577cd022cea37777d3b4e816aa54cd02244708
                  MATRIX: 1056->138|1208->195|1239->397|1268->400|1291->414|1331->416|1366->550|1399->557|1456->593|1491->598
                  LINES: 30->5|35->5|37->11|38->12|38->12|38->12|40->17|41->18|41->18|43->20
                  -- GENERATED --
              */
          