package scalasql.query

import scalasql.{Column, Queryable, query}

/**
 * A SQL `INSERT` query
 */
trait Insert[Q, R] extends WithExpr[Q] {
  def table: TableRef
  def qr: Queryable[Q, R]
  def select[C, R2](columns: Q => C, select: Select[C, R2]): InsertSelect[Q, C, R, R2]
  def values(f: (Q => Column.Assignment[_])*): InsertValues[Q, R]

  def batched[T1](f1: Q => Column.ColumnExpr[T1])(items: Expr[T1]*): InsertValues[Q, R]
  def batched[T1, T2](f1: Q => Column.ColumnExpr[T1], f2: Q => Column.ColumnExpr[T2])(
      items: (Expr[T1], Expr[T2])*
  ): InsertValues[Q, R]

  def batched[T1, T2, T3](
      f1: Q => Column.ColumnExpr[T1],
      f2: Q => Column.ColumnExpr[T2],
      f3: Q => Column.ColumnExpr[T3]
  )(items: (Expr[T1], Expr[T2], Expr[T3])*): InsertValues[Q, R]

  def batched[T1, T2, T3, T4](
      f1: Q => Column.ColumnExpr[T1],
      f2: Q => Column.ColumnExpr[T2],
      f3: Q => Column.ColumnExpr[T3],
      f4: Q => Column.ColumnExpr[T4]
  )(items: (Expr[T1], Expr[T2], Expr[T3], Expr[T4])*): InsertValues[Q, R]

  def batched[T1, T2, T3, T4, T5](
      f1: Q => Column.ColumnExpr[T1],
      f2: Q => Column.ColumnExpr[T2],
      f3: Q => Column.ColumnExpr[T3],
      f4: Q => Column.ColumnExpr[T4],
      f5: Q => Column.ColumnExpr[T5]
  )(items: (Expr[T1], Expr[T2], Expr[T3], Expr[T4], Expr[T5])*): InsertValues[Q, R]

  def batched[T1, T2, T3, T4, T5, T6](
      f1: Q => Column.ColumnExpr[T1],
      f2: Q => Column.ColumnExpr[T2],
      f3: Q => Column.ColumnExpr[T3],
      f4: Q => Column.ColumnExpr[T4],
      f5: Q => Column.ColumnExpr[T5],
      f6: Q => Column.ColumnExpr[T6]
  )(items: (Expr[T1], Expr[T2], Expr[T3], Expr[T4], Expr[T5], Expr[T6])*): InsertValues[Q, R]
}

object Insert {
  class Impl[Q, R](val expr: Q, val table: TableRef)(implicit val qr: Queryable[Q, R])
      extends Insert[Q, R] {

    def newInsertSelect[Q, C, R, R2](
        insert: Insert[Q, R],
        columns: C,
        select: Select[C, R2]
    ): InsertSelect[Q, C, R, R2] = { new InsertSelect.Impl(insert, columns, select) }

    def newInsertValues[Q, R](
        insert: Insert[Q, R],
        columns: Seq[Column.ColumnExpr[_]],
        valuesLists: Seq[Seq[Expr[_]]]
    )(implicit qr: Queryable[Q, R]) = { new InsertValues.Impl(insert, columns, valuesLists) }

    def select[C, R2](columns: Q => C, select: Select[C, R2]): InsertSelect[Q, C, R, R2] = {
      newInsertSelect(this, columns(expr), select)
    }

    def values(f: (Q => Column.Assignment[_])*): InsertValues[Q, R] = {
      val kvs = f.map(_(expr))
      newInsertValues(this, columns = kvs.map(_.column), valuesLists = Seq(kvs.map(_.value)))
    }

    def batched[T1](f1: Q => Column.ColumnExpr[T1])(items: Expr[T1]*): InsertValues[Q, R] = {
      newInsertValues(this, columns = Seq(f1(expr)), valuesLists = items.map(Seq(_)))
    }

    def batched[T1, T2](f1: Q => Column.ColumnExpr[T1], f2: Q => Column.ColumnExpr[T2])(
        items: (Expr[T1], Expr[T2])*
    ) = {
      newInsertValues(
        this,
        columns = Seq(f1(expr), f2(expr)),
        valuesLists = items.map(t => Seq(t._1, t._2))
      )
    }

    def batched[T1, T2, T3](
        f1: Q => Column.ColumnExpr[T1],
        f2: Q => Column.ColumnExpr[T2],
        f3: Q => Column.ColumnExpr[T3]
    )(items: (Expr[T1], Expr[T2], Expr[T3])*): InsertValues[Q, R] = {
      newInsertValues(
        this,
        columns = Seq(f1(expr), f2(expr), f3(expr)),
        valuesLists = items.map(t => Seq(t._1, t._2, t._3))
      )
    }

    def batched[T1, T2, T3, T4](
        f1: Q => Column.ColumnExpr[T1],
        f2: Q => Column.ColumnExpr[T2],
        f3: Q => Column.ColumnExpr[T3],
        f4: Q => Column.ColumnExpr[T4]
    )(items: (Expr[T1], Expr[T2], Expr[T3], Expr[T4])*): InsertValues[Q, R] = {
      newInsertValues(
        this,
        columns = Seq(f1(expr), f2(expr), f3(expr), f4(expr)),
        valuesLists = items.map(t => Seq(t._1, t._2, t._3, t._4))
      )
    }

    def batched[T1, T2, T3, T4, T5](
        f1: Q => Column.ColumnExpr[T1],
        f2: Q => Column.ColumnExpr[T2],
        f3: Q => Column.ColumnExpr[T3],
        f4: Q => Column.ColumnExpr[T4],
        f5: Q => Column.ColumnExpr[T5]
    )(items: (Expr[T1], Expr[T2], Expr[T3], Expr[T4], Expr[T5])*): InsertValues[Q, R] = {
      newInsertValues(
        this,
        columns = Seq(f1(expr), f2(expr), f3(expr), f4(expr), f5(expr)),
        valuesLists = items.map(t => Seq(t._1, t._2, t._3, t._4, t._5))
      )
    }

    def batched[T1, T2, T3, T4, T5, T6](
        f1: Q => Column.ColumnExpr[T1],
        f2: Q => Column.ColumnExpr[T2],
        f3: Q => Column.ColumnExpr[T3],
        f4: Q => Column.ColumnExpr[T4],
        f5: Q => Column.ColumnExpr[T5],
        f6: Q => Column.ColumnExpr[T6]
    )(items: (Expr[T1], Expr[T2], Expr[T3], Expr[T4], Expr[T5], Expr[T6])*): InsertValues[Q, R] = {

      newInsertValues(
        this,
        columns = Seq(f1(expr), f2(expr), f3(expr), f4(expr), f5(expr), f6(expr)),
        valuesLists = items.map(t => Seq(t._1, t._2, t._3, t._4, t._5, t._6))
      )
    }
  }
}