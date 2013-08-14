package templates

import scala.language.implicitConversions
import scalatags._
import play.api.templates.Html
import org.dupontmanual.forms.{ Binding, FormCall }
import _root_.config.users.{ Config, ProvidesInjector }
import controllers.users.VisitRequest
import models.users.User
import models.books.Title
import models.books.Copy
import play.api.Play
import com.google.inject.Inject
import java.io.File
import scala.xml.Unparsed

package object books {
  private[books] class ConfigProvider @Inject() (val config: Config)
  private[books] val injector = Play.current.global.asInstanceOf[ProvidesInjector].provideInjector()
  private[books] implicit lazy val config: Config = injector.getInstance(classOf[ConfigProvider]).config
  
  private[books] val focusFirstTextField = script(Unparsed("""$(document).ready(function() { $("input:text:first").focus(); })"""))

  def displayImage(isbn: String): scalatags.STag = {
    val path = "public/images/books/" + isbn + ".jpg"
    val url = "/assets/images/books/" + isbn + ".jpg"
    val f = new File(path)
    if (f.exists) {
      img.src(url).h("100px").w("100px")
    } else {
      StringSTag("")
    }
  }

  object addPurchaseGroup {
    def apply(addPurchaseGroupForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Add a Purchase Group")(addPurchaseGroupForm.render(), focusFirstTextField)
    }
  }

  object addTitle {
    def apply(titleForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Add Title to Inventory")(titleForm.render(), focusFirstTextField)
    }
  }

  object addTitleToPrintQueueHelper {
    def apply(addTitleToPrintQueueForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Add to Print Queue")(
        h2("Add a Copy Range to the Print Queue"),
        addTitleToPrintQueueForm.render(), focusFirstTextField)
    }
  }

  object allBooksOut {
    def apply(header: String, rows: List[(String, String, String)])(implicit req: VisitRequest[_], config: Config) = {
      config.main("All Books Out")(
        div.cls("page-header")(
          h2(header)), table.cls("table", "table-striped", "table-condensed")(
          thead(
            tr(
              th("Title"),
              th("Date Checked Out"),
              th("Student"))),
          tbody(
            rows.map { row =>
              tr(
                td(row._1),
                td(row._2),
                td(row._3))
            })))
    }
  }

  object checkIn {
    def apply(checkInForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Check in a Copy")(checkInForm.render(), focusFirstTextField)
    }
  }

  object checkInBulk {
    def apply(bulkCheckInForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Bulk Check In")(bulkCheckInForm.render(), focusFirstTextField)
    }
  }

  object checkout {
    def apply(checkoutForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Checkout a Copy")(checkoutForm.render(), focusFirstTextField)
    }
  }

  object checkoutBulk {
    def apply(checkoutBulkForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Checkout in Bulk")(checkoutBulkForm.render(), focusFirstTextField)
    }
  }

  object checkoutBulkHelper {
    def apply(addCopyForm: Binding, stu: String, bks: Vector[((String, String), Int)], stuNum: String)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Checkout Bulk")(
        div.cls("page-header")(
          h2(stu,
            div.cls("span3 pull-right")(
              div.cls("btn-group").attr(("style", "margin-left: auto; margin-right: auto; width: 180px"))(
                button.cls("btn btn-primary").ctype("button").onclick("window.location.href='/books/checkoutBulkSubmit/" + stuNum + "'")("Checkout"),
                button.cls("btn").ctype("button").onclick("window.location.href='/books/cancelBulkCheckout'")("Cancel"))))), div(), table.cls("table", "table-striped", "table-condensed")(
          thead(
            tr(
              th("Number"),
              th("Tittle"),
              th("Copy Number"),
              th(
                button.cls("btn").ctype("button").onclick("window.location.href='/books/removeAllCopiesFromList/" + stuNum + "'")("Remove All")))),
          tbody(
            bks.map { bk =>
              tr(
                td((bk._2 + 1).toString),
                td(Title.getByIsbn(bk._1._2).get.name),
                td(Copy.getByBarcode(bk._1._1).get.number.toString),
                td(button.cls("btn").ctype("button").onclick("window.location.href='/books/removeCopyFromList/" + stuNum + "/" + bk._1._1 + "'")("Remove")))
            })),
        div.cls("row")(addCopyForm.render()))
    }
  }

  object checkoutHistory {
    def apply(header: String, rows: List[(String, String, String, String)])(implicit req: VisitRequest[_], config: Config) = {
      config.main("Checkout History")(
        div.cls("page-header")(
          h2(header)), table.cls("table", "table-striped", "table-condensed")(
          thead(
            tr(
              th("Title"),
              th("Copy"),
              th("Date Checked Out"),
              th("Date returned"))),
          tbody(
            rows.map { row =>
              tr(
                td(row._1),
                td(row._2),
                td(row._3),
                td(row._4))
            })))
    }
  }

  object copyHistory {
    def apply(header: String, rows: List[(String, String, String)])(implicit req: VisitRequest[_], config: Config) = {
      config.main("Copy History")(
        div.cls("page-header")(
          h2(header)), table.cls("table", "table-striped", "table-condensed")(
          thead(
            tr(
              th("Student"),
              th("Date Checked Out"),
              th("Date returned"))),
          tbody(
            rows.map { row =>
              tr(
                td(row._1),
                td(row._2),
                td(row._3))
            })))
    }
  }

  object copyInfo {
    def apply(header: String, rows: List[(String, String)], isbn: String)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Copy Info")(
        div.cls("page-header row")(
          div.cls("span2").attr(("style", "margin-right: 0px; padding-right: 0px"))(displayImage(isbn)),
          h2.cls().attr(("style", "margin-top: 65px; margin-left: 0px; padding-left: 0px"))(header)),
        table.cls("table", "table-striped", "table-condensed")(
          tbody(
            rows.map { row =>
              tr(
                td(row._1),
                td(row._2))
            })))
    }
  }

  object copyStatusByTitle {
    def apply(header: String, rows: List[(String, String, String, String)])(implicit req: VisitRequest[_], config: Config) = {
      config.main("Copy Status By Title")(
        div.cls("page-header")(
          h2(header)), table.cls("table", "table-striped", "table-condensed")(
          thead(
            tr(
              th("Tittle"),
              th("Date Checked Out"))),
          tbody(
            rows.map { row: (String, String, String, String) =>
              if (row._4 == "true") {
                tr.cls("error deleted")(td(row._1), td("Deleted"))
              } else if (row._3 == "true") {
                tr.cls("error lost")(td(row._1), td("Lost"))
              } else if (row._2 == "true") {
                tr.cls("error checkedOut")(td(row._1), td("Checked Out"))
              } else {
                tr.cls("success checkedIn")(td(row._1), td("Checked In"))
              }
            })))
    }
  }

  object copyStatusByTitleForm {
    def apply()(implicit req: VisitRequest[_], config: Config) = {
      config.main("Not Done")()
    }
  }

  object currentCheckouts {
    def apply(header: String, rows: List[(String, String, String, String)])(implicit req: VisitRequest[_], config: Config) = {
      config.main("Current Checkouts")(
        div.cls("page-header")(
          h2(header)), table.cls("table", "table-striped", "table-condensed")(
          thead(
            tr(
              th("Title"),
              th("Copy"),
              th("Date Checked Out"))),
          tbody(
            rows.map { row =>
              tr(
                td(row._1),
                td(row._2),
                td(row._3),
                td(a.cls("btn btn-primary").href("/books/reportCopyLost/" + row._4)("Mark Lost")))
            })))
    }
  }

  object deleteCopy {
    def apply(chooseCopyForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Delete Copy")(chooseCopyForm.render(), focusFirstTextField)
    }
  }

  object deleteTitle {
    def apply(chooseTitleForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Delete Copy")(chooseTitleForm.render(), focusFirstTextField)
    }
  }

  object editTitle {
    def apply(chooseTitleForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Edit a Title")(chooseTitleForm.render(), focusFirstTextField)
    }
  }

  object editTitleHelper {
    def apply(editTitleForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Edit a Title")(editTitleForm.render(), focusFirstTextField)
    }
  }

  object findAllBooksOut {
    def apply(allBooksOutLookupForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Find All Books Out")(allBooksOutLookupForm.render(), focusFirstTextField)
    }
  }

  object findCheckoutHistory {
    def apply(roleHistoryLookupForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Find Checkout History")(roleHistoryLookupForm.render(), focusFirstTextField)
    }
  }

  object findCopyHistory {
    def apply(copyHistoryLookupForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Find Copy History")(copyHistoryLookupForm.render(), focusFirstTextField)
    }
  }

  object findCopyInfo {
    def apply(copyInfoLookupForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Find Copy Information")(copyInfoLookupForm.render(), focusFirstTextField)
    }
  }

  object findCopyStatusByTitle {
    def apply(copyStatusByTitleLookupForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Find Copy Status by Title")(copyStatusByTitleLookupForm.render(), focusFirstTextField)
    }
  }

  object findRoleHistory {
    def apply(roleHistoryLookupForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Find Role History")(roleHistoryLookupForm.render(), focusFirstTextField)
    }
  }

  object inventory {
    def apply(rows: List[(String, String, String, String, String)])(implicit req: VisitRequest[_], config: Config) = {
      config.main("Inventory")(
        div.cls("page-header")(
          h2("Inventory")), table.cls("table", "table-striped", "table-condensed")(
          thead(
            tr(
              th("Title"),
              th("Total Copies"),
              th("Copies Checked Out"),
              th("Copies Lost"),
              th("Copies In"))),
          tbody(
            rows.map { row =>
              tr(
                td(row._1),
                td(row._2),
                td(row._3),
                td(row._4),
                td(row._5))
            })))
    }
  }

  object printSectionsByDept {
    def apply(chooseDeptForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Print Section Barcodes by Department")(chooseDeptForm.render())
    }
  }

  object printSingleSection {
    def apply(chooseSectionForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Print Section Barcodes")(chooseSectionForm.render(), focusFirstTextField)
    }
  }

  object quickCheckout {
    def apply()(implicit req: VisitRequest[_], config: Config) = {
      config.main("Quick Checkout", 
          style("""ul#checkout-list {
            |  counter-reset:yourCounter;
    		|}
    		|ul#checkout-list li:not(.skip) {
    		|  counter-increment:yourCounter;
    		|  list-style:none;
    		|}
    		|ul#checkout-list li:not(.skip):before {
    		|  content:counter(yourCounter) '. ';
    		|}
            |ul#checkout-list li.skip {
            |   list-style: none;
            |}
    		|ul#checkout-list li.skip:before {
    		|  content:'\a0\a0\a0'; /* some white-space... optional */
    		|}""".stripMargin))(
        div.cls("page-header")(
          h2("Quick Checkout")),
        div.cls()(
          ul.id("checkout-list")(),
          form.cls("form-inline").id("checkout-form")(
            input.id("student").cls("form-control").attr("type" -> "text", "placeholder" -> "Student"),
            input.id("book").cls("form-control").attr("type" -> "text", "placeholder" -> "Barcode"),
            button.ctype("button").attr("onclick" -> "$('#student').val(''); $('#book').val(''); $('#student').focus();")("Clear"))),
        script(Unparsed("""
        |$("#student").keypress(function (event) {
        |    if (event.which == 13) {
        |      event.preventDefault();
        |      $("#book").select();
        |    }
        |  });
        |
        |$("#book").keypress(function (event) {
        |    if (event.which == 13) {
        |      event.preventDefault();
        |      var student = $("#student").val();
        |      var book = $("#book").val();
        |      $.ajax({
        |          type: 'GET',
        |          url: '/books/quickCheckoutHelper/' + student + '/' + book,
        |          success: function(result) {
        |            $("#checkout-list").append(result);
        |            $("#student").val("");
        |            $("#book").val("");
        |            $("#student").select();
        |          }
        |        });
        |    }
        |});
        |    
        |$(document).ready( function() { $('#student').select(); });
        """.stripMargin)))
    }
  }

  object viewPrintQueue {
    def apply(viewPrintQueueForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Print Queue")(viewPrintQueueForm.render())
    }
  }
}
