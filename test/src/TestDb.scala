package usql
import pprint.PPrinter

class TestDb(name: String) {
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
  db.runRaw(os.read(os.pwd / "test" / "resources" / "customers.sql"))
  def apply[T, V](query: T)(implicit qr: Queryable[T, V])  = new Apply(query)
  class Apply[T, V](query: T)(implicit qr: Queryable[T, V]) {
    def expect(sql: String = null, value: V) = {
      if (sql != null){
        val sqlResult = db.toSqlQuery(query)
        //       pprint.log(sqlResult)
        val expectedSql = sql.trim.replaceAll("\\s+", " ")
        assert(sqlResult == expectedSql, pprint.apply(sqlResult))
      }

      val result = db.run(query)
      lazy val pprinter: PPrinter = PPrinter.Color.copy(
        additionalHandlers = {
          case v: Val[_] => pprinter.treeify(v.apply(), false, true)
        }
      )
      // pprinter.log(result)
      assert(result == value, pprint.apply(result))
    }
  }
}