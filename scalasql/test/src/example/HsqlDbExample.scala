package scalasql.example

import scalasql.Table

import java.sql.DriverManager
import scalasql.dialects.HsqlDbDialect._
object HsqlDbExample {

  case class ExampleProduct[+T[_]](
      id: T[Int],
      kebabCaseName: T[String],
      name: T[String],
      price: T[Double]
  )

  object ExampleProduct extends Table[ExampleProduct] {
    val metadata = initMetadata()
  }

  // The example HsqlDB database comes from the library `org.hsqldb:hsqldb:2.5.1`
  lazy val hsqlDbClient = new scalasql.DatabaseClient.Connection(
    DriverManager.getConnection("jdbc:hsqldb:mem:mydb"),
    dialectConfig = scalasql.dialects.HsqlDbDialect,
    config = new scalasql.Config {}
  )

  def main(args: Array[String]): Unit = {
    hsqlDbClient.transaction { db =>
      db.runRawUpdate("""
      CREATE TABLE example_product (
          id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY,
          kebab_case_name VARCHAR(256),
          name VARCHAR(256),
          price DECIMAL(20, 2)
      );
      """)

      val inserted = db.run(
        ExampleProduct.insert.batched(_.kebabCaseName, _.name, _.price)(
          ("face-mask", "Face Mask", 8.88),
          ("guitar", "Guitar", 300),
          ("socks", "Socks", 3.14),
          ("skate-board", "Skate Board", 123.45),
          ("camera", "Camera", 1000.00),
          ("cookie", "Cookie", 0.10)
        )
      )

      assert(inserted == 6)

      val result =
        db.run(ExampleProduct.select.filter(_.price > 10).sortBy(_.price).desc.map(_.name))

      assert(result == Seq("Camera", "Guitar", "Skate Board"))

      db.run(ExampleProduct.update(_.name === "Cookie").set(_.price := 11.0))

      db.run(ExampleProduct.delete(_.name === "Guitar"))

      val result2 =
        db.run(ExampleProduct.select.filter(_.price > 10).sortBy(_.price).desc.map(_.name))

      assert(result2 == Seq("Camera", "Skate Board", "Cookie"))
    }
  }
}