package usql
import com.github.vertical_blank.sqlformatter.SqlFormatter
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import pprint.PPrinter
import usql.query.{Expr, SubqueryRef}

class TestDb(name: String) {

//  private val DB_PORT: Int = 7777
//  private var pg: EmbeddedPostgres = EmbeddedPostgres.builder()
//    .setPort(DB_PORT)
//    .setDataDirectory((os.root / "tmp" / "my_unit_tests" / "data").toNIO)
//    .start()

  def camelToSnake(s: String) = {
    s.replaceAll("([A-Z])", "#$1").split('#').map(_.toLowerCase).mkString("_").stripPrefix("_")
  }

  def snakeToCamel(s: String) = {
    val out = new StringBuilder()
    val chunks = s.split("_", -1)
    for (i <- Range(0, chunks.length)) {
      val chunk = chunks(i)
      if (i == 0) out.append(chunk)
      else {
        out.append(chunk(0).toUpper)
        out.append(chunk.drop(1))
      }
    }
    out.toString()
  }

  println("Creating Test DB")
  Class.forName("org.sqlite.JDBC")
  val db = new DatabaseApi(
    java.sql.DriverManager.getConnection("jdbc:sqlite::memory:"),
    tableNameMapper = camelToSnake,
    tableNameUnMapper = snakeToCamel,
    columnNameMapper = camelToSnake,
    columnNameUnMapper = snakeToCamel
  )
  db.runRaw(os.read(os.pwd / "test" / "resources" / "sqlite-test-data.sql"))
  def apply[T, V](query: T, sql: String = null, value: V = null.asInstanceOf[V], normalize: V => V = null)(implicit qr: Queryable[T, V]) = {
    if (sql != null) {
      val sqlResult = db.toSqlQuery(query)
      val expectedSql = sql.trim.replaceAll("\\s+", " ")
      assert(sqlResult == expectedSql, pprint.apply(SqlFormatter.format(sqlResult)))
    }


    val result = db.run(query)
    // pprinter.log(result)
    if (value != null) {
      val normalized = if (normalize == null) result else normalize(result)
      assert(normalized == value, pprint.apply(normalized))
    }

  }
}

object TestDb {
  lazy val pprinter: PPrinter = PPrinter.Color.copy(
    additionalHandlers = {
      case v: Val[_] => pprinter.treeify(v.apply(), false, true)
      case v: SubqueryRef[_] => pprinter.treeify(v.value, false, true)
      case v: Expr[_] if !v.isInstanceOf[scala.Product] =>
        pprinter.treeify(v.exprToString, false, true)
    }
  )
}
