package scalasql.query

import scalasql._
import sourcecode.Text
import utest._
import utils.ScalaSqlSuite

trait ValuesTests extends ScalaSqlSuite {
  def description = "Basic `SELECT`` operations: map, filter, join, etc."

  def tests = Tests {
    test("basic") - checker(
      query = Text { values(Seq(1, 2, 3)) },
      sqls = Seq("VALUES (?), (?), (?)", "VALUES ROW(?), ROW(?), ROW(?)"),
      value = Seq(1, 2, 3),
      docs = """
        You can use `Values` to generate a SQL `VALUES` clause
      """
    )

    test("contains") - checker(
      query = Text { values(Seq(1, 2, 3)).contains(1) },
      sqls = Seq(
        "SELECT (? IN (VALUES (?), (?), (?))) AS res",
        "SELECT (? IN (VALUES ROW(?), ROW(?), ROW(?))) AS res"
      ),
      value = true,
      docs = """
        `Values` supports `.contains`
      """
    )

    test("max") - checker(
      query = Text { values(Seq(1, 2, 3)).max },
      sqls = Seq(
        "SELECT MAX(subquery0.column1) AS res FROM (VALUES (?), (?), (?)) subquery0",
        "SELECT MAX(subquery0.c1) AS res FROM (VALUES (?), (?), (?)) subquery0",
        "SELECT MAX(subquery0.column_0) AS res FROM (VALUES ROW(?), ROW(?), ROW(?)) subquery0"
      ),
      value = 3,
      docs = """
        `Values` supports aggregate functions like `.max`
      """
    )

    test("map") - checker(
      query = Text { values(Seq(1, 2, 3)).map(_ + 1) },
      sqls = Seq(
        "SELECT (subquery0.column1 + ?) AS res FROM (VALUES (?), (?), (?)) subquery0",
        "SELECT (subquery0.c1 + ?) AS res FROM (VALUES (?), (?), (?)) subquery0",
        "SELECT (subquery0.column_0 + ?) AS res FROM (VALUES ROW(?), ROW(?), ROW(?)) subquery0"
      ),
      value = Seq(2, 3, 4),
      docs = """
        `Values` supports most `.select` operators like `.map`, `.filter`, `.crossJoin`, and so on
      """
    )

    test("filter") - checker(
      query = Text { values(Seq(1, 2, 3)).filter(_ > 2) },
      sqls = Seq(
        "SELECT subquery0.column1 AS res FROM (VALUES (?), (?), (?)) subquery0 WHERE (subquery0.column1 > ?)",
        "SELECT subquery0.c1 AS res FROM (VALUES (?), (?), (?)) subquery0 WHERE (subquery0.c1 > ?)",
        "SELECT subquery0.column_0 AS res FROM (VALUES ROW(?), ROW(?), ROW(?)) subquery0 WHERE (subquery0.column_0 > ?)",
      ),
      value = Seq(3),
      docs = ""
    )


    test("crossJoin") - checker(
      query = Text {
        values(Seq(1, 2, 3)).crossJoin(values(Seq(4, 5, 6))).map{case (a, b) => (a * 10 + b)}
      },
      sqls = Seq(
        """
        SELECT ((subquery0.column1 * ?) + subquery1.column1) AS res
        FROM (VALUES (?), (?), (?)) subquery0
        CROSS JOIN (VALUES (?), (?), (?)) subquery1
        """,
        """
        SELECT ((subquery0.c1 * ?) + subquery1.c1) AS res
        FROM (VALUES (?), (?), (?)) subquery0
        CROSS JOIN (VALUES (?), (?), (?)) subquery1
        """,
        """
        SELECT ((subquery0.column_0 * ?) + subquery1.column_0) AS res
        FROM (VALUES ROW(?), ROW(?), ROW(?)) subquery0
        CROSS JOIN (VALUES ROW(?), ROW(?), ROW(?)) subquery1
        """,
      ),
      value = Seq(14, 15, 16, 24, 25, 26, 34, 35, 36),
      docs = "",
      normalize = (x: Seq[Int]) => x.sorted
    )

    test("joinValuesAndTable") - checker(
      query = Text {
        for{
          name <- values(Seq("Socks", "Face Mask", "Camera"))
          product <- Product.join(_.name === name)
        } yield (name, product.price)
      },
      sqls = Seq(
        """
        SELECT subquery0.column1 AS res__0, product1.price AS res__1
        FROM (VALUES (?), (?), (?)) subquery0
        JOIN product product1 ON (product1.name = subquery0.column1)
        """,
        """
        SELECT subquery0.c1 AS res__0, product1.price AS res__1
        FROM (VALUES (?), (?), (?)) subquery0
        JOIN product product1 ON (product1.name = subquery0.c1)
        """,
        """
        SELECT subquery0.column_0 AS res__0, product1.price AS res__1
        FROM (VALUES ROW(?), ROW(?), ROW(?)) subquery0
        JOIN product product1 ON (product1.name = subquery0.column_0)
        """,
      ),
      value = Seq(("Socks", 3.14), ("Face Mask", 8.88), ("Camera", 1000.0)),
      docs = "You can also mix `values` calls and normal `selects` in the same query, e.g. with joins",
      normalize = (x: Seq[(String, Double)]) => x.sortBy(_._2)
    )


  }
}