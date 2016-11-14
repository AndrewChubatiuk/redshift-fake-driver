package org.h2.jdbc

import java.sql.{Statement, PreparedStatement}
import java.util.Properties

import jp.ne.opt.redshiftfake.{PreparedStatementType, RedshiftFakePreparedStatement}

class RedshiftFakeH2Connection(url: String, info: Properties) extends JdbcConnection(url, info) {

  //========================
  // Intercept Statement
  //========================
  override def createStatement(): Statement = super.createStatement()

  override def createStatement(resultSetType: Int, resultSetConcurrency: Int): Statement = super.createStatement(resultSetType, resultSetConcurrency)

  override def createStatement(resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): Statement = super.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability)

  //========================
  // Intercept PreparedStatement
  //========================
  override def prepareStatement(sql: String): PreparedStatement =
    new RedshiftFakePreparedStatement(super.prepareStatement(sql), sql, this, PreparedStatementType.Plain)

  override def prepareStatement(sql: String, columnNames: Array[String]): PreparedStatement =
    new RedshiftFakePreparedStatement(super.prepareStatement(sql, columnNames), sql, this, PreparedStatementType.ColumnNames(columnNames))

  override def prepareStatement(sql: String, resultSetType: Int, resultSetConcurrency: Int): PreparedStatement =
    new RedshiftFakePreparedStatement(
      super.prepareStatement(sql, resultSetType, resultSetConcurrency),
      sql, this,
      PreparedStatementType.ResultSetTypeConcurrency(resultSetType, resultSetConcurrency))

  override def prepareStatement(sql: String, resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): PreparedStatement =
    new RedshiftFakePreparedStatement(
      super.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability),
      sql, this,
      PreparedStatementType.ResultSetTypeConcurrencyHoldability(resultSetType, resultSetConcurrency, resultSetHoldability))

  override def prepareStatement(sql: String, autoGeneratedKeys: Int): PreparedStatement =
    new RedshiftFakePreparedStatement(super.prepareStatement(sql, autoGeneratedKeys), sql, this, PreparedStatementType.AutoGeneratedKeys(autoGeneratedKeys))

  override def prepareStatement(sql: String, columnIndexes: Array[Int]): PreparedStatement =
    new RedshiftFakePreparedStatement(super.prepareStatement(sql, columnIndexes), sql, this, PreparedStatementType.ColumnIndexes(columnIndexes))

  override def prepareAutoCloseStatement(sql: String): PreparedStatement =
    new RedshiftFakePreparedStatement(super.prepareAutoCloseStatement(sql), sql, this, PreparedStatementType.Plain)
}
