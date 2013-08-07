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

package object books {
  private[books] class ConfigProvider @Inject()(val config: Config)
  private[books] val injector = Play.current.global.asInstanceOf[ProvidesInjector].provideInjector()
  private[books] implicit lazy val config: Config = injector.getInstance(classOf[ConfigProvider]).config

  def displayImage(isbn: String) {
    val f = new File("/public/" + isbn)
    if (f.exists) {
      img.src("/public/" + isbn)
    } else {
      p() //TODO - this needs to be an empty STag
    }
  }

  object addPurchaseGroup {
    def apply(addPurchaseGroupForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Add a Purchase Group")(addPurchaseGroupForm.render())
    }
  }

  object addTitle {
    def apply(titleForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Add Title to Inventory")(titleForm.render())
    }
  }

  object addTitleToPrintQueueHelper {
    def apply(addTitleToPrintQueueForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Add to Print Queue")(
        h2("Add a Copy Range to the Print Queue"),
        addTitleToPrintQueueForm.render()
      )
    }
  }

  object allBooksOut {
    def apply(header: String, rows: List[(String, String, String)])(implicit req: VisitRequest[_], config: Config) = {
      config.main("All Books Out")(
        div.cls("page-header")(
          h2(header)
        ),table.cls("table", "table-striped", "table-condensed")(
        thead(
          tr(
            th("Title"),
            th("Date Checked Out"),
            th("Student")
          )
      ),
    tbody(
      rows.map { row => 
      tr(
        td(row._1),
        td(row._2),
        td(row._3)
      )
  }
)
        )
    )
}
}

object checkIn {
  def apply(checkInForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Check in a Copy")(checkInForm.render())
  }
}

object checkInBulk {
  def apply(bulkCheckInForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Bulk Check In")(bulkCheckInForm.render())
  }
}

object checkout {
  def apply(checkoutForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Checkout a Copy")(checkoutForm.render())
  }
}

object checkoutBulk {
  def apply(checkoutBulkForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Checkout in Bulk")(checkoutBulkForm.render())
  }
}

object checkoutBulkHelper {
  def apply(addCopyForm: Binding, stu: String, bks: Vector[((String, String), Int)], stuNum: String)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Checkout Bulk")(
      div.cls("page-header")(
        h2(stu)
      ),div(
    ),table.cls("table", "table-striped", "table-condensed")(
    thead(
      tr(
        th("Number"),
        th("Tittle"),
        th("Copy Number"),
        th(
          button.cls("btn").ctype("button").onclick("window.location.href='/books/removeAllCopiesFromList/" + stuNum + "'")("Remove All")
        )
    )
),
            tbody(
              bks.map { bk =>
              tr(
                td((bk._2 + 1).toString),
                td(Title.getByIsbn(bk._1._2).get.name),
                td(Copy.getByBarcode(bk._1._1).get.number.toString),
                td(button.cls("btn").ctype("button").onclick("window.location.href='/books/removeCopyFromList/" + stuNum + "/" + bk._1._1 + "'")("Remove"))
              )
          }
        )
    ),
  div.cls("row")(addCopyForm.render()),
  div.cls("span4 well")(
    button.cls("btn btn-primary").ctype("button").onclick("window.location.href='/books/checkoutBulkSubmit/" + stuNum + "'")("Checkout"),
    button.cls("btn").ctype("button").onclick("window.location.href='/books/cancelBulkCheckout'")("Cancel"))

)
  }
}

object checkoutHistory {
  def apply(header: String, rows: List[(String, String, String)])(implicit req: VisitRequest[_], config: Config) = {
    config.main("Checkout History")(
      div.cls("page-header")(
        h2(header)
      ),table.cls("table", "table-striped", "table-condensed")(
      thead(
        tr(
          th("Title"),
          th("Date Checked Out"),
          th("Date returned")
        )
    ),
  tbody(
    rows.map { row =>
    tr(
      td(row._1),
      td(row._2),
      td(row._3)
    )
}
            )
        )
    )
}
}

object copyHistory {
  def apply(header: String, rows: List[(String, String, String)])(implicit req: VisitRequest[_], config: Config) = {
    config.main("Copy History")(
      div.cls("page-header")(
        h2(header)
      ),table.cls("table", "table-striped", "table-condensed")(
      thead(
        tr(
          th("Student"),
          th("Date Checked Out"),
          th("Date returned")
        )
    ),
  tbody(
    rows.map { row =>
    tr(
      td(row._1),
      td(row._2),
      td(row._3)
    )
}
            )
        )
    )
}
}

