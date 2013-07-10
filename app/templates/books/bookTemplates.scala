package templates.books

import scala.language.implicitConversions
import scalatags._
import play.api.templates.Html
import forms.{ Binding, FormCall }
import config.users.MainTemplate
import controllers.users.VisitRequest
import models.users.User
import models.books.Title
import models.books.Copy

object addPurchaseGroup {
  def apply(main: MainTemplate, addPurchaseGroupForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Add a Purchase Group")(addPurchaseGroupForm.render())
  }
}

object addTitle {
  def apply(main: MainTemplate, titleForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Add Title to Inventory")(titleForm.render())
  }
}

object addTitleToPrintQueueHelper {
  def apply(main: MainTemplate, addTitleToPrintQueueForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Add to Print Queue")(
        h2("Add a Copy Range to the Print Queue"),
        addTitleToPrintQueueForm.render()
    )
  }
}

object allBooksOut {
  def apply(main: MainTemplate, header: String, rows: List[(String, String, String)])(implicit req: VisitRequest[_]) = {
    main("All Books Out")(
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
  def apply(main: MainTemplate, checkInForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Check in a Copy")(checkInForm.render())
  }
}

object checkout {
  def apply(main: MainTemplate, checkoutForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Checkout a Copy")(checkoutForm.render())
  }
}

object checkoutBulk {
  def apply(main: MainTemplate, checkoutBulkForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Checkout in Bulk")(checkoutBulkForm.render())
  }
}

object checkoutBulkHelper {
  def apply(main: MainTemplate,addCopyForm: Binding, stu: String, bks: Vector[((String, String), Int)], stuNum: String)(implicit req: VisitRequest[_]) = {
    main("Checkout Bulk")(
    	div.cls("page-header")(
    	    h2(stu)
    	),div(
    		button.cls("btn").attr("type" -> "button").onclick("window.location.href='/books/checkoutBulkSubmit/@stuNum'")("Submit"),
    		button.cls("btn").attr("type" -> "button").onclick("window.location.href='/books/cancelBulkCheckout'")("Cancel")
    	),table.cls("table", "table-striped", "table-condensed")(
            thead(
                tr(
                    th("Number"),
                    th("Tittle"),
                    th("Copy Number"),
                    th(
                    	button.cls("btn").attr("type" -> "button").onclick("window.location.href='/books/removeAllCopiesFromList/@stuNum'")("Remove All")
                    )
                )
            ),
            tbody(
                bks.map { bk =>
	            	tr(
	            	    td((bk._2 + 1).toString),
	            	    td(Title.getByIsbn(bk._1._2).get.name),
	            	    td(Copy.getByBarcode(bk._1._1).get.number.toString),
	            	    button.cls("btn").attr("type" -> "button").onclick("window.location.href='/books/removeCopyFromList/@stuNum/@bk._1._1'")("Remove")
	            	)
                }
            )
        ),
        addCopyForm.render()
    )
  }
}

object checkoutHistory {
  def apply(main: MainTemplate, header: String, rows: List[(String, String, String)])(implicit req: VisitRequest[_]) = {
    main("Checkout History")(
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
  def apply(main: MainTemplate, header: String, rows: List[(String, String, String)])(implicit req: VisitRequest[_]) = {
    main("Copy History")(
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
  def apply(main: MainTemplate, header: String, rows: List[(String, String)])(implicit req: VisitRequest[_]) = {
    main("Copy Info")(
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
  def apply(main: MainTemplate, header: String, rows: List[(String, String, String, String)])(implicit req: VisitRequest[_]) = {
    main("Copy Status By Title")(
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
  def apply(main: MainTemplate)(implicit req: VisitRequest[_]) = {
    main("Not Done")()
  }
}

object currentCheckouts {
  def apply(main: MainTemplate, header: String, rows: List[(String, String)])(implicit req: VisitRequest[_]) = {
    main("Current Checkouts")(
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
	            	    td(row._2)
	            	)
                }
            )
        )
    )
  }
}

object deleteCopyHelper {
  def apply(main: MainTemplate, chooseCopyForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Delete Copy")(chooseCopyForm.render())
  }
}

object deleteTitleHelper {
  def apply(main: MainTemplate, chooseTitleForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Delete Copy")(chooseTitleForm.render())
  }
}

object editTitle {
  def apply(main: MainTemplate, editTitleForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Edit a Title")(editTitleForm.render())
  }
}

object findAllbooksOut {
  def apply(main: MainTemplate, allBooksOutLookupForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Find All Books Out")(allBooksOutLookupForm.render())
  }
} 

object findCheckoutHistory {
  def apply(main: MainTemplate, roleHistoryLookupForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Find Checkout History")(roleHistoryLookupForm.render())
  }
} 

object findCopyHistory {
  def apply(main: MainTemplate, copyHistoryLookupForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Find Copy History")(copyHistoryLookupForm.render())
  }
} 

object findCopyInfo {
  def apply(main: MainTemplate, copyInfoLookupForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Find Copy Information")(copyInfoLookupForm.render())
  }
} 

object findCopyStatusByTitle {
  def apply(main: MainTemplate, copyStatusByTitleLookupForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Find Copy Status by Title")(copyStatusByTitleLookupForm.render())
  }
} 

object findRoleHistory {
  def apply(main: MainTemplate, roleHistoryLookupForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Find Role History")(roleHistoryLookupForm.render())
  }
} 

object inventory {
  def apply(main: MainTemplate, rows: List[(String, String, String, String, String)])(implicit req: VisitRequest[_]) = {
    main("Inventory")(
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

object viewPrintQueue {
  def apply(main: MainTemplate, rows: List[(String, String, String, Long)])(implicit req: VisitRequest[_]) = {
    main("Print Queue")(
        div.cls("page-header")(
            h2("Print Queue")
        ),table.cls("table", "table-striped", "table-condensed")(
            thead(
                tr(
                    th("Title Name"),
                    th("ISBN"),
                    th("Copy Range"),
                    th("")
                )
            ),
            tbody(
                rows.map { row =>
	            	tr(
	            	    td(row._1),
	            	    td(row._2),
	            	    td(row._3),
	            	    td(button.cls("btn").attr("type" -> "button").onclick("window.location.href='/books/removeFromPrintQueue/@row._4'")("Remove"))
	            	)
                }
            )
        ),
        button.cls("btn").attr("type" -> "button").onclick("printAllFunc();")("Print All"),
        script("""
            function printAllFunc() {
            	window.open("/books/printEntireQueue");
            	document.location.reload(true);
            }"""   )
    )
  }
}