
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

object index extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template2[List[RepositoryModel],AssetsFinder,play.twirl.api.HtmlFormat.Appendable] {

  /*
* This template takes a two arguments, a String containing a
* message to display and an AssetsFinder to locate static assets.
*/
  def apply/*5.2*/(repositorys: List[RepositoryModel])(implicit assetsFinder: AssetsFinder):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*6.1*/("""
"""),format.raw/*11.4*/("""
"""),_display_(/*12.2*/main("Gitterific")/*12.20*/ {_display_(Seq[Any](format.raw/*12.22*/("""

    """),format.raw/*17.8*/("""
    """),_display_(/*18.6*/welcome(repositorys, style = "java")),format.raw/*18.42*/("""

""")))}),format.raw/*20.2*/("""
"""))
      }
    }
  }

  def render(repositorys:List[RepositoryModel],assetsFinder:AssetsFinder): play.twirl.api.HtmlFormat.Appendable = apply(repositorys)(assetsFinder)

  def f:((List[RepositoryModel]) => (AssetsFinder) => play.twirl.api.HtmlFormat.Appendable) = (repositorys) => (assetsFinder) => apply(repositorys)(assetsFinder)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  SOURCE: app/views/index.scala.html
                  HASH: 3c46b793af80ae55181285c57a5cc9411d6aa095
                  MATRIX: 1063->134|1230->208|1258->403|1286->405|1313->423|1353->425|1386->554|1418->560|1475->596|1508->599
                  LINES: 30->5|35->6|36->11|37->12|37->12|37->12|39->17|40->18|40->18|42->20
                  -- GENERATED --
              */
          