object copyInfo {
  def apply(header: String, rows: List[(String, String)])(implicit req: VisitRequest[_], config: Config) = {
    config.main("Copy Info")(
      div.cls("page-header")(
        h2(header)
      ),table.cls("table", "table-striped", "table-condensed")(
      tbody(
        rows.map { row =>
        tr(
          td(row._1),
          td(row._2)
        )
    }
  )
        )
    )
}
}

object copyStatusByTitle {
  def apply(header: String, rows: List[(String, String, String, String)])(implicit req: VisitRequest[_], config: Config) = {
    config.main("Copy Status By Title")(
      div.cls("page-header")(
        h2(header)
      ),table.cls("table", "table-striped", "table-condensed")(
      thead(
        tr(
          th("Tittle"),
          th("Date Checked Out")
        )
    ),
  tbody(
    rows.map { row: (String, String, String, String) =>
    if (row._4  == "true") {
      tr.cls("error deleted")(td(row._1), td("Deleted"))
  } else if (row._3 == "true") {
    tr.cls("error lost")(td(row._1), td("Lost"))
} else if (row._2 == "true") {
  tr.cls("error checkedOut")(td(row._1),td("Checked Out"))
                } else {
                  tr.cls("success checkedIn")(td(row._1),td("Checked In"))
              }
            }
          )
      )
  )
  }
}

object copyStatusByTitleForm {
  def apply()(implicit req: VisitRequest[_], config: Config) = {
    config.main("Not Done")()
  }
}

object currentCheckouts {
  def apply(header: String, rows: List[(String, String, String)])(implicit req: VisitRequest[_], config: Config) = {
    config.main("Current Checkouts")(
      div.cls("page-header")(
        h2(header)
      ),table.cls("table", "table-striped", "table-condensed")(
      thead(
        tr(
          th("Title"),
          th("Date Checked Out")
        )
    ),
  tbody(
    rows.map { row =>
    tr(
      td(row._1),
      td(row._2),
      td(a.cls("btn btn-primary").href("/books/reportCopyLost/" + row._3)("Mark Lost"))
    )
}
            )
        )
    )
}
}

object deleteCopy {
  def apply(chooseCopyForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Delete Copy")(chooseCopyForm.render())
  }
}

object deleteTitle {
  def apply(chooseTitleForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Delete Copy")(chooseTitleForm.render())
  }
}

object editTitle {
  def apply(chooseTitleForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Edit a Title")(chooseTitleForm.render())
  }
}

object editTitleHelper {
  def apply(editTitleForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Edit a Title")(editTitleForm.render())
  }
}

object findAllBooksOut {
  def apply(allBooksOutLookupForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Find All Books Out")(allBooksOutLookupForm.render())
  }
} 

object findCheckoutHistory {
  def apply(roleHistoryLookupForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Find Checkout History")(roleHistoryLookupForm.render())
  }
} 

object findCopyHistory {
  def apply(copyHistoryLookupForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Find Copy History")(copyHistoryLookupForm.render())
  }
} 

object findCopyInfo {
  def apply(copyInfoLookupForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Find Copy Information")(copyInfoLookupForm.render())
  }
} 

object findCopyStatusByTitle {
  def apply(copyStatusByTitleLookupForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Find Copy Status by Title")(copyStatusByTitleLookupForm.render())
  }
} 

object findRoleHistory {
  def apply(roleHistoryLookupForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Find Role History")(roleHistoryLookupForm.render())
  }
} 

object inventory {
  def apply(rows: List[(String, String, String, String, String)])(implicit req: VisitRequest[_], config: Config) = {
    config.main("Inventory")(
      div.cls("page-header")(
        h2("Inventory")
      ),table.cls("table", "table-striped", "table-condensed")(
      thead(
        tr(
          th("Title"),
          th("Total Copies"),
          th("Copies Checked Out"),
          th("Copies Lost"),
          th("Copies In")
        )
    ),
  tbody(
    rows.map { row =>
    tr(
      td(row._1),
      td(row._2),
      td(row._3),
      td(row._4),
      td(row._5)
    )
}
            )
        )
    )
}
}

object printSectionsByDept {
  def apply(chooseDeptForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Print Section Barcodes by Department")(chooseDeptForm.render())
  }
}

object printSingleSection {
  def apply(chooseSectionForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Print Section Barcodes")(chooseSectionForm.render())
  }
}

object viewPrintQueue {
  def apply(viewPrintQueueForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Print Queue")(viewPrintQueueForm.render())
  }
}
}
